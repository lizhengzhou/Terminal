package com.lizz.terminal;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.Vector;

public class BluetoothPort  {
    private static final String TAG = BluetoothPort.class.getSimpleName();
    private static final UUID SERIAL_PORT_SERVICE_CLASS_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String NAME = "BluetoothPort";
    private BluetoothAdapter mAdapter = null;
    private BluetoothDevice device;
    private BluetoothSocket mSocket;
    private int state = 0;
    private String macAddress;

    InputStream inputStream;
    OutputStream outputStream;

    private void initSocketStream() throws IOException {
        this.inputStream = this.mSocket.getInputStream();
        this.outputStream = this.mSocket.getOutputStream();
    }

    public boolean openPort() {
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mAdapter.cancelDiscovery();
        if (this.mAdapter == null) {
            this.state = 0;
            Log.e(TAG, "Bluetooth is not support");
        } else if (!this.mAdapter.isEnabled()) {
            this.state = 0;
            Log.e(TAG, "Bluetooth is not open");
        } else {
            try {
                if (BluetoothAdapter.checkBluetoothAddress(this.macAddress)) {
                    this.device = this.mAdapter.getRemoteDevice(this.macAddress);
                    this.mSocket = this.device.createInsecureRfcommSocketToServiceRecord(SERIAL_PORT_SERVICE_CLASS_UUID);
                    this.mSocket.connect();
                    this.initSocketStream();
                    this.state = 3;
                    return true;
                }

                this.state = 0;
                Log.e(TAG, "Bluetooth address is invalid");
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }

        this.macAddress = "";
        return false;
    }

    public BluetoothPort(String macAddress) {
        this.macAddress = macAddress;
    }

    public void writeData(byte[] data) {
        if (this.mSocket != null && this.outputStream != null && data != null && data.length > 0) {
            try {
                this.outputStream.write(data, 0, data.length);
                this.outputStream.flush();
            } catch (IOException var3) {
                Log.e(TAG, "Exception occured while sending data immediately: ", var3);
            }
        }

    }

    public void writeDataImmediately(Vector<Byte> data) throws IOException {
        this.writeDataImmediately(data, 0, data.size());
    }

    public void writeDataImmediately(Vector<Byte> data, int offset, int len) throws IOException {
        if (this.mSocket != null && this.outputStream != null && data != null && data.size() > 0) {
            try {
                this.outputStream.write(this.convertVectorByteToBytes(data), offset, len);
                this.outputStream.flush();
            } catch (IOException var5) {
                Log.e(TAG, "Exception occured while sending data immediately: ", var5);
                throw var5;
            }
        }

    }

    public int readData(byte[] bytes) throws IOException {
        if (this.mSocket == null) {
            throw new IOException();
        } else {
            boolean var2 = false;

            try {
                int len = this.inputStream.read(bytes);
                Log.e(TAG, "read length" + len);
                return len;
            } catch (IOException var4) {
                var4.printStackTrace();
                Log.e(TAG, "connection device is lost");
                throw var4;
            }
        }
    }

    public boolean closePort() {
        try {
            this.closeConn();
            this.state = 0;
            return true;
        } catch (IOException var2) {
            Log.e(TAG, "Close port error! ", var2);
            return false;
        }
    }

    private void closeConn() throws IOException {
        if (this.inputStream != null) {
            this.inputStream.close();
            this.inputStream = null;
        }

        if (this.outputStream != null) {
            this.outputStream.close();
            this.outputStream = null;
        }

        if (this.mSocket != null) {
            this.mSocket.close();
            this.mSocket = null;
        }

    }

    byte[] convertVectorByteToBytes(Vector<Byte> data) {
        byte[] sendData = new byte[data.size()];
        if (data.size() > 0) {
            for(int i = 0; i < data.size(); ++i) {
                sendData[i] = (Byte)data.get(i);
            }
        }

        return sendData;
    }
}
