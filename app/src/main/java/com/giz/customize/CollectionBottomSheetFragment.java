package com.giz.customize;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.giz.bmob.CollectionDB;
import com.giz.bmob.MuseumLibrary;
import com.giz.bmob.StarMuseum;
import com.giz.museum.MuseumActivity;
import com.giz.museum.R;

import org.apache.http.cookie.SM;

import java.util.List;

public class CollectionBottomSheetFragment extends BottomSheetDialogFragment {

    private BottomSheetBehavior mBehavior;

//    @Override
//    @NonNull
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        BottomSheetDialog dialog = (BottomSheetDialog)super.onCreateDialog(savedInstanceState);
//        View view = View.inflate(getContext(), R.layout.collection_bottom_sheet, null);
//        dialog.setContentView(view);
//        mBehavior = BottomSheetBehavior.from((View)view.getParent());
//        mBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//            /**BottomSheetBehavior
//             * STATE_DRAGGING = 1;  拖动
//             * STATE_SETTLING = 2;  释放时
//             * STATE_EXPANDED = 3;  展开
//             * STATE_COLLAPSED = 4; 关闭为peekHeight高度
//             * STATE_HIDDEN = 5;    隐藏
//             * STATE_HALF_EXPANDED = 6;
//             * PEEK_HEIGHT_AUTO = -1;
//             */
//            @Override
//            public void onStateChanged(@NonNull View view, int i) {
//                Log.d("BottomSheet State", String.valueOf(i));
////                if(i == BottomSheetBehavior.STATE_SETTLING){
////                    mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
////                }
//                if(i == BottomSheetBehavior.STATE_HIDDEN){
//                    dismiss();
//                }
//            }
//
//            @Override
//            public void onSlide(@NonNull View view, float v) {
//                Log.d("BottomSheet", String.valueOf(v));
////                view.setAlpha((v+1)/2.0f);
//            }
//        });
//        return dialog;
//    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.collection_bottom_sheet, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.collection_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        TextView sum = view.findViewById(R.id.collection_num);
        CollectionAdapter adapter = new CollectionAdapter(getContext(),
                CollectionDB.get(getContext()).getStarredMuseums(), sum);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
//        mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private class CollectionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mLogo;
        private TextView mName;
        private TextView mAddress;
        private TextView mDate;

        private String mMuseumId;

        private CollectionHolder(View view){
            super(view);

            itemView.setOnClickListener(this);
            mLogo = itemView.findViewById(R.id.star_museum_logo);
            mName = itemView.findViewById(R.id.star_museum_name);
            mAddress = itemView.findViewById(R.id.star_museum_address);
            mDate = itemView.findViewById(R.id.star_museum_date);
        }

        private void bind(StarMuseum museum){
            try{
                mMuseumId = museum.getMuseumId();
                mLogo.setImageDrawable(MuseumLibrary.get().getMuseumById(museum.getMuseumId()).getLogo());
                mName.setText(museum.getName());
                mAddress.setText(museum.getAddress());
                mDate.setText(museum.getCollectionDate());
            }catch(Exception e){
                Toast.makeText(getContext(), "还未加载完成", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onClick(View v) {
            Intent intent = MuseumActivity.newIntent(getContext(), mMuseumId);
            startActivity(intent);
            dismiss();
        }
    }

    private class CollectionAdapter extends RecyclerView.Adapter<CollectionHolder>{

        private List<StarMuseum> mStarMuseumList;
        private Context mContext;

        private CollectionAdapter(Context context, List<StarMuseum> museums, TextView view){
            mStarMuseumList = museums;
            mContext = context;
            view.setText(String.valueOf(museums.size()));
        }

        @NonNull
        @Override
        public CollectionHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_collection,
                    viewGroup, false);
            return new CollectionHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CollectionHolder collectionHolder, int i) {
            collectionHolder.bind(mStarMuseumList.get(i));
        }

        @Override
        public int getItemCount() {
            return mStarMuseumList.size();
        }
    }
}
