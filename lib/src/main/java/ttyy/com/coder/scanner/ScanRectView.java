package ttyy.com.coder.scanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import ttyy.com.coder.util.QRCodeUtil;

/**
 * Author: Administrator
 * Date  : 2016/12/07 17:09
 * Name  : ScanRectView
 * Intro : 二维码扫描 中间的框子
 * Modification  History:
 * Date          Author        	 Version          Description
 * ----------------------------------------------------------
 * 2016/12/07    Administrator   1.0              1.0
 */
public class ScanRectView extends View {

    Paint mPaint;

    // 中间的矩形框
    Rect mFrameRect;
    // 中间矩形框 线条粗度
    int mBorderSize;
    // 中间矩形框 线条颜色
    int mBorderColor = Color.WHITE;
    // 矩形框宽度
    int mBoxWidth;
    // 二维码矩形框高度
    int mQRCodeBoxHeight;
    // 二维码矩形框Y偏移
    int mQRCodeTopOffset = -1;

    // 条形码矩形框高度
    int mBarBoxTopOffset = -1;
    // 条形码矩形框高度
    int mBarBoxBoxHeight;

    // 是否是条形码
    boolean mIsBarCode;

    // 遮罩色
    int mMaskColor = Color.parseColor("#33FFFFFF");

    // 矩形框 四个角落
    int mCornorColor = Color.WHITE;
    // 矩形框 四个角落加粗区域大小
    int mCornorLength;
    // 矩形框 四个角落线条加粗 粗度
    int mCornorSize;
    float mHalfCornorSize;

    // 扫描线颜色
    int mScanLineColor = Color.WHITE;
    // 扫描线粗细度
    int mScanLineSize;
    // 扫描线运动控制flag
    float mScanLineTop, mScanLineLeft;
    int mScanlineMoveStepDistance;

    // 动画时间控制 mTotalAnimTime为走完一个总长度所消耗的总时间
    int mTotalAnimTime = 1800;
    // 单元刷新delay时延
    int mAnimDelayTime;

    // 提示信息文本
    String mTipText;
    // 提示文本颜色
    int mTipTextColor = Color.WHITE;
    // 提示文本字体大小
    int mTipTextSize = 14;
    // 提示信息文本绘制layout
    int mTipBackgroundRadius;
    // 提示文本绘制框边距
    int mTipTextMargin;
    // 提示文本框背景色
    int mTipBackgroundColor = Color.parseColor("#22000000");
    StaticLayout mTipTextSl;
    TextPaint mTipPaint;

    public ScanRectView(Context context) {
        this(context, null);
    }

    public ScanRectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanRectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    void init(AttributeSet attrs) {

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mTipPaint = new TextPaint();
        mTipPaint.setAntiAlias(true);
        mTipPaint.setColor(mTipTextColor);
        mTipPaint.setTextSize(QRCodeUtil.sp2px(getContext(), mTipTextSize));

        mBorderSize = QRCodeUtil.dp2px(getContext(), 1);

        mBoxWidth = QRCodeUtil.dp2px(getContext(), 200);
        mQRCodeBoxHeight = mBoxWidth;
        mQRCodeTopOffset = QRCodeUtil.dp2px(getContext(), 90);
        mBarBoxBoxHeight = QRCodeUtil.dp2px(getContext(), 120);
        mBarBoxTopOffset = QRCodeUtil.dp2px(getContext(), 110);

        mCornorLength = QRCodeUtil.dp2px(getContext(), 20);
        mCornorSize = QRCodeUtil.dp2px(getContext(), 3);
        mHalfCornorSize = (float) mCornorSize / 2;

        mScanLineSize = QRCodeUtil.dp2px(getContext(), 1);
        mScanlineMoveStepDistance = QRCodeUtil.dp2px(getContext(), 2);

        mTipBackgroundRadius = QRCodeUtil.dp2px(getContext(), 4);
        mTipTextMargin = QRCodeUtil.dp2px(getContext(), 20);

        setIsBarCode(mIsBarCode);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateBoxFrameRect();
    }

