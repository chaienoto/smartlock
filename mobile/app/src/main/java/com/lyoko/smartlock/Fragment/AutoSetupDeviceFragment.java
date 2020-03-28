package com.lyoko.smartlock.Fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lyoko.smartlock.Activities.AddDeviceActivity;
import com.lyoko.smartlock.Activities.MainActivity;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.BluetoothLeService;
import com.lyoko.smartlock.Services.Database_Service;
import com.lyoko.smartlock.Services.Find_Device;
import com.lyoko.smartlock.Services.IFindLock;

/**
 * A simple {@link Fragment} subclass.
 */
public class AutoSetupDeviceFragment extends Fragment implements IFindLock {
    public static final int REQUEST_ENABLE_BT = 1;
    private Find_Device findLock;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothLeService bluetoothLeService;
    private Database_Service databaseService;

    public AutoSetupDeviceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_auto_setup_device, container, false);
        checkBluetoothEnable();
        databaseService = new Database_Service();
        databaseService.checkAuthenticDevice(AddDeviceActivity.device_mac_address,this);
        bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();


        return view;
    }

    public void checkBluetoothEnable() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            getActivity().startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onDeviceFound(final BluetoothDevice device, int rssi) {
        bluetoothLeService = new BluetoothLeService(getContext(),false,device,this);
        bluetoothLeService.connectDevice();
    }

    @Override
    public void onNotOwner(String address) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), "Ổ Khóa đã được đăng kí bởi số điện thoại khác", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onAsOwner(String address) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), "Bạn đã đăng kí ổ khóa này rồi", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onConnected() {
        bluetoothLeService.sendWifiData(AddDeviceActivity.wifi_ssid,AddDeviceActivity.wifi_password);
    }


    @Override
    public void onReadyToAddDevice(String address) {
        findLock = new Find_Device(getContext(), address, bluetoothAdapter,this);
        findLock.startScan();
    }

    @Override
    public void onComplete() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bluetoothLeService.close();
    }
}
