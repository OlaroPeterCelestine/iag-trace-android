package africa.iag.trace.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import africa.iag.trace.android.ui.TraceApp
import africa.iag.trace.android.ui.theme.TraceAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as TraceAndroidApp
        setContent {
            TraceAndroidTheme(store = app.store) {
                TraceApp(store = app.store)
            }
        }
    }
}
