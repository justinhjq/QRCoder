package ttyy.com.coder.scanner.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

/**
 * author: admin
 * date: 2017/03/14
 * version: 0
 * mail: secret
 * desc: CameraPermissionChecker
 */

public class CameraPermissionChecker {

    private CameraPermissionChecker() {

    }

    public static boolean hasCameraDevice(Context context){
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static boolean hasCameraPermission(Context context) {
        PackageManager pm = context.getPackageManager();
        return PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.CAMERA", context.getPackageName());
    }

    public static void checkCameraPermissionInteractive(Context context){

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle("提示");
        dialogBuilder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        if(!hasCameraDevice(context)){
            dialogBuilder.setMessage("您的设备没有相机!");
            dialogBuilder.create().show();
        }else if(!hasCameraPermission(context)){
            dialogBuilder.setMessage("您还没有授予App相机权限,去\"设置>隐私>相机\"设置一下吧");
            dialogBuilder.create().show();
        }
    }

    public static void checkCameraPermissionInteractive(Context context, Callback callback){
        if(!hasCameraDevice(context)){

            if(callback != null){
                callback.onCameraDevice(false);
            }

        }else if(!hasCameraPermission(context)){

            if(callback != null){
                callback.onCameraDevice(true);
                callback.onCameraPermission(false);
            }

        }else {

            if(callback != null){
                callback.onCameraDevice(true);
                callback.onCameraPermission(true);
            }
        }
    }

    public interface Callback{

        void onCameraDevice(boolean exist);

        void onCameraPermission(boolean grunted);
    }
}
