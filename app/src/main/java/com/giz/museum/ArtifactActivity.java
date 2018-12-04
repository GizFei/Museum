package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class ArtifactActivity extends AppCompatActivity {

    private static final String TAG = "ArtifactActivity";
    private static final String EXTRA_ID = "museumId";

    private RecyclerView mRecyclerView;
    private ArtifactAdapter mAdapter;

    private String mMuseumId;

    public static Intent newIntent(Context context, String museumId){
        Intent intent = new Intent(context, ArtifactActivity.class);
        intent.putExtra(EXTRA_ID, museumId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artifact);

        mRecyclerView = findViewById(R.id.artifact_rv);

        mMuseumId = getIntent().getStringExtra(EXTRA_ID); // 获得ID
        mAdapter = new ArtifactAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }

    private class ArtifactHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ArtifactHolder(View view){
            super(view);

        }

        private void bind(){
        }

        @Override
        public void onClick(View v) {
        }
    }

    private class ArtifactAdapter extends RecyclerView.Adapter<ArtifactHolder>{

        private static final int TYPE_LEFT = 0;
        private static final int TYPE_RIGHT = 1;

        private LayoutInflater mInflater;

        private ArtifactAdapter(){
            mInflater = LayoutInflater.from(ArtifactActivity.this);
        }

        @NonNull
        @Override
        public ArtifactHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return i == TYPE_LEFT ? new ArtifactHolder(mInflater.inflate(R.layout.list_item_artifact,
                    viewGroup, false)) : new ArtifactHolder(mInflater.inflate(R.layout.list_item_artifact_reverse,
                    viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ArtifactHolder artifactHolder, int i) {
            artifactHolder.bind();
        }

        @Override
        public int getItemCount() {
            return 4;
        }

        @Override
        public int getItemViewType(int position) {
            return position % 2 == 0 ? TYPE_LEFT : TYPE_RIGHT;
        }
    }
}
