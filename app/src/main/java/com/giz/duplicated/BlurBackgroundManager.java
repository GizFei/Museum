//package com.giz.utils;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;
//
//import com.giz.bmob.Museum;
//import com.giz.bmob.MuseumLibrary;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class BlurBackgroundManager {
//
//    private static BlurBackgroundManager sManager;
//    private List<Drawable> mBlurBackgrounds;
//
//    public static BlurBackgroundManager get(Context context){
//        if(sManager == null){
//            sManager = new BlurBackgroundManager(context);
//        }
//        return sManager;
//    }
//
//    private BlurBackgroundManager(Context context){
//        mBlurBackgrounds = new ArrayList<>();
//        List<Museum> museums = MuseumLibrary.get().getMuseumList();
//        for(Museum museum : museums){
//            Drawable drawable = new PictureDownloader(context, museum.getPicFolder()).getFirstDrawable();
//            Bitmap blurBg = FastBlur.doBlur(((BitmapDrawable)drawable).getBitmap(), 20, false);
//            mBlurBackgrounds.add(new BitmapDrawable(blurBg));
//        }
//    }
//
//    public List<Drawable> getBlurBackgrounds() {
//        return mBlurBackgrounds;
//    }
//
//    public Drawable getBlurDrawable(int i){
//        return mBlurBackgrounds.get(i);
//    }
//
//    public void setBlurBackgrounds(Context context, List<Museum> museumList){
//        mBlurBackgrounds.clear();
//        for(Museum museum : museumList){
//            Drawable drawable = new PictureDownloader(context, museum.getPicFolder()).getFirstDrawable();
//            Bitmap blurBg = FastBlur.doBlur(((BitmapDrawable)drawable).getBitmap(), 20, false);
//            mBlurBackgrounds.add(new BitmapDrawable(blurBg));
//        }
//    }
//}

//private class BlurBgTask extends AsyncTask<List<String>, Void, Void>{
//    @Override
//    protected Void doInBackground(List<String>... lists) {
//        List<String> urls = lists[0];
//        List<String> ids = lists[1];
//        try{
//            mBgDrawables = new HashMap<>();
//            for(int i = 0; i < urls.size(); i++){
//                Drawable drawable = Drawable.createFromStream(new URL(urls.get(i)).openStream(), "BG");
//                Bitmap blurBg = FastBlur.doBlur(((BitmapDrawable)drawable).getBitmap(), 10, false);
//                mBgDrawables.put(ids.get(i), new BitmapDrawable(blurBg));
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @Override
//    protected void onPostExecute(Void aVoid) {
//        if(mPagerAdapter == null){
//            mPagerAdapter = new CoverFlowPagerAdapter(MuseumListActivity.this,
//                    mMuseumList, MuseumListActivity.this, mSearchView);
//            mMuseumViewPager.setAdapter(mPagerAdapter);
//        }
//
//        mSwitchIcon.setEnabled(true);
//        mProgressBar.setVisibility(View.GONE);
//        super.onPostExecute(aVoid);
//    }
//}
