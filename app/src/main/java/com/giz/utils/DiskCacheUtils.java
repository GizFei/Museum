package com.giz.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static android.os.Environment.isExternalStorageRemovable;

public class DiskCacheUtils {
    private DiskLruCache mDiskLruCache;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

    private class InitDiskCacheTask extends AsyncTask<File, Void, Void>{
        @Override
        protected Void doInBackground(File... files) {
            synchronized (mDiskCacheLock){
                File cacheDir = files[0];
                try{
                    mDiskLruCache = DiskLruCache.open(cacheDir, 2, 1, DISK_CACHE_SIZE);
                    mDiskCacheStarting = false; // 完成初始化
                    mDiskCacheLock.notifyAll();;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    public void addBitmapToCache(String key, Bitmap bitmap){
        if(getBitmapFromDiskCache(key) == null){
            return;
        }
        synchronized (mDiskCacheLock){
            if(mDiskLruCache != null){
                try {
                    DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                    if(editor != null){
                        OutputStream outputStream = editor.newOutputStream(0);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        editor.commit();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Bitmap getBitmapFromDiskCache(String key) {
        synchronized (mDiskCacheLock){
            // 等待缓存初始化完成
            while(mDiskCacheStarting){
                try{
                    mDiskCacheLock.wait();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            if(mDiskLruCache != null){
                try {
                    return BitmapFactory.decodeStream(mDiskLruCache.get(key).getInputStream(0));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }
}
