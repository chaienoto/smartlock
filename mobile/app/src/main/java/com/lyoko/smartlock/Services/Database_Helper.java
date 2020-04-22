package com.lyoko.smartlock.Services;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lyoko.smartlock.Activities.MainActivity;
import com.lyoko.smartlock.Interface.IAuth;
import com.lyoko.smartlock.Interface.ICheckPhoneNumber;
import com.lyoko.smartlock.Interface.IDeviceList;
import com.lyoko.smartlock.Interface.IHistory;
import com.lyoko.smartlock.Interface.ILock;
import com.lyoko.smartlock.Interface.ILogin;
import com.lyoko.smartlock.Interface.IQRCheck;
import com.lyoko.smartlock.Interface.IRegister;
import com.lyoko.smartlock.Models.Device_info;
import com.lyoko.smartlock.Models.History;
import com.lyoko.smartlock.Models.NewSMLock;
import com.lyoko.smartlock.Models.Remote_device;
import com.lyoko.smartlock.Models.NewUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.lyoko.smartlock.Utils.LyokoString.AUTH_ID;
import static com.lyoko.smartlock.Utils.LyokoString.DEVICE_NAME;
import static com.lyoko.smartlock.Utils.LyokoString.HISTORY_UNLOCK_NAME;
import static com.lyoko.smartlock.Utils.LyokoString.HISTORY_UNLOCK_TIME;
import static com.lyoko.smartlock.Utils.LyokoString.HISTORY_UNLOCK_TYPE;
import static com.lyoko.smartlock.Utils.LyokoString.LOCK;
import static com.lyoko.smartlock.Utils.LyokoString.LOCK_OTP;
import static com.lyoko.smartlock.Utils.LyokoString.LOCK_STATE;
import static com.lyoko.smartlock.Utils.LyokoString.LYOKO_DEVICES;
import static com.lyoko.smartlock.Utils.LyokoString.HISTORIES;
import static com.lyoko.smartlock.Utils.LyokoString.LYOKO_USERS;
import static com.lyoko.smartlock.Utils.LyokoString.OWNER_NAME;
import static com.lyoko.smartlock.Utils.LyokoString.OWN_DEVICES;
import static com.lyoko.smartlock.Utils.LyokoString.PASSWORD;
import static com.lyoko.smartlock.Utils.LyokoString.PHONE_NUMBER_REGISTERED;
import static com.lyoko.smartlock.Utils.LyokoString.REMOTE_BY;
import static com.lyoko.smartlock.Utils.LyokoString.REMOTE_DEVICES;
import static com.lyoko.smartlock.Utils.LyokoString.TRUSTED_DEVICES_ADDRESS;
import static com.lyoko.smartlock.Utils.LyokoString.TRUSTED_DEVICES_NAME;
import static com.lyoko.smartlock.Utils.LyokoString.phone_login;

public class Database_Helper {
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference user = db.getReference(LYOKO_USERS);
    DatabaseReference phone_number =  db.getReference(PHONE_NUMBER_REGISTERED);

