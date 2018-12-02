package com.giz.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

public class BitmapUtils {

    public static Bitmap getBitmapFromPath(String path){
        Log.d("BitmapUtils", path);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap resizeBitmap(Bitmap bm, int width, int height, boolean ifRecycle){
        int orgHeight = bm.getHeight();
        int orgWidth = bm.getWidth();

        float scaleWidth = ((float)width) / orgWidth;
        float scaleHeight = ((float)height) / orgHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizeBm = Bitmap.createBitmap(bm, 0,0, orgWidth, orgHeight, matrix, false);
        if(ifRecycle){
            bm.recycle();
        }
        return resizeBm;
    }

}
