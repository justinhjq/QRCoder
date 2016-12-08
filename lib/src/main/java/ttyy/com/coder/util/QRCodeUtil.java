package ttyy.com.coder.util;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

/**
 * Author: hjq
 * Date  : 2016/12/07 19:29
 * Name  : QRCodeUtil
 * Intro : Edit By hjq
 * Modification  History:
 * Date          Author        	 Version          Description
 * ----------------------------------------------------------
 * 2016/12/07    hjq   1.0              1.0
 */
public class QRCodeUtil {

    private QRCodeUtil(){}

    /**
     * dp转换为px
     * @param context
     * @param dpValue
     * @return
     */
    public static int dp2px(Context context, int dpValue){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    /**
     * sp转换为px
     * @param context
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, int spValue){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }

    /**
     * 获取屏幕可绘制区域的长宽
     * @param context
     * @return
     */
    public static Point getScreenDisplaySize(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        if(Build.VERSION.SDK_INT >= 13){
            display.getSize(point);
        }else {
            point = new Point(display.getWidth(), display.getHeight());
        }

        return point;
    }

}
