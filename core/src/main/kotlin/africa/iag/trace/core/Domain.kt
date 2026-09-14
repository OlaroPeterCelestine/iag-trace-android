package africa.iag.trace.core

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

val batchStages = listOf("Intake", "Wet mill", "Drying", "Dry mill", "Approved", "Exported")
val intakeTypes = listOf("Farm", "Cooperative", "Shop", "Factory")

fun nextCode(prefix: String, existing: List<String>): String {
    val used = mutableSetOf<Int>()
    val re = Regex("^${Regex.escape(prefix)}-(\\d+)$", RegexOption.IGNORE_CASE)
    for (code in existing) {
        val m = re.find(code)
        if (m != null) used.add(m.groupValues[1].toInt())
    }
    var n = existing.size + 1
    while (used.contains(n)) n += 1
    return "$prefix-${n.toString().padStart(4, '0')}"
}

fun netKg(gross: String, tare: String): String {
    val net = (gross.toDoubleOrNull() ?: 0.0) - (tare.toDoubleOrNull() ?: 0.0)
    return "%.1f".format(maxOf(0.0, net))
}

fun hasGps(row: TraceRecord?): Boolean {
    if (row == null) return false
    val gps = row["gps"].trim()
    val lat = row["latitude"].trim()
    val lng = row["longitude"].trim()
    if (lat.isNotEmpty() && lng.isNotEmpty()) return true
    return Regex("""[-+]?\d+(\.\d+)?\s*,\s*[-+]?\d+(\.\d+)?""").containsMatchIn(gps)
}

fun normalizeStage(raw: String): String {
    val key = raw.trim().lowercase()
    for (s in batchStages) if (s.lowercase() == key) return s
    return "Intake"
}

fun stageIndex(raw: String): Int = batchStages.indexOf(normalizeStage(raw))

fun nextStage(raw: String): String? {
    val i = stageIndex(raw)
    if (i < 0 || i >= batchStages.size - 1) return null
    return batchStages[i + 1]
}

fun eudrStatus(farmer: TraceRecord, farm: TraceRecord?): EudrResult {
    val gps = if (hasGps(farm) || hasGps(farmer)) "verified" else "missing"
    val farmerBio = farmer["bio"].trim()
    val farmBio = farm?.get("bio")?.trim().orEmpty()
    val bio = if (farmerBio.isNotEmpty() || farmBio.isNotEmpty()) "recorded" else "missing"
    val satRaw = (if (farmer["satellite"].isEmpty()) farm?.get("satellite").orEmpty() else farmer["satellite"]).lowercase()
    val satellite = if (Regex("pass|ok|clear").containsMatchIn(satRaw)) "pass" else "pending"
    val due = (if (farmer["dueDiligence"].isEmpty()) farm?.get("dueDiligence").orEmpty() else farmer["dueDiligence"]).trim()
    val status = if (gps == "verified" && bio == "recorded" && satellite == "pass" && due.isNotEmpty()) "Compliant" else "Incomplete"
    return EudrResult(gps, satellite, bio, due, status)
}

fun matchesQuery(row: TraceRecord, query: String): Boolean {
    val needle = query.trim().lowercase()
    if (needle.isEmpty()) return true
    return row.data.values.joinToString(" ").lowercase().contains(needle)
}

fun recordStamp(row: TraceRecord): Int {
    for (key in listOf("updatedAt", "createdAt", "date")) {
        val raw = row[key]
        if (raw.isEmpty()) continue
        try {
            return Instant.parse(raw).epochSecond.toInt()
        } catch (_: Exception) {
            try {
                return LocalDate.parse(raw).atStartOfDay().toEpochSecond(ZoneOffset.UTC).toInt()
            } catch (_: Exception) {
            }
        }
    }
    return 0
}
