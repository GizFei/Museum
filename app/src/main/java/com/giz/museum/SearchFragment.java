package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.giz.database.Museum;
import com.giz.database.MuseumLibrary;
import com.giz.utils.TestFragment;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends TestFragment {

    private static final String TAG = "SearchFragment";

    private RecyclerView mRecyclerView;
    private MuseumAdapter mAdapter;
    private Context mContext;

    public static SearchFragment newInstance() {
        Bundle args = new Bundle();

        SearchFragment fragment = new SearchFragment();
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
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container,false);
        mRecyclerView = view.findViewById(R.id.search_list);
        updateRecyclerView("");

        return view;
    }

    public void updateRecyclerView(String query){
        if(mRecyclerView != null){
            if(mAdapter == null){
                mAdapter = new MuseumAdapter(MuseumLibrary.get().queryMuseumsByWord(query));
                mRecyclerView.setAdapter(mAdapter);
            }else{
                mAdapter.setMuseumList(MuseumLibrary.get().queryMuseumsByWord(query));
                mAdapter.notifyDataSetChanged();
            }
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
            mMuseumName = itemView.findViewById(R.id.index_museum_name);
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
                    mMuseumCatalogs.get(i).setVisibility(View.VISIBLE);
                    mMuseumCatalogs.get(i).setText(museum.getCatalog().get(i));
                }else{
                    mMuseumCatalogs.get(i).setVisibility(View.GONE);
                }
            }
            mMuseumLogo.setImageDrawable(museum.getLogo());
        }

        @Override
        public void onClick(View v) {
            Intent intent = MuseumActivity.newIntent(mContext,
                    mMuseum.getMuseumId());
            ActivityOptionsCompat compat = ActivityOptionsCompat.makeCustomAnimation(
                    mContext, R.anim.activity_in, R.anim.activity_out);
            ActivityCompat.startActivity(mContext, intent, compat.toBundle());
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
            LayoutInflater inflater = LayoutInflater.from(mContext);
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
