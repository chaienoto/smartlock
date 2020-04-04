package com.lyoko.smartlock.Services;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.lyoko.smartlock.Interface.IFindLock;

import static com.lyoko.smartlock.Utils.FormatData.hexToString;
import static com.lyoko.smartlock.Utils.LyokoString.CHARACTERISTIC_WIFI_CREDENTIAL_UUID;
import static com.lyoko.smartlock.Utils.LyokoString.CHARACTERISTIC_WIFI_TX_UUID;
import static com.lyoko.smartlock.Utils.LyokoString.SERVICE_BATTERY_UUID;
import static com.lyoko.smartlock.Utils.LyokoString.SERVICE_WIFI_UUID;
import static com.lyoko.smartlock.Utils.LyokoString.CHARACTERISTIC_BATTERY_LEVEL_UUID;

public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private BluetoothGatt gatt;
    private IFindLock iFindLock;
    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private Context context;
    private  boolean autoConnect;
    private BluetoothDevice bluetoothDevice;
    BluetoothGattCharacteristic wifi_rx, wifi_tx, battery_level;


    public final static String ACTION_GATT_CONNECTED = "com.lyoko.smartlock.Services.BluetoothLeService.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.lyoko.smartlock.Services.BluetoothLeService.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.lyoko.smartlock.Services.BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.lyoko.smartlock.Services.BluetoothLeService.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_UUID = "com.lyoko.smartlock.Services.BluetoothLeService.EXTRA_UUID";
    public final static String EXTRA_DATA = "com.lyoko.smartlock.Services.BluetoothLeService.EXTRA_DATA";


    public BluetoothLeService(Context context, boolean autoConnect, BluetoothDevice bluetoothDevice, IFindLock iFindLock) {
        this.context = context;
        this.autoConnect = autoConnect;
        this.iFindLock = iFindLock;
        this.bluetoothDevice = bluetoothDevice;
    }
    public void connectDevice(){
        gatt = bluetoothDevice.connectGatt(context,autoConnect, gattCallback);
    }
    public void close(){
        if (gatt == null) {
            return;
        }
        gatt.close();
        gatt = null;
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                intentAction = ACTION_GATT_CONNECTED;
//                mConnectionState = STATE_CONNECTED;
//                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" +
                        gatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                intentAction = ACTION_GATT_DISCONNECTED;
//                mConnectionState = STATE_DISCONNECTED;
//                broadcastUpdate(intentAction);
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                iFindLock.onConnected();
//                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//               broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
                battery_level =gatt.getService(SERVICE_BATTERY_UUID).getCharacteristic(CHARACTERISTIC_BATTERY_LEVEL_UUID);
                wifi_tx = gatt.getService(SERVICE_WIFI_UUID).getCharacteristic(CHARACTERISTIC_WIFI_TX_UUID);
                enableNotify(wifi_tx);
                enableNotify(battery_level);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            if ( CHARACTERISTIC_BATTERY_LEVEL_UUID.equals(characteristic.getUuid())) {
                int flag = characteristic.getProperties();
                int format = -1;
                if ((flag & 0x01) != 0) {
                    format = BluetoothGattCharacteristic.FORMAT_UINT16;
                    Log.d("format", "UINT16");
                    final int heartRate = characteristic.getIntValue(format, 0);
                    Log.d("Value", String.format(heartRate+""));
                } else {
                    format = BluetoothGattCharacteristic.FORMAT_UINT8;
                    Log.d("format", "UINT8");
                    final int heartRate = characteristic.getIntValue(format, 0);
                    Log.d("Value", String.format(heartRate+""));
                }
            }
                String wifi_tx_result  = wifi_tx.getStringValue(0);

                if(characteristic.getStringValue(0).equalsIgnoreCase("ok")){
                    Log.d("wifi_tx",wifi_tx_result);

//                    disableNotify(wifi_tx);
//                    disableNotify(battery_level);
//                    iFindLock.onComplete();
                }
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {

        final Intent intent = new Intent(action);

        intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());

        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();

        if (data != null && data.length > 0) {

            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + hexToString(data));
        }
        else {
            intent.putExtra(EXTRA_DATA, "0");
        }

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
    private void enableNotify(BluetoothGattCharacteristic characteristic){
        gatt.setCharacteristicNotification(characteristic,true);
        characteristic.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
    }
    private void disableNotify(BluetoothGattCharacteristic characteristic){
        gatt.setCharacteristicNotification(characteristic,false);
        characteristic.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);

    }


    public void sendWifiData(String wifi_ssid, String wifi_password) {
        wifi_rx = gatt.getService(SERVICE_WIFI_UUID).getCharacteristic(CHARACTERISTIC_WIFI_CREDENTIAL_UUID);
        wifi_rx.setValue(wifi_ssid+"|"+wifi_password);
        gatt.writeCharacteristic(wifi_rx);
    }
}
