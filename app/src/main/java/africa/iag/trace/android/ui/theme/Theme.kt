package africa.iag.trace.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import africa.iag.trace.core.TraceStore

val IagOrange = Color(0xFFF97316)
val IagSuccess = Color(0xFF059669)
val IagInk = Color(0xFF18181B)
val IagSlate = IagInk
val IagEmerald = IagSuccess
val IagSurface = Color(0xFFF4F4F5)
val IagMuted = Color(0xFF71717A)

private val LightColors = lightColorScheme(
    primary = IagOrange,
    onPrimary = Color.White,
    secondary = IagOrange,
    onSecondary = Color.White,
    tertiary = IagInk,
    background = Color(0xFFF5F5F7),
    onBackground = IagInk,
    surface = Color.White,
    onSurface = IagInk,
    surfaceVariant = Color(0xFFE4E4E7),
    onSurfaceVariant = IagMuted,
    outline = Color(0xFFE4E4E7),
    outlineVariant = Color(0xFFE4E4E7),
    error = Color(0xFFB91C1C),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFB923C),
    onPrimary = Color(0xFF1C1917),
    secondary = Color(0xFFFB923C),
    onSecondary = Color(0xFF1C1917),
    tertiary = Color(0xFFF4F4F5),
    background = Color(0xFF0C0C0E),
    onBackground = Color(0xFFF4F4F5),
    surface = Color(0xFF18181B),
    onSurface = Color(0xFFF4F4F5),
    surfaceVariant = Color(0xFF27272A),
    onSurfaceVariant = Color(0xFFA1A1AA),
    outline = Color(0xFF3F3F46),
    outlineVariant = Color(0xFF3F3F46),
    error = Color(0xFFEF4444),
)

@Composable
fun rememberStoreTick(store: TraceStore): Int {
    var tick by remember { mutableIntStateOf(0) }
    DisposableEffect(store) {
        val listener = { tick += 1 }
        store.addListener(listener)
        onDispose { store.removeListener(listener) }
    }
    return tick
}

@Composable
fun TraceAndroidTheme(store: TraceStore, content: @Composable () -> Unit) {
    rememberStoreTick(store)
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}

fun greetingLabel(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}

fun statusColor(status: String): Color {
    val s = status.lowercase()
    if (listOf("paid", "approved", "active", "live", "complete", "valid", "received", "pass", "open").any { s.contains(it) }) {
        return IagSuccess
    }
    if (listOf("reject", "void", "cancel", "revok").any { s.contains(it) }) {
        return Color(0xFFB91C1C)
    }
    if (listOf("pending", "open", "draft", "follow", "incomplete").any { s.contains(it) }) {
        return IagOrange
    }
    return IagMuted
}
