package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.RouteSearch;
import com.giz.bmob.MuseumLibrary;

public class MuseumTrackActivity extends AppCompatActivity {

    private static final String KEY_ID = "museumId";
    private String museumId;

    public static Intent newIntent(Context packageContext, String Id) {
        Intent intent = new Intent(packageContext, MuseumTrackActivity.class);
        intent.putExtra(KEY_ID, Id);
        return intent;
    }

    private MapView mMapView;
    public AMapLocationListener locationListener;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    RouteSearch mRouteSearch;
    double posEnd[] = MuseumLibrary.get().getMuseumById(museumId).getLocation();
    LatLonPoint mStartPoint;
    LatLonPoint mEndPoint = new LatLonPoint(posEnd[1], posEnd[0]);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        museumId = getIntent().getStringExtra(KEY_ID);

        mMapView = findViewById(R.id.the_map);
        mMapView.onCreate(savedInstanceState);
        AMap aMap = mMapView.getMap();

        initLocation();
        startLocation();

        //显示定位蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        //连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        //蓝点精确度个性化
        myLocationStyle.strokeColor(255);
        myLocationStyle.radiusFillColor(100);
        myLocationStyle.strokeWidth(10);
        //设置定位蓝点的Style
        aMap.setMyLocationStyle(myLocationStyle);
        //设置默认定位按钮是否显示，即回到小蓝点
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        //设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false
        aMap.setMyLocationEnabled(true);
        //是否显示定位蓝点
        myLocationStyle.showMyLocation(true);

        /*测试从Dialog转到Activity，成功
        String name = MuseumLibrary.get().getMuseumById(museumId).getName();
        Toast t = Toast.makeText(getApplicationContext(), name, Toast.LENGTH_LONG);
        t.show();
        */

        initRouteSearch();
    }

    private void initLocation() {
        //初始化client
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = getDefaultOption();
        //设置定位监听
        locationClient.setLocationListener(locationListener);
    }

    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        //可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //可选，设置定位间隔。默认为2秒
        mOption.setInterval(2000);
        //可选，设置是否使用缓存定位，默认为true
        mOption.setLocationCacheEnable(true);
        return mOption;
    }

    private void startLocation() {
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        //启动定位
        locationClient.startLocation();
    }

    private void initRouteSearch() {
        mRouteSearch = new RouteSearch(this);
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(mStartPoint, mEndPoint);
    }

    private void destroyLocation(){
        if (null != locationClient) {
            /*
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.stopLocation();
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        destroyLocation();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityCompat.finishAfterTransition(this);
    }
}
