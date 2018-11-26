package com.giz.museum;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class IndexDrawerFragment extends Fragment {

    private static final String TAG = "IndexDrawerFragment";

    public interface OnItemSelectedListener{
        void onItemSelected(View view, int pos);
    }

    private OnItemSelectedListener mItemSelectedListener;
    private LinearLayout mMenuLayout;

    public static IndexDrawerFragment newInstance(OnItemSelectedListener listener) {
        Bundle args = new Bundle();

        IndexDrawerFragment fragment = new IndexDrawerFragment();
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

        View rootView = inflater.inflate(R.layout.fragment_index_drawer, container, false);
        mMenuLayout = rootView.findViewById(R.id.drawer_menu_ll);
        Log.d(TAG, String.valueOf(mMenuLayout.getChildCount()) + " children");
        for(int i = 0; i < mMenuLayout.getChildCount(); i++){
            final int pos = i;
            mMenuLayout.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mItemSelectedListener != null){
                        Toast.makeText(getContext(), "on listener click item " + String.valueOf(pos), Toast.LENGTH_SHORT).show();
                        setItemSelected(pos);
                        mItemSelectedListener.onItemSelected(v, pos);
                    }else{
                        Toast.makeText(getContext(), "click item " + String.valueOf(pos), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        return rootView;
    }

    private void setItemSelected(int pos) {
        for(int i = 0; i < mMenuLayout.getChildCount(); i++) {
            TextView textView = (TextView)mMenuLayout.getChildAt(i);
            if(i == pos){
                // 选中项
                textView.setBackgroundResource(R.drawable.drawer_menu_bg);
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
