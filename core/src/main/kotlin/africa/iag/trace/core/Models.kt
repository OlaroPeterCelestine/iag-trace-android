package africa.iag.trace.core

import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale
import kotlin.math.abs

data class AuthUser(
    val username: String,
    val name: String,
    val role: TraceRole,
    val farmerId: String? = null,
    val farmerName: String? = null,
) {
    val isAgent: Boolean get() = isStaffRole(role)
    val isStaff: Boolean get() = isStaffRole(role)
    val isAdmin: Boolean get() = role == TraceRole.ADMIN
    val isPortal: Boolean get() = isPortalRole(role)

    fun toJson(): Map<String, Any?> = mapOf(
        "username" to username,
        "name" to name,
        "role" to role.name.lowercase(),
        "farmerId" to farmerId,
        "farmerName" to farmerName,
    )

    companion object {
        fun fromJson(j: Map<String, Any?>): AuthUser = AuthUser(
            username = j["username"]?.toString() ?: "",
            name = j["name"]?.toString() ?: "",
            role = parseTraceRole(j["role"]?.toString()),
            farmerId = j["farmerId"]?.toString(),
            farmerName = j["farmerName"]?.toString(),
        )
    }
}

class TraceRecord(data: Map<String, String>) {
    val data: MutableMap<String, String> = data.toMutableMap()
    val id: String get() = data["id"] ?: ""
    val status: String get() = data["status"] ?: ""
    operator fun get(key: String): String = data[key] ?: ""
    operator fun set(key: String, value: String) {
        data[key] = value
    }

    val label: String
        get() {
            for (key in listOf("name", "code", "reference", "traceCode")) {
                val v = (data[key] ?: "").trim()
                if (v.isNotEmpty()) return v
            }
            return id
        }

    fun subtitle(columns: List<String>): String {
        val parts = mutableListOf<String>()
        for (c in columns) {
            if (c == "name" || c == "code" || c == "reference") continue
            val v = (data[c] ?: "").trim()
            if (v.isNotEmpty()) parts.add(v)
        }
        return parts.take(3).joinToString(" · ")
    }

    fun toJson(): Map<String, String> = data.toMap()

    companion object {
        fun fromJson(j: Map<String, Any?>): TraceRecord {
            val out = linkedMapOf<String, String>()
            for ((k, v) in j) {
                if (v != null) out[k] = v.toString()
            }
            return TraceRecord(out)
        }
    }
}

data class AuditEntry(
    val id: String,
    val at: String,
    val action: String,
    val entity: String,
    val label: String,
    val actor: String,
) {
    fun toJson(): Map<String, Any?> = mapOf(
        "id" to id, "at" to at, "action" to action, "entity" to entity, "label" to label, "actor" to actor,
    )

    companion object {
        fun fromJson(j: Map<String, Any?>): AuditEntry = AuditEntry(
            id = j["id"]?.toString() ?: newId(),
            at = j["at"]?.toString() ?: "",
            action = j["action"]?.toString() ?: "",
            entity = j["entity"]?.toString() ?: "",
            label = j["label"]?.toString() ?: "",
            actor = j["actor"]?.toString() ?: "",
        )
    }
}

data class EudrResult(
    val gps: String,
    val satellite: String,
    val bio: String,
    val dueDiligence: String,
    val status: String,
) {
    val compliant: Boolean get() = status == "Compliant"
}

data class TraceHit(
    val code: String,
    val farmer: String,
    val district: String,
    val farm: String,
    val crop: String,
    val harvestDate: String,
    val quantityKg: Double,
    val grade: String,
    val certs: List<String>,
    val custody: List<String>,
    val story: String = "",
    val stage: String = "",
    val status: String = "",
)

data class ActivityItem(
    val title: String,
    val copy: String,
    val routeId: String,
    val at: Int,
)

interface KeyValueStore {
    fun get(key: String): String?
    fun put(key: String, value: String)
}

class MemoryKeyValueStore : KeyValueStore {
    private val data = linkedMapOf<String, String>()
    override fun get(key: String): String? = data[key]
    override fun put(key: String, value: String) {
        data[key] = value
    }
}

fun todayIsoDate(): String = LocalDate.now().toString()

fun newId(): String = "${System.nanoTime()}-${(System.currentTimeMillis() % 997)}"

fun asNum(raw: String): Double = raw.replace(",", "").toDoubleOrNull() ?: 0.0

fun kg(n: Double): String = if (n == n.toLong().toDouble()) "${n.toLong()} kg" else "%.1f kg".format(n)

fun ugx(n: Double): String {
    val fmt = NumberFormat.getIntegerInstance(Locale.US)
    return "${if (n < 0) "-" else ""}UGX ${fmt.format(abs(n).toLong())}"
}
