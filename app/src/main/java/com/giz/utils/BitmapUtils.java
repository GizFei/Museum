package com.giz.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapUtils {

    public static Bitmap halfBitmap(String path){
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(path, options);

//        int srcWidth = options.outWidth;
//        int srcHeight = options.outHeight;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;

        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap getBitmapFromPath(String path){
        return BitmapFactory.decodeFile(path);
    }

}
