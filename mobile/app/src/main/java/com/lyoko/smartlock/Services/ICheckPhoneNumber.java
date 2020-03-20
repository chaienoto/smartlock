package com.lyoko.smartlock.Services;

public interface ICheckPhoneNumber {
    void phoneNumExist(String phoneNumber);
    void phoneNumNotExist(String phoneNumber);
}
