package com.wyc.hzaiplugin.ai.aiRecognize;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

public class MultiUsbPermissionHelper {

    public interface OnAllPermissionsResultListener {
        void onCompleted(List<UsbDevice> grantedDevices, List<UsbDevice> deniedDevices);
    }

    private final Activity mActivity;
    private final UsbManager mUsbManager;
    private final Queue<UsbDevice> mDeviceQueue = new LinkedList<>();
    private final List<UsbDevice> mGrantedDevices = new ArrayList<>();
    private final List<UsbDevice> mDeniedDevices = new ArrayList<>();
    private OnAllPermissionsResultListener mFinalListener;

    private final List<BroadcastReceiver> mRegisteredReceivers = new CopyOnWriteArrayList<>();

    public MultiUsbPermissionHelper(Activity activity) {
        this.mActivity = activity;
        this.mUsbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
    }

    /**
     * 批量请求多个 USB 设备权限
     */
    public void requestMultiplePermissions(List<UsbDevice> devices, OnAllPermissionsResultListener listener) {
        if (devices == null || devices.isEmpty()) {
            if (listener != null) listener.onCompleted(mGrantedDevices, mDeniedDevices);
            return;
        }

        this.mFinalListener = listener;
        this.mDeviceQueue.clear();
        this.mGrantedDevices.clear();
        this.mDeniedDevices.clear();

        // 检查队列中是否有 UVC 视频摄像头
        boolean hasCameraDevice = false;
        for (UsbDevice dev : devices) {
            if (isUvcDevice(dev)) {
                hasCameraDevice = true;
                break;
            }
        }

        // 如果有 USB 摄像头，且还没有原生 CAMERA 权限，先申请原生相机权限
        if (hasCameraDevice && ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // 存入队列，等待 CAMERA 权限回调后再启动 USB 队列
            mDeviceQueue.addAll(devices);
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.CAMERA}, 2001);
            return;
        }

        // 过滤掉已有权限的设备，将需要弹窗的入队
        for (UsbDevice dev : devices) {
            if (mUsbManager.hasPermission(dev)) {
                mGrantedDevices.add(dev);
            } else {
                mDeviceQueue.offer(dev);
            }
        }

        // 开始串行处理队列中的设备
        processNextDevice();
    }

    /**
     * CAMERA 权限申请完后调用此方法接续
     */
    public void onCameraPermissionGranted() {
        List<UsbDevice> remainingList = new ArrayList<>(mDeviceQueue);
        mDeviceQueue.clear();
        requestMultiplePermissions(remainingList, mFinalListener);
    }

    /**
     * 核心：一次只为一个设备弹窗请求
     */
    private void processNextDevice() {
        UsbDevice targetDevice = mDeviceQueue.poll();

        // 队列为空，说明全部设备处理完毕，回调结果
        if (targetDevice == null) {
            if (mFinalListener != null) {
                mFinalListener.onCompleted(mGrantedDevices, mDeniedDevices);
            }
            return;
        }

        final String action = "com.wyc.USB_PERMISSION." + targetDevice.getDeviceId();

        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (action.equals(intent.getAction())) {
                    safeUnregisterReceiver(this);

                    boolean isGranted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (isGranted && device != null) {
                        mGrantedDevices.add(device);
                    } else {
                        mDeniedDevices.add(targetDevice);
                    }

                    // 当前端口授权结束，自动递归调用处理下一个端口！
                    processNextDevice();
                }
            }
        };


        IntentFilter filter = new IntentFilter(action);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mActivity.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            mActivity.registerReceiver(receiver, filter);
        }

        mRegisteredReceivers.add(receiver);

        // PendingIntent
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_MUTABLE;
        }

        PendingIntent pi = PendingIntent.getBroadcast(
                mActivity, targetDevice.getDeviceId(), new Intent(action), flags
        );

        // 触发弹窗
        mUsbManager.requestPermission(targetDevice, pi);
    }

    /**
     * 判断是否为 UVC 视频设备
     */
    private boolean isUvcDevice(UsbDevice device) {
        if (device.getDeviceClass() == UsbConstants.USB_CLASS_VIDEO) return true;
        for (int i = 0; i < device.getInterfaceCount(); i++) {
            if (device.getInterface(i).getInterfaceClass() == UsbConstants.USB_CLASS_VIDEO) {
                return true;
            }
        }
        return false;
    }

    private void safeUnregisterReceiver(BroadcastReceiver receiver) {
        if (receiver != null && mRegisteredReceivers.contains(receiver)) {
            try {
                mActivity.unregisterReceiver(receiver);
            } catch (Exception ignored) {}
            mRegisteredReceivers.remove(receiver);
        }
    }

    public void destroy() {
        for (BroadcastReceiver receiver : mRegisteredReceivers) {
            try {
                mActivity.unregisterReceiver(receiver);
            } catch (Exception ignored) {}
        }
        mRegisteredReceivers.clear();
        mDeviceQueue.clear();
        mFinalListener = null;
    }
}