package com.giz.museum;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.giz.utils.BlurBackgroundManager;
import com.giz.utils.CoverFlowEffectTransformer;
import com.giz.utils.CoverFlowPagerAdapter;
import com.giz.utils.Museum;
import com.giz.utils.MuseumLib;
import com.giz.utils.SliderTouchHelperCallback;

import java.util.ArrayList;
import java.util.List;

public class MuseumListActivity extends AppCompatActivity {

    // 列表显示样式：
    // true：列表形式，false：网格形式
    private boolean isListStyle = true;
    private MuseumAdapter mMuseumAdapter;
    private RecyclerView mMuseumRecyclerView;
    private ViewPager mMuseumViewPager;
    private ImageView mSwitchIcon;
    private View mPagerBg;
    private List<Drawable> mBgDrawables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_museum);

        // 初始化列表
        mMuseumRecyclerView = findViewById(R.id.list_museum);
        mMuseumRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMuseumAdapter = new MuseumAdapter(MuseumLib.get(this).getMuseumList());
        mMuseumRecyclerView.setAdapter(mMuseumAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SliderTouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(mMuseumRecyclerView);

        // 初始化悬浮按钮
        FloatingActionButton fab = findViewById(R.id.map_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityOptionsCompat compat = ActivityOptionsCompat.makeScaleUpAnimation(v,
                        v.getWidth()/2, v.getHeight()/2, 0, 0);
                ActivityCompat.startActivity(MuseumListActivity.this,
                        new Intent(MuseumListActivity.this, MuseumMapActivity.class),
                        compat.toBundle());
            }
        });

        // 初始化列表样式转换按钮
        mSwitchIcon = findViewById(R.id.list_style);
        mSwitchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchRecyclerView();
            }
        });

        // 初始化搜索框事件
        SearchView mSearchView = findViewById(R.id.search_view);
        View underline = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        if(underline != null){
            underline.setBackgroundColor(Color.TRANSPARENT);
        }
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mMuseumAdapter.setMuseumList(MuseumLib.get(MuseumListActivity.this).queryMuseumsByWord(newText));
                mMuseumAdapter.notifyDataSetChanged();
                return true;
            }
        });

        mMuseumViewPager = findViewById(R.id.pager_museum);
        mPagerBg = findViewById(R.id.pager_bg);
        initViewPager();
    }

    private void initViewPager(){
        // 预先加载好模糊背景
        mBgDrawables = BlurBackgroundManager.get(this).getBlurBackgrounds();

        mMuseumViewPager.setClipChildren(false);
        mMuseumViewPager.setOffscreenPageLimit(3);
        mMuseumViewPager.setPageTransformer(false, new CoverFlowEffectTransformer(this));

        CoverFlowPagerAdapter adapter = new CoverFlowPagerAdapter(this,
                MuseumLib.get(this).getMuseumList(), this);
        mMuseumViewPager.setAdapter(adapter);

        mMuseumViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            /**
             * state
             * SCROLL_STATE_DRAGGING 1 手指按下
             * SCROLL_STATE_SETTLING 2 手指松开
             * SCROLL_STATE_IDLE 0 停止滑动
             */
            int currentPage = 0;
            int state;
            boolean pageChanged = false;

            private float half(float alpha){
                return (alpha * 0.5f + 0.5f);
            }

            @Override
            public void onPageScrolled(int i, float v, int i1) {
//                Log.d("onPageScrolled", String.format("%d %f", i, v));
                if(state == 1){ // 手指滑动
                    if(i < currentPage)
                        mPagerBg.setAlpha(half(v));
                    else
                        mPagerBg.setAlpha(half(1-v));
                }else if(state == 2){ // 布局自己滑动（补足偏差）
                    if(!pageChanged){
                        if(i == currentPage){
                            mPagerBg.setAlpha(half(1-v));
                        }else{
                            if(v != 0f)
                                mPagerBg.setAlpha(half(v));
                        }
                    }else{
                        if(i == currentPage){
                            mPagerBg.setAlpha(half(v));
                        }else{
                            mPagerBg.setAlpha(half(1-v));
                        }
                    }
                }
            }

            @Override
            public void onPageSelected(int i){
                pageChanged = true;
//                Log.d("onPageSelected", String.valueOf(i));
                currentPage = i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                state = i;
                if(i == ViewPager.SCROLL_STATE_IDLE){
                    if(pageChanged){
                        setMuseumViewPagerBg(currentPage);
                        pageChanged = false;
                    }else{
                        mPagerBg.setAlpha(1);
                    }

                }
//                Log.d("onPageScrollState", String.valueOf(i));
            }
        });
    }


    /**
     * 设置ViewPager列表样式下应用背景
     * @param i 列表项位置
     */
    private void setMuseumViewPagerBg(int i){
        // 前后图片平滑过渡
        TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{
                mPagerBg.getBackground(), mBgDrawables.get(i)});
        mPagerBg.setBackground(transitionDrawable);
        transitionDrawable.startTransition(400);

        // 恢复图片的透明度至1
