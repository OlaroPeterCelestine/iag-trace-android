package africa.iag.trace.core

const val DEMO_PASSWORD = "iagdemo"
const val APP_NAME = "IAG Trace"
const val APP_VERSION = "1.0.0"
const val STORE_KEY = "iag-trace-android-v1"

enum class TraceRole {
    FARMER,
    SUPPLIER,
    AGENT,
    ADMIN,
}

val knownUsernames = setOf("farmer", "supplier", "agent", "admin", "superadmin")

fun parseTraceRole(raw: String?): TraceRole =
    when ((raw ?: "").trim().lowercase()) {
        "farmer" -> TraceRole.FARMER
        "supplier" -> TraceRole.SUPPLIER
        "admin", "superadmin", "super admin" -> TraceRole.ADMIN
        else -> TraceRole.AGENT
    }

fun isStaffRole(role: TraceRole): Boolean = role == TraceRole.AGENT || role == TraceRole.ADMIN

fun isPortalRole(role: TraceRole): Boolean = role == TraceRole.FARMER || role == TraceRole.SUPPLIER

fun canAccessRoute(role: TraceRole, route: TraceRoute): Boolean {
    if (route.id == "register" && !isStaffRole(role)) return false
    if (isPortalRole(role)) {
        return route.portal || route.open || route.special == TraceSpecial.LOOKUP
    }
    if (route.portal && !isStaffRole(role)) return false
    if (route.staff && !isStaffRole(role)) return false
    return true
}

fun canCreateIn(role: TraceRole, entity: String): Boolean {
    if (isPortalRole(role)) return entity == "harvest-requests"
    return isStaffRole(role)
}

fun canEditIn(role: TraceRole, entity: String): Boolean {
    return isStaffRole(role)
}

fun canDeleteIn(role: TraceRole, entity: String): Boolean = role == TraceRole.ADMIN

fun canIntake(role: TraceRole): Boolean = isStaffRole(role)

fun canAdvanceBatch(role: TraceRole): Boolean = isStaffRole(role)

fun canPublishQr(role: TraceRole): Boolean = isStaffRole(role)

fun canRequestHarvest(role: TraceRole): Boolean = isPortalRole(role)

fun canInspect(role: TraceRole): Boolean = isStaffRole(role)

fun canCollect(role: TraceRole): Boolean = isStaffRole(role)
