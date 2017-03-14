package ttyy.com.coder.scanner.decode;

import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

import ttyy.com.coder.scanner.camera.JinCameraManager;

/**
 * author: admin
 * date: 2017/03/14
 * version: 0
 * mail: secret
 * desc: ZXingDecoder
 */

public class ZXingDecoder {
    private static final String TAG = ZXingDecoder.class.getSimpleName();

    private Map<DecodeHintType, Object> mHints;
    private QRCodeReader mQrCodeReader;

    private ZXingDecoder(){
        mQrCodeReader = new QRCodeReader();
        mHints = new Hashtable<>();
        mHints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        mHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        mHints.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.QR_CODE);
    }

    static class Holder{
        static ZXingDecoder INSTANCE = new ZXingDecoder();
    }

    public static ZXingDecoder get(){
        return Holder.INSTANCE;
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency, reuse the same reader
     * objects from one decode to the next.
     *
     * @param data The YUV preview frame.
     * @param width The width of the preview frame.
     * @param height The height of the preview frame.
     */
    public void decode(byte[] data, int width, int height, DecodeCallback callback) {

        if(callback == null){
            Log.i(TAG, "No Decode Callback Won't Decode Data!");
            return;
        }

        // 线程解析 不影响UI主线程
        new DecodeThread(data, width, height, callback).start();

    }

    private class DecodeThread extends Thread{

        byte[] data;
        int width, height;
        DecodeCallback callback;

        private DecodeThread(byte[] data, int width, int height, DecodeCallback callback){
            this.data = data;
            this.width = width;
            this.height = height;
            this.callback = callback;
        }

        @Override
        public void run() {
            super.run();
            byte[] mRotatedData = new byte[width * height];
            Arrays.fill(mRotatedData, (byte) 0);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (x + y * width >= data.length) {
                        break;
                    }
                    mRotatedData[x * height + height - y - 1] = data[x + y * width];
                }
            }
            int tmp = width; // Here we are swapping, that's the difference to #11
            int width = height;
            int height = tmp;

            Result rawResult = null;
            try {
                PlanarYUVLuminanceSource source =
                        new PlanarYUVLuminanceSource(mRotatedData, width, height, 0, 0, width, height, false);
                BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
                rawResult = mQrCodeReader.decode(bitmap1, mHints);
            } catch (ReaderException e) {
                if (rawResult != null) {
                    // 解析失败
                    callback.onDecodeFail(e.getMessage());
                }
            } finally {
                mQrCodeReader.reset();
            }

            if (rawResult != null) {
                // 解析成功
                callback.onDecodeSuccess(rawResult.getText());
            }else {
                // 没有解析到有用的数据
                if(JinCameraManager.get(null) != null){
                    // 默认继续解析
                    JinCameraManager.get(null).requestPreviewFrameOnce();
                }else {
                    // 没有CameraManager 回调
                    callback.onDecodeSuccess("");
                }

            }
        }
    }

}
