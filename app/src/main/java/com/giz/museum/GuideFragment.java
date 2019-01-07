package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.giz.customize.CustomToast;
import com.giz.customize.StereoView;
import com.giz.database.Museum;
import com.giz.database.MuseumLibrary;
import com.giz.utils.DetailUtils;
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

import static cn.bmob.v3.Bmob.getApplicationContext;


// TODO 图像信息、对应楼层信息的补充
// TODO scrollListener中写旋转文字更新逻辑，

public class GuideFragment extends TestFragment {

    private static final String ARGS_ID = "argsID";
    private static final String TAG = "GuideFragment";
    private scrollListener mScrollListener;
    private int mcurItem = 1;               // 当前楼层

    private TextView mTextViewTitle;
    private TextView mTextViewContent;
    private StereoView mStereoView;

    private int floor;                      // 该博物馆的层数
    private Museum mMuseum;                 // 该博物馆
    private MuseumActivity mActivity;
    
    private String[] mStrings = {"第一单元 雕塑工艺", "第二单元 陶瓷工艺", "第三单元 织绣工艺", "第四单元 编织工艺", "第五单元 金属工艺", "第六单元 民间工艺"};

    private List<String> mTitleList;
    private List<String> mContentList;

    @Override
    public String getTAG() {
        return TAG;
    }

    public static GuideFragment newInstance(String museumId) {
        
        Bundle args = new Bundle();
        args.putString(ARGS_ID, museumId);
        
        GuideFragment fragment = new GuideFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (MuseumActivity)context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        String id = getArguments().getString(ARGS_ID);
        mMuseum = MuseumLibrary.get().getMuseumById(id);

        floor = 0;
        mTitleList = new ArrayList<>();
        mContentList = new ArrayList<>();
        mScrollListener = new scrollListener();

//        initContent();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        BmobQuery query = new BmobQuery("detail");
        query.addWhereEqualTo("museumId", mMuseum.getMuseumId());
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray array, BmobException e) {
                try {
                    JSONObject object = array.getJSONObject(0);
                    String jsonUrl = object.getJSONObject("guide").getString("url");
                    JsonArrayRequest request = new JsonArrayRequest(jsonUrl, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            initContent(response);
                            initViews(response);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            CustomToast.make(mActivity, "数据丢了...").show();
                        }
                    });
                    HttpSingleTon.getInstance(mActivity).addToRequestQueue(request);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    /**
     *  scrollListener实现了StereoListener接口
     */
    public class scrollListener implements StereoView.StereoListener {
        //上滑一页时
        public void toPre(int curItem) {
            Log.d(TAG, "toPre: ");
            mcurItem = curItem;
            mTextViewTitle.setText(mTitleList.get(mcurItem));
            mTextViewContent.setText(DetailUtils.createIndentText(mContentList.get(mcurItem)));
//            mTextViewTitle.setText(String.valueOf(curItem));
//            mTextViewContent.setText(String.valueOf(curItem));
        }
        //下滑一页时
        public void toNext(int curItem) {
            Log.d(TAG, "toNext: ");
            mcurItem = curItem;
            mTextViewTitle.setText(mTitleList.get(mcurItem));
            mTextViewContent.setText(DetailUtils.createIndentText(mContentList.get(mcurItem)));
//            mTextViewTitle.setText(String.valueOf(curItem));
//            mTextViewContent.setText(String.valueOf(curItem));
        }
    }

    // 添加floor个部分的图像、title和content
    private void initContent(JSONArray guideArr) {
        try {
            for(int i = 0; i < guideArr.length(); i++){
                JSONObject object = guideArr.getJSONObject(i);
                mTitleList.add(object.getString("title"));
                mContentList.add(object.getString("content"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        titleList = new ArrayList<String>();
//        for (int i=0; i<floor; i++) {
//            titleList.add(mStrings[i]);
//            Log.d(TAG, "initContent: " + mStrings[i]);
//        }
    }

    // 初始化图像数据，绑定监听
    private void initViews(JSONArray guideArray) {
        try {
            floor = guideArray.length();
            for (int i=0; i<floor; i++) {
                final ImageView imageView = new ImageView(getApplicationContext());
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
    //            imageView.setImageResource(R.drawable.sview);
                imageView.setLayoutParams(params);
                mStereoView.addView(imageView);

                final String thumbUrl = guideArray.getJSONObject(i).getString("thumb");
                HttpSingleTon.getInstance(mActivity).addImageRequest(thumbUrl, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        imageView.setImageBitmap(response);
                    }
                }, 0, 0);

                imageView.setOnClickListener(new ImageView.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = ImageDetailActivity.newIntent(mActivity, imageView.getDrawable());
                        startActivity(intent);
                        Log.d(TAG, "onClick: "+mcurItem);
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mStereoView.setStereoListener(mScrollListener);

//        补充初始化文字信息
        mTextViewTitle.setText(mTitleList.get(0));
        mTextViewContent.setText(DetailUtils.createIndentText(mContentList.get(0)));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_guide, container, false);

        mStereoView = view.findViewById(R.id.stereoView);
        mTextViewTitle = view.findViewById(R.id.sectionTitle);
        mTextViewContent = view.findViewById(R.id.sectionContent);

//        initViews();

        return view;
    }

}
