package com.giz.customize;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.giz.bmob.Museum;
import com.giz.bmob.MuseumLibrary;
import com.giz.museum.MuseumTrackActivity;
import com.giz.museum.R;

public class CustomBottomSheet extends BottomSheetDialog implements View.OnClickListener {

    private ImageView mShareIcon;    // 分享
    private ImageView mCollectIcon;  // 收藏
    private ImageView mMarkIcon;     // 打卡
    private ImageView mLocateIcon;   // 定位
    private ImageView mRecordIcon;   // 记录
    private Museum mMuseum;

    public CustomBottomSheet(@NonNull Context context, String museumId) {
        super(context);
        setContentView(R.layout.bottom_sheet);
        mMuseum = MuseumLibrary.get().getMuseumById(museumId);

        initViews();
    }

    private void initViews() {
        mShareIcon = findViewById(R.id.action_share);
        mCollectIcon = findViewById(R.id.action_collect);
        mMarkIcon = findViewById(R.id.action_mark);
        mLocateIcon = findViewById(R.id.action_locate);
        mRecordIcon = findViewById(R.id.action_record);

        mShareIcon.setOnClickListener(this);
        mCollectIcon.setOnClickListener(this);
        mMarkIcon.setOnClickListener(this);
        mLocateIcon.setOnClickListener(this);
        mRecordIcon.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.action_share:
                break;
            case R.id.action_collect:
                break;
            case R.id.action_mark:
                break;
            case R.id.action_locate: {
                //Dialog转化到Activity
                Intent intent = MuseumTrackActivity.newIntent(getContext(), mMuseum.getMuseumId());
                getContext().startActivity(intent);
                break;
            }
            case R.id.action_record:
                break;
        }
    }
}
