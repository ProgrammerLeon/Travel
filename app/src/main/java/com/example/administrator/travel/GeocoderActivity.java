package com.example.administrator.travel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.List;

/**
 * modified by han yuanfeng
 * 地理编码与逆地理编码功能介绍
 */
public class GeocoderActivity extends Activity implements Inputtips.InputtipsListener, TextWatcher, mListener, PoiSearch.OnPoiSearchListener {
    private ListView mpointListview;
    private AutoCompleteTextView searchText;// 输入搜索关键字
    private LinearLayout search_header;
    private TextView msearchBtn;
    private PoiResult poiResult; // poi返回的结果
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;// POI搜索
    private ProgressDialog progDialog = null;// 搜索时进度条
    private String keyWord = "";// 要输入的poi搜索关键字
    private String editCity = "中国";// 要输入的城市名字或者城市区号
    private PoiOverlay mpoiOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geocoder_activity);
        init();
        ListenerManager.getInstance().registerListtener(this);//注册广播接收器
//        Intent mIntent = getIntent();
//        msearchBtn.setText(mIntent.getExtras().getString("btnName"));
        msearchBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doSearchQuery();
            }
        });
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        searchText = (AutoCompleteTextView) findViewById(R.id.keyWord);
        searchText.addTextChangedListener(this);// 添加文本输入框监听事件
        mpointListview = (ListView) findViewById(R.id.point_list_view);
        search_header = (LinearLayout) findViewById(R.id.search_header);
        msearchBtn = (TextView) findViewById(R.id.btn_search);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String newText = s.toString().trim();
        if (!AMapUtil.IsEmptyOrNullString(newText)) {
            InputtipsQuery inputquery = new InputtipsQuery(newText, "中国");
            Inputtips inputTips = new Inputtips(GeocoderActivity.this, inputquery);
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
    public void onPoiSearched(PoiResult result, int rCode) {
        dissmissProgressDialog();// 隐藏对话框
        List<String> listString = new ArrayList<String>();
        List<LatLng> pointList = new ArrayList<LatLng>();
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
                        mpoiOverlay = new PoiOverlay(null, poiItems);
                        listString = mpoiOverlay.getAddress();
                        for (int i = 0; i < poiItems.size(); i++) {
                            pointList.add(new LatLng(poiItems.get(0).getLatLonPoint().getLatitude(),
                                    poiItems.get(0).getLatLonPoint().getLongitude()));
                        }
                        SearchresultAdaper aAdapter = new SearchresultAdaper(
                                getApplicationContext(), listString,pointList, searchText);
                        mpointListview.setAdapter(aAdapter);
                        aAdapter.notifyDataSetChanged();
                    }
                    else {
                        ToastUtil.show(GeocoderActivity.this,
                                R.string.no_result);
                    }
                }
            } else {
                ToastUtil.show(GeocoderActivity.this,
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
     * 自定义广播接收接口回调
     */

    @Override
    public void notifyAllActivity(Intent intent) {
        finish();
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
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
}