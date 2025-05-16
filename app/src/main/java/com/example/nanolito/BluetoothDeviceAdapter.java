package com.example.nanolito;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {

    private List<BluetoothDevice> deviceList;

    public BluetoothDeviceAdapter(List<BluetoothDevice> deviceList) {
        this.deviceList = deviceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bluetooth_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        BluetoothDevice device = deviceList.get(position);
        holder.textViewDeviceName.setText(device.getName());
        holder.textViewDeviceAddress.setText(device.getAddress());
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDeviceName;
        TextView textViewDeviceAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(view -> {
                
            });
            textViewDeviceName = itemView.findViewById(R.id.textViewDeviceName);
            textViewDeviceAddress = itemView.findViewById(R.id.textViewDeviceAddress);
        }
    }
}