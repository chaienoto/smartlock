package com.lyoko.smartlock.Services;

public interface ILock {
    void onLock();
    void onUnlock();
    void onGetOwnerName(String ownerName);
}
