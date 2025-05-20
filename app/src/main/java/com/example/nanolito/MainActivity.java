package com.example.nanolito;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.io.IOException;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Logs";
    public static Handler handler;
    private final static int ERROR_READ = 0;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice esp32;
    private BluetoothService service;
    private Button pidButton;
    private CompoundButton stateButton;
    private EditText pText;
    private EditText iText;
    private EditText dText;

    private boolean bluetoothState;

    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pText = findViewById(R.id.PText);
        iText = findViewById(R.id.IText);
        dText = findViewById(R.id.DText);

        pidButton = findViewById(R.id.PIDButton);
        stateButton = findViewById(R.id.connectionSwitch);
        stateButton.setChecked(bluetoothState);
        setBluetoothState(false);

        sharedPref = getSharedPreferences("NanolitoSettings", Context.MODE_PRIVATE);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            this.finish();
        }

        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch(msg.what) {
                    case MessageConstants.MESSAGE_READ:
                        String espMsg = msg.obj.toString();
                        Log.i(TAG, "received: " + espMsg);
                        break;
                    case MessageConstants.MESSAGE_TOAST:
                        String text = (String) msg.getData().get(MessageConstants.TOAST);
                        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                        break;
                    case MessageConstants.MESSAGE_STATE_CHANGE:
                        handleBluetoothState(msg.arg1);
                        break;
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();

        if (bluetoothAdapter == null) {
            return;
        }

        if (!bluetoothAdapter.isEnabled())
        {
            BluetoothPermissionHelper bluetoothPermissionHelper = new BluetoothPermissionHelper(this);
            bluetoothPermissionHelper.setOnBluetoothPermissionsGrantedListener(this::setupBluetoothComs);
            bluetoothPermissionHelper.askForBluetoothPermissions();
        }

        setupBluetoothComs();
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override public void onDestroy() {
        super.onDestroy();
        if (service != null) {
            service.stop();
        }
    }

    private void setupBluetoothComs() {
        String address = sharedPref.getString("deviceAddress", null);
        if(address != null) {

            esp32 = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
            service = new BluetoothService(handler);
        }
        stateButton.setEnabled(address != null);
    }

    private void handleBluetoothState(final int state) {
        switch(state) {
            case BluetoothService.STATE_CONNECTED:
                setBluetoothState(true);
                break;
            case BluetoothService.STATE_CONNECTING:
            case BluetoothService.STATE_NONE:
                setBluetoothState(false);
                break;
        }
    }

    public void launchSettings(View v) {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    public void sendPID(View v) {
        float pValue =  Float.parseFloat(pText.getText().toString());
        float iValue =  Float.parseFloat(iText.getText().toString());
        float dValue =  Float.parseFloat(dText.getText().toString());

        try{
            service.sendMessage("p:" + pValue + "|i:" + iValue + "|d:" + dValue);
        } catch (IOException e) {
            Log.e(TAG, "Bluetooth Connection not started", e);
        }
    }

    public void onConnectionSwitch(View v) {
        if(stateButton.isChecked()) {
            if (service != null) {
                service.start(esp32);
            }
        } else {
            if (service != null) {
                service.stop();
            }
        }

        Log.i(TAG, "Connection switch pressed: " + (stateButton.isChecked() ? "ON" : "OFF"));
    }

    private void setBluetoothState(boolean state) {
        bluetoothState = state;
        pidButton.setEnabled(bluetoothState);
        pText.setEnabled(bluetoothState);
        iText.setEnabled(bluetoothState);
        dText.setEnabled(bluetoothState);
    }
}