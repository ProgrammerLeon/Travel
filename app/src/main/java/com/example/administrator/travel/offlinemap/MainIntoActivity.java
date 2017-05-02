package com.example.administrator.travel.offlinemap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.LocationSource;
import com.example.administrator.travel.CacheUtil;
import com.example.administrator.travel.NaviActivity;
import com.example.administrator.travel.R;
import com.example.administrator.travel.RestRouteShowActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainIntoActivity extends AppCompatActivity implements LocationSource {
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private AMapLocation myLastLocation;
    private ImageView mSpring;
    private ImageView mSummer;
    private ImageView mAutumn;
    private ImageView mWinter;
    private Button mButton;
    private TextView desctiption;
    private String des;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_into);
        initLocation();
        startLocation();
        String url_Summer = getIntent().getExtras().getString("Summer");
        String url_Autumn = getIntent().getExtras().getString("Autumn");
        String url_Spring = getIntent().getExtras().getString("Spring");
        String url_Winter = getIntent().getExtras().getString("Winter");
//        String url = "http://www.davidzhao.cn/pic/badaguan-win.png";
        String url = url_Winter;
        mButton = (Button) findViewById(R.id.share_life_btn);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(MainIntoActivity.this, NaviActivity.class);
                startActivity(mIntent);
                if (myLastLocation != null) {
                    navijudge();
                } else {
                    System.out.println("高德错误 请检查使用的电脑是否为梁焕祥的电脑 O(∩_∩)O哈哈~");
                }
            }
        });
        desctiption = (TextView) findViewById(R.id.description);
//        desctiption.getSettings().setJavaScriptEnabled(true);
//        desctiption.loadUrl("http://www.baidu.com");
//        setContentView(desctiption);
        ImageView mSummer = (ImageView) findViewById(R.id.spot_summer);
        ImageView mAutumn = (ImageView) findViewById(R.id.spot_autumn);
        ImageView mSpring = (ImageView) findViewById(R.id.spot_spring);
        ImageView mWinter = (ImageView) findViewById(R.id.spot_winter);
        loadImage(url_Summer, mSummer);
        loadImage(url_Autumn, mAutumn);
        loadImage(url_Spring, mSpring);
        loadImage(url_Winter, mWinter);


        String url_des = url.replace("-win.png", ".txt");
        DesFetch fetch = new DesFetch(url_des);
        fetch.start();
        des = null;
        while ((des = fetch.getDes()) == null) {
//            try
//            {
//                Thread.sleep(50);
//            }
//            catch (InterruptedException e)
//            {
//                e.printStackTrace();
//            }
        }
        desctiption.setText(des.split("xxx")[0]);