    public void getHistories(String owner_phone_number, String current_device_address, final IHistory iHistory) {
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

    public void checkPhoneNumber(final String phoneNum, final ICheckPhoneNumber iCheckPhoneNumber) {
        phone_number.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(phoneNum).exists()) {
                    iCheckPhoneNumber.phoneNumExist();
                } else iCheckPhoneNumber.phoneNumNotExist();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                iCheckPhoneNumber.phoneNumNotExist();
            }
        });
    }

    public void checkPassword(final String password, final ILogin iLogin){
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

    public void getOwnDevices(String phone_login, final IDeviceList iDeviceList) {
        user.child(phone_login).child(OWN_DEVICES)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final ArrayList<Device_info> list = new ArrayList<>();
                    for (DataSnapshot deviceSnapshot: dataSnapshot.getChildren()){
                        String macAddress = deviceSnapshot.getKey();
                        String name = deviceSnapshot.child(DEVICE_NAME).getValue(String.class);
                        list.add(new Device_info(name,macAddress));
                    }
                    if (list.size()==0){
                        return;
                    }
                    iDeviceList.showOwnDevices(list);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
    }

    public void getRemoteList(final IDeviceList iDeviceList){
        user.child(phone_login).child(REMOTE_DEVICES).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ArrayList<Remote_device> list = new ArrayList<>();
                for (DataSnapshot remoteDevice: dataSnapshot.getChildren()){
                    final String address = remoteDevice.getKey();
                    final String owner = remoteDevice.getValue(String.class);
                    list.add(new Remote_device(owner, address, "null" ));
                }
                if (list.size()==0){
                    return;
                }
                iDeviceList.onGetRemoteList(list);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getRemoteDevice(final IDeviceList iDeviceList, final ArrayList<Remote_device> list){
        for (final Remote_device device : list){
            final String address = device.getAddress();
            final String owner = device.getOwnerPhoneNumber();
            user.child(owner).child(OWN_DEVICES).child(address).child(DEVICE_NAME)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String name = dataSnapshot.getValue(String.class);
                            device.setDevice_name(name);
                            iDeviceList.showRemoteDevices(list);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
        }

    }

    public void getLockState(String owner_phone_number, String current_device_address, final ILock iLock) {
        user.child(owner_phone_number).child(OWN_DEVICES).child(current_device_address).child(LOCK).child(LOCK_STATE)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int state = dataSnapshot.getValue(Integer.class);
                        switch (state){
                            case 0: iLock.onLock();
                                return;
                            case 1: iLock.onUnlock();
                                return;
                            case 2: iLock.onHold();
                                return;
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    public void changeLockState(String owner_phone_number,String current_device_address, int state) {
        user.child(owner_phone_number).child(OWN_DEVICES).child(current_device_address).child(LOCK).child(LOCK_STATE).setValue(state);
    }

    public void saveHistory(String owner_phone_number, String current_device_address, String owner_name){
        DatabaseReference history = user.child(owner_phone_number).child(OWN_DEVICES).child(current_device_address).child(HISTORIES);
        String hisID = history.push().getKey();
        String pattern= "EE, MMMM dd yyyy HH:mm:ss";
        Date date =new Date(new Date().getTime());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String unlock_time = simpleDateFormat.format(date);
        String unlock_type = "smartphone";
        History his = new History(owner_name,unlock_time,unlock_type);
        history.child(hisID).setValue(his);
    }

    public void checkAuthenticDevice(final String address,final IQRCheck iqrCheck){
        db.getReference(LYOKO_DEVICES).addListenerForSingleValueEvent(new ValueEventListener() {
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

    public void registerNewUser(IRegister iRegister, NewUser userInfo) {
        user.child(phone_login).setValue(userInfo);
        db.getReference(PHONE_NUMBER_REGISTERED).child(phone_login).setValue(new Date().getTime());
        iRegister.onRegisterSuccess();
    }

    public void addRemoteDevices(final String current_device_address, final String remote_phone_number){
        DatabaseReference remoteDevice = user.child(remote_phone_number);
        remoteDevice.child(REMOTE_DEVICES).child(current_device_address).setValue(phone_login);
        remoteDevice.child(OWNER_NAME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.getValue(String.class);
                user.child(phone_login).child(OWN_DEVICES).child(current_device_address).child(REMOTE_BY).child(remote_phone_number).setValue(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void getName(String phone_login, final IAuth iAuth){
        user.child(phone_login).child(OWNER_NAME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.getValue(String.class);
                iAuth.onGetName(name);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void changePassword(String pw) {
        user.child(phone_login).child(PASSWORD).setValue(pw);
    }

    public void getOTP(String address) {
        Log.d("getOTPof", address);
        user.child(phone_login).child(OWN_DEVICES).child(address).child(LOCK)
                .child(LOCK_OTP)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String _otp = dataSnapshot.getValue(String.class);
                MainActivity.otp = _otp;
                MainActivity.otp_saved = true;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setOTP(String address, String otp) {
       user.child(phone_login).child(OWN_DEVICES).child(address).child(LOCK).child(LOCK_OTP).setValue(otp);
    }

    public void removeOTP(String address) {
        user.child(phone_login).child(OWN_DEVICES).child(address).child(LOCK).child(LOCK_OTP).removeValue();
    }

    public void addNewDevice(String add_device_address, String device_name) {
        user.child(phone_login).child(OWN_DEVICES).child(add_device_address).setValue(new NewSMLock(device_name, new NewSMLock.lock(0, "null")));
    }

    public void addTrustedDevice(String current_device_address, String owner_phone_number, String device_count, String ble_name, String ble_address) {
        user.child(owner_phone_number)
                .child(OWN_DEVICES)
                .child(current_device_address)
                .child(LOCK).child(TRUSTED_DEVICES_ADDRESS).child(device_count).setValue(ble_address);
        user.child(owner_phone_number)
                .child(OWN_DEVICES)
                .child(current_device_address)
                .child(LOCK).child(TRUSTED_DEVICES_NAME).child(device_count).setValue(ble_name);
    }
}