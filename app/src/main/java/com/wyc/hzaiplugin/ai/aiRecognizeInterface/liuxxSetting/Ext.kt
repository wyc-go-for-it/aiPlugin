package com.wyc.hzaiplugin.ai.aiRecognizeInterface.liuxxSetting

import android.content.res.Resources

val Number.dp: Int get() = (toInt() * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
