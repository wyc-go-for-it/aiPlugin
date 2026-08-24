package com.wyc.hzaiplugin.ai.aiRecognizeInterface.liuxxSetting

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IntRange
import androidx.core.content.edit
import com.sanqn.ai.core.bean.RecogAreaInfo
import com.sanqn.ai.core.common.GsonUtils
import com.sanqn.ai.core.common.log.Logger
import com.sanqn.ai.lib.SanQNSDK

class AreaView : View {
    //定义四个点的坐标
    private var dot1X = 0f
    private var dot1Y = 0f
    private var dot2X = 0f
    private var dot2Y = 0f
    private var dot3X = 0f
    private var dot3Y = 0f
    private var dot4X = 0f
    private var dot4Y = 0f

    //触摸事件的坐标
    private var eventX = 0f
    private var eventY = 0f

    //四点坐标的数组
    private var rectLeft: RectF? = null
    private var rectTop: RectF? = null
    private var rectRight: RectF? = null
    private var rectBottom: RectF? = null

    private val path = Path() //初始化路径（用于填充颜色）

    private val effects = DashPathEffect(floatArrayOf(8f, 8f, 8f, 8f), 1f);//设置虚线的间隔和点的长度

    //控件的宽和高
    private var vWidth = 0f
    private var vHeight = 0f

    //手势选中的点 标记
    private var selectIndex = 0
    private var scaleImage = 3

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    private fun getValue(value: Float, maxValue: Float): Float {
        val min = Math.min(maxValue, value)
        return if (min < 0) 0f else min
    }

    fun updateViewScale(@IntRange(from = 2, to = 4) scaleImage: Int) {
        this.scaleImage = scaleImage
        setPreview()
    }

    var cropPoint: RecogAreaInfo
        get() {
            val sharedPreferences = context.getSharedPreferences("share", 0)
            val cropPoint = sharedPreferences.getString("cropPoint", null)
            if (cropPoint == null) {
                return SanQNSDK.camera().recogArea().execute().data
            }
            return GsonUtils.fromJson(cropPoint, RecogAreaInfo::class.java)
        }
        set(value) {
            val sharedPreferences = context.getSharedPreferences("share", 0)
            if (value != null) {
                sharedPreferences.edit(commit = true) {
                    putString(
                        "cropPoint",
                        GsonUtils.toJson(value)
                    )
                }
            } else {
                sharedPreferences.edit(commit = true) { remove("cropPoint") }
            }
        }

    private fun setPreview() {
        try {
            val sdkCameraAreaRequest = cropPoint
            dot1X = (1.0 * sdkCameraAreaRequest.leftTopX * vWidth / 100.0).toFloat()
            dot1Y = (1.0 * sdkCameraAreaRequest.leftTopY * vHeight / 100.0).toFloat()
            dot2X = (1.0 * sdkCameraAreaRequest.rightTopX * vWidth / 100.0).toFloat()
            dot2Y = (1.0 * sdkCameraAreaRequest.rightTopY * vHeight / 100.0).toFloat()
            dot3X = (1.0 * sdkCameraAreaRequest.rightBottomX * vWidth / 100.0).toFloat()
            dot3Y = (1.0 * sdkCameraAreaRequest.rightBottomY * vHeight / 100.0).toFloat()
            dot4X = (1.0 * sdkCameraAreaRequest.leftBottomX * vWidth / 100.0).toFloat()
            dot4Y = (1.0 * sdkCameraAreaRequest.leftBottomY * vHeight / 100.0).toFloat()
            rectLeft = getRect(dot1X, dot1Y)
            rectTop = getRect(dot2X, dot2Y)
            rectRight = getRect(dot3X, dot3Y)
            rectBottom = getRect(dot4X, dot4Y)
            Logger.i("setPreview>>$sdkCameraAreaRequest")
        } catch (e: Exception) {
            Logger.e("setPreview:${e.message}")
        }
    }

