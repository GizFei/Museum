package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.giz.bmob.CollectionDB;
import com.giz.bmob.Museum;
import com.giz.bmob.MuseumLibrary;
import com.giz.bmob.RecordDB;
import com.giz.customize.ArcMenu;
import com.giz.customize.CustomToast;
import com.giz.utils.MuseumPicturePagerAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
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
    private ImageView mStarImgView;

    private ViewPager mViewPager;
    private MuseumPicturePagerAdapter mPagerAdapter;

    private boolean mHasStarred;

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

        mStarImgView = findViewById(R.id.arc_img_star);
        mHasStarred = CollectionDB.get(this).hasStarred(mMuseum.getMuseumId());
        if(mHasStarred){
            mStarImgView.setImageResource(R.drawable.ic_arc_starred);
        }

        mProgressBar = findViewById(R.id.progressBar);

        mCoordinatorLayout = findViewById(R.id.coordinator);
        mScrollView = findViewById(R.id.scrollView);

        mViewPager = findViewById(R.id.picture_vp);
        setUpPager();

        CollapsingToolbarLayout ctl = findViewById(R.id.ctl);
        ctl.setTitle(mMuseum.getName());
        ctl.setCollapsedTitleTextColor(getResources().getColor(R.color.white));
        ctl.setExpandedTitleColor(getResources().getColor(R.color.transparent));
        ctl.setStatusBarScrimResource(R.color.colorPrimaryDark);

        mDotsLinearLayout = findViewById(R.id.dots_ll);

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

        final ArcMenu menu = findViewById(R.id.action_arcmenu);
        final ImageView arcMain = findViewById(R.id.arc_main);
        menu.setMenuItemClickListener(new ArcMenu.OnMenuItemClickListener() {
            @Override
            public void onClick(View view, int pos) {
                switch(pos){
                    case 0: // 分享
                        share();
                        break;
                    case 1: // 导航
                        Intent locateIntent = MuseumTrackActivity.newIntent(MuseumActivity.this, mMuseum.getMuseumId());
                        startActivity(locateIntent);
                        break;
                    case 2: // 收藏
                        if(mHasStarred){
                            CollectionDB.get(MuseumActivity.this).removeStarMuseum(mMuseum.getMuseumId());
                            mStarImgView.setImageResource(R.drawable.ic_arc_star);
                        }else{
                            CollectionDB.get(MuseumActivity.this).addStarMuseum(mMuseum);
                            mStarImgView.setImageResource(R.drawable.ic_arc_starred);
                        }
                        mHasStarred = !mHasStarred;
                        break;
                    case 3: // 记录
                        if(!RecordDB.get(MuseumActivity.this).isMuseumRecordFull(mMuseum.getMuseumId())){
                            Intent intent1 = WriteRecordActivity.newIntent(MuseumActivity.this, mMuseum.getMuseumId());
                            startActivity(intent1);
                        }else{
                            CustomToast.make(MuseumActivity.this, "记录已满5条").show();
//                            Toast.makeText(MuseumActivity.this, "记录已满5条", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 4: // 打卡
                        Intent markIntent = MarkActivity.newIntent(MuseumActivity.this, mMuseum.getMuseumId());
                        startActivity(markIntent);
                        break;
                }
            }
        });

        mAppBarLayout = findViewById(R.id.myAppBar);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
                if(i != 0 && menu.isOpen())
                    menu.fold();
                float factor = 1.0f - (-(float)i) / appBarLayout.getTotalScrollRange();
                menu.setAlpha(factor);
                arcMain.setScaleX(factor);
                arcMain.setScaleY(factor);
            }
        });

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
        CoordinatorLayout.LayoutParams params = ((CoordinatorLayout.LayoutParams)mAppBarLayout.getLayoutParams());
        CoordinatorLayout.Behavior behavior = params.getBehavior();
        behavior.onNestedPreScroll(mCoordinatorLayout, mAppBarLayout, mScrollView, 0,
                0, new int[]{0,0}, 0);
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
                        new PagerPicTask().execute(urls);
                    }catch (Exception ee){
                        mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(MuseumActivity.this, "未找到图片数据", Toast.LENGTH_SHORT).show();
                        ee.printStackTrace();
                    }
                }
            }
        });
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
            mProgressBar.setVisibility(View.GONE);
            if(drawables == null){
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(MuseumActivity.this, "未找到图片数据", Toast.LENGTH_SHORT).show();
            }else{
                mPagerAdapter = new MuseumPicturePagerAdapter(MuseumActivity.this, drawables);
                mViewPager.setAdapter(mPagerAdapter);

                for(int i = mPagerAdapter.getCount(); i < 5; i++){
                    mDotsLinearLayout.getChildAt(i).setVisibility(View.GONE);
                }
            }
        }
    }

    //Textview转化为Bitmap
    private Bitmap textViewToBitmap(TextView t) {
        t.setDrawingCacheEnabled(true);
        t.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        t.layout(0, 0, t.getMeasuredWidth(), t.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(t.getDrawingCache());
        //千万别忘最后一步
        t.destroyDrawingCache();
        return bitmap;
    }

    private void share() {
        //只是用到了Android自带的分享，如果有更高需求可以使用shareSDK包
        TextView textView = new TextView(getApplicationContext());
        textView.setBackgroundResource(R.color.white);
        textView.setMaxWidth(2048);
        textView.setTextSize(18);
        textView.setPadding(20, 20,20,20);
        textView.setText(mMuseum.getName()+"\n"+"地址："+mMuseum.getAddress()+"\n"
                         +"门票："+mMuseum.getTicket()+"\n"+"开放时间："+mMuseum.getOpenTime()+"\n"
                         +"\n"+"\t\t\t\t"+mMuseum.getIntro());

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        ArrayList<Uri> imageUris = new ArrayList<>();
        Drawable img = MuseumLibrary.get().getMuseumById(mMuseum.getMuseumId()).getCover();
        //强制转换Drawable/Textview为Bitmap
        //Bitmap bitmap1 = drawableToBitmap(img);
        Bitmap bitmap1 = ((BitmapDrawable)img).getBitmap();
        //Bitmap bitmap1 = drawableToBitmap(img);
        Bitmap bitmap2 = textViewToBitmap(textView);
        //Bitmap转化为Uri
        Uri uri1 = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap1, null,null));
        Uri uri2 = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap2, null,null));
        imageUris.add(uri1);
        imageUris.add(uri2);

        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

    /*
    //Drawable转化为Bitmap
    public static Bitmap drawableToBitmap(Drawable drawable) {
        //取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        //取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        //建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        //建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        //把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }
    //Bitmap转化为byte[]
    public byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
    //Text文本转化为Bitmap
    //需要规定所有的简介不能很长
    public Bitmap StringtoBitmap(String s) {
        Bitmap bmp = Bitmap.createBitmap(256, 300, Bitmap.Config.ARGB_4444);
        Canvas canvasTemp = new Canvas(bmp);
        canvasTemp.drawColor(Color.WHITE);
        TextPaint p = new TextPaint();
        p.setAntiAlias(true);
        p.setTextSize(16.0F);
        //第三个参数自动换行
        StaticLayout staticLayout = new StaticLayout(s, p, bmp.getWidth()-8,
                Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        canvasTemp.translate(6, 20);
        staticLayout.draw(canvasTemp);
        return bmp;
    }
    */
}
