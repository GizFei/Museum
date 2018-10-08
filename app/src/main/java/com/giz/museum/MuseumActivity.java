package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import com.giz.utils.Museum;
import com.giz.utils.MuseumLib;
import com.giz.utils.MuseumPicturePagerAdapter;
import com.giz.utils.PictureManager;

import java.util.UUID;

public class MuseumActivity extends AppCompatActivity {

    private static final String EXTRA_MUSEUM = "museum_intent";

    private Museum mMuseum;
    private LinearLayout mDotsLinearLayout;

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
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(mMuseum.getName());

        ViewPager pictures = findViewById(R.id.picture_vp);
        final MuseumPicturePagerAdapter adapter = new MuseumPicturePagerAdapter(this,
                mMuseum.getPicFolder());
        pictures.setAdapter(adapter);

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
    }
}
