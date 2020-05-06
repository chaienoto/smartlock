package com.lyoko.smartlock.Utils;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lyoko.smartlock.Activities.MainActivity;
import com.lyoko.smartlock.Interface.iCanRemote;
import com.lyoko.smartlock.Interface.iCheckPhoneNumber;
import com.lyoko.smartlock.Interface.iDeviceList;
import com.lyoko.smartlock.Interface.iHistory;
import com.lyoko.smartlock.Interface.iLock;
import com.lyoko.smartlock.Interface.iLogin;
import com.lyoko.smartlock.Interface.iQRCheck;
import com.lyoko.smartlock.Interface.iRegister;
import com.lyoko.smartlock.Interface.iTrustedDevice;
import com.lyoko.smartlock.Models.CanRemotePersonInfo;
import com.lyoko.smartlock.Models.Device_info;
import com.lyoko.smartlock.Models.Device_settings;
import com.lyoko.smartlock.Models.History;
import com.lyoko.smartlock.Models.NewSMLock;
import com.lyoko.smartlock.Models.Remote_device;
import com.lyoko.smartlock.Models.NewUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.lyoko.smartlock.Utils.LyokoString.AUTH_ID;
import static com.lyoko.smartlock.Utils.LyokoString.DELAY;
import static com.lyoko.smartlock.Utils.LyokoString.DEVICE_NAME;
import static com.lyoko.smartlock.Utils.LyokoString.DEVICE_TYPE;
import static com.lyoko.smartlock.Utils.LyokoString.FCM;
import static com.lyoko.smartlock.Utils.LyokoString.FCM_UPDATE;
import static com.lyoko.smartlock.Utils.LyokoString.HISTORY_UNLOCK_NAME;
import static com.lyoko.smartlock.Utils.LyokoString.HISTORY_UNLOCK_TIME;
import static com.lyoko.smartlock.Utils.LyokoString.HISTORY_UNLOCK_TYPE;
import static com.lyoko.smartlock.Utils.LyokoString.LOCK;
import static com.lyoko.smartlock.Utils.LyokoString.LOCK_OTP;
import static com.lyoko.smartlock.Utils.LyokoString.LOCK_STATE;
import static com.lyoko.smartlock.Utils.LyokoString.LYOKO_DEVICES;
import static com.lyoko.smartlock.Utils.LyokoString.HISTORIES;
import static com.lyoko.smartlock.Utils.LyokoString.LYOKO_USERS;
import static com.lyoko.smartlock.Utils.LyokoString.OTP_LIMIT_ENTRY;
import static com.lyoko.smartlock.Utils.LyokoString.OWNER_NAME;
import static com.lyoko.smartlock.Utils.LyokoString.OWN_DEVICES;
import static com.lyoko.smartlock.Utils.LyokoString.PASSWORD;
import static com.lyoko.smartlock.Utils.LyokoString.PHONE_NUMBER_REGISTERED;
import static com.lyoko.smartlock.Utils.LyokoString.REMOTE_BY;
import static com.lyoko.smartlock.Utils.LyokoString.REMOTE_DEVICES;
import static com.lyoko.smartlock.Utils.LyokoString.TRUSTED_DEVICES_ADDRESS;
import static com.lyoko.smartlock.Utils.LyokoString.TRUSTED_DEVICES_NAME;
import static com.lyoko.smartlock.Utils.LyokoString.UPDATE_CODE;
import static com.lyoko.smartlock.Utils.LyokoString.auth_id;
import static com.lyoko.smartlock.Utils.LyokoString.phone_login;

public class Database_Helper {
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference user = db.getReference(LYOKO_USERS);
    DatabaseReference devices = db.getReference(LYOKO_DEVICES);
    DatabaseReference phone_number =  db.getReference(PHONE_NUMBER_REGISTERED);

