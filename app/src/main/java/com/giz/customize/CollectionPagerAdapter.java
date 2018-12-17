package com.giz.customize;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.giz.database.CollectionDB;
import com.giz.database.StarMuseum;
import com.giz.museum.CollectionFragment;
import com.giz.museum.MuseumActivity;
import com.giz.museum.R;
import com.giz.museum.WebViewActivity;
import com.giz.utils.ACache;
import com.giz.utils.DetailUtils;
import com.giz.utils.HttpSingleTon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.os.Environment.isExternalStorageRemovable;
import static com.giz.utils.DetailUtils.dp2px;

public class CollectionPagerAdapter extends PagerAdapter {

    private Context mContext;
    private TextView mTipTv;
    private ACache mACache;

    List<StarMuseum> mStarMuseumList = new ArrayList<>();
    List<IndexInfo> mIndexInfoList = new ArrayList<>();

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
            final SwipeRefreshLayout refreshLayout = view.findViewById(R.id.collection_museum_srl);
            // 初始化下拉刷新布局
            refreshLayout.setProgressViewOffset(true, 0, 100);
            refreshLayout.setColorSchemeResources(R.color.colorAccent);

            mStarMuseumList = CollectionDB.get(mContext).getStarredMuseums();
            final MuseumCollectionAdapter adapter = new MuseumCollectionAdapter(mStarMuseumList);
            if(adapter.getItemCount() != 0)
                mTipTv.setVisibility(View.GONE);
            recyclerView.setAdapter(adapter);

            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mStarMuseumList = CollectionDB.get(mContext).getStarredMuseums();
                    adapter.setStarMuseumList(mStarMuseumList);
                    if(adapter.getItemCount() != 0)
                        mTipTv.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    refreshLayout.setRefreshing(false);
                }
            });

            // 滑动删除
            ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
                @Override
                public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                    return makeMovementFlags(0, ItemTouchHelper.RIGHT);
                }

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                    final int pos = viewHolder.getAdapterPosition();
                    new AlertDialog.Builder(mContext)
                            .setTitle("是否删除该收藏？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    StarMuseum museum = mStarMuseumList.remove(pos);
                                    CollectionDB.get(mContext).removeStarMuseum(museum.getMuseumId());
                                    adapter.notifyItemRemoved(pos);
                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.notifyDataSetChanged();
                        }
                    }).show();
                }
            });
            helper.attachToRecyclerView(recyclerView);

            container.addView(view);
            return view;
        }else{
            // 收藏的活动、展览信息等等
            Log.d("PagerAdapter", "position 1");
            View view = LayoutInflater.from(mContext).inflate(R.layout.collection_vp_an, null);
            final RecyclerView recyclerView = view.findViewById(R.id.collection_list_ans);
            final SwipeRefreshLayout refreshLayout = view.findViewById(R.id.collection_ans_srl);
            // 初始化下拉刷新布局
            refreshLayout.setProgressViewOffset(true, 0, 100);
            refreshLayout.setColorSchemeResources(R.color.colorAccent);

            JSONArray infoArray = mACache.getAsJSONArray(CollectionFragment.ACACHE_ANS_KEY);
            final AnsCollectionAdapter adapter = new AnsCollectionAdapter();
//            Log.d("CACHE file", mACache.file(CollectionFragment.ACACHE_ANS_KEY).getAbsolutePath());
            if(infoArray != null){
                try {
                    for(int i = 0; i < infoArray.length(); i++){
                        mIndexInfoList.add(new IndexInfo(infoArray.getJSONObject(i)));
                    }
                    Collections.reverse(mIndexInfoList);  // 倒序
                    adapter.setIndexInfoList(mIndexInfoList);
                    if(adapter.getItemCount() != 0)
                        mTipTv.setVisibility(View.GONE);
                    recyclerView.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                // 设置空的adapter，能触发下拉刷新事件
                recyclerView.setAdapter(adapter);
            }
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    JSONArray infoArray = mACache.getAsJSONArray(CollectionFragment.ACACHE_ANS_KEY);
                    if(infoArray != null){
                        try {
                            mIndexInfoList.clear();
                            for(int i = 0; i < infoArray.length(); i++){
                                mIndexInfoList.add(new IndexInfo(infoArray.getJSONObject(i)));
                            }
                            Collections.reverse(mIndexInfoList);  // 倒序
                            adapter.setIndexInfoList(mIndexInfoList);
                            if(adapter.getItemCount() != 0)
                                mTipTv.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    refreshLayout.setRefreshing(false);
                }
            });

            // 滑动删除
            ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
                @Override
                public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                    return makeMovementFlags(0, ItemTouchHelper.LEFT);
                }

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    final int pos = viewHolder.getAdapterPosition();
                    new AlertDialog.Builder(mContext)
                            .setTitle("是否删除该收藏？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    IndexInfo indexInfo = mIndexInfoList.remove(pos);
                                    removeIndexCollection(indexInfo);
                                    adapter.notifyItemRemoved(pos);
                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.notifyDataSetChanged();
                        }
                    }).show();
                }
            });
            helper.attachToRecyclerView(recyclerView);

            container.addView(view);
            return view;
        }
    }

    private void removeIndexCollection(IndexInfo indexInfo) {
        try {
            JSONArray array = mACache.getAsJSONArray(CollectionFragment.ACACHE_ANS_KEY);
            if(array != null){
                for(int i = 0; i < array.length(); i++){
                    IndexInfo info = new IndexInfo(array.getJSONObject(i));
                    if(info.idxMuseumId.equals(indexInfo.idxMuseumId)){
                        array.remove(i);
                        mACache.put(CollectionFragment.ACACHE_ANS_KEY, array);
                        return;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
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
                    mLogo.setImageResource(R.drawable.skeleton_image);
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
        private LayoutInflater mInflater;

        private MuseumCollectionAdapter(List<StarMuseum> museums){
            Log.d("CPA", String.valueOf(museums.size()));
            mStarMuseumList = museums;
            mInflater = LayoutInflater.from(mContext);
        }

        @NonNull
        @Override
        public MuseumCollectionHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = mInflater.inflate(R.layout.list_item_collection,
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

        public void setStarMuseumList(List<StarMuseum> starMuseumList) {
            mStarMuseumList = starMuseumList;
        }
    }

    private class AnsCollectionHolder extends RecyclerView.ViewHolder {

        private ImageView mThumbImg;
        private TextView mTitle;
        private TextView mPlace;
        private TextView mDate;

        private AnsCollectionHolder(View view){
            super(view);

            mThumbImg = itemView.findViewById(R.id.ans_collection_thumb);
            mTitle = itemView.findViewById(R.id.ans_collection_title);
            mPlace = itemView.findViewById(R.id.ans_collection_place);
            mDate = itemView.findViewById(R.id.ans_collection_date);
        }

        private void bind(final IndexInfo info){
            mTitle.setText(info.idxTitle);
            mPlace.setText(info.idxPlace);
            mDate.setText(info.idxDate);
            HttpSingleTon.getInstance(mContext).addImageRequest(info.idxThumbUrl, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    mThumbImg.setImageBitmap(response);
                }
            }, (int)dp2px(mContext, 100), (int)dp2px(mContext, 75));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(WebViewActivity.newIntent(mContext, info.idxUrl));
                }
            });
        }
    }

    private class AnsCollectionAdapter extends RecyclerView.Adapter<AnsCollectionHolder>{

        private List<IndexInfo> mIndexInfoList;
        private LayoutInflater mInflater;

        private AnsCollectionAdapter(){
            mIndexInfoList = new ArrayList<>();
            mInflater = LayoutInflater.from(mContext);
        }

        private AnsCollectionAdapter(List<IndexInfo> indexInfos){
            mIndexInfoList = indexInfos;
            mInflater = LayoutInflater.from(mContext);
        }

        @NonNull
        @Override
        public AnsCollectionHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = mInflater.inflate(R.layout.list_item_ans_collection,
                    viewGroup, false);
            return new AnsCollectionHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AnsCollectionHolder ansCollectionHolder, int i) {
            ansCollectionHolder.bind(mIndexInfoList.get(i));
        }

        @Override
        public int getItemCount() {
            return mIndexInfoList.size();
        }

        public void setIndexInfoList(List<IndexInfo> indexInfoList) {
            mIndexInfoList = indexInfoList;
        }
    }

    // 记录博物馆活动/展览/新闻信息
    private class IndexInfo{
        String idxMuseumId = "";
        String idxMuseumName = "";
        String idxLogoUrl = "";
        String idxType = "";
        String idxTitle = "";
        String idxIntro = "";
        String idxThumbUrl = "";
        String idxDate = "";
        String idxPlace = "";
        String idxUrl = "";
        String idxObjectId = "";

        private IndexInfo(){}
        private IndexInfo(JSONObject object){
            try {
                idxMuseumId = object.getString("museumId");
                idxObjectId = object.getString("objectId");
                idxType = object.getString("type");
                idxTitle = object.getString("title");
                idxIntro = object.getString("intro");
                idxThumbUrl = object.getString("thumburl");
                idxDate = object.getString("date");
                idxUrl = object.getString("url");
                idxPlace = object.getString("place");
                idxMuseumName = object.getString("name");
                idxLogoUrl = object.getString("logourl");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        private JSONObject toJSON(){
            try {
                JSONObject object = new JSONObject();
                object.put("museumId", idxMuseumId);
                object.put("objectId", idxObjectId);
                object.put("type", idxType);
                object.put("title", idxTitle);
                object.put("intro", idxIntro);
                object.put("thumburl", idxThumbUrl);
                object.put("date", idxDate);
                object.put("url", idxUrl);
                object.put("place", idxPlace);
                object.put("name", idxMuseumName);
                object.put("logourl", idxLogoUrl);
                return object;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
