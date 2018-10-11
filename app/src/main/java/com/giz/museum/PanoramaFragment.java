package com.giz.museum;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

public class PanoramaFragment extends Fragment {

    private VrPanoramaView mPanoramaView;

    public static PanoramaFragment newInstance(){
        PanoramaFragment fragment = new PanoramaFragment();
        return fragment;
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
        VrPanoramaView.Options options = new VrPanoramaView.Options();
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
        mPanoramaView.loadImageFromBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.panorama2), options);
    }
}
