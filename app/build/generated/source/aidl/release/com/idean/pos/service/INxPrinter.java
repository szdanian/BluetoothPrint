/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/danian/Documents/Developer/BluetoothPrint/BluetoothPrint/app/src/main/aidl/com/idean/pos/service/INxPrinter.aidl
 */
package com.idean.pos.service;
public interface INxPrinter extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.idean.pos.service.INxPrinter
{
private static final java.lang.String DESCRIPTOR = "com.idean.pos.service.INxPrinter";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.idean.pos.service.INxPrinter interface,
 * generating a proxy if needed.
 */
public static com.idean.pos.service.INxPrinter asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.idean.pos.service.INxPrinter))) {
return ((com.idean.pos.service.INxPrinter)iin);
}
return new com.idean.pos.service.INxPrinter.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_initPrinter:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.initPrinter();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setConfig:
{
data.enforceInterface(DESCRIPTOR);
android.os.Bundle _arg0;
if ((0!=data.readInt())) {
_arg0 = android.os.Bundle.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.setConfig(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_startPrint:
{
data.enforceInterface(DESCRIPTOR);
com.idean.pos.service.OnPrintListener _arg0;
_arg0 = com.idean.pos.service.OnPrintListener.Stub.asInterface(data.readStrongBinder());
int _result = this.startPrint(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getStatus:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getStatus();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_appendPrnStr:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
boolean _arg2;
_arg2 = (0!=data.readInt());
int _arg3;
_arg3 = data.readInt();
int _result = this.appendPrnStr(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_appendBarcode:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
int _arg2;
_arg2 = data.readInt();
int _arg3;
_arg3 = data.readInt();
int _arg4;
_arg4 = data.readInt();
int _arg5;
_arg5 = data.readInt();
int _result = this.appendBarcode(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_appendQRcode:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
int _arg2;
_arg2 = data.readInt();
int _result = this.appendQRcode(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_appendImage:
{
data.enforceInterface(DESCRIPTOR);
android.graphics.Bitmap _arg0;
if ((0!=data.readInt())) {
_arg0 = android.graphics.Bitmap.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
int _result = this.appendImage(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_feedPaper:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
this.feedPaper(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_cutPaper:
{
data.enforceInterface(DESCRIPTOR);
this.cutPaper();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.idean.pos.service.INxPrinter
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public int initPrinter() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_initPrinter, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void setConfig(android.os.Bundle bundle) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((bundle!=null)) {
_data.writeInt(1);
bundle.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_setConfig, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int startPrint(com.idean.pos.service.OnPrintListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_startPrint, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getStatus() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getStatus, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int appendPrnStr(java.lang.String text, int fontsize, boolean isBoldFont, int align) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(text);
_data.writeInt(fontsize);
_data.writeInt(((isBoldFont)?(1):(0)));
_data.writeInt(align);
mRemote.transact(Stub.TRANSACTION_appendPrnStr, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int appendBarcode(java.lang.String content, int height, int margin, int scale, int barcodeFormat, int align) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(content);
_data.writeInt(height);
_data.writeInt(margin);
_data.writeInt(scale);
_data.writeInt(barcodeFormat);
_data.writeInt(align);
mRemote.transact(Stub.TRANSACTION_appendBarcode, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int appendQRcode(java.lang.String content, int height, int align) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(content);
_data.writeInt(height);
_data.writeInt(align);
mRemote.transact(Stub.TRANSACTION_appendQRcode, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int appendImage(android.graphics.Bitmap bitmap) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((bitmap!=null)) {
_data.writeInt(1);
bitmap.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_appendImage, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void feedPaper(int value, int unit) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(value);
_data.writeInt(unit);
mRemote.transact(Stub.TRANSACTION_feedPaper, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void cutPaper() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_cutPaper, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_initPrinter = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_setConfig = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_startPrint = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_appendPrnStr = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_appendBarcode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_appendQRcode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_appendImage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_feedPaper = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_cutPaper = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
}
public int initPrinter() throws android.os.RemoteException;
public void setConfig(android.os.Bundle bundle) throws android.os.RemoteException;
public int startPrint(com.idean.pos.service.OnPrintListener listener) throws android.os.RemoteException;
public int getStatus() throws android.os.RemoteException;
public int appendPrnStr(java.lang.String text, int fontsize, boolean isBoldFont, int align) throws android.os.RemoteException;
public int appendBarcode(java.lang.String content, int height, int margin, int scale, int barcodeFormat, int align) throws android.os.RemoteException;
public int appendQRcode(java.lang.String content, int height, int align) throws android.os.RemoteException;
public int appendImage(android.graphics.Bitmap bitmap) throws android.os.RemoteException;
public void feedPaper(int value, int unit) throws android.os.RemoteException;
public void cutPaper() throws android.os.RemoteException;
}
