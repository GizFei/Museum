package com.giz.museum;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import com.giz.customize.CollectionPagerAdapter;
import com.giz.utils.TestFragment;

public class CollectionFragment extends TestFragment {

    private static final String TAG = "CollectionFragment";
    public static final String ACACHE_ANS_KEY = "ansCollection";

//    private TabLayout mTabLayout;
//    private ViewPager mViewPager;
//    private TextView mNoCollectionTipTv;
    private DrawerActivity mDrawerActivity;

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
    public void onAttach(Context context) {
        super.onAttach(context);
        mDrawerActivity = (DrawerActivity)context;
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

        TabLayout mTabLayout = view.findViewById(R.id.collection_tab_layout);
        ViewPager mViewPager = view.findViewById(R.id.collection_view_pager);
        TextView mNoCollectionTipTv = view.findViewById(R.id.tip_no_collection);
        // 打开抽屉菜单
        view.findViewById(R.id.collection_open_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerActivity.openDrawerMenu();
            }
        });

        // 初始化ViewPager和TabLayout
        mTabLayout.setupWithViewPager(mViewPager);
        CollectionPagerAdapter adapter = new CollectionPagerAdapter(getActivity(), mNoCollectionTipTv);
        mViewPager.setAdapter(adapter);

        return view;
    }
}
