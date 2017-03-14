package ttyy.com.coder.scanner;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import ttyy.com.coder.R;

/**
 * author: admin
 * date: 2017/03/14
 * version: 0
 * mail: secret
 * desc: ScannerRectView
 */

public class ScannerRectView extends View {
    Rect mScanRectBoxFrame;
    Paint mRectPaint;

    /**
     * 边框
     */
    int mScanRectBorderWidth;
    int mScanRectBorderColor = Color.WHITE;
    int mScanRectBoxWidth, mScanRectBoxHeight;

    /**
     * 四个角
     */
    int mScanRectCornorBorderWidth;
    int mScanRectCornorWidth;
    int mScanRectCornorBorderColor = Color.WHITE;

    /**
     * 遮罩颜色
     */
    int mMaskColor = Color.parseColor("#33FFFFFF");

    /**
     * 文字
     */
    StaticLayout mTipsTextLayout;
    String mTipsText;
    TextPaint mTextPaint;
    int mTipsTextColor = Color.parseColor("#cccccc");
    int mTipsBackColor = Color.parseColor("#b0000000");
    boolean mEnableTips = true;

    /**
     * 扫描线
     */
    int mScanLineBorderWidth;
    int mScanLineColor = Color.WHITE;
    int mScanLineTopOffset;
    int mScanLineStepDistance;
    long mRefreshDelayMillions;
    boolean mEnableScanLine = true;

    public ScannerRectView(Context context) {
        this(context, null);
    }

