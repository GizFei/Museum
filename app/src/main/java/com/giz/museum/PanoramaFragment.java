package com.giz.museum;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.giz.database.Museum;
import com.giz.database.MuseumLibrary;
import com.giz.utils.HttpSingleTon;
import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import org.json.JSONArray;
import org.json.JSONException;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;

public class PanoramaFragment extends Fragment {
    private static final String ARGS_ID = "museumId";

    private VrPanoramaView mPanoramaView;
    private Museum mMuseum;
    private MuseumActivity mActivity;

    public static PanoramaFragment newInstance(String museumId) {
        Bundle args = new Bundle();
        args.putString(ARGS_ID, museumId);

        PanoramaFragment fragment = new PanoramaFragment();
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_panorama, container, false);

        mPanoramaView = view.findViewById(R.id.vrView);
        mMuseum = MuseumLibrary.get().getMuseumById(getArguments().getString(ARGS_ID));

        initPanoramaView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPanoramaView.resumeRendering();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPanoramaView.pauseRendering();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPanoramaView.shutdown();
    }

    private void initPanoramaView(){
        final VrPanoramaView.Options options = new VrPanoramaView.Options();
        options.inputType = VrPanoramaView.Options.TYPE_MONO;
//        mPanoramaView.setFullscreenButtonEnabled(false); //隐藏全屏模式按钮
        mPanoramaView.setInfoButtonEnabled(false);
        mPanoramaView.setStereoModeButtonEnabled(false);
        mPanoramaView.setEventListener(new VrPanoramaEventListener(){
            @Override
            public void onLoadSuccess() {
            }

            @Override
            public void onLoadError(String errorMessage) {
            }

            @Override
            public void onClick() {
                super.onClick();
            }

            @Override
            public void onDisplayModeChanged(int newDisplayMode) {
                super.onDisplayModeChanged(newDisplayMode);
            }
        });
//        HttpSingleTon
        BmobQuery query = new BmobQuery("detail");
        query.addWhereEqualTo("museumId", mMuseum.getMuseumId());
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray array, BmobException e) {
                try {
                    String url = array.getJSONObject(0).getJSONObject("panorama").getString("url");
                    HttpSingleTon.getInstance(mActivity).addImageRequest(url, new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            mPanoramaView.loadImageFromBitmap(response, options);
                        }
                    }, 0, 0);
                } catch (JSONException e1) {
                    mPanoramaView.loadImageFromBitmap(BitmapFactory.decodeResource(getResources(),
                            R.drawable.panorama2), options);
                    e1.printStackTrace();
                }
            }
        });

    }
}
