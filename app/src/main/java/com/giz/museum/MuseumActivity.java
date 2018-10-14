package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.giz.bmob.Museum;
import com.giz.bmob.MuseumLibrary;
import com.giz.customize.CustomBottomSheet;
import com.giz.utils.MuseumPicturePagerAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;

public class MuseumActivity extends AppCompatActivity {

    private static final String EXTRA_MUSEUM = "museum_intent";

    private Museum mMuseum;
    private AppBarLayout mAppBarLayout;
    private LinearLayout mDotsLinearLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private InfoFragment mInfoFragment;
    private PanoramaFragment mPanoramaFragment;
    private NestedScrollView mScrollView;
    private ContentLoadingProgressBar mProgressBar;

    private ViewPager mViewPager;
    private MuseumPicturePagerAdapter mPagerAdapter;

    public static Intent newIntent(Context context, String museumId){
        Intent intent = new Intent(context, MuseumActivity.class);
        intent.putExtra(EXTRA_MUSEUM, museumId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_museum);

        String museumId = getIntent().getStringExtra(EXTRA_MUSEUM);
        mMuseum = MuseumLibrary.get().getMuseumById(museumId);

        initViews();
        initFragments();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.fragment_container, mInfoFragment)
                .add(R.id.fragment_container, mPanoramaFragment).commit();
        setFragment(1);
    }

    /**
     * 初始化Fragment
     */
    private void initFragments() {
        mInfoFragment = InfoFragment.newInstance(mMuseum.getMuseumId());
        mPanoramaFragment = PanoramaFragment.newInstance();
    }

    /**
     * 初始化布局
     */
    private void initViews() {

        mProgressBar = findViewById(R.id.progressBar);

        mCoordinatorLayout = findViewById(R.id.coordinator);
        mScrollView = findViewById(R.id.scrollView);

        mViewPager = findViewById(R.id.picture_vp);
        setUpPager();
//        final MuseumPicturePagerAdapter adapter = new MuseumPicturePagerAdapter(this,
//                mMuseum.getPicFolder());
//        pictures.setAdapter(adapter);

        CollapsingToolbarLayout ctl = findViewById(R.id.ctl);
        ctl.setTitle(mMuseum.getName());
        ctl.setCollapsedTitleTextColor(getResources().getColor(R.color.white));
        ctl.setExpandedTitleColor(getResources().getColor(R.color.transparent));
        ctl.setStatusBarScrimResource(R.color.colorPrimaryDark);

        mDotsLinearLayout = findViewById(R.id.dots_ll);
//        for(int i = mPagerAdapter.getCount(); i < 5; i++){
//            mDotsLinearLayout.getChildAt(i).setVisibility(View.GONE);
//        }

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                for(int k = 0; k < mPagerAdapter.getCount(); k++){
                    if(k == i){
                        mDotsLinearLayout.getChildAt(k).
                                setBackgroundResource(R.drawable.icon_dot_active);
                    }else{
                        mDotsLinearLayout.getChildAt(k).setBackgroundResource(R.drawable.icon_dot);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.nav_info:
                        showHeaders();
                        setFragment(1);
                        return true;
                    case R.id.nav_nav:
                        hideHeaders();
                        setFragment(2);
                        return true;
                    case R.id.nav_pano:
                        hideHeaders();
                        setFragment(3);
                        return true;
                }
                return false;
            }
        });
        bottomNavigationView.setOnNavigationItemReselectedListener(null);

        FloatingActionButton mFAB = findViewById(R.id.action_fab);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomBottomSheet dialog = new CustomBottomSheet(MuseumActivity.this, mMuseum.getMuseumId());
//                dialog.setContentView(R.layout.bottom_sheet);
                dialog.show();
            }
        });

        mAppBarLayout = findViewById(R.id.myAppBar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    // 收起AppBarLayout和FAB
    private void hideHeaders() {
        Log.d("MuseumActivity", "HideHeaders");
        // 禁止滑动
        mScrollView.setNestedScrollingEnabled(false);
        // 向上滑动AppBarLayout以隐藏
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)mAppBarLayout.getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();
        behavior.onNestedPreScroll(mCoordinatorLayout, mAppBarLayout, mScrollView, 0,
                mAppBarLayout.getTotalScrollRange(), new int[]{0, 0}, 0);
    }

    private void showHeaders(){
        Log.d("MuseumActivity", "ShowHeaders");
        // 允许滑动
        mScrollView.setNestedScrollingEnabled(true);
        // 向下滑出以显示
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams)mAppBarLayout.getLayoutParams()).getBehavior();
        behavior.onNestedPreScroll(mCoordinatorLayout, mAppBarLayout, mScrollView, 0,
                -mAppBarLayout.getTotalScrollRange(), new int[]{0, 0}, 0);

    }

    /**
     * 显示当前的Fragment
     * @param i Fragment的编号，从左到右，从1开始
     */
    private void setFragment(int i) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragments(transaction);
        switch (i){
            case 1:
                transaction.show(mInfoFragment);
                break;
            case 2:
                break;
            case 3:
                transaction.show(mPanoramaFragment);
                break;
        }
        transaction.commit();
    }

    /**
     * 隐藏所有Fragment
     */
    private void hideFragments(FragmentTransaction transaction) {
        if(mInfoFragment != null){
            transaction.hide(mInfoFragment);
        }
        if(mPanoramaFragment != null){
            transaction.hide(mPanoramaFragment);
        }
    }

    @Override
    public void onBackPressed() {
        getSupportFragmentManager().beginTransaction().remove(mInfoFragment)
                .remove(mPanoramaFragment).commit();
        super.onBackPressed();
//        ActivityCompat.finishAfterTransition(this);
    }

    private void setUpPager(){
        BmobQuery query = new BmobQuery("picture");
        query.addWhereEqualTo("museumId", mMuseum.getMuseumId());
        Log.d("ID", mMuseum.getMuseumId());
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray array, BmobException e) {
                if(e == null && array.length() != 0){
                    try{
                        List<String> urls = new ArrayList<>();
                        JSONObject pics = array.getJSONObject(0);
                        int num = pics.getInt("num");
                        for(int i = 0; i < num; i++){
                            urls.add(pics.getJSONObject("img" + i).getString("url"));
                        }
                        new PicturesTask().execute(urls);
                    }catch (Exception ee){
                        ee.printStackTrace();
                    }
                }
            }
        });
    }

    private class PicturesTask extends AsyncTask<List<String>, Void, List<Drawable>>{

        @Override
        protected List<Drawable> doInBackground(List<String>... url) {
            try {
                List<String> urls = url[0];
                List<Drawable> drawables = new ArrayList<>();
                for(String u : urls){
                    drawables.add(Drawable.createFromStream(new URL(u).openStream(), "Drawable"));
                }
                return drawables;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Drawable> drawables) {
            mProgressBar.setVisibility(View.GONE);
            mPagerAdapter = new MuseumPicturePagerAdapter(MuseumActivity.this, drawables);
            mViewPager.setAdapter(mPagerAdapter);

            for(int i = mPagerAdapter.getCount(); i < 5; i++){
                mDotsLinearLayout.getChildAt(i).setVisibility(View.GONE);
            }
        }
    }
}
