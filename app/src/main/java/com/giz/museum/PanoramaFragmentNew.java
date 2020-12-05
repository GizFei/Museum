package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.giz.database.Museum;
import com.giz.database.MuseumLibrary;
import com.giz.utils.DetailUtils;
import com.giz.utils.HttpSingleTon;
import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;

public class PanoramaFragmentNew extends Fragment {
    private static final String TAG = PanoramaFragmentNew.class.getName();
    private static final String ARGS_ID = "museumId";

    List<View> mDotsView;
    List<TextView> mIntroTextViews;
    private Museum mMuseum;
    private MuseumActivity mActivity;
    private ViewPager mViewPager;
    private PanoAdapter mPanoAdapter;
    private int panoNum = 5;

    private List<PanoInfo> mPanoInfoList;

    public static PanoramaFragmentNew newInstance(String museumId) {
        Bundle args = new Bundle();
        args.putString(ARGS_ID, museumId);

        PanoramaFragmentNew fragment = new PanoramaFragmentNew();
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
        mDotsView = new ArrayList<>();
        mIntroTextViews = new ArrayList<>();
        mPanoInfoList = new ArrayList<>();
        mMuseum = MuseumLibrary.get().getMuseumById(getArguments().getString(ARGS_ID));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pano, container, false);

        LinearLayout ll = view.findViewById(R.id.pano_ll);
        for(int i = 0; i < 5; i++){
            LinearLayout tempLL = (LinearLayout)ll.getChildAt(i);
            mDotsView.add(tempLL.getChildAt(0));
            mIntroTextViews.add((TextView)tempLL.getChildAt(1));
        }
        mViewPager = view.findViewById(R.id.pano_viewpager);
        mViewPager.setOffscreenPageLimit(5);

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 获取数据
        BmobQuery query = new BmobQuery("detail");
        query.addWhereEqualTo("museumId", mMuseum.getMuseumId());
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray array, BmobException e) {
                try {
                    JSONObject object = array.getJSONObject(0);
                    String panoUrl = "https://museum-treasure.oss-cn-beijing.aliyuncs.com/%E6%9D%AD%E5%B7%9E%E5%B7%A5%E8%89%BA%E7%BE%8E%E6%9C%AF%E5%8D%9A%E7%89%A9%E9%A6%86/Panorama/%E6%9D%AD%E5%B7%9E%E5%B7%A5%E8%89%BA%E9%A6%86.json";
                    JsonArrayRequest request = new JsonArrayRequest(panoUrl, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            initAdapter(response);
                        }
                    }, null);
                    HttpSingleTon.getInstance(mActivity).addToRequestQueue(request);
                }catch (Exception e1){
                    Log.d(TAG, "done: " + e.getLocalizedMessage());
                }
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                for(int k = 0; k < panoNum; k++){
                    if(i == k){
                        mDotsView.get(k).setBackgroundResource(R.drawable.ring_dot_active);
                        mIntroTextViews.get(k).setTextColor(0xCCFFFFFF);
                        continue;
                    }
                    mDotsView.get(k).setBackgroundResource(R.drawable.ring_dot);
                    mIntroTextViews.get(k).setTextColor(0x7DFFFFFF);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private void initAdapter(JSONArray response) {
        panoNum = response.length();
        for(int i = panoNum; i < 5; i++){
            ((LinearLayout)mDotsView.get(i).getParent()).setVisibility(View.GONE);
        }
        try {
            Log.d(TAG, "initAdapter: " + response.toString(4));
            for(int i = 0; i < response.length(); i++){
                JSONObject object = response.getJSONObject(i);
                PanoInfo info = new PanoInfo();
                info.title = object.getString("title");
                info.panoUrl = object.getString("pano");
                info.intro = object.getString("intro");
                mPanoInfoList.add(info);
            }
            mPanoAdapter = new PanoAdapter();
            mViewPager.setAdapter(mPanoAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        mPanoramaView.resumeRendering();
    }

    @Override
    public void onPause() {
        super.onPause();
//        mPanoramaView.pauseRendering();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        mPanoramaView.shutdown();
    }

    private class PanoAdapter extends PagerAdapter{

        @Override
        public int getCount() {
            return mPanoInfoList.size();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = LayoutInflater.from(mActivity).inflate(R.layout.pager_item_pano, null);

            mIntroTextViews.get(position).setText(mPanoInfoList.get(position).title);
            final ImageView imageView = view.findViewById(R.id.pip_imageView);
            ((TextView)view.findViewById(R.id.pip_tv)).setText(DetailUtils.createIndentText(mPanoInfoList.get(position).intro));

            String url = mPanoInfoList.get(position).panoUrl;
            HttpSingleTon.getInstance(mActivity).addImageRequest(url, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    imageView.setImageBitmap(response);
                }
            }, 0, 0);
            imageView.setTag(url);

//            imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(),
//                    R.drawable.panorama2));
//            imageView.setTag("LOCAL");

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityOptionsCompat compat = ActivityOptionsCompat.makeScaleUpAnimation(imageView,
                            imageView.getWidth()/2, imageView.getHeight()/2, 0, 0);
                    Intent intent = PanoramaDetailActivity.newIntent(mActivity, imageView.getTag().toString());
                    startActivity(intent, compat.toBundle());
                }
            });

            container.addView(view);
            return view;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View)object);
        }
    }

    private class PanoInfo{
        String title;
        String panoUrl;
        String intro;
    }
}
