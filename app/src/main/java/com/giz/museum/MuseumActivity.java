package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.giz.database.CollectionDB;
import com.giz.database.Museum;
import com.giz.database.MuseumLibrary;
import com.giz.database.RecordDB;
import com.giz.customize.ArcMenu;
import com.giz.customize.CustomToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;

public class MuseumActivity extends AppCompatActivity {

    private static final String TAG = "MuseumActivity";
    private static final String EXTRA_MUSEUM = "museum_intent";

    private Museum mMuseum;

    // 管理的Fragment
    private InfoFragment mInfoFragment;
    private PanoramaFragment mPanoramaFragment;
    private AnsFragment mAnsFragment;
    private TreasureFragment mTreasureFragment;

    private FloatingActionButton mStarImgView;
    private FloatingActionButton mArcMainBtn;
    private ArcMenu mArcMenu;

    private boolean mHasStarred;

    public static Intent newIntent(Context context, String museumId){
        Intent intent = new Intent(context, MuseumActivity.class);
        intent.putExtra(EXTRA_MUSEUM, museumId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_museum);
        mArcMainBtn = findViewById(R.id.arc_main);
        mArcMenu = findViewById(R.id.action_arcmenu);
        // 禁止菜单
        mArcMainBtn.setEnabled(false);

        // 获得Museum信息
        String museumId = getIntent().getStringExtra(EXTRA_MUSEUM);
        mMuseum = MuseumLibrary.get().getMuseumById(museumId);
        if(mMuseum == null){
            // 直接进入该页面
            mMuseum = new Museum(museumId);
            BmobQuery query = new BmobQuery("museum");
            query.addWhereEqualTo("objectId", museumId);
            query.findObjectsByTable(new QueryListener<JSONArray>() {
                @Override
                public void done(JSONArray array, BmobException e) {
                    try {
                        JSONObject object = array.getJSONObject(0);
                        Log.d(TAG, "get the museum info" + object.toString(4));
                        mMuseum.setName(object.getString("name"));
                        mMuseum.setCatalog(getCatalog(object.getJSONArray("catalog")));
                        mMuseum.setLogoUrl(object.getJSONObject("logo").getString("url"));
                        mMuseum.setLocation(new double[]{object.getJSONArray("location").getDouble(0),
                                object.getJSONArray("location").getDouble(1)});
                        MuseumLibrary.get().addMuseum(mMuseum);

                        // 初始化Fragment
                        initFragments();
                        // 添加信息Fragment
                        FragmentManager fm = getSupportFragmentManager();
                        fm.beginTransaction().add(R.id.fragment_container, mInfoFragment).commit();
                        setFragment(1);

                        mArcMainBtn.setEnabled(true);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
            });
        }else {
            mArcMainBtn.setEnabled(true);
            // 初始化Fragment
            initFragments();
            // 添加信息Fragment
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().add(R.id.fragment_container, mInfoFragment).commit();
            setFragment(1);
        }

        // 初始化控件
        initViews();
    }

    /**
     * 初始化Fragment
     */
    private void initFragments() {
        mInfoFragment = InfoFragment.newInstance(mMuseum.getMuseumId(), mArcMainBtn);
    }

    /**
     * 初始化布局
     */
    private void initViews() {
        // 底部导航条
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.nav_info:
                        // 博物馆信息
                        setFragment(1);
                        return true;
                    case R.id.nav_an:
                        // 博物馆近期动态（活动和新闻）
                        setFragment(2);
                        return true;
                    case R.id.nav_collection:
                        // 馆藏展示
                        setFragment(3);
                        return true;
                    case R.id.nav_nav:
                        // 博物馆导览（楼层导引、店铺导引）
                        setFragment(4);
                        return true;
                    case R.id.nav_pano:
                        setFragment(5);
                        // 全景图
                        return true;
                }
                return false;
            }
        });
        bottomNavigationView.setOnNavigationItemReselectedListener(null);

        // 右下角悬浮菜单
        final ArcMenu menu = findViewById(R.id.action_arcmenu);
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
                            mStarImgView.setImageResource(R.drawable.ic_star_filled_white);
                        }else{
                            CollectionDB.get(MuseumActivity.this).addStarMuseum(mMuseum);
                            mStarImgView.setImageResource(R.drawable.arc_ic_star_yellow);
                        }
                        mHasStarred = !mHasStarred;
                        break;
                    case 3: // 记录
                        if(!RecordDB.get(MuseumActivity.this).isMuseumRecordFull(mMuseum.getMuseumId())){
                            Intent intent1 = WriteRecordActivity.newIntent(MuseumActivity.this, mMuseum.getMuseumId());
                            startActivity(intent1);
                        }else{
                            CustomToast.make(MuseumActivity.this, "记录已满5条").show();
                        }
                        break;
                    case 4: // 打卡
                        Intent markIntent = MarkActivity.newIntent(MuseumActivity.this, mMuseum.getMuseumId());
                        startActivity(markIntent);
                        break;
                }
            }
        });

        // 是否收藏过了
        mStarImgView = findViewById(R.id.arc_img_star);
        mHasStarred = CollectionDB.get(MuseumActivity.this).hasStarred(mMuseum.getMuseumId());
        if(mHasStarred){
            mStarImgView.setImageResource(R.drawable.arc_ic_star_yellow);
        }
    }

