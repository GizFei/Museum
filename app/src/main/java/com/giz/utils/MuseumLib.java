package com.giz.utils;

import android.content.Context;

import com.giz.museum.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MuseumLib {
    /**
     * Museum Library 博物馆单例
     */
    private static MuseumLib sMuseumLib;
    private List<Museum> mMuseumList;

    public static MuseumLib get(Context context){
        if(sMuseumLib == null){
            sMuseumLib = new MuseumLib(context);
        }
        return sMuseumLib;
    }

    private MuseumLib(Context  context){
        mMuseumList = new ArrayList<>();
        initMuseumList();
    }

    private void initMuseumList(){
        Museum museum = new Museum();

        museum.setName("浙江省博物馆");
        museum.setCatalog(Arrays.asList("综合", "人文", "省级"));
        museum.setLogo(R.drawable.museum_zhejiang);
        mMuseumList.add(museum);

        museum.setName("杭州韩美林艺术馆");
        museum.setCatalog(Arrays.asList("个人", "艺术"));
        museum.setLogo(R.drawable.museum_hml);
        mMuseumList.add(museum);

        museum.setName("杭州工艺美术馆");
        museum.setCatalog(Arrays.asList("工艺", "刀剪剑"));
        museum.setLogo(R.drawable.museum_ac);
        mMuseumList.add(museum);

        museum.setName("中国印学博物馆");
        museum.setCatalog(Arrays.asList("印章", "园林式"));
        museum.setLogo(R.drawable.museum_print);
        mMuseumList.add(museum);
    }

    public List<Museum> getMuseumList(){
        return mMuseumList;
    }
}
