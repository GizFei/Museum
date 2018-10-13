package com.giz.bmob;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.giz.museum.MuseumListActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;

public class MuseumLibrary {

    /**
     * 用于管理博物馆信息的静态类
     * 该类全局存在，供不同的活动调用获取博物馆信息
     */
    private List<Museum> mMuseumList;   // 博物馆列表，从云端获取后不变
    private static MuseumLibrary sMuseumLibrary;   // 静态类变量

    /**
     * 获取该静态量，先判断是否已经建立，为空则调用内部构造函数
     * @return MuseumLibrary类静态常量
     */
    public static MuseumLibrary get(){
        if(sMuseumLibrary == null){
            sMuseumLibrary = new MuseumLibrary();
        }
        return sMuseumLibrary;
    }

    /**
     * 内部构造函数
     */
    private MuseumLibrary(){
        mMuseumList = new ArrayList<>();
    }

    public List<Museum> getMuseumList() {
        return mMuseumList;
    }

    public void setMuseumList(List<Museum> museums){
        mMuseumList = museums;
    }

    /**
     * 根据词语简单地查询博物馆
     * @param newText 词语
     * @return 满足条件的博物馆列表
     */
    public List<Museum> queryMuseumsByWord(String newText) {
        List<Museum> museums = new ArrayList<>();
        for(Museum museum: mMuseumList){
            if(museum.getName().contains(newText)){
                museums.add(museum);
            }
        }
        return museums;
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
}
