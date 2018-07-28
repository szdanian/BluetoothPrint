package com.danian.btmanage;

import android.bluetooth.BluetoothDevice;
import android.os.Message;

/**
 * Created by Danian on 2018/3/2.
 */

public interface BluetoothInterface {

    interface ScanState {
        int BLUETOOTH_NO = 0;                               // 不支持蓝牙
        int BLUETOOTH_OPEN = 1;                             // 打开蓝牙
        int BLUETOOTH_SCAINING = 2;                         // 正在搜索蓝牙设备
        int BLUETOOTH_SCAN_FINISH = 3;                      // 搜索蓝牙设备完成
        int BLUETOOTH_SCAN_UPDATE = 4;                      // 搜索到有效蓝牙设备
        int BLUETOOTH_BOND_CHANGED = 5;                     // 蓝牙设备配对状态改变

        void callBluetoothScan(int action);
    }

    interface BondState {
        int DEVICE_BONDED = 0;                              // 完成配对
        int DEVICE_BONDING = 1;                             // 正在配对
        int DEVICE_UNBOND = 2;                              // 解除配对

        void callBondDevice(BluetoothDevice device);
    }

    interface ConnectState {
        int STATE_NONE = 0;                                 // doing nothing or no connection
        int STATE_LISTEN = 1;                               // listening for incoming connection
        int STATE_CONNECTING = 2;                           // initiating an outgoing connection
        int STATE_CONNECTED = 3;                            // connected to a remote device

        // Message types sent from the BluetoothConnect Handler
        int MESSAGE_STATE_CHANGE = 4;
        int MESSAGE_READ = 5;
        int MESSAGE_WRITE = 6;
        int MESSAGE_DEVICE_NAME = 7;
        int MESSAGE_TOAST = 8;

        // Key names received from the BluetoothConnect Handler
        String DEVICE_NAME = "device_name";
        String TOAST = "toast";

        void callBluetoothConnect(Message message);
    }

    interface OnPrintCallback {

        void onFinish();

        void onFail(int errorCode);
    }

    interface BtptCommand {
        byte START_COMMAND = (byte) 0xBF;
        byte PRINT_COMMAND = (byte) 0xF0;
        byte PRINT_QUERY = (byte) 0xF1;
        byte PRINT_SEND = (byte) 0xF2;
        byte PRINT_COMPLETE = (byte) 0xF3;
        byte PRINT_APPLY = (byte) 0xF9;
        byte PRINT_RESULT = (byte) 0xFA;
        byte BATTERY_INFO = (byte) 0xFB;
        byte PRINT_VERSION = (byte) 0xFF;
        byte END_COMMAND = (byte) 0xA5;
        byte APPLY_UPDATE = (byte) 0xD0;
        byte SEND_UPDATE = (byte) 0xD1;
        byte COMPLETE_UPDATE = (byte) 0xD3;

        byte PRINT_START = 0x01;
        byte PRINT_STOP = 0x00;
        byte PRINT_SUCCESS = (byte) 0x88;
        int MIN_WORK_VOLTAGE = 7100;

        class BatteryInfo {
            private int mVoltage;
            private boolean mCharge;

            public BatteryInfo(int voltage, byte charge) {
                mVoltage = voltage;
                if (charge == 1) {
                    mCharge = true;
                } else {
                    mCharge = false;
                }
            }

            public int getVoltage() {
                return mVoltage;
            }

            public boolean isCharge() {
                return mCharge;
            }
        }
    }

    interface ErrorCode {
        int ERROR_OK = 0x00;

        int ERROR_OPEN_FAIL = 0x11;
        int ERROR_SEND_FAIL = 0x22;
        int ERROR_APPLY_FAIL = 0x33;

        int ERROR_NO_PAPER = 0xAA;
        int ERROR_OVERTEMP = 0xBB;
        int ERROR_PRINT_BUSY = 0xCC;
        int ERROR_LOW_VOLTAGE = 0xDD;
        int ERROR_CONNECT_LOST = 0xEE;
    }
}
