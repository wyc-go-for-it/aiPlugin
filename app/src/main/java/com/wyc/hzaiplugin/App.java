package com.wyc.hzaiplugin;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.wyc.hzaiplugin.utils.Utils;

import java.lang.reflect.Field;

public class App extends Application {

    private static App mApplication;
    private static volatile Toast mGlobalToast;

    public App() {
        super();

        mApplication = this;
    }

    @Override
    public  void  onCreate() {
        super.onCreate();
    }

    @Override
    public Resources getResources() {
        final Resources res = super.getResources();
        final Configuration configuration = res.getConfiguration();

        if (configuration.fontScale != 1f){
            configuration.fontScale = 1f;
            res.updateConfiguration(configuration,res.getDisplayMetrics());
        }
        return res;
    }

    public static App self(){
        return mApplication;
    }

    public static void showGlobalToast(final String message){
        if (Looper.myLooper() == Looper.getMainLooper()){
            showToast(message);
        }
    }

    private static void showToast(final String message){
        if (Utils.isNotEmpty(message)){
            if (mGlobalToast == null){
                mGlobalToast = Toast.makeText(getUIContext(),"",Toast.LENGTH_LONG);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
                    hookToast(mGlobalToast);
                }
            }
            mGlobalToast.setText(message);
            mGlobalToast.show();
        }
    }

    public static Context getUIContext(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R){
            final Display display;

            DisplayManager dm =  mApplication.getSystemService(DisplayManager.class);
            if (dm != null){
                display = dm.getDisplay(Display.DEFAULT_DISPLAY);
            }else{
                final WindowManager wm = (WindowManager) mApplication.getSystemService(Context.WINDOW_SERVICE);
                display = wm.getDefaultDisplay();
            }

            return mApplication.createDisplayContext(display).createWindowContext(display,WindowManager.LayoutParams.TYPE_APPLICATION,null);
        }
        return mApplication;
    }

     private static void hookToast(Toast toast) {
        Class<Toast> cToast = Toast.class;
        try {
            //TN是private的
            Field fTn = cToast.getDeclaredField("mTN");
            fTn.setAccessible(true);

            //获取tn对象
            Object oTn = fTn.get(toast);
            //获取TN的class，也可以直接通过Field.getType()获取。
            Class<?> cTn = oTn.getClass();
            Field fHandle = cTn.getDeclaredField("mHandler");

            //重新set->mHandler
            fHandle.setAccessible(true);
            fHandle.set(oTn, new HandlerProxy((Handler) fHandle.get(oTn)));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    //7.x
    private static class HandlerProxy extends Handler {

        private Handler mHandler;

        public HandlerProxy(Handler handler) {
            this.mHandler = handler;
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                mHandler.handleMessage(msg);
            } catch (WindowManager.BadTokenException e) {
                //ignore
            }
        }
    }
}
