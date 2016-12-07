package ttyy.com.coder.generator;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.decoder.Version;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

import java.util.Hashtable;

/**
 * Author: Administrator
 * Date  : 2016/12/06 16:18
 * Name  : QRCodeBuilder
 * Intro : 二维码图标生成器
 * Modification  History:
 * Date          Author        	 Version          Description
 * ----------------------------------------------------------
 * 2016/12/06    Administrator   1.0              1.0
 */
public class QRCodeBuilder {

    /**
     * 二维码包含的信息
     */
    private String mInfoText = "QRCodeBuilder";
    /**
     * 二维码信息编码Charset
     */
    private String mCharSet = "utf-8";
    /**
     * 生成的二维码大小 只生成正方形
     */
    private int nQRCodeSize = 500;
    /**
     * 二维码间隙的填充色
     */
    private int nQRCodeIntervalColor = Color.WHITE;
    /**
     * 二维码的填充色
     */
    private int nQRCodeFillColor = Color.BLACK;
    /**
     * 二维码logo配置信息
     */
    private QRCodeLogoInfo mQRCodeLogoInfo;
    /**
     * 三个定位点信息
     */
    private QRCodeCornorSquare mQRCodeCornorSquare;
    /**
     * 二维码对象
     */
    private QRCode mQRCode;

    /**
     * 二维码左上角，左下角，右上角的正方形信息
     */
    public static class QRCodeCornorSquare {
        /**
         * 二维码三个角落的 左下，左上，右上
         * 每个二维码图片这三个角落都有个正方形标识
         */
        private int nLeftTopColor = -1;
        private int nRightTopColor = -1;
        private int nLeftBottomColor = -1;

        // 二维码三个定位基点信息
        private int nLeftStartX, nLeftEndX;
        private int nTopStartY, nTopEndY;
        private int nBottomStartY, nBottomEndY;
        private int nRightStartX, nRightEndX;

        // 定位正方形包括 框子，框子内部的实体正放心
        // 是否填充所有
        // 1:1:3:1:1
        private boolean fillAll;

        /**
         * 从QRCode对象中获取三个定位基点信息
         * @param builder
         */
        private void loadQRCodeCornroInfoFromQRCode(QRCodeBuilder builder, BitMatrix bitMatrix){
            Version version = builder.mQRCode.getVersion();
            // left top right bottom
            int[] tl = bitMatrix.getTopLeftOnBit();

            int totalModeNum = (version.getVersionNumber() - 1) * 4 + 21;
            int realCodeWidth = builder.nQRCodeSize - 2 * (tl[0]);
            int nModeSize = realCodeWidth / totalModeNum;

            int startModel = fillAll ? 0 : 2;
            int endModel = fillAll ? 7 : 5;

            nLeftStartX = startModel * nModeSize + tl[0];
            nLeftEndX = endModel * nModeSize + tl[0];
            nTopStartY = startModel * nModeSize + tl[1];
            nTopEndY = endModel * nModeSize + tl[1];
            nBottomStartY = (totalModeNum - endModel) * nModeSize + tl[1];
            nBottomEndY = (totalModeNum - startModel) * nModeSize + tl[1];

            nRightStartX = (totalModeNum - endModel) * nModeSize + tl[0];
            nRightEndX = (totalModeNum - startModel) * nModeSize + tl[0];

        }

        /**
         * 三个定位基点正方形的色彩
         * @param pixels
         * @param builder
         * @param x
         * @param y
         */
        private void applyToPixels(int[] pixels, QRCodeBuilder builder, int x, int y) {

            if(x >= nLeftStartX && x <= nLeftEndX
                    && y >= nTopStartY && y <= nTopEndY){
                // 左上角
                if(nLeftTopColor == -1)
                    return;
                pixels[y * builder.nQRCodeSize + x] = nLeftTopColor;
            }else if(x >= nRightStartX && x <= nRightEndX
                        && y >= nTopStartY && y <= nTopEndY){
                // 右上角
                if(nRightTopColor == -1)
                    return;
                pixels[y * builder.nQRCodeSize + x] = nRightTopColor;
            }else if(x >= nLeftStartX && x <= nLeftEndX
                        && y >= nBottomStartY && y <= nBottomEndY){
                // 左下角
                if(nLeftBottomColor == -1)
                    return;
                pixels[y * builder.nQRCodeSize + x] = nLeftBottomColor;
            }

        }
    }

    /**
     * 二维码图标配置信息
     */
    public static class QRCodeLogoInfo {

        public enum ComponentType {

