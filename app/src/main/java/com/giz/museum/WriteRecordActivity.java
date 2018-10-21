package com.giz.museum;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.giz.bmob.Museum;
import com.giz.bmob.MuseumLibrary;
import com.giz.bmob.MuseumRecord;
import com.giz.bmob.RecordDB;
import com.giz.customize.CustomToast;
import com.giz.utils.BitmapUtils;

import java.io.File;
import java.util.Date;
import java.util.List;

public class WriteRecordActivity extends AppCompatActivity {

    private static final int REQUEST_PHOTO = 0;
    private static final int REQUEST_PERMISSION_CAMERA = 1;
    private static final String EXTRA_ID = "museum_id";
    private static final String FILE_PROVIDER_AUTH = "com.giz.museum.fileprovider";

    private File mPhotoFile;
    private String mMuseumId;

    private ImageView mPhoto;
    private TextView mContent;
    private ImageButton mTakePhoto;
    private Button mSaveBtn;
    private TextView mTitle;
    private ImageView mDeletePhoto;

    public static Intent newIntent(Context context, String museumId){
        Intent intent = new Intent(context, WriteRecordActivity.class);
        intent.putExtra(EXTRA_ID, museumId);

        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_record);

        mPhoto = findViewById(R.id.wr_picture);
        mContent = findViewById(R.id.wr_content);
        mTakePhoto = findViewById(R.id.wr_camera);
        mSaveBtn = findViewById(R.id.wr_save);
        mTitle = findViewById(R.id.wr_title);
        mDeletePhoto = findViewById(R.id.wr_delete_photo);

        mTakePhoto.setEnabled(false);
        initEvents();

        mMuseumId = getIntent().getStringExtra(EXTRA_ID);
        requestCamera();
    }

    private void initEvents() {
        mTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                mPhotoFile = getFile();
                Uri uri = FileProvider.getUriForFile(WriteRecordActivity.this, FILE_PROVIDER_AUTH, mPhotoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                List<ResolveInfo> cameraActivities = getPackageManager().queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
                for(ResolveInfo activity: cameraActivities){
                    grantUriPermission(activity.activityInfo.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                startActivityForResult(intent, REQUEST_PHOTO);
            }
        });

        mTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(WriteRecordActivity.this)
                        .setTitle("不保存该记录吗？")
                        .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                back(true);
                            }
                        }).setNegativeButton("取消", null)
                        .show();
            }
        });

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPhoto.getDrawable() == null){
                    new AlertDialog.Builder(WriteRecordActivity.this)
                            .setTitle("还没拍照片呢").setPositiveButton("去拍", null)
                            .show();
                    return;
                }
                Log.d("MSAVE", mContent.getText().toString());
                if(mContent.getText().toString().equals("") || mContent.getText() == null){
                    new AlertDialog.Builder(WriteRecordActivity.this)
                            .setTitle("还没写点东西呢").setPositiveButton("去写", null)
                            .show();
                    return;
                }
                Museum museum = MuseumLibrary.get().getMuseumById(mMuseumId);
                MuseumRecord record = new MuseumRecord();
                record.setMuseumId(mMuseumId);
                record.setName(museum.getName());
                record.setContent(mContent.getText().toString());
                record.setPicturePath(mPhotoFile.getPath());
                record.setRecordDate(formatDateForRecord(new Date()));
                RecordDB.get(WriteRecordActivity.this).addMuseumRecord(record);
                CustomToast.make(WriteRecordActivity.this, "添加成功！").show();
                back(true);
            }
        });

        mDeletePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(WriteRecordActivity.this)
                        .setTitle("删除该照片吗？")
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPhoto.setImageDrawable(null);
                                mTakePhoto.setEnabled(true);
                                mDeletePhoto.setVisibility(View.GONE);
                            }
                        }).setNegativeButton("取消", null).show();
            }
        });

        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPhotoFile != null && mPhotoFile.exists())
                    PhotoDetailFragment.newInstance(mPhotoFile.getPath()).show(getSupportFragmentManager(), "LookAtPhoto");
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == REQUEST_PHOTO){
                Uri uri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTH, mPhotoFile);
                revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                updateView();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSION_CAMERA){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                mTakePhoto.setEnabled(true);
            }else{
                new AlertDialog.Builder(WriteRecordActivity.this)
                        .setTitle("无法拍照")
                        .setPositiveButton("返回", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                back(true);
                            }
                        }).setCancelable(false).show();
            }
        }
    }

    private void updateView(){
        if(mPhotoFile == null || !mPhotoFile.exists()){
            mPhoto.setImageDrawable(null);
        }else{
            Bitmap bitmap = BitmapUtils.getBitmapFromPath(mPhotoFile.getPath());
            Log.d("WRA", String.valueOf(bitmap.getHeight()));
            mPhoto.setImageBitmap(bitmap);
            mTakePhoto.setEnabled(false);
            mDeletePhoto.setVisibility(View.VISIBLE);
        }
    }

    private String formatDateForFile(Date date){
        String format = "yyyyMMddhhmmss";
        return DateFormat.format(format, date).toString();
    }

    private String formatDateForRecord(Date date){
        String format = "yyyy-MM-dd hh:mm:ss a";
        Log.d("FORMAT", DateFormat.format(format, date).toString());
        return DateFormat.format(format, date).toString();
    }

    private void requestCamera(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)){
                Log.d("WRA", "shouldPermission");
                new AlertDialog.Builder(this)
                        .setTitle("需要获取允许拍照的权限。")
                        .setPositiveButton("修改权限", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(WriteRecordActivity.this,
                                        new String[]{Manifest.permission.CAMERA}, 1);
                            }
                        }).setCancelable(false).show();
            }else{
                Log.d("WRA", "requestPermission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                        REQUEST_PERMISSION_CAMERA);
            }
        }else{
            mTakePhoto.setEnabled(true);
        }
    }

    private File getFile(){
        String photoName = "IMG_" + formatDateForFile(new Date()) + ".jpg";
        return new File(WriteRecordActivity.this.getFilesDir(), photoName);
    }

    private void back(boolean backDirectly){
        if(backDirectly)
            super.onBackPressed();
        else{
            new AlertDialog.Builder(WriteRecordActivity.this)
                    .setTitle("不保存该记录吗？")
                    .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            WriteRecordActivity.super.onBackPressed();
                        }
                    }).setNegativeButton("取消", null)
                    .show();
        }
    }

    @Override
    public void onBackPressed() {
        back(false);
    }
}
