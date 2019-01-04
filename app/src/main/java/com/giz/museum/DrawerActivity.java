package com.giz.museum;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.transition.Fade;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v8.renderscript.RenderScript;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import com.giz.customize.CustomToast;
import com.giz.database.Museum;
import com.giz.database.MuseumLibrary;
import com.giz.utils.ACache;
import com.giz.utils.BitmapUtils;
import com.giz.utils.FastBlur;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;

public class DrawerActivity extends AppCompatActivity {

    private static final String TAG = "DrawerActivity";
    private static final String TAG_INDEX_FRAGMENT = "IndexFragment";
    private static final String TAG_COLLECT_FRAGMENT = "CollectionFragment";
    private static final String TAG_RECORD_FRAGMENT = "RecordFragment";
    private static final String CACHE_ARRAY_KEY = "MuseumListArray";

    // 抽屉菜单容器
    private static final int drawerMenuContainer = R.id.left_menu_container;
    // 内容容器
    private static final int drawerContentContainer = R.id.drawer_content_container;

    private DrawerLayout mDrawerLayout;

    private IndexFragment mIndexFragment;      // 保存首页片段
    private CollectionFragment mCollectionFragment; // 我的收藏
    private RecordFragment mRecordFragment;         // 我的记录

    private FragmentManager mManager;

    private Bitmap captureOfContent;
    private ACache mACache;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.dark_gray));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        Bmob.initialize(this, "d86d0b43c41c255217e9377f570e3283");

        mDrawerLayout = findViewById(R.id.index_drawer_layout);
        mManager = getSupportFragmentManager();
        mACache = ACache.get(getApplicationContext());

        initDrawerMenu();
        initEvents();
        initFragments();
        setupWindowAnimations();
        // 获得缓存的列表
        getCacheMuseumList();
    }

    private void setupWindowAnimations() {
//        Slide slide = (Slide)TransitionInflater.from(this).inflateTransition(R.transition.activity_slide);
//        slide.setSlideEdge(Gravity.START);
        Fade fade = (Fade)TransitionInflater.from(this).inflateTransition(R.transition.activity_fade);
        getWindow().setExitTransition(fade);
    }

    /**
     * 初始化抽屉菜单
     */
    private void initDrawerMenu(){
        // 遮罩颜色为透明
        mDrawerLayout.setScrimColor(0x00FFFFFF);
        // 左侧抽屉菜单监听事件
        DrawerMenuFragment drawerMenuFragment = DrawerMenuFragment.newInstance(new DrawerMenuFragment.OnItemSelectedListener() {
            @Override
            public void onItemSelected(View view, int pos) {
                mDrawerLayout.closeDrawer(Gravity.START);
//                Log.d(TAG, "drawer menu item tag" + view.getTag().toString());
                switch (view.getTag().toString()){
                    case DrawerMenuFragment.ITEM_INDEX:
                        //首页
                        hideFragments();
                        if(mIndexFragment == null){
                            mIndexFragment = IndexFragment.newInstance();
                            mManager.beginTransaction().setCustomAnimations(R.anim.fragment_from_bottom, 0).add(drawerContentContainer,
                                    mIndexFragment, TAG_INDEX_FRAGMENT).commit();
                        }else{
                            mManager.beginTransaction().setCustomAnimations(R.anim.fragment_from_bottom, 0).show(mIndexFragment).commit();
                        }
                        break;
                    case DrawerMenuFragment.ITEM_COLLECTION:
                        // 我的收藏
                        hideFragments();
                        if(mCollectionFragment == null){
                            mCollectionFragment = CollectionFragment.newInstance();
                            mManager.beginTransaction().setCustomAnimations(R.anim.fragment_from_bottom, 0).add(drawerContentContainer,
                                    mCollectionFragment, TAG_COLLECT_FRAGMENT).commit();
                        }else{
                            mManager.beginTransaction().setCustomAnimations(R.anim.fragment_from_bottom, 0).show(mCollectionFragment).commit();
                        }
                        break;
                    case DrawerMenuFragment.ITEM_RECORD:
                        // 我的记录
                        hideFragments();
                        if(mRecordFragment == null){
                            mRecordFragment = RecordFragment.newInstance();
                            mManager.beginTransaction().setCustomAnimations(R.anim.fragment_from_bottom, 0).add(drawerContentContainer,
                                    mRecordFragment, TAG_RECORD_FRAGMENT).commit();
                        }else{
                            mManager.beginTransaction().setCustomAnimations(R.anim.fragment_from_bottom, 0).show(mRecordFragment).commit();
                        }
                        break;
                    case DrawerMenuFragment.ITEM_ABOUT:
                        // 关于Museum
                        break;
                }
            }
        });
        // 添加左侧抽屉菜单
        mManager.beginTransaction().add(drawerMenuContainer,
                drawerMenuFragment).commit();
    }

    /**
     * 隐藏所有的碎片
     */
    private void hideFragments() {
        FragmentTransaction transaction = mManager.beginTransaction();
        if(mIndexFragment != null){
            transaction.hide(mIndexFragment);
        }
        if(mCollectionFragment != null){
            transaction.hide(mCollectionFragment);
        }
        if(mRecordFragment != null){
            transaction.hide(mRecordFragment);
        }
        transaction.commit();
    }

    /**
     * 初始化事件
     */
    private void initEvents(){
//        Toolbar toolbar = findViewById(R.id.drawer_toolbar);
////        setSupportActionBar(toolbar);
//        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
//                toolbar, R.string.drawer_open_desc, R.string.drawer_close_desc)
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {
                View mainContentView = mDrawerLayout.getChildAt(0);

                // 抽屉布局固定位置，透明度从0.6 - 1
                view.setAlpha(0.6f + 0.4f * v);
                view.setTranslationX(view.getMeasuredWidth() * (1 - v));
                // 主页面变小
                float contentScaleFactor = 0.8f + 0.2f * (1 - v); // 1 - 0.8
                mainContentView.invalidate();
                mainContentView.setPivotX(0);
                mainContentView.setPivotY(mainContentView.getMeasuredHeight() / 2);
                mainContentView.setScaleX(contentScaleFactor);
                mainContentView.setScaleY(contentScaleFactor);
                mainContentView.setTranslationX(view.getMeasuredWidth() * v * 0.8f);

                // 实时模糊
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    View mContent = mDrawerLayout.getChildAt(0);
                    if(captureOfContent == null){
                        mContent.setDrawingCacheEnabled(true);
                        mContent.buildDrawingCache();
                        captureOfContent = Bitmap.createBitmap(mContent.getDrawingCache());
                    }
                    Bitmap smallBm = BitmapUtils.resizeBitmap(captureOfContent, captureOfContent.getWidth()/4, captureOfContent.getHeight()/4, false);
                    Bitmap blur = BitmapUtils.resizeBitmap(FastBlur.gaussianBlur(RenderScript.create(DrawerActivity.this), Math.round(8 * v + 0.5f), smallBm), captureOfContent.getWidth(), captureOfContent.getHeight(), true);
                    mContent.setForeground(new BitmapDrawable(getResources(), blur));
                }
            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
                // 在打开状态模糊
//                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//                    View mContent = mDrawerLayout.getChildAt(0);
//                    mContent.setDrawingCacheEnabled(true);
//                    mContent.buildDrawingCache();
//                    Bitmap captureOfContent = Bitmap.createBitmap(mContent.getDrawingCache());
//
//                    Bitmap blur = FastBlur.gaussianBlur(RenderScript.create(DrawerActivity.this), 16, captureOfContent);
//                    mContent.setForeground(new BitmapDrawable(blur));
//                }
            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
//                Log.d(TAG, "drawer CLOSE");
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    mDrawerLayout.getChildAt(0).setForeground(null);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
//                Log.d(TAG, "drawer state" + String.valueOf(newState));
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && newState == DrawerLayout.STATE_IDLE && !mDrawerLayout.isDrawerOpen(Gravity.START)){
                    mDrawerLayout.getChildAt(0).setForeground(null);
                    captureOfContent = null;
                }
            }
        });

