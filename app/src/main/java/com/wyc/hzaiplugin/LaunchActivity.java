package com.wyc.hzaiplugin;

import static android.app.Service.STOP_FOREGROUND_REMOVE;
import static androidx.core.app.ServiceCompat.stopForeground;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.wyc.cloudapp.activity.base.BaseWindowActivity;
import com.wyc.hzaiplugin.ai.aiRecognize.MultiUsbPermissionHelper;
import com.wyc.hzaiplugin.service.AiService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LaunchActivity extends BaseWindowActivity {

    private final static String TAG = "LaunchActivity";
    private final static int REQUEST_CODE_FILE_PERMISSION = 1001;

    private MultiUsbPermissionHelper mMultiUsbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndRequestCameraPermissions();
    }

    public void checkAndRequestCameraPermissions() {
        if(mMultiUsbHelper == null){
            mMultiUsbHelper = new MultiUsbPermissionHelper(this);
            UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            List<UsbDevice> deviceList = new ArrayList<>(usbManager.getDeviceList().values());
            mMultiUsbHelper.requestMultiplePermissions(deviceList, (grantedDevices, deniedDevices) -> {
                Log.d("USB", "所有端口处理完成！成功设备数: " + grantedDevices.size() + ", 失败设备数: " + deniedDevices.size());
                rxPermissionsAndroid11();
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMultiUsbHelper.destroy();
        stopService(new Intent(this, AiService.class));
    }

    private void rxPermissionsAndroid11() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, REQUEST_CODE_FILE_PERMISSION);
            } else {
                checkPermission();
            }
        } else {
            checkPermission();
        }
    }

    private void checkPermission() {
        List<String> permissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14 (API 34)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) { // Android 13 (API 33)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.READ_PHONE_STATE);

        PermissionUtils.permission(
                permissions.toArray(new String[0])
        ).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(@NonNull List<String> granted) {
                Log.d(TAG,"onGranted:" + Arrays.toString(granted.toArray()));
                bindAiService();
            }

            @Override
            public void onDenied(@NonNull List<String> deniedForever, @NonNull List<String> denied) {
                Log.d(TAG,"onDenied:" + Arrays.toString(denied.toArray()));
            }
        }).request();
    }

    private void bindAiService(){

        final Intent intent = new Intent(this, AiService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        startActivity(new Intent(this, MainActivity.class));

        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FILE_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    checkPermission();
                } else {
                    ToastUtils.showShort("当前权限没有打开，请前往设置页面开启");
                }
            }
        }else if(requestCode == 99){
            rxPermissionsAndroid11();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMultiUsbHelper.onCameraPermissionGranted();
            } else {
                Toast.makeText(this, "拒绝相机权限将导致 USB 摄像头不可用", Toast.LENGTH_SHORT).show();
            }
        }
    }
}