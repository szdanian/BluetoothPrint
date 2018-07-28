package org.join.image.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

public class JoinImage {

    static {
        System.loadLibrary("JoinImage");
    }

    /**
     * LOG标识
     */
    private static final String TAG = "JoinImage";

    /**
     * 图像存储路径
     */
    public static final String PATH = Environment.getExternalStorageDirectory().toString() + "/" + TAG + "/";

    /**
     * 判断是否有SD卡
     */
    public static boolean hasSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? true : false;
    }

    /**
     * 保存图像为bitName.png
     */
    public static void saveBitmap(String bitName, Bitmap mBitmap) {
        // 不存在SD卡直接返回
        if (!hasSDCard()) {
            return;
        }

        // 判断并创建图像存储路径
        File dirFile = new File(PATH);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }

        // 保存图像为高质量png
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(PATH + bitName + ".png");
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 缩放Bitmap图像并返回
     */
    public static Bitmap stretch(Bitmap mBitmap, int newW, int newh) {
        int w = mBitmap.getWidth(), h = mBitmap.getHeight(); // 获取图像长宽
        int[] colors = getColors(mBitmap, w, h); // 获取Bitmap颜色数组
        colors = stretch(colors, w, h, newW, newh); // 调用动态库方法缩放图像返回颜色数组
        return createBitmap(colors, newW, newh); // 返回由新颜色数组重建的Bitmap
    }

    /**
     * 灰度化Bitmap图像并返回
     */
    public static Bitmap imgToGray(Bitmap mBitmap) {
        int w = mBitmap.getWidth(), h = mBitmap.getHeight(); // 获取图像长宽
        int[] colors = getColors(mBitmap, w, h); // 获取Bitmap颜色数组
        colors = imgToGray(colors, w, h); // 调用动态库方法灰度化颜色数组
        return createBitmap(colors, w, h); // 返回由新颜色数组重建的Bitmap
    }

    /**
     * 二值化灰度图像并返回
     */
    public static int[] binarization(Bitmap mBitmap, byte methodCode) {
        int w = mBitmap.getWidth(), h = mBitmap.getHeight(); // 获取图像长宽
        int[] colors = getColors(mBitmap, w, h); // 获取Bitmap颜色数组
        // 调用动态库方法二值化灰度图像
        switch (methodCode) {
            case 0:
                colors = binarization(colors, w, h);
                break;
            case 1:
                colors = binarization2(colors, w, h);
                break;
            default:
                throw new IllegalArgumentException("请选择正确的二值方法，现有0或1。");
        }
        return colors;//createBitmap(colors, w, h); // 返回由新颜色数组重建的Bitmap
    }

    /**
     * 二值化灰度图像并返回
     */
    public static byte[] binarization(Bitmap mBitmap, int feedPrintSteps) {
        int w = mBitmap.getWidth();  // 获取图像宽宽
        int h = mBitmap.getHeight(); // 获取图像高宽
        int[] colors = getColors(mBitmap, w, h); // 获取Bitmap颜色数组

        byte[] bt = binarization(colors, w, h, feedPrintSteps);
        return bt;
    }

    /**
     * 填充二值化图像并返回
     * <p>
     * 填充方式：背景色点上下左右>=3点为前景色，则将其填充为前景色
     */
    public static Bitmap filling(Bitmap mBitmap) {
        int w = mBitmap.getWidth(), h = mBitmap.getHeight(); // 获取图像长宽
        int[] colors = getColors(mBitmap, w, h); // 获取Bitmap颜色数组
        colors = filling(colors, w, h); // 调用动态库方法膨胀二值化图像
        return createBitmap(colors, w, h); // 返回由新颜色数组重建的Bitmap
    }

    /**
     * 膨胀二值化图像并返回
     * <p>
     * 膨胀结构元素：3x3 全
     */
    public static Bitmap dilation(Bitmap mBitmap) {
        int w = mBitmap.getWidth(), h = mBitmap.getHeight(); // 获取图像长宽
        int[] colors = getColors(mBitmap, w, h); // 获取Bitmap颜色数组
        colors = dilation(colors, w, h); // 调用动态库方法膨胀二值化图像
        return createBitmap(colors, w, h); // 返回由新颜色数组重建的Bitmap
    }

    /**
     * 腐蚀二值化图像并返回
     * <p>
     * 腐蚀结构元素：3x3 全
     */
    public static Bitmap erosion(Bitmap mBitmap) {
        int w = mBitmap.getWidth(), h = mBitmap.getHeight(); // 获取图像长宽
        int[] colors = getColors(mBitmap, w, h); // 获取Bitmap颜色数组
        colors = erosion(colors, w, h); // 调用动态库方法腐蚀二值化图像
        return createBitmap(colors, w, h); // 返回由新颜色数组重建的Bitmap
    }

    /**
     * 腐蚀二值化图像并返回
     * <p>
     * 腐蚀结构元素：3x3 全
     */
    public static Bitmap erosion(Bitmap mBitmap, int iterations) {
        int w = mBitmap.getWidth(), h = mBitmap.getHeight(); // 获取图像长宽
        int[] colors = getColors(mBitmap, w, h); // 获取Bitmap颜色数组
        while (iterations-- > 0) {
            colors = erosion(colors, w, h); // 调用动态库方法腐蚀二值化图像
        }
        return createBitmap(colors, w, h); // 返回由新颜色数组重建的Bitmap
    }

    /**
     * 细化二值化图像并返回
     */
    public static Bitmap thinning(Bitmap mBitmap, int methodCode) {
        int w = mBitmap.getWidth(), h = mBitmap.getHeight(); // 获取图像长宽
        int[] colors = getColors(mBitmap, w, h); // 获取Bitmap颜色数组
        // 调用动态库方法细化二值化图像
        switch (methodCode) {
            case 0:
                colors = thinning(colors, w, h);
                break;
            case 1:
                colors = thinning2(colors, w, h);
                break;
            default:
                throw new IllegalArgumentException("请选择正确的细化方法，现有0或1。");
        }
        return createBitmap(colors, w, h); // 返回由新颜色数组重建的Bitmap
    }

    /**
     * 分割细化图像
     */
    public static void split(Bitmap mBitmap) {
        int w = mBitmap.getWidth(), h = mBitmap.getHeight(); // 获取图像长宽
        int[] colors = getColors(mBitmap, w, h); // 获取Bitmap颜色数组
        split(colors, w, h); // 调用动态库方法分割细化图像
    }

    /**
     * 获取指定索引的分割图像
     */
    public static Bitmap getSplitBmp(int index) {
        // 调用动态库方法获取指定索引分割图像颜色数组
        int[] colors = getSplitImg(index);
        if (null == colors) {
            throw new IllegalStateException("请确认已执行了分割图像，并且索引未越界。");
        }
        // 调用动态库方法获取指定索引分割图像的长宽，并返回由新颜色数组重建的Bitmap
        return createBitmap(colors, getSplitImgW(index), getSplitImgH(index));
    }

    /**
     * 获取所有分割图像
     */
    public static Bitmap[] getSplitBmps() {
        int num = getSplitNum(); // 调用动态库方法获取分割图像个数
        Bitmap bitmap[] = new Bitmap[num];
        for (int i = 0; i < num; i++) {
            bitmap[i] = getSplitBmp(i);
        }
        return bitmap;
    }

    /**
     * 解析识别分割图像
     * <p>
     * mBitmap：单个字符的二值化图像
     */
    public static char analyseImg(Bitmap mBitmap) {
        int w = mBitmap.getWidth(), h = mBitmap.getHeight(); // 获取图像长宽
        int[] colors = getColors(mBitmap, w, h); // 获取Bitmap颜色数组
        return analyseImg(colors, w, h); // 调用动态库方法解析识别分割图像
    }

    /**
     * 二值化身份证号码彩图
     */
    public static Bitmap binaryCid(Bitmap mBitmap) {
        int w = mBitmap.getWidth(), h = mBitmap.getHeight(); // 获取图像长宽
        int[] colors = getColors(mBitmap, w, h); // 获取Bitmap颜色数组
        colors = binaryCid(colors, w, h); // 调用动态库方法二值化彩图身份证号码
        return createBitmap(colors, w, h); // 返回由新颜色数组重建的Bitmap
    }

    /**
     * 获取Bitmap颜色数组
     */
    public static int[] getColors(Bitmap mBitmap, int w, int h) {
        int[] pix = new int[w * h];
        mBitmap.getPixels(pix, 0, w, 0, 0, w, h);
        return pix;
    }

    /**
     * 由颜色数组重建Bitmap
     */
    public static Bitmap createBitmap(int[] colors, int w, int h) {
        Bitmap img = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        img.setPixels(colors, 0, w, 0, 0, w, h);
        return img;
    }

    public static String getTime() {
        return String.valueOf(System.currentTimeMillis());
    }

    public void sayHello(String msg) {
        Log.e("C调用Java", msg);
    }

    public static native int[] stretch(int[] buf, int srcW, int srcH, int dstW, int dstH);

    public static native int[] imgToGray(int[] buf, int w, int h);

    public static native int[] binarization(int[] buf, int w, int h);

    public static native byte[] binarization(int[] buf, int w, int h, int steps);

    public static native int[] binarization2(int[] buf, int w, int h);

    public static native ArrayList<byte[]> formatData(byte[] binarizationData);

    public static native int[] filling(int[] buf, int w, int h);

    public static native int[] dilation(int[] buf, int w, int h);

    public static native int[] erosion(int[] buf, int w, int h);

    public static native int[] thinning(int[] buf, int w, int h);

    public static native int[] thinning2(int[] buf, int w, int h);

    public static native void split(int[] buf, int w, int h);

    public static native int getSplitNum();

    public static native int[] getSplitImg(int index);

    public static native int getSplitImgW(int index);

    public static native int getSplitImgH(int index);

    public static native char analyseImg(int[] buf, int w, int h);

    public static native int[] locateCid(int[] buf, int w, int h);

    public static native int[] binaryCid(int[] buf, int w, int h);
}
