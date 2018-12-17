package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.giz.customize.CustomToast;
import com.giz.database.Museum;
import com.giz.database.MuseumLibrary;
import com.giz.utils.HttpSingleTon;
import com.giz.utils.TestFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;

public class TreasureFragment extends TestFragment {
    private static final String ARGS_ID = "argsID";
    private static final String TAG = "TreasureFragment";

    private MuseumActivity mActivity;
    private Museum mMuseum;
    private TextView mArtifactBtn;
    private TextView mCreativeBtn;
    private TreasureAdapter mTreasureAdapter;
    private RecyclerView mRecyclerView;

    private List<Treasure> mTreasureList;

    public static TreasureFragment newInstance(String museumId) {
        Bundle args = new Bundle();
        args.putString(ARGS_ID, museumId);

        TreasureFragment fragment = new TreasureFragment();
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
        mActivity = (MuseumActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String id = getArguments().getString(ARGS_ID);
        mMuseum = MuseumLibrary.get().getMuseumById(id);

        mTreasureList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_treasure, container, false);

        mArtifactBtn = view.findViewById(R.id.treasure_artifact_btn);
        mCreativeBtn = view.findViewById(R.id.treasure_creative_btn);
        mRecyclerView = view.findViewById(R.id.treasure_detail_rv);

        mArtifactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 文物");
                startActivity(ArtifactActivity.newIntent(mActivity, mMuseum.getMuseumId()));
            }
        });
        mCreativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 文创");
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        BmobQuery query = new BmobQuery("detail");
        query.addQueryKeys("treasure");
        query.addWhereEqualTo("museumId", mMuseum.getMuseumId());
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray array, BmobException e) {
                try {
                    Log.d(TAG, array.toString(4));
                    JSONObject treasureFile = array.getJSONObject(0).getJSONObject("treasure");
                    JsonArrayRequest request1 = new JsonArrayRequest(treasureFile.getString("url"),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    updateGridLayout(response);
                                }
                            }, null);
                    HttpSingleTon.getInstance(mActivity).addToRequestQueue(request1);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                    CustomToast.make(mActivity, "数据丢了...").show();
                }
            }
        });
    }

    private void updateGridLayout(JSONArray response) {
        try {
            Log.d(TAG, "treasure array" + response.toString(4).getBytes().length);
            for (int i = 0; i < response.length(); i++) {
                Treasure treasure = new Treasure(response.getJSONObject(i));
                Log.d(TAG, "updateGridLayout: " + treasure.toJsonString());
                mTreasureList.add(treasure);
            }
            if (mTreasureAdapter == null) {
                mTreasureAdapter = new TreasureAdapter();
                mRecyclerView.setAdapter(mTreasureAdapter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            CustomToast.make(mActivity, "更新失败").show();
        }
    }

    private class TreasureHolder extends RecyclerView.ViewHolder {

        private ImageView mTreasureImgView;

        private TreasureHolder(View view) {
            super(view);
            mTreasureImgView = itemView.findViewById(R.id.grid_treasure_img);
        }

        private void bind(final Treasure treasure) {
            mTreasureImgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = TreasureDetailActivity.newIntent(mActivity, treasure.toJsonString());
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            mActivity, mTreasureImgView, getResources().getString(R.string.image_trans));
                    startActivity(intent, optionsCompat.toBundle());
                }
            });
            ImageRequest request = new ImageRequest(treasure.treasureUrl, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    mTreasureImgView.setImageBitmap(response);
                }
            }, 0, 0, ImageView.ScaleType.CENTER_INSIDE, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "ImageRequest error");
                    mTreasureImgView.setImageResource(R.drawable.treasure_eg);
                }
            });
            HttpSingleTon.getInstance(mActivity).addToRequestQueue(request);
        }
    }

    private class TreasureAdapter extends RecyclerView.Adapter<TreasureHolder> {

        private LayoutInflater mInflater;

        private TreasureAdapter() {
            mInflater = LayoutInflater.from(mActivity);
        }

        @NonNull
        @Override
        public TreasureHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new TreasureHolder(mInflater.inflate(R.layout.list_item_treasure_grid, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull TreasureHolder treasureHolder, int i) {
            treasureHolder.bind(mTreasureList.get(i));
        }

        @Override
        public int getItemCount() {
            return mTreasureList.size();
        }
    }

    private class Treasure {
        String treasureName = "";
        String treasureUrl = "";
        String treasureIntro = "";

        private Treasure(JSONObject object) {
            try {
                treasureName = object.getString("name");
                treasureUrl = object.getString("url");
                if (object.has("intro"))
                    treasureIntro = object.getString("intro");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private String toJsonString() {
            JSONObject object = new JSONObject();
            try {
                object.put("name", treasureName);
                object.put("url", treasureUrl);
                object.put("intro", treasureIntro);
                return object.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                return "";
            }
        }
    }
}
