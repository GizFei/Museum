package com.giz.museum;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.ArraySet;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.giz.database.MuseumRecord;
import com.giz.database.RecordDB;
import com.giz.customize.CardSlideTransformer;
import com.giz.customize.CustomToast;
import com.giz.utils.BitmapUtils;
import com.giz.utils.TestFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecordFragment extends TestFragment {

    private static final String TAG = "RecordFragment";

    private RecyclerView mRecyclerView;
    private RecordCoverAdapter mAdapter;
    private TextView mNoRecordTv;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private DrawerActivity mActivity;

    public static RecordFragment newInstance() {
        Bundle args = new Bundle();

        RecordFragment fragment = new RecordFragment();
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
        mActivity = (DrawerActivity)context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_new, container, false);

        mRecyclerView = view.findViewById(R.id.record_rv);
        mNoRecordTv = view.findViewById(R.id.tip_no_record);
        mSwipeRefreshLayout = view.findViewById(R.id.record_srl);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        // 打开抽屉菜单
        view.findViewById(R.id.record_open_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.openDrawerMenu();
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateRv();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateRv();
    }

    private void updateRv() {
        List<MuseumRecord> records = RecordDB.get(mActivity).getMuseumRecords();
        List<String> museumNames = new ArrayList<>();
        Map<String, String> covers = new HashMap<>();
        Map<String, Integer> nums = new HashMap<>();

        for(MuseumRecord record : records){
            if(museumNames.contains(record.getName())){
                nums.put(record.getName(), nums.get(record.getName()) + 1);
            }else{
                museumNames.add(record.getName());
                nums.put(record.getName(), 1);
                covers.put(record.getName(), record.getPicturePath());
            }
        }
        if(museumNames.size() > 0){
            mNoRecordTv.setVisibility(View.GONE);
        }
        if(mAdapter == null){
            mAdapter = new RecordCoverAdapter(museumNames, covers, nums);
            mRecyclerView.setAdapter(mAdapter);
        }else{
            mAdapter.setData(museumNames, covers, nums);
            mAdapter.notifyDataSetChanged();
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private class RecordCoverHolder extends RecyclerView.ViewHolder{

        private ImageView mCoverImg;
        private TextView mCoverTitle;
        private TextView mCoverNum;

        private RecordCoverHolder(View view){
            super(view);
            mCoverImg = itemView.findViewById(R.id.record_cover_img);
            mCoverTitle = itemView.findViewById(R.id.record_cover_title);
            mCoverNum = itemView.findViewById(R.id.record_cover_num);
        }

        private void bind(final String name, String coverPath, int num){
            mCoverImg.setImageBitmap(BitmapFactory.decodeFile(coverPath));
            mCoverTitle.setText(name);
            mCoverNum.setText(num + "条记录");

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 进入该博物馆的详细记录
                    Intent intent = RecordDetailActivity.newIntent(mActivity, name);
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(mActivity).toBundle());
                }
            });
        }
    }

    private class RecordCoverAdapter extends RecyclerView.Adapter<RecordCoverHolder>{

        private LayoutInflater mInflater;
        private Map<String, String> mCovers;
        private List<String> mNameList;
        private Map<String, Integer> mNums;

        private RecordCoverAdapter(List<String> names, Map<String, String> covers, Map<String, Integer> nums){
            mInflater = LayoutInflater.from(mActivity);
            mCovers = covers;
            mNameList = names;
            mNums = nums;
        }

        @NonNull
        @Override
        public RecordCoverHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new RecordCoverHolder(mInflater.inflate(R.layout.list_item_record_cover, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecordCoverHolder recordCoverHolder, int i) {
            String name = mNameList.get(i);
            recordCoverHolder.bind(name, mCovers.get(name), mNums.get(name));
        }

        @Override
        public int getItemCount() {
            return mNameList.size();
        }

        private void setData(List<String> names, Map<String, String> covers, Map<String, Integer> nums) {
            mCovers = covers;
            mNameList = names;
            mNums = nums;
        }
    }


}
