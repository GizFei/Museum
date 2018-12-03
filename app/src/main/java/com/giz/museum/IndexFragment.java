package com.giz.museum;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.giz.utils.TestFragment;

public class IndexFragment extends TestFragment {

    private static final String TAG = "IndexFragment";

    private RecyclerView mRecyclerView;
    private Toolbar mToolbar;
    private DrawerActivity mDrawerActivity;

    private IndexAdapter mIndexAdapter;

    public static IndexFragment newInstance() {
        Bundle args = new Bundle();
        
        IndexFragment fragment = new IndexFragment();
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_index, container, false);

        mToolbar = view.findViewById(R.id.index_toolbar);
        mRecyclerView = view.findViewById(R.id.index_rv);

        // 初始化Toolbar
        mToolbar.inflateMenu(R.menu.index_menu);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.index_museums:
                        Intent intent = new Intent(getActivity(), MuseumListActivityNew.class);
                        mDrawerActivity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(mDrawerActivity).toBundle());
                        return true;
                }
                return false;
            }
        });
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerActivity.openDrawerMenu();
            }
        });
        updateRecyclerView();
        return view;
    }

    /**
     * 更新列表视图
     */
    private void updateRecyclerView(){
        if(mIndexAdapter == null){
            mIndexAdapter = new IndexAdapter();
            mRecyclerView.setAdapter(mIndexAdapter);
        }else{
            mRecyclerView.setAdapter(mIndexAdapter);
        }
    }

    private class IndexHolder extends RecyclerView.ViewHolder{

        private IndexHolder(@NonNull View view) {
            super(view);
        }
    }

    private class IndexAdapter extends RecyclerView.Adapter<IndexHolder>{

        @NonNull
        @Override
        public IndexHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.card_item_index, viewGroup, false);
            return new IndexHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull IndexHolder indexHolder, int i) {

        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
