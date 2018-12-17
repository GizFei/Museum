package com.giz.museum;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.giz.database.MuseumLibrary;
import com.giz.database.Museum;
import com.giz.utils.ACache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;

public class MuseumListActivity extends AppCompatActivity {
    private static final String TAG = "MuseumListActivity";
    private static final String CACHE_ARRAY_KEY = "MuseumListArray";

    private MuseumAdapter mMuseumAdapter;
    private RecyclerView mMuseumRecyclerView;

    private FloatingActionButton mFab;
    private Toolbar mToolbar;
    private Toolbar mSearchToolbar;
    private SwipeRefreshLayout mRefreshLayout;

    private List<Museum> mMuseumList;
    private SearchFragment mSearchFragment;
    private FrameLayout mSearchFragmentContainer;

    private ACache mACache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);
        // 初始化窗口动画
        setupWindowAnimations();
        setContentView(R.layout.activity_list_museum);

        Bmob.initialize(this, "d86d0b43c41c255217e9377f570e3283");

        // 初始化布局控件
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mSearchToolbar = findViewById(R.id.search_toolbar);
        mSearchFragmentContainer = findViewById(R.id.search_fragment_container);
        // 初始化列表
        mMuseumRecyclerView = findViewById(R.id.list_museum);
        mMuseumRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 初始化悬浮按钮
        mFab= findViewById(R.id.map_fab);
        // 初始化下拉刷新布局
        mRefreshLayout = findViewById(R.id.refresh_layout);
        mRefreshLayout.setProgressViewOffset(true, 0, 100);
        mRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        // 初始化缓存
        mACache = ACache.get(getApplicationContext());
        // 初始化事件
        initEvents();

        setupSearchView();
        mMuseumList = new ArrayList<>();
        downloadMuseumList();
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

    private void setupWindowAnimations() {
        Fade fade = (Fade)TransitionInflater.from(this).inflateTransition(R.transition.activity_fade);
//        Slide slide = (Slide)TransitionInflater.from(this).inflateTransition(R.transition.activity_slide);
//        slide.setSlideEdge(Gravity.END);
        getWindow().setEnterTransition(fade);
    }

    private void initEvents(){

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

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // 下拉刷新事件
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                downloadMuseumList();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_museum_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_search:
                expandSearchView();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
            mMuseumName = itemView.findViewById(R.id.index_museum_name);
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

    private class MuseumListTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("kkk", "doInBackground");
            for(int i = 0; i < mMuseumList.size(); i++){
                try{
                    Museum museum = mMuseumList.get(i);
                    Drawable drawable = mACache.getAsDrawable(museum.getLogoCacheKey());
                    if(drawable == null){
                        drawable = (Drawable.createFromStream(new URL(museum.getLogoUrl()).openStream(), "LL"));
                        museum.setLogo(drawable);
                        mACache.put(museum.getLogoCacheKey(), drawable);
                    }else{
                        museum.setLogo(drawable);
                    }
//                    museum.setCover((Drawable.createFromStream(new URL(museum.getCoverUrl()).openStream(), "COVER")));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            Log.d("kkk", "onPostExecute");
            if (mMuseumAdapter == null) {
                mMuseumAdapter = new MuseumAdapter(mMuseumList);
                mMuseumRecyclerView.setAdapter(mMuseumAdapter);
                mMuseumRecyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(MuseumListActivity.this,
                        R.anim.layout_anim_from_bottom));
                mMuseumRecyclerView.startLayoutAnimation();

                mRefreshLayout.setRefreshing(false);
            }else{
                mRefreshLayout.setRefreshing(false);
            }
        }
    }

    // 从云端下载博物馆列表
    private void downloadMuseumList(){
        if(!isNetWorkAvailableAndConnected()){
            mRefreshLayout.setRefreshing(false);
            getCacheMuseumList();
            return;
        }

        mRefreshLayout.setRefreshing(true); // 显示下拉刷新图标
        Log.d(TAG, "download museum list");
        BmobQuery query = new BmobQuery("museum");
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray jsonArray, BmobException e) {
                Log.d("kkk", "download");
                if(e == null){
                    try {
                        mACache.put(CACHE_ARRAY_KEY, jsonArray);
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
                        Collections.shuffle(mMuseumList); // 乱序
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

    /**
     * 获取缓存的博物馆列表
     */
    private void getCacheMuseumList() {
        try {
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
            Collections.shuffle(mMuseumList); // 乱序
            MuseumLibrary.get().setMuseumList(mMuseumList);
            if (mMuseumAdapter == null) {
                mMuseumAdapter = new MuseumAdapter(mMuseumList);
                mMuseumRecyclerView.setAdapter(mMuseumAdapter);
                mMuseumRecyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(MuseumListActivity.this,
                        R.anim.layout_anim_from_bottom));
                mMuseumRecyclerView.startLayoutAnimation();

                mRefreshLayout.setRefreshing(false);
            }
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
        Menu menu = mSearchToolbar.getMenu();
        MenuItem searchView = menu.findItem(R.id.action_search);
        searchView.expandActionView();
        searchView.getActionView().requestFocus();
        int cx = mToolbar.getWidth();
        int cy = mToolbar.getHeight();
        Animator anim = ViewAnimationUtils.createCircularReveal(mSearchToolbar, cx,
                cy / 2, 0, cx);
        anim.setDuration(400);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mSearchToolbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mToolbar.setVisibility(View.GONE);
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
    // 折叠搜索框
    private void collapseSearchView(){
        int cx = mToolbar.getWidth();
        int cy = mToolbar.getHeight();
        mToolbar.setVisibility(View.VISIBLE);
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
