package com.giz.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.giz.museum.R;

public class MuseumPicturePagerAdapter extends PagerAdapter {

    private Context mContext;
    private String[] mPictures;
    private PictureManager mPictureManager;

    public MuseumPicturePagerAdapter(Context context, String folderName){
        mContext = context;
        mPictureManager = new PictureManager(context, folderName);
        mPictures = mPictureManager.getPictures();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageView = new ImageView(mContext);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageDrawable(mPictureManager.getDrawable(mPictures[position]));
        container.addView(imageView);
        return imageView;
    }

    @Override
    public int getCount() {
        return mPictures.length;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView)object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return (view == o);
    }
}
