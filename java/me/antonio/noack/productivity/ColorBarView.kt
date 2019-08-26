package me.antonio.noack.productivity

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.util.TypedValue
import me.antonio.noack.productivity.tasks.EntryDisplayer.colorCountSum
import me.antonio.noack.productivity.tasks.EntryDisplayer.colorCounts
import kotlin.math.roundToInt


class ColorBarView(ctx: Context, attributeSet: AttributeSet?): View(ctx, attributeSet){

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        val height = dpToPx(5f).roundToInt()
        setMeasuredDimension(width, height)
    }

    private val paint = Paint()

    override fun onDraw(canvas: Canvas?) {
        if(canvas == null) return

        val width = measuredWidth.toFloat()
        val height = measuredHeight.toFloat()

        val colorCountSum = colorCountSum

        var ctr = 0
        var lastX = 0f
        for((color, size) in colorCounts.entries.sortedBy { it.key }){
            ctr += size
            val thisX = (ctr * width) / colorCountSum
            paint.color = color
            canvas.drawRect(lastX, 0f, thisX, height, paint)
            lastX = thisX
        }
    }

    private fun dpToPx(dp: Float): Float {
        val r = resources
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.displayMetrics)
    }

}