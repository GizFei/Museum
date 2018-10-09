package com.giz.museum;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Explode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.giz.utils.Museum;
import com.giz.utils.MuseumLib;

import java.util.ArrayList;
import java.util.List;

public class MuseumListActivity extends AppCompatActivity {

    // 列表显示样式：
    // true：列表形式，false：网格形式
    private boolean isListStyle = true;
    private MuseumAdapter mMuseumAdapter;
    private RecyclerView mMuseumRecyclerView;
    private ImageView mSwitchIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_museum);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(new Explode());
        getWindow().setReenterTransition(new Explode());

        mMuseumRecyclerView = findViewById(R.id.list_museum);
        mMuseumRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMuseumAdapter = new MuseumAdapter(MuseumLib.get(this).getMuseumList());
//        mMuseumRecyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this,
//                R.anim.layout_anim_from_bottom));
        mMuseumRecyclerView.setAdapter(mMuseumAdapter);

        final FloatingActionButton fab = findViewById(R.id.map_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityOptionsCompat compat = ActivityOptionsCompat.makeScaleUpAnimation(fab,
                        fab.getWidth()/2, fab.getHeight()/2, 0, 0);
                ActivityCompat.startActivity(MuseumListActivity.this,
                        new Intent(MuseumListActivity.this, MuseumMapActivity.class),
                        compat.toBundle());
            }
        });

        mSwitchIcon = findViewById(R.id.list_style);
        mSwitchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchRecyclerView();
            }
        });
    }

    /**
     * 切换列表显示样式
     */
    private void switchRecyclerView() {
        if(isListStyle){
            AnimatedVectorDrawableCompat listToGridAnim = AnimatedVectorDrawableCompat.create(this,
                    R.drawable.av_list_to_grid);
            mSwitchIcon.setImageDrawable(listToGridAnim);
            ((Animatable)mSwitchIcon.getDrawable()).start();
            isListStyle = false;
            mMuseumAdapter.setMuseumList(MuseumLib.get(MuseumListActivity.this).getMuseumList());
            mMuseumRecyclerView.setAdapter(mMuseumAdapter);
            mMuseumRecyclerView.startAnimation(mMuseumRecyclerView.getLayoutAnimation().getAnimation());
        }else{
            AnimatedVectorDrawableCompat gridToListAnim = AnimatedVectorDrawableCompat.create(this,
                    R.drawable.av_grid_to_list);
            mSwitchIcon.setImageDrawable(gridToListAnim);
            ((Animatable)mSwitchIcon.getDrawable()).start();
            isListStyle = true;
            mMuseumAdapter.setMuseumList(MuseumLib.get(MuseumListActivity.this).getMuseumList());
            mMuseumRecyclerView.setAdapter(mMuseumAdapter);
            mMuseumRecyclerView.startAnimation(mMuseumRecyclerView.getLayoutAnimation().getAnimation());
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
            startActivity(intent);
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
            if(isListStyle){
                return new MuseumHolder(inflater.inflate(R.layout.list_museum_item, viewGroup,
                        false));
            }else{
                return new MuseumHolder(inflater.inflate(R.layout.list_museum_item_grid, viewGroup,
                        false));
            }
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
