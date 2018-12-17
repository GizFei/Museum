package com.giz.museum;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v8.renderscript.RenderScript;
import android.transition.Fade;
import android.transition.TransitionInflater;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.giz.database.MuseumLibrary;
import com.giz.customize.CustomToast;
import com.giz.utils.CoverFlowEffectTransformer;
import com.giz.utils.CoverFlowPagerAdapter;
import com.giz.database.Museum;
import com.giz.utils.FastBlur;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;

public class MuseumPagerActivity extends AppCompatActivity {
    // 卡片布局
    private static final String TAG = "MuseumPagerActivity";
    private static final String CURRENT_PAGE = "currentPage";

    private ViewPager mMuseumViewPager;         // 卡片
    private ImageView mPagerBg;                 // 背景
    private ImageView mBackBtn;                 // 返回按钮
    private ImageView mSearchBtn;               // 搜索按钮
    private Map<String, Drawable> mBgDrawables; // museumId -> drawable
    private CoverFlowPagerAdapter mPagerAdapter;    // 适配器
    private ProgressBar mProgressBar;               // 加载进度条
    private Toolbar mSearchToolbar;                 // 搜索工具栏
    private ConstraintLayout mToolbarLike;
    private FrameLayout mSearchFragmentContainer;

    private SearchFragment mSearchFragment;
    private List<Museum> mMuseumList;
    private int mCurrentPage = 0;            // 记录当前的卡片

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化窗口动画
        setupWindowAnimations();
        setContentView(R.layout.activity_pager_museum);

        Bmob.initialize(this, "d86d0b43c41c255217e9377f570e3283");

        mPagerBg = findViewById(R.id.pager_bg);     // 背景
        mBackBtn = findViewById(R.id.pager_museum_back);
        mProgressBar = findViewById(R.id.pager_museum_progress);
        mSearchBtn = findViewById(R.id.pager_museum_search);
        mSearchToolbar = findViewById(R.id.pager_museum_search_toolbar);
        mMuseumViewPager = findViewById(R.id.pager_museum_pager);
        mToolbarLike = findViewById(R.id.pager_museum_cl);
        mSearchFragmentContainer = findViewById(R.id.search_fragment_container);

        initEvents();
        setupSearchView();

