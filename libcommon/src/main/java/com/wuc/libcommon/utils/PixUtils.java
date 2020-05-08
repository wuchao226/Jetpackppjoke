package com.wuc.libcommon.utils;

import android.util.DisplayMetrics;

import com.wuc.libcommon.global.AppGlobals;

/**
 * @author: wuchao
 * @date: 2020-02-03 00:59
 * @desciption:
 */
public class PixUtils {
    /**
     * dp转为px
     */
    public static int dp2px(int dpValue) {
        DisplayMetrics metrics = AppGlobals.getApplication().getResources().getDisplayMetrics();
        return (int) (metrics.density * dpValue + 0.5f);
    }

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidth() {
        DisplayMetrics metrics = AppGlobals.getApplication().getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    /**
     * 获取屏幕高度
     */
    public static int getScreenHeight() {
        DisplayMetrics metrics = AppGlobals.getApplication().getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }
}
