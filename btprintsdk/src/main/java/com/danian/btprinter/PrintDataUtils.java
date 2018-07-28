package com.danian.btprinter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import java.util.List;

/**
 * Created by Danian on 2018/3/8.
 */

public class PrintDataUtils {
    private static final int DEFAULT_PRINT_WIDTH = 384;
    private static final int DEFALUT_TOP_SPACE = 3;
    private static final int ALIGN_LEFT = 0;
    private static final int ALIGN_CENTER = 1;
    private static final int ALIGN_RIGHT = 2;

    private List<MessageStyle> messageList;
    private Paint mPaint;
    private final Bitmap mBitmap;
    private final Canvas mCanvas;

    public PrintDataUtils(List<MessageStyle> textList) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth((float) 0.8);
        mPaint.setTypeface(Typeface.SANS_SERIF);
        mPaint.setAlpha(0xFF);

        messageList = textList;
        mBitmap = Bitmap.createBitmap(DEFAULT_PRINT_WIDTH, countBitmapHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.WHITE);
        drawBitmap();
    }

    public PrintDataUtils(Bitmap bitmap) {
        int height;
        if (bitmap.getWidth() > DEFAULT_PRINT_WIDTH) {
            height = bitmap.getHeight() * DEFAULT_PRINT_WIDTH / bitmap.getWidth();
        } else {
            height = bitmap.getHeight();
        }

        mBitmap = Bitmap.createBitmap(DEFAULT_PRINT_WIDTH, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.WHITE);
        mCanvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                new Rect(0, 0, DEFAULT_PRINT_WIDTH, height), null);
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    private void drawBitmap() {
        Rect textBound = new Rect();
        float baseLine = DEFALUT_TOP_SPACE;
        float left;

        for (MessageStyle message : messageList) {
            mPaint.setTypeface(message.typeFace);
            mPaint.setTextSize(message.textSize);
            mPaint.getTextBounds(message.text, 0, message.text.length(), textBound);
            baseLine += message.topSpace - (mPaint.getFontMetrics().top + mPaint.getFontMetrics().bottom) / 2;
            if (message.printAlign == ALIGN_LEFT) {
                left = message.leftSpace;
            } else if (message.printAlign == ALIGN_CENTER) {
                left = (DEFAULT_PRINT_WIDTH - mPaint.measureText(message.text)) / 2;
            } else {
                left = DEFAULT_PRINT_WIDTH - mPaint.measureText(message.text);
            }
            mCanvas.drawText(message.text, left, baseLine, mPaint);

            if (message.isCutLine) {
                baseLine += message.topSpace;
                mPaint.setStrokeWidth((float) 2.5);
                mCanvas.drawLine(0, baseLine, DEFAULT_PRINT_WIDTH, baseLine, mPaint);
                mPaint.setStrokeWidth((float) 1.0);
            }
            baseLine += mPaint.getFontMetrics().bottom;
            baseLine += DEFALUT_TOP_SPACE;
        }
    }

    private int countBitmapHeight() {
        int height = 0;
        for (MessageStyle message : messageList) {
            mPaint.setTextSize(message.textSize);
            height += message.topSpace - mPaint.getFontMetrics().top - mPaint.getFontMetrics().bottom;
            if (message.isCutLine) {
                height += message.topSpace;
            }
        }
        return height;
    }

    public static class MessageStyle {
        private String text;
        private Typeface typeFace;                                      // 字体形状
        private int textSize;                                           // 字体大小
        private int leftSpace;                                          // 左间距
        private int topSpace;                                           // 上间距
        private int printAlign;                                         // 打印位置
        private boolean isCutLine;                                      // 打印分割线
    }

    public static MessageStyle createPrintLine(String text, Typeface fontStyle, int textSize, int leftSpace,
                                               int topSpace, int printAlign, boolean isCutLine) {
        MessageStyle message = new MessageStyle();
        message.text = text;
        message.typeFace = fontStyle;
        message.textSize = textSize;
        message.leftSpace = leftSpace;
        message.topSpace = topSpace;
        message.printAlign = printAlign;
        message.isCutLine = isCutLine;
        return message;
    }
}
