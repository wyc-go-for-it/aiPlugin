package com.wyc.hzaiplugin.ai.aiRecognizeInterface;

import com.wyc.ai.AiGoods;

import java.util.List;

public interface AiListener {
    void onGoodsDelivery(List<AiGoods> aiGoods);
}
