package com.admai.sdk.mai;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.admai.sdk.str.MaiMonitor;
import com.admai.sdk.str.MaiPath;
import com.admai.sdk.str.MaiRequest;
import com.admai.sdk.type.MaiLType;
import com.admai.sdk.util.AdvertisingIdClient;
import com.admai.sdk.util.MD5;
import com.admai.sdk.util.MaiUtils;
import com.admai.sdk.util.log.L;
import com.admai.sdk.util.log.LogSSUtil;
import com.admai.sdk.util.log.LogUtil;
import com.admai.sdk.util.log.MaiLogs;
import com.admai.sdk.util.persistance.SharePreferencePersistance;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by macmi001 on 16/7/4.
 * 设备信息管理
 * 在app 刚启动的时候就要new 获取设备信息 放入
 *
 * @NotProguard
 */
public class MaiManager {
    private static final String TAG = MaiManager.class.getSimpleName();
    public static String deviceid;
    private static String adID; //android 系统的广告id 
    private static String tel;
    private static String iccid;
    private static String imsi;
    private static String android_id;
    private static String aaid;
    private static String mobileName;
    public static String osVersion;
    private static String macAddress;
    private static String softwareVersion;
    private static String phoneType;
    private static String applicationName;
    private static String packageName;
    private static int operatorName;
    private static int networktype;
    private static String ipaddress;
    private static String user_agent;
    public static int screenwidth;
    public static int screenheight;
    private static String manufacturer;
    private static String model;
    private static String language;
    private static String postion;
    private static String wifissid;
    private static ArrayList<Integer> type;
    private static volatile MaiManager sMaiManager;
    public static DisplayMetrics outMetrics;

    //app 应用的分类编号
    private static String cat;
    //应用商店的编号
    private static String mkt;
    //app 在上述应用商店内的编号
    private static String mkt_app;
    //app 在上述应用商店内的分类编号
    private static String mkt_cat;
    //app 在上述应用商店内的标签(英文或中文 UTF8- R urlencode 编码) 多个标签使用半角逗号分隔
    private static String mkt_tag;
    private String provider;

    private double latitude = 0.0;   //维度
    private double longitude = 0.0;  //经度
    private Timer mTimer = null;


    public static MaiManager getInstance(Context context) {
        if (sMaiManager == null) {

            synchronized (MaiManager.class) {
                if (sMaiManager == null) {

                    sMaiManager = new MaiManager(context);
                }
            }
        }
        
        return sMaiManager;
    }


    public static String getaplctInfo() {
        return "applicationName:" + applicationName + "--" + "packageName:" + packageName;
    }

    public static int getNetworktype() {
        return networktype;
    }

    /**
     * @param _cat
     *     app 应用的分类编号
     * @NotProguard
     */

    public void setcat(String _cat) {
        cat = _cat;
    }

    /**
     * @param _mkt
     *     应用商店的编号
     * @NotProguard
     */

    public void setmkt(String _mkt) {
        mkt = _mkt;
    }

    /**
     * @param _mkt_app
     *     app 在上述应用商店内的编号
     * @NotProguard
     */
    public void setmkt_app(String _mkt_app) {
        mkt_app = _mkt_app;
    }

    /**
     * @param _mkt_cat
     *     app 在上述应用商店内的分类编号
     * @NotProguard
     */
    public void setmkt_cat(String _mkt_cat) {
        mkt_cat = _mkt_cat;
    }

    /**
     * @param _mkt_tag
     *     在上述应用商店内的标签(英文或中文 UTF8- R urlencode 编码) 多个标签使用半角逗号分隔
     * @NotProguard
     */
    public void setmkt_tag(String _mkt_tag) {
        mkt_tag = _mkt_tag;
    }

    public void setType(boolean hasImage, boolean hasFlash, boolean hasHtml, boolean hasVideo) {
        type = new ArrayList<>();
        if (hasImage) {
            type.add(1);
        }
        if (hasFlash) {
            type.add(2);
        }
        if (hasHtml) {
            type.add(4);
        }
        if (hasVideo) {
            type.add(5);
        }

        if (!hasImage && !hasFlash && !hasHtml && !hasVideo) {
            type = null;
        }

    }

    Context context;
    TelephonyManager tm;
    LocationManager mLocationManager;
    WifiManager wifi;
    public static MaiManager maiManager;

