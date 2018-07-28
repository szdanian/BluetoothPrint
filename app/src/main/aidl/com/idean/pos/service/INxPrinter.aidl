// INxPrinter.aidl
package com.idean.pos.service;

import com.idean.pos.service.OnPrintListener;

interface INxPrinter {

	int initPrinter();

	void setConfig(in Bundle bundle);

	int startPrint(in OnPrintListener listener);

	int getStatus();

	int appendPrnStr(String text, int fontsize, boolean isBoldFont, int align);

	int appendBarcode(String content, int height, int margin, int scale, int barcodeFormat, int align);

    int appendQRcode(String content, int height, int align);

	int appendImage(in Bitmap bitmap);

	void feedPaper(int value, int unit);

	void cutPaper();
}
