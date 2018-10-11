package com.giz.museum;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.giz.utils.Museum;
import com.giz.utils.MuseumLib;
import com.giz.utils.MuseumPicturePagerAdapter;

import java.util.UUID;

public class InfoFragment extends Fragment {

    private static final String ARGS_ID = "bundle_id";
    private static final String TAG = "InfoFragment";

    private Museum mMuseum;

    /**
     * 创建InfoFragment，传入博物馆的ID
     * @param museumId 博物馆ID
     * @return InfoFragment实例
     */
    public static InfoFragment newInstance(UUID museumId){
        InfoFragment fragment = new InfoFragment();
        Bundle bundle = new Bundle();

        bundle.putSerializable(ARGS_ID, museumId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID uuid = (UUID)getArguments().getSerializable(ARGS_ID);
//        Log.d(TAG, uuid.toString());

        mMuseum = MuseumLib.get(getContext()).getMuseumById(uuid);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
