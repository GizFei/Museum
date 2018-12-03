package com.giz.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

public class TestFragment extends Fragment {

    public String TAG = "Fragment";

    public String getTAG(){
        return TAG;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(getTAG(), "on attach");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(getTAG(), "on activity created");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(getTAG(), "on start");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(getTAG(), "on pause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(getTAG(), "on resume");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(getTAG(), "on stop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(getTAG(), "on destroy");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(getTAG(), "on destroy view");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(getTAG(), "on view created");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(getTAG(), "on detach");
    }
}