    public void getHistories(String owner_phone_number, String current_device_address, final iHistory iHistory) {
        user.child(owner_phone_number).child(OWN_DEVICES).child(current_device_address).child(HISTORIES)
                .addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                         final ArrayList<History> list = new ArrayList<>();
                         for (DataSnapshot historySnapshot: dataSnapshot.getChildren()){
                             String unlock_name = historySnapshot.child(HISTORY_UNLOCK_NAME).getValue(String.class);
                             String unlock_type = historySnapshot.child(HISTORY_UNLOCK_TYPE).getValue(String.class);
                             String unlock_time = historySnapshot.child(HISTORY_UNLOCK_TIME).getValue(String.class) ;
                             list.add(new History(unlock_name, unlock_time, unlock_type));
                         }
                         if (list.size()==0){
                             return;
                         }
                         iHistory.show_history(list);

                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError databaseError) {

                     }
                });

    }

    public void checkPhoneNumber(final String phoneNum, final iCheckPhoneNumber iCheckPhoneNumber) {
        phone_number.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(phoneNum).exists()) {
                    iCheckPhoneNumber.phoneNumExist(dataSnapshot.child(phoneNum).getValue(String.class));
                } else iCheckPhoneNumber.phoneNumNotExist();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void checkPassword(final String password, final iLogin iLogin){
         user.child(phone_login).child(PASSWORD).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String pass = dataSnapshot.getValue(String.class);
                if (pass.equalsIgnoreCase(password)){
                    iLogin.onPasswordMatched();
                } else iLogin.onPasswordNotMatch();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getOwnDevices(final iDeviceList iDeviceList) {
        user.child(phone_login).child(OWN_DEVICES).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount()>0) {
                    Log.d("count", String.valueOf(dataSnapshot.getChildrenCount()));
                    final ArrayList<Device_info> s = new ArrayList<>();
                    for (DataSnapshot deviceSnapshot: dataSnapshot.getChildren()){
                        String macAddress = deviceSnapshot.getKey();
                        String name =  deviceSnapshot.child(DEVICE_NAME).getValue(String.class);
                        String type = deviceSnapshot.child(DEVICE_TYPE).getValue(String.class);
                        int state = deviceSnapshot.child(LOCK_STATE).getValue(Integer.class);
                        s.add(new Device_info(phone_login,macAddress,name,type,state));
                    }
                    iDeviceList.showOwnDevices(s);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }


    public void getRemoteDevice(final iDeviceList iDeviceList){
        user.child(phone_login).child(REMOTE_DEVICES).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount()>0){
                    final ArrayList<Device_info> list = new ArrayList<>();
                    for (DataSnapshot remoteDevice: dataSnapshot.getChildren()){
                        final String address = remoteDevice.getKey();
                        final String owner = remoteDevice.getValue(String.class);
                        list.add(new Device_info(owner,address));

                    }
                    subGetRemoteDevice(list,iDeviceList);
                } else  iDeviceList.showRemoteDevices(new ArrayList<Device_info>());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }
    private void subGetRemoteDevice(final ArrayList<Device_info> list, final iDeviceList iDeviceList) {
        for (final Device_info device:list)
        user.child(device.getOwner()).child(OWN_DEVICES).child(device.getAddress()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child(DEVICE_NAME).getValue(String.class);
                String type = dataSnapshot.child(DEVICE_TYPE).getValue(String.class);
                int state = dataSnapshot.child(LOCK_STATE).getValue(Integer.class);
                device.setName(name);
                device.setType(type);
                device.setState(state);
                iDeviceList.showRemoteDevices(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }



    public void changed_update_code(String owner_phone_number,String current_device_address, int update_code) {
        user.child(owner_phone_number).child(OWN_DEVICES).child(current_device_address).child(UPDATE_CODE).setValue(update_code);
    }

    public void saveHistory(String owner_phone_number, String current_device_address, String unlock_name){
        DatabaseReference history = user.child(owner_phone_number).child(OWN_DEVICES).child(current_device_address).child(HISTORIES);
        String hisID = history.push().getKey();
        String pattern= "EE, MMMM dd yyyy HH:mm:ss";
        Date date =new Date(new Date().getTime());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String unlock_time = simpleDateFormat.format(date);
        String unlock_type = "smartphone";
        History his = new History(unlock_name,unlock_time,unlock_type);
        history.child(hisID).setValue(his);
    }

    public void checkAuthenticDevice(final String address,final iQRCheck iqrCheck){
        devices.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean check = false;
                for (DataSnapshot authDeviceSnapshot: dataSnapshot.getChildren()){
                    if (authDeviceSnapshot.getKey().equals(address) && check == false){
                        check = true;
                        String owner = authDeviceSnapshot.getValue(String.class);
                        if (!owner.equals("null")){
                            if (phone_login == null || phone_login == ""){
                                phone_login = "0";
                            }
                            if (owner.equals(phone_login)){
                                iqrCheck.onAsOwner(address);
                            } else {
                                iqrCheck.onNotOwner(address);
                            }
                        } else  iqrCheck.onReadyToAddDevice(address);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void registerNewUser(iRegister iRegister, NewUser userInfo) {
        user.child(phone_login).setValue(userInfo);
        db.getReference(PHONE_NUMBER_REGISTERED).child(phone_login).setValue(userInfo.getOwner_name());
        iRegister.onRegisterSuccess();
    }

    public void addRemoteDevices(final String current_device_address, final String remote_phone_number, final String id){
        user.child(remote_phone_number).child(REMOTE_DEVICES).child(current_device_address).setValue(phone_login);
        user.child(phone_login).child(OWN_DEVICES).child(current_device_address).child(REMOTE_BY).child(id).setValue(remote_phone_number);

    }

    public void getAuthID(String phone_login){
        user.child(phone_login).child(AUTH_ID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.getValue(String.class);
                auth_id = name;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void changePassword(String pw) {
        user.child(phone_login).child(PASSWORD).setValue(pw);
    }


    public void setOTP(String address, String otp) {
       user.child(phone_login).child(OWN_DEVICES).child(address).child(LOCK_OTP).setValue(otp);
    }


    public void addNewDevice(String add_device_address, String device_name) {
        user.child(phone_login).child(OWN_DEVICES).child(add_device_address).setValue(new NewSMLock(device_name, new NewSMLock.lock(0, "null")));
    }

    public void addTrustedDevice(String current_device_address, String device_count, String ble_name, String ble_address) {
        user.child(phone_login)
                .child(OWN_DEVICES)
                .child(current_device_address)
                .child(TRUSTED_DEVICES_ADDRESS).child(device_count).setValue(ble_address);
        user.child(phone_login)
                .child(OWN_DEVICES)
                .child(current_device_address)
                .child(TRUSTED_DEVICES_NAME).child(device_count).setValue(ble_name);
    }

    public void updateDeviceName(String device_address, String name) {
        user.child(phone_login).child(OWN_DEVICES).child(device_address).child(DEVICE_NAME).setValue(name);
    }

    public void getDeviceSettings(final Activity activity, final String device_address) {
        user.child(phone_login).child(OWN_DEVICES).child(device_address)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child(DEVICE_NAME).getValue(String.class);
                        int delay = dataSnapshot.child(DELAY).getValue(Integer.class);
                        int otp_limit = dataSnapshot.child(OTP_LIMIT_ENTRY).getValue(Integer.class);
                        DialogShow.onGetDeviceSettings(activity,  new Device_settings(name,device_address,delay/1000,otp_limit));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    public void updateDelay(String device_address, String delay) {
        user.child(phone_login).child(OWN_DEVICES).child(device_address).child(DELAY).setValue(Integer.parseInt(delay)*1000);
    }

    public void updateOTPLimitEntry(String device_address, String limit) {
        user.child(phone_login).child(OWN_DEVICES).child(device_address).child(OTP_LIMIT_ENTRY).setValue(Integer.parseInt(limit));
    }

    public void getTrustedDevice(final String device_address, final iTrustedDevice iTrustedDevice) {
        user.child(phone_login).child(OWN_DEVICES).child(device_address).child(TRUSTED_DEVICES_NAME)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<String> list = new ArrayList<>();
                        for (DataSnapshot trustedDevice: dataSnapshot.getChildren()){
                            String trustedName = trustedDevice.getValue(String.class);
                            list.add(trustedName);
                        }
                        iTrustedDevice.showTrustedDevice(list);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        iTrustedDevice.noDeviceToShow();
                    }
                });
    }

    public void updateTrustedDevices(final int position, final String device_address, final String path) {
        user.child(phone_login).child(OWN_DEVICES).child(device_address).child(path)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<String> list = new ArrayList<>();
                        for (DataSnapshot trustedDevice: dataSnapshot.getChildren()){
                            String trustedName = trustedDevice.getValue(String.class);
                            list.add(trustedName);
                        }
                        list.remove(position);
                        user.child(phone_login).child(OWN_DEVICES).child(device_address).child(path).removeValue();
                        for (int i=0; i < list.size(); i++)
                            user.child(phone_login).child(OWN_DEVICES).child(device_address).child(path).child(String.valueOf(i)).setValue(list.get(i));

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    public void getCanRemoteDevice(String current_device_address, final iCanRemote iCanRemote) {
        user.child(phone_login).child(OWN_DEVICES).child(current_device_address).child(REMOTE_BY)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final  ArrayList<CanRemotePersonInfo> list = new ArrayList<>();
                        for (DataSnapshot data: dataSnapshot.getChildren()){
                            list.add(new CanRemotePersonInfo(data.getValue(String.class),data.getKey()));
                        }
                        Log.d("list", list.toString());
                        subGetCanRemoteDevice(list);
                    }

                    private void subGetCanRemoteDevice(final ArrayList<CanRemotePersonInfo> listKey) {
                        phone_number.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                final ArrayList<CanRemotePersonInfo> info = new ArrayList<>();
                                for (final CanRemotePersonInfo _info: listKey) {
                                    info.add(new CanRemotePersonInfo(_info.getPhoneNumber(),dataSnapshot.child(_info.getPhoneNumber()).getValue(String.class),_info.getAuthid()));
                                }
                                iCanRemote.onGetCanRemoteDevices(info);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    public void removeCanRemoteDevice(String current_device_address, String phone, String authid) {
        user.child(phone).child(REMOTE_DEVICES).child(current_device_address).removeValue();
        user.child(phone_login).child(OWN_DEVICES).child(current_device_address).child(REMOTE_BY).child(authid).removeValue();
    }

    public void change_name(String name) {
        user.child(phone_login).child(OWNER_NAME).setValue(name);
        phone_number.child(phone_login).setValue(name);
    }

    public void updateToken(final String phone_login, final String token) {
        user.child(phone_login).child(FCM).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (!dataSnapshot.getValue(String.class).equals(token)){
                        makeAllDevicesUpdateOwnerToken(phone_login,token);
                    }
                } else  makeAllDevicesUpdateOwnerToken(phone_login,token);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void makeAllDevicesUpdateOwnerToken(final String phone_login, String token) {
        user.child(phone_login).child(FCM).setValue(token);
        user.child(phone_login).child(OWN_DEVICES).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount()>0) {
                    for (DataSnapshot deviceSnapshot: dataSnapshot.getChildren()){
                        String address = deviceSnapshot.getKey();
                        user.child(phone_login).child(OWN_DEVICES).child(address).child(UPDATE_CODE).setValue(FCM_UPDATE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}