//        mDrawerLayout.addDrawerListener(drawerToggle);
//        drawerToggle.syncState();
    }

    private void initFragments() {
        mIndexFragment = IndexFragment.newInstance();
        mManager.beginTransaction().
                add(drawerContentContainer, mIndexFragment, TAG_INDEX_FRAGMENT).commit();
    }

    @Override
    public void onBackPressed() {
        // 先关闭抽屉
        // 再回退到桌面而不退出应用
        Log.d(TAG, "stack count" + String.valueOf(mManager.getBackStackEntryCount()));
        if(mDrawerLayout.isDrawerOpen(Gravity.START)){
            mDrawerLayout.closeDrawer(Gravity.START);
        }else{
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
    }

    public void openDrawerMenu(){
        if(!mDrawerLayout.isDrawerOpen(Gravity.START)){
            mDrawerLayout.openDrawer(Gravity.START);
        }
    }

    /**
     * 获取缓存的博物馆列表
     */
//    private void getCacheMuseumList() {
//        try {
//            JSONArray array = mACache.getAsJSONArray(CACHE_ARRAY_KEY);
//            if(array != null){
//                for(int i = 0; i < array.length(); i++){
//                    JSONObject object = array.getJSONObject(i);
//                    Museum museum = new Museum(object.getString("objectId"));
//                    museum.setName(object.getString("name"));
//                    museum.setLogoUrl(object.getJSONObject("logo").getString("url"));
//                    museum.setCoverUrl(object.getJSONObject("cover").getString("url"));
//                    museum.setLocation(new double[]{object.getJSONArray("location").getDouble(0),
//                            object.getJSONArray("location").getDouble(1)});
//                    museum.setLogo(mACache.getAsDrawable(museum.getLogoCacheKey()));
//                    mMuseumList.add(museum);
//                }
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    private boolean isNetWorkAvailableAndConnected(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected());
    }

    /**
     * 获取缓存的博物馆列表
     */
    private void getCacheMuseumList() {
        try {
            List<Museum> mMuseumList = new ArrayList<>();
            JSONArray array = mACache.getAsJSONArray(CACHE_ARRAY_KEY);
            for(int i = 0; i < array.length(); i++){
                JSONObject object = array.getJSONObject(i);
                Museum museum = new Museum(object.getString("objectId"));
                museum.setName(object.getString("name"));
                museum.setCatalog(getCatalog(object.getJSONArray("catalog")));
                museum.setLogoUrl(object.getJSONObject("logo").getString("url"));
                museum.setCoverUrl(object.getJSONObject("cover").getString("url"));
                museum.setLocation(new double[]{object.getJSONArray("location").getDouble(0),
                        object.getJSONArray("location").getDouble(1)});
                museum.setLogo(mACache.getAsDrawable(museum.getLogoCacheKey()));
                mMuseumList.add(museum);
            }
            MuseumLibrary.get().setMuseumList(mMuseumList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<String> getCatalog(JSONArray array) throws JSONException {
        List<String> list = new ArrayList<>();
        for(int j = 0; j < array.length(); j++){
            list.add(array.getString(j));
        }
        return list;
    }
}
