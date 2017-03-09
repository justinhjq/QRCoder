package ttyy.com.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;

import ttyy.com.coder.scanner.DecodeScannerView;
import ttyy.com.coder.scanner.ScannerParentView;

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

        qrcodeview.getScanRectView().setBorderColor(Color.BLUE)// 边框颜色
                .setCornorColor(Color.RED) // 四个角颜色
                .setScanLineColor(Color.GREEN) // 扫描线颜色
                .setQRCodeBoxHeight(500) // 二维码框 高度 px
                .setBoxWidth(300) // 二维码框 宽度 px
                .setQRCodeBoxTopOffset(50)// 二维码 距离上边距 px
                .resetCodeBoxInfo(); // 设置完新信息，通知view重新计算框的位置信息
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
