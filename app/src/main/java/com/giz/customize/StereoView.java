package com.giz.customize;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;
import android.widget.Toast;


/**
 * 自定义控件 3D旋转容器
 * 参考 blog.csdn.net/Mr_immortalZ/article/details/51918560
 * 进行实现
 */
public class StereoView extends ViewGroup {

    public enum State { Normal, Pre, Next }
    private State mState = State.Normal;
    private boolean bindListener = false;           // 由于新增View所以需要在layout绑定监听1次

    private static final String TAG = "StereoView";
    private static final int startChild = 1;        // 开始时的child位置
    private static final float angleItem = 90;      // 两个item间的夹角
    private static final float resistance = 2.f;    // 滑动阻力
    private static final int speedThreshold = 2000; // Y向上的速度判断限制
    private static final int flingSpeed = 800;      // 控制滑动数目敏感程度

    private Context mContext;
    private Camera mCamera;
    private Matrix mMatrix;
    private Scroller mScroller;     // 滚动器
    private int mWidth;             // 容器的宽度
    private int mHeight;            // 容器的高度
    private int mCurItem = 1;       // 记录当前item
    private StereoListener mStereoListener;

    private VelocityTracker mVelocityTracker;     // 滑动速度跟踪
    private int mTouchSlop;
    private boolean isSliding = false;
    private boolean isAdding = false;      //fling时正在添加新页面，在绘制时不需要开启camera绘制效果，否则页面会有闪动
    private float mDownX, mDownY;
    private int addCount;           // 手离开屏幕后，需要新增的页面数
    private int alreadyAdd = 0;     // 滑动多页时已经新增页面数


    public StereoView(Context context) {
        this(context, null);
    }

