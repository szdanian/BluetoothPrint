package com.danian.btmanage;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.danian.btmanage.BluetoothInterface.ConnectState;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Danian on 2018/3/6.
 */

public class BluetoothConnect {
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState, mOldState;

    private BluetoothConnect(BluetoothAdapter adapter, Handler handler) {
        mAdapter = adapter;
        mHandler = handler;
        mState = ConnectState.STATE_NONE;
        mOldState = mState;
    }

    public static BluetoothConnect getInstance(BluetoothAdapter adapter, Handler handler) {
        return new BluetoothConnect(adapter, handler);
    }

    public synchronized int getState() {
        return mState;
    }

    private synchronized void updateConnectState() {
        mState = getState();
        mHandler.obtainMessage(ConnectState.MESSAGE_STATE_CHANGE, mState, mOldState).sendToTarget();
        mOldState = mState;
    }

    public synchronized void start() {
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        mAcceptThread = new AcceptThread();
        mAcceptThread.start();
        updateConnectState();
    }

    public synchronized void connect(BluetoothDevice device) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        updateConnectState();
    }

    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        Message message = mHandler.obtainMessage(ConnectState.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(ConnectState.DEVICE_NAME, device.getName());
        message.setData(bundle);
        mHandler.sendMessage(message);
        updateConnectState();
    }

    public synchronized void stop() {
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    public void write(byte[] outbuf) {
        ConnectedThread connected;
        synchronized (BluetoothConnect.this) {
            if (mState == ConnectState.STATE_CONNECTED) {
                connected = mConnectedThread;
            } else {
                return;
            }
        }
        connected.write(outbuf);
    }

    private void connectionFailed() {
        Message message = mHandler.obtainMessage(ConnectState.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(ConnectState.TOAST, "unable to connect device");
        message.setData(bundle);
        mHandler.sendMessage(message);

        mState = ConnectState.STATE_NONE;
        updateConnectState();
    }

    private void connectionLost() {
        Message message = mHandler.obtainMessage(ConnectState.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(ConnectState.TOAST, "device connection was lost");
        message.setData(bundle);
        mHandler.sendMessage(message);

        mState = ConnectState.STATE_NONE;
        updateConnectState();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mServerSocket;

        public AcceptThread() {
            BluetoothServerSocket temp = null;

            while (temp == null) {
                try {
                    temp = mAdapter.listenUsingRfcommWithServiceRecord("BluetoothConnect", MY_UUID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mServerSocket = temp;
            mState = ConnectState.STATE_LISTEN;
        }

        @Override
        public void run() {
            BluetoothSocket socket;

            while (mState == ConnectState.STATE_LISTEN) {
                try {
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

                if (socket != null) {
                    synchronized (BluetoothConnect.this) {
                        connected(socket, socket.getRemoteDevice());
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }

        public void cancel() {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;

        public ConnectThread(BluetoothDevice device) {
            mDevice = device;
            BluetoothSocket temp = null;

            while (temp == null) {
                try {
                    temp = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mSocket = temp;
            mState = ConnectState.STATE_CONNECTING;
        }

        @Override
        public void run() {
            try {
                mSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                connectionFailed();
                return;
            }
            connected(mSocket, mDevice);
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInStream;
        private final OutputStream mOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = mSocket.getInputStream();
                tempOut = mSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mInStream = tempIn;
            mOutStream = tempOut;
            mState = ConnectState.STATE_CONNECTED;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int length;

            while (mState == ConnectState.STATE_CONNECTED) {
                try {
                    length = mInStream.read(buffer);
                    mHandler.obtainMessage(ConnectState.MESSAGE_READ, length, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                    connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mOutStream.write(buffer);
                mOutStream.flush();
                mHandler.obtainMessage(ConnectState.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                mOutStream.close();
                mInStream.close();
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
