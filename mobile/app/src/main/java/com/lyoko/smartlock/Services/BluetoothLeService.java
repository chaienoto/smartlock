package com.lyoko.smartlock.Services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.lyoko.smartlock.Utils.FormatData;

import java.util.UUID;

public class BluetoothLeService extends Service {
    private final static String TAG = "BLE_SERVICE";
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private String bluetoothDeviceAddress;
    FormatData formatData = new FormatData();
    private BluetoothGatt gatt;
    private int connectionState = STATE_DISCONNECTED;
    private final UUID BATTERY_SERVICE_UUID = formatData.convertFromInteger(0x180F);
    private final UUID BATTERY_LEVEL_CHARACTERISTIC_UUID = formatData.convertFromInteger(0x2A19);
    private final UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = formatData.convertFromInteger(0x2902);
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private Context context;
    private  boolean autoConnect;
    private BluetoothDevice bluetoothDevice;


    public BluetoothLeService(Context context, boolean autoConnect, BluetoothDevice bluetoothDevice) {
        this.context = context;
        this.autoConnect = autoConnect;
        this.bluetoothDevice = bluetoothDevice;
    }
    public void connectDevice(){
        gatt = bluetoothDevice.connectGatt(context,autoConnect, gattCallback);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectionState = STATE_CONNECTED;
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" +
                        gatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattCharacteristic characteristic =
                        gatt.getService(BATTERY_SERVICE_UUID)
                                .getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID);
//                gatt.setCharacteristicNotification(characteristic,true);
                gatt.readCharacteristic(characteristic);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                String value = characteristic.getStringValue(1);
                String value = characteristic.toString();
                Log.d("Value", value+" %, status: "+ status);

            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            //gatt.readCharacteristic(characteristic);
            super.onCharacteristicChanged(gatt, characteristic);
        }
    };
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

//    private void broadcastUpdate(final String action,
//                                 final BluetoothGattCharacteristic characteristic) {
//        final Intent intent = new Intent(action);
//
////        // This is special handling for the Heart Rate Measurement profile. Data
////        // parsing is carried out as per profile specifications.
////        intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());
////        Log.i("EXTRA_UUID", characteristic.getUuid().toString());
////        // For all other profiles, writes the data formatted in HEX.
//
//        int flag = characteristic.getProperties();
//        int format = -1;
//        if ((flag & 0x01) != 0) {
//            format = BluetoothGattCharacteristic.FORMAT_UINT16;
//            Log.d(TAG, "format UINT16.");
//        } else {
//            format = BluetoothGattCharacteristic.FORMAT_UINT8;
//            Log.d(TAG, "format UINT8.");
//        }
//        final int value =characteristic.getIntValue(format,1);
//        Log.d(TAG, String.format("Received heart rate: %d", value));
//
////        final byte[] data = characteristic.getValue();
////
////        if (data != null && data.length > 0) {
////
////            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + Request.hexToString(data));
////        }
////        else {
////            intent.putExtra(EXTRA_DATA, "0");
////        }
////        sendBroadcast(intent);
//    }



//    private void displayGattServices(List<BluetoothGattService> gattServices) {
//        if (gattServices == null) return;
//        // Loops through available GATT Services.
//
//        for (BluetoothGattService gattService : gattServices) {
//
//            final String uuid = gattService.getUuid().toString();
//            Log.d("Service discovered: ", uuid);
//            gattService.getCharacteristics();
//
//            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
//            // Loops through available Characteristics.
//            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//
//                final String charUuid = gattCharacteristic.getUuid().toString();
//                Log.d("Characteristic: " , charUuid);
//                bluetoothGatt.readCharacteristic(gattCharacteristic);
//
//            }
//        }
//    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
