package com.wyc.hzaiplugin.ai.aiRecognize;

import android.content.Context;
import androidx.annotation.CallSuper;
import com.wyc.ai.AiGoods;
import com.wyc.hzaiplugin.R;
import com.wyc.hzaiplugin.ai.aiRecognizeInterface.AiListener;
import com.wyc.hzaiplugin.ai.aiRecognizeInterface.IAIRecognize;
import com.wyc.hzaiplugin.baseDialog.MyDialog;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import com.wyc.hzaiplugin.bean.AiSetting;
import com.wyc.hzaiplugin.bean.TreeListItem;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.dialog.aiRecognize
 * @ClassName: AIRecognizeImp
 * @Description: Ai识别父类
 * @Author: wyc
 * @CreateDate: 2023-06-30 14:13
 * @UpdateUser: 更新者：
 * @UpdateDate: 2023-06-30 14:13
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public abstract class AIRecognizeImp implements IAIRecognize {
    private volatile static IAIRecognize sAIRecognize = null;
    private static int initCount = 0;
    protected AiListener mListener;
    private static IAIRecognize initAi(){
        if (initCount == 0 && sAIRecognize == null){
            synchronized (AIRecognizeImp.class){
                if (sAIRecognize == null){
                    initCount++;
                    AiSetting setting = AiSetting.getInstance();
                    if (setting != null && setting.isEnable()){
                        try {
                            Class<?> scale_class = Class.forName(setting.getCls());
                            Constructor<?> constructor = scale_class.getConstructor();
                            sAIRecognize = (IAIRecognize)constructor.newInstance();
                        }catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e){
                            e.printStackTrace();
                            if (e instanceof ClassNotFoundException || e instanceof InvocationTargetException){
                                MyDialog.toastMessage(R.string.not_support_feature);
                            }else
                                MyDialog.toastMessage("初始化Ai识别错误：" + e);
                        }
                    }
                }
            }
        }
        return sAIRecognize;
    }

    public static boolean enableAi(){
        if (sAIRecognize != null){
            return true;
        }else {
            if (initCount == 0){
                IAIRecognize iaiRecognize = initAi();
                return iaiRecognize != null;
            }
        }
        return false;
    }

    public static void settingAi(Context context){
        if (sAIRecognize != null){
            sAIRecognize.setting(context);
        }
    }

    public static void aiRecognize(double num){
        if (sAIRecognize != null){
            sAIRecognize.recognize(num);
        }
    }

    public static void openAi(AiListener aiListener){
        IAIRecognize iaiRecognize = initAi();
        if (iaiRecognize != null && aiListener != null){
            iaiRecognize.setListener(aiListener);
            iaiRecognize.open();
        }
    }

    public static void closeAi(){
        synchronized (AIRecognizeImp.class){
            initCount = 0;
            if (sAIRecognize != null){
                sAIRecognize.close();
                sAIRecognize = null;
            }
        }
    }

    public static void updateGoodsInfo(AiGoods aiGoods){
        if (sAIRecognize != null){
            sAIRecognize.submitInfo(aiGoods);
        }
    }

    public static List<TreeListItem> support(){
        final List<TreeListItem> data = new ArrayList<>();

        TreeListItem item = new TreeListItem();
        item.setItem_id("NONE");
        item.setItem_name("NONE");
        data.add(item);

        item = new TreeListItem();
        item.setItem_id("com.wyc.hzaiplugin.ai.aiRecognize.BalanceAi");
        item.setItem_name("佰伦斯Ai");
        data.add(item);

        item = new TreeListItem();
        item.setItem_id("com.wyc.hzaiplugin.ai.aiRecognize.YoTuAi");
        item.setItem_name("由由Ai");
        data.add(item);

        item = new TreeListItem();
        item.setItem_id("com.wyc.hzaiplugin.ai.aiRecognize.LiuXXAi");
        item.setItem_name("六小象Ai");
        data.add(item);

        return data;
    }

    @Override
    public final IAIRecognize setListener(AiListener listener) {
        mListener = listener;
        return this;
    }

    @CallSuper
    @Override
    public void close() {
        mListener = null;
    }

    @Override
    public void recognize(double num) {

    }
}
