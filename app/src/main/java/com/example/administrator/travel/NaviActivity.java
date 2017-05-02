package com.example.administrator.travel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.List;
/**
 * 高德api应用
 *
 * @author 高德工程师
 * @modify han yuanfeng
 */

/**
 * 定位图标箭头指向手机朝向
 */
public class NaviActivity extends Activity implements LocationSource,
        AMapLocationListener, AMap.OnMapClickListener, AMap.OnMapLongClickListener, GeocodeSearch.OnGeocodeSearchListener,
        OnClickListener, TextWatcher, Inputtips.InputtipsListener, PoiSearch.OnPoiSearchListener,
        AMap.InfoWindowAdapter, AMap.OnMarkerClickListener {
    private AMap aMap;
    private MapView mapView;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private AMapLocation myLastLocation;
    private LatLng markerLocation;
    private TextView mLocationErrText;
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    private boolean mFirstFix = false, isCalculateComplete = false;
    private Marker mLocMarker,mMapClickMarker,mInfowindowMarker;
    private SensorEventHelper mSensorHelper;
    private Circle mCircle;
    private ImageButton LocationButton, inButton, outButton;//map功能性自定义按钮
    private static int BUTTONFLAG = 0;//缩放按钮可见度标志,0为可见,1为不可见
    private ImageButton imageButton_navi;
    private TextView topInfoBtn;
    private GeocodeSearch geocoderSearch;
    private String addressName;
    private ListView mpointListview;
    private AutoCompleteTextView searchText;// 输入搜索关键字
    private LinearLayout search_header, buttonGroup, point_result;
    private TextView searchBtn;
    private String keyWord = "";// 要输入的poi搜索关键字
    private String editCity = "青岛";// 要输入的城市名字或者城市区号
    private PoiResult poiResult; // poi返回的结果
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;// POI搜索
    private ProgressDialog progDialog = null;// 搜索时进度条
    private PoiOverlay mpoiOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 不显示程序的标题栏
        setContentView(R.layout.naviactivity_main);
        mapView = (MapView) findViewById(R.id.map);
        System.out.println(mapView);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init();
        startLocation();
    }

    /**
     * 初始化
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }
        mSensorHelper = new SensorEventHelper(this);
        if (mSensorHelper != null) {
            mSensorHelper.registerSensorListener();
        }
        mLocationErrText = (TextView) findViewById(R.id.location_errInfo_text);
        mLocationErrText.setVisibility(View.GONE);

        LocationButton = (ImageButton) findViewById(R.id.location_button_main);//自定义定位按钮
        LocationButton.setOnClickListener(this);
        inButton = (ImageButton) findViewById(R.id.imageButtonIN_main);//自定义缩小按钮
        inButton.setOnClickListener(this);
        outButton = (ImageButton) findViewById(R.id.imageButtonOUT_main);//自定义放大按钮
        outButton.setOnClickListener(this);

        imageButton_navi = (ImageButton) findViewById(R.id.imageBtn_navi);//导航按钮
        imageButton_navi.setOnClickListener(this);

        searchText = (AutoCompleteTextView) findViewById(R.id.keyWord);
        searchText.addTextChangedListener(this);// 添加文本输入框监听事件
        search_header = (LinearLayout) findViewById(R.id.search_header);
        search_header.setOnClickListener(this);
        searchBtn = (TextView) findViewById(R.id.btn_search);
        searchBtn.setOnClickListener(this);

        buttonGroup = (LinearLayout) findViewById(R.id.buttonGroup);
        topInfoBtn = (Button) findViewById(R.id.topInfoBtn);//地址显示按钮

        mpointListview = (ListView) findViewById(R.id.point_list_view);
        point_result = (LinearLayout) findViewById(R.id.point_result);
        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        aMap.setOnMarkerClickListener(this);// 添加点击marker监听事件
        aMap.setInfoWindowAdapter(this);// 添加显示infowindow监听事件
        aMap.setLocationSource(this);// 设置定位监听
        aMap.setOnMapClickListener(this);// 对amap添加单击地图事件监听器
        aMap.setOnMapLongClickListener(this);// 对amap添加长按地图事件监听器
        aMap.getUiSettings().setCompassEnabled(true);//指南针是否显示
        aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.location_button_main://定位按钮
                aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_ROTATE);
                startLocation();
                if (myLastLocation != null) {
                    LatLng location = new LatLng(myLastLocation.getLatitude(), myLastLocation.getLongitude());
                    aMap.animateCamera(CameraUpdateFactory.changeLatLng(location), 500, null);
                    buttonGroup.setVisibility(View.VISIBLE);
                    LatLonPoint latLonPoint = new LatLonPoint(myLastLocation.getLatitude(), myLastLocation.getLongitude());
                    getAddress(latLonPoint);
                }
                if (mMapClickMarker != null) {
                    mMapClickMarker.remove();//清除上一个marker图标
                }
                break;
            case R.id.imageButtonIN_main://放大按钮
                changeCamera(CameraUpdateFactory.zoomIn(), null);
                buttonGroup.setVisibility(View.GONE);
                break;
            case R.id.imageButtonOUT_main://缩小按钮
                changeCamera(CameraUpdateFactory.zoomOut(), null);
                buttonGroup.setVisibility(View.GONE);
                break;
            case R.id.imageBtn_navi://路线规划
                Intent intent = new Intent(NaviActivity.this, RestRouteShowActivity.class);
                Bundle bundle = new Bundle();
                bundle.putDouble("startLocation_latitude", myLastLocation.getLatitude());
                bundle.putDouble("startLocation_longitude", myLastLocation.getLongitude());
                bundle.putString("startAdName", myLastLocation.getAddress() + "附近");
                if (markerLocation != null) {
                    bundle.putDouble("endLocation_latitude", markerLocation.latitude);
                    bundle.putDouble("endLocation_longitude", markerLocation.longitude);
                    LatLonPoint endLatLonPoint = new LatLonPoint(markerLocation.latitude, markerLocation.longitude);
                    getAddress(endLatLonPoint);
                    bundle.putString("endAdName", addressName);
                } else {
                    bundle.putDouble("endLocation_latitude", myLastLocation.getLatitude());
                    bundle.putDouble("endLocation_longitude", myLastLocation.getLongitude());
                    bundle.putString("endAdName", myLastLocation.getAddress() + "附近");
                }
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.btn_search:
                doSearchQuery();
                point_result.setVisibility(View.GONE);
            case R.id.search_header:

                break;
//            case R.id.keyWord:
//                point_result.setVisibility(View.VISIBLE);
            default:
                break;
        }

    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (mSensorHelper != null) {
            mSensorHelper.registerSensorListener();
        }
        mlocationClient = new AMapLocationClient(this);
        mLocationOption = getDefaultOption();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mSensorHelper != null) {
            mSensorHelper.unRegisterSensorListener();
            mSensorHelper.setCurrentMarker(null);
            mSensorHelper = null;
        }
        mapView.onPause();
        deactivate();
        mFirstFix = false;
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
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
        if (mLocMarker != null) {
            mLocMarker.destroy();
        }
        mapView.onDestroy();
        if (null != mlocationClient) {
            mlocationClient.onDestroy();
        }
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
                mLocationErrText.setVisibility(View.GONE);
                myLastLocation = amapLocation;
                LatLng location = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                if (!mFirstFix) {
                    mFirstFix = true;
                    addCircle(location, amapLocation.getAccuracy());//添加定位精度圆
                    addMarker(location, amapLocation);//添加定位图标
                    mSensorHelper.setCurrentMarker(mLocMarker);//定位图标旋转
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18));
                } else {
                    mCircle.setCenter(location);
                    mCircle.setRadius(amapLocation.getAccuracy());
                    mLocMarker.setPosition(location);
//                    if (myLastLocation != amapLocation) {
//                        aMap.moveCamera(CameraUpdateFactory.changeLatLng(location));
//                    }
                }
            } else {
                String errText = "定位失败,请检查网络或者GPS定位功能是否打开";
                Log.e("AmapErr", errText);
                mLocationErrText.setVisibility(View.VISIBLE);
                mLocationErrText.setText(errText);
            }
        }
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = getDefaultOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
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
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    private void addCircle(LatLng latlng, double radius) {
        CircleOptions options = new CircleOptions();
        options.strokeWidth(1f);
        options.fillColor(FILL_COLOR);
        options.strokeColor(STROKE_COLOR);
        options.center(latlng);
        options.radius(radius);
        mCircle = aMap.addCircle(options);
    }

    private void addMarker(LatLng latlng, AMapLocation amapLocation) {
        if (mLocMarker != null) {
            return;
        }
        MarkerOptions options = new MarkerOptions();
        options.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(),
                R.drawable.map_location_marker)));
        options.anchor(0.5f, 0.5f);
        options.position(latlng);
        mLocMarker = aMap.addMarker(options);
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

    /**
     * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
     */
    private void changeCamera(CameraUpdate update, AMap.CancelableCallback callback) {
        aMap.animateCamera(update, 250, null);
    }

    /**
     * 对单击地图事件回调
     */
    @Override
    public void onMapClick(LatLng point) {
        if (mMapClickMarker != null) {
            mMapClickMarker.remove();//清除上一个marker图标
        }        //设置缩放按钮可见度
        buttonGroup.setVisibility(View.GONE);
        if (BUTTONFLAG == 0) {
            inButton.setVisibility(View.GONE);
            outButton.setVisibility(View.GONE);
            BUTTONFLAG = 1;
        } else {
            inButton.setVisibility(View.VISIBLE);
            outButton.setVisibility(View.VISIBLE);
            BUTTONFLAG = 0;
        }
        if(mInfowindowMarker!=null){
            mInfowindowMarker.hideInfoWindow();
        }
    }

    /**
     * 对长按地图事件回调
     */
    @Override
    public void onMapLongClick(LatLng point) {
        addMarkersToMap(point);//在地图上添加marker
        if (BUTTONFLAG == 1) {
            inButton.setVisibility(View.VISIBLE);
            outButton.setVisibility(View.VISIBLE);
        }
        buttonGroup.setVisibility(View.VISIBLE);
    }

    /**
     * 在地图上添加marker
     */
    private void addMarkersToMap(LatLng point) {
        markerLocation = point;
        if (mMapClickMarker != null) {
            mMapClickMarker.remove();//清除上一个marker图标
        }
        dropInto(point);//显示定位图标
        LatLonPoint latLonPoint = new LatLonPoint(point.latitude, point.longitude);
        getAddress(latLonPoint);
    }

    /**
     * 动画效果从屏幕上方落下图标mark
     * marker 必须有设置图标，否则无效果
     */
    private void dropInto(LatLng point) {
        aMap.animateCamera(CameraUpdateFactory.changeLatLng(point), 500, null);
        mMapClickMarker = aMap.addMarker(new MarkerOptions().position(
                point).icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(),
                R.drawable.b_poi_hl_old))));//图标大
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final LatLng markerLatlng = mMapClickMarker.getPosition();
        Projection proj = aMap.getProjection();
        Point markerPoint = proj.toScreenLocation(markerLatlng);
        Point startPoint = new Point(markerPoint.x, 0);// 从marker的屏幕上方下落
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 300;// 动画总时长

        final Interpolator interpolator = new AccelerateInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * markerLatlng.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * markerLatlng.latitude + (1 - t)
                        * startLatLng.latitude;
                mMapClickMarker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }


    /**
     * 地理编码查询回调
     */
    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
    }

    /**
     * 响应逆地理编码
     */
    public void getAddress(final LatLonPoint latLonPoint) {
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
                GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        geocoderSearch.getFromLocationAsyn(query);// 设置异步逆地理编码请求
    }

    /**
     * 逆地理编码回调
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {
                addressName = result.getRegeocodeAddress().getFormatAddress()
                        + "附近";
                isCalculateComplete = true;
                topInfoBtn.setText(addressName);
            } else {
                Toast.makeText(NaviActivity.this, R.string.no_result, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(NaviActivity.this, R.string.no_result, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        point_result.setVisibility(View.VISIBLE);
        String newText = s.toString().trim();
        if (!AMapUtil.IsEmptyOrNullString(newText)) {
            InputtipsQuery inputquery = new InputtipsQuery(newText, "青岛");
            Inputtips inputTips = new Inputtips(NaviActivity.this, inputquery);
            inputTips.setInputtipsListener(this);
            inputTips.requestInputtipsAsyn();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onGetInputtips(List<Tip> tipList, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {// 正确返回
            List<String> listString = new ArrayList<String>();
            for (int i = 0; i < tipList.size(); i++) {
                listString.add(tipList.get(i).getName());
            }
            TipListAdapter aAdapter = new TipListAdapter(
                    getApplicationContext(), listString, searchText);
            mpointListview.setAdapter(aAdapter);
            aAdapter.notifyDataSetChanged();
        } else {
            ToastUtil.showerror(this, rCode);
        }
    }

    @Override
    public View getInfoWindow(final Marker marker) {
        View view = getLayoutInflater().inflate(R.layout.poikeywordsearch_uri,
                null);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(marker.getTitle());

        TextView snippet = (TextView) view.findViewById(R.id.snippet);
        snippet.setText(marker.getSnippet());
        ImageButton button = (ImageButton) view
                .findViewById(R.id.start_amap_app);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NaviActivity.this, RestRouteShowActivity.class);
                Bundle bundle = new Bundle();
                bundle.putDouble("startLocation_latitude", myLastLocation.getLatitude());
                bundle.putDouble("startLocation_longitude", myLastLocation.getLongitude());
                bundle.putString("startAdName", myLastLocation.getAddress() + "附近");

                bundle.putDouble("endLocation_latitude", marker.getPosition().latitude);
                bundle.putDouble("endLocation_longitude", marker.getPosition().longitude);
                LatLonPoint endLatLonPoint = new LatLonPoint(marker.getPosition().latitude, marker.getPosition().longitude);
                bundle.putString("endAdName",marker.getTitle());
                intent.putExtras(bundle);
                    startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        dissmissProgressDialog();// 隐藏对话框
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
                    if (mpoiOverlay != null) {
                        mpoiOverlay.removeFromMap();// 清理之前的图标
                    }
                    if (poiItems != null && poiItems.size() > 0) {
                        mpoiOverlay = new PoiOverlay(aMap, poiItems);
                        mpoiOverlay.removeFromMap();
                        mpoiOverlay.addToMap();
                        mpoiOverlay.zoomToSpan();
                    } //else if (suggestionCities != null
//                            && suggestionCities.size() > 0) {
//                        showSuggestCity(suggestionCities);
//                    }
                    else {
                        ToastUtil.show(NaviActivity.this,
                                R.string.no_result);
                    }
                }
            } else {
                ToastUtil.show(NaviActivity.this,
                        R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this, rCode);
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery() {
        showProgressDialog();// 显示进度框
        currentPage = 0;
        keyWord = AMapUtil.checkEditText(searchText);
        query = new PoiSearch.Query(keyWord, "", editCity);// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页

        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(false);
        progDialog.setMessage("正在搜索:\n" + keyWord);
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        mInfowindowMarker=marker;
        return false;
    }
}