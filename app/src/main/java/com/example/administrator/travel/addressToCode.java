package com.example.administrator.travel;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;

/**
 * Created by han yuanfeng on 2017/2/8.
 */

public class addressToCode extends Service implements GeocodeSearch.OnGeocodeSearchListener {
    private GeocodeSearch mGeocoderSearch;
    int mStartMode;       // indicates how to behave if the service is killed
    IBinder mBinder;      // interface for clients that bind
    boolean mAllowRebind; // indicates whether onRebind should be used
    private String mAddressName;

    @Override
    public void onCreate() {
        super.onCreate();
        mGeocoderSearch = new GeocodeSearch(this);
        mGeocoderSearch.setOnGeocodeSearchListener(this);
    }

    /**
     * 响应地理编码
     */
    public void getLatlon(final String name) {
        GeocodeQuery query = new GeocodeQuery(name, "青岛");// 第一个参数表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode，
        mGeocoderSearch.getFromLocationNameAsyn(query);// 设置同步地理编码请求
    }

    /**
     * 地理编码查询回调
     */
    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getGeocodeAddressList() != null
                    && result.getGeocodeAddressList().size() > 0) {
                GeocodeAddress address = result.getGeocodeAddressList().get(0);
                mAddressName = "经纬度值:" + address.getLatLonPoint() + "\n位置描述:"
                        + address.getFormatAddress();
                if (mAddressName != null) {
                    Intent intent = new Intent();
                    intent.putExtra("addressName",address.getFormatAddress());
                    intent.putExtra("Latitude", address.getLatLonPoint().getLatitude());
                    intent.putExtra("Longitude", address.getLatLonPoint().getLongitude());
                    //发送广播通知所有注册该接口的监听器
                    ListenerManager.getInstance().sendBroadCast(intent);
                }
                ToastUtil.show(addressToCode.this, mAddressName);
            } else {
                ToastUtil.show(addressToCode.this, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this, rCode);
        }
    }

    /**
     * 逆地理编码回调
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle locationBundle = new Bundle();
        locationBundle = intent.getExtras();
        String address = locationBundle.getString("address");
        getLatlon(address);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
    }

    @Override
    public void onRebind(Intent intent) {
    }

    @Override
    public void onDestroy() {
    }
}
