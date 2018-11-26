package com.giz.museum;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.giz.bmob.Museum;
import com.giz.bmob.MuseumLibrary;

public class AnFragment extends Fragment {

    private static final String TAG = "AnFragment";
    private static final String ARGS_ID = "args_id";

    private Museum mMuseum;

    private TabLayout mTabLayout;           // 顶部切换条
    private ViewPager mViewPager;           // 展示各模块的视图

    /**
     * 创建AnFragment，传入博物馆的ID
     * @param museumId 博物馆ID
     * @return AnFragment实例
     * 这是展示博物馆动态（活动、新闻、展览的信息）
     */
    public static AnFragment newInstance(String museumId){
        AnFragment fragment = new AnFragment();
        Bundle bundle = new Bundle();

        bundle.putString(ARGS_ID, museumId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 获得博物馆信息
        String id = getArguments().getString(ARGS_ID);
        Log.d(TAG, "AnFragment onCreate " + id);
        mMuseum = MuseumLibrary.get().getMuseumById(id);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "AnFragment onCreateView ");
        View view = inflater.inflate(R.layout.fragment_an, container, false);

        mTabLayout = view.findViewById(R.id.an_tab_layout);
        mViewPager = view.findViewById(R.id.an_view_pager);

        // 返回事件
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        return view;
    }
}
