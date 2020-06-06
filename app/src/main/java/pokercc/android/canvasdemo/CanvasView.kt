package pokercc.android.canvasdemo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

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

    private val lineWidth = 4f.dpToPx()

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.strokeWidth = lineWidth
        it.color = Color.BLACK
        it.strokeCap = Paint.Cap.ROUND
        it.strokeJoin=Paint.Join.ROUND
    }
    private var bitmap: Bitmap? = null
    private var bitmapCanvas: Canvas? = null

    private fun Float.dpToPx(): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {
            canvas.drawBitmap(it, left.toFloat(), top.toFloat(), null)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    }

    private var lastX = 0f
    private var lastY = 0f
    private fun addPoint(x: Float, y: Float) {
        val bitmap = this.bitmap ?: return
        if (bitmapCanvas == null) {
            bitmapCanvas = Canvas(bitmap)
        }
        val canvas = bitmapCanvas ?: return
        canvas.drawLine(lastX, lastY, x, y, linePaint)
        lastX = x
        lastY = y
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_POINTER_DOWN -> {

            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.historySize) {
                    val hx = event.getHistoricalX(i)
                    val hy = event.getHistoricalY(i)
                    addPoint(hx, hy)
                }
                invalidate()
            }
            MotionEvent.ACTION_POINTER_UP -> {

            }
            MotionEvent.ACTION_UP -> {

            }
            MotionEvent.ACTION_CANCEL -> {

            }
        }

        return true
    }

    /**
     * 是否有历史笔迹
     */
    fun hadHistory(): Boolean = true

    /**
     * 撤销
     */
    fun withDraw() {

    }

    /**
     * 清除画板
     */
    fun clear() {
        val canvas = bitmapCanvas ?: return
        canvas.drawColor(Color.WHITE)
        invalidate()
    }
}