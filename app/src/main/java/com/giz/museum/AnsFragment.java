package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.giz.customize.CustomToast;
import com.giz.database.Museum;
import com.giz.database.MuseumLibrary;
import com.giz.utils.HttpSingleTon;
import com.giz.utils.TestFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class AnsFragment extends TestFragment {

    private static final String TAG = "AnsFragment";
    private static final String ARGS_ID = "args_id";

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mRefreshLayout;

    private Museum mMuseum;
    private AnsAdapter mAdapter;
    private MuseumActivity mActivity;
    private List<ANSInfo> mANSInfoList;

    /**
     * 创建AnFragment，传入博物馆的ID
     * @param museumId 博物馆ID
     * @return AnFragment实例
     * 这是展示博物馆动态（活动、新闻、展览的信息）
     */
    public static AnsFragment newInstance(String museumId){
        AnsFragment fragment = new AnsFragment();
        Bundle bundle = new Bundle();

        bundle.putString(ARGS_ID, museumId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (MuseumActivity)context;
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 获得博物馆信息
        String id = getArguments().getString(ARGS_ID);
        Log.d(TAG, "AnsFragment onCreate " + id);
        mMuseum = MuseumLibrary.get().getMuseumById(id);

        mANSInfoList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "AnsFragment onCreateView ");
        View view = inflater.inflate(R.layout.fragment_ans, container, false);

        mRecyclerView = view.findViewById(R.id.ans_recycler_view);
        mRefreshLayout = view.findViewById(R.id.ans_refresh_layout);

        mRefreshLayout.setProgressViewOffset(true, 0, 100);
        mRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        mAdapter = new AnsAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setupRecyclerView();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView");
        BmobQuery query = new BmobQuery("detail");
        query.addQueryKeys("ans");
        query.addWhereEqualTo("museumId", mMuseum.getMuseumId());
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray array, BmobException e) {
                if(e == null && array.length() != 0){
                    try {
                        Log.d(TAG, array.toString(4));
                        JSONObject anInfo = array.getJSONObject(0);
                        String ansJSONUrl = anInfo.getJSONObject("ans").getString("url");
                        JsonObjectRequest ansRequest = new JsonObjectRequest(ansJSONUrl, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                updateRv(response);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "volley error" + error.getMessage());
                            }
                        });
                        HttpSingleTon.getInstance(mActivity).addToRequestQueue(ansRequest);
                    } catch (JSONException e1) {
                        CustomToast.make(mActivity, "数据丢了...").show();
                        Log.d(TAG, "Bmob error");
                        e1.printStackTrace();
                    }
                }
                mRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void updateRv(JSONObject ansJSON){
        Log.d(TAG, "updateRv");
        mANSInfoList.clear();   // 清除数据
        try {
            Log.d(TAG, ansJSON.toString(4));
            JSONArray showArray = ansJSON.getJSONArray("show");
            if(showArray.length() != 0){
                ANSInfo showHInfo = new ANSInfo(); // 展览头
                showHInfo.ansType = "Head";
                showHInfo.hText = "展览";
                mANSInfoList.add(showHInfo);
                for(int i = 0; i < showArray.length(); i++){
                    ANSInfo info = new ANSInfo(showArray.getJSONObject(i));
                    mANSInfoList.add(info);
                }
            }
            JSONArray activityArray = ansJSON.getJSONArray("activity");
            if(activityArray.length() != 0) {
                ANSInfo activityHInfo = new ANSInfo(); // 活动头
                activityHInfo.ansType = "Head";
                activityHInfo.hText = "活动";
                mANSInfoList.add(activityHInfo);
                for (int i = 0; i < activityArray.length(); i++) {
                    ANSInfo info = new ANSInfo(activityArray.getJSONObject(i));
                    mANSInfoList.add(info);
                }
            }
            JSONArray newsArray = ansJSON.getJSONArray("news");
            if(newsArray.length() != 0){
                ANSInfo newsHInfo = new ANSInfo(); // 新闻头
                newsHInfo.ansType = "Head";
                newsHInfo.hText = "新闻";
                mANSInfoList.add(newsHInfo);
                for(int i = 0; i < newsArray.length(); i++){
                    ANSInfo info = new ANSInfo(newsArray.getJSONObject(i));
                    mANSInfoList.add(info);
                }
            }

            if(mAdapter == null){
                mAdapter = new AnsAdapter();
                mRecyclerView.setAdapter(mAdapter);
            }else{
                mAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mRefreshLayout.setRefreshing(false);
    }

    private class AnsHolder extends RecyclerView.ViewHolder{

        private int mType;

        private AnsHolder(View view, int type){
            super(view);
            mType = type;
        }

        private void bind(final ANSInfo info){
            if(mType == AnsAdapter.TYPE_HEAD){
                ((TextView)itemView.findViewById(R.id.ans_head)).setText(info.hText);
            }else{
                ImageRequest thumbRequest = new ImageRequest(info.ansThumbUrl, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        ((ImageView)itemView.findViewById(R.id.ans_thumb)).setImageBitmap(response);
                    }
                }, 100, 75, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ((ImageView)itemView.findViewById(R.id.ans_thumb)).setImageResource(R.drawable.skeleton_image);
                    }
                });
                HttpSingleTon.getInstance(mActivity).addToRequestQueue(thumbRequest);
                ((TextView)itemView.findViewById(R.id.ans_title)).setText(info.ansTitle);
                ((TextView)itemView.findViewById(R.id.ans_date)).setText("时间：" + info.ansDate);
                if(info.asPlace.equals("")){
                    itemView.findViewById(R.id.ans_place).setVisibility(View.GONE);
                }else{
                    ((TextView)itemView.findViewById(R.id.ans_place)).setText("地点：" + info.asPlace);
                }
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = WebViewActivity.newIntent(mActivity, info.ansUrl);
                    startActivity(intent);
                }
            });
        }
    }

    private class AnsAdapter extends RecyclerView.Adapter<AnsHolder>{

        private static final int TYPE_HEAD = 0;
        private static final int TYPE_CONTENT = 1;
        private LayoutInflater mInflater;

        private AnsAdapter(){
            mInflater =  LayoutInflater.from(mActivity);
        }

        @NonNull
        @Override
        public AnsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            if(i == TYPE_HEAD){
                // 头布局
                return new AnsHolder(mInflater.inflate(R.layout.list_item_ans_head, viewGroup, false), i);
            }else{
                return new AnsHolder(mInflater.inflate(R.layout.list_item_ans, viewGroup, false), i);
            }
        };

        @Override
        public void onBindViewHolder(@NonNull AnsHolder ansHolder, int i) {
            ansHolder.bind(mANSInfoList.get(i));
        }

        @Override
        public int getItemCount() {
            return mANSInfoList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if(mANSInfoList.get(position).ansType.equals("Head")){
                return TYPE_HEAD;
            }else{
                return TYPE_CONTENT;
            }
        }
    }

    private boolean isNetWorkAvailableAndConnected(){
        ConnectivityManager cm = (ConnectivityManager)mActivity.getSystemService(CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected());
    }

    // 记录博物馆活动/展览/新闻信息
    private class ANSInfo{
        String ansType = "";
        String ansTitle = "";
        String ansIntro = "";
        String ansThumbUrl = "";
        String ansDate = "";
        String asPlace = "";
        String ansUrl = "";
        String showOrganize = "";
        String activityPeople = "";
        String hText = ""; // 头部标题

        private ANSInfo(){}
        private ANSInfo(JSONObject object){
            try {
                Log.d(TAG, object.toString(4));
                ansType = object.getString("type");
                ansTitle = object.getString("title");
                ansIntro = object.getString("intro");
                ansThumbUrl = object.getString("thumburl");
                ansDate = object.getString("date");
                ansUrl = object.getString("url");
                if(ansType.equals("show")){
                    asPlace = object.getString("place");
                    showOrganize = object.getString("organize");
                }
                if(ansType.equals("activity")){
                    asPlace = object.getString("place");
                    activityPeople = object.getString("people");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
