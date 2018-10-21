package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.giz.bmob.Museum;
import com.giz.bmob.MuseumLibrary;
import com.giz.bmob.MarkDB;
import com.giz.customize.CustomToast;

import java.util.ArrayList;
import java.util.List;

/*
    To_Do_List:
    1.点击Logo的Button效果
    2.左右滑动提示功能
    3.加载完成再进行打卡(否则会Logo不完整)
    4.个性化Toast
 */

public class MarkActivity extends AppCompatActivity {

    private MarkDB mMarkDB;
    private int museumNum;
    private Museum mMuseum;
    private static final String KEY_ID = "museumId";
    private String museumId;
    private int int_museumId;                   //Museum在MuseumList中的序号

    private ImageView curLogo;
    private ImageView curStamp;
    private int tempInt;
    private int tempPage;

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
        mMarkDB = MarkDB.get(this);
        mMuseum = MuseumLibrary.get().getMuseumById(museumId);
        museumNum = MuseumLibrary.get().getMuseumList().size();

        //获取该博物馆的Logo位置
        for (int i = 0; i < museumNum; i++) {
            if (museumId.equals(MuseumLibrary.get().getMuseumList().get(i).getMuseumId())) {
                int_museumId = i;
                break;
            }
        }
        tempInt = int_museumId % 6;
        tempPage = int_museumId / 6;

        initView();
    }

    private void playAnim_logoHover(ImageView img) {
        Animation hoverAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.logo_hover);
        img.startAnimation(hoverAnim);
    }

    private void playAnim_stampHover(ImageView img) {
        Animation hoverAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.stamp_hover);
        img.startAnimation(hoverAnim);
    }

    private void playAnim_before(ImageView curStamp) {
        curStamp.setVisibility(ImageView.VISIBLE);
        Animation stampAni = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.stamp_anim);
        curStamp.startAnimation(stampAni);
    }

    private void playAnim_after(final ImageView curStamp) {
        curStamp.setVisibility(ImageView.VISIBLE);
        Animation stampAni = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.stamp_anim_jump);
        curStamp.startAnimation(stampAni);
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

    private class MarkAdapter extends PagerAdapter {

        class stampListener implements View.OnClickListener {
            private ImageView curStamp;
            private int i;
            private int position;

            private stampListener(ImageView curStamp, int i, int position) {
                this.curStamp = curStamp;
                this.i = i;
                this.position = position;
            }

            @Override
            public void onClick(View view) {
                if(i == tempInt && position == tempPage && !mMarkDB.hasMarked(museumId)) {
                    playAnim_before(curStamp);
                    mMarkDB.addMarkMuseum(mMuseum);
                }
                else if(mMarkDB.hasMarked(MuseumLibrary.get().getMuseumList().get(6*position+i).getMuseumId())) {
//                    Toast t = Toast.makeText(getApplicationContext(), "已打卡", Toast.LENGTH_SHORT);
//                    t.setGravity(Gravity.CENTER, 0, 0);
//                    t.show();
                    CustomToast t = CustomToast.make(getApplicationContext(), "已打卡");
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }
                else if(!mMarkDB.hasMarked(MuseumLibrary.get().getMuseumList().get(6*position+i).getMuseumId())) {
//                    Toast t = Toast.makeText(getApplicationContext(), "请到对应博物馆页面进行打卡", Toast.LENGTH_SHORT);
//                    t.setGravity(Gravity.CENTER, 0, 0);
//                    t.show();
                    CustomToast t = CustomToast.make(getApplicationContext(), "请到对应博物馆页面进行打卡");
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }
            }
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = LayoutInflater.from(MarkActivity.this).inflate(R.layout.pager_item_mark, null);

            int[] ids = {R.id.logo1, R.id.logo2, R.id.logo3, R.id.logo4, R.id.logo5, R.id.logo6};
            int[] ids_stamp = {R.id.stamp1, R.id.stamp2, R.id.stamp3, R.id.stamp4, R.id.stamp5, R.id.stamp6};
            List<Drawable> drawables = mPages.get(position);

            for(int i = 0; i < 6; i++) {
                if(i < drawables.size()) {
                    ImageView tempLogo = ((ImageView)view.findViewById(ids[i]));
                    tempLogo.setImageDrawable(mPages.get(position).get(i));
                    ImageView tempStamp = ((ImageView)view.findViewById(ids_stamp[i]));
                    //加动画特效
                    if(mMarkDB.hasMarked(MuseumLibrary.get().getMuseumList().get(6*position+i).getMuseumId())
                            && int_museumId != 6*position+i)
                        playAnim_after(tempStamp);
                    else if(mMarkDB.hasMarked(MuseumLibrary.get().getMuseumList().get(6*position+i).getMuseumId())
                            && int_museumId == 6*position+i) {
                        curLogo = tempLogo;
                        curStamp = tempStamp;
                        playAnim_stampHover(curStamp);
                        playAnim_logoHover(curLogo);
                    }
                    else if(!mMarkDB.hasMarked(MuseumLibrary.get().getMuseumList().get(6*position+i).getMuseumId())
                            && int_museumId == 6*position+i) {
                        curLogo = tempLogo;
                        playAnim_logoHover(curLogo);
                    }

                    tempLogo.setOnClickListener(new stampListener(tempStamp, i, position));
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
