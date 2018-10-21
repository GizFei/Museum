package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.giz.bmob.Museum;
import com.giz.bmob.MuseumLibrary;

import com.amap.api.services.core.AMapException;
import com.giz.customize.CustomToast;

import overlay.WalkRouteOverlay;

/*
    To_Do_List:
    1.导航对应的文字部分
    2.适当增加骑行路行选择
 */

public class MuseumTrackActivity extends AppCompatActivity {

    AMap aMap;
    private static final String KEY_ID = "museumId";
    private String museumId;
    RouteSearch mRouteSearch;

    public static Intent newIntent(Context packageContext, String Id) {
        Intent intent = new Intent(packageContext, MuseumTrackActivity.class);
        intent.putExtra(KEY_ID, Id);
        return intent;
    }

    private MapView mMapView;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    LatLonPoint mStartPoint, mEndPoint;
    private WalkRouteResult mWalkRouteResult;


    //NETWORK耗电小精确度差，GPS反之
    double[] posStart = new double[2];
    boolean isLocated = false;
    String provider;
    AMapLocationListener mAMapLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        museumId = getIntent().getStringExtra(KEY_ID);

        mMapView = findViewById(R.id.the_map);
        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();
        //默认显示杭州市
        LatLng centerHZPoint = new LatLng(30.294833, 120.159627);
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerHZPoint,13));

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

        //监听器是异步的！！！
        //initRouteSearch();
    }

    private void initLocation() {
        //初始化client
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = getDefaultOption();
        //定位监听
        mAMapLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    if (aMapLocation.getErrorCode() == 0) {
                        if(!isLocated) {
                            posStart[0] = aMapLocation.getLongitude();
                            posStart[1] = aMapLocation.getLatitude();
                            isLocated = true;
                            initRouteSearch();
                        }
                    }
                    else {
                        Log.e("AmapError","location Error, ErrCode:"
                                + aMapLocation.getErrorCode() + ", errInfo:"
                                + aMapLocation.getErrorInfo());
                    }
                }
                else
                    CustomToast.make(getApplicationContext(), "无法定位", Toast.LENGTH_LONG).show();
            }
        };
        //声明定位回调监听器
        locationClient.setLocationListener(mAMapLocationListener);
    }

    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        //可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //可选，设置定位间隔。默认为2秒
        mOption.setInterval(2000);
        //可选，设置是否使用缓存定位，默认为true
        mOption.setLocationCacheEnable(false);
        return mOption;
    }

    private void startLocation() {
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        //启动定位
        locationClient.startLocation();
    }

    private void initRouteSearch() {
        double[] posEnd = MuseumLibrary.get().getMuseumById(museumId).getLocation();
        //初始化RouteSearch对象
        mRouteSearch = new RouteSearch(this);
        mStartPoint = new LatLonPoint(posStart[1], posStart[0]);
        mEndPoint = new LatLonPoint(posEnd[1], posEnd[0]);

        //设置数据回调监听器
        mRouteSearch.setRouteSearchListener(new RouteSearch.OnRouteSearchListener() {
            @Override
            public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {
            }
            @Override
            public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
            }
            @Override
            public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
                if (i == AMapException.CODE_AMAP_SUCCESS) {
                    if (walkRouteResult != null && walkRouteResult.getPaths() != null) {
                        if (walkRouteResult.getPaths().size() > 0) {
                            mWalkRouteResult = walkRouteResult;
                            final WalkPath walkPath = mWalkRouteResult.getPaths().get(0);
                            if (walkPath == null) {
                                return;
                            }
                            WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
                                    getApplicationContext(), aMap, walkPath,
                                    mWalkRouteResult.getStartPos(),
                                    mWalkRouteResult.getTargetPos());
                            walkRouteOverlay.removeFromMap();
                            walkRouteOverlay.addToMap();
                            walkRouteOverlay.zoomToSpan();
                        }
                    }
                }
            }
            @Override
            public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {
            }
        });

        //设置搜索参数
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(mStartPoint, mEndPoint);
        RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo);
        //进行骑行规划路径计算，发送请求
        mRouteSearch.calculateWalkRouteAsyn(query);
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
