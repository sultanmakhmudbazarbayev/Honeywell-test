package com.honeywell.bccrfid.utils;

import android.content.Context;

public class DensityUtil {

    public static float dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dpValue * scale;
    }

    public static float px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return pxValue / scale;
    }

}  
