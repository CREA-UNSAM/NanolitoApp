package com.example.nanolito;

import static android.app.PendingIntent.getActivity;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {

    private List<BluetoothDevice> deviceList;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private SharedPreferences.Editor prefEditor;

    public BluetoothDeviceAdapter(List<BluetoothDevice> deviceList, SharedPreferences sharedPref) {
        this.deviceList = deviceList;
        this.prefEditor = sharedPref.edit();
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

        boolean isSelected = (position == selectedPosition);

        holder.itemView.setBackgroundColor(isSelected ? Color.LTGRAY : Color.TRANSPARENT);
        holder.textViewDeviceAddress.setTextColor(isSelected ? Color.BLACK : Color.WHITE);
        holder.textViewDeviceName.setTextColor(isSelected ? Color.BLACK : Color.WHITE);

        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            prefEditor.putString("deviceAddress", device.getAddress());
            prefEditor.apply();
        });
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