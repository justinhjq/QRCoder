package ttyy.com.coder.scanner;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import ttyy.com.coder.scanner.camera.CameraPreviewView;
import ttyy.com.coder.scanner.decode.DecodeDataTask;

/**
 * Author: Administrator
 * Date  : 2016/12/08 14:05
 * Name  : ScannerView
 * Intro : Edit By Administrator
 * Modification  History:
 * Date          Author        	 Version          Description
 * ----------------------------------------------------------
 * 2016/12/08    Administrator   1.0              1.0
 */
public abstract class ScannerParentView extends FrameLayout implements DecodeDataTask.DecodeTracer, Camera.PreviewCallback {

    Camera mCamera;
    CameraPreviewView mCameraPreviewView;
    ScanRectView mScanRectView;
    DecodeDataTask mDecodeDataTask;
    DecodeStatusListener mDecodeStatusListener;

    protected Handler mHandler;
    boolean mIsDecodeable;

    public ScannerParentView(Context context) {
        this(context, null);
    }

    public ScannerParentView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScannerParentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initWidgets(context, attrs);
    }

    /**
     * 初始化控件
     * @param context
     * @param attrs
     */
    private void initWidgets(Context context, AttributeSet attrs) {
        mCameraPreviewView = new CameraPreviewView(context, attrs);
        mScanRectView = new ScanRectView(context, attrs);

        addView(mCameraPreviewView);
        addView(mScanRectView);
    }


    @Override
    public void onPreviewFrame(final byte[] bytes, final Camera camera) {
        if (mIsDecodeable) {
            cancelProcessDataTask();
            mDecodeDataTask = new DecodeDataTask(camera, bytes, this) {
                @Override
                protected void onPostExecute(String result) {
                    if (mIsDecodeable) {
                        // 取到数据了，那么就不用继续拿Camera的预览图像了
                        if (mDecodeStatusListener != null && !TextUtils.isEmpty(result)) {
                            try {
                                mDecodeStatusListener.onDecodeSuccess(result);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                // 没有解析到数据，继续取Camera的预览
                                mCamera.setOneShotPreviewCallback(ScannerParentView.this);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }.start();
        }
    }

    /**
     * 取消正在解析的任务
     */
    protected void cancelProcessDataTask() {
        if (mDecodeDataTask != null) {
            mDecodeDataTask.cancelTask();
            mDecodeDataTask = null;
        }
    }

    /**
     * 设置状态监听
     * @param listener
     */
    public void setDecodeStatusListener(DecodeStatusListener listener){
        this.mDecodeStatusListener = listener;
    }

    /**
     * 打开指定摄像头开始预览
     * @param cameraFacing
     */
    public void openCamera(int cameraFacing) {
        if (mCamera != null) {
            return;
        }
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++) {
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.facing == cameraFacing) {
                try {
                    mCamera = Camera.open(cameraId);
                    mCameraPreviewView.setCamera(mCamera);
                } catch (Exception e) {
                    if (mDecodeStatusListener != null) {
                        mDecodeStatusListener.onDecodeError(e);
                    }
                }
                return;
            }
        }
    }

    /**
     * 关闭摄像头预览，并且隐藏扫描框
     */
    public void closeCamera() {
        if (mCamera != null) {
            mCameraPreviewView.stopCameraPreview();
            mCameraPreviewView.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
    }

    public ScanRectView getScanRectView(){
        return mScanRectView;
    }

    public void onCreate(){
        mHandler = new Handler();
    }

    public void onStart(){
        openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        showScanRectView();
        startDecode();
    }

    public void onStop(){
        stopDecode();
        hideScanRectView();
        closeCamera();
    }

    public void onDestroy(){
        closeCamera();
        mHandler = null;
        mCamera = null;
        mOneShotPreviewCallbackTask = null;
    }

    /**
     * 显示扫描框
     */
    public void showScanRectView(){
        mScanRectView.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏扫描框
     */
    public void hideScanRectView(){
        mScanRectView.setVisibility(View.GONE);
    }

    /**
     * 开始一次解析
     */
    public void startDecode(){
        startDecodeDelay(1500);
    }

    /**
     * 延迟解析
     * @param delayMillions
     */
    public void startDecodeDelay(long delayMillions){
        mIsDecodeable = true;
        // 开始前先移除之前的任务
        mHandler.removeCallbacks(mOneShotPreviewCallbackTask);
        mHandler.postDelayed(mOneShotPreviewCallbackTask, delayMillions);
    }

    /**
     * 停止解析
     */
    public void stopDecode(){
        mIsDecodeable = false;

        // 停止当前正在解析的任务
        cancelProcessDataTask();

        if(mCamera != null){
            mCamera.setOneShotPreviewCallback(null);
        }

        if(mHandler != null){
            mHandler.removeCallbacksAndMessages(mOneShotPreviewCallbackTask);
        }
    }

    private Runnable mOneShotPreviewCallbackTask = new Runnable() {
        @Override
        public void run() {
            if (mCamera != null && mIsDecodeable) {
                // 此方法调用一次，取一次Camera预览的图像
                mCamera.setOneShotPreviewCallback(ScannerParentView.this);
            }
        }
    };

    /**
     * 数据解析状态侦听
     */
    public interface DecodeStatusListener{

        /**
         * 解析成功
         * @param data
         */
        void onDecodeSuccess(String data);

        /**
         * 解析过程中失败了
         * @param e
         */
        void onDecodeError(Exception e);

    }
}
