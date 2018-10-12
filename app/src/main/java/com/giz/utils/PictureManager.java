package com.giz.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;

public class PictureManager {

    /**
     * 图片资源管理类，用于获取assets文件夹中的资源
     */

    private AssetManager mAssetManager;
    private String mFolderName;

    public PictureManager(Context context, String folderName){
        mAssetManager = context.getAssets();
        mFolderName = folderName;
    }


    public String[] getPictures(){
        String[] mPictures;
        try{
            mPictures = mAssetManager.list(mFolderName);
        }catch (IOException ioe){
            mPictures = null;
            Log.d("PictureManager", "Couldn't list the folder.", ioe);
        }
        return mPictures;
    }

    public Drawable getDrawable(String name){
        Drawable drawable = null;
        try {
            String assetPath = mFolderName + "/" + name;
            InputStream is = mAssetManager.open(assetPath);
            drawable = Drawable.createFromStream(is, name.replace(".",""));
            is.close();
        } catch (IOException e) {
            Log.d("PictureManager", "Couldn't find the file", e);
        }
        return drawable;
    }

    public Drawable getFirstDrawable(){
        String name = getPictures()[0];
        return getDrawable(name);
    }
}
