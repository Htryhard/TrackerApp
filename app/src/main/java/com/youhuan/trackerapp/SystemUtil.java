package com.youhuan.trackerapp;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.security.MessageDigest;
import java.util.Locale;

/**
 * 系统信息工具类
 * Created by YouHuan on 17/11/8.
 */

public class SystemUtil {
    /**
     * 获取当前手机系统语言。
     *
     * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN”
     */
    public static String getSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 获取当前系统上的语言列表(Locale列表)
     *
     * @return 语言列表
     */
    public static Locale[] getSystemLanguageList() {
        return Locale.getAvailableLocales();
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return 系统版本号
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 获取手机IMEI(需要“android.permission.READ_PHONE_STATE”权限)
     *
     * @return 手机IMEI
     */
    public static String getIMEI(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Activity.TELEPHONY_SERVICE);
        if (tm != null) {
            return tm.getDeviceId();
        }
        return null;
    }

    /***
     * 获取16位唯一标识码 需要权限
     * <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
     * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
     * <uses-permission android:name="android.permission.INTERNET" />
     *
     * @return
     */
    public static String getUniqueID(Context ctx) {
        String mIMEI = "";
        String mWLAN_MAC = "";
        try {
            TelephonyManager TelephonyMgr = (TelephonyManager) ctx.getSystemService(ctx.TELEPHONY_SERVICE);
            mIMEI = TelephonyMgr.getDeviceId();
        } catch (Exception ex) {
            System.out.println(ex);
        }
        try {
            WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            mWLAN_MAC = wm.getConnectionInfo().getMacAddress();
        } catch (Exception ex) {
            System.out.println(ex);
        }
        System.out.println("====mIMEI=" + mIMEI);
        System.out.println("====mWLAN_MAC=" + mWLAN_MAC);
        System.out.println("====Build.BOARD=" + Build.BOARD);
        System.out.println("====Build.SERIAL=" + Build.SERIAL);
        System.out.println("====Build.MODEL=" + Build.MODEL);

        String mUniqueID = md5_16b(mIMEI + Build.BOARD + Build.SERIAL + mWLAN_MAC + Build.MODEL).toUpperCase();

        System.out.println("====mUniqueID=" + mUniqueID);
        return mUniqueID;
    }


    /***
     * 16位MD5
     *
     * @param sourceStr
     * @return
     */
    public final static String md5_16b(String sourceStr) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sourceStr.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            result = buf.toString().substring(8, 24);
        } catch (Exception e) {
            System.out.println(e);
        }
        return result;
    }
}