        mMuseumList = new ArrayList<>();
        downloadMuseumList();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: ");
        outState.putInt(CURRENT_PAGE, mCurrentPage);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        mMuseumViewPager.setCurrentItem(mCurrentPage);
        super.onResume();
    }

    private void setupWindowAnimations() {
        Fade fade = (Fade)TransitionInflater.from(this).inflateTransition(R.transition.activity_fade);
//        Slide slide = (Slide)TransitionInflater.from(this).inflateTransition(R.transition.activity_slide);
//        slide.setSlideEdge(Gravity.END);
        getWindow().setEnterTransition(fade);
    }

    private void setupSearchView() {
        mSearchToolbar.inflateMenu(R.menu.menu_search);
        Menu menu = mSearchToolbar.getMenu();
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("搜索博物馆...");
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Log.d(TAG, "on expand");
                // 展开搜索框
                mSearchFragmentContainer.setVisibility(View.VISIBLE);
                mSearchFragment = SearchFragment.newInstance();
                getSupportFragmentManager().beginTransaction().add(R.id.search_fragment_container,
                        mSearchFragment).commit();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Log.d(TAG, "on collapse");
                collapseSearchView();
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fragment_to_bottom,
                        R.anim.fragment_to_bottom, R.anim.fragment_to_bottom, R.anim.fragment_to_bottom).remove(mSearchFragment).commit();
                mSearchFragment = null;
                //mSearchFragmentContainer.setVisibility(View.GONE); // 隐藏容器
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
//                Log.d(TAG, "Focus "+String.valueOf(s));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(TAG, "Focus "+String.valueOf(s));
                if(mSearchFragment == null){
                    mSearchFragment = SearchFragment.newInstance();
                    getSupportFragmentManager().beginTransaction().add(R.id.search_fragment_container,
                            mSearchFragment).commit();
                    mSearchFragment.updateRecyclerView(s);
                }else{
                    mSearchFragment.updateRecyclerView(s);
                }
                return true;
            }
        });
    }

    private void initEvents(){

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandSearchView();
            }
        });

        initViewPager();
    }

    private void initViewPager(){

        mMuseumViewPager.setClipChildren(false);
        mMuseumViewPager.setOffscreenPageLimit(3);
        mMuseumViewPager.setPageTransformer(false, new CoverFlowEffectTransformer(this));

        mMuseumViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            /**
             * state
             * SCROLL_STATE_DRAGGING 1 手指按下
             * SCROLL_STATE_SETTLING 2 手指松开
             * SCROLL_STATE_IDLE 0 停止滑动
             */
            int currentPage = 0;
            int state;
            boolean pageChanged = false;

            private float half(float alpha){
                return (alpha * 0.5f + 0.5f);
            }

            @Override
            public void onPageScrolled(int i, float v, int i1) {
//                Log.d("onPageScrolled", String.format("%d %f", i, v));
                if(state == 1){ // 手指滑动
                    if(i < currentPage)
                        mPagerBg.setAlpha(half(v));
                    else
                        mPagerBg.setAlpha(half(1-v));
                }else if(state == 2){ // 布局自己滑动（补足偏差）
                    if(!pageChanged){
                        if(i == currentPage){
                            mPagerBg.setAlpha(half(1-v));
                        }else{
                            if(v != 0f)
                                mPagerBg.setAlpha(half(v));
                        }
                    }else{
                        if(i == currentPage){
                            mPagerBg.setAlpha(half(v));
                        }else{
                            mPagerBg.setAlpha(half(1-v));
                        }
                    }
                }
            }

            @Override
            public void onPageSelected(int i){
                pageChanged = true;
                currentPage = i;
                mCurrentPage = i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                state = i;
                if(i == ViewPager.SCROLL_STATE_IDLE){
                    if(pageChanged){
                        Log.d("onPageScrollState", String.valueOf(currentPage));
                        setMuseumViewPagerBg(currentPage);
                        pageChanged = false;
                    }else{
                        mPagerBg.setAlpha(1f);
                    }

                }
            }
        });
    }

    /**
     * 设置ViewPager列表样式下应用背景
     * @param i 列表项位置
     */
    private void setMuseumViewPagerBg(int i){
        // 前后图片平滑过渡
        TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{
                mPagerBg.getDrawable(), mBgDrawables.get(mPagerAdapter.getMuseumIdByPosition(i))});
        mPagerBg.setImageDrawable(transitionDrawable);
        transitionDrawable.startTransition(400);

        // 恢复图片的透明度至1
        ValueAnimator animator = ObjectAnimator.ofFloat(mPagerBg, "alpha",
                0.5f, 1.0f);
        animator.setDuration(200);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    private class MuseumListTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("kkk", "doInBackground");
            for(int i = 0; i < mMuseumList.size(); i++){
                try{
                    mMuseumList.get(i).setLogo((Drawable.createFromStream(new URL(mMuseumList.get(i).getLogoUrl()).openStream(), "LL")));
                    mMuseumList.get(i).setCover((Drawable.createFromStream(new URL(mMuseumList.get(i).getCoverUrl()).openStream(), "COVER")));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            Log.d("kkk", "onPostExecute");

            // 预先加载好模糊背景
            setUpBlurBackground();
        }
    }

    // 从云端下载博物馆列表
    private void downloadMuseumList(){
//        // 防止误点击
        if(!isNetWorkAvailableAndConnected()){
            mProgressBar.setVisibility(View.GONE);
            mPagerBg.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mPagerBg.setImageResource(R.drawable.tip_no_internet);
            return;
        }
        // 不重复请求
        if(mPagerAdapter != null){
            return;
        }

        Log.d(TAG, "download museum list");
        BmobQuery query = new BmobQuery("museum");
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray jsonArray, BmobException e) {
                Log.d("kkk", "download");
                if(e == null){
                    try {
                        for(int i = 0; i < jsonArray.length(); i++){
                            JSONObject object = jsonArray.getJSONObject(i);
                            Museum museum = new Museum(object.getString("objectId"));
                            museum.setName(object.getString("name"));
                            museum.setCatalog(getCatalog(object.getJSONArray("catalog")));
                            museum.setLogoUrl(object.getJSONObject("logo").getString("url"));
                            museum.setCoverUrl(object.getJSONObject("cover").getString("url"));
                            museum.setLocation(new double[]{object.getJSONArray("location").getDouble(0),
                                    object.getJSONArray("location").getDouble(1)});
//                            Log.d("BMOB", museum.getMuseumId());
                            mMuseumList.add(museum);
                        }
                        MuseumLibrary.get().setMuseumList(mMuseumList);
                        new MuseumListTask().execute();
                    }catch (Exception je){
                        Log.e("JSON Error: ", je.getMessage());
                        je.printStackTrace();
                    }
                }
            }
        });
    }

    private List<String> getCatalog(JSONArray array) throws JSONException {
        List<String> list = new ArrayList<>();
        for(int j = 0; j < array.length(); j++){
            list.add(array.getString(j));
        }
        return list;
    }

    private void setUpBlurBackground(){
        mBgDrawables = new HashMap<>();
        for(Museum museum: mMuseumList){
            Drawable drawable = museum.getCover();
//            Bitmap blurBg = FastBlur.doBlur(((BitmapDrawable)drawable).getBitmap(), 64, false);
            Bitmap blurBg = FastBlur.scaleGaussianBlur(RenderScript.create(this), ((BitmapDrawable)drawable).getBitmap(), 16);
            mBgDrawables.put(museum.getMuseumId(), new BitmapDrawable(getResources(), blurBg));
        }
        if(mPagerAdapter == null){
            mPagerAdapter = new CoverFlowPagerAdapter(MuseumPagerActivity.this, mMuseumList,
                    MuseumPagerActivity.this, null);
            mMuseumViewPager.setAdapter(mPagerAdapter);
            setMuseumViewPagerBg(0);    // 首个
        }

        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if(mSearchToolbar.getVisibility() == View.VISIBLE){
            collapseSearchView();
//            mSearchFragmentContainer.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fragment_to_bottom,
                    R.anim.fragment_to_bottom, R.anim.fragment_to_bottom, R.anim.fragment_to_bottom)
                    .remove(mSearchFragment).commit();
            mSearchFragment = null;
        }else{
            super.onBackPressed();
        }
    }

    private boolean isNetWorkAvailableAndConnected(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected());
    }

    // 展开搜索框
    private void expandSearchView(){
        Log.d(TAG, "Expand search view");
        mSearchToolbar.setVisibility(View.VISIBLE);
        Menu menu = mSearchToolbar.getMenu();
        MenuItem searchView = menu.findItem(R.id.action_search);
        searchView.expandActionView();
        searchView.getActionView().requestFocus();
        int cx = mToolbarLike.getWidth();
        int cy = mToolbarLike.getHeight();
        Animator anim = ViewAnimationUtils.createCircularReveal(mSearchToolbar, cx,
                cy / 2, 0, cx);
        anim.setDuration(400);
        anim.start();
    }
    // 折叠搜索框
    private void collapseSearchView(){
        int cx = mToolbarLike.getWidth();
        int cy = mToolbarLike.getHeight();
        mToolbarLike.setVisibility(View.VISIBLE);
        Animator anim = ViewAnimationUtils.createCircularReveal(mSearchToolbar, cx,
                cy / 2, cx, 0);
        anim.setDuration(300);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mSearchToolbar.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationCancel(Animator animation) {

            }
            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.start();
    }

}
