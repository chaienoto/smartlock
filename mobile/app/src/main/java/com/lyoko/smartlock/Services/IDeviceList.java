package com.lyoko.smartlock.Services;

import com.lyoko.smartlock.Models.Device_info;

import java.util.ArrayList;

public interface IDeviceList {
    void showDevices(ArrayList<Device_info> list);
    void notThingToShow();
}
