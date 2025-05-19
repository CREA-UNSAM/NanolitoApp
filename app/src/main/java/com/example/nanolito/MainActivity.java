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
    private BluetoothDevice esp32;
    private UUID espUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothService service;

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

        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch(msg.what) {
                    default:
                        String espMsg = msg.obj.toString();
                        addressText.setText(espMsg);
                        break;
                }
            }
        };

        if(address != null) {

            esp32 = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
            service = new BluetoothService(handler);
            service.start(esp32);
        }
    }

    public void launchSettings(View v) {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    public void sendMessage(View v)  {
        EditText text = findViewById(R.id.textBox);
        try{
            service.sendMessage(text.getText().toString());
        } catch (IOException e) {
            Log.e(TAG, "Bluetooth Connection not started", e);
        }
        text.setText("");
    }
}