//        mPagerBg.setBackground(mBgDrawables.get(i));
        ValueAnimator animator = ObjectAnimator.ofFloat(mPagerBg, "alpha",
                0.5f, 1.0f);
        animator.setDuration(200);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    /**
     * 切换列表显示样式
     */
    private void switchRecyclerView() {
        if(isListStyle){
            AnimatedVectorDrawableCompat listToGridAnim = AnimatedVectorDrawableCompat.create(this,
                    R.drawable.av_list_to_pager);
            mSwitchIcon.setImageDrawable(listToGridAnim);
            ((Animatable)mSwitchIcon.getDrawable()).start();
            isListStyle = false;

            mMuseumRecyclerView.setVisibility(View.GONE);
            mMuseumViewPager.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this,
                    R.anim.layout_anim_from_bottom));
            mMuseumViewPager.setVisibility(View.VISIBLE);
//            mPagerBg.setVisibility(View.VISIBLE);
            mMuseumViewPager.setCurrentItem(0, true);
            mPagerBg.setBackground(mBgDrawables.get(0));

//            LinearLayoutManager llm = new LinearLayoutManager(this);
//            llm.setOrientation(LinearLayoutManager.HORIZONTAL);
//            mMuseumRecyclerView.setLayoutManager(llm);
//            LinearSnapHelper linearSnapHelper= new LinearSnapHelper();
//            new LinearSnapHelper().attachToRecyclerView(mMuseumRecyclerView);
//            mMuseumAdapter.setMuseumList(MuseumLib.get(MuseumListActivity.this).getMuseumList());
//            mMuseumRecyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this,
//                    R.anim.layout_anim_from_bottom));
//            mMuseumRecyclerView.setAdapter(mMuseumAdapter);
//            mMuseumRecyclerView.startLayoutAnimation();
        }else{
            AnimatedVectorDrawableCompat gridToListAnim = AnimatedVectorDrawableCompat.create(this,
                    R.drawable.av_pager_to_list);
            mSwitchIcon.setImageDrawable(gridToListAnim);
            ((Animatable)mSwitchIcon.getDrawable()).start();
            isListStyle = true;

            mMuseumRecyclerView.setVisibility(View.VISIBLE);
            mMuseumViewPager.setVisibility(View.GONE);
            mPagerBg.setBackgroundResource(R.color.light_bg);
//            mPagerBg.setVisibility(View.GONE);

//            LinearLayoutManager llm = new LinearLayoutManager(this);
//            llm.setOrientation(LinearLayoutManager.VERTICAL);
//            mMuseumRecyclerView.setLayoutManager(llm);
            mMuseumAdapter.setMuseumList(MuseumLib.get(MuseumListActivity.this).getMuseumList());
            mMuseumRecyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this,
                    R.anim.layout_anim_from_bottom));
            mMuseumRecyclerView.setAdapter(mMuseumAdapter);
            mMuseumRecyclerView.startLayoutAnimation();
        }
    }

    private class MuseumHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView mMuseumName;
        private List<TextView> mMuseumCatalogs;
        private ImageView mMuseumLogo;
        private Museum mMuseum;

        private MuseumHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mMuseumCatalogs = new ArrayList<>();
            mMuseumName = itemView.findViewById(R.id.museum_name);
            mMuseumCatalogs.add((TextView)itemView.findViewById(R.id.museum_catalog1));
            mMuseumCatalogs.add((TextView)itemView.findViewById(R.id.museum_catalog2));
            mMuseumCatalogs.add((TextView)itemView.findViewById(R.id.museum_catalog3));
            mMuseumLogo = itemView.findViewById(R.id.museum_logo);
        }

        private void bind(Museum museum){
            mMuseum = museum;
            mMuseumName.setText(museum.getName());
            int catalogs = museum.getCatalog().size();
            for(int i = 0; i < mMuseumCatalogs.size(); i++){
                if(i < catalogs){
                    mMuseumCatalogs.get(i).setText(museum.getCatalog().get(i));
                }else{
                    mMuseumCatalogs.get(i).setVisibility(View.GONE);
                }
            }
            mMuseumLogo.setImageResource(museum.getLogo());
        }

        @Override
        public void onClick(View v) {
            Intent intent = MuseumActivity.newIntent(MuseumListActivity.this,
                    mMuseum.getMuseumId());
            ActivityOptionsCompat compat = ActivityOptionsCompat.makeCustomAnimation(
                        MuseumListActivity.this, R.anim.activity_in, R.anim.activity_out);
            ActivityCompat.startActivity(MuseumListActivity.this, intent, compat.toBundle());
        }
    }

    private class MuseumAdapter extends RecyclerView.Adapter<MuseumHolder>{

        private List<Museum> mMuseums;

        private MuseumAdapter(List<Museum> list){
            mMuseums = list;
        }

        @NonNull
        @Override
        public MuseumHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(MuseumListActivity.this);
            return new MuseumHolder(inflater.inflate(R.layout.list_museum_item, viewGroup,
                        false));
        }

        @Override
        public void onBindViewHolder(@NonNull MuseumHolder museumHolder, int i) {
            museumHolder.bind(mMuseums.get(i));
        }

        @Override
        public int getItemCount() {
            return mMuseums.size();
        }

        private void setMuseumList(List<Museum> list){
            mMuseums = list;
        }
    }

}
