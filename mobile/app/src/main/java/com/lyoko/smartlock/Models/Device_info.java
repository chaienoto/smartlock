package com.lyoko.smartlock.Models;

public class Device_info {
    String device_name,address;

    public Device_info(String name, String address) {
        this.device_name = name;
        this.address = address;
    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String name) {
        this.device_name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
