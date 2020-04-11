package com.lyoko.smartlock.Models;

public class User_Info {
    String owner_name, password;

    public User_Info(String cover_name, String password) {
        this.owner_name = cover_name;
        this.password = password;
    }

    public String getOwner_name() {
        return owner_name;
    }

    public void setCover_name(String cover_name) {
        this.owner_name = cover_name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