    /**
     * 计算二维码/条形码扫描框的区域
     */
    void calculateBoxFrameRect() {
        Point displaySize = QRCodeUtil.getScreenDisplaySize(getContext());
        int leftOffset = (displaySize.x - mBoxWidth) / 2;
        if (mIsBarCode) {

            if (mBarBoxTopOffset < 0) {
                mBarBoxTopOffset = (displaySize.y - mBarBoxBoxHeight) / 2;
            }

            mFrameRect = new Rect(leftOffset, mBarBoxTopOffset, leftOffset + mBoxWidth, mBarBoxTopOffset + mBarBoxBoxHeight);
            mScanLineLeft = mFrameRect.left + mHalfCornorSize + 0.5f;
        } else {

            if (mQRCodeTopOffset < 0) {
                mQRCodeTopOffset = (displaySize.y - mQRCodeBoxHeight) / 2;
            }
            mFrameRect = new Rect(leftOffset, mQRCodeTopOffset, leftOffset + mBoxWidth, mQRCodeTopOffset + mQRCodeBoxHeight);
            mScanLineTop = mFrameRect.top + mHalfCornorSize + 0.5f;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mFrameRect == null) {
            super.onDraw(canvas);
            return;
        }

        drawMask(canvas);
        drawBorderLine(canvas);
        drawCornorLine(canvas);
        drawTipText(canvas);

        drawScanLine(canvas);
        moveScanLine();
    }

