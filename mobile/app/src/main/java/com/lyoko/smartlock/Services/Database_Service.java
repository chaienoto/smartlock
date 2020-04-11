package com.lyoko.smartlock.Services;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import com.lyoko.smartlock.Models.Remote_device;
import com.lyoko.smartlock.Models.User_Info;
import com.lyoko.smartlock.Utils.LyokoString;

import java.util.ArrayList;
import java.util.Date;

import static com.lyoko.smartlock.Utils.LyokoString.DEVICE_NAME;
import static com.lyoko.smartlock.Utils.LyokoString.HISTORY_UNLOCK_NAME;
import static com.lyoko.smartlock.Utils.LyokoString.HISTORY_UNLOCK_TIME;
import static com.lyoko.smartlock.Utils.LyokoString.HISTORY_UNLOCK_TYPE;
import static com.lyoko.smartlock.Utils.LyokoString.LOCK_STATE;
import static com.lyoko.smartlock.Utils.LyokoString.LYOKO_DEVICES;
import static com.lyoko.smartlock.Utils.LyokoString.DEVICES;
import static com.lyoko.smartlock.Utils.LyokoString.HISTORIES;
import static com.lyoko.smartlock.Utils.LyokoString.OWNER_NAME;
import static com.lyoko.smartlock.Utils.LyokoString.PASSWORD;
import static com.lyoko.smartlock.Utils.LyokoString.PHONE_NUMBER_REGISTERED;
import static com.lyoko.smartlock.Utils.LyokoString.REMOTES;
import static com.lyoko.smartlock.Utils.LyokoString.phone_login;

public class Database_Service {
    FirebaseDatabase db = FirebaseDatabase.getInstance();

    public void getHistories(String owner_phone_number, String current_device_address, final IHistory iHistory) {
         db.getReference(PHONE_NUMBER_REGISTERED)
                .child(owner_phone_number)
                .child(DEVICES)
                .child(current_device_address)
                .child(HISTORIES).addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 final ArrayList<History> list = new ArrayList<>();
                 for (DataSnapshot historySnapshot: dataSnapshot.getChildren()){
                     String unlock_name = historySnapshot.child(HISTORY_UNLOCK_NAME).getValue(String.class);
                     String unlock_type = historySnapshot.child(HISTORY_UNLOCK_TYPE).getValue(String.class);
                     Long unlock_time = historySnapshot.child(HISTORY_UNLOCK_TIME).getValue(Long.class) ;
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
        db.getReference(PHONE_NUMBER_REGISTERED).child(phoneNum).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(OWNER_NAME).exists()) {
                     iCheckPhoneNumber.phoneNumExist();
                } else
                     iCheckPhoneNumber.phoneNumNotExist();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void checkPassword(final String password, final ILogin iLogin){
         db.getReference(PHONE_NUMBER_REGISTERED).child(phone_login).child(PASSWORD).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String pass = dataSnapshot.getValue(String.class);
                Log.d("pass nè", pass+"");
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
        db.getReference(PHONE_NUMBER_REGISTERED)
                .child(phone_login)
                .child(DEVICES)
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
                            iDeviceList.notThingToShow("own");
                            return;
                        }
                        iDeviceList.showOwnDevices(list);

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public void getLockState(String owner_phone_number, String current_device_address, final ILock iLock) {
        db.getReference(PHONE_NUMBER_REGISTERED)
                .child(owner_phone_number)
                .child(DEVICES)
                .child(current_device_address)
                .child(LOCK_STATE)
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
        db.getReference(PHONE_NUMBER_REGISTERED)
                .child(owner_phone_number)
                .child(DEVICES)
                .child(current_device_address)
                .child(LOCK_STATE)
                .setValue(state);
    }

    public void saveHistory(String owner_phone_number, String current_device_address, String owner_name){
        DatabaseReference history = db.getReference(PHONE_NUMBER_REGISTERED)
                .child(owner_phone_number)
                .child(DEVICES)
                .child(current_device_address)
                .child(HISTORIES);
        String hisID = history.push().getKey();
        Long unlock_time = new Date().getTime();
        String unlock_type = "smartphone";
        History his = new History(owner_name,unlock_time,unlock_type);
        history.child(hisID).setValue(his);
    }

    public void getOwnerName(String phone_login, final ILock iLock){
        db.getReference(PHONE_NUMBER_REGISTERED)
                .child(phone_login)
                .child(OWNER_NAME)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.getValue(String.class);
                iLock.onGetOwnerName(name);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

    public void registerNewUser(IRegister iRegister,User_Info userInfo) {
        db.getReference(PHONE_NUMBER_REGISTERED).child(phone_login).setValue(userInfo);
        iRegister.onRegisterSuccess();
    }

    public void addRemoteDevices(String current_device_address, String remote_phone_number){
         db.getReference(PHONE_NUMBER_REGISTERED)
                 .child(remote_phone_number)
                 .child(REMOTES)
                 .child(current_device_address)
                 .setValue(phone_login);
    }

    public void getRemoteList(final IDeviceList iDeviceList){
        db.getReference(PHONE_NUMBER_REGISTERED)
                .child(phone_login)
                .child(REMOTES)
                .addValueEventListener(new ValueEventListener() {
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
            db.getReference(PHONE_NUMBER_REGISTERED)
                    .child(owner)
                    .child(DEVICES)
                    .child(address).child(DEVICE_NAME)
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

    public void getName(String phone_login, final IAuth iAuth){
        db.getReference(PHONE_NUMBER_REGISTERED)
                .child(phone_login)
                .child(OWNER_NAME)
                .addListenerForSingleValueEvent(new ValueEventListener() {
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
        db.getReference(PHONE_NUMBER_REGISTERED).child(phone_login).child(PASSWORD).setValue(pw);
    }
}