package com.giz.utils;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.util.TypedValue;

public class DetailUtils {
    // 细节工具类

    /**
     * dp 转 px
     * @param context 上下文
     * @param value dp值
     * @return px值
     */
    public static float dp2px(Context context, float value){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics());
    }

    /**
     * 段落缩进
     * @param text 文本
     * @return 带缩进的文本
     */
    public static SpannableString createIndentText(String text){
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new LeadingMarginSpan.Standard(80, 0), 0, text.length(), 0);
        return spannableString;
    }
}
