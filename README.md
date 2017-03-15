# QRCoder
基于zxing 3.3的二维码生成，二维码解析

## 二维码扫描
* QRCodeScannerView 集成入Layout文件
* QRCodeScannerView.setDecodeCallback(DecodeCallback callback) 二维码扫描的结果回调
```Java
  QRCodeScannerView view;
  view.set(DecodeCallback callback)
```

## 二维码生成 QRCodeBuilder
* 普通模式
* 加入Logo，Logo在二维码中心
* 加入Logo, Logo作为二维码底色
* 加入Logo，Logo填充Logo与二维码重叠部分
* 自定义二维码底色，二维码填充色
* 自定义二维码定位点颜色
* 自定义二维码宽高
