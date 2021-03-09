package com.lizz.terminal;

import android.content.Context;
import android.webkit.JavascriptInterface;

import java.io.UnsupportedEncodingException;

public class BTPrinterInterface {

    private Context context;

    private BluetoothPort mPort = null;
    private String macAddress = null;
    private boolean hasOpen = false;

    public BTPrinterInterface(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public boolean open(String macAddress) {
        try {

            if (mPort == null || !this.macAddress.equals(macAddress)) {

                if (mPort != null) mPort.closePort();

                mPort = new BluetoothPort(macAddress);
                hasOpen = false;
            }

            if (!hasOpen) {
                hasOpen = mPort.openPort();
                return hasOpen;
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @JavascriptInterface
    public void print(String data) {

        if (mPort == null) return;
        byte[] buf = new byte[0];
        try {
            buf = data.getBytes("GB18030");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        mPort.writeData(buf);

    }

    public void destroy() {
        if (mPort != null) {
            mPort.closePort();
            mPort = null;
        }
    }
}
