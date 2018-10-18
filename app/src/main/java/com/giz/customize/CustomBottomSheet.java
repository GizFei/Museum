package com.giz.customize;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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

    private Bitmap bitmap;

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
            case R.id.action_share: {
                //只是用到了Android自带的分享，如果有更高需求可以使用shareSDK包
                /*
                //分享文字
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                String name = MuseumLibrary.get().getMuseumById(mMuseum.getMuseumId()).getName();
                String address = MuseumLibrary.get().getMuseumById(mMuseum.getMuseumId()).getAddress();
                String ticket = MuseumLibrary.get().getMuseumById(mMuseum.getMuseumId()).getTicket();
                String opentime = MuseumLibrary.get().getMuseumById(mMuseum.getMuseumId()).getOpenTime();
                String intro  = MuseumLibrary.get().getMuseumById(mMuseum.getMuseumId()).getIntro();
                sendIntent.putExtra(Intent.EXTRA_TEXT,name+"\n"+address+"\n"+ticket+"\n"+opentime+"\n"+intro);
                sendIntent.setType("text/plain");
                getContext().startActivity(Intent.createChooser(sendIntent, "分享到"));
                */
                //分享图像
                /*
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);
                shareIntent.setType("image/jpeg");
                getContext().startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
                */
                break;
            }
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
