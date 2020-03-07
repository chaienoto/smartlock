package com.lyoko.smartlock.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;


public class History {
    @PropertyName("cover_name")
    private String Cover_Name;
    @PropertyName("state")
    private boolean State;
    //@PropertyName("time")
//    @ServerTimestamp
    private Timestamp Time;
    @PropertyName("unlock_type")
    private String Unlock_Type;




    public History(String Cover_Name, Boolean State, Timestamp Time, String Unlock_Type) {
        this.Cover_Name = Cover_Name;
        this.State = State;
        this.Time = Time;
        this.Unlock_Type = Unlock_Type;
    }

    public String getCover_Name() {
        return Cover_Name;
    }

    public void setCover_Name(String cover_Name) {
        Cover_Name = cover_Name;
    }

    public boolean getState() {
        return State;
    }

    public void setState(Boolean state) {
        State = state;
    }

    public Timestamp getTime() {
        return Time;
    }
    public void setTime(Timestamp time) {
        Time = time;
    }

    public String getUnlock_Type() {
        return Unlock_Type;
    }

    public void setUnlock_Type(String unlock_Type) {
        Unlock_Type = unlock_Type;
    }

//
}
