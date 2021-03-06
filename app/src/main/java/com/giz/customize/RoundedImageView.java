package com.giz.customize;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.giz.museum.R;


public class RoundedImageView extends AppCompatImageView {

    // 圆形，圆角，默认
    private static int TYPE_CIRCLE = 1;
    private static int TYPE_CORNER = 2;
    private static int TYPE_DEFAULT = 0;

    private int mType;    // 类型
    private float mRadius;   // 圆角半径
    private Paint mPaint;

    public RoundedImageView(Context context) {
        this(context, null);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initStyles(context, attrs, defStyleAttr);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    }

    private void initStyles(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView, defStyleAttr, 0);

        mType = array.getInt(R.styleable.RoundedImageView_type, TYPE_DEFAULT);
        mRadius = array.getDimension(R.styleable.RoundedImageView_radius, dp2px(8f));

        array.recycle();
    }

    private float dp2px(float value){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 当模式为圆形模式的时候，我们强制让宽高一致
        if(mType == TYPE_CIRCLE){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int result = Math.min(getMeasuredWidth(), getMeasuredHeight());
            setMeasuredDimension(result, result);
        }else{
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        Matrix matrix = getImageMatrix();

        if(drawable == null){
            return;
        }
        if(drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0){
            return;
        }
        if(matrix == null && getPaddingTop() == 0 && getPaddingLeft() == 0){
            drawable.draw(canvas);
        }else{
            final int saveCount = canvas.getSaveCount();
            canvas.save();

            if (getCropToPadding()) {
                final int scrollX = getScrollX();
                final int scrollY = getScrollY();
                canvas.clipRect(scrollX + getPaddingLeft(), scrollY + getPaddingTop(),
                        scrollX + getRight() - getLeft() - getPaddingRight(),
                        scrollY + getBottom() - getTop() - getPaddingBottom());
            }

            canvas.translate(getPaddingLeft(), getPaddingTop());
            if(mType == TYPE_CIRCLE){
                Bitmap bitmap = drawable2Bitmap(drawable);
                mPaint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
                canvas.drawCircle(getWidth()/2, getHeight()/2, getWidth()/2, mPaint);
            }else if(mType == TYPE_CORNER){
                Bitmap bitmap = drawable2Bitmap(drawable);
                mPaint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
                canvas.drawRoundRect(new RectF(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(),
                        getHeight() - getPaddingBottom()), mRadius, mRadius, mPaint);
            }else{
                if(matrix != null){
                    canvas.concat(matrix);
                }
                drawable.draw(canvas);
            }
            canvas.restoreToCount(saveCount);
        }
    }

    /**
     * drawable转换成bitmap
     */
    private Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        //根据传递的scaletype获取matrix对象，设置给bitmap
        Matrix matrix = getImageMatrix();
        if (matrix != null) {
            canvas.concat(matrix);
        }
        drawable.draw(canvas);
        return bitmap;
    }
}
