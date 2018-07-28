package com.danian.btprinter;

import android.bluetooth.BluetoothDevice;
import android.os.Message;

import com.danian.btmanage.BluetoothInterface.BtptCommand.BatteryInfo;
import com.danian.btmanage.BluetoothInterface.ConnectState;
import com.danian.btmanage.BluetoothInterface.ErrorCode;
import com.danian.btmanage.BluetoothInterface.OnPrintCallback;
import com.danian.btmanage.BluetoothManage.BluetoothService;

import java.util.ArrayList;

/**
 * Created by Danian on 2018/3/8.
 */

public class BasePrinter {
    private OnPrintCallback mPrintCallback;
    private final BluetoothService mBtService;
    private final BtptPrinter mBtptPrinter;

    private BasePrinter(BluetoothService btService, BluetoothDevice btPrinter) {
        mBtService = btService;
        if (btPrinter.getName().startsWith("BTPT")) {
            mBtptPrinter = BtptPrinter.getInstance(mBtService);
        } else {
            mBtptPrinter = null;
        }
    }

    public static BasePrinter getInstance(BluetoothService btService, BluetoothDevice btPrinter) {
        return new BasePrinter(btService, btPrinter);
    }

    public boolean isConnected() {
        if (mBtService.getConnectState() == ConnectState.STATE_CONNECTED) {
            return true;
        } else {
            return false;
        }
    }

    public void callConnectState(Message message) {
        if (mBtptPrinter != null) {
            mBtptPrinter.callConnectState(message);
        }
        switch (message.what) {
            case ConnectState.MESSAGE_STATE_CHANGE:
                switch (message.arg1) {
                    case ConnectState.STATE_NONE:
                        if (mPrintCallback != null) {
                            mPrintCallback.onFail(ErrorCode.ERROR_CONNECT_LOST);
                        }
                        break;
                }
                break;
        }
    }

    public void startBtptPrint(OnPrintCallback printCallback, ArrayList<byte[]> printData) {
        mPrintCallback = printCallback;
        mBtptPrinter.startPrint(mPrintCallback, printData);
    }

    public boolean checkBtptRead(byte[] buffer, int length) {
        if (mBtptPrinter != null) {
            return mBtptPrinter.checkReadData(buffer, length);
        } else {
            return false;
        }
    }

    public void readBtptVersion() {
        if (mBtptPrinter != null) {
            mBtptPrinter.readBaseVersion();
        }
    }

    public String getBtptVersion() {
        if (mBtptPrinter != null) {
            return mBtptPrinter.getBaseVersion();
        } else {
            return null;
        }
    }

    public void readBtptBattery() {
        if (mBtptPrinter != null) {
            mBtptPrinter.readBatteryInfo();
        }
    }

    public BatteryInfo getBtptBattery() {
        if (mBtptPrinter != null) {
            return mBtptPrinter.getBatteryInfo();
        } else {
            return null;
        }
    }

    public void updateBtptBase() {
        if (mBtptPrinter != null) {
            mBtptPrinter.updateBase();
        }
    }

    public static String hexToString(byte[] buffer, int length) {
        String hexString = "";

        for (int num = 0; num < length; num++) {
            String hex = Integer.toHexString(buffer[num] & 0xFF);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            hexString += hex + ",";
        }
        return hexString.toUpperCase();
    }
}
