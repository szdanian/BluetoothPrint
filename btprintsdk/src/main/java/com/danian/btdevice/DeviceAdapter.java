package com.danian.btdevice;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.danian.btmanage.BluetoothInterface.BondState;

import java.util.List;

/**
 * Created by Danian on 2018/3/5.
 */

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    private BondState bondCallback;
    private final List<BluetoothDevice> mDeviceList;

    public DeviceAdapter(List<BluetoothDevice> deviceList) {
        mDeviceList = deviceList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.device_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.deviceBond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    BluetoothDevice device = mDeviceList.get(position);
                    if (device.getBondState() != BluetoothDevice.BOND_BONDING &&
                            device.getName().startsWith("BTPT")) {
                        bondCallback.callBondDevice(device);
                    }
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BluetoothDevice device = mDeviceList.get(position);
        holder.deviceName.setText(device.getName());
        holder.deviceAddress.setText(device.getAddress());
        switch (device.getBondState()) {
            case BluetoothDevice.BOND_NONE:
                holder.deviceBond.setText("未配对");
                break;
            case BluetoothDevice.BOND_BONDING:
                holder.deviceBond.setText("配对中");
                break;
            case BluetoothDevice.BOND_BONDED:
                holder.deviceBond.setText("已配对");
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }

    public void setBondCallback(BondState callback) {
        bondCallback = callback;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView deviceName;
        private final TextView deviceAddress;
        private final Button deviceBond;

        public ViewHolder(View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.da_device_name);
            deviceAddress = itemView.findViewById(R.id.da_device_address);
            deviceBond = itemView.findViewById(R.id.da_device_bond);
        }
    }
}
