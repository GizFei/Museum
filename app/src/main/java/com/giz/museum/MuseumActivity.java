package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.giz.utils.Museum;
import com.giz.utils.MuseumLib;
import com.giz.utils.MuseumPicturePagerAdapter;

import java.util.UUID;

public class MuseumActivity extends AppCompatActivity {

    private static final String EXTRA_MUSEUM = "museum_intent";

    private Museum mMuseum;
    private LinearLayout mDotsLinearLayout;

    private InfoFragment mInfoFragment;

    public static Intent newIntent(Context context, UUID museumId){
        Intent intent = new Intent(context, MuseumActivity.class);
        intent.putExtra(EXTRA_MUSEUM, museumId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_museum);

        UUID museumId = (UUID)getIntent().getSerializableExtra(EXTRA_MUSEUM);
        mMuseum = MuseumLib.get(this).getMuseumById(museumId);

        initViews();
        initFragments();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.fragment_container, mInfoFragment).commit();
    }

    /**
     * 初始化Fragment
     */
    private void initFragments() {
        mInfoFragment = InfoFragment.newInstance(mMuseum.getMuseumId());
    }

    /**
     * 初始化布局
     */
    private void initViews() {

        ViewPager pictures = findViewById(R.id.picture_vp);
        final MuseumPicturePagerAdapter adapter = new MuseumPicturePagerAdapter(this,
                mMuseum.getPicFolder());
        pictures.setAdapter(adapter);

        CollapsingToolbarLayout ctl = findViewById(R.id.ctl);
        ctl.setTitle(mMuseum.getName());
        ctl.setCollapsedTitleTextColor(getResources().getColor(R.color.white));
        ctl.setExpandedTitleColor(getResources().getColor(R.color.transparent));
        ctl.setStatusBarScrimResource(R.color.colorPrimaryDark);

        mDotsLinearLayout = findViewById(R.id.dots_ll);
        for(int i = adapter.getCount(); i < 5; i++){
            mDotsLinearLayout.getChildAt(i).setVisibility(View.GONE);
        }

        pictures.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                for(int k = 0; k < adapter.getCount(); k++){
                    if(k == i){
                        mDotsLinearLayout.getChildAt(k).
                                setBackgroundResource(R.drawable.icon_dot_active);
                    }else{
                        mDotsLinearLayout.getChildAt(k).setBackgroundResource(R.drawable.icon_dot);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.nav_info:
                        setFragment(1);
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * 显示当前的Fragment
     * @param i Fragment的编号，从左到右，从1开始
     */
    private void setFragment(int i) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragments(transaction);
        switch (i){
            case 1:
                transaction.show(mInfoFragment);
                break;
        }
        transaction.commit();
    }

    /**
     * 隐藏所有Fragment
     */
    private void hideFragments(FragmentTransaction transaction) {
        if(mInfoFragment != null){
            transaction.hide(mInfoFragment);
        }
    }
}
