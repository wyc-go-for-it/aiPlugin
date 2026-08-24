// IAiCallback.aidl
package com.wyc.ai;

import com.wyc.ai.AiGoods;

interface IAiCallback {
     void onGoodsDelivery(in List<AiGoods> aiGoods);
     void onError(String msg);
}