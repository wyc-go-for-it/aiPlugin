package com.wyc.hzaiplugin.ai.aiRecognizeInterface.liuxxSetting;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SizedFrameLayout extends FrameLayout {
    private int mWidth;
    private int mHeight;

    public SizedFrameLayout(@NonNull Context context) {
        super(context);
    }

    public SizedFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSize(int width, int height) {
        if (width <= 0 || height <= 0 || mWidth == 0 || mHeight == 0) return;
        float aspectRatio = (float) width / height;
        float viewAspectRatio = (float) mWidth / mHeight;
        if (aspectRatio == viewAspectRatio) {
            setPadding(0, 0, 0, 0);
        }
        if (aspectRatio > viewAspectRatio) {
            int newHeight = (int) (mWidth / aspectRatio);
            setPadding(0, (mHeight - newHeight) / 2, 0, (mHeight - newHeight) / 2);
        } else {
            int newWidth = (int) (mHeight * aspectRatio);
            setPadding((mWidth - newWidth) / 2, 0, (mWidth - newWidth) / 2, 0);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }
}
