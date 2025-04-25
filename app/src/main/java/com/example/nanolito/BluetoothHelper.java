package com.example.nanolito;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.Manifest;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Map;
import java.util.Set;

public class BluetoothHelper {
    private static BluetoothHelper INSTANCE;
    public static final int REQUEST_ENABLE_BLUETOOTH = 2;
    private static BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    private BluetoothHelper() {

    }

    public static void resetAdapter(){
        adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static boolean isAvailable(){
        return adapter == null;
    }

    public static void enableBluetooth(Activity activity){
        if(!adapter.isEnabled()){
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(i, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    public static boolean isEnabled(){
        return adapter.isEnabled();
    }

    public static void disableBluetooth(){
        adapter.disable();
    }

    public static Set<BluetoothDevice> getPairedDevices(){
        return adapter.getBondedDevices();
    }

    public static BluetoothHelper getInstance(){
        if (INSTANCE == null) {
            INSTANCE = new BluetoothHelper();
        }
        return INSTANCE;
    }
}