    public MaiManager(Context _context) {
        context = _context;
        maiManager = this;
    }

    public void getSysInfoandSendLogs() {
        getSysInfo(context);
        MaiLogs maiLogs = new MaiLogs();
        maiLogs.m_event_type = MaiLType.SYS_INFO;
        maiLogs.m_event_time = MaiUtils.getCurrentTime();
        maiLogs.m_app_info = getaplctInfo();
        maiLogs.m_sdk_version = MaiPath.MAI_VERSION;
        maiLogs.m_net_type = getNetworktype();
        maiLogs.m_d_info = "MANUFACTURER:" + Build.MANUFACTURER + "--MODEL:" + Build.MODEL;
        maiLogs.m_d_id = deviceid;
        maiLogs.m_d_version = osVersion;
        maiLogs.m_android_id=android_id;
        maiLogs.m_aaid=aaid;
        maiLogs.m_imsi=imsi;
        maiLogs.m_mac=macAddress;
        maiLogs.m_opr = operatorName;
        maiLogs.m_ip = ipaddress;
        maiLogs.m_ua = user_agent;
        maiLogs.m_dvw = String.valueOf(screenwidth);
        maiLogs.m_dvh = String.valueOf(screenheight);
        maiLogs.m_lan = language;
        maiLogs.m_pos = postion;
        maiLogs.m_ssid = wifissid;
//        LogSSUtil.getInstance().sendLogs(maiLogs);
        LogSSUtil.getInstance().saveLogs(maiLogs);
    }

