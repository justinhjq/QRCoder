/*
 * Copyright (C) 2008 ZXing authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package ttyy.com.coder.scanner.camera;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The implementation
 * encapsulates the steps needed to take preview-sized images, which are used for both preview and decoding.
 *
 */
public final class JinCameraManager {
    private static final String TAG = JinCameraManager.class.getSimpleName();

    private volatile static JinCameraManager sCameraManager;

    private final CameraConfigurationManager mConfigManager;
    private Camera mCamera;
    private boolean mInitialized;
    private boolean mPreviewing;
    /**
     * Preview frames are delivered here, which we pass on to the registered handler. Make sure to clear the handler so
     * it will only receive one message.
     */
    private final JinPreviewCallback mPreviewCallback;

    /** Auto-focus callbacks arrive here, and are dispatched to the Handler which requested them. */
    private final Camera.AutoFocusCallback mAutoFocusCallback;
    private boolean isCameraAutoFocus = false;

    private Handler mHandler;

    /**
     * Gets the JinCameraManager singleton instance.
     *
     * @return A reference to the JinCameraManager singleton.
     */
    public static JinCameraManager get(Context context) {
        JinCameraManager inst = sCameraManager;
        if(inst == null){
            synchronized (JinCameraManager.class){
                inst = sCameraManager;
                if(inst == null){
                    if(context != null){
                        inst = new JinCameraManager(context);
                        sCameraManager = inst;
                    }
                }
            }
        }
        return inst;
    }

    private JinCameraManager(Context ctx) {
        Context context = ctx.getApplicationContext();
        this.mConfigManager = new CameraConfigurationManager(context);
        mPreviewCallback = new JinPreviewCallback(mConfigManager);
        mAutoFocusCallback = new Camera.AutoFocusCallback(){
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                Log.i(TAG, "Camera onAutoFocus isSuccess ? "+success);
                if(isCameraAutoFocus){
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setAutoFocus(true);
                        }
                    }, 1500);
                }else {
                    mHandler.removeCallbacksAndMessages(null);
                }
            }
        };
        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Opens the mCamera driver and initializes the hardware parameters.
     *
     * @param holder The surface object which the mCamera will draw preview frames into.
     * @throws IOException Indicates the mCamera driver failed to open.
     */
    public void openDriver(SurfaceHolder holder) throws IOException {
        if (mCamera == null) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++) {
                Camera.getCameraInfo(cameraId, cameraInfo);
                if (cameraInfo.facing == 0) {
                    try {
                        mCamera = Camera.open(cameraId);
                    } catch (Exception e) {
                        Log.w(TAG, "OpenCamera Failed");
                        if(getCameraPreviewCallback().getDecodeCallback() != null){
                            getCameraPreviewCallback().getDecodeCallback().onDecodeFail(e.getMessage());
                        }
                        return;
                    }
                }
            }
            mCamera.setPreviewDisplay(holder);

            if (!mInitialized) {
                mInitialized = true;
                mConfigManager.initFromCameraParameters(mCamera);
            }
            mConfigManager.setDesiredCameraParameters(mCamera);
        }
    }

    /**
     * 打开或关闭闪光灯
     *
     * @param open 控制是否打开
     * @return 打开或关闭失败，则返回false。
     */
    public boolean setFlashLight(boolean open) {
        if (mCamera == null) {
            return false;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return false;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        // Check if camera flash exists
        if (null == flashModes || 0 == flashModes.size()) {
            // Use the screen as a flashlight (next best thing)
            return false;
        }
        String flashMode = parameters.getFlashMode();
        if (open) {
            if (Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
                return true;
            }
            // Turn on the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
                return true;
            } else {
                return false;
            }
        } else {
            if (Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)) {
                return true;
            }
            // Turn on the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
                return true;
            } else
                return false;
        }
    }

    /**
     * Closes the camera driver if still in use.
     */
    public void closeDriver() {
        if (mCamera != null) {
            mCamera.release();
            mInitialized = false;
            mPreviewing = false;
            mCamera = null;
            mPreviewCallback.setDecodeCallback(null);
        }
    }

    /**
     * Asks the mCamera hardware to begin drawing preview frames to the screen.
     */
    public void startPreview() {
        if (mCamera != null && !mPreviewing) {
            mCamera.startPreview();
            mPreviewing = true;
            setAutoFocus(true);
        }
    }

    /**
     * Tells the mCamera to stop drawing preview frames.
     */
    public void stopPreview() {
        if (mCamera != null && mPreviewing) {
            mCamera.stopPreview();
            mPreviewing = false;
        }
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data will arrive as byte[] in the
     * message.obj field, with width and height encoded as message.arg1 and message.arg2, respectively.
     */
    public boolean requestPreviewFrameOnce() {
        if (mCamera != null && mPreviewing) {
            mCamera.setOneShotPreviewCallback(mPreviewCallback);
            return true;
        }

        return false;
    }

    /**
     * 获取Camera预览回调 设置二维码解析回调入口
     * @return
     */
    public JinPreviewCallback getCameraPreviewCallback(){
        return mPreviewCallback;
    }

    /**
     * Asks the mCamera hardware to perform an autofocus.
     */
    public void setAutoFocus(boolean value) {
        isCameraAutoFocus = value;
        if (mCamera != null && mPreviewing && value) {
            mCamera.autoFocus(mAutoFocusCallback);
        }else {
            mHandler.removeCallbacksAndMessages(null);
            mCamera.autoFocus(null);
        }
    }
}
