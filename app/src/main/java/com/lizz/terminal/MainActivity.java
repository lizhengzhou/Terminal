package com.lizz.terminal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class MainActivity extends Activity {

    WebView mWebview;
    WebSettings mWebSettings;

    String _curUrl = "file:///android_asset/index.html";

    private ThreadPool threadPool;

    BTPrinterInterface btPrinter;

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        mWebview = findViewById(R.id.webView);

        mWebSettings = mWebview.getSettings();
        mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setUseWideViewPort(true);
        mWebSettings.setLoadWithOverviewMode(true);

        btPrinter = new BTPrinterInterface(this);

        mWebview.addJavascriptInterface(btPrinter, "btPrinter");

        mWebview.loadUrl(_curUrl);

        //设置不用系统浏览器打开,直接显示在当前Webview
        mWebview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });

        WebView.setWebContentsDebuggingEnabled(true);

    }

    //fix -> Binary XML file line #9: Error inflating class android.webkit.WebView
    @Override
    public AssetManager getAssets() {
        return getResources().getAssets();
    }

    //点击返回上一页面而不是退出浏览器
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebview.canGoBack()) {
            mWebview.goBack();//返回上个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);//退出H5界面
    }


    //销毁Webview
    @Override
    protected void onDestroy() {
        if(btPrinter!=null){
            btPrinter.destroy();
        }

        if (mWebview != null) {
            mWebview.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mWebview.clearHistory();

            ((ViewGroup) mWebview.getParent()).removeView(mWebview);
            mWebview.destroy();
            mWebview = null;
        }
        super.onDestroy();
    }


    private void checkPermission() {

        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH
        };

        ArrayList<String> per = new ArrayList<>();

        for (String permission : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, permission)) {
                per.add(permission);
            }
        }

        if (per.size() > 0) {
            String[] p = new String[per.size()];
            ActivityCompat.requestPermissions(this, per.toArray(p), 0x004);
        }

    }

