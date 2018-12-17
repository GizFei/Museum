package com.giz.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

public class HttpSingleTon {

    private static HttpSingleTon sHttpSingleTon;
    private Context mContext;
    private RequestQueue mRequestQueue;

    private HttpSingleTon(Context context){
        mContext = context;
        mRequestQueue = getRequestQueue();
    }

    public static HttpSingleTon getInstance(Context context){
        if(sHttpSingleTon == null){
            sHttpSingleTon = new HttpSingleTon(context);
        }
        return sHttpSingleTon;
    }

    private RequestQueue getRequestQueue(){
        if(mRequestQueue == null){
            mRequestQueue = Volley.newRequestQueue(mContext);
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req){
        getRequestQueue().add(req);
    }

    public void addImageRequest(String url, Response.Listener<Bitmap> listener, int w, int h){
        ImageRequest request = new ImageRequest(url, listener, w, h, ImageView.ScaleType.CENTER_INSIDE,
                Bitmap.Config.RGB_565, null);
        getRequestQueue().add(request);
    }
}
