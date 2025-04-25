package com.example.nanolito;

public class BluetoothDeviceModel {
    private String name;
    private String address;

    public BluetoothDeviceModel(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}