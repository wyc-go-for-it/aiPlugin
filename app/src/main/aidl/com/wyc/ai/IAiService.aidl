// IAiService.aidl
package com.wyc.ai;

import com.wyc.ai.AiGoods;
import com.wyc.ai.IAiCallback;

interface IAiService {
       void registerCallback(IAiCallback callback);
       void unregisterCallback(IAiCallback callback);

       void sendGoodsInfo(in AiGoods goods);
       void aiRecognize(in double num);
}