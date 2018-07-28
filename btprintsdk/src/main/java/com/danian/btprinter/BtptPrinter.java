package com.danian.btprinter;

import android.os.Message;

import com.danian.btmanage.BluetoothInterface.BtptCommand;
import com.danian.btmanage.BluetoothInterface.BtptCommand.BatteryInfo;
import com.danian.btmanage.BluetoothInterface.ConnectState;
import com.danian.btmanage.BluetoothInterface.ErrorCode;
import com.danian.btmanage.BluetoothInterface.OnPrintCallback;
import com.danian.btmanage.BluetoothManage.BluetoothService;

import java.util.ArrayList;

/**
 * Created by Danian on 2018/3/8.
 */

public class BtptPrinter {
    private final BluetoothService mBtService;
    private OnPrintCallback mPrintCallback;
    private ArrayList<byte[]> mPrintData;
    private BatteryInfo batteryInfo;
    private String baseVersion;
    private boolean startFlag;
    private int numList;

    private BtptPrinter(BluetoothService btService) {
        mBtService = btService;
    }

    public static BtptPrinter getInstance(BluetoothService btService) {
        return new BtptPrinter(btService);
    }

    public void callConnectState(Message message) {
        switch (message.what) {
            case ConnectState.MESSAGE_READ:
                byte[] readbuf = (byte[]) message.obj;
                if (checkReadData(readbuf, message.arg1)) {
                    switch (readbuf[6]) {
                        case BtptCommand.PRINT_COMMAND:
                            if (readbuf[7] == ErrorCode.ERROR_OK) {
                                if (startFlag) {
                                    mBtService.sendPrintData(getBtptCommand(BtptCommand.PRINT_QUERY));
                                    startFlag = false;
                                    numList = 0;
                                }
                            } else if (startFlag) {
                                mPrintCallback.onFail(ErrorCode.ERROR_OPEN_FAIL);
                                startFlag = false;
                            }
                            break;
                        case BtptCommand.PRINT_QUERY:
                            if (readbuf[7] == ErrorCode.ERROR_OK) {
                                mBtService.sendPrintData(mPrintData.get(numList++));
                            } else {
                                mBtService.sendPrintData(getBtptCommand(BtptCommand.PRINT_COMMAND, BtptCommand.PRINT_STOP));
                                mPrintCallback.onFail(readbuf[7] & 0xFF);
                            }
                            break;
                        case BtptCommand.PRINT_SEND:
                            if (readbuf[7] == ErrorCode.ERROR_OK) {
                                if (numList < mPrintData.size()) {
                                    mBtService.sendPrintData(getBtptCommand(BtptCommand.PRINT_QUERY));
                                } else {
                                    mBtService.sendPrintData(getBtptCommand(BtptCommand.PRINT_COMPLETE));
                                }
                            } else {
                                mBtService.sendPrintData(getBtptCommand(BtptCommand.PRINT_COMMAND, BtptCommand.PRINT_STOP));
                                mPrintCallback.onFail(ErrorCode.ERROR_SEND_FAIL);
                            }
                            break;
                        case BtptCommand.PRINT_COMPLETE:
                            if (readbuf[7] == ErrorCode.ERROR_OK) {
                                mBtService.sendPrintData(getBtptCommand(BtptCommand.PRINT_APPLY));
                            } else {
                                mBtService.sendPrintData(getBtptCommand(BtptCommand.PRINT_COMMAND, BtptCommand.PRINT_STOP));
                                mPrintCallback.onFail(ErrorCode.ERROR_SEND_FAIL);
                            }
                            break;
                        case BtptCommand.PRINT_APPLY:
                            if (readbuf[7] == ErrorCode.ERROR_OK) {
                                mBtService.sendPrintData(getBtptCommand(BtptCommand.PRINT_RESULT));
                            } else {
                                mBtService.sendPrintData(getBtptCommand(BtptCommand.PRINT_COMMAND, BtptCommand.PRINT_STOP));
                                mPrintCallback.onFail(ErrorCode.ERROR_APPLY_FAIL);
                            }
                            break;
                        case BtptCommand.PRINT_RESULT:
                            if (readbuf[7] == ErrorCode.ERROR_OK) {
                                mBtService.sendPrintData(getBtptCommand(BtptCommand.PRINT_RESULT));
                            } else {
                                mBtService.sendPrintData(getBtptCommand(BtptCommand.PRINT_COMMAND, BtptCommand.PRINT_STOP));
                                if (readbuf[7] == BtptCommand.PRINT_SUCCESS) {
                                    mPrintCallback.onFinish();
                                } else {
                                    mPrintCallback.onFail(readbuf[7] & 0xFF);
                                }
                            }
                            break;
                        case BtptCommand.BATTERY_INFO:
                            batteryInfo = new BatteryInfo(
                                    ((readbuf[15] & 0xFF) << 8) | (readbuf[14] & 0xFF), readbuf[12]);
                            if (startFlag) {
                                if (batteryInfo.getVoltage() > BtptCommand.MIN_WORK_VOLTAGE) {
                                    mBtService.sendPrintData(getBtptCommand(BtptCommand.PRINT_COMMAND, BtptCommand.PRINT_START));
                                } else {
                                    mPrintCallback.onFail(ErrorCode.ERROR_LOW_VOLTAGE);
                                }
                            }
                            break;
                        case BtptCommand.PRINT_VERSION:
                            baseVersion = new String(readbuf).substring(8, 53);
                            break;
                        case BtptCommand.APPLY_UPDATE:

                            break;
                    }
                }
                break;
        }
    }

