package com.giz.customize;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

import com.giz.museum.R;

public class PopupMenu extends ViewGroup {

    private boolean isOpen = false;
    private int mDistanceBetweenView;

    private OnMenuItemClickListener mMenuItemClickListener;

    public PopupMenu(Context context) {
        this(context, null);
    }

    public PopupMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PopupMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mDistanceBetweenView = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
                getResources().getDisplayMetrics());

        initCustomAttributes(context, attrs, defStyleAttr);
    }

    private void initCustomAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PopupMenu, defStyleAttr, 0);

        mDistanceBetweenView = (int)a.getDimension(R.styleable.PopupMenu_distanceBetweenView,
                (float)mDistanceBetweenView);
        a.recycle();
    }

    public interface OnMenuItemClickListener{
        void onClick(View view);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(changed){
            for(int i = 0; i < getChildCount(); i++)
                layoutButton(i);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        for(int i = 0; i < getChildCount(); i++)
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener){
        mMenuItemClickListener = listener;
    }

    private void layoutButton(int i) {
        View view = getChildAt(i);
        view.setVisibility(GONE);

        int width = view.getMeasuredWidth();
        int height = view.getMeasuredHeight();

        int l = (getMeasuredWidth() - width) / 2;
        int t = getMeasuredHeight() - height * (i+1) - mDistanceBetweenView * i; // 16是两个控件之间的距离

        view.layout(l, t, l+width, t+height);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMenuItemClickListener != null)
                    mMenuItemClickListener.onClick(v);
                toggle();
            }
        });
    }

    public void toggle(){
        if(isOpen)
            fold();
        else
            unfold();
        isOpen = !isOpen;
    }

    /**
     * 收起
     */
    private void fold(){
        for(int i = 0; i < getChildCount(); i++){
            View view = getChildAt(i);
            view.setVisibility(GONE);

            TranslateAnimation translateAnimation = new TranslateAnimation(0, 0,
                    0 ,view.getMeasuredHeight()*(i+1)+mDistanceBetweenView*i);
            AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0.5f);

            AnimationSet animationSet = new AnimationSet(true);
            animationSet.addAnimation(translateAnimation);
            animationSet.addAnimation(alphaAnimation);
            animationSet.setDuration(300);
            animationSet.setFillAfter(true);
            animationSet.setInterpolator(new AccelerateDecelerateInterpolator());

            view.startAnimation(animationSet);
        }
    }

    /**
     * 展开
     */
    private void unfold(){
        for(int i = 0; i < getChildCount(); i++){
            View view = getChildAt(i);
            view.setVisibility(VISIBLE);

            TranslateAnimation translateAnimation = new TranslateAnimation(0, 0,
                    view.getMeasuredHeight()*(i+1)+mDistanceBetweenView*i ,0);
            AlphaAnimation alphaAnimation = new AlphaAnimation(0.5f, 1);

            AnimationSet animationSet = new AnimationSet(true);
            animationSet.addAnimation(translateAnimation);
            animationSet.addAnimation(alphaAnimation);
            animationSet.setDuration(400);
            animationSet.setInterpolator(new AccelerateDecelerateInterpolator());

            view.startAnimation(animationSet);
        }
    }
}
