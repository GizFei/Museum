package com.giz.museum;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.giz.bmob.Museum;
import com.giz.customize.MuseumLib;

public class InfoFragment extends Fragment {

    private static final String ARGS_ID = "bundle_id";
    private static final String TAG = "InfoFragment";

    private Museum mMuseum;

    /**
     * 创建InfoFragment，传入博物馆的ID
     * @param museumId 博物馆ID
     * @return InfoFragment实例
     */
    public static InfoFragment newInstance(String museumId){
        InfoFragment fragment = new InfoFragment();
        Bundle bundle = new Bundle();

        bundle.putSerializable(ARGS_ID, museumId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String uuid = getArguments().getString(ARGS_ID);
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
