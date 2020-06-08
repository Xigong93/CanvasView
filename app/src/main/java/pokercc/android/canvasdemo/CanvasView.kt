package pokercc.android.canvasdemo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.os.TraceCompat
import java.util.*

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
    private var bitmap: Bitmap? = null
    private var bitmapCanvas: Canvas? = null

    private val paths = Stack<Path>()
    private val rubbishPaths = Stack<Path>()
    private var currentPath: Path? = null
    private fun Float.dpToPx(): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        TraceCompat.beginSection("${LOG_TAG}:onDraw")
        try {
            for (path in paths) {
                canvas.drawPath(path, linePaint)
            }
//            currentPath?.let {
//                canvas.drawPath(it, linePaint)
//            }
            bitmap?.let {
                canvas.drawBitmap(it, left.toFloat(), top.toFloat(), null)
            }
        } finally {
            TraceCompat.endSection()
        }
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bitmap?.recycle()
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmapCanvas = Canvas(bitmap!!)
    }


    private var lastX = 0f
    private var lastY = 0f

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bitmap?.recycle()
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                currentPath = Path()
                currentPath?.moveTo(event.x, event.y)
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_POINTER_DOWN -> {

            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.historySize) {
                    val hx = event.getHistoricalX(i)
                    val hy = event.getHistoricalY(i)
                    bitmapCanvas?.drawLine(lastX, lastY, hx, hy, linePaint)
                    lastX = hx
                    lastY = hy
                    currentPath?.lineTo(hx, hy)
                }
                invalidate()
            }
            MotionEvent.ACTION_POINTER_UP -> {

            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                currentPath?.let {
                    paths.push(it)
                }
                currentPath = null
                clearCanvas()
                invalidate()
            }
        }

        return true
    }

    /** 是否有历史笔迹 */
    fun hadHistory(): Boolean = paths.isNotEmpty()

    /** 获取笔画的数量 */
    fun getLineCount(): Int = paths.size

    /** 撤销 */
    fun undo() {
        if (paths.isNotEmpty()) {
            val path = paths.pop()
            rubbishPaths.push(path)
        }
        invalidate()
    }

    /** 恢复 */
    fun redo() {
        if (rubbishPaths.isNotEmpty()) {
            val path = rubbishPaths.pop()
            paths.push(path)
        }
        invalidate()
    }

    /** 清除画板 */
    fun clear() {
        clearCanvas()
        rubbishPaths.clear()
        paths.clear()
        invalidate()
    }

    private fun clearCanvas() {
        bitmapCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    }
}
