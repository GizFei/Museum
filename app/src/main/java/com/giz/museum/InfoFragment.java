package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.giz.database.Museum;
import com.giz.database.MuseumLibrary;
import com.giz.customize.CustomToast;
import com.giz.customize.JustifyTextView;
import com.giz.utils.MuseumPicturePagerAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * getActivity为空：回退的时候，Fragment被销毁，但是异步进程还在运行，导致异步进程中的getActivity方法错误
 */
public class InfoFragment extends Fragment {

    private static final String ARGS_ID = "bundle_id";
    private static final String TAG = "InfoFragment";

    private MuseumActivity mActivity;       // 属于的活动
    private Museum mMuseum;                 // 博物馆
    private CardView mInfoCard;             // 信息卡片
    private CardView mIntroCard;            // 博物馆介绍卡片
    private ProgressBar mMuseumProgress;    // 博物馆信息加载进度条
    private Toolbar mToolbar;               // 工具栏

    private AppBarLayout mAppBarLayout;                 // 工具栏
    private LinearLayout mDotsLinearLayout;             // 轮播圆点提示
    private NestedScrollView mScrollView;               // 嵌套滚动视图
    private ContentLoadingProgressBar mImagesProgressBar;  // 轮播图片加载进度条
    private ViewPager mViewPager;                       // 轮播视图
    private MuseumPicturePagerAdapter mPagerAdapter;    // 轮播视图适配器

    private FloatingActionButton mArcMainBtn;

    /**
     * 创建InfoFragment，传入博物馆的ID
     * @param museumId 博物馆ID
     * @return InfoFragment实例
     */
    public static InfoFragment newInstance(String museumId, FloatingActionButton arcMain){
        InfoFragment fragment = new InfoFragment();
        Bundle bundle = new Bundle();

        fragment.mArcMainBtn = arcMain;
        bundle.putString(ARGS_ID, museumId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (MuseumActivity)context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 获得博物馆信息
        String id = getArguments().getString(ARGS_ID);
        Log.d(TAG, id);
        mMuseum = MuseumLibrary.get().getMuseumById(id);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        mInfoCard = view.findViewById(R.id.info);
        mIntroCard = view.findViewById(R.id.introduction);
        mMuseumProgress = view.findViewById(R.id.progress_museum);
        mImagesProgressBar = view.findViewById(R.id.progressBar);
        mAppBarLayout = view.findViewById(R.id.myAppBar);
        mDotsLinearLayout = view.findViewById(R.id.dots_ll);
        mScrollView = view.findViewById(R.id.scrollView);
        mViewPager = view.findViewById(R.id.picture_vp);
        mToolbar = view.findViewById(R.id.toolbar);

        // 折叠布局
        CollapsingToolbarLayout ctl = view.findViewById(R.id.ctl);
        ctl.setTitle(mMuseum.getName());
        ctl.setCollapsedTitleTextColor(getResources().getColor(R.color.white));
        ctl.setExpandedTitleColor(getResources().getColor(R.color.transparent));
        ctl.setStatusBarScrimResource(R.color.colorPrimaryDark);

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

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
                if(-i == appBarLayout.getTotalScrollRange()){
                    mArcMainBtn.animate().scaleY(0).scaleX(0).setDuration(400).alpha(0.0f).start();
                    mArcMainBtn.hide();
                }else if(i == 0){
                    mArcMainBtn.animate().scaleY(1).scaleX(1).setDuration(400).alpha(1f).start();
                    mArcMainBtn.show();
                }
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 结束后台任务
//        if(mActivityOrShowTask != null){
//            mActivityOrShowTask.cancel(true);
//        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 初始化视图中的内容

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.onBackPressed();
            }
        });

        // 初始化轮播视图
        setUpPager();
        // 初始化博物馆信息
        initDetails();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void setUpPager(){
        if(!isNetWorkAvailableAndConnected()){
            mImagesProgressBar.setVisibility(View.GONE);
            mDotsLinearLayout.setVisibility(View.GONE);
            return;
        }
        mDotsLinearLayout.setVisibility(View.VISIBLE);

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
                        new PagerPicTask().execute(urls);
                    }catch (Exception ee){
                        mImagesProgressBar.setVisibility(View.GONE);
                        CustomToast.make(getContext(), "未找到图片数据").show();
                        ee.printStackTrace();
                    }
                }
            }
        });
    }

    private void initInfoCard(){
        LinearLayout infoContainer = mInfoCard.findViewById(R.id.info_container);
        ((TextView)infoContainer.getChildAt(0)).setText(mMuseum.getName()); // 名称
        ((TextView)infoContainer.getChildAt(1)).setText("地址：" + mMuseum.getAddress()); // 地址
        ((TextView)infoContainer.getChildAt(2)).setText("门票：" + mMuseum.getTicket());  // 门票
        ((TextView)infoContainer.getChildAt(3)).setText("开放时间：" + mMuseum.getOpenTime()); // 开放时间
    }

    private void initIntroCard(){
        ((JustifyTextView)mIntroCard.findViewById(R.id.museum_intro)).setText(mMuseum.getIntro());
        mMuseumProgress.setVisibility(View.GONE);
    }

    private void initDetails(){
        if(!isNetWorkAvailableAndConnected()){
            mMuseumProgress.setVisibility(View.GONE);
        }else{
            BmobQuery query = new BmobQuery("detail");
            query.addWhereEqualTo("museumId", mMuseum.getMuseumId());
            query.findObjectsByTable(new QueryListener<JSONArray>() {
                @Override
                public void done(JSONArray array, BmobException e) {
                    if(e == null){
                        try{
                            JSONObject object = array.getJSONObject(0);
                            mMuseum.setAddress(object.getString("address"));
                            mMuseum.setTicket(object.getString("ticket"));
                            mMuseum.setOpenTime(object.getString("opentime"));
                            mMuseum.setIntro(object.getString("intro"));
                            initInfoCard();
                            initIntroCard();
                        }catch (Exception ee){
                            ee.printStackTrace();
                            mMuseumProgress.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }
    }

    private class PagerPicTask extends AsyncTask<List<String>, Void, List<Drawable>>{

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
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Drawable> drawables) {
            mImagesProgressBar.setVisibility(View.GONE);
            if(drawables == null){
                mImagesProgressBar.setVisibility(View.GONE);
                CustomToast.make(getContext(), "未找到图片数据").show();
            }else{
                mPagerAdapter = new MuseumPicturePagerAdapter(mActivity, drawables);
                mViewPager.setAdapter(mPagerAdapter);

                for(int i = mPagerAdapter.getCount(); i < 5; i++){
                    mDotsLinearLayout.getChildAt(i).setVisibility(View.GONE);
                }
            }
        }
    }

    private boolean isNetWorkAvailableAndConnected(){
        ConnectivityManager cm = (ConnectivityManager)mActivity.getSystemService(CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected());
    }
}
