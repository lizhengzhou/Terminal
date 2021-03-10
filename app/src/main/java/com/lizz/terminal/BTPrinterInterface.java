package com.lizz.terminal;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;

import java.io.UnsupportedEncodingException;

public class BTPrinterInterface {

    private static final String TAG = BTPrinterInterface.class.getSimpleName();

    public static final String name = "btPrinter";

    private Context context;
    Handler mHandler;

    private BluetoothPort mPort = null;
    private String macAddress = null;
    private boolean hasOpen = false;
    PrinterStatus status = PrinterStatus.Normal;
    PrinterReader statusReader;
    /**
     * TSC查询打印机状态指令
     */
    private byte[] tsc = {0x1b, '!', '?'};

    /**
     * TSC指令查询打印机实时状态 打印机缺纸状态
     */
    private static final int TSC_STATE_PAPER_ERR = 0x04;

    /**
     * TSC指令查询打印机实时状态 打印机开盖状态
     */
    private static final int TSC_STATE_COVER_OPEN = 0x01;

    /**
     * TSC指令查询打印机实时状态 打印机出错状态
     */
    private static final int TSC_STATE_ERR_OCCURS = 0x80;


    public BTPrinterInterface(Context context, Handler handler) {

        this.context = context;
        this.mHandler = handler;
    }

    @JavascriptInterface
    public boolean open(String macAddress) {
        try {

            if (mPort != null && !this.macAddress.equals(macAddress)) {
                close();
            }

            if (mPort == null) {
                mPort = new BluetoothPort(macAddress);
                this.macAddress = macAddress;
                hasOpen = false;
            }

            if (!hasOpen) {
                hasOpen = mPort.openPort();

                if (hasOpen) {

                    status = PrinterStatus.Opened;

                    statusReader = new PrinterReader();
                    statusReader.start();

                }

                return hasOpen;
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @JavascriptInterface
    public boolean print(String data) {

        if (mPort == null) return false;

        try {
            byte[] buf = data.getBytes("GB18030");

            mPort.writeData(buf);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @JavascriptInterface
    public void close() {
        hasOpen = false;
        macAddress = null;
        status = PrinterStatus.Normal;
        if (statusReader != null) {
            statusReader.cancel();
            statusReader = null;
        }
        if (mPort != null) {
            mPort.closePort();
            mPort = null;
        }

    }


    class PrinterReader extends Thread {
        private boolean isRun = true;
        private byte[] buffer = new byte[100];

        @Override
        public void run() {

            try {
                while (isRun) {
                    if (hasOpen) {
                        Log.e(TAG, "write tsc ");
                        mPort.writeData(tsc);

                        Thread.sleep(1000);

                        int available = mPort.inputStream.available();
                        Log.e(TAG, "available read " + available);
                        if (available > 0) {
                            //读取打印机返回信息,打印机没有返回纸返回-1
                            Log.e(TAG, "wait read ");
                            int len = mPort.readData(buffer);
                            Log.e(TAG, " read " + len);
                            if (len > 0) {
                                // 判断是实时状态（10 04 02）还是查询状态（1D 72 01）
                                int result = (byte) ((buffer[0] & 0x10) >> 4);//数据右移
                                if (len == 1) {
                                    //查询打印机实时状态

                                    if ((buffer[0] & TSC_STATE_PAPER_ERR) > 0) {//缺纸
                                        status = PrinterStatus.OutOfPaper;
                                    }
                                    if ((buffer[0] & TSC_STATE_COVER_OPEN) > 0) {//开盖
                                        status = PrinterStatus.CoverOpened;
                                    }
                                    if ((buffer[0] & TSC_STATE_ERR_OCCURS) > 0) {//打印机报错
                                        status = PrinterStatus.ErrOccurs;
                                    }

                                    Message message = Message.obtain();
                                    message.what = 0x01;
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("STATUS", status.getCode());
                                    message.setData(bundle);
                                    mHandler.sendMessage(message);

                                }
                            }

                        }

                    }
                }
            } catch (Exception e) {//异常断开

                e.printStackTrace();

                close();

                Message message = Message.obtain();
                message.what = 0x01;
                Bundle bundle = new Bundle();
                bundle.putInt("status", PrinterStatus.ConnectLost.getCode());
                message.setData(bundle);
                mHandler.sendMessage(message);

            }

        }

        public void cancel() {
            isRun = false;
        }
    }


}
