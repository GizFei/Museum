package com.giz.customize;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.giz.database.CollectionDB;
import com.giz.database.StarMuseum;
import com.giz.museum.MuseumActivity;
import com.giz.museum.R;
import com.giz.utils.ACache;

import java.util.List;

public class CollectionPagerAdapter extends PagerAdapter {

    private Context mContext;
    private TextView mTipTv;
    private ACache mACache;

    public CollectionPagerAdapter(Context context, TextView tipTv){
        mContext = context;
        mTipTv = tipTv;
        mACache = ACache.get(context.getApplicationContext());
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 0){
            return "博物馆";
        }else{
            return "活动/展览";
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if(position == 0){
            // 收藏的博物馆
            Log.d("PagerAdapter", "position 0");
            View view = LayoutInflater.from(mContext).inflate(R.layout.collection_vp_museum, null);
            RecyclerView recyclerView = view.findViewById(R.id.collection_list_museum);
            MuseumCollectionAdapter adapter = new MuseumCollectionAdapter(mContext,
                    CollectionDB.get(mContext).getStarredMuseums());
            if(adapter.getItemCount() != 0)
                mTipTv.setVisibility(View.GONE);
            recyclerView.setAdapter(adapter);

            container.addView(view);
            return view;
        }else{
            // 收藏的活动、展览信息等等
            Log.d("PagerAdapter", "position 1");
            View view = LayoutInflater.from(mContext).inflate(R.layout.collection_vp_an, null);

            container.addView(view);
            return view;
        }
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    private class MuseumCollectionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mLogo;
        private TextView mName;
        private TextView mAddress;
        private TextView mDate;

        private String mMuseumId;

        private MuseumCollectionHolder(View view){
            super(view);

            itemView.setOnClickListener(this);
            mLogo = itemView.findViewById(R.id.star_museum_logo);
            mName = itemView.findViewById(R.id.star_museum_name);
            mAddress = itemView.findViewById(R.id.star_museum_address);
            mDate = itemView.findViewById(R.id.star_museum_date);
        }

        private void bind(StarMuseum museum){
            try{
                mMuseumId = museum.getMuseumId();
//                mLogo.setImageDrawable(MuseumLibrary.get().getMuseumById(museum.getMuseumId()).getLogo());
                Drawable logo = mACache.getAsDrawable(museum.getLogoCacheKey());
                if(logo == null){
                    mLogo.setImageResource(R.drawable.info_ic_museum);
                }else{
                    mLogo.setImageDrawable(logo);
                }
                mName.setText(museum.getName());
                mAddress.setText(museum.getAddress());
                mDate.setText(museum.getCollectionDate());
            }catch(Exception e){
                Toast.makeText(mContext, "还未加载完成", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onClick(View v) {
            Intent intent = MuseumActivity.newIntent(mContext, mMuseumId);
            mContext.startActivity(intent);
        }
    }

    private class MuseumCollectionAdapter extends RecyclerView.Adapter<MuseumCollectionHolder>{

        private List<StarMuseum> mStarMuseumList;
        private Context mContext;

        private MuseumCollectionAdapter(Context context, List<StarMuseum> museums){
            Log.d("CPA", String.valueOf(museums.size()));
            mStarMuseumList = museums;
            mContext = context;
        }

        @NonNull
        @Override
        public MuseumCollectionHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_collection,
                    viewGroup, false);
            return new MuseumCollectionHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MuseumCollectionHolder MuseumCollectionHolder, int i) {
            MuseumCollectionHolder.bind(mStarMuseumList.get(i));
        }

        @Override
        public int getItemCount() {
            return mStarMuseumList.size();
        }
    }
}
