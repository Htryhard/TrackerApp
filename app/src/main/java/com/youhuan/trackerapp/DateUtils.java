package com.youhuan.trackerapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期处理工具类
 * Created by Huan on 2017/3/20 0020.
 */

public class DateUtils {
    private static SimpleDateFormat sf = null;

    /*获取系统时间 格式为："yyyy/MM/dd "*/
    public static String getCurrentDate() {
        Date d = new Date();
        sf = new SimpleDateFormat("yyyy-MM-dd");
        return sf.format(d);
    }

    /*时间戳转换成字符窜*/
    public static String getDateToString(long time) {
        Date d = new Date(time * 1000L);
        sf = new SimpleDateFormat("yyyy年MM月dd日");
        return sf.format(d);
    }


    /*将字符串转为时间戳*/
    public static long getStringToDate(String time) {
        sf = new SimpleDateFormat("yyyy年MM月dd日");
        Date date = new Date();
        try {
            date = sf.parse(time);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date.getTime();
    }

    public static String getEndDate(long time, long val) {
        val *= 60;
        time += val;
        Date d = new Date(time * 1000L);
        sf = new SimpleDateFormat("yyyy年MM月dd日");
        return sf.format(d);
    }

    /*时间戳转换成字符窜*/
    public static String getDateHMSToString(long time) {
        Date d = new Date(time * 1000L);
        sf = new SimpleDateFormat("HH:mm:ss");
        return sf.format(d);
    }

    public static String getEndDateHMS(long time, long val) {
        val *= 60;
        time += val;
        Date d = new Date(time * 1000L);
        sf = new SimpleDateFormat("HH:mm:ss");
        return sf.format(d);
    }

    public static String getDateStr(String str) {
        DateFormat df = new SimpleDateFormat(" yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String d = df.format(str);
        return d;
    }

    /**
     * 得到系统当前日期的前或者后几天
     *
     * @param iDate 如果要获得前几天日期，该参数为负数； 如果要获得后几天日期，该参数为正数
     * @return Date 返回系统当前日期的前或者后几天
     * @see Calendar#add(int, int)
     */
    public static Date getDateBeforeOrAfter(int iDate) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, iDate);
        return cal.getTime();
    }

    public static String getDateString(Date date) {
        sf = new SimpleDateFormat("yyyy年MM月dd日");
        return sf.format(date);
    }

    /**
     * 服务器上的时间转换成客户端时间
     */
    private String serverToClientTime(String times) {
        if (times == null)
            return "";
        Calendar serverNow = Calendar.getInstance();
        //从PHP转成Java的时间值,在末尾添加三位
        try {
            serverNow.setTime(new Date(Long.parseLong(times + "000")));
        } catch (NumberFormatException e) {
            return times;
        }
        int serverHour = serverNow.get(Calendar.HOUR_OF_DAY);
        int serverMinute = serverNow.get(Calendar.MINUTE);

        return serverHour + ":" + serverMinute;
    }
}
