package ttyy.com.demo;

import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;

import ttyy.com.coder.scanner.DecodeScannerView;

public class MainActivity extends AppCompatActivity implements ScannerParentView.DecodeStatusListener{

    ScannerParentView qrcodeview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        qrcodeview = (DecodeScannerView) findViewById(R.id.qrcodeview);
        qrcodeview.onCreate();

        qrcodeview.setDecodeStatusListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        qrcodeview.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        qrcodeview.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qrcodeview.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onDecodeSuccess(String data) {
        Log.e("Test", "data "+data);
        vibrate();
    }

    @Override
    public void onDecodeError(Exception e) {
        Log.d("Test", "e "+e.getMessage());
    }
}
