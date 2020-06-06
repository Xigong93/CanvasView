package pokercc.android.canvasdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import pokercc.android.canvasdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        activityMainBinding.clearButton.setOnClickListener {
            activityMainBinding.canvasView.clear()
        }
    }
}