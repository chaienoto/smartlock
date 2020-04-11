package com.lyoko.smartlock.Interface;

public interface ILock {
    void onLock();
    void onUnlock();
    void onGetOwnerName(String ownerName);
    void onHold();
}
