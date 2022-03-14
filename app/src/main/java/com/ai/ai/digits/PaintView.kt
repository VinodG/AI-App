package com.ai.ai.digits

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import com.ai.ai.R
import android.graphics.Bitmap


class PaintView : View, OnTouchListener {
    private val TAG: String = PaintView::class.java.simpleName
    private var init = true
    private lateinit var mPaint: Paint
    private var path: Path = Path()
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private val brushColor = Color.WHITE
    private val brushWidth = 32f
    private var rectF = RectF()
    private val backGroundColor = Color.BLACK


    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.PaintView, defStyle, 0
        )


        a.recycle()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (init) {
            rectF = RectF(
                this.left.toFloat(), this.top.toFloat(), this.right.toFloat(),
                this.bottom.toFloat()
            )
            Log.e(
                TAG, Exception().stackTrace[0].methodName + " left " + rectF.left
                        + " right -" + rectF.right + " top- " + rectF.top + " bottom-" + rectF.bottom
            )
            path = Path()
            mPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = brushWidth
                color = brushColor
            }
            setBackgroundColor(backGroundColor)
            setOnTouchListener(this)
            init = false
        }
        canvas.drawPath(path, mPaint)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val eventX = event!!.x
        val eventY = event.y
//        Log.e(TAG," X - "+eventX+" Y - "+eventY );
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(eventX, eventY)
                lastTouchX = eventX
                lastTouchY = eventY
                return true
            }
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                val historySize = event.historySize
                for (i in 0 until historySize) {
                    val historicalX = event.getHistoricalX(i)
                    val historicalY = event.getHistoricalY(i)
                    path.lineTo(historicalX, historicalY)
                }
//                var i = 0
//                while (i < historySize) {
//                    val historicalX = event.getHistoricalX(i)
//                    val historicalY = event.getHistoricalY(i)
//                    path.lineTo(historicalX, historicalY)
//                    i++
//                }
                path.lineTo(eventX, eventY)
                invalidate()
            }
            else -> return false
        }

        lastTouchX = eventX
        lastTouchY = eventY

        return true

    }

    fun clear() {
        path.reset()
        this.invalidate()
        init = true
    }

    fun getBitmap(): Bitmap? {
        var bm: Bitmap? = null
        if (bm == null) {
            bm = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(bm!!)
        draw(canvas)
        return bm
    }
}