    private void getSysInfo(Context context) {
        MaiPlatformController.getInstance().init(context);
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        // 所有的设备都可以返回一个TelephonyManager.getDeviceId()
        // 如果是GSM网络，返回IMEI；如果是CDMA网络，返回MEID
        // 手机的唯一标识，像GSM手机的 IMEI 和 CDMA手机的 MEID. 但在中国山寨手机导致这个号码不是唯一标识了
        // 取出的值是international mobile Equipment identity手机唯一标识码，即imei；

        deviceid = tm.getDeviceId(); //phone state

        // 对于移动的用户，手机号码(MDN)保存在运营商的服务器中，而不是保存在SIM卡里。SIM卡只保留了IMSI和一些验证信息
        // 取出的 值是手机号，即MSISDN ： mobile subscriber ISDN用户号码，这个是我们说的139，136那个号码；
        // 是通过SIM卡相关文件记录得到的数据
        // 归结到是否可以从SIM卡的EFmsisdn文件取出手机号码了，不幸的是一般运营商不会把用户号码写在这个文件的，为什么呢？
        // 因为这个手机号码是在用户买到卡并开通时才将IMSI和MSISDN对应上的，卡内生产出来时只有IMSI，你不知道用户喜欢那个手机号码，因此一般不先对应IMSI和MSISDN，即时有对应也不写这个文件的。
        tel = tm.getLine1Number();   //phone state

        // 所有的GSM设备(测试设备都装载有SIM卡) 可以返回一个TelephonyManager.getSimSerialNumber()
        // 所有的CDMA 设备对于 getSimSerialNumber() 却返回一个空值！
        // 360手机卫士可能会影响到imei和imsi的获取，禁止了“获取该应用获取设备信息”，改为“允许”即可正常获取IMEI、IMSI
        // 返回SIM卡的序列号(ICCID) ICCID:ICC
        // identity集成电路卡标识，这个是唯一标识一张卡片物理号码的
        iccid = tm.getSimSerialNumber();  //phone state

        // sim卡的序列号(IMSI)，international
        // mobiles subscriber
        // identity国际移动用户号码标识，
        imsi = tm.getSubscriberId();   //phone state
        LogSSUtil.getInstance().setImsi(imsi);
        L.e("lssimsi",imsi);
        softwareVersion = tm.getDeviceSoftwareVersion();// String  //phone state

        // 获取imei和imsi的第二种方法
        // String imsi2 =android.os.SystemProperties.get(
        // android.telephony.TelephonyProperties.PROPERTY_IMSI);
        // String imei2
        // =android.os.SystemProperties.get(android.telephony.TelephonyProperties.PROPERTY_IMEI);
        // 此处获取设备ANDROID_ID
        // 所有添加有谷歌账户的设备可以返回一个 ANDROID_ID
        // 所有的CDMA设备对于 ANDROID_ID 和
        // TelephonyManager.getDeviceId()返回相同的值（只要在设置时添加了谷歌账户）
        // 有些山寨手机这个号码是一个…
        // 是一个64位的数字，在设备第一次启动的时候随机生成并在设备的整个生命周期中不变。（如果重新进行出厂设置可能会改变）
        android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        LogSSUtil.getInstance().setAndroidId(android_id);
        // 获取Android手机型号和OS的版本号
        mobileName = Build.DEVICE;
        osVersion = Build.VERSION.RELEASE;
        wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        

        // String versionName = null;
        // String versionCode = null;
        // PackageManagerpm = this.getPackageManager();
        // PackageInfo pi;
        // try {
        // pi = pm.getPackageInfo(this.getPackageName(), 0);
        // versionName = pi.versionName;
        // versionCode = String.valueOf(pi.versionCode);
        // } catch (NameNotFoundException e) {
        // e.printStackTrace();
        // }

        //获取当前手机支持的移动网络类型
        switch (tm.getPhoneType()) {
            case TelephonyManager.PHONE_TYPE_NONE:
                phoneType = "NONE: ";
                break;
            case TelephonyManager.PHONE_TYPE_GSM:
                phoneType = "GSM: IMEI";
                break;
            case TelephonyManager.PHONE_TYPE_CDMA:
                phoneType = "CDMA: MEID/ESN";
                break;
        /*
        * for API Level 11 or above case TelephonyManager.PHONE_TYPE_SIP:
        * return "SIP";
        */
            default:
                phoneType = "UNKNOWN: ID";
                break;
        }

        //api 23
        if (Build.VERSION.SDK_INT >= 23) {

            try {
                List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface nif : all) {
                    if (nif.getName().equalsIgnoreCase("wlan0")) {
                        byte[] macBytes = nif.getHardwareAddress();
                        if (macBytes == null) {
                            return;
                        }

                        StringBuilder res1 = new StringBuilder();
                        for (byte b : macBytes) {
                            res1.append(String.format("%02X:", b));
                        }

                        if (res1.length() > 0) {
                            res1.deleteCharAt(res1.length() - 1);
                        }
                        macAddress = res1.toString();
                        L.e("sytemInfo", res1.toString());
                    }
                }

            } catch (Exception e) {
                L.e("MobileAcces", "Erreur lecture propriete Adresse MAC ");
            }

        }else {
            WifiInfo info = wifi.getConnectionInfo();      //wifi state
            L.e("wifi info __"+ info);
            macAddress = info.getMacAddress();// 更换为MacAddressWifi地址
        }
        String sytemInfo = "设备名称 : " + mobileName +
                               "\n系统版本:" + osVersion +
                               "\n软件版本: " + softwareVersion +
                               "\n设备ID（imei）: " + deviceid +
                               "\n手机号:  " + tel +
                               "\nSIM卡集成电路卡标识: " + iccid +
                               "\nSIM国际移动号码标示: " + imsi +
                               "\nANDROID_ID: " + android_id +
                               "\n手机网络类型: " + phoneType +
                               "\nMAC地址: " + macAddress;
        L.e("sytemInfo",sytemInfo);
        applicationName = getApplicationName();
        packageName = context.getPackageName();
        operatorName = getOperatorName();

        new Thread() {
            public void run() {
                networktype = networktype();
                aaid = getAdvertisingId();
                LogSSUtil.getInstance().setAaId(aaid);
            }
        }.start();

