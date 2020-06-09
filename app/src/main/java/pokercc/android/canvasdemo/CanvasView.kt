package pokercc.android.canvasdemo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
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
        it.style = Paint.Style.STROKE
        it.strokeJoin = Paint.Join.ROUND
    }

    private val strokes = ArrayList<Stroke>()
    private val rubbishStrokes = ArrayList<Stroke>()
    private var currentStroke: Stroke? = null
    private fun Float.dpToPx(): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        TraceCompat.beginSection("${LOG_TAG}:onDraw")
        try {
            for (stroke in strokes) {
                stroke.draw(canvas, linePaint)
            }
        } finally {
            TraceCompat.endSection()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                currentStroke = Stroke(event.x, event.y)
                strokes.add(currentStroke!!)
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.historySize) {
                    val hx = event.getHistoricalX(i)
                    val hy = event.getHistoricalY(i)
                    currentStroke?.addPoint(hx, hy)
                }
                invalidate()
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                currentStroke = null
            }
        }

        return true
    }

    /** 是否有历史笔迹 */
    fun hadHistory(): Boolean = strokes.isNotEmpty()

    /** 获取笔画的数量 */
    fun getLineCount(): Int = strokes.size

    /** 撤销 */
    fun undo() {
        if (strokes.isNotEmpty()) {
            val path = strokes.removeAt(strokes.size - 1)
            rubbishStrokes.add(path)
        }
        invalidate()
    }

    /** 恢复 */
    fun redo() {
        if (rubbishStrokes.isNotEmpty()) {
            val path = rubbishStrokes.removeAt(rubbishStrokes.size - 1)
            strokes.add(path)
        }
        invalidate()
    }

    /** 清除画板 */
    fun clear() {
        rubbishStrokes.clear()
        strokes.clear()
        invalidate()
    }

    private class Stroke(private val startX: Float, private val startY: Float) {
        private val paths = ArrayList<Path>()
        private var currentPath: Path? = null

        private var pointCount = 0
        private var lastX = 0f
        private var lastY = 0f
        fun addPoint(x: Float, y: Float) {
            if (currentPath == null) {
                currentPath = Path()
                currentPath?.moveTo(startX, startY)
                paths.add(currentPath!!)
            }
            // 大于100步，新建一个Path
            if (pointCount >= 100) {
                pointCount = 0
                currentPath = Path()
                paths.add(currentPath!!)
                currentPath?.moveTo(lastX, lastY)
            }
            currentPath?.lineTo(x, y)
            lastX = x
            lastY = y
            pointCount++
        }

        fun draw(canvas: Canvas, paint: Paint) {
            for (path in paths) {
                canvas.drawPath(path, paint)
            }
        }
    }
}
