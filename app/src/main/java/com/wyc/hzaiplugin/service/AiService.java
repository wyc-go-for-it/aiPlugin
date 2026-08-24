package com.wyc.hzaiplugin.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.wyc.ai.AiGoods;
import com.wyc.ai.IAiCallback;
import com.wyc.ai.IAiService;
import com.wyc.hzaiplugin.R;
import com.wyc.hzaiplugin.ai.aiRecognize.AIRecognizeImp;

import java.util.Arrays;

public class AiService extends Service {
    private final static String TAG = "AiService";
    private static final String CHANNEL_ID = "CameraChannel";

    public AiService() {
    }

    private final RemoteCallbackList<IAiCallback> mCallbacks = new RemoteCallbackList<>();

    private final IAiService.Stub mBinder = new IAiService.Stub() {

        @Override
        public void registerCallback(IAiCallback callback) {
            if (callback != null) mCallbacks.register(callback);
        }

        @Override
        public void unregisterCallback(IAiCallback callback) {
            if (callback != null) mCallbacks.unregister(callback);
        }

        @Override
        public void sendGoodsInfo(AiGoods goods) {
            AIRecognizeImp.updateGoodsInfo(goods);
            Log.d(TAG,goods.toString());
        }

        @Override
        public void aiRecognize(double num) {
            AIRecognizeImp.aiRecognize(num);
            Log.d(TAG,"aiRecognize:" + num);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {

        startForegroundServiceNotification();
        initAi();

        Log.d(TAG,"onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopForeground(STOP_FOREGROUND_REMOVE);
        AIRecognizeImp.closeAi();

        Log.d(TAG,"onDestroy");
    }

    private void initAi() {
        AIRecognizeImp.openAi(aiGoods -> {
            int count = mCallbacks.beginBroadcast();
            for (int i = 0; i < count; i++) {
                try {
                    mCallbacks.getBroadcastItem(i).onGoodsDelivery(aiGoods);
                } catch (RemoteException ignored) {
                }
            }
            mCallbacks.finishBroadcast();
        });
    }


    private void startForegroundServiceNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Ai服务",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Ai服务正在运行")
                .setContentText("正在后台监测摄像头数据")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        startForeground(1001, notification);
    }
}