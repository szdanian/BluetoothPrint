package com.danian.bluetoothprint;

import android.app.Application;
import android.content.Intent;

import com.danian.btmanage.BluetoothManage;

/**
 * Created by Danian on 2018/3/5.
 */

public class StartService extends Application {
    private Intent btIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        btIntent = new Intent(this, BluetoothManage.class);
        startService(btIntent);
    }

    public void stopService() {
        stopService(btIntent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
