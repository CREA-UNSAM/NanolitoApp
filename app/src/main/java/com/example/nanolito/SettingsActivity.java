package com.example.nanolito;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
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

        sharedPref = getSharedPreferences("NanolitoSettings", Context.MODE_PRIVATE);

        if(BluetoothAdapter.getDefaultAdapter() == null){
            Toast.makeText(this, "Bluetooth not working", Toast.LENGTH_LONG).show();
        }

        if(!BluetoothHelper.isEnabled()){
            BluetoothHelper.enableBluetooth(this);
        }
        else {
            Set<BluetoothDevice> devices = BluetoothHelper.getPairedDevices();
            List<BluetoothDevice> deviceList = new ArrayList<>(devices);

            RecyclerView deviceRecyclerView = findViewById(R.id.deviceList);
            deviceRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            BluetoothDeviceAdapter adapter = new BluetoothDeviceAdapter(deviceList, sharedPref);

            deviceRecyclerView.setAdapter(adapter);
        }

    }
}