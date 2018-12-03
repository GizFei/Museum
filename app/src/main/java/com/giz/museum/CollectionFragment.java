package com.giz.museum;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.giz.customize.CollectionPagerAdapter;
import com.giz.utils.TestFragment;

public class CollectionFragment extends TestFragment {

    private static final String TAG = "CollectionFragment";

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private TextView mNoCollectionTipTv;

    public static CollectionFragment newInstance() {

        Bundle args = new Bundle();

        CollectionFragment fragment = new CollectionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "on create");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "on create view");
        View view = inflater.inflate(R.layout.fragment_collection, container, false);

        mTabLayout = view.findViewById(R.id.collection_tab_layout);
        mViewPager = view.findViewById(R.id.collection_view_pager);
        mNoCollectionTipTv = view.findViewById(R.id.tip_no_collection);

        // 初始化ViewPager和TabLayout
        mTabLayout.setupWithViewPager(mViewPager);
        CollectionPagerAdapter adapter = new CollectionPagerAdapter(getActivity(), mNoCollectionTipTv);
        mViewPager.setAdapter(adapter);

        return view;
    }
}
