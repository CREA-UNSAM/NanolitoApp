package com.example.nanolito;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class SettingsActivity extends AppCompatActivity {
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if(!BluetoothHelper.isEnabled()){
            BluetoothHelper.enableBluetooth(this);
        }
        else {
            RecyclerView deviceRecyclerView = findViewById(R.id.deviceList);
            Set<BluetoothDevice> devices = BluetoothHelper.getPairedDevices();
            List<BluetoothDevice> deviceList = new ArrayList<>(devices);

            DeviceAdapter adapter = new DeviceAdapter(deviceList, device -> {
                Toast.makeText(this, "Clicked: " + device.getName(), Toast.LENGTH_SHORT).show();
            });

            deviceRecyclerView.setAdapter(adapter);
        }

        sharedPref = getSharedPreferences("NanolitoSettings", Context.MODE_PRIVATE);

    }
}