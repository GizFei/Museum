package com.giz.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class BitmapUtils {

    public static Bitmap getBitmapFromPath(String path){
        Log.d("BitmapUtils", path);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        return BitmapFactory.decodeFile(path, options);
    }

}