    public StereoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StereoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init(mContext);
    }

    private void init(Context context) {
        // ViewConfiguration滑动参数设置类
        // getScaledTouchSlop是一个距离，表示滑动的时候，手的移动要大于这个距离才开始移动控件
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        mCamera = new Camera();
        mMatrix = new Matrix();
        if (mScroller == null) {
            mScroller = new Scroller(context);
        }
    }


    /*************************************** ViewGroup 布局 ***************************************/
    /**
     * 布局所有的view，会循环调用
     * 放置父控件的矩形可用空间（除去margin和padding的空间）
     * @param b 判断View的大小与位置是否发生了改变
     * @param i,i1,i2,i3 左上右下
     */
    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        int top = 0;

        for (int index=0; index<getChildCount(); index++) {
            // Returns the view at the specified position in the group
            View child = getChildAt(index);
            if (child.getVisibility() != GONE) {
                // 传入的是10, 10, 100, 100，则该View在距离父控件的左上角位置(10, 10)处显示，显示的大小是宽高是90
                child.layout(0, top, child.getMeasuredWidth(), child.getMeasuredHeight() + top);
                top += child.getMeasuredHeight();
            }
        }
        Log.d(TAG, "onLayout: "+Integer.toString(getChildCount()));
        /*
        // 绑定监听
        if (bindListener) {
            for (int index=0;index<getChildCount();index++) {
                View child = getChildAt(index);
                child.setOnClickListener(new itemListener(index));
            }
            bindListener = false;
        }
        */
    }
    /**
     * 当父控件要放置子控件时，父控件会调用子控件的onMeasure方法询问子控件，在onLayout()之前
     * @param widthMeasureSpec,heightMeasureSpec 子控件可获得的空间
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 遍历所有的子view去测量自己（跳过GONE类型View），不加这段代码，不会出现子View
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        // 获取宽和高
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        // 滑动到设置的startChild位置
        scrollTo(0, startChild * mHeight);
        Log.d(TAG, "onMeasure: ");
    }
    /*************************************** ViewGroup 布局 ***************************************/


    /***************************************监听事件处理的部分***************************************/
    // 只执行一次，直接绑定监听
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d(TAG, "onFinishInflate: ");
        Log.d(TAG, Integer.toString(getChildCount()));
        bindListener = true;
    }
    /*
    // 可以添加更多方法和属性
    public class itemListener implements View.OnClickListener {
        private int id;

        private itemListener(int i) {
            this.id = i;
            Log.d(TAG, Integer.toString(i));
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(getContext(), Integer.toString(id), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onClick: "+id);
        }
    }
    */
    /***************************************监听事件处理的部分***************************************/


    /***************************************触摸事件的传递过程***************************************/
    /**
     * Pass the touch screen motion event down to the target view, or this view if it is the target
     * @param ev The motion event to be dispatched
     * @return True if the event was handled by the view, false otherwise
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isSliding = false;
                mDownX = x;
                mDownY = y;
                // 滑动没有结束时再次点击，强制滑动结束
                if (!mScroller.isFinished()) {
                    // 设置mScroller最终停留的竖直位置，直接跳到目标位置
                    mScroller.setFinalY(mScroller.getCurrY());
                    // 停止动画并滑动到指定位置
                    mScroller.abortAnimation();
                    scrollTo(0, getScrollY());
                    isSliding = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isSliding)
                    isSliding = judgeSliding(ev);
                break;
            default:
                break;
        }

        return super.dispatchTouchEvent(ev);
    }
    // 拦截TouchEvent
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isSliding;
    }
    // 处理TouchEvent
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                if (isSliding) {
                    int delta = (int)(mDownY - y);
                    mDownY = y;
                    if (mScroller.isFinished()) {
                        // 当为滚动式，启动滚动
                        moveView(delta);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isSliding) {
                    isSliding = false;
                    // 一秒内运动了多少个像素
                    mVelocityTracker.computeCurrentVelocity(1000);
                    float yVelocity = mVelocityTracker.getYVelocity();
                    // 滑动的速度大于规定的速度，或者向上滑动的高度超过1/2
                    if (yVelocity > speedThreshold || ((getScrollY() + mHeight/2)/mHeight < startChild)) {
                        mState = State.Pre;
                    }
                    else if (yVelocity < -speedThreshold || ((getScrollY() + mHeight/2)/mHeight > startChild)) {
                        mState = State.Next;
                    }
                    else {
                        mState = State.Normal;
                    }
                    // 根据mState进行相应的变化
                    scrollByState(yVelocity);
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }

        return super.onTouchEvent(event);
    }
    /***************************************触摸事件的传递过程***************************************/


    private void toNormal() {
        int startY, deltaY, duration;
        addCount = 0;
        startY = getScrollY();
        deltaY = mHeight * startChild - getScrollY();
        duration = (Math.abs(deltaY)) * 4;
        mScroller.startScroll(0, startY, 0, deltaY, duration);
    }

    private void toPre(float yVelocity) {
        int startY, deltaY, duration;

        addPre();

        // 计算松手后滑动的item个数
        int flingSpeedCount = (yVelocity - speedThreshold) > 0 ? (int) (yVelocity - speedThreshold) : 0;
        addCount = flingSpeedCount / flingSpeed + 1;
        // mScroller开始的坐标
        startY = getScrollY() + mHeight;
        setScrollY(startY);
        // mScroller移动的距离
        deltaY = -(startY - startChild * mHeight) - (addCount - 1) * mHeight;
        duration = (Math.abs(deltaY)) * 3;
        mScroller.startScroll(0, startY, 0, deltaY, duration);
        addCount--;
    }

    private void toNext(float yVelocity) {
        int startY, deltaY, duration;

        addNext();

        int flingSpeedCount = (Math.abs(yVelocity) - speedThreshold) > 0 ? (int) (Math.abs(yVelocity) - speedThreshold) : 0;
        addCount = flingSpeedCount / flingSpeed + 1;
        startY = getScrollY() - mHeight;
        setScrollY(startY);
        deltaY = mHeight * startChild - startY + (addCount - 1) * mHeight;
        duration = (Math.abs(deltaY)) * 3;
        mScroller.startScroll(0, startY, 0, deltaY, duration);
        addCount--;
    }

    private void scrollByState(float yVelocity) {
        alreadyAdd = 0; // 重置滑动多页时的计数
        if (getScrollY() != mHeight) {
            switch (mState) {
                case Normal:
                    toNormal();
                    break;
                case Pre:
                    toPre(yVelocity);
                    break;
                case Next:
                    toNext(yVelocity);
                    break;
            }
            // 请求重新绘制
            invalidate();
        }
    }

    // 判断是否能够滑动
    private boolean judgeSliding(MotionEvent ev) {
        float x = ev.getX(), y = ev.getY();
        if (Math.abs(y - mDownX) > mTouchSlop && (Math.abs(y - mDownY) > (Math.abs(x - mDownX)))) {
            return true;
        }

        return false;
    }

    // 开始滚动，参数为手指滑动距离
    private void moveView(int delta) {
        delta = (int)((delta % mHeight) / resistance);
        // 滑动太短不进行滑动
        if (Math.abs(delta) > mHeight / 4)
            return;

        scrollBy(0, delta);
        if (getScrollY() < 5 && startChild != 0) {
            addPre();
            scrollBy(0, mHeight);
        }
        else if (getScrollY() > (getChildCount() - 1) * mHeight - 5) {
            addNext();
            scrollBy(0, -mHeight);
        }
    }

    private void addNext() {
        mCurItem = (mCurItem + 1) % getChildCount();
        int childCount = getChildCount();
        View view = getChildAt(0);
        removeViewAt(0);
        addView(view, childCount - 1);
        if (mStereoListener != null) {
            mStereoListener.toNext(mCurItem);
        }
    }

    private void addPre() {
        mCurItem = ((mCurItem - 1) + getChildCount()) % getChildCount();
        int childCount = getChildCount();
        View view = getChildAt(childCount - 1);
        removeViewAt(childCount - 1);
        addView(view, 0);
        if (mStereoListener != null) {
            mStereoListener.toPre(mCurItem);
        }
    }

    @Override
    public void computeScroll() {
        // 滑动没有结束时
        if (mScroller.computeScrollOffset()) {
            if (mState == State.Pre) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY() + mHeight * alreadyAdd);
                if (getScrollY() < (mHeight + 2) && addCount > 0) {
                    isAdding = true;
                    addPre();
                    alreadyAdd++;
                    addCount--;
                }
            } else if (mState == State.Next) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY() - mHeight * alreadyAdd);
                if (getScrollY() > (mHeight) && addCount > 0) {
                    isAdding = true;
                    addNext();
                    addCount--;
                    alreadyAdd++;
                }
            } else {
                //mState == State.Normal状态
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }
            postInvalidate();
        }
        // 滑动结束时
        if (mScroller.isFinished()) {
            alreadyAdd = 0;
            addCount = 0;
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (!isAdding) {
            // 如果不做这个判断，addPre() 或者addNext()时页面会进行闪动一下
            for (int i = 0; i < getChildCount(); i++) {
                draw3D(canvas, i, getDrawingTime());
            }
        } else {
            isAdding = false;
            super.dispatchDraw(canvas);
        }

    }

    // 进行3D绘制
    private void draw3D(Canvas canvas, int i, long drawingTime) {
        int curScreenY = mHeight * i;
        // 不显示的部分不进行绘制
        if (getScrollY() + mHeight < curScreenY)
            return;
        if (curScreenY < getScrollY() - mHeight)
            return;

        float centerX = mWidth / 2;
        float centerY = (getScrollY() > curScreenY) ? curScreenY + mHeight : curScreenY;
        float degree = angleItem * (getScrollY() - curScreenY) / mHeight;
        if (degree > 90 || degree < -90) {
            return;
        }
        canvas.save();

        mCamera.save();
        mCamera.rotateX(degree);
        mCamera.getMatrix(mMatrix);
        mCamera.restore();

        mMatrix.preTranslate(-centerX, -centerY);
        mMatrix.postTranslate(centerX, centerY);
        canvas.concat(mMatrix);
        drawChild(canvas, getChildAt(i), drawingTime);
        canvas.restore();
    }

    // 对外接口
    public interface StereoListener {
        void toPre(int curItem);
        void toNext(int curItem);
    }

    public void setStereoListener(StereoListener mStereoListener) {
        this.mStereoListener = mStereoListener;
    }
}
