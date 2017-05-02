package com.example.administrator.travel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Window;
import android.widget.Toast;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviStaticInfo;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.autonavi.tbt.NaviStaticInfo;
import com.autonavi.tbt.TrafficFacilityInfo;


public class RouteNaviActivity extends Activity implements AMapNaviListener, AMapNaviViewListener {

	AMapNaviView mAMapNaviView;
	AMapNavi mAMapNavi;
	TTSController mTtsManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_basic_navi);
		mTtsManager = TTSController.getInstance(getApplicationContext());
		mTtsManager.init();

		mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_view);
		mAMapNaviView.onCreate(savedInstanceState);
		mAMapNaviView.setAMapNaviViewListener(this);

		mAMapNavi = AMapNavi.getInstance(getApplicationContext());
		mAMapNavi.addAMapNaviListener(this);
		mAMapNavi.addAMapNaviListener(mTtsManager);
		mAMapNavi.setEmulatorNaviSpeed(20000);
		boolean gps=getIntent().getBooleanExtra("gps", true);
		if(gps){
			mAMapNavi.startNavi(AMapNavi.GPSNaviMode);
		}else{
			mAMapNavi.startNavi(AMapNavi.EmulatorNaviMode);
		}
		initGPS();

	}
	private void initGPS() {
		LocationManager locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		// 判断GPS模块是否开启，如果没有则开启
		if (!locationManager
				.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			Toast.makeText(RouteNaviActivity.this, "请打开GPS，确保导航正常进行",
					Toast.LENGTH_SHORT).show();
			AlertDialog.Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK);
			dialog.setMessage("请打开GPS，确保导航正常进行");
			dialog.setPositiveButton("确定",
					new android.content.DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// 转到手机设置界面，用户设置GPS
							Intent intent = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivityForResult(intent, 0); // 设置完成后返回到原来的界面

						}
					});
			dialog.setNeutralButton("取消", new android.content.DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
				}
			} );
			dialog.show();
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		mAMapNaviView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mAMapNaviView.onPause();
		//        仅仅是停止你当前在说的这句话，一会到新的路口还是会再说的
		mTtsManager.stopSpeaking();
		//
		//        停止导航之后，会触及底层stop，然后就不会再有回调了，但是讯飞当前还是没有说完的半句话还是会说完
		//        mAMapNavi.stopNavi();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mAMapNaviView.onDestroy();
		mAMapNavi.stopNavi();
		mAMapNavi.destroy();
		mTtsManager.destroy();
	}

	@Override
	public void onInitNaviFailure() {
		Toast.makeText(this, "初始化失败，请重试", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onInitNaviSuccess() {
	}

	@Override
	public void onStartNavi(int type) {

	}

	@Override
	public void onTrafficStatusUpdate() {

	}

	@Override
	public void onLocationChange(AMapNaviLocation location) {

	}

	@Override
	public void onGetNavigationText(int type, String text) {

	}

	@Override
	public void onEndEmulatorNavi() {
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
	public void onCalculateRouteSuccess() {
	}

	@Override
	public void onCalculateRouteFailure(int errorInfo) {
	}

	@Override
	public void onReCalculateRouteForYaw() {

	}

	@Override
	public void onReCalculateRouteForTrafficJam() {

	}

	@Override
	public void onArrivedWayPoint(int wayID) {

	}

	@Override
	public void onGpsOpenStatus(boolean enabled) {
	}

	@Override
	public void onNaviSetting() {
	}

	@Override
	public void onNaviMapMode(int isLock) {

	}

	@Override
	public void onNaviCancel() {
		finish();
	}

	@Override
	public void onNaviTurnClick() {

	}

	@Override
	public void onNextRoadClick() {

	}

	@Override
	public void onScanViewButtonClick() {
	}

	@Deprecated
	@Override
	public void onNaviInfoUpdated(AMapNaviInfo naviInfo) {
	}

	@Override
	public void onNaviInfoUpdate(NaviInfo naviinfo) {
	}

	@Override
	public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

	}

	@Override
	public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

	}

	@Override
	public void showCross(AMapNaviCross aMapNaviCross) {
	}

	@Override
	public void hideCross() {
	}

	@Override
	public void showLaneInfo(AMapLaneInfo[] laneInfos, byte[] laneBackgroundInfo, byte[] laneRecommendedInfo) {

	}

	@Override
	public void hideLaneInfo() {

	}

	@Override
	public void onCalculateMultipleRoutesSuccess(int[] ints) {

	}

	@Override
	public void notifyParallelRoad(int i) {

	}

	@Override
	public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

	}

	@Override
	public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

	}

	@Override
	public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

	}

	@Override
	public void onLockMap(boolean isLock) {
	}

	@Override
	public void onNaviViewLoaded() {
	}

	@Override
	public boolean onNaviBackClick() {
		return false;
	}

}