            /**
             * 默认 不带任何效果
             */
            Default {
                @Override
                public int[] renderPixelsWithLogo(QRCodeBuilder builder, BitMatrix bitMatrix) {

                    int[] pixels = new int[builder.nQRCodeSize * builder.nQRCodeSize];
                    for (int y = 0; y < builder.nQRCodeSize; y++) {
                        for (int x = 0; x < builder.nQRCodeSize; x++) {

                            if (bitMatrix.get(x, y)) {
                                // 用nQRCodeFillColor填充二维码
                                pixels[y * builder.nQRCodeSize + x] = builder.nQRCodeFillColor;
                                // 渲染左下，左上，右上正方形
                                builder.mQRCodeCornorSquare.applyToPixels(pixels, builder, x, y);
                            } else {
                                // 用nQRCodeIntervalColor填充二维码其他区域
                                pixels[y * builder.nQRCodeSize + x] = builder.nQRCodeIntervalColor;
                            }
                        }
                    }

                    return pixels;
                }
            },
            /**
             * 二维码logo作为背景
             */
            Background {
                @Override
                public int[] renderPixelsWithLogo(QRCodeBuilder builder, BitMatrix bitMatrix) {
                    // 带Logo信息，根据logo新建一个可以进行矩阵变换的Bitmap
                    // 并且生成的新Logo Bitmap与二维码大小相等
                    builder.mQRCodeLogoInfo.mQRCodeLogo = Bitmap.createScaledBitmap(builder.mQRCodeLogoInfo.mQRCodeLogo, builder.nQRCodeSize, builder.nQRCodeSize, false);

                    int[] pixels = new int[builder.nQRCodeSize * builder.nQRCodeSize];
                    for (int y = 0; y < builder.nQRCodeSize; y++) {
                        for (int x = 0; x < builder.nQRCodeSize; x++) {

                            if (bitMatrix.get(x, y)) {
                                // 用nQRCodeFillColor填充二维码
                                pixels[y * builder.nQRCodeSize + x] = builder.nQRCodeFillColor;
                                // 渲染左下，左上，右上正方形
                                builder.mQRCodeCornorSquare.applyToPixels(pixels, builder, x, y);
                            } else {
                                // 0x66ffffff 带点透明度中和下
                                // 此时不使用nQRCodeIntervalColor
                                pixels[y * builder.nQRCodeSize + x] = builder.mQRCodeLogoInfo.mQRCodeLogo.getPixel(x, y) & 0x66ffffff;
                            }
                        }
                    }

                    return pixels;
                }
            },
            /**
             * 二维码logo覆盖在二维码上
             */
            Foreground {
                @Override
                public int[] renderPixelsWithLogo(QRCodeBuilder builder, BitMatrix bitMatrix) {
                    // 带Logo信息，根据logo新建一个可以进行矩阵变换的Bitmap
                    // 并且生成的新Logo Bitmap与二维码大小相等
                    builder.mQRCodeLogoInfo.mQRCodeLogo = Bitmap.createScaledBitmap(builder.mQRCodeLogoInfo.mQRCodeLogo, builder.nQRCodeSize, builder.nQRCodeSize, false);

                    int width = bitMatrix.getWidth();//矩阵高度
                    int height = bitMatrix.getHeight();//矩阵宽度
                    int halfW = width / 2;
                    int halfH = height / 2;

                    Matrix m = new Matrix();
                    float sx = (float) 2 * builder.mQRCodeLogoInfo.nQRCodeLogoSize / builder.mQRCodeLogoInfo.mQRCodeLogo.getWidth();
                    float sy = (float) 2 * builder.mQRCodeLogoInfo.nQRCodeLogoSize / builder.mQRCodeLogoInfo.mQRCodeLogo.getHeight();
                    m.setScale(sx, sy);
                    //设置缩放信息
                    //将logo图片按martix设置的信息缩放
                    builder.mQRCodeLogoInfo.mQRCodeLogo = Bitmap.createBitmap(builder.mQRCodeLogoInfo.mQRCodeLogo, 0, 0,
                            builder.mQRCodeLogoInfo.mQRCodeLogo.getWidth(), builder.mQRCodeLogoInfo.mQRCodeLogo.getHeight(), m, false);

                    int[] pixels = new int[builder.nQRCodeSize * builder.nQRCodeSize];
                    for (int y = 0; y < builder.nQRCodeSize; y++) {
                        for (int x = 0; x < builder.nQRCodeSize; x++) {

                            if (x > halfW - builder.mQRCodeLogoInfo.nQRCodeLogoSize && x < halfW + builder.mQRCodeLogoInfo.nQRCodeLogoSize
                                    && y > halfH - builder.mQRCodeLogoInfo.nQRCodeLogoSize
                                    && y < halfH + builder.mQRCodeLogoInfo.nQRCodeLogoSize) {
                                //该位置用于存放图片信息
                                //记录图片每个像素信息
                                pixels[y * width + x] = builder.mQRCodeLogoInfo.mQRCodeLogo.getPixel(x - halfW
                                        + builder.mQRCodeLogoInfo.nQRCodeLogoSize, y - halfH + builder.mQRCodeLogoInfo.nQRCodeLogoSize);
                            } else {
                                if (bitMatrix.get(x, y)) {
                                    // 用nQRCodeFillColor填充二维码
                                    pixels[y * builder.nQRCodeSize + x] = builder.nQRCodeFillColor;
                                    // 渲染左下，左上，右上正方形
                                    builder.mQRCodeCornorSquare.applyToPixels(pixels, builder, x, y);
                                } else {
                                    // 用nQRCodeIntervalColor填充二维码其他区域
                                    pixels[y * builder.nQRCodeSize + x] = builder.nQRCodeIntervalColor;
                                }
                            }
                        }
                    }

                    return pixels;
                }
            },
            /**
             * 二维码logo与二维码的交集由logo填充
             */
            Intersect {
                @Override
                public int[] renderPixelsWithLogo(QRCodeBuilder builder, BitMatrix bitMatrix) {
                    // 带Logo信息，根据logo新建一个可以进行矩阵变换的Bitmap
                    // 并且生成的新Logo Bitmap与二维码大小相等
                    builder.mQRCodeLogoInfo.mQRCodeLogo = Bitmap.createScaledBitmap(builder.mQRCodeLogoInfo.mQRCodeLogo, builder.nQRCodeSize, builder.nQRCodeSize, false);

                    int[] pixels = new int[builder.nQRCodeSize * builder.nQRCodeSize];
                    for (int y = 0; y < builder.nQRCodeSize; y++) {
                        for (int x = 0; x < builder.nQRCodeSize; x++) {

                            if (bitMatrix.get(x, y)) {
                                // 用nQRCodeFillColor填充二维码
                                pixels[y * builder.nQRCodeSize + x] = builder.mQRCodeLogoInfo.mQRCodeLogo.getPixel(x, y);
                                // 渲染左下，左上，右上正方形
                                builder.mQRCodeCornorSquare.applyToPixels(pixels, builder, x, y);
                            } else {
                                // 用nQRCodeIntervalColor填充二维码其他区域
                                pixels[y * builder.nQRCodeSize + x] = builder.nQRCodeIntervalColor;
                            }
                        }
                    }

                    return pixels;
                }
            };

