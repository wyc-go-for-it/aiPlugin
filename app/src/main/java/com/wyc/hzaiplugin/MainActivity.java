package com.wyc.hzaiplugin;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.sanqn.ai.core.common.log.Logger;
import com.wyc.ai.AiGoods;
import com.wyc.ai.IAiCallback;
import com.wyc.ai.IAiService;
import com.wyc.hzaiplugin.ai.aiRecognize.AIRecognizeImp;
import com.wyc.hzaiplugin.ai.aiRecognize.LiuXXAi;
import com.wyc.hzaiplugin.bean.AiSetting;
import com.wyc.hzaiplugin.bean.TreeListItem;
import com.wyc.hzaiplugin.service.AiService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    private int mPosition = 0;


    private IAiService mScaleService;
    private ServiceConnection mConnection;
    private IAiCallback mCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initAiDevice();
        initSave();
        initAiSetting();
        initService();
     }

    private void initAiDevice(){
        final ViewGroup camera_type_layout = findViewById(R.id.camera_type_layout);

        final Spinner ai_device = findViewById(R.id.ai_device) ;
        ArrayAdapter<String> aiDeviceAdapter = new ArrayAdapter<>(this, R.layout.drop_down_style);
        aiDeviceAdapter.setDropDownViewResource(R.layout.drop_down_style);

        final List<TreeListItem> supportLst = AIRecognizeImp.support();

        ai_device.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPosition = position;

                if(LiuXXAi.class.getCanonicalName().equals(supportLst.get(position).getItem_id())){
                    camera_type_layout.setVisibility(View.VISIBLE);
                }else{
                    camera_type_layout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        for(TreeListItem value : supportLst){
            aiDeviceAdapter.add(value.getItem_name());
        }
        ai_device.setAdapter(aiDeviceAdapter);

        final AiSetting setting = AiSetting.getInstance();
        for (int i = 0;i < supportLst.size();++i){
            final TreeListItem item = supportLst.get(i);
            if (item.getItem_id().equals(setting.getCls())){
                ai_device.setSelection(i);
                if(LiuXXAi.class.getCanonicalName().equals(setting.getCls())){
                    camera_type_layout.setVisibility(View.VISIBLE);
                }
                break;
            }
        }
    }

    private void initSave(){
        final Button btn_save = findViewById(R.id.btn_save);

        final RadioButton local = findViewById(R.id.local);
        final RadioButton uvc = findViewById(R.id.uvc);

        btn_save.setOnClickListener(view -> {
            final AiSetting setting = new AiSetting();

            if(mPosition > 0){
                final List<TreeListItem> lst = AIRecognizeImp.support();
                if(mPosition< lst.size()){
                    final TreeListItem item = lst.get(mPosition);
                    setting.setCls(item.getItem_id());
                    setting.setName(setting.getCls());
                    setting.save();

                    Toast.makeText(this, "保存成功,请退出重进。", Toast.LENGTH_LONG).show();
                }
            }

            if(local != null && uvc != null){
                final SharedPreferences preferences=getSharedPreferences("cameraInfo", Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor= preferences.edit();

                if(uvc.isChecked()){
                    editor.putInt("cameraType", 0);
                }else{
                    editor.putInt("cameraType", 1);
                }

                editor.apply();
            }
        });

        final SharedPreferences preferences=getSharedPreferences("cameraInfo", Context.MODE_PRIVATE);
        final int cameraType = preferences.getInt("cameraType",1);
        if(cameraType == 0){
            uvc.setChecked(true);
        }


        final Button btn_exit = findViewById(R.id.btn_exit);
        btn_exit.setOnClickListener(view -> finish());
    }

    private void initAiSetting(){
        final Button ai_setting = findViewById(R.id.ai_setting);
        ai_setting.setOnClickListener(view -> AIRecognizeImp.settingAi(this));
    }

    private void initService(){
        mCallback = new IAiCallback.Stub() {

            @Override
            public void onGoodsDelivery(List<AiGoods> aiGoods) {
                Log.d(TAG,"aiGoods:" + Arrays.toString(aiGoods.toArray()));
            }

            @Override
            public void onError(String msg) {

            }
        };

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mScaleService = IAiService.Stub.asInterface(service);
                try {
                    mScaleService.registerCallback(mCallback);
                } catch (RemoteException ignored) {
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mScaleService = null;
                Log.e(TAG, "AI 秤服务意外断开！尝试重新绑定与拉起...");
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    bindAiService();
                }, 2000);
            }
        };

        bindAiService();
    }

    private void bindAiService(){
        Intent intent = new Intent("com.wyc.ai.AI_SERVICE");
        intent.setPackage("com.wyc.hzaiplugin");
        boolean  code = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        Log.d(TAG,"bindService:" + code);
    }

    private void destroy(){
        if (mScaleService != null) {
            try {
                mScaleService.unregisterCallback(mCallback);
            } catch (RemoteException ignored) {
            }
        }
        mScaleService = null;
        mCallback=null;
    }

     @Override
    protected void onDestroy() {
        super.onDestroy();
        destroy();
    }
}