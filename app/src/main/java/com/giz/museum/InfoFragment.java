package com.giz.museum;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
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

import com.giz.bmob.Museum;
import com.giz.bmob.MuseumLibrary;
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

    private Museum mMuseum;                 // 博物馆
    private CardView mInfoCard;             // 信息卡片
    private CardView mIntroCard;            // 博物馆介绍卡片
    private CardView mActivityCard;         // 活动卡片
    private CardView mNewsCard;             // 新闻卡片
    private ProgressBar mMuseumProgress;    // 博物馆信息加载进度条

    //private AppBarLayout mAppBarLayout;                 // 工具栏
    private LinearLayout mDotsLinearLayout;             // 轮播圆点提示
    private CoordinatorLayout mCoordinatorLayout;       // 坐标布局
    private NestedScrollView mScrollView;               // 嵌套滚动视图
    private ContentLoadingProgressBar mImagesProgressBar;  // 轮播图片加载进度条
    private ViewPager mViewPager;                       // 轮播视图
    private MuseumPicturePagerAdapter mPagerAdapter;    // 轮播视图适配器

    private ActivityOrShowTask mActivityOrShowTask;
    private NewsTask mNewsTask;
    private boolean mHasStarred;

    /**
     * 创建InfoFragment，传入博物馆的ID
     * @param museumId 博物馆ID
     * @return InfoFragment实例
     */
    public static InfoFragment newInstance(String museumId){
        InfoFragment fragment = new InfoFragment();
        Bundle bundle = new Bundle();

        bundle.putString(ARGS_ID, museumId);
        fragment.setArguments(bundle);
        return fragment;
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
        mActivityCard = view.findViewById(R.id.recent_activity);
        mNewsCard = view.findViewById(R.id.recent_news);
        mMuseumProgress = view.findViewById(R.id.progress_museum);
        mImagesProgressBar = view.findViewById(R.id.progressBar);
        mCoordinatorLayout = view.findViewById(R.id.coordinator);
        mScrollView = view.findViewById(R.id.scrollView);
        mViewPager = view.findViewById(R.id.picture_vp);

        // 折叠布局
        CollapsingToolbarLayout ctl = view.findViewById(R.id.ctl);
        ctl.setTitle(mMuseum.getName());
        ctl.setCollapsedTitleTextColor(getResources().getColor(R.color.white));
        ctl.setExpandedTitleColor(getResources().getColor(R.color.transparent));
        ctl.setStatusBarScrimResource(R.color.colorPrimaryDark);

        mDotsLinearLayout = view.findViewById(R.id.dots_ll);

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

//        mAppBarLayout = view.findViewById(R.id.myAppBar);
//        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
//            @Override
//            public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
//                if(i != 0 && menu.isOpen())
//                    menu.fold();
//                float factor = 1.0f - (-(float)i) / appBarLayout.getTotalScrollRange();
//                menu.setAlpha(factor);
//                arcMain.setScaleX(factor);
//                arcMain.setScaleY(factor);
//            }
//        });

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        // 初始化轮播视图
        setUpPager();
        // 初始化博物馆信息
        initDetails();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 结束后台任务
        if(mActivityOrShowTask != null){
            mActivityOrShowTask.cancel(true);
        }
        if(mNewsTask != null){
            mNewsTask.cancel(true);
        }
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
            //getView().findViewById(R.id.detail_tip_no_net).setVisibility(View.VISIBLE);
            return;
        }
        mDotsLinearLayout.setVisibility(View.VISIBLE);
        //getView().findViewById(R.id.detail_tip_no_net).setVisibility(View.GONE);

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
    }

    private void initActivityCard(List<MuseumAOrS> museumAOrs){
        LinearLayout activityContainer = mActivityCard.findViewById(R.id.activity_container);
        if(museumAOrs == null)
            return;
        for(int i = 0; i < museumAOrs.size(); i++){
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.card_item_activity, null);
            MuseumAOrS aOrS = museumAOrs.get(i);
            ((ImageView)view.findViewById(R.id.pic_activity)).setImageDrawable(aOrS.thumbDrawable);
            ((TextView)view.findViewById(R.id.title_activity)).setText(aOrS.title);
            if(aOrS.tag.equals("activity")){
                ((ImageView)view.findViewById(R.id.tag_activity)).setImageResource(R.mipmap.tag_activity);
            }else{
                ((ImageView)view.findViewById(R.id.tag_activity)).setImageResource(R.mipmap.tag_show);
            }
            ((TextView)view.findViewById(R.id.date_activity)).setText(aOrS.date);
            ((TextView)view.findViewById(R.id.place_activity)).setText(aOrS.place);
            ((TextView)view.findViewById(R.id.people_activity)).setText(aOrS.people);
            if(i == museumAOrs.size()-1)
                view.findViewById(R.id.divider).setVisibility(View.GONE);
            activityContainer.addView(view);
        }
    }

    private void initNewsCard(List<MuseumNews> museumNews){
        LinearLayout newsContainer = mNewsCard.findViewById(R.id.news_container);
        if(museumNews == null){
            mMuseumProgress.setVisibility(View.GONE);
            return;
        }
        for(int i = 0; i < museumNews.size(); i++){
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.card_item_news, null);
            final MuseumNews news = museumNews.get(i);
            ((ImageView)view.findViewById(R.id.pic_news)).setImageDrawable(news.thumbDrawable);
            ((TextView)view.findViewById(R.id.title_news)).setText(news.title);
            ((TextView)view.findViewById(R.id.date_news)).setText(news.date);
            ((TextView)view.findViewById(R.id.url_news)).setText(news.url);
            if(i == museumNews.size()-1)
                view.findViewById(R.id.divider).setVisibility(View.GONE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = WebViewActivity.newIntent(getActivity(), news.url);
                    startActivity(intent);
                }
            });
            newsContainer.addView(view);
        }
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
                            mActivityOrShowTask.execute(object.getJSONArray("activities"));
                            mNewsTask.execute(object.getJSONArray("news"));
                        }catch (Exception ee){
                            ee.printStackTrace();
                            mMuseumProgress.setVisibility(View.GONE);
                        }
                    }
                }
            });

            mActivityOrShowTask = new ActivityOrShowTask();
            mNewsTask = new NewsTask();
        }
    }

    private class ActivityOrShowTask extends AsyncTask<JSONArray, Void, List<MuseumAOrS>>{
        @Override
        protected List<MuseumAOrS> doInBackground(JSONArray... arrays) {
            try{
                List<MuseumAOrS> museumAOrs = new ArrayList<>();
                Log.d("INFOFRAGMENT", String.valueOf(arrays[0].length()));
                for(int i = 0; i < arrays[0].length(); i++){
                    JSONObject activity = arrays[0].getJSONObject(i);
                    MuseumAOrS aOrS = new MuseumAOrS();
                    aOrS.tag = activity.getString("tag");
                    aOrS.title = activity.getString("title");
                    aOrS.thumbDrawable = Drawable.createFromStream(new URL(activity.getString("thumburl")).openStream(), "THUMB");
                    aOrS.date = activity.getString("date");
                    aOrS.place = activity.getString("place");
                    aOrS.people = activity.getString("people");
                    museumAOrs.add(aOrS);
                }
                return museumAOrs;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<MuseumAOrS> aOrS) {
            initActivityCard(aOrS);
        }
    }

    private class NewsTask extends AsyncTask<JSONArray, Void, List<MuseumNews>>{
        @Override
        protected List<MuseumNews> doInBackground(JSONArray... jsonArrays) {
            try{
                List<MuseumNews> museumNews = new ArrayList<>();
                for(int i = 0; i < jsonArrays[0].length(); i++){
                    JSONObject news = jsonArrays[0].getJSONObject(i);
                    MuseumNews mn = new MuseumNews();
                    mn.title = news.getString("title");
                    mn.date = news.getString("date");
                    mn.url = news.getString("url");
                    mn.thumbDrawable = Drawable.createFromStream(new URL(news.getString("thumburl")).openStream(), "THUMB");
                    museumNews.add(mn);
                }
                return museumNews;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<MuseumNews> museumNews) {
            initNewsCard(museumNews);
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
                mPagerAdapter = new MuseumPicturePagerAdapter(getContext(), drawables);
                mViewPager.setAdapter(mPagerAdapter);

                for(int i = mPagerAdapter.getCount(); i < 5; i++){
                    mDotsLinearLayout.getChildAt(i).setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * 博物馆活动或展览类
     */
    private class MuseumAOrS {
        String tag;
        String title;
        Drawable thumbDrawable;
        String date;
        String place;
        String people;
    }

    private class MuseumNews{
        String title;
        Drawable thumbDrawable;
        String date;
        String url;
    }

    private boolean isNetWorkAvailableAndConnected(){
        ConnectivityManager cm = (ConnectivityManager)getContext().getSystemService(CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected());
    }

    // 收起AppBarLayout
//    private void hideHeaders() {
//        Log.d("MuseumActivity", "HideHeaders");
//        // 禁止滑动
//        mScrollView.setNestedScrollingEnabled(false);
//        // 向上滑动AppBarLayout以隐藏
//        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)mAppBarLayout.getLayoutParams();
//        CoordinatorLayout.Behavior behavior = params.getBehavior();
//        behavior.onNestedPreScroll(mCoordinatorLayout, mAppBarLayout, mScrollView, 0,
//                mAppBarLayout.getTotalScrollRange(), new int[]{0, 0}, 0);
//    }
}
