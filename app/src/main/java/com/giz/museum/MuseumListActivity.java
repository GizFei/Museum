package com.giz.museum;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.giz.bmob.MuseumLibrary;
import com.giz.utils.BlurBackgroundManager;
import com.giz.utils.CoverFlowEffectTransformer;
import com.giz.utils.CoverFlowPagerAdapter;
import com.giz.bmob.Museum;

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

public class MuseumListActivity extends AppCompatActivity {

    // 列表显示样式：
    // true：列表形式，false：网格形式
    private boolean isListStyle = true;
    private MuseumAdapter mMuseumAdapter;
    private RecyclerView mMuseumRecyclerView;
    private ViewPager mMuseumViewPager;
    private ImageView mSwitchIcon;
    private View mPagerBg;
    private SearchView mSearchView;
    private Map<String, Drawable> mBgDrawables; // museumId -> drawable
    private FloatingActionButton mFab;
    private CoverFlowPagerAdapter mPagerAdapter;
    private AppBarLayout mAppBarLayout;
    private ProgressBar mProgressBar;

    private List<Museum> mMuseumList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_museum);

        Bmob.initialize(this, "d86d0b43c41c255217e9377f570e3283");

        mAppBarLayout = findViewById(R.id.myAppBar);
        // 初始化列表
        mMuseumRecyclerView = findViewById(R.id.list_museum);
        mMuseumRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        mMuseumAdapter = new MuseumAdapter(MuseumLib.get(this).getMuseumList());
//        mMuseumRecyclerView.setAdapter(mMuseumAdapter);
//        new MuseumListTask().execute();
        // 初始化悬浮按钮
        mFab= findViewById(R.id.map_fab);
        // 初始化列表样式转换按钮
        mSwitchIcon = findViewById(R.id.list_style);
        // 初始化搜索框
        mSearchView = findViewById(R.id.search_view);
        View underline = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        if(underline != null){
            underline.setBackgroundColor(Color.TRANSPARENT);
        }
        // 初始化ViewPager
        mMuseumViewPager = findViewById(R.id.pager_museum);
        mPagerBg = findViewById(R.id.pager_bg);
        //
        mProgressBar = findViewById(R.id.progressBar);
        // 初始化事件
        initEvents();

//        mMuseumLibrary = new MuseumLibrary();
        mMuseumList = new ArrayList<>();
        downloadMuseumList();
//        new MuseumListTask().execute();
        //
    }

    private void initEvents(){
        final CardView cardView = findViewById(R.id.card_search_view);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
//                Log.d("APPBAR", String.valueOf(appBarLayout.getTotalScrollRange()));
//                Log.d("APPBAR", String.valueOf(i));
                if(i == 0){
                    cardView.setBackgroundTintList(ColorStateList.valueOf(getResources().
                            getColor(R.color.light_gray)));
                }else if(i == -appBarLayout.getTotalScrollRange()){
                    cardView.setBackgroundTintList(ColorStateList.valueOf(getResources().
                            getColor(R.color.white)));
                }
                appBarLayout.getBackground().setAlpha((int)((float)(appBarLayout.getTotalScrollRange()+i)/appBarLayout.getTotalScrollRange() * 255));
            }
        });

        //        final SwipeController controller = new SwipeController();
//        mMuseumRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
//            @Override
//            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
//                controller.onDraw(c);
//            }
//        });
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(controller);
//        itemTouchHelper.attachToRecyclerView(mMuseumRecyclerView);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityOptionsCompat compat = ActivityOptionsCompat.makeClipRevealAnimation(v,
                        v.getWidth()/2, v.getHeight()/2, 0, 0);
                ActivityCompat.startActivity(MuseumListActivity.this,
                        new Intent(MuseumListActivity.this, MuseumMapActivity.class),
                        compat.toBundle());
            }
        });

        mSwitchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchRecyclerView();
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(isListStyle){
                    mMuseumAdapter.setMuseumList(MuseumLibrary.get().queryMuseumsByWord(newText));
                    mMuseumAdapter.notifyDataSetChanged();
                }else{
                    mPagerAdapter.setMuseumList(MuseumLibrary.get().queryMuseumsByWord(newText));
                    mPagerAdapter.notifyDataSetChanged();
//                    mMuseumViewPager.setAdapter(mPagerAdapter);
                }
                return true;
            }
        });

        initViewPager();
    }

    private void initViewPager(){

        mMuseumViewPager.setClipChildren(false);
        mMuseumViewPager.setOffscreenPageLimit(3);
        mMuseumViewPager.setPageTransformer(false, new CoverFlowEffectTransformer(this));

//        mPagerAdapter = new CoverFlowPagerAdapter(this,
//                MuseumLib.get(this).getMuseumList(), this, mSearchView);
//        mMuseumViewPager.setAdapter(mPagerAdapter);

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
//                Log.d("onPageSelected", String.valueOf(i));
                currentPage = i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                state = i;
                if(i == ViewPager.SCROLL_STATE_IDLE){
                    if(pageChanged){
                        setMuseumViewPagerBg(currentPage);
                        pageChanged = false;
                    }else{
                        mPagerBg.setAlpha(1);
                    }

                }
//                Log.d("onPageScrollState", String.valueOf(i));
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
                mPagerBg.getBackground(), mBgDrawables.get(mMuseumList.get(i).getMuseumId())});
        mPagerBg.setBackground(transitionDrawable);
        transitionDrawable.startTransition(400);

        // 恢复图片的透明度至1
