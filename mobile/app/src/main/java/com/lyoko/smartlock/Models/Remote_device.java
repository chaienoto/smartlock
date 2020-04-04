package com.lyoko.smartlock.Models;

public class Remote_device {
    String device_owner, device_mac;

    public Remote_device(String device_owner, String device_mac) {
        this.device_owner = device_owner;
        this.device_mac = device_mac;
    }

    public String getDevice_owner() {
        return device_owner;
    }

    public void setDevice_owner(String device_owner) {
        this.device_owner = device_owner;
    }

    public String getDevice_mac() {
        return device_mac;
    }

    public void setDevice_mac(String device_mac) {
        this.device_mac = device_mac;
    }
}
