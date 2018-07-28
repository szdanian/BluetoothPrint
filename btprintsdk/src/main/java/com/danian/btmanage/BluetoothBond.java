package com.danian.btmanage;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Danian on 2018/3/5.
 */

public class BluetoothBond {

    // 与设备配对
    public static boolean createBond(Class btClass, BluetoothDevice btDevice)
            throws Exception {
        Method createBondMethod = btClass.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    // 解绑配对设备
    public static boolean removeBond(Class<?> btClass, BluetoothDevice btDevice)
            throws Exception {
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    // 设置PIN
    public static boolean setPin(Class<? extends BluetoothDevice> btClass, BluetoothDevice btDevice, String str)
            throws Exception {
        try {
            Method removeBondMethod = btClass.getDeclaredMethod("setPin", new Class[]{byte[].class});
            Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice, new Object[]{str.getBytes()});
            Log.e("returnValue: ", "" + returnValue);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    // 取消用户输入
    public static boolean cancelPairingUserInput(Class<?> btClass, BluetoothDevice device)
            throws Exception {
        Method createBondMethod = btClass.getMethod("cancelPairingUserInput");
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue.booleanValue();
    }

    // 取消正在配对
    public static boolean cancelBondProcess(Class<?> btClass, BluetoothDevice device)
            throws Exception {
        Method createBondMethod = btClass.getMethod("cancelBondProcess");
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue.booleanValue();
    }

    // 确认配对
    public static void setPairingConfirmation(Class<?> btClass, BluetoothDevice device, boolean isConfirm)
            throws Exception {
        Method setPairingConfirmation = btClass.getDeclaredMethod("setPairingConfirmation", boolean.class);
        setPairingConfirmation.invoke(device, isConfirm);
    }

    // 打印所有方法信息
    public static void printAllInform(Class clsShow) {
        try {
            Method[] hideMethod = clsShow.getMethods();
            for (int i = 0; i < hideMethod.length; i++) {
                Log.e("method name: ", hideMethod[i].getName() + "; and the i is: " + i);
            }

            Field[] allFields = clsShow.getFields();
            for (int i = 0; i < allFields.length; i++) {
                Log.e("Field name: ", allFields[i].getName());
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