//
//    private void printTest() {
//
//        threadPool = ThreadPool.getInstantiation();
//        threadPool.addSerialTask(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                printLabel();
//            }
//        });
//
//        threadPool.addSerialTask(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                printLabel();
//            }
//        });
//
//        threadPool.addSerialTask(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                printLabel();
//            }
//        });
//
//    }
//
//    private void printLabel() {
//
//        BluetoothPort mPort = new BluetoothPort("DC:1D:30:B8:50:EB");
//
//        mPort.openPort();
//
//        byte[] byteArray = Base64.getDecoder().decode(base64Str);
//
//        Vector<Byte> data = new Vector();
//
//        for (int i = 0; i < byteArray.length; ++i) {
//            data.add(byteArray[i]);
//        }
//
//        try {
//            mPort.writeDataImmediately(data);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        mPort.closePort();
//
//    }
//
//    String base64Str = "U1BFRUQgNg0KREVOU0lUWSA4DQpTRVQgUEVFTCBPRkYNClNFVCBDVVRURVIgT0ZGDQpTRVQgUEFSVElBTF9DVVRURVIgT0ZGDQpTRVQgVEVBUiBPTg0KRElSRUNUSU9OIDENClNJWkUgMTAwLjAwIG1tLDgwLjAwIG1tDQpHQVAgMy4wMCBtbSwwLjAwIG1tDQpPRkZTRVQgMC4wMCBtbQ0KU0hJRlQgMA0KUkVGRVJFTkNFIDAsMA0KRE9XTkxPQUQgIkdSQVBISUMxLlBDWCIsMzM1NSwKBQEBAAAAAJIAkgBIAEgAAAAA////AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEUAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMYAf8H/wfwHwfAfwcDFAB8AxgB/wf/B/AfB8B/BwMUAHwDGAH/B/8H8B8HwH8HAxQAfAMYAf8H/wfwHwfAfwcDFAB8AxgB/wf/B/AfB8B/BwMUAHwDGAH/B/8H8B8HwH8HAxQAfAMYAf8H/wfwHwfAfwcDFAB8AAcT/wcB/w//B8B/BwH/D/8HwHwABxP/BwH/D/8HwH8HAf8P/wfAfAAHE/8HAf8P/wfAfwcB/w//B8B8AAcT/wcB/w//B8B/BwH/D/8HwHwABxP/BwH/D/8HwH8HAf8P/wfAfAAHE/8HAf8P/wfAfwcB/w//B8B8AAcT/wcB/w//B8B/BwH/D/8HwHwABwfzCAB/BwH8BwfwHwv/BwH/CAAfB8B8AAcH8wgAfwcB/AcH8B8L/wcB/wgAHwfAfAAHB/MIAH8HAfwHB/AfC/8HAf8IAB8HwHwABwfzCAB/BwH8BwfwHwv/BwH/CAAfB8B8AAcH8wgAfwcB/AcH8B8L/wcB/wgAHwfAfAAHB/MIAH8HAfwHB/AfC/8HAf8IAB8HwHwABwfzCAB/BwH8BwfwHwv/BwH/CAAfB8B8AAcH8wgAfwcB/w//B8B/BwH/CAAfB8B8AAcH8wgAfwcB/w//B8B/BwH/CAAfB8B8AAcH8wgAfwcB/w//B8B/BwH/CAAfB8B8AAcH8wgAfwcB/w//B8B/BwH/CAAfB8B8AAcH8wgAfwcB/w//B8B/BwH/CAAfB8B8AAcH8wgAfwcB/w//B8B/BwH/CAAfB8B8AAcH8wgAfwcB/w//B8B/BwH/CAAfB8B8AAcH8wgAfwcB/wf4DwfgAH8HAf8IAB8HwHwABwfzCAB/BwH/B/gPB+AAfwcB/wgAHwfAfAAHB/MIAH8HAf8H+A8H4AB/BwH/CAAfB8B8AAcH8wgAfwcB/wf4DwfgAH8HAf8IAB8HwHwABwfzCAB/BwH/B/gPB+AAfwcB/wgAHwfAfAAHB/MIAH8HAf8H+A8H4AB/BwH/CAAfB8B8AAcH8wgAfwcB/wf4DwfgAH8HAf8IAB8HwHwABxP/BwH/B/sIAD8H/wcB/w//B8B8AAcT/wcB/wf7CAA/B/8HAf8P/wfAfAAHE/8HAf8H+wgAPwf/BwH/D/8HwHwABxP/BwH/B/sIAD8H/wcB/w//B8B8AAcT/wcB/wf7CAA/B/8HAf8P/wfAfAAHE/8HAf8H+wgAPwf/BwH/D/8HwHwABxP/BwH/B/sIAD8H/wcB/w//B8B8AxgB/AcH8B8HwH8HAxQAfAMYAfwHB/AfB8B/BwMUAHwDGAH8BwfwHwfAfwcDFAB8AxgB/AcH8B8HwH8HAxQAfAMYAfwHB/AfB8B/BwMUAHwDGAH8BwfwHwfAfwcDFAB8AxgB/AcH8B8HwH8HAxQAfAMf/AcH8B8n/AMf/AcH8B8n/AMf/AcH8B8n/AMf/AcH8B8n/AMf/AcH8B8n/AMf/AcH8B8n/AMf/AcH8B8n/AMIAB8HwwwABwfwHwfDCAH/B/8H8B8L/AMIAB8HwwwABwfwHwfDCAH/B/8H8B8L/AMIAB8HwwwABwfwHwfDCAH/B/8H8B8L/AMIAB8HwwwABwfwHwfDCAH/B/8H8B8L/AMIAB8HwwwABwfwHwfDCAH/B/8H8B8L/AMIAB8HwwwABwfwHwfDCAH/B/8H8B8L/AMIAB8HwwwABwfwHwfDCAH/B/8H8B8L/AAHD/8HgP4DB/sIAD8HgP4DD/8HwHwABw//B4D+Awf7CAA/B4D+Aw//B8B8AAcP/weA/gMH+wgAPweA/gMP/wfAfAAHD/8HgP4DB/sIAD8HgP4DD/8HwHwABw//B4D+Awf7CAA/B4D+Aw//B8B8AAcP/weA/gMH+wgAPweA/gMP/wfAfAAHD/8HgP4DB/sIAD8HgP4DD/8HwHwDDAA/B4AB/wf4DwfgPweDCAMH+A8P/AMMAD8HgAH/B/gPB+A/B4MIAwf4Dw/8AwwAPweAAf8H+A8H4D8HgwgDB/gPD/wDDAA/B4AB/wf4DwfgPweDCAMH+A8P/AMMAD8HgAH/B/gPB+A/B4MIAwf4Dw/8AwwAPweAAf8H+A8H4D8HgwgDB/gPD/wDDAA/B4AB/wf4DwfgPweDCAMH+A8P/AMH+A8H/wfAAP8H/AcH8wwA/gAADwf/B8B8Awf4Dwf/B8AA/wf8BwfzDAD+AAAPB/8HwHwDB/gPB/8HwAD/B/wHB/MMAP4AAA8H/wfAfAMH+A8H/wfAAP8H/AcH8wwA/gAADwf/B8B8Awf4Dwf/B8AA/wf8BwfzDAD+AAAPB/8HwHwDB/gPB/8HwAD/B/wHB/MMAP4AAA8H/wfAfAMH+A8H/wfAAP8H/AcH8wwA/gAADwf/B8B8AAcH8AA/B/8HAAMH/wfwAD8HgwgDC/8H4AB8AAcH8AA/B/8HAAMH/wfwAD8HgwgDC/8H4AB8AAcH8AA/B/8HAAMH/wfwAD8HgwgDC/8H4AB8AAcH8AA/B/8HAAMH/wfwAD8HgwgDC/8H4AB8AAcH8AA/B/8HAAMH/wfwAD8HgwgDC/8H4AB8AAcH8AA/B/8HAAMH/wfwAD8HgwgDC/8H4AB8AAcH8AA/B/8HAAMH/wfwAD8HgwgDC/8H4AB8Ax/8BwfwHwv/BwH/B/gPB+A/B/wDH/wHB/AfC/8HAf8H+A8H4D8H/AMf/AcH8B8L/wcB/wf4DwfgPwf8Ax/8BwfwHwv/BwH/B/gPB+A/B/wDH/wHB/AfC/8HAf8H+A8H4D8H/AMf/AcH8B8L/wcB/wf4DwfgPwf8Ax/8BwfwHwv/BwH/B/gPB+A/B/wDGAH8AA8H/wfAfwv8Bwv/B8B8AxgB/AAPB/8HwH8L/AcL/wfAfAMYAfwADwf/B8B/C/wHC/8HwHwDGAH8AA8H/wfAfwv8Bwv/B8B8AxgB/AAPB/8HwH8L/AcL/wfAfAMYAfwADwf/B8B/C/wHC/8HwHwDGAH8AA8H/wfAfwv8Bwv/B8B8AAcT/wcB/AcT/wcB/wv/B+AAfAAHE/8HAfwHE/8HAf8L/wfgAHwABxP/BwH8BxP/BwH/C/8H4AB8AAcT/wcB/AcT/wcB/wv/B+AAfAAHE/8HAfwHE/8HAf8L/wfgAHwABxP/BwH8BxP/BwH/C/8H4AB8AAcT/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";

}
