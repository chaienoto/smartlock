package com.lyoko.smartlock.Services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.lyoko.smartlock.Utils.Request;

import java.util.UUID;

public class BluetoothLeService extends Service {
    private final static String TAG = "BLE_SERVICE";
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private String bluetoothDeviceAddress;
    private BluetoothGatt bluetoothGatt;
    private int connectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private Context context;
    private  boolean autoConnect;
    private BluetoothDevice bluetoothDevice;


    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static String EXTRA_UUID =
            "com.example.bluetooth.le.EXTRA_DATA";


    public BluetoothLeService(Context context, boolean autoConnect, BluetoothDevice bluetoothDevice) {
        this.context = context;
        this.autoConnect = autoConnect;
        this.bluetoothDevice = bluetoothDevice;
    }
    public void connectDevice(){
        bluetoothGatt = bluetoothDevice.connectGatt(context,autoConnect, gattCallback);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                connectionState = STATE_CONNECTED;
//                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" +
                        bluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                connectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                for (BluetoothGattService gattService : gatt.getServices()){
//                    if (gattService.getUuid()==)
                    Log.d("service_uuid",gattService.getUuid().toString());
                    Log.d("service_characteristic", String.valueOf(gattService.getCharacteristics().get(0).FORMAT_UINT16));

                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }
    };
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());
        Log.i("EXTRA_UUID", characteristic.getUuid().toString());
        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();

        if (data != null && data.length > 0) {

            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + Request.hexToString(data));
        }
        else {
            intent.putExtra(EXTRA_DATA, "0");
        }
        sendBroadcast(intent);
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
