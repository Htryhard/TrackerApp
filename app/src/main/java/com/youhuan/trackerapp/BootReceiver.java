package com.youhuan.trackerapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * 广播接收器
 * Created by YouHuan on 17/11/8.
 */

public class BootReceiver extends BroadcastReceiver {
    private PendingIntent mAlarmSender;

    @Override
    public void onReceive(Context context, Intent intent) {
        // 在这里干你想干的事（启动一个Service，Activity等），本例是启动一个定时调度程序，每30分钟启动一个Service去更新数据
//        mAlarmSender = PendingIntent.getService(context, 0, new Intent(context,
//                RefreshDataService.class), 0);
//        long firstTime = SystemClock.elapsedRealtime();
//        AlarmManager am = (AlarmManager) context
//                .getSystemService(Activity.ALARM_SERVICE);
//        am.cancel(mAlarmSender);
//        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime,
//                30 * 60 * 1000, mAlarmSender);
        Log.e("----------->", "收到开机广播！");
//        Toast.makeText(context,"收到开机广播！",Toast.LENGTH_SHORT).show();
        if (!isServiceWork(context, "com.youhuan.trackerapp.PollingService")) {
            //开启服务
            Intent s = new Intent(context, PollingService.class);
            context.startService(s);
        }
    }

    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param serviceName 是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(100);
        if (myList.size() <= 0) {
            return false;
        }
        int size = myList.size();
        for (int i = 0; i < size; i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
}