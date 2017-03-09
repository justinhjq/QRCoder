package ttyy.com.coder.scanner.decode;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;

public class DecodeDataTask extends AsyncTask<Void, Void, String> {
    private Camera mCamera;
    private byte[] mData;
    private DecodeTracer mDecodeTracer;

    public DecodeDataTask(Camera camera, byte[] data, DecodeTracer tracer) {
        mCamera = camera;
        mData = data;
        mDecodeTracer = tracer;
    }

    public DecodeDataTask start() {
        if (Build.VERSION.SDK_INT >= 11) {
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            execute();
        }
        return this;
    }

    public void cancelTask() {
        if (getStatus() != Status.FINISHED) {
            cancel(true);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mDecodeTracer = null;
    }

    @Override
    protected String doInBackground(Void... params) {
        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        int width = size.width;
        int height = size.height;

        byte[] rotatedData = new byte[mData.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rotatedData[x * height + height - y - 1] = mData[x + y * width];
            }
        }
        int tmp = width;
        width = height;
        height = tmp;

        try {
            if (mDecodeTracer == null) {
                return null;
            }
            return mDecodeTracer.decodeData(rotatedData, width, height, false);
        } catch (Exception e1) {
            try {
                return mDecodeTracer.decodeData(rotatedData, width, height, true);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    /**
     * 分析数据具体实现
     * 分发给具体的View处理
     */
    public interface DecodeTracer {
        String decodeData(byte[] data, int width, int height, boolean isRetry);
    }
}