    public ScannerRectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAttributes(attrs);
    }

    void getAttributes(AttributeSet attrs){
        if(attrs != null){

            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.ScannerRectView);

            mScanRectBorderWidth = ta.getDimensionPixelOffset(R.styleable.ScannerRectView_boxBorderWidth, 0);
            mScanRectCornorWidth = ta.getDimensionPixelOffset(R.styleable.ScannerRectView_boxCornorWidth, 0);
            mScanRectCornorBorderWidth = ta.getDimensionPixelOffset(R.styleable.ScannerRectView_boxCornorBorderWidth, 0);
            mScanRectBoxWidth = ta.getDimensionPixelOffset(R.styleable.ScannerRectView_boxWidth, 0);
            mScanRectBoxHeight = ta.getDimensionPixelOffset(R.styleable.ScannerRectView_boxHeight, 0);

            mScanRectBorderColor = ta.getColor(R.styleable.ScannerRectView_boxBorderColor, Color.WHITE);
            mScanRectCornorBorderColor = ta.getColor(R.styleable.ScannerRectView_boxCornorColor, Color.WHITE);
            mMaskColor = ta.getColor(R.styleable.ScannerRectView_maskColor, Color.parseColor("#33FFFFFF"));
            mScanLineColor = ta.getColor(R.styleable.ScannerRectView_scanLineColor, Color.WHITE);
            mTipsTextColor = ta.getColor(R.styleable.ScannerRectView_tipsTextColor, Color.parseColor("#cccccc"));
            mTipsBackColor = ta.getColor(R.styleable.ScannerRectView_tipsBackColor, Color.parseColor("#b0000000"));

            mTipsText = ta.getString(R.styleable.ScannerRectView_tipsText);
            if (TextUtils.isEmpty(mTipsText)) {
                mTipsText = "请将二维码置于扫描框内";
            }

            ta.recycle();
        }

        mTextPaint = new TextPaint();
        mTextPaint.setTextSize(dp2px(14));
        mTextPaint.setColor(mTipsTextColor);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setFilterBitmap(true);

        mRectPaint = new Paint();
        mRectPaint.setAntiAlias(true);
        mRectPaint.setFilterBitmap(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mScanRectBoxFrame == null){
            super.onDraw(canvas);
            return;
        }
        canvas.drawColor(Color.TRANSPARENT);

        drawMask(canvas);

        drawScanRect(canvas);

        if(mEnableTips && TextUtils.isEmpty(mTipsText)){
            drawTipsText(canvas);
        }

        if(mEnableScanLine){
            drawScanLine(canvas);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setScanRectBoxFrameAttribute();
    }

    // 设置扫描框信息
    void setScanRectBoxFrameAttribute(){

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        // 扫描框
        mScanRectBoxFrame = new Rect();
        if(mScanRectBoxWidth == 0){
            // 启用默认设置
            mScanRectBoxFrame.left = width / 4;
            mScanRectBoxFrame.right = 3 * width / 4;
            mScanRectBoxFrame.top = height / 2 - dp2px(54) - width / 4;
            mScanRectBoxFrame.bottom = height / 2 - dp2px(54) + width / 4;
        }else {
            // 启用用户自定义设置
            mScanRectBoxFrame.left = width / 2 - mScanRectBoxWidth / 2;
            mScanRectBoxFrame.right = mScanRectBoxFrame.left + mScanRectBoxWidth;

            if(mScanRectBoxHeight <= 0){
                mScanRectBoxHeight = mScanRectBoxWidth;
            }

            mScanRectBoxFrame.top = height / 2 - mScanRectBoxHeight / 2;
            mScanRectBoxFrame.bottom = mScanRectBoxFrame.top + mScanRectBoxHeight;
        }

        // 扫描框粗细
        if(mScanRectBorderWidth <= 0){
            mScanRectBorderWidth = dp2px(1);
        }

        if(mScanRectCornorBorderWidth <= 0){
            mScanRectCornorBorderWidth = dp2px(5);
        }

        if(mScanRectCornorWidth <= 0){
            mScanRectCornorWidth = mScanRectBoxFrame.width() / 7;
        }

        // 扫描线颜色
        if(mScanLineBorderWidth <= 0){
            mScanLineBorderWidth = dp2px(1);
        }
        mScanLineTopOffset = mScanRectBoxFrame.top;
        mScanLineStepDistance = dp2px(1.3f);
        mRefreshDelayMillions = mScanLineStepDistance * 2200 / mScanRectBoxFrame.height();

        // 文字宽度
        int mTextLayoutWidth = (int) mTextPaint.measureText(mTipsText);
        mTipsTextLayout = new StaticLayout(mTipsText, mTextPaint, mTextLayoutWidth, Layout.Alignment.ALIGN_CENTER, 1f, 0, true);
    }

    int dp2px(float value){
        return (int) (getResources().getDisplayMetrics().density * value + 0.5f);
    }

    // 绘制遮罩
    void drawMask(Canvas canvas){
        mRectPaint.setColor(mMaskColor);
        mRectPaint.setStyle(Paint.Style.FILL);

        canvas.drawRect(0, 0, mScanRectBoxFrame.left, getHeight(), mRectPaint);
        canvas.drawRect(mScanRectBoxFrame.left, 0, getWidth(), mScanRectBoxFrame.top, mRectPaint);
        canvas.drawRect(mScanRectBoxFrame.left, mScanRectBoxFrame.bottom, getWidth(), getHeight(), mRectPaint);
        canvas.drawRect(mScanRectBoxFrame.right, mScanRectBoxFrame.top, getWidth(), mScanRectBoxFrame.bottom, mRectPaint);
    }

    // 绘制扫描框
    void drawScanRect(Canvas canvas){
        mRectPaint.setStyle(Paint.Style.STROKE);

        // 绘制扫描框 // 边框四条线
        int rectOffsetLeft = mScanRectBoxFrame.left + mScanRectCornorWidth - mScanRectCornorBorderWidth / 2;
        int rectOffsetRight = mScanRectBoxFrame.right - mScanRectCornorWidth + mScanRectCornorBorderWidth / 2;
        int rectOffsetTop = mScanRectBoxFrame.top + mScanRectCornorWidth - mScanRectCornorBorderWidth / 2;
        int rectOffsetBottom = mScanRectBoxFrame.bottom - mScanRectCornorWidth + mScanRectCornorBorderWidth / 2;
        mRectPaint.setStrokeWidth(mScanRectBorderWidth);
        mRectPaint.setColor(mScanRectBorderColor);
        canvas.drawLine(rectOffsetLeft, mScanRectBoxFrame.top, rectOffsetRight, mScanRectBoxFrame.top, mRectPaint);
        canvas.drawLine(mScanRectBoxFrame.left, rectOffsetTop, mScanRectBoxFrame.left, rectOffsetBottom, mRectPaint);
        canvas.drawLine(mScanRectBoxFrame.right, rectOffsetTop, mScanRectBoxFrame.right, rectOffsetBottom, mRectPaint);
        canvas.drawLine(rectOffsetLeft, mScanRectBoxFrame.bottom, rectOffsetRight, mScanRectBoxFrame.bottom, mRectPaint);

        // 边框四个角
        int cornorOffsetLeft = mScanRectBoxFrame.left - mScanRectCornorBorderWidth / 2;
        int cornorOffsetRight = mScanRectBoxFrame.right + mScanRectCornorBorderWidth / 2;
        int cornorOffsetTop = mScanRectBoxFrame.top - mScanRectCornorBorderWidth / 2;
        int cornorOffsetBottom = mScanRectBoxFrame.bottom + mScanRectCornorBorderWidth / 2;
        mRectPaint.setStrokeWidth(mScanRectCornorBorderWidth);
        mRectPaint.setColor(mScanRectCornorBorderColor);
        // 左上角
        canvas.drawLine(cornorOffsetLeft, mScanRectBoxFrame.top, rectOffsetLeft, mScanRectBoxFrame.top, mRectPaint);
        canvas.drawLine(mScanRectBoxFrame.left, cornorOffsetTop, mScanRectBoxFrame.left, rectOffsetTop, mRectPaint);
        // 右上角
        canvas.drawLine(rectOffsetRight, mScanRectBoxFrame.top, cornorOffsetRight, mScanRectBoxFrame.top, mRectPaint);
        canvas.drawLine(mScanRectBoxFrame.right, cornorOffsetTop, mScanRectBoxFrame.right, rectOffsetTop, mRectPaint);
        // 左下角
        canvas.drawLine(cornorOffsetLeft, mScanRectBoxFrame.bottom, rectOffsetLeft, mScanRectBoxFrame.bottom, mRectPaint);
        canvas.drawLine( mScanRectBoxFrame.left, cornorOffsetBottom,  mScanRectBoxFrame.left, rectOffsetBottom, mRectPaint);
        // 右下角
        canvas.drawLine(rectOffsetRight, mScanRectBoxFrame.bottom, cornorOffsetRight, mScanRectBoxFrame.bottom, mRectPaint);
        canvas.drawLine(mScanRectBoxFrame.right, cornorOffsetBottom, mScanRectBoxFrame.right, rectOffsetBottom, mRectPaint);

    }

    void drawTipsText(Canvas canvas){
        mRectPaint.setStyle(Paint.Style.FILL);
        mRectPaint.setColor(mTipsBackColor);

        RectF mTipsBackFrame = new RectF();

        int radius = dp2px(5);
        int centerX = getWidth() / 2;
        int verticalPadding = dp2px(14);
        int horizontalPadding = dp2px(16);

        mTipsBackFrame.left = centerX - mTipsTextLayout.getWidth() / 2 - horizontalPadding;
        mTipsBackFrame.right = centerX + mTipsTextLayout.getWidth() / 2 + horizontalPadding;
        mTipsBackFrame.bottom = mScanRectBoxFrame.top - dp2px(22);
        mTipsBackFrame.top = mTipsBackFrame.bottom - mTipsTextLayout.getHeight() - verticalPadding;

        canvas.drawRoundRect(mTipsBackFrame, radius, radius, mRectPaint);

        canvas.save();
        canvas.translate(mTipsBackFrame.left + horizontalPadding, mTipsBackFrame.top + verticalPadding / 2);
        mTipsTextLayout.draw(canvas);
        canvas.restore();
    }

    // 绘制扫描线
    void drawScanLine(Canvas canvas){
        mRectPaint.setColor(mScanLineColor);
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeWidth(mScanLineBorderWidth);

        canvas.drawLine(mScanRectBoxFrame.left, mScanLineTopOffset, mScanRectBoxFrame.right, mScanLineTopOffset, mRectPaint);

        mScanLineTopOffset += mScanLineStepDistance;
        if(mScanLineTopOffset >= mScanRectBoxFrame.bottom){
            mScanLineTopOffset = mScanRectBoxFrame.top;
        }

        postInvalidateDelayed(mRefreshDelayMillions, mScanRectBoxFrame.left, mScanLineTopOffset, mScanRectBoxFrame.right, mScanRectBoxFrame.bottom);
    }

    public ScannerRectView setBoxWidth(int width){
        mScanRectBoxWidth = width;
        return this;
    }

    public ScannerRectView setBoxHeight(int height){
        mScanRectBoxHeight = height;
        return this;
    }

    public ScannerRectView setRectBorderWidth(int strokeWidth){
        mScanRectBorderWidth = strokeWidth;
        return this;
    }

    public ScannerRectView setRectCornorWidth(int cornorWidth){
        mScanRectCornorWidth = cornorWidth;
        return this;
    }

    public ScannerRectView setRectCornorBorderWidth(int cornorBorderWidth){
        mScanRectCornorBorderWidth = cornorBorderWidth;
        return this;
    }

    public ScannerRectView setRectCornorColor(int color){
        mScanRectCornorBorderColor = color;
        return this;
    }

    public ScannerRectView setRectBorderColor(int color){
        mScanRectBorderColor = color;
        return this;
    }

    public ScannerRectView setMaskColor(int color){
        mMaskColor = color;
        return this;
    }

    public ScannerRectView setTipsText(String text){
        mTipsText = text;
        return this;
    }

    public ScannerRectView setTipsTextColor(int color){
        mTipsTextColor = color;
        return this;
    }

    public ScannerRectView setTipsBoxBackColor(int color){
        mTipsBackColor = color;
        return this;
    }

    public void calculateBoxInfos(){
        if(getMeasuredWidth() > 0){
            setScanRectBoxFrameAttribute();
        }
    }

}
