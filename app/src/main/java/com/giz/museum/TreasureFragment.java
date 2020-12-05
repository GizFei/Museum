package com.giz.museum;

import android.animation.FloatArrayEvaluator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
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
import android.widget.Toast;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HandshakeCompletedListener;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.QueryListener;

import static cn.bmob.v3.Bmob.getApplicationContext;

public class TreasureFragment extends TestFragment {
    private static final String ARGS_ID = "argsID";
    private static final String TAG = "TreasureFragment";

    private MuseumActivity mActivity;
    private Museum mMuseum;
    private TreasureAdapter mTreasureAdapter;
    private RecyclerView mRecyclerView;

    private List<Treasure> mTreasureList;

    private SensorManager mSensorManager;       // 传感器管理器
    private Sensor mSensor;
    private SoundPool soundPool;                // 音效池
    private int musicId;

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

    private void init_soundPool() {
        if(Build.VERSION.SDK_INT >= 21) {
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setMaxStreams(2);
            AudioAttributes.Builder attrbuilder = new AudioAttributes.Builder();
            builder.setAudioAttributes(attrbuilder.build());
            soundPool = builder.build();
        }
        else {
            soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC,0);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        init_soundPool();
        musicId = soundPool.load(getApplicationContext(), R.raw.shake,1);

        // 音频是否加载完成
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                CustomToast.make(getApplicationContext(), "试着摇一摇手机～", Toast.LENGTH_LONG).show();
            }
        });

        String id = getArguments().getString(ARGS_ID);
        mMuseum = MuseumLibrary.get().getMuseumById(id);

        mTreasureList = new ArrayList<>();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        mSensorManager.registerListener(mShakeListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        mSensorManager.unregisterListener(mShakeListener, mSensor);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 释放soundPool
        soundPool.release();
        soundPool = null;
    }

    SensorEventListener mShakeListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            int type = sensorEvent.sensor.getType();
            if (type == Sensor.TYPE_ACCELEROMETER) {
                float values[] = sensorEvent.values;
                float threshold = Math.abs(values[0]) + Math.abs(values[1]) + Math.abs(values[2]);
                if (threshold > 30) {
                    soundPool.play(musicId,1.0f,1.0f,1,0,1);

                    int itemCount = mRecyclerView.getAdapter().getItemCount();
                    List<Integer> list = new ArrayList<Integer>();
                    for (int i = 0; i < itemCount; i++)
                        list.add(new Integer(i));
                    // 顺序调整
                    Collections.shuffle(list);
                    for (int i = 0; i < itemCount; i++)
                        mRecyclerView.getAdapter().notifyItemMoved(i, list.get(i));

                    Log.d(TAG, Float.toString(threshold));
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_treasure, container, false);

        mRecyclerView = view.findViewById(R.id.treasure_detail_rv);

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

                    String treasureJsonFile = "https://museum-treasure.oss-cn-beijing.aliyuncs.com/" + mMuseum.getName() + "/Treasure/treasures.json";
                    JsonArrayRequest request1 = new JsonArrayRequest(treasureJsonFile,
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

    private JSONArray getJson(String jsonFilePath) throws JSONException {
        if (jsonFilePath == null) {
            return new JSONArray("");
        }
        File file = new File(jsonFilePath);
        BufferedReader br = null;
        StringBuilder json = new StringBuilder("");
        try {
            br = new BufferedReader(new FileReader(file));
            char[] line = new char[1024];
            int read = 0;
            while ((read = br.read(line, 0, 1024)) != -1) {
                json.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (br != null) {
            try {
                br.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.d(TAG, "getJson 馆藏jSON: " + json.toString());
        return new JSONArray(json.toString());
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

    public void toggleSensor(boolean state){
        if (state) {
            // 延迟监听时间可调
            if (mSensorManager != null)
                mSensorManager.registerListener(mShakeListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            // 取消注册
            if (mSensorManager != null)
                mSensorManager.unregisterListener(mShakeListener, mSensor);
        }
    }
}