    public boolean isConnected() {
        if (mBtService.getConnectState() == ConnectState.STATE_CONNECTED) {
            return true;
        } else {
            return false;
        }
    }

    public void startPrint(OnPrintCallback printCallback, ArrayList<byte[]> printData) {
        startFlag = true;
        mPrintCallback = printCallback;
        mBtService.sendPrintData(getBtptCommand(BtptCommand.BATTERY_INFO));
        mPrintData = printData;
    }

    private byte[] getBtptCommand(byte command) {
        byte[] btptCmd = new byte[9];

        btptCmd[0] = 'Z';
        btptCmd[1] = 'Z';
        btptCmd[2] = 'Z';
        btptCmd[3] = BtptCommand.START_COMMAND;
        btptCmd[4] = 0;
        btptCmd[5] = 1;
        btptCmd[6] = command;
        btptCmd[7] = BtptCommand.END_COMMAND;
        btptCmd[8] = 0;
        for (int num = 2; num < 8; num++) {
            btptCmd[8] += btptCmd[num];
        }
        return btptCmd;
    }

    private byte[] getBtptCommand(byte command1, byte command2) {
        byte[] btptCmd = new byte[10];

        btptCmd[0] = 'Z';
        btptCmd[1] = 'Z';
        btptCmd[2] = 'Z';
        btptCmd[3] = BtptCommand.START_COMMAND;
        btptCmd[4] = 0;
        btptCmd[5] = 2;
        btptCmd[6] = command1;
        btptCmd[7] = command2;
        btptCmd[8] = BtptCommand.END_COMMAND;
        btptCmd[9] = 0;
        for (int num = 2; num < 9; num++) {
            btptCmd[9] += btptCmd[num];
        }
        return btptCmd;
    }

    public void readBaseVersion() {
        mBtService.sendPrintData(getBtptCommand(BtptCommand.PRINT_VERSION));
    }

    public String getBaseVersion() {
        return baseVersion;
    }

    public void readBatteryInfo() {
        mBtService.sendPrintData(getBtptCommand(BtptCommand.BATTERY_INFO));
    }

    public BatteryInfo getBatteryInfo() {
        return batteryInfo;
    }

    public void updateBase() {
        mBtService.sendPrintData(getBtptCommand(BtptCommand.APPLY_UPDATE));
    }

    public boolean checkReadData(byte[] buffer, int length) {
        int max = length - 1;
        byte check = 0;

        for (int num = 2; num < max; num++) {
            check += buffer[num];
        }
        if (check == buffer[max]) {
            return true;
        } else {
            return false;
        }
    }
}