            /**
             * 渲染像素数组
             *
             * @param builder
             * @param bitMatrix
             * @return
             */
            public abstract int[] renderPixelsWithLogo(QRCodeBuilder builder, BitMatrix bitMatrix);
        }

        /**
         * 二维码的Logo
         */
        private Bitmap mQRCodeLogo;
        /**
         * 二维码logo与二维码的组合方式
         */
        private ComponentType mComponentType;
        /**
         * 二维码logo的大小
         */
        private int nQRCodeLogoSize;
    }

    public QRCodeBuilder() {
        mQRCodeLogoInfo = new QRCodeLogoInfo();
        mQRCodeLogoInfo.mComponentType = QRCodeLogoInfo.ComponentType.Intersect;
        mQRCodeLogoInfo.mQRCodeLogo = null;

        mQRCodeCornorSquare = new QRCodeCornorSquare();
    }

    /**
     * 二维码内容
     * @param text
     * @return
     */
    public QRCodeBuilder setText(String text) {
        this.mInfoText = text;
        return this;
    }

    /**
     * 内容编码
     * @param charset
     * @return
     */
    public QRCodeBuilder setCharset(String charset) {
        this.mCharSet = charset;
        return this;
    }

    /**
     * 二维码大小
     * @param size
     * @return
     */
    public QRCodeBuilder setQRCodeSize(int size) {
        this.nQRCodeSize = size;
        this.mQRCodeLogoInfo.nQRCodeLogoSize = size / 10;
        return this;
    }

    /**
     * 二维码剩余区域填充色 默认白色
     * @param color
     * @return
     */
    public QRCodeBuilder setQRCodeIntervalColor(int color) {
        this.nQRCodeIntervalColor = color;
        return this;
    }

    /**
     * 二维码填充色 默认黑色
     * @param color
     * @return
     */
    public QRCodeBuilder setQRCodeFillColor(int color) {
        this.nQRCodeFillColor = color;
        return this;
    }

