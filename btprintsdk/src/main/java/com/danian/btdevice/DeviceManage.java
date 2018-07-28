package com.danian.btdevice;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.danian.btmanage.BluetoothInterface.BondState;
import com.danian.btmanage.BluetoothInterface.ScanState;
import com.danian.btmanage.BluetoothManage;

import java.util.ArrayList;
import java.util.List;

public class DeviceManage extends AppCompatActivity implements View.OnClickListener {
    private BluetoothManage.BluetoothService btService;
    private DeviceAdapter deviceAdapter;
    private final List<BluetoothDevice> deviceList = new ArrayList<>();
    private Button searchDevice;

    private final ScanState bluetoothScan = new ScanState() {
        @Override
        public void callBluetoothScan(int action) {
            switch (action) {
                case BLUETOOTH_OPEN:
                    searchDevice.setText("正打开蓝牙...");
                    break;
                case BLUETOOTH_SCAINING:
                    deviceList.clear();
                    deviceAdapter.notifyDataSetChanged();
                    searchDevice.setText("正在搜索...");
                    break;
                case BLUETOOTH_SCAN_FINISH:
                    searchDevice.setText("搜索设备");
                    break;
                case BLUETOOTH_SCAN_UPDATE:
                case BLUETOOTH_BOND_CHANGED:
                    deviceList.clear();
                    deviceList.addAll(btService.getDeviceList());
                    deviceAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    private final BondState bluetoothBond = new BondState() {
        @Override
        public void callBondDevice(BluetoothDevice device) {
            btService.autoBondDevice(device);
        }
    };

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = (BluetoothManage.BluetoothService) service;
            btService.setScanCallback(bluetoothScan);

            deviceList.clear();
            deviceList.addAll(btService.getDeviceList());
            if (btService.isDiscovering()) {
                searchDevice.setText("正在搜索...");
            } else {
                searchDevice.setText("搜索设备");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_manage);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        deviceAdapter = new DeviceAdapter(deviceList);
        deviceAdapter.setBondCallback(bluetoothBond);

        RecyclerView deviceRecycler = findViewById(R.id.device_Recycler);
        deviceRecycler.setLayoutManager(layoutManager);
        deviceRecycler.setAdapter(deviceAdapter);
        deviceRecycler.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL));

        searchDevice = (Button) findViewById(R.id.dm_button_scan);
        searchDevice.setOnClickListener(this);
        findViewById(R.id.dm_button_back).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, BluetoothManage.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (btService != null) {
            btService.setScanCallback(null);
        }
        unbindService(connection);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onClick(View v) {
        int keyValue = v.getId();
        if (keyValue == R.id.dm_button_scan) {
            if (btService != null && btService.getAdapter() != null) {
                if (searchDevice.getText().equals("搜索设备")) {
                    if (!btService.isBonding()) {
                        deviceList.clear();
                        deviceAdapter.notifyDataSetChanged();
                        btService.startDiscovery();
                        searchDevice.setText("正在搜索...");
                    } else {
                        Toast.makeText(this, "请等待配对完成", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    btService.cancelDiscovery();
                }
            }
        } else if (keyValue == R.id.dm_button_back) {
            if (btService != null && btService.getAdapter() != null) {
                if (!btService.isBonding()) {
                    this.finish();
                } else {
                    Toast.makeText(this, "请等待配对完成", Toast.LENGTH_SHORT).show();
                }
            } else {
                this.finish();
            }
        }
    }
}
