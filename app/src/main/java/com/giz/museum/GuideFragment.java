package com.giz.museum;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.giz.database.Museum;
import com.giz.database.MuseumLibrary;
import com.giz.utils.TestFragment;

// TODO 状态栏颜色同步，绑定元素

public class GuideFragment extends TestFragment {

    private static final String ARGS_ID = "argsID";
    private static final String TAG = "GuideFragment";

    private Museum mMuseum;
    private MuseumActivity mActivity;
    
    public static GuideFragment newInstance(String museumId) {
        
        Bundle args = new Bundle();
        args.putString(ARGS_ID, museumId);
        
        GuideFragment fragment = new GuideFragment();
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
        Log.d(TAG, "onCreate: ");

        String id = getArguments().getString(ARGS_ID);
        mMuseum = MuseumLibrary.get().getMuseumById(id);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_guide, container, false);
        
        return view;
    }
}
