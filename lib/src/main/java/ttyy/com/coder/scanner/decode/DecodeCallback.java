package ttyy.com.coder.scanner.decode;

/**
 * author: admin
 * date: 2017/03/14
 * version: 0
 * mail: secret
 * desc: DecodeCallback
 */
public interface DecodeCallback {

    void onDecodeSuccess(String message);

    void onDecodeFail(String message);
}
