package com.wuyz.fastlockscreen;

import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private static final boolean CLOSE_NETWORK = false;
    
    private DevicePolicyManager mDevicePolicyManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDevicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        final ComponentName componentName = new ComponentName(this, MyDeviceAdminReceiver.class);
        if (!mDevicePolicyManager.isAdminActive(componentName)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            //intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "explanation");
            startActivityForResult(intent, 1);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (CLOSE_NETWORK) {
                        setWlan(false);
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
                            setDataConnect(false);
                        else
                            setDataConnect2(false);
                    }
                    lockScreen();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                }
            }).start();
        }
    }

    private void lockScreen() {
        try {
            mDevicePolicyManager.lockNow();
        } catch (Exception e) {
            Log2.e(TAG, "lockScreen", e);
        }
    }

    private void setWlan(boolean enable) {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        int state = wifiManager.getWifiState();
        if (!enable) {
            if (state == WifiManager.WIFI_STATE_ENABLED || state == WifiManager.WIFI_STATE_ENABLING) {
                Log2.d(TAG, "setWlan disable");
                wifiManager.setWifiEnabled(false);
            }
        } else {
            if (state == WifiManager.WIFI_STATE_DISABLED || state == WifiManager.WIFI_STATE_DISABLING) {
                Log2.d(TAG, "setWlan enable");
                wifiManager.setWifiEnabled(true);
            }
        }
    }

    public void setDataConnect(boolean enable) {
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (!enable) {
            try {
//                Method[] methods = ConnectivityManager.class.getDeclaredMethods();
//                for (Method m : methods) {
//                    Log2.d(TAG, "setDataConnect: %s", m);
//                }
                Method method = ConnectivityManager.class.getDeclaredMethod("getMobileDataEnabled", (Class[])null);
                boolean enabled = (Boolean)method.invoke(connectivityManager, (Object[])null);
                //Log2.d(TAG, "setDataConnect: enabled[%b]", enabled);
                if (enabled) {
                    Method method2 = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
                    method2.setAccessible(true);
//                    Log2.d(TAG, "setDataConnect: method[%s]", method);
                    method2.invoke(connectivityManager, false);
                    Log2.d(TAG, "setDataConnect: end");
                    Thread.sleep(1000);
                    enabled = (Boolean)method.invoke(connectivityManager, (Object[])null);
                    Log2.d(TAG, "setDataConnect: enabled[%b]", enabled);
                }
            } catch (Exception e) {
                Log2.e(TAG, "setDataConnect", e);
            }
        } else {
            try {
                Method method = ConnectivityManager.class.getDeclaredMethod("getMobileDataEnabled", (Class[])null);
                boolean enabled = (Boolean)method.invoke(connectivityManager, (Object[])null);
                if (!enabled) {
                    method = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
                    method.invoke(connectivityManager, true);
                }
            } catch (Exception e) {
                Log2.e(TAG, "setDataConnect", e);
            }
        }
    }

    /**
     * if sdk version newer than Build.VERSION_CODES.KITKAT, use this
     * */
    public void setDataConnect2(boolean enable) {
        final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (!enable) {
            try {
//                Method[] methods = telephonyManager.class.getDeclaredMethods();
//                for (Method m : methods) {
//                    Log2.d(TAG, "setDataConnect: %s", m);
//                }
                Method method = TelephonyManager.class.getDeclaredMethod("getDataEnabled", (Class[])null);
                boolean enabled = (Boolean)method.invoke(telephonyManager, (Object[])null);
                Log2.d(TAG, "setDataConnect2: enabled[%b]", enabled);
                if (enabled) {
                    Method method2 = TelephonyManager.class.getDeclaredMethod("setDataEnabled", boolean.class);
                    method2.setAccessible(true);
                    Log2.d(TAG, "setDataConnect2: method[%s]", method);
                    method2.invoke(telephonyManager, false);
                    Log2.d(TAG, "setDataConnect2: end");
                    Thread.sleep(1000);
                    enabled = (Boolean)method.invoke(telephonyManager, (Object[])null);
                    Log2.d(TAG, "setDataConnect2: enabled[%b]", enabled);
                }
            } catch (Exception e) {
                Log2.e(TAG, "setDataConnect2", e);
            }
        } else {
            try {
                Method method = TelephonyManager.class.getDeclaredMethod("getDataEnabled", (Class[])null);
                boolean enabled = (Boolean)method.invoke(telephonyManager, (Object[])null);
                if (!enabled) {
                    method = TelephonyManager.class.getDeclaredMethod("setDataEnabled", boolean.class);
                    method.invoke(telephonyManager, true);
                }
            } catch (Exception e) {
                Log2.e(TAG, "setDataConnect2", e);
            }
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, Intent data) {
        Log2.d(TAG, "onActivityResult: requestCode[%d], resultCode[%d], data[%s]",
                requestCode, resultCode, data);
        new Thread(new Runnable() {
            @Override
            public void run() {
                setWlan(false);
                setDataConnect(false);
                if (requestCode == 1 && resultCode == RESULT_OK) {
                    lockScreen();
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
            }
        }).start();
    }

    public static class MyDeviceAdminReceiver extends DeviceAdminReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log2.d(TAG, "onReceive: %s", intent.getAction());
            super.onReceive(context, intent);
        }
    }
}
