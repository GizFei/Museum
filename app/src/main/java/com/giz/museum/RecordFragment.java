package com.giz.museum;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.giz.database.MuseumRecord;
import com.giz.database.RecordDB;
import com.giz.customize.CardSlideTransformer;
import com.giz.customize.CustomToast;
import com.giz.utils.BitmapUtils;
import com.giz.utils.TestFragment;

import java.io.File;
import java.util.List;

public class RecordFragment extends TestFragment {

    private static final String TAG = "RecordFragment";

    private ViewPager mViewPager;
    private RecordAdapter mRecordAdapter;
    private TextView mNoRecordTv;

    private DrawerActivity mActivity;

    public static RecordFragment newInstance() {
        Bundle args = new Bundle();

        RecordFragment fragment = new RecordFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (DrawerActivity)context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        mViewPager = view.findViewById(R.id.record_pager);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setPageTransformer(true, new CardSlideTransformer());
        mNoRecordTv = view.findViewById(R.id.tip_no_record);
        // 打开抽屉菜单
        view.findViewById(R.id.record_open_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.openDrawerMenu();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateView();
    }

    private class RecordAdapter extends PagerAdapter {

        private List<MuseumRecord> mMuseumRecords;

        private RecordAdapter(List<MuseumRecord> records){
            mMuseumRecords = records;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            final View view = LayoutInflater.from(mActivity).inflate(R.layout.pager_item_record, null);

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
                                        mActivity, R.drawable.av_up_to_down);
                                contentSwitch.setImageDrawable(drawableCompat);
                                ((Animatable)contentSwitch.getDrawable()).start();
                                ValueAnimator animator = ObjectAnimator.ofInt(content, "height", 0, contentHeight);
                                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                                animator.setDuration(400);
                                animator.start();
                            }else{
                                AnimatedVectorDrawableCompat drawableCompat = AnimatedVectorDrawableCompat.create(
                                        mActivity, R.drawable.av_down_to_up);
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
                    view.findViewById(R.id.record_share_icon).setVisibility(View.GONE);
                    view.findViewById(R.id.record_delete_icon).setVisibility(View.GONE);
                    view.setDrawingCacheEnabled(true);
                    view.buildDrawingCache();
                    Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
                    view.setDrawingCacheEnabled(false);
                    view.findViewById(R.id.record_share_icon).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.record_delete_icon).setVisibility(View.VISIBLE);
                    Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(mActivity.getContentResolver(), bitmap, null, null));

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
                    new AlertDialog.Builder(mActivity)
                            .setTitle("删除该记录吗")
                            .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    RecordDB.get(mActivity).removeMuseumRecord(record.getRecordDate());
                                    File file = new File(record.getPicturePath());
                                    if(file.delete()){
                                        CustomToast.make(mActivity, "删除成功").show();
                                    }else{
                                        CustomToast.make(mActivity, "图片未删除").show();
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
            List<MuseumRecord> records = RecordDB.get(mActivity).getMuseumRecords();
            if(records.size() > 0){
                mNoRecordTv.setVisibility(View.GONE);
            }
            mRecordAdapter = new RecordAdapter(records);
            mViewPager.setAdapter(mRecordAdapter);
        }else{
            List<MuseumRecord> records = RecordDB.get(mActivity).getMuseumRecords();
            if(records == null || records.size() == 0)
                mNoRecordTv.setVisibility(View.VISIBLE);
            else
                mNoRecordTv.setVisibility(View.GONE);
            mRecordAdapter = new RecordAdapter(records);
            mViewPager.setAdapter(mRecordAdapter);
        }
    }

}
