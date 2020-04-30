package com.lyoko.smartlock.Models;

public class Device_info {
    String device_name,address, type;
    int state;


    public Device_info(String device_name, String address, String type,  int state) {
        this.device_name = device_name;
        this.address = address;
        this.type = type;
        this.state = state;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
