package com.wyc.hzaiplugin.ai.aiRecognize;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import com.wyc.ai.AiGoods;
import com.wyc.hzaiplugin.utils.Utils;
import com.wyc.hzaiplugin.App;
import com.yoyo.ui.constants.YoyoSdkConfig;
import com.yoyo.yoyobase.log.LogExtKt;
import com.yoyo.yoyodata.bean.response.Reply;
import com.yoyo.yoyodata.constants.ReplyCode;
import com.yoyo.yoyodata.enums.MarkTypeEnum;
import com.yoyo.yoyodata.enums.PriceUnitTypeEnum;
import com.yoyo.yoyodata.enums.RecognitionModelEnum;
import com.yoyo.yoyodata.enums.ScaleTypeEnum;
import com.yoyo.yoyodata.enums.WeightStableEnum;
import com.yoyo.yoyodata.enums.YoYoActivityTagEnum;
import com.yoyo.yoyodata.utils.ScBaseConfig;

import java.util.ArrayList;
import java.util.List;

import cn.smart.yoyolib.core.aidl.YoYoItemInfo;
import cn.smart.yoyolib.libs.YoYoUtils;
import cn.smart.yoyolib.libs.bean.request.AIMatchingRequest;
import cn.smart.yoyolib.libs.bean.request.AiMarkRequest;
import cn.smart.yoyolib.libs.bean.request.InitRequest;
import cn.smart.yoyolib.libs.bean.request.ScaleDataRequest;
import cn.smart.yoyolib.libs.bean.response.MatchingReply;
import cn.smart.yoyolib.libs.callback.IYoYoAiListener;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.dialog.aiRecognize
 * @ClassName: YoTuAi
 * @Description: 由图AI识别
 * @Author: wyc
 * @CreateDate: 2023-07-04 16:44
 * @UpdateUser: 更新者：
 * @UpdateDate: 2023-07-04 16:44
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class YoTuAi extends AIRecognizeImp {
    private boolean mEnable;
    private String matchingTag = "";
    public YoTuAi(){
        ScBaseConfig.INSTANCE.setLoadSoDynamically(false);
    }
    @Override
    public boolean isEnable() {
        return mEnable;
    }

    @Override
    public void open() {
        YoyoSdkConfig config = new YoyoSdkConfig();
        YoYoUtils.init(App.self(), new InitRequest("HZRJ", "33b430f960eb4ab74a7f1041b9ca072b"), new IYoYoAiListener() {
            @Override
            public void onEventAIMatching(Reply<MatchingReply> reply) {
                final MatchingReply matchingReply = reply.getData();
                if (mListener != null && reply.getCode() == ReplyCode.Success){
                    final List<String> aList =  matchingReply.getPlus();
                    final List<AiGoods> aiGoodsList = new ArrayList<>();
                    if (!aList.isEmpty()){
                        for (String pul : aList){
                            aiGoodsList.add(new AiGoods(pul,""));
                        }
                    }
                    mListener.onGoodsDelivery(aiGoodsList);
                }
                matchingTag = matchingReply.getMatchingTag();
            }

            @Override
            public void onEventInit(Reply<?> reply) {
                if (reply.getCode() == ReplyCode.Success) {
                    doTransData();
                }
            }

            @Override
            public void onEventActive(Reply<?> reply) {
                Log.d("YoTuAi","onEventActive:" + JSON.toJSONString(reply));
                if ((mEnable = reply.getCode() == ReplyCode.Success)) {
                    LogExtKt.logI("设备已激活");
                } else {
                    LogExtKt.logE("设备未激活");
                }
            }
        },config);
    }

    private void doTransData(){
        final List<YoYoItemInfo> itemInfoList = new ArrayList<>();
        YoYoUtils.setScaleData(new ScaleDataRequest(itemInfoList, true, ScaleTypeEnum.Increments));
    }

    @Override
    public void close() {
        super.close();
        YoYoUtils.saveStudyData();
        YoYoUtils.unInit();
        mListener = null;
    }

    @Override
    public void submitInfo(AiGoods aiGoods) {
        final AiMarkRequest aiMarkRequest = new AiMarkRequest();
        aiMarkRequest.setPlu(aiGoods.getId());
        aiMarkRequest.setBarCode(aiGoods.getBarcode());
        aiMarkRequest.setWeight((int) (aiGoods.getNum() * 1000));
        aiMarkRequest.setPrice((int) (aiGoods.getPrice() * 100));
        aiMarkRequest.setAmount((int) ((aiGoods.getNum() * aiGoods.getPrice()) * 100));
        aiMarkRequest.setDeviceNo("88");
        aiMarkRequest.setOperatorName("admin");
        aiMarkRequest.setType(MarkTypeEnum.MarkSearch);
        aiMarkRequest.setPriceUnit(PriceUnitTypeEnum.WeightType);
        aiMarkRequest.setMatchingTag(matchingTag);

        matchingTag = "";
        YoYoUtils.aiMark(aiMarkRequest);
    }

    @Override
    public void setting(Context activity) {
        YoYoUtils.startYoYoActivity(activity, YoYoActivityTagEnum.SETTING);
    }

    public static void setting(){
        YoYoUtils.startYoYoActivity(App.self(), YoYoActivityTagEnum.SETTING);
    }

    @Override
    public void recognize(double num) {
        if (Utils.equalDouble(0.0,num)){
            if (mListener != null)
                mListener.onGoodsDelivery(new ArrayList<>());
        }else {
            YoYoUtils.aiMatching(new AIMatchingRequest((int) (num * 1000), WeightStableEnum.WeightStable, RecognitionModelEnum.Forcibly));
        }
    }
}
