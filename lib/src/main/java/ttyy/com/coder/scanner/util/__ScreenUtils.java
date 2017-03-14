package ttyy.com.coder.scanner.util;

import android.content.Context;

/**
 * author: admin
 * date: 2017/03/14
 * version: 0
 * mail: secret
 * desc: __ScreenUtils
 */

public class __ScreenUtils {

    private __ScreenUtils(){

    }

    public static int getScreenWidth(Context context){
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight(Context context){
        return context.getResources().getDisplayMetrics().heightPixels;
    }

}