    /**
     * 绘制遮罩层
     *
     * @param canvas
     */
    void drawMask(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mMaskColor);
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        canvas.drawRect(0, 0, width, mFrameRect.top, mPaint);
        canvas.drawRect(0, mFrameRect.top, mFrameRect.left, height, mPaint);
        canvas.drawRect(mFrameRect.right, mFrameRect.top, width, height, mPaint);
        canvas.drawRect(mFrameRect.left, mFrameRect.bottom, mFrameRect.right, height, mPaint);
    }

    /**
     * 绘制边框线条
     *
     * @param canvas
     */
    void drawBorderLine(Canvas canvas) {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mBorderColor);
        mPaint.setStrokeWidth(mBorderSize);

        canvas.drawLine(mFrameRect.left + mCornorLength, mFrameRect.top, mFrameRect.right - mCornorLength, mFrameRect.top, mPaint);
        canvas.drawLine(mFrameRect.left + mCornorLength, mFrameRect.bottom, mFrameRect.right - mCornorLength, mFrameRect.bottom, mPaint);
        canvas.drawLine(mFrameRect.left, mFrameRect.top + mCornorLength, mFrameRect.left, mFrameRect.bottom - mCornorLength, mPaint);
        canvas.drawLine(mFrameRect.right, mFrameRect.top + mCornorLength, mFrameRect.right, mFrameRect.bottom - mCornorLength, mPaint);
    }

    /**
     * 绘制四个角
     *
     * @param canvas
     */
    void drawCornorLine(Canvas canvas) {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mCornorSize);
        mPaint.setColor(mCornorColor);

        canvas.drawLine(mFrameRect.left - mHalfCornorSize, mFrameRect.top, mFrameRect.left + mCornorLength, mFrameRect.top, mPaint);
        canvas.drawLine(mFrameRect.left, mFrameRect.top - mHalfCornorSize, mFrameRect.left, mFrameRect.top + mCornorLength, mPaint);

        canvas.drawLine(mFrameRect.right + mHalfCornorSize, mFrameRect.top, mFrameRect.right - mCornorLength, mFrameRect.top, mPaint);
        canvas.drawLine(mFrameRect.right, mFrameRect.top - mHalfCornorSize, mFrameRect.right, mFrameRect.top + mCornorLength, mPaint);

        canvas.drawLine(mFrameRect.left - mHalfCornorSize, mFrameRect.bottom, mFrameRect.left + mCornorLength, mFrameRect.bottom, mPaint);
        canvas.drawLine(mFrameRect.left, mFrameRect.bottom - mHalfCornorSize, mFrameRect.left, mFrameRect.bottom - mCornorLength, mPaint);

        canvas.drawLine(mFrameRect.right + mHalfCornorSize, mFrameRect.bottom, mFrameRect.right - mCornorLength, mFrameRect.bottom, mPaint);
        canvas.drawLine(mFrameRect.right, mFrameRect.bottom + mHalfCornorSize, mFrameRect.right, mFrameRect.bottom - mCornorLength, mPaint);

    }

    /**
     * 绘制提示信息
     * @param canvas
     */
    void drawTipText(Canvas canvas){
        if(mTipTextSl == null)
            return;

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mTipBackgroundColor);

        canvas.drawRoundRect(new RectF(mFrameRect.left, mFrameRect.top - mTipTextMargin - mTipTextSl.getHeight() - mTipBackgroundRadius * 2, mFrameRect.right, mFrameRect.top - mTipTextMargin), mTipBackgroundRadius, mTipBackgroundRadius, mPaint);

        canvas.save();
        canvas.translate(mFrameRect.left + mTipBackgroundRadius, mFrameRect.top - mTipTextMargin - mTipTextSl.getHeight() - mTipBackgroundRadius);
        mTipTextSl.draw(canvas);
        canvas.restore();

    }

    /**
     * 画扫描线
     *
     * @param canvas
     */
    void drawScanLine(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mScanLineColor);

        if (mIsBarCode) {
            canvas.drawRect(mScanLineLeft, mFrameRect.top + mHalfCornorSize, mScanLineLeft + mScanLineSize, mFrameRect.bottom - mHalfCornorSize, mPaint);
        } else {
            canvas.drawRect(mFrameRect.left + mHalfCornorSize, mScanLineTop, mFrameRect.right - mHalfCornorSize, mScanLineTop + mScanLineSize, mPaint);
        }
    }

    /**
     * 移动扫描线
     */
    void moveScanLine() {

        if (mIsBarCode) {
            mScanLineLeft += mScanlineMoveStepDistance;

            if (mScanLineLeft + mScanLineSize > mFrameRect.right - mHalfCornorSize) {
                mScanLineLeft = mFrameRect.left + mHalfCornorSize + 0.5f;
            }
        } else {
            mScanLineTop += mScanlineMoveStepDistance;

            if (mScanLineTop + mScanLineSize > mFrameRect.bottom - mHalfCornorSize) {
                mScanLineTop = mFrameRect.top + mHalfCornorSize + 0.5f;
            }
        }

        postInvalidateDelayed(mAnimDelayTime, mFrameRect.left, mFrameRect.top, mFrameRect.right, mFrameRect.bottom);
    }

    /**
     * 设置提示信息
     * @param text
     */
    public void setTipText(String text){
        mTipText = text;
        postInvalidate();
    }

    /**
     * 设置是否是扫描条形码
     * @param value
     */
    public void setIsBarCode(boolean value) {
        mIsBarCode = value;

        if (mIsBarCode) {
            mAnimDelayTime = mTotalAnimTime * mScanlineMoveStepDistance / mBoxWidth;

            if (TextUtils.isEmpty(mTipText)) {
                mTipText = "请将条形码置于扫描框内";
            }

            mTipTextSl = new StaticLayout(mTipText, mTipPaint, mBoxWidth - 2 * mTipBackgroundRadius, Layout.Alignment.ALIGN_CENTER, 1.0f, 0, true);

        } else {
            mAnimDelayTime = mTotalAnimTime * mScanlineMoveStepDistance / mQRCodeBoxHeight;

            if (TextUtils.isEmpty(mTipText)) {
                mTipText = "请将二维码置于扫描框内";
            }

            mTipTextSl = new StaticLayout(mTipText, mTipPaint, mBoxWidth - 2 * mTipBackgroundRadius, Layout.Alignment.ALIGN_CENTER, 1.0f, 0, true);

        }

        calculateBoxFrameRect();
        postInvalidate();
    }

    public ScanRectView setBorderColor(int color){
        mBorderColor = color;
        return this;
    }

    public ScanRectView setCornorColor(int color){
        mCornorColor = color;
        return this;
    }

    public ScanRectView setScanLineColor(int color){
        mScanLineColor = color;
        return this;
    }

    public ScanRectView setQRCodeBoxHeight(int height){
        mQRCodeBoxHeight = height;
        return this;
    }

    public ScanRectView setQRCodeBoxTopOffset(int offset){
        mQRCodeTopOffset = offset;
        return this;
    }

    public ScanRectView setBoxWidth(int width){
        mBoxWidth =width;
        return this;
    }

    public ScanRectView setBarCodeBoxHeight(int height){
        mBarBoxBoxHeight = height;
        return this;
    }

    public ScanRectView setBarCodeBoxTopOffset(int offset){
        mBarBoxTopOffset = offset;
        return this;
    }

    public ScanRectView resetCodeBoxInfo(){
        calculateBoxFrameRect();
        return this;
    }
}
