package com.lyoko.smartlock.Utils;
import android.util.Log;
import com.lyoko.smartlock.Services.Database_Service;

import java.util.Random;

public class OTP  {
    String otp;
    Database_Service db_service = new Database_Service();

    public OTP(String otp) {
        this.otp = otp;
    }

    public OTP() {
    }

    public void getOTPDetail(String address){
        db_service.getOTP(address);
    }

    public String generateOTP(){
        String s = "";
        while (s.length()<6)
            s = String.valueOf(new Random().nextInt(999999 - 100000));
        setOtp(s);
        return s ;
    }

    public void confirmOTPChanged(String address){
        db_service.setOTP(address, otp);
    }
    public void removeOTP(String address){
        db_service.removeOTP(address);
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        Log.d("otp", otp+"");
        this.otp = otp;
    }




}
