package pokercc.android.canvasdemo

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Choreographer

class FpsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {

    private var lastTime: Long = 0
    private var fps: Int = 0
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            val frameTime = frameTimeNanos - lastTime
            fps = (1000000000 / frameTime).toInt()
            lastTime = frameTimeNanos
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    private val showFps = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            val newText = "${fps}fps"
            if (newText != text) {
                this@FpsView.text = newText
            }
            postDelayed(this, 1000)
        }

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Choreographer.getInstance().postFrameCallback(frameCallback)
        post(showFps)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }
}