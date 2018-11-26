package com.giz.museum;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v8.renderscript.RenderScript;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.giz.utils.FastBlur;

public class IndexActivity extends AppCompatActivity {

    private static final String TAG = "IndexActivity";

    private RecyclerView mRecyclerView;
    private IndexAdapter mIndexAdapter;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index_drawer);

        mRecyclerView = findViewById(R.id.activity_index_rv);
        mDrawerLayout = findViewById(R.id.index_drawer_layout);

        initDrawerMenu();
        updateRecyclerView();
        initEvents();
    }

    /**
     * 初始化抽屉菜单
     */
    private void initDrawerMenu(){
        // 遮罩颜色为透明
        mDrawerLayout.setScrimColor(0x00FFFFFF);
        // 左侧抽屉菜单监听事件
        IndexDrawerFragment indexDrawerFragment = IndexDrawerFragment.newInstance(new IndexDrawerFragment.OnItemSelectedListener() {
            @Override
            public void onItemSelected(View view, int pos) {
                mDrawerLayout.closeDrawer(Gravity.START);
                switch (pos){
                    case 0:
                        // 所有博物馆
                        Intent intent = new Intent(IndexActivity.this, MuseumListActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        // 我的收藏
                        break;
                    case 2:
                        // 我的记录
                        break;
                    case 3:
                        // 关于Museum
                        break;
                }
            }
        });
        // 添加左侧抽屉菜单
        getSupportFragmentManager().beginTransaction().add(R.id.left_menu_container,
                indexDrawerFragment).commit();
    }

    /**
     * 初始化事件
     */
    private void initEvents(){
        Toolbar toolbar = findViewById(R.id.activity_index_toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                toolbar, R.string.drawer_open_desc, R.string.drawer_close_desc){

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
            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
                super.onDrawerOpened(view);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    View mContent = mDrawerLayout.getChildAt(0);
                    mContent.setDrawingCacheEnabled(true);
                    mContent.buildDrawingCache();
                    Bitmap captureOfContent = Bitmap.createBitmap(mContent.getDrawingCache());

                    Bitmap blur = FastBlur.gaussianBlur(RenderScript.create(IndexActivity.this), 16, captureOfContent);
                    mContent.setForeground(new BitmapDrawable(blur));
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
                super.onDrawerClosed(view);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    mDrawerLayout.getChildAt(0).setForeground(null);
            }

        };
        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
//        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
//            @Override
//            public void onDrawerSlide(@NonNull View view, float v) {
//                View mainContentView = mDrawerLayout.getChildAt(0);
//
//                // 抽屉布局固定位置，透明度从0.6 - 1
//                view.setAlpha(0.6f + 0.4f * v);
//                view.setTranslationX(view.getMeasuredWidth() * (1 - v));
//                //
//                float contentScaleFactor = 0.8f + 0.2f * (1 - v); // 1 - 0.8
//                mainContentView.invalidate();
//                mainContentView.setPivotX(0);
//                mainContentView.setPivotY(mainContentView.getMeasuredHeight() / 2);
//                mainContentView.setScaleX(contentScaleFactor);
//                mainContentView.setScaleY(contentScaleFactor);
//                mainContentView.setTranslationX(view.getMeasuredWidth() * v * 0.8f);
//            }
//
//            @Override
//            public void onDrawerOpened(@NonNull View view) {
//                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//                    View mContent = mDrawerLayout.getChildAt(0);
//                    mContent.setDrawingCacheEnabled(true);
//                    mContent.buildDrawingCache();
//                    Bitmap captureOfContent = Bitmap.createBitmap(mContent.getDrawingCache());
//
//                    Bitmap blur = FastBlur.gaussianBlur(RenderScript.create(IndexActivity.this), 16, captureOfContent);
//                    mContent.setForeground(new BitmapDrawable(blur));
//                }
//            }
//
//            @Override
//            public void onDrawerClosed(@NonNull View view) {
//                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//                    mDrawerLayout.getChildAt(0).setForeground(null);
//            }
//
//            @Override
//            public void onDrawerStateChanged(int i) {
//
//            }
//        });
    }

    /**
     * 更新列表视图
     */
    private void updateRecyclerView(){
        if(mIndexAdapter == null){
            mIndexAdapter = new IndexAdapter();
            mRecyclerView.setAdapter(mIndexAdapter);
        }else{
            mRecyclerView.setAdapter(mIndexAdapter);
        }
    }

    private class IndexHolder extends RecyclerView.ViewHolder{

        private IndexHolder(@NonNull View view) {
            super(view);
        }
    }

    private class IndexAdapter extends RecyclerView.Adapter<IndexHolder>{

        @NonNull
        @Override
        public IndexHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(IndexActivity.this).inflate(R.layout.card_item_index, viewGroup, false);
            return new IndexHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull IndexHolder indexHolder, int i) {

        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    @Override
    public void onBackPressed() {
        // 回退到桌面而不退出应用
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}
