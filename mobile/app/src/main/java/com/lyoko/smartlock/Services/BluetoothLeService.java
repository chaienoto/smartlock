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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.lyoko.smartlock.Utils.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BluetoothLeService extends Service {
    private final static String TAG = "BLE_SERVICE";
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private String bluetoothDeviceAddress;
    private BluetoothGatt bluetoothGatt;
    private int connectionState = STATE_DISCONNECTED;
    private UUID BatteryService = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    private UUID BatteryLevelCharacteristic = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");
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
                displayGattServices(bluetoothGatt.getServices());
//                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED,gatt.getService(BatteryService).getCharacteristic(BatteryLevelCharacteristic));
//                Log.d("service", gatt.getService(BatteryService).getCharacteristic(BatteryLevelCharacteristic).getUuid().toString());
//                Log.d("value", String.valueOf(gatt.getService(BatteryService).getCharacteristic(BatteryLevelCharacteristic).getUuid().getIntValue(-1,1)));
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

//        // This is special handling for the Heart Rate Measurement profile. Data
//        // parsing is carried out as per profile specifications.
//        intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());
//        Log.i("EXTRA_UUID", characteristic.getUuid().toString());
//        // For all other profiles, writes the data formatted in HEX.

        int flag = characteristic.getProperties();
        int format = -1;
        if ((flag & 0x01) != 0) {
            format = BluetoothGattCharacteristic.FORMAT_UINT16;
            Log.d(TAG, "format UINT16.");
        } else {
            format = BluetoothGattCharacteristic.FORMAT_UINT8;
            Log.d(TAG, "format UINT8.");
        }
        final int value =characteristic.getIntValue(format,1);
        Log.d(TAG, String.format("Received heart rate: %d", value));

//        final byte[] data = characteristic.getValue();
//
//        if (data != null && data.length > 0) {
//
//            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + Request.hexToString(data));
//        }
//        else {
//            intent.putExtra(EXTRA_DATA, "0");
//        }
//        sendBroadcast(intent);
    }



    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            final String uuid = gattService.getUuid().toString();
            System.out.println("Service discovered: " + uuid);
//            MainActivity.this.runOnUiThread(new Runnable() {
//                public void run() {
//                    peripheralTextView.append("Service disovered: "+uuid+"\n");
//                }
//            });
            new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {

                final String charUuid = gattCharacteristic.getUuid().toString();
                System.out.println("Characteristic discovered for service: " + charUuid);
//                MainActivity.this.runOnUiThread(new Runnable() {
//                    public void run() {
//                        peripheralTextView.append("Characteristic discovered for service: "+charUuid+"\n");
//                    }
//                });

            }
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
