package com.danian.btmanage;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.danian.btmanage.BluetoothInterface.BondState;
import com.danian.btmanage.BluetoothInterface.ConnectState;
import com.danian.btmanage.BluetoothInterface.ScanState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class BluetoothManage extends Service {
    private ScanState scanCallback;
    private ConnectState connectCallbak;
    private final BluetoothAdapter btAdapter;
    private final BluetoothConnect btConnect;
    private final List<BluetoothDevice> btDeviceList = new ArrayList<>();
    private BluetoothDevice btDevice;
    private BluetoothDevice btBondDevice;
    private BluetoothDevice btConnectDevice;
    private int btScanState;
    private int btBondState;
    private boolean connectFlag;

    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int numList;
            switch (intent.getAction()) {
                case BluetoothDevice.ACTION_FOUND:
                    btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (btDevice.getName() != null) {
                        for (numList = 0; numList < btDeviceList.size(); numList++) {
                            if (btDevice.getAddress().equals(btDeviceList.get(numList).getAddress())) {
                                break;
                            }
                        }
                        if (numList == btDeviceList.size()) {
                            btDeviceList.add(btDevice);
                            Collections.sort(btDeviceList, new SortByBond());
                            if (scanCallback != null) {
                                scanCallback.callBluetoothScan(ScanState.BLUETOOTH_SCAN_UPDATE);
                            }
                        }
                    }
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (btDevice.getName() != null) {
                        for (numList = 0; numList < btDeviceList.size(); numList++) {
                            if (btDevice.getAddress().equals(btDeviceList.get(numList).getAddress())) {
                                break;
                            }
                        }
                        if (numList < btDeviceList.size()) {
                            btDeviceList.set(numList, btDevice);
                            if (btDevice.getBondState() != BluetoothDevice.BOND_BONDING) {
                                Collections.sort(btDeviceList, new SortByBond());
                            }
                            if (btBondState != BondState.DEVICE_BONDED &&
                                    btDevice.getAddress().equals(btBondDevice.getAddress()) &&
                                    btDevice.getBondState() != BluetoothDevice.BOND_BONDING) {
                                btBondState = BondState.DEVICE_BONDED;
                            }
                            if (scanCallback != null) {
                                scanCallback.callBluetoothScan(ScanState.BLUETOOTH_BOND_CHANGED);
                            }
                        }
                    }
                    break;
                case BluetoothDevice.ACTION_PAIRING_REQUEST:
                    btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (btDevice.getName() != null && btDevice.getName().startsWith("BTPT")) {
                        try {
                            BluetoothBond.setPairingConfirmation(btDevice.getClass(), btDevice, true);
                            abortBroadcast();
                            BluetoothBond.setPin(btDevice.getClass(), btDevice, "0000");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            abortBroadcast();
                            BluetoothBond.cancelBondProcess(btDevice.getClass(), btDevice);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    if (btScanState == ScanState.BLUETOOTH_SCAINING) {
                        btScanState = ScanState.BLUETOOTH_SCAN_FINISH;
                        if (btBondState == BondState.DEVICE_BONDING) {
                            try {
                                BluetoothBond.createBond(btBondDevice.getClass(), btBondDevice);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (btBondState == BondState.DEVICE_UNBOND) {
                            try {
                                BluetoothBond.removeBond(btBondDevice.getClass(), btBondDevice);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (connectFlag) {
                            btConnect.connect(btConnectDevice);
                            connectFlag = false;
                        }
                        if (scanCallback != null) {
                            scanCallback.callBluetoothScan(ScanState.BLUETOOTH_SCAN_FINISH);
                        }
                    }
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    if (!btAdapter.isEnabled() && btScanState != ScanState.BLUETOOTH_OPEN) {
                        btScanState = ScanState.BLUETOOTH_OPEN;
                        btAdapter.enable();
                    }
                    break;
            }
        }
    };

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            if (connectCallbak != null) {
                connectCallbak.callBluetoothConnect(message);
            }
        }
    };

    public BluetoothManage() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btConnect = BluetoothConnect.getInstance(btAdapter, mHandler);
        btScanState = ScanState.BLUETOOTH_NO;
        btBondState = BondState.DEVICE_BONDED;
        connectFlag = false;
    }

    @Override
    public void onCreate() {
        if (btAdapter != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(btReceiver, filter);

            Set<BluetoothDevice> bondedDevices = btAdapter.getBondedDevices();
            if (bondedDevices.size() > 0) {
                btDeviceList.addAll(bondedDevices);
            }

            if (btAdapter.isEnabled()) {
                if (btAdapter.isDiscovering()) {
                    btAdapter.cancelDiscovery();
                }
            } else {
                btScanState = ScanState.BLUETOOTH_OPEN;
                btAdapter.enable();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (btAdapter != null) {
            unregisterReceiver(btReceiver);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new BluetoothService();
    }

    private class SortByBond implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            BluetoothDevice device1 = (BluetoothDevice) o1;
            BluetoothDevice device2 = (BluetoothDevice) o2;
            if (device1.getBondState() > device2.getBondState()) {
                return -1;
            } else if (device1.getBondState() == device2.getBondState()) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    public class BluetoothService extends Binder {

        public void setConnectCallback(ConnectState callback) {
            connectCallbak = callback;
        }

        public void setScanCallback(ScanState callback) {
            scanCallback = callback;
        }

        public BluetoothAdapter getAdapter() {
            return btAdapter;
        }

        public boolean isDiscovering() {
            if (btAdapter != null) {
                return btAdapter.isDiscovering();
            } else {
                return false;
            }
        }

        public boolean startDiscovery() {
            btDeviceList.clear();
            btScanState = ScanState.BLUETOOTH_SCAINING;
            return btAdapter.startDiscovery();
        }

        public boolean cancelDiscovery() {
            return btAdapter.cancelDiscovery();
        }

        public BluetoothDevice getDevice() {
            return btDevice;
        }

        public boolean isBonding() {
            return !(btBondState == BondState.DEVICE_BONDED);
        }

        public List<BluetoothDevice> getDeviceList() {
            return btDeviceList;
        }

        public boolean autoBondDevice(BluetoothDevice device) {
            btBondDevice = device;
            if (btBondDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                btBondState = BondState.DEVICE_BONDING;
                if (!btAdapter.isDiscovering()) {
                    try {
                        BluetoothBond.createBond(btBondDevice.getClass(), btBondDevice);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                } else {
                    btAdapter.cancelDiscovery();
                }
            } else if (btBondDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                btBondState = BondState.DEVICE_UNBOND;
                if (!btAdapter.isDiscovering()) {
                    try {
                        BluetoothBond.removeBond(btBondDevice.getClass(), btBondDevice);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                } else {
                    btAdapter.cancelDiscovery();
                }
            }
            return true;
        }

        public BluetoothDevice searchBondedDevice() {
            for (BluetoothDevice device : btDeviceList) {
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    if (device.getName().startsWith("BTPT")) {
                        return device;
                    }
                } else {
                    break;
                }
            }
            return null;
        }

        public int getConnectState() {
            return btConnect.getState();
        }

        public void setConnectDevice(BluetoothDevice device) {
            btConnectDevice = device;
            if (btConnectDevice != null) {
                if (!btAdapter.isDiscovering()) {
                    btConnect.connect(btConnectDevice);
                } else {
                    connectFlag = true;
                    btAdapter.cancelDiscovery();
                }
            }
        }

        public void sendPrintData(byte[] data) {
            btConnect.write(data);
        }

        public void stopConnect() {
            connectFlag = false;
            btConnect.stop();
        }
    }
}
