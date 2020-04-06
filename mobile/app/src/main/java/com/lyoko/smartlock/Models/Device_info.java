package com.lyoko.smartlock.Models;

public class Device_info {
    String device_name,address;


    public Device_info(String device_name, String address) {
        this.device_name = device_name;
        this.address = address;

    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


}