//        desctiption.setText("hehe");
    }

    public void loadImage(String url, ImageView view) {
        NetService netService = new NetService(url);
        netService.start();
        Bitmap bitmap;
        if ((bitmap = CacheUtil.getBitmapCache(url)) != null) {
            view.setImageBitmap(bitmap);
        } else {
            while ((bitmap = netService.getBitmap()) == null) {
//            try
//            {
//                Thread.sleep(50);
//            }
//            catch (InterruptedException e)
//            {
//                e.printStackTrace();
//            }
            }
            view.setImageBitmap(bitmap);
            CacheUtil cacheUtil = new CacheUtil();
            cacheUtil.setBitmapCache(url, bitmap);
        }
    }

    class DesFetch extends Thread {
        String url;
        String des = null;

        DesFetch(String url) {
            this.url = url;
        }

        public String getDes() {
            return des;
        }

        @Override
        public void run() {
            URL myFileURL;
            try {
                myFileURL = new URL(url);
                //获得连接
                HttpURLConnection conn = (HttpURLConnection) myFileURL.openConnection();
                //设置超时时间为6000毫秒，conn.setConnectionTiem(0);表示没有时间限制
                conn.setConnectTimeout(0);
                //连接设置获得数据流
                conn.setDoInput(true);
                //不使用缓存
                conn.setUseCaches(true);
                //这句可有可无，没有影响
                conn.connect();
                //得到数据流
                InputStream is = conn.getInputStream();
                //解析得到图片
                BufferedReader bufr = new BufferedReader(new InputStreamReader(is));
                StringBuffer stringBuffer = new StringBuffer();
                String s = null;
                while ((s = bufr.readLine()) != null) {
                    stringBuffer.append(s);
                }
                des = stringBuffer.toString();
                System.out.println(des);
                //关闭数据流
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void initLocation() {
        //初始化client
        mlocationClient = new AMapLocationClient(this.getApplicationContext());
        //设置定位参数
        mlocationClient.setLocationOption(getDefaultOption());
        // 设置定位监听
        mlocationClient.setLocationListener(locationListener);
    }

    /**
     * 开始定位
     *
     * @author hongming.wang
     * @modify han yuanfeng
     * @since 2.8.0
     */
    private void startLocation() {
        // 设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        // 启动定位
        mlocationClient.startLocation();
    }

    private void navijudge() {
        Intent intent = new Intent(MainIntoActivity.this, RestRouteShowActivity.class);
        Bundle bundle = new Bundle();
        bundle.putDouble("startLocation_latitude", myLastLocation.getLatitude());
        bundle.putDouble("startLocation_longitude", myLastLocation.getLongitude());
        bundle.putString("startAdName", myLastLocation.getAddress() + "附近");
        bundle.putDouble("endLocation_latitude", Double.parseDouble(des.split("xxx")[1]));
        bundle.putDouble("endLocation_longitude", Double.parseDouble(des.split("xxx")[2]));
        bundle.putString("endAdName", des.split("xxx")[3]);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * 默认的定位参数
     *
     * @author hongming.wang
     * @modify han yuanfeng
     * @since 2.8.0
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(true);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(10000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(true);//可选，设置是否使用传感器。默认是false
        // mOption.setWifiScan(false); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = getDefaultOption();
            //设置定位监听
            mlocationClient.setLocationListener(locationListener);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // mlocationOption
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            //            mlocationClient.startLocation();
        }
    }

    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation != null
                        && amapLocation.getErrorCode() == 0) {
                    myLastLocation = amapLocation;
                }
            } else {
                String errText = "定位失败,请检查网络或者GPS定位功能是否打开";
                Log.e("AmapErr", errText);
            }
        }
    };

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mlocationClient = new AMapLocationClient(this);
        mLocationOption = getDefaultOption();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        deactivate();

    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mlocationClient) {
            mlocationClient.onDestroy();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }
}

class NetService extends Thread {
    String url;
    Bitmap bitmap = null;


    public Bitmap getBitmap() {
        return bitmap;
    }

    NetService(String url) {
        this.url = url;
    }

    @Override
    public void run() {
        URL myFileURL;
        try {
            myFileURL = new URL(url);
            //获得连接
            HttpURLConnection conn = (HttpURLConnection) myFileURL.openConnection();
            //设置超时时间为6000毫秒，conn.setConnectionTiem(0);表示没有时间限制
            conn.setConnectTimeout(0);
            //连接设置获得数据流
            conn.setDoInput(true);
            //不使用缓存
            conn.setUseCaches(true);
            //这句可有可无，没有影响
            conn.connect();
            //得到数据流
            InputStream is = conn.getInputStream();
            //解析得到图片
            bitmap = BitmapFactory.decodeStream(is);
            //关闭数据流
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("网络错误", "通过HTTP协议获取服务器上的图片 请确认Tomcat已经开启并且手机连接了正确的WIFI");
        }
    }

    public static Bitmap getHttpBitmap(String url)//通过HTTP协议获取服务器上的图片 请确认Tomcat已经开启并且手机连接了正确的WIFI
    {
        URL myFileURL;
        Bitmap bitmap = null;
        try {
            myFileURL = new URL(url);
            //获得连接
            HttpURLConnection conn = (HttpURLConnection) myFileURL.openConnection();
            //设置超时时间为6000毫秒，conn.setConnectionTiem(0);表示没有时间限制
            conn.setConnectTimeout(6000);
            //连接设置获得数据流
            conn.setDoInput(true);
            //不使用缓存
            conn.setUseCaches(false);
            //这句可有可无，没有影响
            //conn.connect();
            //得到数据流
            InputStream is = conn.getInputStream();
            //解析得到图片
            bitmap = BitmapFactory.decodeStream(is);
            //关闭数据流
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("网络错误", "通过HTTP协议获取服务器上的图片 请确认Tomcat已经开启并且手机连接了正确的WIFI");
        }

        return bitmap;

    }

}
