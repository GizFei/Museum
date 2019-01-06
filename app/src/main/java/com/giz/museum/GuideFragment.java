package com.giz.museum;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.giz.customize.StereoView;
import com.giz.database.Museum;
import com.giz.database.MuseumLibrary;
import com.giz.utils.TestFragment;

import java.util.ArrayList;

import static cn.bmob.v3.Bmob.getApplicationContext;


// TODO 图像信息、对应楼层信息的补充
// TODO scrollListener中写旋转文字更新逻辑，

public class GuideFragment extends TestFragment {

    private static final String ARGS_ID = "argsID";
    private static final String TAG = "GuideFragment";
    private scrollListener mScrollListener;
    private int mcurItem = 1;               // 当前楼层

    private TextView mTextViewTitle;
    private TextView mTextViewContent;
    private StereoView mStereoView;

    private int floor;                      // 该博物馆的层数
    private Museum mMuseum;                 // 该博物馆
    private MuseumActivity mActivity;
    
    private String[] mStrings = {"第一单元 雕塑工艺", "第二单元 陶瓷工艺", "第三单元 织绣工艺", "第四单元 编织工艺", "第五单元 金属工艺", "第六单元 民间工艺"};
    private ArrayList titleList;

    @Override
    public String getTAG() {
        return TAG;
    }

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

        floor = 6;
        mScrollListener = new scrollListener();

        initContent();
    }

    /**
     *  scrollListener实现了StereoListener接口
     */
    public class scrollListener implements StereoView.StereoListener {
        //上滑一页时
        public void toPre(int curItem) {
            Log.d(TAG, "toPre: ");
            mcurItem = curItem;
            mTextViewTitle.setText(Integer.toString(curItem));
            mTextViewContent.setText(Integer.toString(curItem));
        }
        //下滑一页时
        public void toNext(int curItem) {
            Log.d(TAG, "toNext: ");
            mcurItem = curItem;
            mTextViewTitle.setText(Integer.toString(curItem));
            mTextViewContent.setText(Integer.toString(curItem));
        }
    }

    // 添加floor个部分的图像、title和content
    private void initContent() {
        titleList = new ArrayList<String>();
        for (int i=0; i<floor; i++) {
            titleList.add(mStrings[i]);
            Log.d(TAG, "initContent: " + mStrings[i]);
        }
    }

    // 初始化图像数据，绑定监听
    private void initViews() {
        for (int i=0; i<floor; i++) {
            ImageView imageView = new ImageView(getApplicationContext());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            imageView.setImageResource(R.drawable.sview);
            imageView.setLayoutParams(params);
            mStereoView.addView(imageView);

            imageView.setOnClickListener(new ImageView.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO 在这里面写点击进入对应楼层的藏品逻辑
                    Log.d(TAG, "onClick: "+mcurItem);
                }
            });
        }

        mStereoView.setStereoListener(mScrollListener);

//        补充初始化文字信息
//        mTextViewTitle.setText();
//        mTextViewContent.setText();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_guide, container, false);

        mStereoView = view.findViewById(R.id.stereoView);
        mTextViewTitle = view.findViewById(R.id.sectionTitle);
        mTextViewContent = view.findViewById(R.id.sectionContent);

        initViews();

        return view;
    }

}