//        mPagerBg.setBackground(mBgDrawables.get(i));
        ValueAnimator animator = ObjectAnimator.ofFloat(mPagerBg, "alpha",
                0.5f, 1.0f);
        animator.setDuration(200);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    /**
     * 切换列表显示样式
     */
    private void switchRecyclerView() {
        mSearchView.setQuery("", false);
        mSearchView.clearFocus();
        if(isListStyle){
            AnimatedVectorDrawableCompat listToGridAnim = AnimatedVectorDrawableCompat.create(this,
                    R.drawable.av_list_to_pager);
            mSwitchIcon.setImageDrawable(listToGridAnim);
            ((Animatable)mSwitchIcon.getDrawable()).start();
            isListStyle = false;

            mMuseumRecyclerView.setVisibility(View.GONE);
            mMuseumViewPager.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this,
                    R.anim.layout_anim_from_bottom));
            mMuseumViewPager.setVisibility(View.VISIBLE);
//            mPagerBg.setVisibility(View.VISIBLE);
            mMuseumViewPager.setCurrentItem(0, true);
            mPagerBg.setBackground(mBgDrawables.get(mMuseumList.get(0).getMuseumId()));

            // 悬浮球隐藏
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams)mFab.getLayoutParams();
            mFab.animate().translationY(mFab.getHeight() + layoutParams.bottomMargin).
                    setInterpolator(new LinearInterpolator()).start();
            // 工具栏透明
            mAppBarLayout.setBackgroundResource(R.color.transparent);
        }else{
            AnimatedVectorDrawableCompat gridToListAnim = AnimatedVectorDrawableCompat.create(this,
                    R.drawable.av_pager_to_list);
            mSwitchIcon.setImageDrawable(gridToListAnim);
            ((Animatable)mSwitchIcon.getDrawable()).start();
            isListStyle = true;

            mMuseumRecyclerView.setVisibility(View.VISIBLE);
            mMuseumViewPager.setVisibility(View.GONE);
            mPagerBg.setBackgroundResource(R.color.light_bg);
//            mPagerBg.setVisibility(View.GONE);

            //new MuseumListTask().execute();
            mMuseumAdapter.setMuseumList(mMuseumList);
            mMuseumAdapter.notifyDataSetChanged();
            mMuseumRecyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this,
                    R.anim.layout_anim_from_bottom));
            //mMuseumRecyclerView.setAdapter(mMuseumAdapter);
            mMuseumRecyclerView.startLayoutAnimation();

            // 悬浮球显示
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams)mFab.getLayoutParams();
            mFab.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
            // 工具栏恢复
            mAppBarLayout.setBackgroundResource(R.color.white);
        }
    }

    private class MuseumHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView mMuseumName;
        private List<TextView> mMuseumCatalogs;
        private ImageView mMuseumLogo;
        private Museum mMuseum;

        private MuseumHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mMuseumCatalogs = new ArrayList<>();
            mMuseumName = itemView.findViewById(R.id.museum_name);
            mMuseumCatalogs.add((TextView)itemView.findViewById(R.id.museum_catalog1));
            mMuseumCatalogs.add((TextView)itemView.findViewById(R.id.museum_catalog2));
            mMuseumCatalogs.add((TextView)itemView.findViewById(R.id.museum_catalog3));
            mMuseumLogo = itemView.findViewById(R.id.museum_logo);
        }

        private void bind(Museum museum){
            mMuseum = museum;
            mMuseumName.setText(museum.getName());
            int catalogs = museum.getCatalog().size();
            for(int i = 0; i < mMuseumCatalogs.size(); i++){
                if(i < catalogs){
                    mMuseumCatalogs.get(i).setVisibility(View.VISIBLE);
                    mMuseumCatalogs.get(i).setText(museum.getCatalog().get(i));
                }else{
                    mMuseumCatalogs.get(i).setVisibility(View.GONE);
                }
            }
            mMuseumLogo.setImageDrawable(museum.getLogo());
        }

        @Override
        public void onClick(View v) {
            mSearchView.setQuery("", false);
            mSearchView.clearFocus();
            Intent intent = MuseumActivity.newIntent(MuseumListActivity.this,
                    mMuseum.getMuseumId());
            ActivityOptionsCompat compat = ActivityOptionsCompat.makeCustomAnimation(
                        MuseumListActivity.this, R.anim.activity_in, R.anim.activity_out);
            ActivityCompat.startActivity(MuseumListActivity.this, intent, compat.toBundle());
        }
    }

    private class MuseumAdapter extends RecyclerView.Adapter<MuseumHolder>{

        private List<Museum> mMuseums;

        private MuseumAdapter(List<Museum> list){
            mMuseums = list;
        }

        @NonNull
        @Override
        public MuseumHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(MuseumListActivity.this);
            return new MuseumHolder(inflater.inflate(R.layout.list_museum_item, viewGroup,
                        false));
        }

        @Override
        public void onBindViewHolder(@NonNull MuseumHolder museumHolder, int i) {
            museumHolder.bind(mMuseums.get(i));
        }

        @Override
        public int getItemCount() {
            return mMuseums.size();
        }

        private void setMuseumList(List<Museum> list){
            mMuseums = list;
        }
    }

