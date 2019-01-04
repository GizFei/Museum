package com.giz.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.giz.duplicated.PictureDownloader;
import com.giz.museum.ImageDetailActivity;
import com.giz.museum.MuseumActivity;
import com.giz.museum.R;

import java.util.List;

public class MuseumPicturePagerAdapter extends PagerAdapter {

    private Context mContext;
//    private String[] mPictures;
//    private PictureDownloader mPictureManager;

    private List<Drawable> mDrawables;

//    public MuseumPicturePagerAdapter(Context context, String folderName){
//        mContext = context;
//        mPictureManager = new PictureDownloader(context, folderName);
//        mPictures = mPictureManager.getPictures();
//    }

    public MuseumPicturePagerAdapter(Context context, List<Drawable> drawables){
        mContext = context;
        mDrawables = drawables;
//        mPictureManager = new PictureDownloader(context, folderName);
//        mPictures = mPictureManager.getPictures();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        final ImageView imageView = new ImageView(mContext);
        imageView.setTransitionName(mContext.getResources().getString(R.string.image_trans));
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        imageView.setImageDrawable(mPictureManager.getDrawable(mPictures[position]));
        imageView.setImageDrawable(mDrawables.get(position));
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityOptionsCompat optionsCompat1 = ActivityOptionsCompat.makeScaleUpAnimation(imageView,
                        imageView.getWidth()/2, imageView.getHeight()/2, 0,0);

//                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                        (MuseumActivity)mContext, imageView, imageView.getTransitionName());
                mContext.startActivity(ImageDetailActivity.newIntent(mContext, imageView.getDrawable()),
                        optionsCompat1.toBundle());
            }
        });
        container.addView(imageView);
        return imageView;
    }

    @Override
    public int getCount() {
        return mDrawables.size();
    }

    /**
     * 必须重写该函数，否则无法正常显示内容
     * @param container
     * @param position
     * @param object
     */
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView)object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return (view == o);
    }
}
