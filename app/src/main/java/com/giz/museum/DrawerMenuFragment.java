package com.giz.museum;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DrawerMenuFragment extends Fragment {

    private static final String TAG = "DrawerMenuFragment";

    public static final String ITEM_INDEX = "INDEX";
//    public static final String ITEM_MUSEUMS = "MUSEUMS";
    public static final String ITEM_COLLECTION = "COLLECTION";
    public static final String ITEM_RECORD = "RECORD";
    public static final String ITEM_ABOUT = "ABOUT";

    public interface OnItemSelectedListener{
        void onItemSelected(View view, int pos);
    }

    private OnItemSelectedListener mItemSelectedListener;
    private LinearLayout mMenuLayout;

    public static DrawerMenuFragment newInstance(OnItemSelectedListener listener) {
        Bundle args = new Bundle();

        DrawerMenuFragment fragment = new DrawerMenuFragment();
        fragment.setOnItemSelectedListener(listener);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_drawer_menu, container, false);
        mMenuLayout = rootView.findViewById(R.id.drawer_menu_ll);
        Log.d(TAG, String.valueOf(mMenuLayout.getChildCount()) + " children");
        for(int i = 0; i < mMenuLayout.getChildCount(); i++){
            final int pos = i;
            mMenuLayout.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mItemSelectedListener != null){
//                        Toast.makeText(getContext(), "on listener click item " + String.valueOf(pos), Toast.LENGTH_SHORT).show();
                        setItemSelected(pos);
                        mItemSelectedListener.onItemSelected(v, pos);
                    }else{
                        Toast.makeText(getContext(), "click item " + String.valueOf(pos), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        Button settingBtn = rootView.findViewById(R.id.drawer_menu_setting);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 进入设置活动
                startActivity(new Intent(getContext(), SettingActivity.class));
            }
        });

        return rootView;
    }

    private void setItemSelected(int pos) {
        for(int i = 0; i < mMenuLayout.getChildCount(); i++) {
            TextView textView = (TextView)mMenuLayout.getChildAt(i);
            if(i == pos){
                // 选中项
                textView.setBackgroundResource(R.drawable.bg_drawer_menu);
                textView.setTextColor(0xFF48c4fa);
            }else{
                // 非选中项
                textView.setBackgroundColor(0xFFFFFFFF);
                textView.setTextColor(0xFFA6B6C6);
            }
        }
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener){
        mItemSelectedListener = listener;
    }
}
