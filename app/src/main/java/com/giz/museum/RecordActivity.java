package com.giz.museum;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.giz.bmob.Museum;
import com.giz.bmob.MuseumRecord;
import com.giz.bmob.RecordDB;
import com.giz.customize.CardSlideTransformer;
import com.giz.customize.CustomToast;
import com.giz.utils.BitmapUtils;

import java.io.File;
import java.util.List;

public class RecordActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private RecordAdapter mRecordAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        mViewPager = findViewById(R.id.record_pager);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setPageTransformer(true, new CardSlideTransformer());

        findViewById(R.id.record_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        updateView();
    }

    private class RecordAdapter extends PagerAdapter{

        private List<MuseumRecord> mMuseumRecords;

        private RecordAdapter(List<MuseumRecord> records){
            mMuseumRecords = records;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            final View view = LayoutInflater.from(RecordActivity.this).inflate(R.layout.pager_item_record, null);

            final MuseumRecord record = mMuseumRecords.get(position);
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

            (view.findViewById(R.id.record_share_icon)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    view.setDrawingCacheEnabled(true);
                    view.buildDrawingCache();
                    Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
                    view.setDrawingCacheEnabled(false);
                    Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null, null));

                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.setType("image/*");
                    startActivity(Intent.createChooser(shareIntent, "分享记录到"));
                }
            });

            (view.findViewById(R.id.record_delete_icon)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(RecordActivity.this)
                            .setTitle("删除该记录吗")
                            .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    RecordDB.get(RecordActivity.this).removeMuseumRecord(record.getRecordDate());
                                    File file = new File(record.getPicturePath());
                                    if(file.delete()){
                                        CustomToast.make(RecordActivity.this, "删除成功").show();
                                    }else{
                                        CustomToast.make(RecordActivity.this, "图片未删除").show();
                                    }
                                    updateView();
                                }
                            })
                            .setNegativeButton("取消", null).show();
                }
            });

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            ((ImageView)((View)object).findViewById(R.id.record_switch)).setImageResource(R.drawable.av_up_to_down);
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

    private void updateView() {
        if(mRecordAdapter == null){
            List<MuseumRecord> records = RecordDB.get(this).getMuseumRecords();
            if(records.size() > 0){
                findViewById(R.id.tip_no_record).setVisibility(View.GONE);
            }
            mRecordAdapter = new RecordAdapter(records);
            mViewPager.setAdapter(mRecordAdapter);
        }else{
            List<MuseumRecord> records = RecordDB.get(this).getMuseumRecords();
            if(records == null || records.size() == 0)
                findViewById(R.id.tip_no_record).setVisibility(View.VISIBLE);
            else
                findViewById(R.id.tip_no_record).setVisibility(View.GONE);
            mRecordAdapter = new RecordAdapter(records);
            mViewPager.setAdapter(mRecordAdapter);
        }
    }

//    private SpannableString addIntentToText(String text){
//        SpannableString result = new SpannableString(text);
//        result.setSpan(new LeadingMarginSpan.Standard(10, 10), 0, text.length(), 0);
//        return result;
//    }
}
