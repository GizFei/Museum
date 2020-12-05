package com.giz.museum;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.giz.customize.CustomToast;
import com.giz.database.Museum;
import com.giz.utils.ACache;
import com.giz.utils.HttpSingleTon;
import com.giz.utils.TestFragment;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static com.giz.utils.DetailUtils.dp2px;

public class IndexFragment extends TestFragment {
    static {
        ClassicsFooter.REFRESH_FOOTER_PULLING = "上拉加载更多";
        ClassicsFooter.REFRESH_FOOTER_RELEASE = "释放立即加载";
        ClassicsFooter.REFRESH_FOOTER_REFRESHING = "正在刷新...";
        ClassicsFooter.REFRESH_FOOTER_LOADING = "正在加载...";
        ClassicsFooter.REFRESH_FOOTER_FINISH = "加载完成";
        ClassicsFooter.REFRESH_FOOTER_FAILED = "加载失败";
        ClassicsFooter.REFRESH_FOOTER_NOTHING = "没有更多数据了";
    }
    private static final String TAG = "IndexFragment";
    private static final int REQUEST_PERMISSION_STORAGE = 1;

    private RecyclerView mRecyclerView;
    private Toolbar mToolbar;
    private DrawerActivity mDrawerActivity;
    private ImageView mNoNetTipImg;
    private SmartRefreshLayout mSmartRefreshLayout;

    private List<Museum> mMuseumList;
    private List<IndexInfo> mIndexInfoList;
    private IndexAdapter mIndexAdapter;
    private ACache mACache;

    private int shownNum = 0; // 已加载的数据
    private int loadStep = 4; // 每次加载数据
    private boolean loadEnd = false; // 是否加载完所有数据

    public static IndexFragment newInstance() {
        Bundle args = new Bundle();
        
        IndexFragment fragment = new IndexFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDrawerActivity = (DrawerActivity)context;
        mACache = ACache.get(mDrawerActivity.getApplicationContext());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIndexInfoList = new ArrayList<>();
        mMuseumList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_index, container, false);

        mToolbar = view.findViewById(R.id.index_toolbar);
        mNoNetTipImg = view.findViewById(R.id.no_net_img);
        mSmartRefreshLayout = view.findViewById(R.id.index_srl);

        final LinearLayoutManager manager = new LinearLayoutManager(mDrawerActivity);
        mRecyclerView = view.findViewById(R.id.index_rv);
        mRecyclerView.setLayoutManager(manager);

