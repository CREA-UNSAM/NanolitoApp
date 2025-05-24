package com.example.nanolito;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Logs";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice esp32;
    private BluetoothService service;
    private Button pidButton;
    private CompoundButton stateButton;
    private final EditText[] pidTexts = new EditText[3];
    private final ImageButton[] increaseButtons = new ImageButton[3];
    private final ImageButton[] decreaseButtons = new ImageButton[3];
    private boolean bluetoothState;
    private final TextView[] sensores = new TextView[11];
    private final int UMBRAL = 512;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pidTexts[0] = findViewById(R.id.PText);
        pidTexts[1] = findViewById(R.id.IText);
        pidTexts[2] = findViewById(R.id.DText);

        increaseButtons[0] = findViewById(R.id.increaseButtonP);
        increaseButtons[1] = findViewById(R.id.increaseButtonI);
        increaseButtons[2] = findViewById(R.id.increaseButtonD);

        decreaseButtons[0] = findViewById(R.id.decreaseButtonP);
        decreaseButtons[1] = findViewById(R.id.decreaseButtonI);
        decreaseButtons[2] = findViewById(R.id.decreaseButtonD);

        sensores[0] = findViewById(R.id.sens1);
        sensores[1] = findViewById(R.id.sens2);
        sensores[2] = findViewById(R.id.sens3);
        sensores[3] = findViewById(R.id.sens4);
        sensores[4] = findViewById(R.id.sens5);
        sensores[5] = findViewById(R.id.sens6);
        sensores[6] = findViewById(R.id.sens7);
        sensores[7] = findViewById(R.id.sens8);
        sensores[8] = findViewById(R.id.sens9);
        sensores[9] = findViewById(R.id.sens10);
        sensores[10] = findViewById(R.id.sens11);

        setupButtons();

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

        BluetoothPermissionHelper bluetoothPermissionHelper = new BluetoothPermissionHelper(this);
        bluetoothPermissionHelper.setOnBluetoothPermissionsGrantedListener(this::setupBluetoothComs);
        bluetoothPermissionHelper.askForBluetoothPermissions();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (bluetoothAdapter == null) {
            return;
        }

        if (!bluetoothAdapter.isEnabled())
        {

        }

        setupBluetoothComs();
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (service != null) {
            service.stop();
        }
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MessageConstants.MESSAGE_READ:
                    String espMsg = msg.obj.toString();
                    Log.i(TAG, "Received: " + espMsg);
                    processMessage(espMsg);
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

    private void processMessage(String message) {
        char command = message.charAt(0);
        String[] valuesStr = message.split(":");
        switch(command) {
            case 'A':
                pidTexts[0].setText(valuesStr[1]);
                pidTexts[1].setText(valuesStr[2]);
                pidTexts[2].setText(valuesStr[3]);
                break;
            case 'B':
                for(int i = 1; i < valuesStr.length; i++) {
                    sensores[i - 1].setText(valuesStr[i]);
                    int valor = Integer.parseInt(valuesStr[i]);
                    if (valor > UMBRAL) {
                        sensores[i - 1].setBackgroundColor(Color.BLACK);
                        sensores[i - 1].setTextColor(Color.WHITE);
                    } else {
                        sensores[i - 1].setBackgroundColor(Color.WHITE);
                        sensores[i - 1].setTextColor(Color.BLACK);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void setupButtons() {
        for (int i = 0; i < 3; i++) {
            final int index = i; // final for inner class access

            increaseButtons[i].setOnClickListener(v -> {
                changeTextValue(pidTexts[index], +1);
            });

            decreaseButtons[i].setOnClickListener(v -> {
                changeTextValue(pidTexts[index], -1);
            });
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
        float pValue =  Float.parseFloat(pidTexts[0].getText().toString());
        float iValue =  Float.parseFloat(pidTexts[1].getText().toString());
        float dValue =  Float.parseFloat(pidTexts[2].getText().toString());

        try{
            service.sendMessage("a:" + pValue + ":" + iValue + ":" + dValue);
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
        stateButton.setChecked(bluetoothState);
        pidButton.setEnabled(bluetoothState);

        for (int i = 0; i < 3; i++) {
            pidTexts[i].setEnabled(bluetoothState);
            increaseButtons[i].setEnabled(bluetoothState);
            decreaseButtons[i].setEnabled(bluetoothState);
        }
    }

    private void changeTextValue(EditText field, float value) {
        float preValue = Float.parseFloat(field.getText().toString());
        field.setText(Float.toString(preValue + value));
    }
}