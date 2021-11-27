package com.udacity

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val circleRadius = resources.getDimension(R.dimen.circleRadius)
    private val primaryColor = resources.getColor(R.color.colorPrimary)
    private var widthSize = 0
    private var heightSize = 0

    private val rect = Rect()
    private lateinit var textClipToShow: String
    private fun setTextClipToShow(value: String) {
        textClipToShow = value
        textPaint.getTextBounds(
            textClipToShow,
            0,
            textClipToShow.length,
            rect
        )
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = resources.getColor(R.color.white)
        textSize = resources.getDimension(R.dimen.default_text_size)
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }

    private val rectanglePaint = Paint().apply {
        isAntiAlias = true
        color = resources.getColor(R.color.colorPrimaryDark)
        style = Paint.Style.FILL
    }

    private val circlePaint = Paint().apply {
        isAntiAlias = true
        color = resources.getColor(R.color.colorAccent)
        style = Paint.Style.FILL
    }

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, new ->
        setTextClipToShow(
            when (new) {
                ButtonState.Clicked -> context.getString(R.string.button_loading)
                ButtonState.Completed ->
                    context.getString(R.string.button_name)
                ButtonState.Loading ->
                    context.getString(R.string.button_loading)
            }
        )
        invalidate()
    }

    var progress: Float by Delegates.observable(0f) { _, _, new ->
        invalidate()
    }

    init {
        setTextClipToShow(context.getString(R.string.button_name))
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawColor(primaryColor)
        if (progress > 0f) {
            drawRectangle(canvas)
            drawCircle(canvas)
        }
        drawText(canvas)
    }

    private fun drawRectangle(canvas: Canvas?) {
        canvas?.save()
        canvas?.drawRoundRect(
            0f, 0f, progress * widthSize.toFloat(), heightSize.toFloat(), 0f, 0f, rectanglePaint
        )
        canvas?.restore()
    }

    private fun drawCircle(canvas: Canvas?) {
        canvas?.save()
        canvas?.translate(
            (widthSize / 2) + (rect.right / 2) + circleRadius,
            (heightSize / 2).toFloat() - circleRadius / 2
        )
        canvas?.drawArc(
            0f, 0f, circleRadius, circleRadius, 0f, progress * 360f, true, circlePaint
        )
        canvas?.restore()
    }

    private fun drawText(canvas: Canvas?) {
        canvas?.save()
        canvas?.translate(
            (widthSize / 2).toFloat(),
            (heightSize / 2).toFloat() - rect.centerY()
        )
        canvas?.drawText(
            textClipToShow,
            0f, 0f, textPaint
        )
        canvas?.restore()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }
}