package com.lyoko.smartlock.Fragment;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lyoko.smartlock.Activities.AddDeviceActivity;
import com.lyoko.smartlock.Activities.MainActivity;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.BluetoothLeService;
import com.lyoko.smartlock.Services.Database_Helper;
import com.lyoko.smartlock.Services.Find_Device;
import com.lyoko.smartlock.Interface.IFindLock;

import static com.lyoko.smartlock.Activities.AddDeviceActivity.device_name;
import static com.lyoko.smartlock.Utils.LyokoString.add_device_address;

public class AutoSetupDeviceFragment extends Fragment implements IFindLock {


    private BluetoothLeService bluetoothLeService;

    public AutoSetupDeviceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_auto_setup_device, container, false);
        Find_Device findLock = new Find_Device(getContext(), AddDeviceActivity.device_mac_address, AddDeviceActivity.bluetoothAdapter,this);
        findLock.startScan();
        return view;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDeviceFound(final BluetoothDevice device, int rssi) {
        bluetoothLeService = new BluetoothLeService(getContext(),false,device,this);
        bluetoothLeService.connectDevice();
    }

    @Override
    public void onConnected() {
        add_device_address = AddDeviceActivity.device_mac_address;
        bluetoothLeService.sendInitialInfo(AddDeviceActivity.wifi_ssid,AddDeviceActivity.wifi_password );
    }

    @Override
    public void onComplete() {
        new Database_Helper().addNewDevice(add_device_address, device_name);
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }


}
