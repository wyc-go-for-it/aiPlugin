package com.wyc.hzaiplugin.ai.aiRecognize;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;


import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.sanqn.ai.core.bean.BaseGoodsInfo;
import com.sanqn.ai.core.bean.OriginInfo;
import com.sanqn.ai.core.bean.SkuInfo;
import com.sanqn.ai.core.common.GsonUtils;
import com.sanqn.ai.lib.Callback;
import com.sanqn.ai.lib.OnActivateListener;
import com.sanqn.ai.lib.Response;
import com.sanqn.ai.lib.SanQNSDK;
import com.sanqn.ai.lib.bean.CameraInfo;
import com.sanqn.ai.lib.bean.Configuration;
import com.sanqn.ai.lib.bean.MarkType;
import com.wyc.ai.AiGoods;
import com.wyc.hzaiplugin.App;
import com.wyc.hzaiplugin.Const;
import com.wyc.hzaiplugin.ai.aiRecognizeInterface.liuxxSetting.LiuXXAiSettingDialog;
import com.wyc.hzaiplugin.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class LiuXXAi extends AIRecognizeImp {

    private boolean mEnable;

    private long mRecogId = 0L;

    private final List<AiGoods> aiGoodsList = new ArrayList<>();

    private long mFirstRecogId = 0;

    public LiuXXAi(){
        initSdk();
    }

    private final static String TAG = "LiuXXAi";

    @Override
    public boolean isEnable() {
        return mEnable;
    }

    @Override
    public void open() {

    }

    @Override
    public void close() {
        super.close();
        if (isEnable()){
            SanQNSDK.app().release().execute();
        }
    }

    @Override
    public void submitInfo(AiGoods aiGoods) {
        MarkType markType = MarkType.search;
        if (mRecogId != 0) {
            markType = MarkType.output;
        }

        BaseGoodsInfo info = new BaseGoodsInfo();
        info.setSkuCode(aiGoods.getId());
        info.setSkuName(aiGoods.getName());

        SanQNSDK.ai().mark()
                .goodsInfo(info)
                .markType(markType)
                .recogId(mRecogId)
                .enqueue(new Callback<Boolean>() {
                    @Override
                    public void callback(Response<Boolean> response) {
                        mRecogId = 0L;
                    }
                });
    }

    @Override
    public void setting(Context activity) {
        final LiuXXAiSettingDialog dialog = new LiuXXAiSettingDialog(activity);
        dialog.show();
    }

    public static boolean checkCameraUsb(Activity activity){
        final SharedPreferences preferences= activity.getSharedPreferences("cameraInfo", Context.MODE_PRIVATE);
        int cameraInfoVid = preferences.getInt("vid",0);
        UsbManager manager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();

        int c = 0;

        for (UsbDevice device : deviceList.values()) {
            boolean hasPermission = manager.hasPermission(device);
            Log.d(TAG,String.format(Locale.CHINA,"hasPermission:%s,name:%s", hasPermission, device.getDeviceName()));
            if (!hasPermission) {
                PendingIntent pi = PendingIntent.getBroadcast(
                        activity, 99, new Intent(Const.ACTION_USB_PERMISSION),
                        PendingIntent.FLAG_IMMUTABLE
                );
                manager.requestPermission(device, pi);

                c++;
            }
        }
        return c == 0;
    }

    private void initSdk() {

        final SharedPreferences preferences= App.self().getSharedPreferences("cameraInfo", Context.MODE_PRIVATE);
        final int vid = preferences.getInt("vid",0);
        final int pid = preferences.getInt("pid",0);
        final int cameraType = preferences.getInt("cameraType",0);

        Log.d("LiuXXAi",String.format(Locale.CHINA,"vid:%d,pid:%d",vid,pid));

        Configuration configuration;
        if(vid != 0 && pid != 0){
            final String id = preferences.getString("id","");
            final String name = preferences.getString("name","");
            configuration = new Configuration.Builder().cameraType(cameraType)
                    .cameraInfo(new CameraInfo(id,name,vid,pid)).miniRecogWeight(5)
                    .build();
        }else{
            configuration = new Configuration.Builder().cameraType(cameraType)
                    .build();
        }
        SanQNSDK.app().init().context(App.self())
                .initConfigure(configuration)
                .originInfo(new OriginInfo("CW", "3b5e0b76a2d74b4c8002fd46a3dbab95"))
                .onActivateListener(new OnActivateListener() {
                    @Override
                    public void onActivate(Response<Boolean> value) {
                        mEnable = value.isSuccessful();

                        Log.d("LiuXXAi",String.format(Locale.CHINA,"onActivate %s", "code:" + value.getCode() + " message:" + value.getMessage() + " enable:" + mEnable));

                        if(mEnable){
                            SanQNSDK.ai().onRecogListener().onRecogListener(recogData -> {

                                Log.d("LiuXXAi","识别结果：" + GsonUtils.toJson(recogData));

                                if (recogData != null && mListener != null) {
                                    mRecogId = recogData.getRecogId();
                                    aiGoodsList.clear();
                                    for (SkuInfo pluBaseInfo : recogData.getResultList()) {
                                        final AiGoods aiGoods = new AiGoods();
                                        aiGoods.setId(pluBaseInfo.getSkuCode());
                                        aiGoods.setName(pluBaseInfo.getSkuName());
                                        aiGoodsList.add(aiGoods);
                                    }
                                    mListener.onGoodsDelivery(aiGoodsList);
                                }
                            }).enqueue(new Callback<Boolean>() {
                                @Override
                                public void callback(Response<Boolean> response) {
                                    Log.d("LiuXXAi","注册识别回调:" + response.isSuccessful());
                                }
                            });
                        }
                    }
                }).enqueue(new Callback<Boolean>() {
                    @Override
                    public void callback(Response<Boolean> response) {
                        Log.d(TAG,"SDk初始化:"+ response.isSuccessful());
                    }
                });
    }

    @Override
    public void recognize(double num) {
        if (isEnable()){

            if(Utils.equalDouble(0.0,num) && mListener != null){
                aiGoodsList.clear();
                mListener.onGoodsDelivery(aiGoodsList);
            }

            SanQNSDK.ai().recog()
                    .weight((int) (1000 * num))
                    .weightStable(true)
                    .isForce(false)
                    .enqueue(new Callback<Boolean>() {
                        @Override
                        public void callback(Response<Boolean> response) {
                        }
                    });
        }
    }
}
