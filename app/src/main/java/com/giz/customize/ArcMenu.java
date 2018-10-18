package com.giz.customize;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.giz.museum.R;

public class ArcMenu extends ViewGroup implements View.OnClickListener{

    private static final int POS_LEFT_TOP = 1;
    private static final int POS_LEFT_BOTTOM = 2;
    private static final int POS_RIGHT_TOP = 3;
    private static final int POS_RIGHT_BOTTOM = 4;
    private static final int POS_RIGHT_CENTER = 5;

    private Position mPosition = Position.RIGHT_BOTTOM;
    private int mRadius;

    private Status mCurrentStatus = Status.CLOSE;
    private int mSubBtnCount;

    // 菜单的主按钮
    private View mCButton;
    private OnMenuItemClickListener mMenuItemClickListener;

    // 菜单的状态
    public enum Status{
        OPEN, CLOSE
    }
    // 菜单的位置枚举类
    public enum Position{
        LEFT_TOP, LEFT_BOTTOM, RIGHT_TOP, RIGHT_BOTTOM, RIGHT_CENTER
    }

    public ArcMenu(Context context) {
        this(context, null);
    }

    public ArcMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100,
                getResources().getDisplayMetrics()); // 100dp

        // 获取自定义属性的值
        getCustomAttrValue(context, attrs, defStyleAttr);

    }

    private void getCustomAttrValue(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ArcMenu,
                defStyleAttr, 0);
        int pos = a.getInt(R.styleable.ArcMenu_position, POS_RIGHT_TOP);
        switch (pos){
            case POS_LEFT_TOP:
                mPosition = Position.LEFT_TOP;
                break;
            case POS_LEFT_BOTTOM:
                mPosition = Position.LEFT_BOTTOM;
                break;
            case POS_RIGHT_TOP:
                mPosition = Position.RIGHT_TOP;
                break;
            case POS_RIGHT_BOTTOM:
                mPosition = Position.RIGHT_BOTTOM;
            case POS_RIGHT_CENTER:
                mPosition = Position.RIGHT_CENTER;
                break;
        }
        mRadius = (int)a.getDimension(R.styleable.ArcMenu_radius,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100,
                        getResources().getDisplayMetrics()));
        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        mSubBtnCount = getChildCount() - 1;

        if(changed){
            layoutCButton();

            double angle = 180.0 / (mSubBtnCount - 1);
            for(int i = 0; i < mSubBtnCount; i++)
                layoutSubButton(angle, i);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        for(int i = 0; i < count; i++){
            // 测量child
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    // 点击主菜单项的回调接口
    public interface OnMenuItemClickListener{
        void onClick(View view, int pos);
    }

    public void setMenuItemClickListener(OnMenuItemClickListener mMenuItemClickListener){
        this.mMenuItemClickListener = mMenuItemClickListener;
    }

    // 定位主菜单按钮
    private void layoutCButton(){
        mCButton = getChildAt(mSubBtnCount);
        mCButton.setOnClickListener(this);

        int l = 0;
        int t = 0;

        int width = mCButton.getMeasuredWidth();
        int height = mCButton.getMeasuredHeight();

        switch (mPosition) {
            case LEFT_TOP:
                l = 0;
                t = 0;
                break;
            case LEFT_BOTTOM:
                l = 0;
                t = getMeasuredHeight() - height;
                break;
            case RIGHT_TOP:
                l = getMeasuredWidth() - width;
                t = 0;
                break;
            case RIGHT_BOTTOM:
                l = getMeasuredWidth() - width;
                t = getMeasuredHeight() - height;
                break;
            case RIGHT_CENTER:
                l = getMeasuredWidth() - width;
                t = getMeasuredHeight() / 2 - height / 2;
                break;
        }

        // 定位
        mCButton.layout(l, t, l + width, t + height);
    }

    private void layoutSubButton(double angle, int index){
        View subButton = getChildAt(index);
        subButton.setVisibility(View.GONE);

        double mAngle = Math.toRadians(angle * index);

        int l = 0, t = 0;
        int width = subButton.getMeasuredWidth();
        int height = subButton.getMeasuredHeight();

        switch (mPosition){
            case LEFT_TOP:
                l = (int)(mRadius * Math.cos(mAngle));
                t = (int)(mRadius * Math.sin(mAngle));
                break;
            case LEFT_BOTTOM:
                l = (int)(mRadius * Math.sin(mAngle));
                t = getMeasuredHeight() - (int)(mRadius * Math.cos(mAngle)) - height;
                break;
            case RIGHT_TOP:
                l = getMeasuredWidth() - (int)(mRadius * Math.cos(mAngle)) - width;
                t = (int)(mRadius * Math.sin(mAngle));
                break;
            case RIGHT_BOTTOM:
                l = getMeasuredWidth() - (int)(mRadius * Math.sin(mAngle)) - width;
                t = getMeasuredHeight() - (int)(mRadius * Math.cos(mAngle)) - height;
                break;
            case RIGHT_CENTER:
                l = getMeasuredWidth() - (int)(mRadius * Math.sin(mAngle)) - width;
                if(index <= mSubBtnCount/2){
                    t = getMeasuredHeight()/2 - (int)(mRadius * Math.cos(mAngle)) - height/2;
                }else{
                    t = getMeasuredHeight()/2 - (int)(mRadius * Math.cos(mAngle)) - height/2;
                }
                break;
        }

        l -= 10;
        subButton.layout(l, t, l + width, t + height);
    }

    @Override
    public void onClick(View v) {
//        mCButton = findViewById(R.id.id_button);
//        if(mCButton == null){
//            mCButton = getChildAt(0);
//        }
        if(isOpen())
            rotateCButton(v, -180f, 0f, 300);
        else
            rotateCButton(v, 0f, -180f, 300);

        toggleMenu(300);
    }

    // 切换菜单
    public void toggleMenu(int duration){
        // 为menuItem添加平移动画和旋转动画
        double angele = Math.PI /(mSubBtnCount - 1);
        for(int i = 0; i < mSubBtnCount; i++) {
            final View childView = getChildAt(i);
            childView.setVisibility(VISIBLE);
            // end 0, 0
            // start
            int cl = 0;
            int ct = 0;

            if(mPosition == Position.LEFT_TOP || mPosition == Position.RIGHT_TOP){
                cl = (int) (mRadius * Math.cos(angele * i));
                ct = (int) (mRadius * Math.sin(angele * i));
            }else{
                cl = (int) (mRadius * Math.sin(angele * i));
                ct = (int) (mRadius * Math.cos(angele * i));
            }
//            if(mPosition == Position.RIGHT_CENTER){
//                cl = (int) (mRadius * Math.sin(angele * i));
//                if(i < mSubBtnCount/2){
//                    ct = (int) (mRadius * Math.cos(angele * i));
//                }else{
//                    ct = (int) (mRadius * Math.cos(angele * i));
//                }
//            }

            int xflag = 1;
            int yflag = 1;

            if (mPosition == Position.LEFT_TOP || mPosition == Position.LEFT_BOTTOM)
                xflag = -1;
            if (mPosition == Position.LEFT_TOP || mPosition == Position.RIGHT_TOP)
                yflag = -1;
            if(mPosition == Position.RIGHT_CENTER){
                xflag = 1;
                yflag = 1;
            }

            AnimationSet animSet = new AnimationSet(true);
            Animation tranAnim = null;

            // to open
            if (mCurrentStatus == Status.CLOSE) {
                tranAnim = new TranslateAnimation(xflag * cl, 0,
                        yflag * ct, 0);
                childView.setClickable(true);
                childView.setFocusable(true);
            } else { // to close
                tranAnim = new TranslateAnimation(0, xflag * cl,
                        0, yflag * ct);
                childView.setClickable(false);
                childView.setFocusable(false);
            }
            tranAnim.setFillAfter(true);
            tranAnim.setDuration(duration);

            tranAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if(mCurrentStatus == Status.CLOSE)
                        childView.setVisibility(GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            RotateAnimation rotateAnim = new RotateAnimation(0, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnim.setDuration(duration);
            rotateAnim.setFillAfter(true);

            // 注意添加的顺序，要先旋转动画
            //animSet.addAnimation(rotateAnim);
            animSet.addAnimation(tranAnim);

            childView.startAnimation(animSet);

            final int pos = i;
            childView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mMenuItemClickListener != null)
                        mMenuItemClickListener.onClick(childView, pos);
                    menuItemAnim(pos);
                    changeStatus();
                }
            });
        }

        // 切换菜单状态
        changeStatus();

    }

    public void fold(){
        for(int i = 0; i < mSubBtnCount; i++){
            View view =  getChildAt(i);
            view.setVisibility(GONE);
        }

        if(isOpen())
            rotateCButton(mCButton, -180f, 0f, 300);
        toggleMenu(300);
//        changeStatus();
    }

    public boolean isOpen(){
        return mCurrentStatus == Status.OPEN;
    }

    // i珈menuItem的点击动画
    private void menuItemAnim(int pos) {
        for(int i = 0; i < mSubBtnCount; i++){
            View childView = getChildAt(i);
            if(i == pos){
                childView.startAnimation(scaleBigAnim(300));
            }else{
                childView.startAnimation(scaleSmallAnim(300));
            }

            childView.setClickable(false);
            childView.setFocusable(false);
        }
    }

    private Animation scaleSmallAnim(int duration) {

        AnimationSet animationSet = new AnimationSet(true);

        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f,
                0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0.0f);

        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);

        animationSet.setDuration(duration);
        animationSet.setFillAfter(true);

        return animationSet;
    }

    private Animation scaleBigAnim(int duration) {
        AnimationSet animationSet = new AnimationSet(true);

        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 2.0f, 1.0f,
                2.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0.0f);

        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);

        animationSet.setDuration(duration);
        animationSet.setFillAfter(true);

        return animationSet;
    }

    private void changeStatus() {
        mCurrentStatus = (mCurrentStatus == Status.CLOSE) ? Status.OPEN : Status.CLOSE;
    }

    private void rotateCButton(View v, float start, float end, int duration) {
        RotateAnimation anim = new RotateAnimation(start, end, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(duration);
        anim.setFillAfter(true);
        v.startAnimation(anim);
    }
}
