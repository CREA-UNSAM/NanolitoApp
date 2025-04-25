package com.example.nanolito;

import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import java.util.ArrayList;
import java.util.Map;

public class BluetoothPermissionHelper {

    private final FragmentActivity activity;
    private final ActivityResultLauncher<String[]> requestBluetoothPermissionsLauncher;
    private OnBluetoothPermissionsGrantedListener listener;

    public interface OnBluetoothPermissionsGrantedListener {
        void onBluetoothPermissionsGranted();
    }

    public void setOnBluetoothPermissionsGrantedListener(OnBluetoothPermissionsGrantedListener listener) {
        this.listener = listener;
    }

    public BluetoothPermissionHelper(FragmentActivity activity) {
        this.activity = activity;
        this.requestBluetoothPermissionsLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::handleBluetoothPermissionsResult
        );
    }

    private void handleBluetoothPermissionsResult(Map<String, Boolean> permissionsResult) {
        boolean allGranted = true;
        for (Map.Entry<String, Boolean> entry : permissionsResult.entrySet()) {
            String permissionName = entry.getKey();
            Boolean isGranted = entry.getValue();
            if (isGranted) {
                Toast.makeText(activity, permissionName + " granted", Toast.LENGTH_SHORT).show();
                turnBluetoothOn();
            } else {
                allGranted = false;
                if (activity.shouldShowRequestPermissionRationale(permissionName)) {
                    Toast.makeText(activity, "Bluetooth permission is needed to connect to devices.", Toast.LENGTH_LONG).show();
                    requestBluetoothPermissions();
                } else {
                    Toast.makeText(activity, permissionName + " permanently denied. Please enable it in app settings.", Toast.LENGTH_LONG).show();
                }
                break; // If one is denied, no need to check further for this request
            }
        }
        if (allGranted && listener != null) {
            listener.onBluetoothPermissionsGranted();
        }
    }

    private void requestBluetoothPermissions() {
        ArrayList<String> permissionsToRequest = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.BLUETOOTH_SCAN
        ) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN);
        }

        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.BLUETOOTH_CONNECT
        ) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT);
        }

        if (!permissionsToRequest.isEmpty()) {
            requestBluetoothPermissionsLauncher.launch(permissionsToRequest.toArray(new String[0]));
        } else if (listener != null) {
            listener.onBluetoothPermissionsGranted();
        } else {
            Toast.makeText(activity, "Bluetooth permissions already granted", Toast.LENGTH_SHORT).show();
            turnBluetoothOn();
            // If the listener isn't set but permissions are granted, you might want to handle this differently
        }
    }

    private void turnBluetoothOn(){
        BluetoothHelper.resetAdapter();
        BluetoothHelper.enableBluetooth(activity);
    }

    public void askForBluetoothPermissions() {
        requestBluetoothPermissions();
    }
}