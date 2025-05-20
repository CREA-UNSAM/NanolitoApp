package com.example.nanolito;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BluetoothService {
    private static final String TAG = "BluetoothLog";
    private final Handler handler;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private int state;

    public static final int STATE_NONE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public BluetoothService(@NonNull Handler handler) {
        this.handler = handler;
        this.state = STATE_NONE;
    }

    public synchronized void start(BluetoothDevice device) {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        connectThread = new ConnectThread(device, device.getUuids()[0].getUuid());
        connectThread.start();

        notifyState();
    }

    public synchronized void stop() {
        Log.d(TAG, "Stopping Bluetooth Service");

        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        state = STATE_NONE;
    }

    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        connectedThread = new ConnectedThread(socket);
        connectedThread.start();

        notifyState();
    }

    public synchronized int getState() {
        return state;
    }

    public synchronized void sendMessage(String msg) throws IOException {
        if (connectedThread == null) {
            throw new IOException("No connected device.");
        }

        connectedThread.write((msg + "\n").getBytes());
    }

    private synchronized void notifyState() {
        handler.obtainMessage(MessageConstants.MESSAGE_STATE_CHANGE,
                state, -1).sendToTarget();
    }

    private void connectionFailed() {
        Message msg = handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MessageConstants.TOAST, "Unable to connect device");
        msg.setData(bundle);
        handler.sendMessage(msg);

        state = STATE_NONE;

        notifyState();
    }

    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MessageConstants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        handler.sendMessage(msg);

        state = STATE_NONE;

        notifyState();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer;
        public ConnectedThread(@NonNull BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            state = STATE_CONNECTED;
            notifyState();
            Log.i(TAG, "Successfully established connected thread.");
            write("ping\n".getBytes());
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes;

            Log.i(TAG, "Input stream is now open.");

            while (state == STATE_CONNECTED) {
                try {
                    numBytes = mmInStream.read(mmBuffer);
                    Message readMsg = handler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            new String(mmBuffer, 0, numBytes));
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                Message writtenMsg = handler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
                Message writeErrorMsg = handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast", "Couldn't send data to the other device.");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    public class ConnectThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        private static final String TAG = "ThreadLogs";
        private final static int ERROR_READ = 0;

        @SuppressLint("MissingPermission")
        public ConnectThread(BluetoothDevice device, UUID MY_UUID) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch(IOException e) {
                Log.e(TAG, "Socket's create() method failed, e");
            }
            mmSocket = tmp;
            state = STATE_CONNECTING;
            notifyState();
        }

        @SuppressLint("MissingPermission")
        public void run() {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

            try {
                mmSocket.connect();
            } catch(IOException connectException) {
                handler.obtainMessage(ERROR_READ, "Unable to connect to the BT device").sendToTarget();
                cancel();
                connectionFailed();
                return;
            }

            Log.i(TAG, "Socket connected");
            connected(mmSocket, mmDevice);
            // manageMyConnectedSocket(mmSocket);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }

        public BluetoothSocket getSocket() {
            return mmSocket;
        }
    }
}
