package africa.iag.trace.android

import android.app.Application
import africa.iag.trace.core.KeyValueStore
import africa.iag.trace.core.TraceStore

class TraceAndroidApp : Application() {
    lateinit var store: TraceStore
        private set

    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences("iag-trace-android", MODE_PRIVATE)
        store = TraceStore(
            persistence = object : KeyValueStore {
                override fun get(key: String): String? = prefs.getString(key, null)
                override fun put(key: String, value: String) {
                    prefs.edit().putString(key, value).apply()
                }
            },
        )
        store.load()
    }
}
