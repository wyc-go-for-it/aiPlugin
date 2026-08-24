package com.wyc.hzaiplugin.ai.aiRecognizeInterface;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sdm.ai_sdk.AiDeviceHelper;
import com.sdm.ai_sdk.BaseInfo;
import com.sdm.ai_sdk.WeightHelper;
import com.sdm.ai_sdk.bean.DeviceInfo;
import com.sdm.ai_sdk.bean.ScalePosInfo;
import com.wyc.hzaiplugin.utils.Utils;
import com.wyc.hzaiplugin.R;
import com.wyc.hzaiplugin.baseDialog.AbstractDialog;
import com.wyc.hzaiplugin.baseDialog.MyDialog;

import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.dialog.serialScales
 * @ClassName: BalanceAiSettingDialog
 * @Description: 佰伦斯AI设置
 * @Author: wyc
 * @CreateDate: 2023-06-30 9:59
 * @UpdateUser: 更新者：
 * @UpdateDate: 2023-06-30 9:59
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class BalanceAiSettingDialog extends AbstractDialog {
    private static final double UI_POSITION_RATIO = 0.75;

    private TextView tvSN;
    private TextView tvShopId;
    private TextView tvShopName;
    private TextView tvFirmwareVersion;
    private TextView tvSDKVersion;
    private TextView tvModelVersion;
    private TextView tvConfigVersion;
    private TextView tvScaleType;

    private EditText storeCode;
    private ImageView mIvCamera;
    private MarkRectView markView;

    private Button shopBind,submit,takePhoto,rest;

    public BalanceAiSettingDialog(@NonNull Context context) {
        super(context, context.getString(R.string.balance_ai_setting));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storeCode = findViewById(R.id.store_code);
        mIvCamera = findViewById(R.id.iv_camera_shot);
        markView = findViewById(R.id.rect_view);

        shopBind =findViewById(R.id.bind_btn);
        shopBind.setOnClickListener(view -> shopBind());

        submit =findViewById(R.id.submit_btn);
        submit.setOnClickListener(view -> submit());

        takePhoto =findViewById(R.id.re_take);
        takePhoto.setOnClickListener(view -> takePhoto());

        rest =findViewById(R.id.rest);
        rest.setOnClickListener(view -> rest());

        initDeviceInfo();

        initAiUsing();

        getDeviceInfo();

        initMarkView();
    }

    private void initDeviceInfo(){
        tvSN = findViewById(R.id.tv_ai_sn);
        tvShopId = findViewById(R.id.tv_ai_shop_id);
        tvShopName = findViewById(R.id.tv_ai_shop_name);
        tvFirmwareVersion = findViewById(R.id.tv_ai_firmware_version);
        tvSDKVersion = findViewById(R.id.tv_ai_sdk_version);
        tvModelVersion = findViewById(R.id.tv_ai_model_version);
        tvConfigVersion = findViewById(R.id.tv_ai_config_version);
        tvScaleType = findViewById(R.id.tv_scale_type);
    }

    void getDeviceInfo() {
        AiDeviceHelper.getInstance().init(getContext());
        if (!AiDeviceHelper.getInstance().isCameraActive()) {
            AiDeviceHelper.getInstance().startUpDevice();
        }
        try {
            DeviceInfo deviceInfo = AiDeviceHelper.getInstance().getDeviceInfo();
            if (deviceInfo.getRet() == 0) {
                tvSN.setText("AI设备序列号：" + deviceInfo.getSn());
                tvShopId.setText("门店编号：" + deviceInfo.getClientID());
                tvShopName.setText("门店名称：" + deviceInfo.getClientName());
                tvFirmwareVersion.setText("固件版本号：" + deviceInfo.getFirmwareVersion());
                tvSDKVersion.setText("SDK版本号：" + deviceInfo.getSdkVersion());
                tvModelVersion.setText("模型版本号：" + deviceInfo.getModelVersion());
                tvConfigVersion.setText("配置表版本号：" + deviceInfo.getOnlineVersion());
            } else {
                MyDialog.toastMessage(deviceInfo.getErr());
            }
        } catch (JSONException e) {
            MyDialog.toastMessage("获取称信息错误：" + e.getMessage());
            e.printStackTrace();
        }

    }

    void shopBind() {
        final String code = storeCode.getText().toString();
        if (Utils.isNotEmpty(code)) {
            BaseInfo baseInfo = AiDeviceHelper.getInstance().activeDevice(code);
            if (baseInfo.getRet() == 0) {
                MyDialog.toastMessage("绑定成功");
            } else {
                MyDialog.toastMessage(baseInfo.getErr());
            }
            getDeviceInfo();//刷新设备信息
        } else {
            MyDialog.toastMessage(storeCode.getHint().toString());
        }
    }

    void submit(){
        double left = markView.getCurrentLeft() / UI_POSITION_RATIO;
        double top = markView.getCurrentTop() / UI_POSITION_RATIO;
        double width = markView.getCurrentWidth() / UI_POSITION_RATIO;
        double height = markView.getCurrentHeight() / UI_POSITION_RATIO;
        BaseInfo baseInfo = AiDeviceHelper.getInstance().setCalib((int) height + "_" + (int) left + "_" + (int) top + "_" + (int) width);
        if (baseInfo.getRet() == 0) {
            MyDialog.toastMessage("标定成功");
        } else {
            MyDialog.toastMessage("标定出错：" + baseInfo.getRet());
        }
    }

    void takePhoto(){
        mIvCamera.setImageBitmap(AiDeviceHelper.getInstance().getImage().getBitmap());
    }

    void rest(){
        BaseInfo info = AiDeviceHelper.getInstance().resetDevice();
        if (info.getRet() == 0) {
            MyDialog.toastMessage("恢复出厂设置成功");
            getDeviceInfo();
        } else {
            MyDialog.toastMessage(info.getErr());
        }
    }

    private void initAiUsing(){
        final CheckBox ai_using = findViewById(R.id.ai_using);
        ai_using.setChecked(WeightHelper.getInstance().isAiEnable());//AI功能sdk中默认开启
        ai_using.setOnCheckedChangeListener((compoundButton, b) -> WeightHelper.getInstance().setAiEnable(b));
    }

    void initMarkView() {
        mIvCamera.postDelayed(() -> {
            mIvCamera.setImageBitmap(AiDeviceHelper.getInstance().getImage().getBitmap());
            ScalePosInfo scalePosInfo = AiDeviceHelper.getInstance().getCalib();
            double left = scalePosInfo.getLeft() * UI_POSITION_RATIO;
            double top = scalePosInfo.getTop() * UI_POSITION_RATIO;
            double right = (scalePosInfo.getLeft() + scalePosInfo.getWidth()) * UI_POSITION_RATIO;
            double bottom = (scalePosInfo.getTop() + scalePosInfo.getHeight()) * UI_POSITION_RATIO;
            markView.redrawRect((int) left, (int) top, (int) right, (int) bottom);
        },300);
    }



    @Override
    protected int getContentLayoutId() {
        return R.layout.balance_ai_setting_dialog;
    }
}
