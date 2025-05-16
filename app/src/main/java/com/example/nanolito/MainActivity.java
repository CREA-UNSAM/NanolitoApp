package com.example.nanolito;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainActivity extends AppCompatActivity {
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothPermissionHelper bluetoothPermissionHelper = new BluetoothPermissionHelper(this);
        bluetoothPermissionHelper.askForBluetoothPermissions();

        sharedPref = getSharedPreferences("NanolitoSettings", Context.MODE_PRIVATE);

        String address = sharedPref.getString("deviceAddress", null);
        TextView addressText = findViewById(R.id.testAddress);
        addressText.setText(address == null ? "Selecciona un dispositivo" : address);
    }

    public void launchSettings(View v){
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }
}