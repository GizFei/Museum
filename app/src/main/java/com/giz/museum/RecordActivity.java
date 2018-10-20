package com.giz.museum;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.giz.bmob.MuseumRecord;
import com.giz.bmob.RecordDB;
import com.giz.customize.CardSlideTransformer;
import com.giz.utils.BitmapUtils;

import java.util.List;

public class RecordActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private List<MuseumRecord> mMuseumRecords;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        mViewPager = findViewById(R.id.record_pager);
        mMuseumRecords = RecordDB.get(this).getMuseumRecords();
        PagerAdapter adapter = new RecordAdapter();
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setPageTransformer(true, new CardSlideTransformer());
    }

    private class RecordAdapter extends PagerAdapter{

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = LayoutInflater.from(RecordActivity.this).inflate(R.layout.pager_item_record, null);

            MuseumRecord record = mMuseumRecords.get(position);
            final TextView content = view.findViewById(R.id.record_content);
            final ImageView contentSwitch = view.findViewById(R.id.record_switch);
            ((ImageView)view.findViewById(R.id.record_picture)).setImageBitmap(BitmapUtils.getBitmapFromPath(record.getPicturePath()));
            ((TextView)view.findViewById(R.id.record_location)).setText(record.getName());
            ((TextView)view.findViewById(R.id.record_date)).setText(record.getRecordDate());
            content.setText(record.getContent());

            ViewTreeObserver contentObserver = content.getViewTreeObserver();
            contentObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    content.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    final int contentHeight = content.getMeasuredHeight();
                    Log.d("RecordActivity", String.valueOf(contentHeight));
                    content.setHeight(0);
//                    contentSwitch.setImageResource(R.drawable.av_up_to_down);

                    contentSwitch.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(content.getHeight() == 0){
                                AnimatedVectorDrawableCompat drawableCompat = AnimatedVectorDrawableCompat.create(
                                        RecordActivity.this, R.drawable.av_up_to_down);
                                contentSwitch.setImageDrawable(drawableCompat);
                                ((Animatable)contentSwitch.getDrawable()).start();
                                ValueAnimator animator = ObjectAnimator.ofInt(content, "height", 0, contentHeight);
                                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                                animator.setDuration(400);
                                animator.start();
                            }else{
                                AnimatedVectorDrawableCompat drawableCompat = AnimatedVectorDrawableCompat.create(
                                        RecordActivity.this, R.drawable.av_down_to_up);
                                contentSwitch.setImageDrawable(drawableCompat);
                                ((Animatable)contentSwitch.getDrawable()).start();
                                ValueAnimator animator = ObjectAnimator.ofInt(content, "height", contentHeight, 0);
                                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                                animator.setDuration(400);
                                animator.start();
                            }
                        }
                    });
                }
            });

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View)object);
        }

        @Override
        public int getCount() {
            return mMuseumRecords.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return (view == o);
        }
    }

//    private SpannableString addIntentToText(String text){
//        SpannableString result = new SpannableString(text);
//        result.setSpan(new LeadingMarginSpan.Standard(10, 10), 0, text.length(), 0);
//        return result;
//    }
}
