package com.wyc.hzaiplugin.bean;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wyc.hzaiplugin.App;
import com.wyc.hzaiplugin.baseDialog.MyDialog;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: AiSetting
 * @Description: ai识别参数
 * @Author: wyc
 * @CreateDate: 2023-07-04 16:52
 * @UpdateUser: 更新者：
 * @UpdateDate: 2023-07-04 16:52
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class AiSetting {
    private static final String KEY = "ai";

    private String cls;
    private String name;

    public boolean isEnable() {
        return cls != null && !"NONE".equals(cls);
    }


    public String getCls() {
        return cls;
    }

    public void setCls(String cls) {
        this.cls = cls;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static AiSetting getInstance(){
        final SharedPreferences preferences= App.self().getSharedPreferences("ai_setting", Context.MODE_PRIVATE);
        final JSONObject object = JSON.parseObject(preferences.getString(KEY,"{}"));
        return object.toJavaObject(AiSetting.class);
    }

    public void save(){
        final SharedPreferences preferences=App.self().getSharedPreferences("ai_setting", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor= preferences.edit();
        editor.putString(KEY, JSONObject.toJSONString(this));
        editor.apply();
    }

    @Override
    public String toString() {
        return "AiSetting{" +
                ", cls='" + cls + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