//    private void showHeaders(){
//        Log.d("MuseumActivity", "ShowHeaders");
//        // 允许滑动
//        mScrollView.setNestedScrollingEnabled(true);
//        // 向下滑出以显示
//        CoordinatorLayout.LayoutParams params = ((CoordinatorLayout.LayoutParams)mAppBarLayout.getLayoutParams());
//        CoordinatorLayout.Behavior behavior = params.getBehavior();
//        behavior.onNestedPreScroll(mCoordinatorLayout, mAppBarLayout, mScrollView, 0,
//                0, new int[]{0,0}, 0);
//    }

    /**
     * 显示当前的Fragment
     * 1、信息  2、动态  3、馆藏  4、导览  5、全景
     * @param i Fragment的编号，从左到右，从1开始
     */
    private void setFragment(int i) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragments(transaction);
        switch (i){
            case 1:
                showArcMenu();
                if(mInfoFragment == null){
                    mInfoFragment = InfoFragment.newInstance(mMuseum.getMuseumId(), mArcMainBtn);
                    transaction.add(R.id.fragment_container, mInfoFragment);
                }else{
                    transaction.show(mInfoFragment);
                }
                break;
            case 2:
                hideArcMenu();
                if(mAnsFragment == null){
                    mAnsFragment = AnsFragment.newInstance(mMuseum.getMuseumId());
                    transaction.add(R.id.fragment_container, mAnsFragment);
                }else{
                    transaction.show(mAnsFragment);
                }
                break;
            case 3:
                hideArcMenu();
                if(mTreasureFragment == null){
                    mTreasureFragment = TreasureFragment.newInstance(mMuseum.getMuseumId());
                    transaction.add(R.id.fragment_container, mTreasureFragment);
                }else{
                    transaction.show(mTreasureFragment);
                }
                break;
            case 4:
                hideArcMenu();
                break;
            case 5:
                hideArcMenu();
                if(mPanoramaFragment == null){
                    mPanoramaFragment = PanoramaFragment.newInstance(mMuseum.getMuseumId());
                    transaction.add(R.id.fragment_container, mPanoramaFragment);
                }else{
                    transaction.show(mPanoramaFragment);
                }
                break;
        }
        transaction.commit();
    }

    public void showArcMenu() {
        mArcMainBtn.show();
        mArcMenu.setVisibility(View.VISIBLE);
    }

    public void hideArcMenu() {
        if(mArcMenu.isOpen())
            mArcMenu.fold();
        mArcMainBtn.hide();
        mArcMenu.setVisibility(View.GONE);
    }

    public void foldArcMenu(){
        if(mArcMenu.isOpen()){
            mArcMenu.fold();
        }
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
        if(mAnsFragment != null){
            transaction.hide(mAnsFragment);
        }
        if(mTreasureFragment != null){
            transaction.hide(mTreasureFragment);
        }
    }

    @Override
    public void onBackPressed() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(mInfoFragment != null){
            transaction.remove(mInfoFragment);
        }
        if(mAnsFragment != null){
            transaction.remove(mAnsFragment);
        }
        if(mPanoramaFragment != null){
            transaction.remove(mPanoramaFragment);
        }
        if(mTreasureFragment != null){
            transaction.remove(mTreasureFragment);
        }
        transaction.commit();
        super.onBackPressed();
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

    /**
     * 分享事件
     */
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
    private boolean isNetWorkAvailableAndConnected(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected());
    }

    private List<String> getCatalog(JSONArray array) throws JSONException {
        List<String> list = new ArrayList<>();
        for(int j = 0; j < array.length(); j++){
            list.add(array.getString(j));
        }
        return list;
    }
}
