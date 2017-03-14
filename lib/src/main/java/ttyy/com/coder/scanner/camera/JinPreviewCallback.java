/*
 * Copyright (C) 2010 ZXing authors
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

import android.hardware.Camera;
import android.util.Log;

import ttyy.com.coder.scanner.decode.DecodeCallback;
import ttyy.com.coder.scanner.decode.ZXingDecoder;

public final class JinPreviewCallback implements Camera.PreviewCallback {
    private static final String TAG = JinPreviewCallback.class.getName();
    private CameraConfigurationManager mConfigManager;

    private DecodeCallback mDecodeCallback;

    JinPreviewCallback(CameraConfigurationManager configManager) {
        this.mConfigManager = configManager;
    }

    public JinPreviewCallback setDecodeCallback(DecodeCallback callback){
        this.mDecodeCallback = callback;
        return this;
    }

    public DecodeCallback getDecodeCallback(){
        return this.mDecodeCallback;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.i(TAG, "byte datas ready to decode");
        Camera.Size cameraResolution = mConfigManager.getCameraResolution();
        if (mDecodeCallback != null) {
            ZXingDecoder.get().decode(data, cameraResolution.width, cameraResolution.height, mDecodeCallback);
        } else {
            Log.w(TAG, "Hasn't Set Decode Result Callback");
        }
    }

}
