package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.giz.bmob.Museum;
import com.giz.bmob.MuseumLibrary;

import java.util.ArrayList;
import java.util.List;

public class MarkActivity extends AppCompatActivity {

    private int museumNum;
    private Museum mMuseum;
    private static final String KEY_ID = "museumId";
    private String museumId;
    private int int_museumId;                   //Museum在MuseumList中的序号
    private ImageView curStamp;

    private int tempInt;
    private int tempPage;

    private Animation stampAni;
    private List<Drawable> mImageViews;

    private ViewPager mViewPager;
    List<List<Drawable>> mPages;

    public static Intent newIntent(Context packageContext, String Id) {
        Intent intent = new Intent(packageContext, MarkActivity.class);
        intent.putExtra(KEY_ID, Id);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark);
        museumId = getIntent().getStringExtra(KEY_ID);
        mMuseum = MuseumLibrary.get().getMuseumById(museumId);
        museumNum = MuseumLibrary.get().getMuseumList().size();

        //获取该博物馆的Logo位置
        for (int i = 0; i < museumNum; i++) {
            if (museumId.equals(MuseumLibrary.get().getMuseumList().get(i).getMuseumId())) {
                int_museumId = i;
                break;
            }
        }
        tempInt = int_museumId % 6 + 1;
        tempPage = int_museumId / 6;

        initView();
    }

    private void playAnim_before() {
        curStamp.setVisibility(ImageView.VISIBLE);
        stampAni = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.stamp_anim);
        curStamp.startAnimation(stampAni);
    }

    private void playAnim_after() {
        stampAni = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.stamp_anim);
        curStamp.startAnimation(stampAni);
//        curStamp.setVisibility(ImageView.VISIBLE);
    }

    private void initView() {
        mImageViews = new ArrayList<>();
        mViewPager = findViewById(R.id.pager_stamps);

        for(int i=0;i<museumNum;i++)
            mImageViews.add(MuseumLibrary.get().getMuseumList().get(i).getLogo());

        initPager();
    }

    private void initPager() {
        mPages = new ArrayList<>();

        int p = (int)(Math.ceil(museumNum/6.0));
        for(int i=0;i<p;i++) {
            List<Drawable> drawables = new ArrayList<>();
            for(int j = 0; j < 6; j++) {
                int idx = i * 6 + j;
                if(idx < museumNum) {
                    drawables.add(mImageViews.get(idx));
                }
            }
            mPages.add(drawables);
        }

        MarkAdapter adapter = new MarkAdapter();
        mViewPager.setAdapter(adapter);
    }

    private class MarkAdapter extends PagerAdapter{
        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = LayoutInflater.from(MarkActivity.this).inflate(R.layout.pager_item_mark, null);

            int[] ids = {R.id.logo1, R.id.logo2, R.id.logo3, R.id.logo4, R.id.logo5, R.id.logo6};
            int[] ids_stamp = {R.id.stamp1, R.id.stamp2, R.id.stamp3, R.id.stamp4, R.id.stamp5, R.id.stamp6};
            List<Drawable> drawables = mPages.get(position);

            for(int i = 0; i < 6; i++) {
                if(i < drawables.size()) {
                    ((ImageView)view.findViewById(ids[i])).setImageDrawable(mPages.get(position).get(i));
                    curStamp = ((ImageView)view.findViewById(ids_stamp[i]));
                    if(i+1 == tempInt && position == tempPage)
                        playAnim_before();
                }
                else {
                    ((ImageView)view.findViewById(ids[i])).setVisibility(ImageView.INVISIBLE);
                }
            }
            container.addView(view);

            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View)object);
        }

        //页数
        @Override
        public int getCount() {
            return mPages.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return (view == o);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityCompat.finishAfterTransition(this);
    }
}
