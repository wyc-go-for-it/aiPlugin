package com.wyc.hzaiplugin.ai.aiRecognizeInterface.liuxxSetting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ToastUtils;
import com.sanqn.ai.core.bean.DeviceData;
import com.sanqn.ai.core.bean.ShopInfo;
import com.sanqn.ai.lib.Callback;
import com.sanqn.ai.lib.Response;
import com.sanqn.ai.lib.SanQNSDK;
import com.wyc.hzaiplugin.R;
import com.wyc.hzaiplugin.baseDialog.AbstractDialog;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class LiuXXAiSettingDialog extends AbstractDialog {

    private TextView tvSN;
    private TextView tvShopId;
    private TextView status_name;

    private TextView shop_name_device;

    private EditText shop_name;
    private EditText active_code;


    public LiuXXAiSettingDialog(@NonNull Context context) {
        super(context, "六小象Ai平台参数设置");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDeviceInfo();

        final Button save = findViewById(R.id.save);
        save.setOnClickListener(view -> save());

        final Button markArea = findViewById(R.id.mark_area);
        markArea.setOnClickListener(view -> markArea());

        final Button cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(view -> _cancel());
    }

    private void initDeviceInfo(){
        tvSN = findViewById(R.id.tvSN);
        tvShopId = findViewById(R.id.shop_id);
        status_name = findViewById(R.id.status_name);
        shop_name_device = findViewById(R.id.shop_name_device);

        active_code = findViewById(R.id.active_code);
        shop_name = findViewById(R.id.shop_name);

        deviceInfo();
    }

    private void deviceInfo() {
        SanQNSDK.app().deviceInfo().enqueue(response -> {
            if (response.isSuccessful()) {
                DeviceData deviceData = response.getData();
                if(deviceData != null){
                    tvSN.setText(deviceData.getSn());
                    tvShopId.setText(deviceData.getShopCode());
                    if(deviceData.getStatus() != null && deviceData.getStatus() == 1){
                        status_name.setText("已激活");
                    }
                    shop_name_device.setText(deviceData.getShopName());
                }
            } else {
                ToastUtils.showShort("获取设备信息失败 code:" + response.getCode() + " message:" + response.getMessage());
            }
        });
    }

    void save(){
        SanQNSDK.app().activate()
                .cdKey(active_code.getText().toString())
                .shopInfo(new ShopInfo(shop_name.getText().toString(), "3b5e0b76a2d74b4c8002fd46a3dbab95"))
                .enqueue(new Callback<Boolean>() {
                    @Override
                    public void callback(Response<Boolean> reply) {
                        if (reply.isSuccessful()) {
                            ToastUtils.showShort("激活成功");
                        } else {
                            ToastUtils.showShort("激活失败 code:" + reply.getCode() + " message:" + reply.getMessage());
                        }
                    }
                });
    }

    void markArea(){
        Intent intent = new Intent(getContext(), SettingActivity.class);
        getContext().startActivity(intent);
    }

    void _cancel(){
        closeWindow();
    }

    @Override
    protected int getContentLayoutId() {
        return  R.layout.liuxx_ai_setting;
    }
}

