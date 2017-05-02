package com.example.administrator.travel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStaticInfo;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.autonavi.tbt.NaviStaticInfo;
import com.autonavi.tbt.TrafficFacilityInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * modified by han yuanfeng
 */
public class RestRouteShowActivity extends Activity implements AMapNaviListener
        , OnClickListener, RouteSearch.OnRouteSearchListener, mListener {
    private StrategyBean mStrategyBean;
    private static final float ROUTE_UNSELECTED_TRANSPARENCY = 0.3F;
    private static final float ROUTE_SELECTED_TRANSPARENCY = 1F;

    /**
     * 导航对象(单例)
     */
    private AMapNavi mAMapNavi;

    private MapView mapView;
    private AMap aMap;
    private List<NaviLatLng> startList = new ArrayList<NaviLatLng>();
    /**
     * 途径点坐标集合
     */
    private List<NaviLatLng> wayList = new ArrayList<NaviLatLng>();
    /**
     * 终点坐标集合［建议就一个终点］
     */
    private List<NaviLatLng> endList = new ArrayList<NaviLatLng>();
    /**
     * 保存当前算好的路线
     */
    private SparseArray<RouteOverLay> routeOverlays = new SparseArray<RouteOverLay>();
    /*
     * strategyFlag转换出来的值都对应PathPlanningStrategy常量，用户也可以直接传入PathPlanningStrategy常量进行算路。
     * 如:mAMapNavi.calculateDriveRoute(mStartList, mEndList, mWayPointList,PathPlanningStrategy.DRIVING_DEFAULT);
     */
    int strategyFlag = 0;
    private NaviLatLng mStartPoint = null;//起点
    private NaviLatLng mEndPoint = null;//终点
    private LinearLayout mBusResultLayout, mpreference;
    private RelativeLayout ctrl_UI;
    private ImageView mBus;
    private ImageView mDrive;
    private ImageView mWalk;
    private ListView mBusResultList;
    private ProgressDialog progDialog = null;// 搜索时进度条
    private final int ROUTE_TYPE_BUS = 1;
    private String mCurrentCityName = "青岛";
    private BusRouteResult mBusRouteResult;
    private Context mContext;
    private RouteSearch mRouteSearch;
    private int BTNFLAG = 0, MODEFLAG = 0;//BTNFLAG判断点击的哪个按钮，MODEFLAG判断是步行导航还是驾车导航
    private Button mStartAdBtn, mEndAdBtn;
    private ImageButton locationBtn, inBtn, outBtn;//map功能性自定义按钮
    private static int[] array = new int[1];
    private Button mStartNaviButton;//启动导航按钮
    private LinearLayout mRouteLineLayoutOne, mRouteLinelayoutTwo, mRouteLineLayoutThree;
    private View mRouteViewOne, mRouteViewTwo, mRouteViewThree;//规划完成的线路
    private TextView mRouteTextStrategyOne, mRouteTextStrategyTwo, mRouteTextStrategyThree;//规划路径信息显示选择栏
    private TextView mRouteTextTimeOne, mRouteTextTimeTwo, mRouteTextTimeThree;
    private TextView mRouteTextDistanceOne, mRouteTextDistanceTwo, mRouteTextDistanceThree;
    private TextView mCalculateRouteOverView, mBackBtn;
    private ImageView mImageTraffic, mImageStrategy;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_calculate_route);
        mContext = this.getApplicationContext();
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(bundle);// 此方法必须重写

        Intent locationIntent = getIntent();
        Bundle locationBundle = new Bundle();
        locationBundle = locationIntent.getExtras();
        mStartPoint = new NaviLatLng(locationBundle.getDouble("startLocation_latitude"), locationBundle.getDouble("startLocation_longitude"));
        mEndPoint = new NaviLatLng(locationBundle.getDouble("endLocation_latitude"), locationBundle.getDouble("endLocation_longitude"));

        init();
        initView();

        mStartAdBtn.setText("从：" + locationBundle.getString("startAdName"));
        mEndAdBtn.setText("到：" + locationBundle.getString("endAdName"));

        ListenerManager.getInstance().registerListtener(this);//注册自定义广播接收器
        /*
         *以下为初始化事件
         */
        initNavi();
        mDrive.setImageResource(R.drawable.route_drive_select);
        mBus.setImageResource(R.drawable.route_bus_normal);
        mWalk.setImageResource(R.drawable.route_walk_normal);
        mapView.setVisibility(View.VISIBLE);
        mBusResultLayout.setVisibility(View.GONE);
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.getUiSettings().setZoomControlsEnabled(false);
        }
        array[0] = 1;
        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);

        mAMapNavi = AMapNavi.getInstance(getApplicationContext());
        mAMapNavi.addAMapNaviListener(this);

        aMap.setTrafficEnabled(true);
    }

    /**
     * 导航初始化
     */
    private void initNavi() {
        mStrategyBean = new StrategyBean(false, false, false, false);
        startList.add(mStartPoint);
        endList.add(mEndPoint);
        calculateDriveRoute();
        showProgressDialog();
    }

    private void initView() {
        mDrive = (ImageView) findViewById(R.id.route_drive);
        mBus = (ImageView) findViewById(R.id.route_bus);
        mWalk = (ImageView) findViewById(R.id.route_walk);

        mBusResultLayout = (LinearLayout) findViewById(R.id.bus_result);
        mBusResultList = (ListView) findViewById(R.id.bus_result_list);

        mStartAdBtn = (Button) findViewById(R.id.currentLocation);
        mStartAdBtn.setOnClickListener(this);
        mEndAdBtn = (Button) findViewById(R.id.markerLocation);
        mEndAdBtn.setOnClickListener(this);

        ctrl_UI = (RelativeLayout) findViewById(R.id.ctrl_UI);
        locationBtn = (ImageButton) findViewById(R.id.location_button);
        locationBtn.setOnClickListener(this);
        inBtn = (ImageButton) findViewById(R.id.imageButtonIN);
        inBtn.setOnClickListener(this);
        outBtn = (ImageButton) findViewById(R.id.imageButtonOUT);
        outBtn.setOnClickListener(this);
        mImageTraffic = (ImageButton) findViewById(R.id.trafficBtn);
        mImageTraffic.setOnClickListener(this);

        mStartNaviButton = (Button) findViewById(R.id.calculate_route_start_navi);
        mStartNaviButton.setOnClickListener(this);

        mStartNaviButton = (Button) findViewById(R.id.calculate_route_start_navi);
        mStartNaviButton.setOnClickListener(this);
        mImageStrategy = (ImageButton) findViewById(R.id.prefBtn);
        mImageStrategy.setOnClickListener(this);
        mBackBtn = (TextView) findViewById(R.id.backBtn);
        mBackBtn.setOnClickListener(this);

        mCalculateRouteOverView = (TextView) findViewById(R.id.calculate_route_navi_overview);

        mRouteLineLayoutOne = (LinearLayout) findViewById(R.id.route_line_one);
        mRouteLineLayoutOne.setOnClickListener(this);
        mRouteLinelayoutTwo = (LinearLayout) findViewById(R.id.route_line_two);
        mRouteLinelayoutTwo.setOnClickListener(this);
        mRouteLineLayoutThree = (LinearLayout) findViewById(R.id.route_line_three);
        mRouteLineLayoutThree.setOnClickListener(this);

        mRouteViewOne = (View) findViewById(R.id.route_line_one_view);
        mRouteViewTwo = (View) findViewById(R.id.route_line_two_view);
        mRouteViewThree = (View) findViewById(R.id.route_line_three_view);

        mRouteTextStrategyOne = (TextView) findViewById(R.id.route_line_one_strategy);
        mRouteTextStrategyTwo = (TextView) findViewById(R.id.route_line_two_strategy);
        mRouteTextStrategyThree = (TextView) findViewById(R.id.route_line_three_strategy);

        mRouteTextTimeOne = (TextView) findViewById(R.id.route_line_one_time);
        mRouteTextTimeTwo = (TextView) findViewById(R.id.route_line_two_time);
        mRouteTextTimeThree = (TextView) findViewById(R.id.route_line_three_time);

        mRouteTextDistanceOne = (TextView) findViewById(R.id.route_line_one_distance);
        mRouteTextDistanceTwo = (TextView) findViewById(R.id.route_line_two_distance);
        mRouteTextDistanceThree = (TextView) findViewById(R.id.route_line_three_distance);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        startList.clear();
        wayList.clear();
        endList.clear();
        routeOverlays.clear();
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
        mapView.onDestroy();
        /**
         * 当前页面只是展示地图，activity销毁后不需要再回调导航的状态
         */
        mAMapNavi.removeAMapNaviListener(this);
        //注意：不要调用这个destory方法，因为在当前页面进行算路，算路成功的数据全部存在此对象中。到另外一个activity中只需要开始导航即可。
        //如果用户是回退退出当前activity，可以调用下面的destory方法。
        //mAMapNavi.destroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.location_button://定位按钮
                break;
            case R.id.imageButtonIN://地图放大
                changeCamera(CameraUpdateFactory.zoomIn(), null);
                break;
            case R.id.imageButtonOUT://地图缩小
                changeCamera(CameraUpdateFactory.zoomOut(), null);
                break;
            case R.id.currentLocation://我的位置
                Intent mGoSearchIntentTop = new Intent(RestRouteShowActivity.this, GeocoderActivity.class);
                BTNFLAG = 1;
                startActivity(mGoSearchIntentTop);

                break;
            case R.id.markerLocation://标记位置
                Intent mGoSearchIntentdown = new Intent(RestRouteShowActivity.this, GeocoderActivity.class);
                BTNFLAG = 2;
                startActivity(mGoSearchIntentdown);
                break;
            case R.id.calculate_route_start_navi:
                startNavi();
                this.finish();
                break;
            case R.id.route_line_one://第一条路线
                focuseRouteLine(true, false, false);
                break;
            case R.id.route_line_two:
                focuseRouteLine(false, true, false);
                break;
            case R.id.route_line_three:
                focuseRouteLine(false, false, true);
                break;
            case R.id.prefBtn:
                strategyChoose();
                break;
            case R.id.trafficBtn:
                setTraffic();
                break;
            case R.id.backBtn:
                Intent mIntent = new Intent(RestRouteShowActivity.this,NaviActivity.class);
                startActivity(mIntent);
                finish();
                break;
            default:
                break;
        }
    }


    @Override
    public void onInitNaviSuccess() {
    }

    /**
     * 驾车路径规划计算
     */
    private void calculateDriveRoute() {
        try {
            strategyFlag = mAMapNavi.strategyConvert(mStrategyBean.isCongestion(), mStrategyBean.isCost(), mStrategyBean.isAvoidhightspeed(), mStrategyBean.isHightspeed(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAMapNavi.calculateDriveRoute(startList, endList, wayList, strategyFlag);
    }

    /**
     * 多路径算路成功回调
     *
     * @param ints 路线id数组
     */
    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {
        cleanRouteOverlay();
        HashMap<Integer, AMapNaviPath> paths = mAMapNavi.getNaviPaths();
        for (int i = 0; i < ints.length; i++) {
            AMapNaviPath path = paths.get(ints[i]);
            if (path != null) {
                drawRoutes(ints[i], path);
            }
        }
        setRouteLineTag(paths, ints);
    }

    /**
     * 接收驾车偏好设置项
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Utils.ACTIVITY_RESULT_CODE == resultCode) {
            boolean congestion = data.getBooleanExtra(Utils.INTENT_NAME_AVOID_CONGESTION, false);
            mStrategyBean.setCongestion(congestion);
            boolean cost = data.getBooleanExtra(Utils.INTENT_NAME_AVOID_COST, false);
            mStrategyBean.setCost(cost);
            boolean avoidhightspeed = data.getBooleanExtra(Utils.INTENT_NAME_AVOID_HIGHSPEED, false);
            mStrategyBean.setAvoidhightspeed(avoidhightspeed);
            boolean hightspeed = data.getBooleanExtra(Utils.INTENT_NAME_PRIORITY_HIGHSPEED, false);
            mStrategyBean.setHightspeed(hightspeed);
            calculateDriveRoute();
        }
    }

    /**
     * 绘制路径规划结果
     *
     * @param routeId 路径规划线路ID
     * @param path    AMapNaviPath
     */
    private void drawRoutes(int routeId, AMapNaviPath path) {
        aMap.moveCamera(CameraUpdateFactory.changeTilt(0));
        RouteOverLay routeOverLay = new RouteOverLay(aMap, path, this);
        routeOverLay.setTrafficLine(true);
        routeOverLay.addToMap();
        routeOverlays.put(routeId, routeOverLay);
        dissmissProgressDialog();
    }


    /**
     * 开始导航
     */
    private void startNavi() {
        Intent gpsintent = new Intent(getApplicationContext(), RouteNaviActivity.class);
        gpsintent.putExtra("gps", true); // gps 为true为真实导航，为false为模拟导航
        startActivity(gpsintent);
    }

    /**
     * 路线tag选中设置
     *
     * @param lineOne
     * @param lineTwo
     * @param lineThree
     */
    private void focuseRouteLine(boolean lineOne, boolean lineTwo, boolean lineThree) {
        Log.d("LG", "lineOne:" + lineOne + " lineTwo:" + lineTwo + " lineThree:" + lineThree);
        setLinelayoutOne(lineOne);
        setLinelayoutTwo(lineTwo);
        setLinelayoutThree(lineThree);
    }

    /**
     * 地图实时交通开关
     */
    private void setTraffic() {
        if (aMap.isTrafficEnabled()) {
            mImageTraffic.setImageResource(R.drawable.drive_map_icon_traffic_day);
            aMap.setTrafficEnabled(false);
        } else {
            mImageTraffic.setImageResource(R.drawable.drive_map_icon_traffic_day_checked);
            aMap.setTrafficEnabled(true);
        }
    }

    private void cleanRouteOverlay() {
        for (int i = 0; i < routeOverlays.size(); i++) {
            int key = routeOverlays.keyAt(i);
            RouteOverLay overlay = routeOverlays.get(key);
            overlay.removeFromMap();
            overlay.destroy();
        }
        routeOverlays.clear();
    }

    /**
     * 跳转到驾车偏好设置页面
     */
    private void strategyChoose() {
        Intent intent = new Intent(this, StrategyChooseActivity.class);
        intent.putExtra(Utils.INTENT_NAME_AVOID_CONGESTION, mStrategyBean.isCongestion());
        intent.putExtra(Utils.INTENT_NAME_AVOID_COST, mStrategyBean.isCost());
        intent.putExtra(Utils.INTENT_NAME_AVOID_HIGHSPEED, mStrategyBean.isAvoidhightspeed());
        intent.putExtra(Utils.INTENT_NAME_PRIORITY_HIGHSPEED, mStrategyBean.isHightspeed());
        startActivityForResult(intent, Utils.START_ACTIVITY_REQUEST_CODE);
    }

    /**
     * @param paths 多路线回调路线
     * @param ints  多路线回调路线ID
     */
    private void setRouteLineTag(HashMap<Integer, AMapNaviPath> paths, int[] ints) {
        if (ints.length < 1) {
            visiableRouteLine(false, false, false);
            return;
        }
        int indexOne = 0;
        String stragegyTagOne = Utils.getStrategyDes(paths, ints, indexOne, mStrategyBean);
        if (MODEFLAG == 1) {//步行模式，调整时间
            setLinelayoutOneContentOverride(ints[indexOne], stragegyTagOne);
            MODEFLAG = 0;
        } else {
            setLinelayoutOneContent(ints[indexOne], stragegyTagOne);
        }
        if (ints.length == 1) {
            visiableRouteLine(true, false, false);
            focuseRouteLine(true, false, false);
            return;
        }
        int indexTwo = 1;
        String stragegyTagTwo = Utils.getStrategyDes(paths, ints, indexTwo, mStrategyBean);
        setLinelayoutTwoContent(ints[indexTwo], stragegyTagTwo);
        if (ints.length == 2) {
            visiableRouteLine(true, true, false);
            focuseRouteLine(true, false, false);
            return;
        }
        int indexThree = 2;
        String stragegyTagThree = Utils.getStrategyDes(paths, ints, indexThree, mStrategyBean);
        setLinelayoutThreeContent(ints[indexThree], stragegyTagThree);
        if (ints.length >= 3) {
            visiableRouteLine(true, true, true);
            focuseRouteLine(true, false, false);
        }
    }

    private void visiableRouteLine(boolean lineOne, boolean lineTwo, boolean lineThree) {
        setLinelayoutOneVisiable(lineOne);
        setLinelayoutTwoVisiable(lineTwo);
        setLinelayoutThreeVisiable(lineThree);
    }

    private void setLinelayoutOneVisiable(boolean visiable) {
        if (visiable) {
            mRouteLineLayoutOne.setVisibility(View.VISIBLE);
        } else {
            mRouteLineLayoutOne.setVisibility(View.GONE);
        }
    }

    private void setLinelayoutTwoVisiable(boolean visiable) {
        if (visiable) {
            mRouteLinelayoutTwo.setVisibility(View.VISIBLE);
        } else {
            mRouteLinelayoutTwo.setVisibility(View.GONE);
        }
    }

    private void setLinelayoutThreeVisiable(boolean visiable) {
        if (visiable) {
            mRouteLineLayoutThree.setVisibility(View.VISIBLE);
        } else {
            mRouteLineLayoutThree.setVisibility(View.GONE);
        }
    }

    /**
     * 设置第一条线路Tab 内容
     *
     * @param routeID  路线ID
     * @param strategy 策略标签
     */
    private void setLinelayoutOneContent(int routeID, String strategy) {
        mRouteLineLayoutOne.setTag(routeID);
        RouteOverLay overlay = routeOverlays.get(routeID);
        overlay.zoomToSpan();
        AMapNaviPath path = overlay.getAMapNaviPath();
        mRouteTextStrategyOne.setText(strategy);
        String timeDes = Utils.getFriendlyTime(path.getAllTime());
        mRouteTextTimeOne.setText(timeDes);
        String disDes = Utils.getFriendlyDistance(path.getAllLength());
        mRouteTextDistanceOne.setText(disDes);
    }

    //重写，计算步行导航时间
    private void setLinelayoutOneContentOverride(int routeID, String strategy) {
        mRouteLineLayoutOne.setTag(routeID);
        RouteOverLay overlay = routeOverlays.get(routeID);
        overlay.zoomToSpan();
        AMapNaviPath path = overlay.getAMapNaviPath();
        mRouteTextStrategyOne.setText(strategy);
        String disDes = Utils.getFriendlyDistance(path.getAllLength());
        mRouteTextDistanceOne.setText(disDes);
        String timeDes = Utils.getFriendlyTime((path.getAllLength() / 2));
        mRouteTextTimeOne.setText(timeDes);

    }

    /**
     * 设置第二条路线Tab 内容
     *
     * @param routeID  路线ID
     * @param strategy 策略标签
     */
    private void setLinelayoutTwoContent(int routeID, String strategy) {
        mRouteLinelayoutTwo.setTag(routeID);
        RouteOverLay overlay = routeOverlays.get(routeID);
        AMapNaviPath path = overlay.getAMapNaviPath();
        mRouteTextStrategyTwo.setText(strategy);
        String timeDes = Utils.getFriendlyTime(path.getAllTime());
        mRouteTextTimeTwo.setText(timeDes);
        String disDes = Utils.getFriendlyDistance(path.getAllLength());
        mRouteTextDistanceTwo.setText(disDes);
    }

    /**
     * 设置第三条路线Tab 内容
     *
     * @param routeID  路线ID
     * @param strategy 策略标签
     */
    private void setLinelayoutThreeContent(int routeID, String strategy) {
        mRouteLineLayoutThree.setTag(routeID);
        RouteOverLay overlay = routeOverlays.get(routeID);
        AMapNaviPath path = overlay.getAMapNaviPath();
        mRouteTextStrategyThree.setText(strategy);
        String timeDes = Utils.getFriendlyTime(path.getAllTime());
        mRouteTextTimeThree.setText(timeDes);
        String disDes = Utils.getFriendlyDistance(path.getAllLength());
        mRouteTextDistanceThree.setText(disDes);
    }

    /**
     * 第一条路线是否focus
     *
     * @param focus focus为true 突出颜色显示，标示为选中状态，为false则标示非选中状态
     */
    private void setLinelayoutOne(boolean focus) {
        if (mRouteLineLayoutOne.getVisibility() != View.VISIBLE) {
            return;
        }
        try {
            int routeID = (int) mRouteLineLayoutOne.getTag();
            RouteOverLay overlay = routeOverlays.get(routeID);
            if (focus) {
                mCalculateRouteOverView.setText(Utils.getRouteOverView(overlay.getAMapNaviPath()));
                mAMapNavi.selectRouteId(routeID);
                overlay.setTransparency(ROUTE_SELECTED_TRANSPARENCY);
                mRouteViewOne.setVisibility(View.VISIBLE);
                mRouteTextStrategyOne.setTextColor(getResources().getColor(R.color.colorBlue));
                mRouteTextTimeOne.setTextColor(getResources().getColor(R.color.colorBlue));
                mRouteTextDistanceOne.setTextColor(getResources().getColor(R.color.colorBlue));
            } else {
                overlay.setTransparency(ROUTE_UNSELECTED_TRANSPARENCY);
                mRouteViewOne.setVisibility(View.INVISIBLE);
                mRouteTextStrategyOne.setTextColor(getResources().getColor(R.color.colorDark));
                mRouteTextTimeOne.setTextColor(getResources().getColor(R.color.colorBlack));
                mRouteTextDistanceOne.setTextColor(getResources().getColor(R.color.colorDark));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 第二条路线是否focus
     *
     * @param focus focus为true 突出颜色显示，标示为选中状态，为false则标示非选中状态
     */
    private void setLinelayoutTwo(boolean focus) {
        if (mRouteLinelayoutTwo.getVisibility() != View.VISIBLE) {
            return;
        }
        try {
            int routeID = (int) mRouteLinelayoutTwo.getTag();
            RouteOverLay overlay = routeOverlays.get(routeID);
            if (focus) {
                mCalculateRouteOverView.setText(Utils.getRouteOverView(overlay.getAMapNaviPath()));
                mAMapNavi.selectRouteId(routeID);
                overlay.setTransparency(ROUTE_SELECTED_TRANSPARENCY);
                mRouteViewTwo.setVisibility(View.VISIBLE);
                mRouteTextStrategyTwo.setTextColor(getResources().getColor(R.color.colorBlue));
                mRouteTextTimeTwo.setTextColor(getResources().getColor(R.color.colorBlue));
                mRouteTextDistanceTwo.setTextColor(getResources().getColor(R.color.colorBlue));
            } else {
                overlay.setTransparency(ROUTE_UNSELECTED_TRANSPARENCY);
                mRouteViewTwo.setVisibility(View.INVISIBLE);
                mRouteTextStrategyTwo.setTextColor(getResources().getColor(R.color.colorDark));
                mRouteTextTimeTwo.setTextColor(getResources().getColor(R.color.colorBlack));
                mRouteTextDistanceTwo.setTextColor(getResources().getColor(R.color.colorDark));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 第三条路线是否focus
     *
     * @param focus focus为true 突出颜色显示，标示为选中状态，为false则标示非选中状态
     */
    private void setLinelayoutThree(boolean focus) {
        if (mRouteLineLayoutThree.getVisibility() != View.VISIBLE) {
            return;
        }
        try {
            int routeID = (int) mRouteLineLayoutThree.getTag();
            RouteOverLay overlay = routeOverlays.get(routeID);
            if (overlay == null) {
                return;
            }
            if (focus) {
                mCalculateRouteOverView.setText(Utils.getRouteOverView(overlay.getAMapNaviPath()));
                mAMapNavi.selectRouteId(routeID);
                overlay.setTransparency(ROUTE_SELECTED_TRANSPARENCY);
                mRouteViewThree.setVisibility(View.VISIBLE);
                mRouteTextStrategyThree.setTextColor(getResources().getColor(R.color.colorBlue));
                mRouteTextTimeThree.setTextColor(getResources().getColor(R.color.colorBlue));
                mRouteTextDistanceThree.setTextColor(getResources().getColor(R.color.colorBlue));
            } else {
                overlay.setTransparency(ROUTE_UNSELECTED_TRANSPARENCY);
                mRouteViewThree.setVisibility(View.INVISIBLE);
                mRouteTextStrategyThree.setTextColor(getResources().getColor(R.color.colorDark));
                mRouteTextTimeThree.setTextColor(getResources().getColor(R.color.colorBlack));
                mRouteTextDistanceThree.setTextColor(getResources().getColor(R.color.colorDark));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 单路径算路成功回调
     */
    @Override
    public void onCalculateRouteSuccess() {
        dissmissProgressDialog();
        /**
         * 清空上次计算的路径列表。
         */
        routeOverlays.clear();
        HashMap<Integer, AMapNaviPath> paths = new HashMap<Integer, AMapNaviPath>();
        AMapNaviPath path = mAMapNavi.getNaviPath();
        /**
         * 由于是但路径，为了适应多路径的布局，将路线进行处理
         */
        paths.put(1, path);
        drawRoutes(1, path);
        mStrategyBean = new StrategyBean(false, false, false, false);
        System.out.println(array.length);
        setRouteLineTag(paths, array);
    }

    /**
     * 路线规划失败回调
     */
    @Override
    public void onCalculateRouteFailure(int i) {
        dissmissProgressDialog();
        Toast.makeText(this.getApplicationContext(), "路径计算出错，请重试", Toast.LENGTH_LONG).show();
    }

    /**
     * 驾车路线搜索
     */
    public void onDriveClick(View ciew) {
        aMap.clear();// 清理地图上的所有覆盖物
        mImageStrategy.setVisibility(View.VISIBLE);
        showProgressDialog();
        initNavi();
        mDrive.setImageResource(R.drawable.route_drive_select);
        mBus.setImageResource(R.drawable.route_bus_normal);
        mWalk.setImageResource(R.drawable.route_walk_normal);
        mapView.setVisibility(View.VISIBLE);
        mBusResultLayout.setVisibility(View.GONE);
    }

    /**
     * 公交路线搜索
     */
    public void onBusClick(View view) {
        searchRouteResult(ROUTE_TYPE_BUS, RouteSearch.BusDefault);
        mDrive.setImageResource(R.drawable.route_drive_normal);
        mBus.setImageResource(R.drawable.route_bus_select);
        mWalk.setImageResource(R.drawable.route_walk_normal);
        mapView.setVisibility(View.GONE);
        mBusResultLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 公交路线搜索结果方法回调
     */
    @Override
    public void onBusRouteSearched(BusRouteResult result, int errorCode) {
        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mBusRouteResult = result;
                    BusResultListAdapter mBusResultListAdapter = new BusResultListAdapter(mContext, mBusRouteResult);
                    mBusResultList.setAdapter(mBusResultListAdapter);
                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.show(mContext, R.string.no_result);
                }
            } else {
                ToastUtil.show(mContext, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }
    }


    /**
     * 开始搜索路径规划方案
     */
    public void searchRouteResult(int routeType, int mode) {
        if (mStartPoint == null) {
            ToastUtil.show(mContext, "起点未设置");
            return;
        }
        if (mEndPoint == null) {
            ToastUtil.show(mContext, "终点未设置");
        }
        if (mStartPoint.equals(mEndPoint)) {
            ToastUtil.show(mContext, "起点和终点相同");
        }
        showProgressDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                new LatLonPoint(mStartPoint.getLatitude(), mStartPoint.getLongitude()),
                new LatLonPoint(mEndPoint.getLatitude(), mEndPoint.getLongitude()));
        if (routeType == ROUTE_TYPE_BUS) {// 公交路径规划
            RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(fromAndTo, mode,
                    mCurrentCityName, 0);// 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式，第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
            mRouteSearch.calculateBusRouteAsyn(query);// 异步路径规划公交模式查询
        }
    }

    /**
     * 设置底部状态的可见性
     */
    public void setVisiable(Boolean visiable) {
        visiableRouteLine(false, false, false);
        if (visiable == false) {
            mStartNaviButton.setVisibility(View.INVISIBLE);
            mCalculateRouteOverView.setVisibility(View.INVISIBLE);
        } else {
            mStartNaviButton.setVisibility(View.VISIBLE);
            mCalculateRouteOverView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 步行路线搜索
     */
    public void onWalkClick(View view) {
        MODEFLAG = 1;
        aMap.clear();// 清理地图上的所有覆盖物
        mImageStrategy.setVisibility(View.GONE);
        setVisiable(false);
        mAMapNavi.calculateWalkRoute(mStartPoint, mEndPoint);
        mDrive.setImageResource(R.drawable.route_drive_normal);
        mBus.setImageResource(R.drawable.route_bus_normal);
        mWalk.setImageResource(R.drawable.route_walk_select);
        mapView.setVisibility(View.VISIBLE);
        mBusResultLayout.setVisibility(View.GONE);
        showProgressDialog();
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在规划路线，请稍等。。。");
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
        setVisiable(true);
    }

    /**
     * 自定义广播接收接口
     */
    @Override
    public void notifyAllActivity(Intent intent) {
        double mLatitude = intent.getExtras().getDouble("Latitude");
        double mLongitude = intent.getExtras().getDouble("Longitude");

        if (BTNFLAG == 1) {
            mStartAdBtn.setText("从：" + intent.getExtras().getString("addressName"));
            mStartPoint = new NaviLatLng(mLatitude, mLongitude);

        } else if (BTNFLAG == 2) {
            mEndAdBtn.setText("到：" + intent.getExtras().getString("addressName"));
            mEndPoint = new NaviLatLng(mLatitude, mLongitude);
        } else {
            ToastUtil.show(RestRouteShowActivity.this, "错误，不是任何一个按钮");
        }
        BTNFLAG = 0;
        startList.clear();//清理起点
        endList.clear();//清理终点
        startList.add(mStartPoint);//起点表添加点
        endList.add(mEndPoint);//终点表添加点
        initNavi();
        aMap.clear();//清除地图上的覆盖物
        mDrive.setImageResource(R.drawable.route_drive_select);
        mBus.setImageResource(R.drawable.route_bus_normal);
        mWalk.setImageResource(R.drawable.route_walk_normal);
        mapView.setVisibility(View.VISIBLE);
        mBusResultLayout.setVisibility(View.GONE);
    }

    /**
     * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
     */
    private void changeCamera(CameraUpdate update, AMap.CancelableCallback callback) {
        aMap.animateCamera(update, 250, null);
    }

    //地图路线规划暂时不用
    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

    /**
     * ************************************************** 在算路页面，以下接口全不需要处理，在以后的版本中我们会进行优化***********************************************************************************************
     **/

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo arg0) {


    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo arg0) {


    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] arg0) {


    }

    @Override
    public void hideCross() {


    }

    @Override
    public void hideLaneInfo() {


    }

    @Override
    public void notifyParallelRoad(int arg0) {


    }

    @Override
    public void onArriveDestination() {


    }

    @Override
    public void onArriveDestination(NaviStaticInfo naviStaticInfo) {

    }

    @Override
    public void onArriveDestination(AMapNaviStaticInfo aMapNaviStaticInfo) {

    }

    @Override
    public void onArrivedWayPoint(int arg0) {


    }

    @Override
    public void onEndEmulatorNavi() {


    }

    @Override
    public void onGetNavigationText(int arg0, String arg1) {


    }

    @Override
    public void onGpsOpenStatus(boolean arg0) {


    }

    @Override
    public void onInitNaviFailure() {


    }

    @Override
    public void onLocationChange(AMapNaviLocation arg0) {


    }

    @Override
    public void onNaviInfoUpdate(NaviInfo arg0) {


    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo arg0) {


    }

    @Override
    public void onReCalculateRouteForTrafficJam() {


    }

    @Override
    public void onReCalculateRouteForYaw() {


    }

    @Override
    public void onStartNavi(int arg0) {


    }

    @Override
    public void onTrafficStatusUpdate() {


    }

    @Override
    public void showCross(AMapNaviCross arg0) {


    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] arg0, byte[] arg1, byte[] arg2) {


    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo arg0) {


    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat arg0) {


    }


}
