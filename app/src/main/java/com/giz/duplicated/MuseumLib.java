package com.giz.duplicated;

import android.content.Context;
import android.util.Log;

import com.giz.bmob.Museum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MuseumLib {
    /**
     * Museum Library 博物馆单例，在程序运行期间存在
     */
    private static MuseumLib sMuseumLib;
    private List<Museum> mMuseumList;

    /**
     * 单例的静态构造函数
     * @param context 上下文
     * @return MuseumLib实例
     */
    public static MuseumLib get(Context context){
        if(sMuseumLib == null){
            sMuseumLib = new MuseumLib(context);
        }
        return sMuseumLib;
    }

    /**
     * 内部私有构造函数（实际的构造函数）
     * @param context 上下文
     */
    private MuseumLib(Context context){
        mMuseumList = new ArrayList<>();
        initMuseumList();
    }

    /**
     * 初始化博物馆列表
     */
    private void initMuseumList(){
        Log.d("initMuseumList", "Downloading...");
        Museum museum = new Museum("ZJ");
        museum.setName("浙江省博物馆");
        museum.setCatalog(Arrays.asList("综合", "人文", "省级"));
//        museum.setLogo(R.drawable.museum_zhejiang);
        museum.setPicFolder("ZJMGS");
        mMuseumList.add(museum);

        Museum museum1 = new Museum("HML");
        museum1.setName("杭州韩美林艺术馆");
        museum1.setCatalog(Arrays.asList("个人", "艺术"));
//        museum1.setLogo(R.drawable.museum_hml);
        museum1.setPicFolder("HML");
        mMuseumList.add(museum1);

        Museum museum2 = new Museum("AC");
        museum2.setName("杭州工艺美术博物馆");
        museum2.setCatalog(Arrays.asList("工艺", "刀剪剑"));
        museum2.setPicFolder("HZACM");
//        museum2.setLogo(R.drawable.museum_ac);
        mMuseumList.add(museum2);

        Museum museum3 = new Museum("YX");
        museum3.setName("中国印学博物馆");
        museum3.setCatalog(Arrays.asList("印章", "园林式"));
        museum3.setPicFolder("SLYS");
//        museum3.setLogo(R.drawable.museum_print);
        mMuseumList.add(museum3);
    }

    /**
     * 外部获取博物馆列表接口
     * @return 博物馆列表
     */
    public List<Museum> getMuseumList(){
        return mMuseumList;
    }

    /**
     * 根据ID查询博物馆
     * @param museumId 博物馆ID
     * @return 博物馆对象
     */
    public Museum getMuseumById(String museumId){
        for(Museum museum: mMuseumList){
            if(museum.getMuseumId().equals(museumId)){
                return museum;
            }
        }
        return null;
    }

    public List<Museum> queryMuseumsByWord(String newText) {
        List<Museum> museums = new ArrayList<>();
        for(Museum museum: mMuseumList){
            if(museum.getName().contains(newText)){
                museums.add(museum);
            }
        }
        return museums;
    }
}
