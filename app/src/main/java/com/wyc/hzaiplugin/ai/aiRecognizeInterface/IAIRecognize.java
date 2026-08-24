package com.wyc.hzaiplugin.ai.aiRecognizeInterface;

import android.content.Context;

import com.wyc.ai.AiGoods;

public interface IAIRecognize {
    boolean isEnable();
    void open();
    void close();
    void submitInfo(AiGoods aiGoods);
    void setting(Context activity);
    void recognize(double num);
    IAIRecognize setListener(AiListener listener);
}
