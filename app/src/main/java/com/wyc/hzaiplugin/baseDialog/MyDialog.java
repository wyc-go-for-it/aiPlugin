package com.wyc.hzaiplugin.baseDialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.wyc.hzaiplugin.App;
import com.wyc.hzaiplugin.R;
import com.wyc.hzaiplugin.utils.Utils;

public final class MyDialog extends AbstractDialog {
    @SuppressLint("StaticFieldLeak")
    private static MyDialog mDup;
    private Button mYes,mNo;//mYes确定按钮、mNo取消按钮
    private TextView mMessage;//mTitle标题文本、mMessage消息提示文本
    private String mMessageStr = "";//从外界设置的消息文本
    private final Context mContext;
    private IconType mContentIconType = IconType.INFO;
    private boolean mIsYes,mIsNo;
    //按钮文本的显示内容
    private String mYesStr,mNoStr;
    private onNoOnclickListener noOnclickListener;//取消按钮被点击了的监听器
    private onYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器
    private LifecycleEventObserver mLifecycleEventObserver;
    public MyDialog  setNoOnclickListener(String str, onNoOnclickListener onNoOnclickListener) {
        if (str != null) {
            mNoStr = str;
        }
        this.noOnclickListener = onNoOnclickListener;
        this.mIsNo = true;

        return this;
    }

    public MyDialog setYesOnclickListener(String str, onYesOnclickListener onYesOnclickListener) {
        if (str != null) {
            mYesStr = str;
        }
        this.yesOnclickListener = onYesOnclickListener;
        this.mIsYes = true;

        return  this;
    }

    public MyDialog(Context context,final String title) {
        super(context,title, R.style.MyDialog);
        this.mContext = context;
    }

    public MyDialog(Context context,final String title, IconType type){
        this(context,title);
        mContentIconType = type;
        if (context instanceof LifecycleOwner && Looper.myLooper() == Looper.getMainLooper()){
            mLifecycleEventObserver = new LifecycleEventObserver() {
                @Override
                protected void finalize(){
                    Log.d(this.getClass().getSimpleName(),"MyDialog's LifecycleObserver finalized");
                }
                @Override
                public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                    if (Lifecycle.Event.ON_DESTROY == event){
                        dismiss();
                        source.getLifecycle().removeObserver(this);
                    }
                }
            };
            ((LifecycleOwner)context).getLifecycle().addObserver(mLifecycleEventObserver);
        }
    }

    @Override
    protected void closeWindow() {
        super.closeWindow();
        if (noOnclickListener != null)noOnclickListener.onNoClick(this);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mLifecycleEventObserver != null){
            if (Looper.myLooper() != Looper.getMainLooper()){
                getWindow().getDecorView().post(()-> ((LifecycleOwner)mContext).getLifecycle().removeObserver(mLifecycleEventObserver));
            }else
                ((LifecycleOwner)mContext).getLifecycle().removeObserver(mLifecycleEventObserver);
        }
        mDup = null;
    }

    public enum IconType {
        INFO,WARN,ERROR,ASK;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //按空白处不能取消动画
        setCanceledOnTouchOutside(false);

        //初始化界面控件
        initView();
        //初始化界面数据
        initData();

    }

    protected double getWidthRatio(){
        return 368;
    }



    @Override
    protected int getContentLayoutId() {
        return R.layout.mydialog_layout;
    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        mYes.setOnClickListener(v -> {
            if (yesOnclickListener != null) {
                yesOnclickListener.onYesClick(MyDialog.this);
            }
        });
        //设置取消按钮被点击后，向外界提供监听
        mNo.setOnClickListener(v -> {
            if (noOnclickListener != null) {
                noOnclickListener.onNoClick(MyDialog.this);
            }
        });
    }

    /**
     * 初始化界面控件的显示数据
     */
    private void initData() {
        //如果设置按钮的文字
        if (mYesStr != null) {
            mYes.setText(mYesStr);
        }
        if (mNoStr != null) {
            mNo.setText(mNoStr);
        }

    }
    /**
     * 初始化界面控件
     */
    private void initView() {
        mYes = findViewById(R.id.yes);
        mNo = findViewById(R.id.no);
        mMessage = findViewById(R.id.content);
        mMessage.setMovementMethod(ScrollingMovementMethod.getInstance());
    }
    public MyDialog setMessage(String message) {
        mMessageStr = message;
        return  this;
    }


    @Override
    public void show(){
        try {
            super.show();

            showBtn();
            showIcon();
            initEvent();
        }catch (WindowManager.BadTokenException ignored){
        }
    }

    private void showIcon(){
        Drawable drawable = null;
        switch (mContentIconType){
            case WARN:
                drawable = mContext.getResources().getDrawable(R.drawable.warn,null);
                break;
            case ERROR:
                drawable = mContext.getResources().getDrawable(R.drawable.error,null);
                break;
            case ASK:
                drawable = mContext.getResources().getDrawable(R.drawable.ask,null);
                break;
            default:
                drawable = mContext.getResources().getDrawable(R.drawable.infor,null);
                break;
        }
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
        mMessage.setCompoundDrawables(drawable,null,null,null);
        if (mMessageStr != null) {
            mMessage.setText(mMessageStr);
        }
    }
    private void showBtn(){
        if (mIsYes && !mIsNo) {
            noOnclickListener = null;
            mNo.setVisibility(View.INVISIBLE);
            mYes.setText(mYesStr);
            mYes.setVisibility(View.VISIBLE);
        } else if (mIsNo && !mIsYes) {
            yesOnclickListener = null;
            mYes.setVisibility(View.INVISIBLE);
            mNo.setText(mNoStr);
            mNo.setVisibility(View.VISIBLE);
        } else {
            mNo.setVisibility(View.VISIBLE);
            mYes.setVisibility(View.VISIBLE);
        }
    }

    public static void displayMessage(final Context context,final String message){
        final MyDialog dialog = new MyDialog(context,"提示信息",IconType.INFO);
        dialog.setMessage(message).setNoOnclickListener("确定", Dialog::dismiss).show();
    }

    public static void displayMessageWithSingleWindow(final Context context,final String message){
        if (mDup == null) mDup = new MyDialog(context,"提示信息",IconType.INFO);
        mDup.setMessage(message).setNoOnclickListener("确定", Dialog::dismiss).show();
    }

    public static void displayErrorMessage(final Context context,final String message){
        if (mDup == null) mDup = new MyDialog(context,"提示信息", IconType.ERROR);
        mDup.setMessage(mDup.mMessageStr + "\n" +message).setNoOnclickListener("取消", Dialog::dismiss).show();
    }

    public static void displayErrorMessage(final Context context,final String message,final onNoOnclickListener no){
        final MyDialog dialog = new MyDialog(context,"提示信息", IconType.ERROR);
        dialog.setMessage(message).setNoOnclickListener("取消",no).show();
    }

    public static void displayAskMessage(final Context context,final String message,final onYesOnclickListener yes,final onNoOnclickListener no){
        final MyDialog dialog = new MyDialog(context, "提示信息",IconType.ASK);
        dialog.setMessage(message).setYesOnclickListener("是",yes).setNoOnclickListener("否", no).show();
    }

    public static void toastMessage(final String message){
        App.showGlobalToast(message);
    }

    public static void toastMessage(final @StringRes int id){
        App.showGlobalToast(App.self().getString(id));
    }


    /**
     * 设置确定按钮和取消被点击的接口
     */
    public interface onYesOnclickListener {
          void onYesClick(MyDialog myDialog);
    }
    public interface onNoOnclickListener {
          void onNoClick(MyDialog myDialog);
    }
}