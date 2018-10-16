package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.giz.bmob.Museum;
import com.giz.bmob.MuseumLibrary;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;

/**
 * getActivity为空：回退的时候，Fragment被销毁，但是异步进程还在运行，导致异步进程中的getActivity方法错误
 */
public class InfoFragment extends Fragment {

    private static final String ARGS_ID = "bundle_id";
    private static final String TAG = "InfoFragment";

    private Museum mMuseum;
    private CardView mInfoCard;
    private CardView mIntroCard;
    private CardView mActivityCard;
    private CardView mNewsCard;

    private ActivityOrShowTask mActivityOrShowTask;
    private NewsTask mNewsTask;
    /**
     * 创建InfoFragment，传入博物馆的ID
     * @param museumId 博物馆ID
     * @return InfoFragment实例
     */
    public static InfoFragment newInstance(String museumId){
        InfoFragment fragment = new InfoFragment();
        Bundle bundle = new Bundle();

        bundle.putString(ARGS_ID, museumId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String id = getArguments().getString(ARGS_ID);
        Log.d(TAG, id);

        mMuseum = MuseumLibrary.get().getMuseumById(id);
        mActivityOrShowTask = new ActivityOrShowTask();
        mNewsTask = new NewsTask();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        mInfoCard = view.findViewById(R.id.info);
        mIntroCard = view.findViewById(R.id.introduction);
        mActivityCard = view.findViewById(R.id.recent_activity);
        mNewsCard = view.findViewById(R.id.recent_news);

        initDetails();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mActivityOrShowTask != null){
            mActivityOrShowTask.cancel(true);
        }
        if(mNewsTask != null){
            mNewsTask.cancel(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void initInfoCard(){
        LinearLayout infoContainer = mInfoCard.findViewById(R.id.info_container);
        ((TextView)infoContainer.getChildAt(0)).setText(mMuseum.getName()); // 名称
        ((TextView)infoContainer.getChildAt(1)).setText("地址：" + mMuseum.getAddress()); // 地址
        ((TextView)infoContainer.getChildAt(2)).setText("门票：" + mMuseum.getTicket());  // 门票
        ((TextView)infoContainer.getChildAt(3)).setText("开放时间：" + mMuseum.getOpenTime()); // 开放时间
    }

    private void initIntroCard(){
        ((TextView)mIntroCard.getChildAt(1)).setText(mMuseum.getIntro());
    }

    private void initActivityCard(List<MuseumAOrS> museumAOrs){
        LinearLayout activityContainer = mActivityCard.findViewById(R.id.activity_container);
        if(museumAOrs == null)
            return;
        for(int i = 0; i < museumAOrs.size(); i++){
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.card_item_activity, null);
            MuseumAOrS aOrS = museumAOrs.get(i);
            ((ImageView)view.findViewById(R.id.pic_activity)).setImageDrawable(aOrS.thumbDrawable);
            ((TextView)view.findViewById(R.id.title_activity)).setText(aOrS.title);
            if(aOrS.tag.equals("activity")){
                ((ImageView)view.findViewById(R.id.tag_activity)).setImageResource(R.mipmap.tag_activity);
            }else{
                ((ImageView)view.findViewById(R.id.tag_activity)).setImageResource(R.mipmap.tag_show);
            }
            ((TextView)view.findViewById(R.id.date_activity)).setText(aOrS.date);
            ((TextView)view.findViewById(R.id.place_activity)).setText(aOrS.place);
            ((TextView)view.findViewById(R.id.people_activity)).setText(aOrS.people);
            if(i == museumAOrs.size()-1)
                view.findViewById(R.id.divider).setVisibility(View.GONE);
            activityContainer.addView(view);
        }
    }

    private void initNewsCard(List<MuseumNews> museumNews){
        LinearLayout newsContainer = mNewsCard.findViewById(R.id.news_container);
        if(museumNews == null)
            return;
        for(int i = 0; i < museumNews.size(); i++){
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.card_item_news, null);
            final MuseumNews news = museumNews.get(i);
            ((ImageView)view.findViewById(R.id.pic_news)).setImageDrawable(news.thumbDrawable);
            ((TextView)view.findViewById(R.id.title_news)).setText(news.title);
            ((TextView)view.findViewById(R.id.date_news)).setText(news.date);
            ((TextView)view.findViewById(R.id.url_news)).setText(news.url);
            if(i == museumNews.size()-1)
                view.findViewById(R.id.divider).setVisibility(View.GONE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = WebViewActivity.newIntent(getActivity(), news.url);
                    startActivity(intent);
                }
            });
            newsContainer.addView(view);
        }
    }

    private void initDetails(){
        BmobQuery query = new BmobQuery("detail");
        query.addWhereEqualTo("museumId", mMuseum.getMuseumId());
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray array, BmobException e) {
                if(e == null){
                    try{
                        JSONObject object = array.getJSONObject(0);
                        mMuseum.setAddress(object.getString("address"));
                        mMuseum.setTicket(object.getString("ticket"));
                        mMuseum.setOpenTime(object.getString("opentime"));
                        mMuseum.setIntro(object.getString("intro"));
                        initInfoCard();
                        initIntroCard();
                        mActivityOrShowTask.execute(object.getJSONArray("activities"));
                        mNewsTask.execute(object.getJSONArray("news"));
                    }catch (Exception ee){
                        ee.printStackTrace();
                    }
                }
            }
        });
    }

    private class ActivityOrShowTask extends AsyncTask<JSONArray, Void, List<MuseumAOrS>>{
        @Override
        protected List<MuseumAOrS> doInBackground(JSONArray... arrays) {
            try{
                List<MuseumAOrS> museumAOrs = new ArrayList<>();
                Log.d("INFOFRAGMENT", String.valueOf(arrays[0].length()));
                for(int i = 0; i < arrays[0].length(); i++){
                    JSONObject activity = arrays[0].getJSONObject(i);
                    MuseumAOrS aOrS = new MuseumAOrS();
                    aOrS.tag = activity.getString("tag");
                    aOrS.title = activity.getString("title");
                    aOrS.thumbDrawable = Drawable.createFromStream(new URL(activity.getString("thumburl")).openStream(), "THUMB");
                    aOrS.date = activity.getString("date");
                    aOrS.place = activity.getString("place");
                    aOrS.people = activity.getString("people");
                    museumAOrs.add(aOrS);
                }
                return museumAOrs;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<MuseumAOrS> aOrS) {
            initActivityCard(aOrS);
        }
    }

    private class NewsTask extends AsyncTask<JSONArray, Void, List<MuseumNews>>{
        @Override
        protected List<MuseumNews> doInBackground(JSONArray... jsonArrays) {
            try{
                List<MuseumNews> museumNews = new ArrayList<>();
                for(int i = 0; i < jsonArrays[0].length(); i++){
                    JSONObject news = jsonArrays[0].getJSONObject(i);
                    MuseumNews mn = new MuseumNews();
                    mn.title = news.getString("title");
                    mn.date = news.getString("date");
                    mn.url = news.getString("url");
                    mn.thumbDrawable = Drawable.createFromStream(new URL(news.getString("thumburl")).openStream(), "THUMB");
                    museumNews.add(mn);
                }
                return museumNews;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<MuseumNews> museumNews) {
            initNewsCard(museumNews);
        }
    }

    /**
     * 博物馆活动或展览类
     */
    private class MuseumAOrS {
        String tag;
        String title;
        Drawable thumbDrawable;
        String date;
        String place;
        String people;
    }

    private class MuseumNews{
        String title;
        Drawable thumbDrawable;
        String date;
        String url;
    }
}
