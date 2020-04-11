package com.lyoko.smartlock.Interface;

import com.lyoko.smartlock.Models.Device_info;
import com.lyoko.smartlock.Models.Remote_device;

import java.util.ArrayList;

public interface IDeviceList {
    void showOwnDevices(ArrayList<Device_info> list);
    void showRemoteDevices(ArrayList<Remote_device> list);
    void onGetRemoteList(ArrayList<Remote_device> list);
    void notThingToShow(String list);
}
