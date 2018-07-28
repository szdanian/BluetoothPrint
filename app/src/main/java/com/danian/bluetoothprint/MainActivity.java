package com.danian.bluetoothprint;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.danian.btdevice.DeviceManage;
import com.danian.btmanage.BluetoothInterface.BtptCommand;
import com.danian.btmanage.BluetoothInterface.BtptCommand.BatteryInfo;
import com.danian.btmanage.BluetoothInterface.ConnectState;
import com.danian.btmanage.BluetoothInterface.ErrorCode;
import com.danian.btmanage.BluetoothInterface.ScanState;
import com.danian.btmanage.BluetoothManage;
import com.danian.btmanage.BluetoothManage.BluetoothService;
import com.danian.btprinter.BasePrinter;

import com.danian.btprinter.BtptPrinter;
import com.idean.pos.service.OnPrintListener;
import com.idean.pos.service.PrintDriver;

import com.jxnx.smartpos.api.device.printer.FeedUnit;
import com.jxnx.smartpos.api.device.printer.FontFamily;
import com.jxnx.smartpos.api.device.printer.PrintAlign;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private StartService btApplication;
    private BluetoothService btService;
    private BtptPrinter btPrinter;
    private BluetoothDevice connectDevice;
    private TextView deviceName;
    private TextView deviceAddress;
    private Button deviceBond;
    private BatteryInfo batteryInfo;
    private String version;

    private final ScanState bluetoothScan = new ScanState() {
        @Override
        public void callBluetoothScan(int action) {
            switch (action) {
                case BLUETOOTH_SCAN_UPDATE:
                    if (connectDevice == null) {
                        findMatchDevice();
                    }
                    break;
                case BLUETOOTH_BOND_CHANGED:
                    if (connectDevice != null) {
                        BluetoothDevice tmpDevice = btService.getDevice();
                        if (tmpDevice.getAddress().equals(connectDevice.getAddress())) {
                            switch (tmpDevice.getBondState()) {
                                case BluetoothDevice.BOND_NONE:
                                    deviceBond.setText("未配对");
                                    break;
                                case BluetoothDevice.BOND_BONDING:
                                    deviceBond.setText("配对中");
                                    break;
                            }
                        }
                    }
                    break;
                case BLUETOOTH_SCAN_FINISH:
                    if (connectDevice == null) {
                        Toast.makeText(MainActivity.this, "no bluetooth printe", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    private final ConnectState bluetoothConnect = new ConnectState() {
        @Override
        public void callBluetoothConnect(Message message) {
            if (btPrinter != null) {
                btPrinter.callConnectState(message);
            }
            switch (message.what) {
                case ConnectState.MESSAGE_STATE_CHANGE:
                    switch (message.arg1) {
                        case ConnectState.STATE_NONE:
                            deviceBond.setText("已配对");
                            break;
                        case ConnectState.STATE_CONNECTING:
                            deviceBond.setText("连接中");
                            break;
                        case ConnectState.STATE_CONNECTED:
                            deviceBond.setText("已连接");
                            btPrinter = BtptPrinter.getInstance(btService);
                            btPrinter.readBaseVersion();
                            break;
                    }
                    break;
                case ConnectState.MESSAGE_READ:
                    byte[] readbuf = (byte[]) message.obj;
                    if (btPrinter.checkReadData(readbuf, message.arg1)) {
                        switch (readbuf[6]) {
                            case BtptCommand.PRINT_VERSION:
                                version = btPrinter.getBaseVersion();
                                Toast.makeText(MainActivity.this, version, Toast.LENGTH_SHORT).show();
                                btPrinter.readBatteryInfo();
                                break;
                            case BtptCommand.BATTERY_INFO:
                                batteryInfo = btPrinter.getBatteryInfo();
                                break;
                        }
                    } else {
                        Toast.makeText(MainActivity.this, BasePrinter.hexToString(readbuf, message.arg1),
                                Toast.LENGTH_SHORT).show();
                    }
            }
        }
    };

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = (BluetoothService) service;
            btService.setScanCallback(bluetoothScan);

            findMatchDevice();
            if (connectDevice == null && !btService.isDiscovering()) {
                Toast.makeText(MainActivity.this, "no bluetooth printer", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btApplication = (StartService) getApplication();

        findViewById(R.id.main_test_feed).setOnClickListener(this);
        findViewById(R.id.main_test_print).setOnClickListener(this);
        findViewById(R.id.main_update_base).setOnClickListener(this);
        findViewById(R.id.main_device_manage).setOnClickListener(this);

        deviceName = (TextView) findViewById(R.id.main_device_name);
        deviceAddress = (TextView) findViewById(R.id.main_device_address);
        deviceBond = (Button) findViewById(R.id.main_device_bond);
        deviceBond.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, BluetoothManage.class);
        bindService(intent, connection, BIND_AUTO_CREATE);

        deviceName.setText("");
        deviceAddress.setText("");
        deviceBond.setVisibility(View.GONE);
        connectDevice = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (btService != null) {
            btService.setScanCallback(null);
            btService.setConnectCallback(null);
            btService.stopConnect();
        }
        unbindService(connection);
    }

    @Deprecated
    protected void onDestroy() {
        super.onDestroy();
        btApplication.stopService();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("请确定是否要退出？")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_test_feed:
                if (isPrintConnect()) {
                    PrintDriver printDriver = PrintDriver.getInstance(btPrinter);
                    printDriver.feedPaper(1, FeedUnit.LINE);
                }
                break;
            case R.id.main_test_print:
                if (isPrintConnect()) {
                    testBtptPrinter();
                }
                break;
            case R.id.main_update_base:
                if (isPrintConnect()) {
                    File file = new File("/S600BASE.BIN");
                    if (file.exists()) {
                        btPrinter.updateBase();
                    } else {
                        Toast.makeText(this, "底座升级程序不存在！", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.main_device_manage:
                if (btService != null) {
                    if (btService.getConnectState() != ConnectState.STATE_CONNECTING) {
                        startActivity(new Intent(this, DeviceManage.class));
                    } else {
                        Toast.makeText(this, "请等待完成连接", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.main_device_bond:
                if (btService.getConnectState() == ConnectState.STATE_NONE) {
                    btService.setConnectDevice(connectDevice);
                } else if (btService.getConnectState() == ConnectState.STATE_CONNECTED) {
                    btService.stopConnect();
                }
                break;
        }
    }

    private void findMatchDevice() {
        connectDevice = btService.searchBondedDevice();
        if (connectDevice != null) {
            deviceName.setText(connectDevice.getName());
            deviceAddress.setText(connectDevice.getAddress());
            deviceBond.setText("已配对");
            deviceBond.setVisibility(View.VISIBLE);

            btService.setConnectCallback(bluetoothConnect);
            btService.setConnectDevice(connectDevice);
        }
    }

    private boolean isPrintConnect() {
        if (connectDevice != null) {
            if (btService.getConnectState() == ConnectState.STATE_CONNECTED) {
                return true;
            } else {
                Toast.makeText(this, "未连接蓝牙打印机", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "请检查是否有适配的蓝牙打印机", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void testBtptPrinter() {
        PrintDriver printDriver = PrintDriver.getInstance(btPrinter);

        printDriver.blankLine(1);
        printDriver.appendPrnStr("====== 测试打印 ======",
                FontFamily.SMALL, FontFamily.NOT_NEED_BOLD, PrintAlign.CENTER);
        printDriver.blankLine(1);
        printDriver.appendPrnStr("电池： " + (float) batteryInfo.getVoltage() / 1000 + "V",
                FontFamily.SMALL, FontFamily.NOT_NEED_BOLD, PrintAlign.LEFT);
        printDriver.appendPrnStr("版本： " + version.substring(10, 13),
                FontFamily.SMALL, FontFamily.NOT_NEED_BOLD, PrintAlign.LEFT);
        printDriver.appendPrnStr("日期： " + version.substring(14, 25),
                FontFamily.SMALL, FontFamily.NOT_NEED_BOLD, PrintAlign.LEFT);
        printDriver.appendPrnStr("名称： " + connectDevice.getName(),
                FontFamily.SMALL, FontFamily.NOT_NEED_BOLD, PrintAlign.LEFT);
        printDriver.appendPrnStr("地址： " + connectDevice.getAddress(),
                FontFamily.SMALL, FontFamily.NOT_NEED_BOLD, PrintAlign.LEFT);
        printDriver.appendPrnStr("宽度： 48mm",
                FontFamily.SMALL, FontFamily.NOT_NEED_BOLD, PrintAlign.LEFT);
        printDriver.appendPrnStr("[ASCII Sample]",
                FontFamily.SMALL, FontFamily.NOT_NEED_BOLD, PrintAlign.LEFT);
        printDriver.appendPrnStr(" !\"#$%&'()*+,-./0123456789:;<=>?",
                FontFamily.SMALL, FontFamily.NOT_NEED_BOLD, PrintAlign.LEFT);
        printDriver.appendPrnStr("@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_",
                FontFamily.SMALL, FontFamily.NOT_NEED_BOLD, PrintAlign.LEFT);
        printDriver.appendPrnStr("'abcdefghijklmnopqrstuvwxyz{|}~",
                FontFamily.SMALL, FontFamily.NOT_NEED_BOLD, PrintAlign.LEFT);
        printDriver.blankLine(1);
        printDriver.appendPrnStr("====== 打印结束 ======",
                FontFamily.SMALL, FontFamily.NOT_NEED_BOLD, PrintAlign.CENTER);
        printDriver.blankLine(5);

        printDriver.startPrint(new OnPrintListener.Stub() {
            @Override
            public void onPrintResult(int retCode) throws RemoteException {
                if (retCode == ErrorCode.ERROR_OK) {
                    Toast.makeText(MainActivity.this, "success: Ok", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "errorCode: 0x" + Integer.toHexString(retCode).toUpperCase(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
