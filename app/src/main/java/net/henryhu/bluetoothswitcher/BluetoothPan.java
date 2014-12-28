package net.henryhu.bluetoothswitcher;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by henryhu on 14-12-27.
 */
public class BluetoothPan {
    private String errorMsg = null;
    private Method connectMethod = null;
    private Method disconnectMethod = null;
    private Class bluetoothPanClass = null;
    private BluetoothProfile proxy = null;

    public BluetoothPan(BluetoothProfile panProxy) {
        proxy = panProxy;
        try {
            bluetoothPanClass = Class.forName("android.bluetooth.BluetoothPan");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            errorMsg = "Fail to find BluetoothPan class";
            return;
        }
        try {
            connectMethod = bluetoothPanClass.getMethod("connect", BluetoothDevice.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            errorMsg = "Fail to find BluetoothPan.connect(BluetoothDevice)";
            return;
        }
        try {
            disconnectMethod = bluetoothPanClass.getMethod("disconnect", BluetoothDevice.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            errorMsg = "Fail to find BluetoothPan.disconnect(BluetoothDevice)";
            return;
        }
    }

    public boolean connect(BluetoothDevice device) {
        try {
            return (boolean) connectMethod.invoke(proxy, device);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean disconnect(BluetoothDevice device) {
        try {
            return (boolean) disconnectMethod.invoke(proxy, device);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean initialized() {
        return bluetoothPanClass != null && connectMethod != null && disconnectMethod != null;
    }

    public String getError() {
        return errorMsg;
    }
}
