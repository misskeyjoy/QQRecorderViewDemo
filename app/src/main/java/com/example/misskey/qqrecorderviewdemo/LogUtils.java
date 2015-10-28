package com.example.misskey.qqrecorderviewdemo;

import android.util.Log;

/**
 * Created by misskey on 15-10-28.
 */
public final class LogUtils {
    private static  String TAG="QQRecodrerViewDemo";
    public static void loge(int m){
        Log.e(TAG,m+"");
    }
    public static void loge(String tips){
        Log.e(TAG,tips);
    }
    public static void loge(double x){
        Log.e(TAG,x+"");
    }
    public static void loge(float x){
        Log.e(TAG,x+"");
    }
}
