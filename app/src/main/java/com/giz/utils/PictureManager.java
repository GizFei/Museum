package com.giz.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class PictureManager {

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
}