        user_agent = getUser_agent();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);

        screenwidth = outMetrics.widthPixels;     //屏幕 宽
        screenheight = outMetrics.heightPixels;   //屏幕 高


        manufacturer = android.os.Build.MANUFACTURER;
        model = android.os.Build.MODEL;


        //语言
        Locale locale = context.getResources().getConfiguration().locale;
        language = locale.getLanguage();

        openAndGetLocation();


    }
    
    private String getAdvertisingId() {
        try {
            AdvertisingIdClient.AdInfo adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
            String adid = adInfo.getId();
            //					adid = id;
            //					optOutEnabled = adInfo.isLimitAdTrackingEnabled();
            Log.e("ABC", "advertisingId" + adid);
            //					Log.e("ABC", "optOutEnabled" + optOutEnabled);
            return adid;
        } catch (Exception e) {
            Log.e("adidtest", "advertisingId,error");
            e.printStackTrace();
        }
        return "";
    }
    
    public void initFirst() {
//        MaiReportManager.getInstance(context).registerNetworkStateReceiver();
        MaiReportManager.getInstance(context).restoreTracks();
        MaiReportManager.getInstance(context).restoreMonitors();
    
        if (null != mTimer) {
            mTimer.cancel();
            mTimer = null;

        }

        if (null == mTimer) {

            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {  //
                @Override
                public void run() {
                    LogSSUtil.getInstance().sendLogs();
                }
            }, 0, 5*60000);
        }
    }

    //
    public static MaiMonitor getMonitor(MaiMonitor maiMonitor) {
        maiMonitor.uuid = android_id;
        maiMonitor.loc = postion;
        return maiMonitor;
    }

    //获取到假数据 设备信息     MaiRequest 广告投放请求容器  在这里的信息可能要上传到服务器
    //返回 MaiRequest bean
    public static MaiRequest getRequest(MaiRequest maiRequest) {
        maiRequest.m_os_v = osVersion;
        maiRequest.m1 = android_id;
        maiRequest.m2 = MD5.getMD5(deviceid);
        maiRequest.m3 = deviceid;
        maiRequest.m4 = MD5.getMD5(android_id);
        maiRequest.m6 = MD5.getMD5(macAddress);
        maiRequest.m8 = aaid;
        //        maiRequest.m6 = "isdfhjnaskjb267";

        if (macAddress != null && macAddress.length() > 0) {
            maiRequest.m7 = MD5.getMD5(macAddress.replaceAll(":", ""));
        }
        //        maiRequest.m7 = "aaaetyhrhrtgh545t";

        maiRequest.m_app = applicationName;
        maiRequest.m_app_pn = packageName;
        maiRequest.m_cat = cat;
        maiRequest.m_mkt = mkt;
        maiRequest.m_mkt_app = mkt_app;
        maiRequest.m_mkt_cat = mkt_cat;
        maiRequest.m_mkt_tag = mkt_tag;
        maiRequest.m_opr = operatorName;
        maiRequest.m_net = networktype;
        maiRequest.m_ip = ipaddress;
        maiRequest.m_ua = user_agent;
        //        maiRequest.m_ua = "Mozilla/5.0 (iPhone; CPU iPhone OS 8_0 like Mac OS X) AppleWebKit/600.1.3 (KHTML, like Gecko) Version/8.0 Mobile/12A4345d Safari/600.1.4";
        maiRequest.m_dvw = screenwidth;
        maiRequest.m_dvh = screenheight;
        maiRequest.m_mfr = manufacturer;
        maiRequest.m_mdl = model;
        maiRequest.m_lan = language;
        maiRequest.m_pos = postion;
        maiRequest.m_ssid = wifissid;
        maiRequest.m_type = type;
        ArrayList<Integer> adct = new ArrayList<>();
        adct.add(1);
        maiRequest.m_adct = adct;
        return maiRequest;
    }

    public String getApplicationName() {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = context.getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        String applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
        return applicationName;
    }

    /**
     * 获取运营商名字
     * * 0:未知 1:联通 2:移动 3:电信 4:增值运营商
     */
    private int getOperatorName() {
        String operator = tm.getSimOperator();
        int operatorName = 0;
        if (operator != null) {
            switch (operator) {
                case "46000":
                case "46002":
                    operatorName = 2;
                    // Toast.makeText(this, "此卡属于(中国移动)",
                    // Toast.LENGTH_SHORT).show();
                    break;
                case "46001":
                    operatorName = 1;
                    // Toast.makeText(this, "此卡属于(中国联通)",
                    // Toast.LENGTH_SHORT).show();
                    break;
                case "46003":
                    operatorName = 3;
                    // Toast.makeText(this, "此卡属于(中国电信)",
                    // Toast.LENGTH_SHORT).show();
                    break;
                default:
                    operatorName = 0;
                    break;
            }
        }
        return operatorName;
    }

    //0:未知 1:Ethernet 2:wifi 3:蜂窝网络,2G 4:蜂窝网络,3G 5:蜂窝网络,4G
    public int networktype() {
        int networkType = 0;
        try {
            final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo mobNetInfoActivity = connectivityManager.getActiveNetworkInfo(); //没有网络的时候是null
            if (mobNetInfoActivity == null || !mobNetInfoActivity.isAvailable()) {
                networkType = 0;
            } else {   //有网络
                // NetworkInfo不为null开始判断是网络类型
                int netType = mobNetInfoActivity.getType();
                if (netType == ConnectivityManager.TYPE_WIFI) {
                    // wifi net处理
                    networkType = 2;
                    ipaddress = getwifiipaddress();
                    SharePreferencePersistance share = new SharePreferencePersistance();
                    boolean wifiSSID = share.putString(context, "WifiSSID", wifissid);
                    L.e("MaiInfo---","wifissid:"+wifissid);
                    String wifiSSIDeeeeee = share.getString(context, "WifiSSID", "NNNNNNNNNN");
                    L.e("MaiInfo---","wifissid:"+wifissid+",,put wifiSSID:"+wifiSSID+",wifiSSIDeeeeee:"+wifiSSIDeeeeee);
                } else if (netType == ConnectivityManager.TYPE_MOBILE) {
                    networkType = getNetworkClass(tm.getNetworkType());
                    ipaddress = getnetipaddress();
                }
                L.e("isAvailable",mobNetInfoActivity.isAvailable());
            }

        } catch (Exception e) {
            if (LogUtil.isShowError()) {
                e.printStackTrace();
            }
            return networkType = 0;
        }
        return networkType;
    }

    public static int getNetworkClass(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return 3;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return 4;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return 5;
            default:
                return 0;
        }
    }

    private String getwifiipaddress() {
        //获取wifi服务
        //判断wifi是否开启
        if (wifi.isWifiEnabled()) {
            
            WifiInfo wifiInfo = wifi.getConnectionInfo();
            wifissid = wifiInfo.getSSID();
            //            wifissid = "aaaa";

            if (wifissid.indexOf("\"") == 0) {
                wifissid = wifissid.substring(1, wifissid.length());      //去掉第一个 "
            }
            if (wifissid.lastIndexOf("\"") == (wifissid.length() - 1)) {
                wifissid = wifissid.substring(0, wifissid.length() - 1);  //去掉最后一个 "
            }

            int ipAddress = wifiInfo.getIpAddress();
            String ip = intToIp(ipAddress);
            return ip;
        }else {
    
        }
        return null;
    }

    private String getnetipaddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return "";
    }

    private String intToIp(int i) {

        return (i & 0xFF) + "." +
                   ((i >> 8) & 0xFF) + "." +
                   ((i >> 16) & 0xFF) + "." +
                   (i >> 24 & 0xFF);
    }

    private String getUser_agent() {
        WebView webview;
        webview = new WebView(context);
        webview.layout(0, 0, 0, 0);
        WebSettings settings = webview.getSettings();
        String ua = settings.getUserAgentString();
        return ua;
    }

    //    @TargetApi(Build.VERSION_CODES.M)
    //    private String getpostion() {
    //        String locationProvider = null;
    //        String postion = null;
    //
    //        if (locationManager == null) {
    //            //获取地理位置管理器
    //            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    //            //获取所有可用的位置提供器
    //            List<String> providers = locationManager.getProviders(true);
    //            if (providers.contains(LocationManager.GPS_PROVIDER)) {
    //                //如果是GPS
    //                locationProvider = LocationManager.GPS_PROVIDER;
    //            } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
    //                //如果是Network
    //                locationProvider = LocationManager.NETWORK_PROVIDER;
    //            } else {
    //
    //            }
    //            //获取Location
    //            if (ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
    //                    && ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
    //
    //
    //                Location location = locationManager.getLastKnownLocation(locationProvider);
    ////                while (location == null) {
    ////                    location = locationManager.getLastKnownLocation(locationProvider);
    ////                }
    //                if (location != null) {
    //                    //不为空,显示地理位置经纬度
    //                    postion = "维度：" + location.getLatitude() + ","
    //                                  + "经度：" + location.getLongitude();
    //                }else {
    //                    postion="位置未知";
    //                }
    //
    //
    //                //监视地理位置变化
    //                locationManager.requestLocationUpdates(locationProvider, 3000, 1, locationListener);
    //            }
    //        }
    //        return postion;
    //    }
    //
    //    /**
    //     * LocationListern监听器
    //     * 参数：地理位置提供器、监听位置变化的时间间隔、位置变化的距离间隔、LocationListener监听器
    //     */
    //
    //    LocationListener locationListener = new LocationListener() {
    //
    //        @Override
    //        public void onStatusChanged(String provider, int status, Bundle arg2) {
    //
    //        }
    //
    //        @Override
    //        public void onProviderEnabled(String provider) {
    //
    //        }
    //
    //        @Override
    //        public void onProviderDisabled(String provider) {
    //
    //        }
    //
    //        @Override
    //        public void onLocationChanged(Location location) {
    //
    //            if (location != null) {
    //                //如果位置发生变化,重新显示
    //                postion = "维度：" + location.getLatitude() + ","
    //                              + "经度：" + location.getLongitude();
    //            }else{
    //                postion="未知";
    //            }
    //
    //        }
    //    };

    private void openAndGetLocation() {


        boolean providerEnabled = false;

        try {
            mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            providerEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            if (LogUtil.isShowError()) {
                e.printStackTrace();
            }
            LogUtil.E("Please", "Add ACCESS_FINE_LOCATION Or ACCESS_COARSE_LOCATION Permission");
        }
        if (providerEnabled) {
            getLocation();
            L.e("location", "openAndGetLocation: " + "有权限");
            //            Toast.makeText(this, "定位模块正常", Toast.LENGTH_SHORT).show();

        } else {
            postion = latitude + "," + longitude;
        }

        //        Toast.makeText(this, "请开启定位权限", Toast.LENGTH_SHORT).show();
        //        // 跳转到GPS的设置页面
        //        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        //        startActivityForResult(intent, 0); // 此为设置完成后返回到获取界面

    }

    private void getLocation() {
        // android通过criteria选择合适的地理位置服务
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);// 高精度
        //        criteria.setAccuracy(Criteria. ACCURACY_COARSE);// 高精度
        criteria.setAltitudeRequired(false);// 设置不需要获取海拔方向数据
        criteria.setBearingRequired(false);// 设置不需要获取方位数据
        criteria.setCostAllowed(true);// 设置允许产生资费
        criteria.setPowerRequirement(Criteria.POWER_LOW);// 低功耗

        provider = mLocationManager.getBestProvider(criteria, true);// 获取GPS信息
        L.e("location", "provider: location " + provider);
        if (provider != null) {
            //        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //            // TODO: Consider calling
            //            return;
            //        }

            Location location = mLocationManager.getLastKnownLocation(provider);// 通过GPS获取位置
            updateUIToNewLocation(location);

            // 设置监听器，自动更新的最小时间为间隔N秒(这里的单位是微秒)或最小位移变化超过N米(这里的单位是米) 0.00001F

            mLocationManager.requestLocationUpdates(provider, 10 * 1000, 1, locationListener);
        } else {
            LogUtil.E("Please", "Add ACCESS_FINE_LOCATION Or ACCESS_COARSE_LOCATION Permission");
        }
    }


    private void updateUIToNewLocation(Location location) {

        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            postion = latitude + "," + longitude;

            // Location类的方法：
            // getAccuracy():精度（ACCESS_FINE_LOCATION／ACCESS_COARSE_LOCATION）
            // getAltitude():海拨
            // getBearing():方位，行动方向
            // getLatitude():纬度
            // getLongitude():经度
            // getProvider():位置提供者（GPS／NETWORK）
            // getSpeed():速度
            // getTime():时刻
        } else {
            postion = +latitude + "," + longitude;
        }


    }

    // 定义对位置变化的监听函数
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            if (location != null) {

                latitude = location.getLatitude();
                longitude = location.getLongitude();

                postion = latitude + "," + longitude;
                L.e("location", "onLocationChanged: " + "纬度：" + location.getLatitude() + "\n经度" + location.getLongitude());
            } else {
                postion = latitude + "," + longitude;
            }

        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

            L.e("location", "onStatusChanged: " + "privider:" + provider + "status:" + status + "extras:" + extras);
        }

        public void onProviderEnabled(String provider) {

            L.e("location", "onProviderEnabled: " + "privider:" + provider);

        }

        public void onProviderDisabled(String provider) {

            L.e("location", "onProviderDisabled: " + "privider:" + provider);

        }
    };


    public void destroy() {
        MaiReportManager.getInstance(context).unregisterNetworkStateReceiver();
    }
}
