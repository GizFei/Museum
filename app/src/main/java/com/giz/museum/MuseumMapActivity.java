package com.giz.museum;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.giz.bmob.Museum;
import com.giz.bmob.MuseumLibrary;

/*
    To_Do_List:
    1.镜头动态过渡效果
    2.博物馆卡片中信息的完善
    3.个性化InfoWindow
 */

public class MuseumMapActivity extends AppCompatActivity {

    private MapView mMapView;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mMapView = findViewById(R.id.the_map);

        mMapView.onCreate(savedInstanceState);
        AMap aMap = mMapView.getMap();
        //默认显示杭州市
        LatLng centerHZPoint = new LatLng(30.294833, 120.159627);
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerHZPoint,13));

        //初始化定位
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

        //InfoWindow的监听事件
        AMap.OnInfoWindowClickListener infoListener = new AMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String id = MuseumLibrary.get().queryMuseumsByWord(marker.getTitle()).get(0).getMuseumId();
                startActivity(MuseumActivity.newIntent(MuseumMapActivity.this, id));
                finish();
            }
        };
        aMap.setOnInfoWindowClickListener(infoListener);
        showMuseum(aMap);
    }

    private void initLocation() {
        //初始化client
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = getDefaultOption();
        //设置定位监听
        locationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                return;
            }
        });
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

    private void showMuseum(AMap aMap) {
        int totalNum = MuseumLibrary.get().getMuseumList().size();
        Marker marker;
        for (int i = 0; i < totalNum; i++) {
            String name = MuseumLibrary.get().getMuseumList().get(i).getName();
            double pos[] = MuseumLibrary.get().getMuseumList().get(i).getLocation();
            LatLng temp = new LatLng(pos[1], pos[0]);
            marker = aMap.addMarker(new MarkerOptions().position(temp).title(name).snippet("具体地址"));
        }
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