    /**
     * 设置二维码定位基点的颜色
     * @param leftBottomColor
     * @param leftTopColor
     * @param rightTopColor
     * @param fillAll 是否全填
     * @return
     */
    public QRCodeBuilder setCornorColors(int leftBottomColor, int leftTopColor, int rightTopColor, boolean fillAll) {
        mQRCodeCornorSquare.nLeftBottomColor = leftBottomColor;
        mQRCodeCornorSquare.nLeftTopColor = leftTopColor;
        mQRCodeCornorSquare.nRightTopColor = rightTopColor;
        mQRCodeCornorSquare.fillAll = fillAll;
        return this;
    }

    /**
     * 设置二维码logo
     * @param bitmap
     * @return
     */
    public QRCodeBuilder setQRCodeLogo(Bitmap bitmap) {
        mQRCodeLogoInfo.mQRCodeLogo = bitmap;
        return this;
    }

    /**
     * 设置logo与二维码的组合方式
     * @param type
     * @return
     */
    public QRCodeBuilder setQRCodeComponentType(QRCodeLogoInfo.ComponentType type) {
        mQRCodeLogoInfo.mComponentType = type == null ? QRCodeLogoInfo.ComponentType.Intersect : type;
        return this;
    }

    /**
     * 生成二维码图标
     * @return
     */
    public Bitmap build() {

        try {
            QRCode mQRCode = initQRCodeProperty();
            BitMatrix bitMatrix = renderResult(4);// 二维码边框边距等级设置
            mQRCodeCornorSquare.loadQRCodeCornroInfoFromQRCode(this, bitMatrix);

            int[] pixels = mQRCodeLogoInfo.mComponentType.renderPixelsWithLogo(this, bitMatrix);
            Bitmap bitmap = Bitmap.createBitmap(nQRCodeSize, nQRCodeSize,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, nQRCodeSize, 0, 0, nQRCodeSize, nQRCodeSize);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 初始化QRCode对象
     * @return
     * @throws WriterException
     */
    private QRCode initQRCodeProperty() throws WriterException {

        // 设置组合类型
        if (mQRCodeLogoInfo.mQRCodeLogo == null) {
            mQRCodeLogoInfo.mComponentType = QRCodeLogoInfo.ComponentType.Default;
        }

        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        /*
         * 设置容错级别，默认为ErrorCorrectionLevel L(1),M(0),Q(3),H(2);
         * 因为中间加入logo所以建议你把容错级别调至H,否则可能会出现识别不了
         */
        mQRCode = Encoder.encode(mInfoText, ErrorCorrectionLevel.H, hints);

        // 二维码图标的宽高初始化
        int realQRCodeWidth = mQRCode.getMatrix().getWidth();
        int realQRCodeHeight = mQRCode.getMatrix().getHeight();
        if(nQRCodeSize < realQRCodeWidth
                || nQRCodeSize < realQRCodeHeight){
            nQRCodeSize = realQRCodeWidth > realQRCodeHeight ? realQRCodeWidth : realQRCodeHeight;
        }
        // Logo大小
        mQRCodeLogoInfo.nQRCodeLogoSize = nQRCodeSize / 10;

        return mQRCode;
    }

    /**
     * QRCodeWriter源码 根据ByteMatrix生成二维码图标信息的BitMatrix
     * @param  quietZone EncodeHintType.MARGIN
     * @return
     */
    private BitMatrix renderResult(int quietZone){
        ByteMatrix input;
        if((input = mQRCode.getMatrix()) == null) {
            throw new IllegalStateException();
        } else {
            int inputWidth = input.getWidth();
            int inputHeight = input.getHeight();
            int qrWidth = inputWidth + (quietZone << 1);
            int qrHeight = inputHeight + (quietZone << 1);
            int outputWidth = Math.max(nQRCodeSize, qrWidth);
            int outputHeight = Math.max(nQRCodeSize, qrHeight);
            int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
            int leftPadding = (outputWidth - inputWidth * multiple) / 2;
            int topPadding = (outputHeight - inputHeight * multiple) / 2;
            BitMatrix output = new BitMatrix(outputWidth, outputHeight);
            int inputY = 0;

            for (int outputY = topPadding; inputY < inputHeight; outputY += multiple) {
                int inputX = 0;

                for (int outputX = leftPadding; inputX < inputWidth; outputX += multiple) {
                    if (input.get(inputX, inputY) == 1) {
                        output.setRegion(outputX, outputY, multiple, multiple);
                    }

                    ++inputX;
                }

                ++inputY;
            }

            return output;
        }
    }

}
