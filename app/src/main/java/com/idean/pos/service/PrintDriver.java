package com.idean.pos.service;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.RemoteException;

import com.danian.btmanage.BluetoothInterface.ErrorCode;
import com.danian.btmanage.BluetoothInterface.OnPrintCallback;
import com.danian.btprinter.BtptPrinter;
import com.danian.btprinter.PrintDataUtils;
import com.danian.btprinter.PrintDataUtils.MessageStyle;

import com.jxnx.smartpos.api.device.printer.FeedUnit;
import com.jxnx.smartpos.api.device.printer.FontFamily;
import com.jxnx.smartpos.api.device.printer.PrintAlign;
import com.jxnx.smartpos.api.ServiceResult;

import java.util.ArrayList;
import java.util.List;

import org.join.image.util.JoinImage;

/**
 * Created by Danian on 2018/3/8.
 */

public class PrintDriver extends INxPrinter.Stub {
    private OnPrintListener mListener;
    private final BtptPrinter mBtPrinter;
    private final List<MessageStyle> mlist = new ArrayList<>();
    private Bundle mBundle;
    private Bitmap mBitmap;
    private boolean isText;
    private int printerStatus;

    private PrintDriver(BtptPrinter btPrinter) {
        mBtPrinter = btPrinter;
        isText = true;
    }

    public static PrintDriver getInstance(BtptPrinter btPrinter) {
        return new PrintDriver(btPrinter);
    }

    public final OnPrintCallback printCallback = new OnPrintCallback() {
        @Override
        public void onFinish() {
            try {
                printerStatus = ServiceResult.Success;
                if (mListener != null) {
                    mListener.onPrintResult(printerStatus);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFail(int errorCode) {
            try {
                switch (errorCode) {
                    case ErrorCode.ERROR_NO_PAPER:
                        printerStatus = ServiceResult.Printer_PaperLack;
                        break;
                    case ErrorCode.ERROR_OVERTEMP:
                        printerStatus = ServiceResult.Printre_TooHot;
                        break;
                    case ErrorCode.ERROR_PRINT_BUSY:
                        printerStatus = ServiceResult.Printer_Busy;
                        break;
                    case ErrorCode.ERROR_OPEN_FAIL:
                        printerStatus = ServiceResult.Printer_Fault;
                        break;
                    case ErrorCode.ERROR_SEND_FAIL:
                        printerStatus = ServiceResult.Printer_Wrong_Package;
                        break;
                    case ErrorCode.ERROR_APPLY_FAIL:
                        printerStatus = ServiceResult.Printer_Print_Fail;
                        break;
                    default:
                        printerStatus = ServiceResult.Printer_Other_Error;
                }
                if (mListener != null && errorCode != ErrorCode.ERROR_OK) {
                    //mListener.onPrintResult(printerStatus);
                    mListener.onPrintResult(errorCode);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public int initPrinter() {
        if (mBtPrinter.isConnected()) {
            printerStatus = ServiceResult.Success;
        } else {
            printerStatus = ServiceResult.Fail;
        }
        return printerStatus;
    }

    @Override
    public void setConfig(Bundle bundle) {
        mBundle = bundle;
    }

    @Override
    public int startPrint(OnPrintListener listener) {
        byte[] printData;

        if (isText) {
            if (!mlist.isEmpty()) {
                PrintDataUtils dataUtils = new PrintDataUtils(mlist);
                printData = JoinImage.binarization(dataUtils.getBitmap(), 0);
                mlist.clear();
            } else {
                return ServiceResult.Printer_UnFinished;
            }
        } else {
            if (mBitmap != null && !mBitmap.isRecycled()) {
                PrintDataUtils dataUtils = new PrintDataUtils(mBitmap);
                printData = JoinImage.binarization(dataUtils.getBitmap(), 0);
                mBitmap.recycle();
            } else {
                return ServiceResult.Printer_UnFinished;
            }
        }

        mListener = listener;
        ArrayList<byte[]> mFormatData = JoinImage.formatData(printData);
        mBtPrinter.startPrint(printCallback, mFormatData);
        return ServiceResult.Success;
    }

    @Override
    public int getStatus() {
        return printerStatus;
    }

    @Override
    public int appendPrnStr(String text, int fontsize, boolean isBoldFont, int align) {
        final int[] textSize = new int[]{24, 29, 34, 0};
        final int fontFamliy;

        if (isBoldFont == FontFamily.NOT_NEED_BOLD) {
            fontFamliy = Typeface.NORMAL;
        } else {
            fontFamliy = Typeface.BOLD;
        }

        mlist.add(PrintDataUtils.createPrintLine(text, Typeface.create("宋体", fontFamliy),
                textSize[fontsize], 0, 10, align, false));
        return ServiceResult.Success;
    }

    @Override
    public int appendBarcode(String content, int height, int margin, int scale, int barcodeFormat, int align) {
        return ServiceResult.Printer_Other_Error;
    }

    @Override
    public int appendQRcode(String content, int height, int align) {
        return ServiceResult.Printer_Other_Error;
    }

    @Override
    public int appendImage(Bitmap bitmap) {
        mBitmap = bitmap;
        isText = false;
        return ServiceResult.Success;
    }

    @Override
    public void feedPaper(int value, int unit) {
        if (value > 0) {
            if (unit == FeedUnit.LINE) {
                for (int step = 0; step < value; step++) {
                    appendPrnStr("", FontFamily.MIDDLE, FontFamily.NOT_NEED_BOLD, PrintAlign.LEFT);
                }
            } else {
                for (int step = 0; step < value; step++) {
                    appendPrnStr("", 3, FontFamily.NOT_NEED_BOLD, PrintAlign.LEFT);
                }
            }

            PrintDataUtils dataUtils = new PrintDataUtils(mlist);
            mlist.clear();
            mBitmap = dataUtils.getBitmap();
            byte[] printData = JoinImage.binarization(mBitmap, 0);
            mBitmap.recycle();

            ArrayList<byte[]> mFormatData = JoinImage.formatData(printData);
            mBtPrinter.startPrint(printCallback, mFormatData);
        }
    }

    @Override
    public void cutPaper() {

    }

    public void blankLine(int line) {
        for (int num = 0; num < line; num++) {
            appendPrnStr("", FontFamily.MIDDLE, FontFamily.NOT_NEED_BOLD, PrintAlign.LEFT);
        }
    }
}
