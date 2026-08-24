package com.wyc.hzaiplugin.ai.aiRecognizeInterface.liuxxSetting;

import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    private Resources mResources;

    private static float appDensity;
    private static float appScaledDensity;
    private static DisplayMetrics appDisplayMetrics;

    @Override
    public Resources getResources() {
        if (mResources == null) {
            mResources = new Resources(getAssets(), super.getResources().getDisplayMetrics(), super.getResources().getConfiguration());
        }
        if (appDisplayMetrics == null) {
            if (appDensity == 0) {
                appDisplayMetrics = getApplicationContext().getResources().getDisplayMetrics();
                appDensity = appDisplayMetrics.density;
                appScaledDensity = appDisplayMetrics.scaledDensity;
                getApplicationContext().registerComponentCallbacks(new ComponentCallbacks() {
                    @Override
                    public void onConfigurationChanged(Configuration newConfig) {
                        if (newConfig != null && newConfig.fontScale > 0) {
                            appScaledDensity = getApplication().getResources().getDisplayMetrics().scaledDensity;
                        }
                    }

                    @Override
                    public void onLowMemory() {
                    }
                });
            }
        }
        float targetDensity = 0;
        if (mResources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            targetDensity = appDisplayMetrics.widthPixels / 720f;
        } else {
            targetDensity = (appDisplayMetrics.widthPixels) / 1920f;

        }
        float targetScaledDensity = targetDensity * (appScaledDensity / appDensity);
        int targetDensityDpi = (int) (160 * targetDensity);
        DisplayMetrics activityDisplayMetrics = mResources.getDisplayMetrics();
        activityDisplayMetrics.density = targetDensity;
        activityDisplayMetrics.scaledDensity = targetScaledDensity;
        activityDisplayMetrics.densityDpi = targetDensityDpi;
        return mResources;
    }
}