//    private class BlurBgTask extends AsyncTask<Void, Void, Void>{
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            Log.d("BLUR", "Downloading...");
//            mBgDrawables = BlurBackgroundManager.get(MuseumListActivity.this).getBlurBackgrounds();
//            return null;
//        }
//
//    }

    private class MuseumListTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            // 防止误点击
            mSwitchIcon.setEnabled(false);
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
//            mProgressBar.setVisibility(View.GONE);
            Log.d("kkk", "onPostExecute");
            if(mMuseumAdapter == null){
                mMuseumAdapter = new MuseumAdapter(mMuseumList);
                mMuseumRecyclerView.setAdapter(mMuseumAdapter);
                mMuseumRecyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(MuseumListActivity.this,
                        R.anim.layout_anim_from_bottom));
                mMuseumRecyclerView.startLayoutAnimation();
            }
//            else{
//                mMuseumAdapter.setMuseumList(mMuseumList);
//                mMuseumAdapter.notifyDataSetChanged();
//            }

            if(mPagerAdapter == null){
                mPagerAdapter = new CoverFlowPagerAdapter(MuseumListActivity.this,
                        mMuseumList, MuseumListActivity.this, mSearchView);
                mMuseumViewPager.setAdapter(mPagerAdapter);
            }

            // 预先加载好模糊背景
            setUpBlurBackground();
//            mBgDrawables = BlurBackgroundManager.get(MuseumListActivity.this).getBlurBackgrounds();
//            mSwitchIcon.setEnabled(true);
        }
    }

    private void downloadMuseumList(){
        Log.d("kkk", "download");
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
                            museum.setPicFolder(object.getString("picFolder"));
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
        BmobQuery query = new BmobQuery("picture");
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray array, BmobException e) {
                if(e == null){
                    try{
                        List<String> urls = new ArrayList<>();
                        List<String> ids = new ArrayList<>();
                        for(int i = 0; i < array.length(); i++){
                            ids.add(array.getJSONObject(i).getString("museumId"));
                            urls.add(array.getJSONObject(i).getJSONObject("img0").getString("url"));
                        }
                        new BlurBgTask().execute(urls, ids);
                    }catch (Exception ee){
                        ee.printStackTrace();
                    }
                }
            }
        });
    }

    private class BlurBgTask extends AsyncTask<List<String>, Void, Void>{
        @Override
        protected Void doInBackground(List<String>... lists) {
            List<String> urls = lists[0];
            List<String> ids = lists[1];
            try{
                for(int i = 0; i < urls.size(); i++){
                    mBgDrawables.put(ids.get(i), Drawable.createFromStream(new URL(urls.get(i)).openStream(), "BG"));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mSwitchIcon.setEnabled(true);
            mProgressBar.setVisibility(View.GONE);
            super.onPostExecute(aVoid);
        }
    }
}
