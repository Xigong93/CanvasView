package pokercc.android.canvasdemo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.os.TraceCompat

/**
 * 功能:
 * - 把手指的线路绘制出来
 * - 可以撤销,最近的50笔
 *    撤销的实现思路:
 *     存一个bitmap,然后保留最近50笔轨迹，
 */
class CanvasView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val LOG_TAG = "CanvasView"
    }

    private val lineWidth = 4f.dpToPx()

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.strokeWidth = lineWidth
        it.color = Color.BLACK
        it.strokeCap = Paint.Cap.ROUND
        it.strokeJoin = Paint.Join.ROUND
    }
    private var bitmap: Bitmap? = null
    private var bitmapCanvas: Canvas? = null

    private val lines = ArrayList<Line>()
    private var currentLine: Line? = null
    private fun Float.dpToPx(): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        TraceCompat.beginSection("${LOG_TAG}:onDraw")
        try {
            for (line in lines) {
                line.draw(canvas, linePaint)
            }
            currentLine?.draw(canvas, linePaint)
        } finally {
            TraceCompat.endSection()
        }
//        bitmap?.let {
//            canvas.drawBitmap(it, left.toFloat(), top.toFloat(), null)
//        }

    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmapCanvas = Canvas(bitmap!!)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                currentLine = Line()
                currentLine?.addPoint(event.x, event.y)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {

            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.historySize) {
                    val hx = event.getHistoricalX(i)
                    val hy = event.getHistoricalY(i)
                    currentLine?.addPoint(hx, hy)
                }
                invalidate()
            }
            MotionEvent.ACTION_POINTER_UP -> {

            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                currentLine?.let {
                    lines.add(it)
                }
                currentLine = null

            }
        }

        return true
    }

    /** 是否有历史笔迹 */
    fun hadHistory(): Boolean = lines.isNotEmpty()

    /** 获取笔画的数量 */
    fun getLineCount(): Int = lines.size

    /** 撤销 */
    fun withDraw() {
        if (lines.isNotEmpty()) {
            lines.removeAt(lines.size - 1)
        }
        invalidate()
    }

    /** 清除画板 */
    fun clear() {
        val canvas = bitmapCanvas ?: return
        canvas.drawColor(Color.WHITE)
        invalidate()
    }

    private class Line {

        val xArray = mutableListOf<Float>()
        val yArray = mutableListOf<Float>()
        fun draw(canvas: Canvas, paint: Paint) {
            require(xArray.size == yArray.size)
            for (i in 1 until xArray.size)
                canvas.drawLine(
                    xArray[i - 1], yArray[i - 1],
                    xArray[i], yArray[i],
                    paint
                )
        }

        fun addPoint(x: Float, y: Float) {
            xArray.add(x)
            yArray.add(y)
        }
    }
}