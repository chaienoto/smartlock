package com.lyoko.smartlock.Models;


import java.util.Date;

public class History {
    private String Cover_Name;
    private Date Timestamp;
    private String Unlock_Type;


    public History(String cover_Name, Date time, String unlock_Type) {
        Cover_Name = cover_Name;
        Timestamp = time;
        Unlock_Type = unlock_Type;
    }

    public String getCover_Name() {
        return Cover_Name;
    }

    public Date getTimestamp() {
        return Timestamp;
    }

    public String getUnlock_Type() {
        return Unlock_Type;
    }



//
}
