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

    private List<Museum> mMuseumList;
    private static MuseumLibrary sMuseumLibrary;

    public static MuseumLibrary get(){
        if(sMuseumLibrary == null){
            sMuseumLibrary = new MuseumLibrary();
        }
        return sMuseumLibrary;
    }

    private MuseumLibrary(){
        mMuseumList = new ArrayList<>();
        Log.d("kkk", "MLMLMLML");
    }

    public List<Museum> getMuseumList() {
        return mMuseumList;
    }

    public void setMuseumList(List<Museum> museums){
        mMuseumList = museums;
    }

    /**
     * 根据词语简单地查询博物馆
     * @param newText
     * @return
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
