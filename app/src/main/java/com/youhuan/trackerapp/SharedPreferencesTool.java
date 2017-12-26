package com.youhuan.trackerapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.SharedPreferencesCompat;
import android.util.Log;

import java.util.Map;

/**
 * SharedPreferences操作工具类
 * Created by Huan on 2017/3/16 0016.
 */

public class SharedPreferencesTool {

    /**
     * 保存在手机里面的文件名
     */
    public static final String FILE_NAME = "share_data";

    /**
     * 保存字符串
     *
     * @param context
     * @param key
     * @param value
     * @return
     */
    public static boolean putString(Context context, String key, String value) {
        try {
            SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(key, value);
            editor.apply();
            return true;
        } catch (Exception e) {
            Log.e("==++==", "SharedPreferences报错。msg:" + e.getMessage());
            return false;
        }
    }

    /**
     * 获取字符串
     *
     * @param context
     * @param key
     * @return
     */
    public static String getString(Context context, String key) {
        SharedPreferences preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return preferences.getString(key, "");
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param context
     * @param key
     */
    public static void remove(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * 清除所有数据
     *
     * @param context
     */
    public static void clear(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * 查询某个key是否已经存在
     *
     * @param context
     * @param key
     * @return
     */
    public static boolean contains(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        return sp.contains(key);
    }

    /**
     * 返回所有的键值对
     *
     * @param context
     * @return
     */
    public static Map<String, ?> getAll(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        return sp.getAll();
    }

}