        // 初始化Toolbar
        mToolbar.inflateMenu(R.menu.index_menu);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.index_museums:
                        String style = PreferenceManager.getDefaultSharedPreferences(mDrawerActivity)
                                .getString("museums_style", "LIST");
                        if(style.equals("LIST")){
                            Intent intent = new Intent(getActivity(), MuseumListActivity.class);
                            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(mDrawerActivity).toBundle());
                        }else{
                            Intent intent = new Intent(getActivity(), MuseumPagerActivity.class);
                            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(mDrawerActivity).toBundle());
                        }
                        return true;
                }
                return false;
            }
        });
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerActivity.openDrawerMenu();
            }
        });

        // 上拉加载更多
        mSmartRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                updateRecyclerView();
            }
        });
        // 下拉刷新
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshRecyclerView();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initRecyclerView();
    }

    /**
     * 第一次加载列表视图
     */
    private void initRecyclerView(){
        Log.d(TAG, "执行museum搜索");
        if(!isNetWorkAvailableAndConnected()){
            // 没有网络
//            mIndexAdapter = new IndexAdapter();
//            mRecyclerView.setAdapter(mIndexAdapter);
            mNoNetTipImg.setVisibility(View.VISIBLE);
            return;
        }
        Log.d(TAG, "执行museum搜索1");
        BmobQuery museumQuery = new BmobQuery("museum");
        museumQuery.addQueryKeys("objectId,name,logo");
        museumQuery.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray array, BmobException e) {
                try {
                        Log.d(TAG, "museum list" + array.toString(4));
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        Museum museum = new Museum(object.getString("objectId"));
                        museum.setLogoUrl("https://museum-treasure.oss-cn-beijing.aliyuncs.com/Logo/" + object.getJSONObject("logo").getString("filename"));
                        museum.setName(object.getString("name"));
                        mMuseumList.add(museum);
                    }
                    Log.d(TAG, "结束museum搜索");
                    updateRecyclerView();
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        });

        if(mIndexAdapter == null){
            mIndexAdapter = new IndexAdapter();
            mRecyclerView.setAdapter(mIndexAdapter);
        }else{
            mRecyclerView.setAdapter(mIndexAdapter);
        }
    }

    /**
     * 上拉更新列表视图
     */
    private void updateRecyclerView(){
        Log.d(TAG, "执行index搜索");
        if(loadEnd){
            // 没有更多数据的操作
            mSmartRefreshLayout.finishLoadMoreWithNoMoreData();
            return;
        }
        BmobQuery query = new BmobQuery("index");
        // 时间前推排序
        query.order("-date");
        query.setSkip(shownNum).setLimit(loadStep);
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray array, BmobException e) {
                try {
                    shownNum += array.length();
                    if(array.length() < loadStep){
                        loadEnd = true;
                    }
                    for(int i = 0; i < array.length(); i++){
                        Log.d(TAG, array.getJSONObject(i).toString(4));
                        IndexInfo info = new IndexInfo(array.getJSONObject(i));
                        int idx = getMuseumIdxById(info.idxMuseumId);
                        if(idx != -1){
                            info.idxMuseumName = mMuseumList.get(idx).getName();
                            info.idxLogoUrl = mMuseumList.get(idx).getLogoUrl();
                        }
                        mIndexInfoList.add(info);

                        mIndexAdapter.notifyDataSetChanged();
                        mSmartRefreshLayout.finishLoadMore(true);
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                    mSmartRefreshLayout.finishLoadMore(false);
                }
            }
        });
    }

    /**
     * 下拉刷新列表视图
     */
    private void refreshRecyclerView(){
        shownNum = 0;
        loadEnd =  false; // 回归初始状态
        mIndexInfoList.clear();

        BmobQuery query = new BmobQuery("index");
        // 时间前推排序
        query.order("-date");
        query.setSkip(shownNum).setLimit(loadStep);
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray array, BmobException e) {
                try {
                    shownNum += array.length();
                    if(array.length() < loadStep){
                        loadEnd = true;
                    }
                    for(int i = 0; i < array.length(); i++){
                        Log.d(TAG, array.getJSONObject(i).toString(4));
                        IndexInfo info = new IndexInfo(array.getJSONObject(i));
                        int idx = getMuseumIdxById(info.idxMuseumId);
                        if(idx != -1){
                            info.idxMuseumName = mMuseumList.get(idx).getName();
                            info.idxLogoUrl = mMuseumList.get(idx).getLogoUrl();
                        }
                        mIndexInfoList.add(info);

                        mIndexAdapter.notifyDataSetChanged();
                        mSmartRefreshLayout.finishRefresh(true);
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                    mSmartRefreshLayout.finishRefresh(false);
                }
            }
        });
    }

    private class IndexHolder extends RecyclerView.ViewHolder{

        private ImageView mLogoImg;
        private TextView mNameTv;
        private Button mCollectBtn;
        private Button mShareBtn;
        private ImageView mIndexImg;
        private TextView mTitleTv;
        private TextView mDateTv;
        private TextView mPlaceTv;
        private ImageView mTypeImg;

        private IndexHolder(@NonNull View view) {
            super(view);
            mLogoImg = itemView.findViewById(R.id.index_museum_logo);
            mNameTv = itemView.findViewById(R.id.index_museum_name);
            mCollectBtn = itemView.findViewById(R.id.index_collect);
            mShareBtn = itemView.findViewById(R.id.index_share);
            mIndexImg = itemView.findViewById(R.id.card_index_image);
            mDateTv = itemView.findViewById(R.id.index_date);
            mPlaceTv = itemView.findViewById(R.id.index_place);
            mTitleTv = itemView.findViewById(R.id.index_title);
            mTypeImg = itemView.findViewById(R.id.index_type_img);
        }

        private void bind(final IndexInfo info){
            if(info == null){
                return;
            }
            switch (info.idxType){
                case "show":
                    mTypeImg.setImageResource(R.drawable.index_type_show);
                    break;
                case "news":
                    mTypeImg.setImageResource(R.drawable.index_type_news);
                    break;
                case "activity":
                    mTypeImg.setImageResource(R.drawable.index_type_activity);
                    break;
            }
            mNameTv.setText(info.idxMuseumName);
            mTitleTv.setText(info.idxTitle);
            mDateTv.setText(info.idxDate);
            // 判断是否收藏过
            if(isAnsCollected(info.idxObjectId)){
                mCollectBtn.setSelected(true);
                mCollectBtn.setEnabled(false);
            }else{
                mCollectBtn.setSelected(false);
                mCollectBtn.setEnabled(true);
            }
            if(info.idxPlace.equals(""))
                mPlaceTv.setVisibility(View.GONE);
            else
                mPlaceTv.setText(info.idxPlace);
            HttpSingleTon.getInstance(mDrawerActivity).addImageRequest(info.idxLogoUrl, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    mLogoImg.setImageBitmap(response);
                }
            }, (int)dp2px(mDrawerActivity, 36f), (int)dp2px(mDrawerActivity, 36f));
            HttpSingleTon.getInstance(mDrawerActivity).addImageRequest(info.idxThumbUrl, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    mIndexImg.setImageBitmap(response);
                }
            }, 0, 0);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(mDrawerActivity,
                            mIndexImg, getResources().getString(R.string.image_trans));
                    startActivity(AnsDetailActivity.newIntent(mDrawerActivity, info.toJSON().toString()), optionsCompat.toBundle());
                }
            });
            mNameTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = MuseumActivity.newIntent(mDrawerActivity, info.idxMuseumId);
                    startActivity(intent);
                }
            });
            mLogoImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = MuseumActivity.newIntent(mDrawerActivity, info.idxMuseumId);
                    startActivity(intent);
                }
            });
            mCollectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!isAnsCollected(info.idxObjectId)){
                        mCollectBtn.setSelected(true);
                        mCollectBtn.setEnabled(false);
                        JSONArray array = mACache.getAsJSONArray(CollectionFragment.ACACHE_ANS_KEY);
                        if(array == null){
                            array = new JSONArray();
                        }
                        array.put(info.toJSON());
                        mACache.put(CollectionFragment.ACACHE_ANS_KEY, array);
                        CustomToast.make(mDrawerActivity, "收藏成功").show();
                    }else{
                        mCollectBtn.setSelected(true);
                        mCollectBtn.setEnabled(false);
                        CustomToast.make(mDrawerActivity, "收藏过了").show();
                    }

                }
            });
            mShareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(requestWriteStorage()){
                        itemView.setDrawingCacheEnabled(true);
                        itemView.buildDrawingCache();
                        Bitmap bm = itemView.getDrawingCache(false);
                        String path = MediaStore.Images.Media.insertImage(mDrawerActivity.getContentResolver(), bm,
                                "shareCard", "分享卡片");
                        Uri bitmapUri = Uri.parse(path);
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                        intent.setType("image/*");
                        startActivity(Intent.createChooser(intent, "分享到..."));
                    }
                }
            });
        }
    }

    private class IndexAdapter extends RecyclerView.Adapter<IndexHolder>{

        @NonNull
        @Override
        public IndexHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new IndexHolder(LayoutInflater.from(getContext()).inflate(R.layout.card_item_index, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull IndexHolder indexHolder, int i) {
            if(mIndexInfoList.size() != 0)
                indexHolder.bind(mIndexInfoList.get(i));
        }

        @Override
        public int getItemCount() {
//            return mIndexInfoList.size() == 0 ? 3 : mIndexInfoList.size();
            return mIndexInfoList.size();
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
                if(object.has("place"))
                    idxPlace = object.getString("place");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        private JSONObject toJSON(){
            JSONObject object = new JSONObject();
            try {
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
                return object;
            }
        }
    }

    private int getMuseumIdxById(String museumId){
        for(int i = 0; i < mMuseumList.size(); i++){
            if(mMuseumList.get(i).getMuseumId().equals(museumId)){
                return i;
            }
        }
        return -1;
    }

    private boolean isNetWorkAvailableAndConnected(){
        ConnectivityManager cm = (ConnectivityManager)mDrawerActivity.getSystemService(CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected());
    }

    private boolean isAnsCollected(String ansId){
        try {
            JSONArray array = mACache.getAsJSONArray(CollectionFragment.ACACHE_ANS_KEY);
            if(array != null){
                for(int i = 0; i < array.length(); i++){
                    IndexInfo info = new IndexInfo(array.getJSONObject(i));
                    if(info.idxObjectId.equals(ansId))
                        return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 获得“允许存储”权限
     */
    private boolean requestWriteStorage(){
        if(ContextCompat.checkSelfPermission(mDrawerActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(mDrawerActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                Log.d(TAG, "shouldPermission");
                new AlertDialog.Builder(mDrawerActivity)
                        .setTitle("需要获取允许存储的权限。")
                        .setPositiveButton("修改权限", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(mDrawerActivity,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE);
                            }
                        }).setCancelable(false).show();
                return false;
            }else{
                Log.d(TAG, "requestPermission");
                ActivityCompat.requestPermissions(mDrawerActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_STORAGE);
                return false;
            }
        }else{
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSION_STORAGE){
            // 允许权限
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                new AlertDialog.Builder(mDrawerActivity)
                        .setTitle("未获得修改存储的权限，无法分享")
                        .setPositiveButton("取消", null).setCancelable(false).show();
            }
        }
    }
}
