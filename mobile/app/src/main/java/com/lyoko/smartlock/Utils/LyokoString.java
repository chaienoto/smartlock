package com.lyoko.smartlock.Utils;

import java.util.UUID;

public class LyokoString {
    // DEVICE OWNER ATTRIBUTE
    public static String PHONE_LOGIN = null;
    public final static String DEVICE_OWNER = "owner";
    public final static String PASSWORD = "password";
    public final static String PHONE_NUMBER = "password";
    // WIFI SERVICE AND CHARACTERISTIC
    public final static UUID SERVICE_WIFI_UUID = UUID.fromString("7060da7f-1ce6-43d1-b58c-2c595f8f9a56");
    public final static UUID CHARACTERISTIC_WIFI_RX_UUID = UUID.fromString("b82150b1-48e9-4a1b-a18c-3f2c140a8104");
    public final static UUID CHARACTERISTIC_WIFI_TX_UUID = UUID.fromString("459d013b-061c-430b-a4fe-734cc22012cb");
    // BATTERY SERVICE AND CHARACTERISTIC
    public final static UUID SERVICE_BATTERY_UUID = FormatData.convertFromInteger(0x180F);
    public final static UUID CHARACTERISTIC_BATTERY_LEVEL_UUID = FormatData.convertFromInteger(0x2A19);
    public final static UUID CHARACTERISTIC_CLIENT_CONFIG_UUID = FormatData.convertFromInteger(0x2902);
    // COLOR
    public final static String COLOR_BLUE = "#3498db";
    public final static String COLOR_UNLOCK = "#2ecc71";
    public final static String COLOR_LOCK = "#f95843";
    public final static String COLOR_GRAY = "#8D8C8C";
    // DATABASE PATH
    public final static String PATH_C_HISTORY = "/door/history/files";
    public final static String PATH_C_PHONE_NUMBER_REGISTERED = "/phone_number_registered";
    public final static String PATH_C_AUTH_MAC = "/mac_devices_authentic";
    // HISTORY ATTRIBUTE
    public final static String HISTORY_COVER_NAME = "cover_name";
    public final static String HISTORY_UNLOCK_TYPE = "unlock_type";
    public final static String HISTORY_TIMESTAMP = "time";
    // UNLOCK_TYPE
    public final static String UNLOCK_TYPE_SMARTPHONE = "smartphone";
    public final static String UNLOCK_TYPE_OTP = "otp";
    public final static String UNLOCK_TYPE_FINGERPRINT = "fingerprint";


}
