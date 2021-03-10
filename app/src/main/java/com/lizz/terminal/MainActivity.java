package com.lizz.terminal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    WebView mWebview;
    WebSettings mWebSettings;

    String _curUrl = "file:///android_asset/index.html";

    public static final int REQUEST_PERMISSION = 1;
    public static final int REQUEST_ENABLE_BT = 2;

    BTPrinterInterface btPrinter;

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        initBluetooth();

        mWebview = findViewById(R.id.webView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        mWebSettings = mWebview.getSettings();
        mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setUseWideViewPort(true);
        mWebSettings.setLoadWithOverviewMode(true);

        btPrinter = new BTPrinterInterface(this, mHandler);

        mWebview.addJavascriptInterface(btPrinter, "btPrinter");

        mWebview.loadUrl(_curUrl);

        //设置不用系统浏览器打开,直接显示在当前Webview
        mWebview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //Android 8.0以下版本的需要返回true 并且需要loadUrl()
                if (Build.VERSION.SDK_INT < 26) {
                    view.loadUrl(url);
                    return true;
                }
                return false;
            }
        });

        //设置WebChromeClient类
        mWebview.setWebChromeClient(new WebChromeClient() {
            //获取网站标题
            @Override
            public void onReceivedTitle(WebView view, String title) {

            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.i(TAG, consoleMessage.message());
                return super.onConsoleMessage(consoleMessage);
            }

            //获取加载进度
            @Override
            public void onProgressChanged(WebView view, int newProgress) {

            }
        });
        //设置WebViewClient类
        mWebview.setWebViewClient(new WebViewClient() {
            //设置加载前的函数
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

            }

            //设置结束加载函数
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setTitle(String.valueOf(view.getTitle()));
            }
        });

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
        if (btPrinter != null) {
            btPrinter.close();
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
            ActivityCompat.requestPermissions(this, per.toArray(p), REQUEST_PERMISSION);
        }

    }


    private void initBluetooth() {
        // Get the local Bluetooth adapter
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported by the device", Toast.LENGTH_LONG);
        } else {
            // If BT is not on, request that it be enabled.
            // setupChat() will then be called during onActivityResult
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            }
        }
    }


    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {

                case 0x01:

                    int status = msg.getData().getInt("STATUS");

                    Log.i(TAG, "status:" + status);

                    PrinterStatus currentStatus = PrinterStatus.getByCode(status);

                    if (mWebview != null) {
                        mWebview.evaluateJavascript(BTPrinterInterface.name + ".onStatus" + "('" + currentStatus.getName() + "')", null);
                    }

                    break;

            }
        }
    };

}
