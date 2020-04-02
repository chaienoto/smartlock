package com.lyoko.smartlock.Utils;

import android.graphics.Color;

import java.util.UUID;

public class LyokoString {
    // DEVICE OWNER ATTRIBUTE
    public static String PHONE_LOGIN = null;
    public final static String DEVICE_OWNER = "owner_name";
    public final static String PATH_PASSWORD = "password";
    public final static String PATH_DEVICES = "devices";
    public final static String PATH_HISTORIES = "histories";

    public final static String DEVICE_NAME = "name";
    public final static String LOCK_STATE = "state";
    // UNLOCK DELAY
    public static final long UNLOCK_DELAY = 5000;
    // WIFI SERVICE AND CHARACTERISTIC
    public final static UUID SERVICE_WIFI_UUID = UUID.fromString("7060da7f-1ce6-43d1-b58c-2c595f8f9a56");
    public final static UUID CHARACTERISTIC_WIFI_CREDENTIAL_UUID = UUID.fromString("b82150b1-48e9-4a1b-a18c-3f2c140a8104");
    public final static UUID CHARACTERISTIC_WIFI_TX_UUID = UUID.fromString("459d013b-061c-430b-a4fe-734cc22012cb");
    public final static UUID CHARACTERISTIC_INITIALIZATION_UUID = UUID.fromString("39a9eb9a-a309-46e1-8264-f37277c7c9be");
    // BATTERY SERVICE AND CHARACTERISTIC
    public final static UUID SERVICE_BATTERY_UUID = FormatData.convertFromInteger(0x180F);
    public final static UUID CHARACTERISTIC_BATTERY_LEVEL_UUID = FormatData.convertFromInteger(0x2A19);
    public final static UUID CHARACTERISTIC_CLIENT_CONFIG_UUID = FormatData.convertFromInteger(0x2902);
    // COLOR
    public final static int COLOR_BLUE = Color.parseColor("#3498db");
    public final static int COLOR_UNLOCK = Color.parseColor("#2ecc71");
    public final static int COLOR_LOCK = Color.parseColor("#f95843");
    public final static int COLOR_GRAY = Color.parseColor("#8D8C8C");
    public final static int COLOR_EVEN_POSITION = Color.parseColor("#dff9fb");
    public final static int COLOR_ODD_POSITION = Color.parseColor("#c7ecee");
    // DATABASE PATH
    public final static String PATH_C_HISTORY = "/door/history/files";
    public final static String PHONE_NUMBER_REGISTERED = "phone_number_registered";
    public final static String PATH_C_AUTH_MAC = "/mac_devices_authentic";
    public final static String PATH_C_AUTH_OTP = "/otp";
    // HISTORY ATTRIBUTE
    public final static String HISTORY_UNLOCK_TYPE = "unlock_type";
    public final static String HISTORY_UNLOCK_TIME = "unlock_time";
    public final static String HISTORY_UNLOCK_NAME = "unlock_name";

    // UNLOCK_TYPE
    public final static String UNLOCK_TYPE_SMARTPHONE = "smartphone";
    public final static String UNLOCK_TYPE_OTP = "otp";
    public final static String UNLOCK_TYPE_FINGERPRINT = "fingerprint";


}
