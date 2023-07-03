package com.example.webtopdf

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import kotlin.math.abs


class SignatureView: LinearLayout {
        private var mContext: Context ?= null
        private var starX = 0f
        private var starY = 0f
//筆
        private var paint = Paint()
//路徑
        private var path = Path()
        private var canvas : Canvas ?= null
        private var bitmap : Bitmap ?= null
//寬度
        private var paintWidth = 10f
//顏色
        private var paintColor = Color.BLACK
        private var mBackgroundColor = Color.WHITE
        private var isTouched = false
        public interface Touch {
            fun OnTouch(isTouch: Boolean)
        }
        private var touch: Touch ?= null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        mContext = context
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.setColor(paintColor)
        paint.strokeWidth = paintWidth
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888)
        if (bitmap != null) {
            canvas = Canvas(bitmap!!)
            canvas?.drawColor(mBackgroundColor)
        }
        isTouched = false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (touch != null) touch!!.OnTouch(true)
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> touchDwon(event)
            MotionEvent.ACTION_MOVE -> {
                isTouched = true
                if (touch != null) touch!!.OnTouch(false)
                touchMove(event)
            }
            MotionEvent.ACTION_UP -> {
                canvas!!.drawPath(path, paint)
                path.reset()
            }
        }
        // 更新
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (bitmap != null) {
            canvas?.drawBitmap(bitmap!!, 0f, 0f, paint)
            canvas?.drawPath(path, paint)
        }
    }

    private fun touchDwon(event: MotionEvent) {
        //重置
        path.reset()
        val downX = event.x
        val downY = event.y
        starX = downX
        starY = downY
        //繪制起點
        path.moveTo(downX, downY)
    }

    private fun touchMove(event: MotionEvent) {
//當前的x,y坐標
        val moveX = event.x
        val moveY = event.y
        //之前的x,y坐標
        val previousX = starX
        val previousY = starY

        val dx = abs(moveX - previousX)
        val dy = abs(moveY - previousY)
        if (dx >= 3 || dy >= 3) {
            val cX = (moveX + previousX) / 2
            val cY = (moveY + previousY) / 2
            path.quadTo(previousX, previousY, cX, cY)
            starX = moveX
            starY = moveY
        }
    }

    fun setPaintColor(paintColor: Int) {
        this.paintColor = paintColor
        paint.color = paintColor
    }

    fun setPaintWidth(paintWidth: Int) {
        this.paintWidth = paintWidth.toFloat()
        paint.strokeWidth = paintWidth.toFloat()
    }

    fun setCanvasColor(canvasColor: Int) {
        this.mBackgroundColor = canvasColor
    }

    fun getBitmap(): Bitmap? {
        //旋轉90度
        val matrix = Matrix()
        matrix.setRotate(90f)
        val bm = Bitmap.createBitmap(bitmap!!, 0, 0, bitmap!!.getWidth(), bitmap!!.getHeight(), matrix, true)
        return bm
    }

    fun getSigstatus(): Boolean? {
        return isTouched
    }
}