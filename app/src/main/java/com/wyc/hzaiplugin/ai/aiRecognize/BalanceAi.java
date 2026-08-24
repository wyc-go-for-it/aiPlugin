package com.wyc.hzaiplugin.ai.aiRecognize;

import android.content.Context;

import com.sdm.ai_sdk.AiDeviceHelper;
import com.sdm.ai_sdk.WeightHelper;
import com.sdm.ai_sdk.bean.ResultData;

import com.wyc.ai.AiGoods;
import com.wyc.hzaiplugin.App;
import com.wyc.hzaiplugin.ai.aiRecognizeInterface.BalanceAiSettingDialog;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.dialog.aiScale
 * @ClassName: BalanceAi
 * @Description: 百伦斯AI识别
 * @Author: wyc
 * @CreateDate: 2023-06-30 13:52
 * @UpdateUser: 更新者：
 * @UpdateDate: 2023-06-30 13:52
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
class BalanceAi extends AIRecognizeImp {
    public BalanceAi(){
        if (!AiDeviceHelper.getInstance().isInit()){
            AiDeviceHelper.getInstance().init(App.self());
        }
    }

    @Override
    public boolean isEnable() {
        return enable();
    }

    static boolean enable(){
        return WeightHelper.getInstance().isAiEnable();
    }

    @Override
    public void open() {
        if (isEnable() && mListener != null){
            WeightHelper.getInstance().setAIListener(list -> {
                final List<AiGoods> aiGoodsList = new ArrayList<>(list.size());
                if (!list.isEmpty()){
                    Collections.sort(list, (resultData, t1) -> t1.getConfidence().compareTo(resultData.getConfidence()));
                    final ResultData data = list.get(0);
                    final AiGoods aiGoods = new AiGoods();
                    aiGoods.setId(data.getId());
                    aiGoods.setName(data.getConfidence());
                    aiGoodsList.add(aiGoods);
                }
                mListener.onGoodsDelivery(aiGoodsList);
            });
            if (!AiDeviceHelper.getInstance().isCameraActive()) {
                AiDeviceHelper.getInstance().startUpDevice();
            }
        }
    }

    @Override
    public void close() {
        super.close();
        if (isEnable()){
            AiDeviceHelper.getInstance().unInit();
            WeightHelper.getInstance().setAIListener(null);
        }
    }

    @Override
    public void submitInfo(AiGoods aiGoods) {
        if (aiGoods != null)
            AiDeviceHelper.getInstance().uploadReportWithName(aiGoods.getId() + "_" + aiGoods.getName());
    }

    @Override
    public void setting(Context activity) {
        final BalanceAiSettingDialog balanceAiSettingDialog = new BalanceAiSettingDialog(activity);
        balanceAiSettingDialog.show();
    }
}
