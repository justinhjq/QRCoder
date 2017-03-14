package ttyy.com.demo;

import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;

import ttyy.com.coder.scanner.QRCodeScannerView;
import ttyy.com.coder.scanner.decode.DecodeCallback;

public class MainActivity extends AppCompatActivity implements DecodeCallback{

    QRCodeScannerView qrcodeview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        qrcodeview = (QRCodeScannerView) findViewById(R.id.qrcodeview);
        qrcodeview.setDecodeCallback(this);

        qrcodeview.startDecode();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(500);
    }

    @Override
    public void onDecodeSuccess(String data) {
        Log.e("Test", "data "+data);
        vibrate();
        qrcodeview.startDecode();
    }

    @Override
    public void onDecodeFail(String message) {

    }

}
