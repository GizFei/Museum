package com.giz.utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.giz.museum.MuseumActivity;
import com.giz.museum.MuseumListActivity;
import com.giz.museum.R;

import java.util.ArrayList;
import java.util.List;

public class CoverFlowPagerAdapter extends PagerAdapter {

    private List<Museum> mMuseumList;
    private Context mContext;
    private AppCompatActivity mActivity;

    public CoverFlowPagerAdapter(Context context, List<Museum> list, AppCompatActivity activity) {
        mMuseumList = list;
        mContext = context;
        mActivity = activity;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_museum_item_grid, null);

        final Museum museum = mMuseumList.get(position);

        final ImageView imageView = view.findViewById(R.id.museum_logo);
        TextView mMuseumName = view.findViewById(R.id.museum_name);
        List<TextView> mMuseumCatalogs = new ArrayList<>();
        mMuseumCatalogs.add((TextView) view.findViewById(R.id.museum_catalog1));
        mMuseumCatalogs.add((TextView) view.findViewById(R.id.museum_catalog2));
        mMuseumCatalogs.add((TextView) view.findViewById(R.id.museum_catalog3));

        mMuseumName.setText(museum.getName());
        int catalogs = museum.getCatalog().size();
        for (int i = 0; i < mMuseumCatalogs.size(); i++) {
            if (i < catalogs) {
                mMuseumCatalogs.get(i).setText(museum.getCatalog().get(i));
            } else {
                mMuseumCatalogs.get(i).setVisibility(View.GONE);
            }
        }
        PictureManager pm = new PictureManager(mContext, museum.getPicFolder());
        imageView.setImageDrawable(pm.getDrawable(pm.getPictures()[0]));

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MuseumActivity.newIntent(mContext, museum.getMuseumId());
                ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        mActivity, imageView, mActivity.getResources().getString(R.string.image_trans));
                ActivityCompat.startActivity(mContext, intent, compat.toBundle());
            }
        });

        container.addView(view);

        return view;
    }

    @Override
    public int getCount() {
        return mMuseumList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

}