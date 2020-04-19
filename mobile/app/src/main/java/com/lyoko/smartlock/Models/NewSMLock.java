package com.lyoko.smartlock.Models;


public class NewSMLock {
    String device_name;
    lock lock;

    public NewSMLock(String device_name, NewSMLock.lock lock) {
        this.device_name = device_name;
        this.lock = lock;
    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public NewSMLock.lock getLock() {
        return lock;
    }

    public void setLock(NewSMLock.lock lock) {
        this.lock = lock;
    }

    public static class lock {
        int state;
        String otp;

        public lock(int state, String otp) {
            this.state = state;
            this.otp = otp;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public String getOtp() {
            return otp;
        }

        public void setOtp(String otp) {
            this.otp = otp;
        }
    }
}