    private fun getRect(x: Float, y: Float): RectF {
        return RectF().apply {
            left = x - 22.dp.toFloat()
            top = y - 17.5.dp.toFloat()
            right = x + 22.dp.toFloat()
            bottom = y + 17.5.dp.toFloat()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //初始化四个点的默认坐标
        vWidth = this.width.toFloat() //获取当前View的宽
        vHeight = this.height.toFloat() //获取当前View的高
        Logger.i("W:" + vWidth + "H:" + vHeight)
        setPreview()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DEV_KERN_TEXT_FLAG)
        paint.isFilterBitmap = true
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.isAntiAlias = true
        paint.color = Color.WHITE //画笔颜色 （点）
        paint.strokeWidth = 1.dp.toFloat() //画笔宽度 （点的大小）
        rectLeft?.let { canvas.drawRoundRect(it, 3.dp.toFloat(), 3.dp.toFloat(), paint) }
        rectTop?.let { canvas.drawRoundRect(it, 3.dp.toFloat(), 3.dp.toFloat(), paint) }
        rectRight?.let { canvas.drawRoundRect(it, 3.dp.toFloat(), 3.dp.toFloat(), paint) }
        rectBottom?.let { canvas.drawRoundRect(it, 3.dp.toFloat(), 3.dp.toFloat(), paint) }

        paint.strokeWidth = 3f //画笔宽度 （线的粗细）
        paint.style = Paint.Style.STROKE
        path.reset()
        path.moveTo(dot1X, dot1Y) //路径移动到第一个点（从点 1 开始）
        path.lineTo(dot2X, dot2Y) //路径直线到第二个点
        path.lineTo(dot3X, dot3Y) //路径直线到第三个点
        path.lineTo(dot4X, dot4Y) //路径直线到第四个点
        path.lineTo(dot1X, dot1Y) //路径直线到第一个点（闭合形成四边形）
        paint.pathEffect = effects;
        canvas.drawPath(path, paint) //根据路径画出图形
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        eventX = event.x //当前手势X轴坐标
        eventY = event.y //当前手势Y轴坐标
        when (event.action) {
            MotionEvent.ACTION_DOWN ->                 //当手指按下时判断是否选中四个点中的一个
                selectIndex = if (isRangeOfView(dot1X, dot1Y, eventX, eventY)) {
                    0
                } else if (isRangeOfView(dot2X, dot2Y, eventX, eventY)) {
                    1
                } else if (isRangeOfView(dot3X, dot3Y, eventX, eventY)) {
                    2
                } else if (isRangeOfView(dot4X, dot4Y, eventX, eventY)) {
                    3
                } else {
                    -1 //没选中为 -1
                }

            MotionEvent.ACTION_MOVE -> when (selectIndex) {
                0 -> {
                    dot1X = getValue(eventX, vWidth) //点1 x轴赋值=当前手势x轴
                    dot1Y = getValue(eventY, vHeight) //点1 y轴赋值=当前手势y轴
                    rectLeft?.apply {
                        left = dot1X - 22.dp.toFloat()
                        top = dot1Y - 17.5.dp.toFloat()
                        right = dot1X + 22.dp.toFloat()
                        bottom = dot1Y + 17.5.dp.toFloat()
                    }
                    invalidate() //重绘当前View
                }

                1 -> {
                    dot2X = getValue(eventX, vWidth) //点1 x轴赋值=当前手势x轴
                    dot2Y = getValue(eventY, vHeight) //点1 y轴赋值=当前手势y轴
                    rectTop?.apply {
                        left = dot2X - 22.dp.toFloat()
                        top = dot2Y - 17.5.dp.toFloat()
                        right = dot2X + 22.dp.toFloat()
                        bottom = dot2Y + 17.5.dp.toFloat()
                    }
                    invalidate()
                }

                2 -> {
                    dot3X = getValue(eventX, vWidth) //点1 x轴赋值=当前手势x轴
                    dot3Y = getValue(eventY, vHeight) //点1 y轴赋值=当前手势y轴

                    rectRight?.apply {
                        left = dot3X - 22.dp.toFloat()
                        top = dot3Y - 17.5.dp.toFloat()
                        right = dot3X + 22.dp.toFloat()
                        bottom = dot3Y + 17.5.dp.toFloat()
                    }
                    invalidate()
                }

                3 -> {
                    dot4X = getValue(eventX, vWidth) //点1 x轴赋值=当前手势x轴
                    dot4Y = getValue(eventY, vHeight) //点1 y轴赋值=当前手势y轴
                    rectBottom?.apply {
                        left = dot4X - 22.dp.toFloat()
                        top = dot4Y - 17.5.dp.toFloat()
                        right = dot4X + 22.dp.toFloat()
                        bottom = dot4Y + 17.5.dp.toFloat()
                    }
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                savePoint()
            }
        }
        return true
    }

    fun savePoint() {
        if (vWidth > 0 && vHeight > 0) {
            var areaInfo = RecogAreaInfo().apply {
                leftTopX = (100.0 * dot1X / vWidth).toFloat()
                leftTopY = (100.0 * dot1Y / vHeight).toFloat()
                rightTopX = (100.0 * dot2X / vWidth).toFloat()
                rightTopY = (100.0 * dot2Y / vHeight).toFloat()
                rightBottomX = (100.0 * dot3X / vWidth).toFloat()
                rightBottomY = (100.0 * dot3Y / vHeight).toFloat()
                leftBottomX = (100.0 * dot4X / vWidth).toFloat()
                leftBottomY = (100.0 * dot4Y / vHeight).toFloat()
            }
            cropPoint = areaInfo
            SanQNSDK.camera().recogArea().leftTopX(areaInfo.leftTopX).leftTopY(areaInfo.leftTopY)
                .rightTopX(areaInfo.rightTopX).rightTopY(areaInfo.rightTopY)
                .rightBottomX(areaInfo.rightBottomX).rightBottomY(areaInfo.rightBottomY)
                .leftBottomX(areaInfo.leftBottomX).leftBottomY(areaInfo.leftBottomY).enqueue {
                }
            Logger.i("savePoint>>$areaInfo")
        } else {
            Logger.i("savePoint>> width:$vWidth height:$vHeight")
        }
    }


    private fun isRangeOfView(dx: Float, dy: Float, ev_x: Float, ev_y: Float): Boolean {
        //dx：点的x轴  dy：点的y轴  ev_x：手势触摸到的x轴  ev_y：手势触摸到的y轴
        // 点的 x,y 轴 上下左右各增加40像素，扩大触摸范围，判断触摸手势是否在该范围内
        return ev_x > dx - 40 && ev_x < dx + 40 && ev_y > dy - 40 && ev_y < dy + 40
    }
}