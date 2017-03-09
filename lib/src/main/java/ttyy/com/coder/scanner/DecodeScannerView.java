package ttyy.com.coder.scanner;

import android.content.Context;
import android.util.AttributeSet;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import ttyy.com.coder.scanner.decode.QRCodeDecoder;

/**
 * Author: Administrator
 * Date  : 2016/12/08 14:41
 * Name  : DecodeView
 * Intro : Edit By Administrator
 * Modification  History:
 * Date          Author        	 Version          Description
 * ----------------------------------------------------------
 * 2016/12/08    Administrator   1.0              1.0
 */
public class DecodeScannerView extends ScannerParentView {

    private MultiFormatReader mMultiFormatReader;

    public DecodeScannerView(Context context) {
        this(context, null);
    }

    public DecodeScannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMultiFormatReader();
    }

    private void initMultiFormatReader() {
        mMultiFormatReader = new MultiFormatReader();
        mMultiFormatReader.setHints(QRCodeDecoder.HINTS);
    }

    @Override
    public String decodeData(byte[] data, int width, int height, boolean isRetry) {
        String result = null;
        Result rawResult = null;

        try {
            PlanarYUVLuminanceSource source = null;
            source = new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false);
            rawResult = mMultiFormatReader.decodeWithState(new BinaryBitmap(new HybridBinarizer(source)));
        } catch (Exception e1) {
        } finally {
            mMultiFormatReader.reset();
        }

        if (rawResult != null) {
            result = rawResult.getText();
        }
        return result;
    }
}
