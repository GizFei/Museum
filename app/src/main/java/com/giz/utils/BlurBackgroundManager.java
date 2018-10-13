package com.giz.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.giz.bmob.Museum;
import com.giz.bmob.MuseumLibrary;

import java.util.ArrayList;
import java.util.List;

public class BlurBackgroundManager {

    private static BlurBackgroundManager sManager;
    private List<Drawable> mBlurBackgrounds;

    public static BlurBackgroundManager get(Context context){
        if(sManager == null){
            sManager = new BlurBackgroundManager(context);
        }
        return sManager;
    }

    private BlurBackgroundManager(Context context){
        mBlurBackgrounds = new ArrayList<>();
        List<Museum> museums = MuseumLibrary.get().getMuseumList();
        for(Museum museum : museums){
            Drawable drawable = new PictureDownloader(context, museum.getPicFolder()).getFirstDrawable();
            Bitmap blurBg = FastBlur.doBlur(((BitmapDrawable)drawable).getBitmap(), 20, false);
            mBlurBackgrounds.add(new BitmapDrawable(blurBg));
        }
    }

    public List<Drawable> getBlurBackgrounds() {
        return mBlurBackgrounds;
    }

    public Drawable getBlurDrawable(int i){
        return mBlurBackgrounds.get(i);
    }

    public void setBlurBackgrounds(Context context, List<Museum> museumList){
        mBlurBackgrounds.clear();
        for(Museum museum : museumList){
            Drawable drawable = new PictureDownloader(context, museum.getPicFolder()).getFirstDrawable();
            Bitmap blurBg = FastBlur.doBlur(((BitmapDrawable)drawable).getBitmap(), 20, false);
            mBlurBackgrounds.add(new BitmapDrawable(blurBg));
        }
    }
}
