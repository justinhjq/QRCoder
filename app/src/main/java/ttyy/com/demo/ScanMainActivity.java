package ttyy.com.demo;

import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import ttyy.com.coder.scanner_git.core.QRCodeView;

public class ScanMainActivity extends AppCompatActivity implements QRCodeView.Delegate {

    QRCodeView mQRCodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_zbar);

        mQRCodeView = (QRCodeView) findViewById(R.id.zbarview);
        mQRCodeView.setDelegate(this);

        mQRCodeView.startSpot();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mQRCodeView.startCamera();
        mQRCodeView.showScanRect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mQRCodeView.stopSpot();
        mQRCodeView.stopCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mQRCodeView.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.e("Test", "result " + result);
        Toast.makeText(this, "result", Toast.LENGTH_LONG).show();
        vibrate();
        mQRCodeView.startSpot();
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e("Test", "open camear error");
    }
}
