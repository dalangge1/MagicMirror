package com.example.magicmirror.utils

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 *@author zhangzhe
 *@date 2021/2/8
 *@description
 */

class CornerFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        setWillNotDraw(false)
    }

    var mWidth = 0f
    var mHeight = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w.toFloat()
        mHeight = h.toFloat()
    }

    override fun draw(canvas: Canvas?) {
        canvas?.setDrawFilter(PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))

        canvas?.clipPath(Path().apply {
            addRoundRect(RectF(0f, 0f, mWidth, mHeight), floatArrayOf(50f,50f,50f,50f,50f,50f,50f,50f), Path.Direction.CCW)
        })
        super.draw(canvas)
    }

}