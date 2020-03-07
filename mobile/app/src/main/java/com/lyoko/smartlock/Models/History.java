package com.lyoko.smartlock.Models;


import java.util.Date;

public class History {
    private String Cover_Name;
    private Date Time;
    private String Unlock_Type;


    public History(String cover_Name, Date time, String unlock_Type) {
        Cover_Name = cover_Name;
        Time = time;
        Unlock_Type = unlock_Type;
    }

    public String getCover_Name() {
        return Cover_Name;
    }

    public Date getTime() {
        return Time;
    }

    public String getUnlock_Type() {
        return Unlock_Type;
    }


//
}
