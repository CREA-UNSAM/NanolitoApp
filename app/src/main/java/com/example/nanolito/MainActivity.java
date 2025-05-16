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
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothPermissionHelper bluetoothPermissionHelper = new BluetoothPermissionHelper(this);
        bluetoothPermissionHelper.askForBluetoothPermissions();

        SharedPreferences sharedPref = getSharedPreferences("NanolitoSettings", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

    }

    public void launchSettings(View v){
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    public void changePref(View v){
        TextView text = findViewById(R.id.textBox);
        editor.putString("test", text.getText().toString());
        editor.apply();
    }
}