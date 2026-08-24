package com.wyc.hzaiplugin.ai.aiRecognizeInterface.liuxxSetting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sanqn.ai.core.common.log.Logger;
import com.sanqn.ai.lib.Callback;
import com.sanqn.ai.lib.Response;
import com.sanqn.ai.lib.SanQNSDK;
import com.sanqn.ai.lib.bean.CameraInfo;
import com.sanqn.ai.lib.bean.CameraParameter;
import com.sanqn.camera.client.CameraStatusManager;
import com.wyc.hzaiplugin.databinding.ActivitySettingBinding;

import java.util.List;

public class SettingActivity extends BaseActivity implements CameraStatusManager.OnCameraListener {

    private ActivitySettingBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CameraStatusManager.instance.addListener(this);

        SanQNSDK.camera().cameraList().enqueue(new Callback<List<CameraInfo>>() {
            @Override
            public void callback(Response<List<CameraInfo>> response) {
                if (response.isSuccessful()) {
                    List<CameraInfo> cameraInfoList = response.getData();
                    if (cameraInfoList == null || cameraInfoList.isEmpty()) return;
                    List<String> cameraNameList = new java.util.ArrayList<>();
                    for (CameraInfo cameraInfo : cameraInfoList) {
                        cameraNameList.add(cameraInfo.getName());
                    }

                    CameraInfo cameraInfo = SanQNSDK.camera().cameraInfo().execute().getData();

                    if (cameraInfo == null) return;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(SettingActivity.this, android.R.layout.simple_spinner_item, cameraNameList);
                            binding.cameraSpinner.setAdapter(adapter);

                            binding.cameraSpinner.setSelection(cameraInfoList.indexOf(cameraInfo));

                            binding.cameraSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    CameraInfo cameraInfo = cameraInfoList.get(position);
                                    SanQNSDK.camera().setCameraInfo().cameraInfo(cameraInfo).execute();

                                    final SharedPreferences preferences=getSharedPreferences("cameraInfo", Context.MODE_PRIVATE);
                                    final SharedPreferences.Editor editor= preferences.edit();
                                    editor.putInt("vid", cameraInfo.getVendorId());
                                    editor.putInt("pid", cameraInfo.getProductId());
                                    editor.putString("id", cameraInfo.getId());
                                    editor.putString("name", cameraInfo.getName());
                                    editor.apply();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        }
                    });
                }
            }
        });

        initCameraParams();


        binding.btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SanQNSDK.camera().removeAllPreviewSurface().execute();
            }
        });

        binding.sv.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                Response<Boolean> ret = SanQNSDK.camera().addPreviewSurface().surface(holder.getSurface()).execute();
                if (ret.isSuccessful()) {
                    Log.d("addPreviewSurface", ret.getData().toString());
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                Logger.d("surfaceChanged: " + width + "x" + height);
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                Response<Boolean> ret = SanQNSDK.camera().removePreviewSurface().surface(holder.getSurface()).execute();
                if (ret.isSuccessful()) {
                    Log.d("removePreviewSurface", ret.getData().toString());
                }
            }
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initCameraParams() {
        SanQNSDK.camera().cameraParams().enqueue(new Callback<List<CameraParameter>>() {
            @Override
            public void callback(Response<List<CameraParameter>> response) {
                if (response.isSuccessful()) {
                    List<CameraParameter> cameraParams = response.getData();
                    if (cameraParams == null || cameraParams.isEmpty()) return;
                    List<String> cameraParamsList = new java.util.ArrayList<>();
                    for (CameraParameter cameraParam : cameraParams) {
                        cameraParamsList.add(cameraParam.getWidth() + "x" + cameraParam.getHeight());
                    }
                    CameraParameter cameraParameter = SanQNSDK.camera().cameraParam().execute().getData();
                    if (cameraParameter == null) return;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(SettingActivity.this, android.R.layout.simple_spinner_item, cameraParamsList);
                            binding.sizeSpinner.setAdapter(adapter);

                            binding.sizeSpinner.setSelection(cameraParams.indexOf(cameraParameter));
                            updateView(cameraParameter.getWidth(), cameraParameter.getHeight());

                            binding.sizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    CameraParameter cameraParam = cameraParams.get(position);
                                    updateView(cameraParam.getWidth(), cameraParam.getHeight());
                                    SanQNSDK.camera().setCameraParam().cameraParameter(cameraParam).execute();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private void updateView(int width, int height) {
        binding.content.setSize(width, height);
    }

    @Override
    public void onCameraChange(String action, boolean connected) {

    }

    @Override
    public void onCameraConnected(String action, boolean connected) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (connected) {
                    Response<Boolean> ret = SanQNSDK.camera().addPreviewSurface().surface(binding.sv.getHolder().getSurface()).execute();
                    if (ret.isSuccessful()) {
                        Log.d("addPreviewSurface", ret.getData().toString());
                    }

                    initCameraParams();
                } else {
                    SanQNSDK.camera().removePreviewSurface().surface(binding.sv.getHolder().getSurface()).execute();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraStatusManager.instance.removeListener(this);
        SanQNSDK.camera().removeAllPreviewSurface().execute();
    }
}
