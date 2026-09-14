package africa.iag.trace.core

import java.time.Instant
import kotlin.math.roundToInt

class TraceStore(
    private val persistence: KeyValueStore = MemoryKeyValueStore(),
) {
    var user: AuthUser? = null
        private set
    var records: MutableMap<String, MutableList<TraceRecord>> = mutableMapOf()
    var audit: MutableList<AuditEntry> = mutableListOf()
    var passwords: MutableMap<String, String> = linkedMapOf()
    var seq: Int = 50
    var booted: Boolean = false
        private set

    private val listeners = mutableListOf<() -> Unit>()

    fun addListener(listener: () -> Unit) { listeners.add(listener) }
    fun removeListener(listener: () -> Unit) { listeners.remove(listener) }
    private fun notifyChange() { for (listener in listeners.toList()) listener() }

    val isSignedIn: Boolean get() = user != null
    val isAgent: Boolean get() = user?.isAgent == true
    val isStaff: Boolean get() = user?.isStaff == true
    val isAdmin: Boolean get() = user?.isAdmin == true
    val isPortal: Boolean get() = user?.isPortal == true
    val role: TraceRole? get() = user?.role

    fun of(entity: String): List<TraceRecord> = records[entity] ?: emptyList()

    fun byId(entity: String, id: String): TraceRecord? =
        of(entity).firstOrNull { it.id == id || it["code"] == id || it["reference"] == id }

    val farmers: List<TraceRecord> get() = of("farmers")
    val farms: List<TraceRecord> get() = of("farms")
    val lots: List<TraceRecord> get() = of("harvest-lots")
    val collections: List<TraceRecord> get() = of("collections")
    val inspections: List<TraceRecord> get() = of("field-inspections")
    val certs: List<TraceRecord> get() = of("certifications")
    val batches: List<TraceRecord> get() = of("batches")

    fun farmerById(id: String): TraceRecord? = byId("farmers", id)
    fun farmById(id: String): TraceRecord? = byId("farms", id)

    val myFarmer: TraceRecord?
        get() {
            val id = user?.farmerId
            if (id != null) {
                val hit = farmerById(id)
                if (hit != null) return hit
            }
            val name = user?.farmerName ?: user?.name ?: return null
            return farmers.firstOrNull { it["name"] == name || it["code"] == name }
        }

    private fun owns(row: TraceRecord): Boolean {
        val me = myFarmer
        val name = user?.name ?: ""
        val farmerName = me?.get("name") ?: user?.farmerName ?: ""
        val farmerCode = me?.get("code") ?: ""
        for (k in listOf("farmer", "name", "seller", "supplier", "from")) {
            val v = row[k]
            if (v.isEmpty()) continue
            if (v == name || v == farmerName || v == farmerCode || v == me?.id) return true
        }
        if (row["farmerId"].isNotEmpty() && row["farmerId"] == me?.id) return true
        return false
    }

    fun scoped(entity: String): List<TraceRecord> {
        val rows = of(entity)
        if (!isPortal) return rows
        return rows.filter { owns(it) }
    }

    fun visibleRoutes(): List<TraceRoute> {
        val role = role ?: return emptyList()
        return traceRoutes.filter { canAccessRoute(role, it) }
    }

    val pendingInspections: List<TraceRecord>
        get() = inspections.filter { it.status == "Draft" || it["result"] == "Follow-up" }

    fun load() {
        val raw = persistence.get(STORE_KEY)
        if (raw != null) {
            try {
                val json = MiniJson.parseObject(raw)
                user = (json["user"] as? Map<*, *>)?.let { AuthUser.fromJson(stringMap(it)) }
                seq = (json["seq"] as? Number)?.toInt() ?: 50
                passwords = linkedMapOf()
                val pw = json["passwords"] as? Map<*, *>
                if (pw != null) {
                    for ((k, v) in pw) passwords[k.toString()] = v.toString()
                }
                audit = mutableListOf()
                val rawAudit = json["audit"] as? List<*>
                if (rawAudit != null) {
                    for (item in rawAudit) {
                        val map = item as? Map<*, *> ?: continue
                        audit.add(AuditEntry.fromJson(stringMap(map)))
                    }
                }
                records = mutableMapOf()
                val recs = json["records"] as? Map<*, *>
                if (recs != null) {
                    for ((key, value) in recs) {
                        val list = mutableListOf<TraceRecord>()
                        val rawList = value as? List<*> ?: continue
                        for (item in rawList) {
                            val map = item as? Map<*, *> ?: continue
                            list.add(TraceRecord.fromJson(stringMap(map)))
                        }
                        records[key.toString()] = list
                    }
                }
                if (farmers.isEmpty()) seed()
                for ((key, value) in seedTraceRecords()) {
                    if (records[key] == null) records[key] = value
                }
            } catch (_: Exception) {
                seed()
            }
        } else {
            seed()
            persist()
        }
        booted = true
        notifyChange()
    }

    fun persist() {
        val recs = linkedMapOf<String, Any?>()
        for ((key, rows) in records) {
            recs[key] = rows.map { it.toJson() }
        }
        val payload = linkedMapOf<String, Any?>(
            "user" to user?.toJson(),
            "seq" to seq,
            "passwords" to passwords,
            "audit" to audit.map { it.toJson() },
            "records" to recs,
        )
        persistence.put(STORE_KEY, MiniJson.stringify(payload))
    }

    private fun seed() {
        records = seedTraceRecords()
        audit = seedAudit()
        passwords = linkedMapOf()
        seq = 50
    }

    fun resetDemo() {
        seed()
        user = null
        persist()
        notifyChange()
    }

    fun login(username: String, password: String): String? {
        val u = username.trim().lowercase()
        val expected = passwords[u] ?: DEMO_PASSWORD
        if (password != expected) return "Wrong password."
        user = when (u) {
            "farmer" -> AuthUser("farmer", "Nakato Grace", TraceRole.FARMER, "frm-nakato", "Nakato Grace")
            "supplier" -> AuthUser("supplier", "Namuli Sarah", TraceRole.SUPPLIER, "frm-1", "Namuli Sarah")
            "agent" -> AuthUser("agent", "Auma Inspector", TraceRole.AGENT)
            "admin", "superadmin" -> AuthUser(u, if (u == "admin") "Field Supervisor" else "Trace Admin", TraceRole.ADMIN)
            else -> return "Unknown user. Try farmer, supplier, agent, or admin."
        }
        persist()
        notifyChange()
        return null
    }

    fun resetPassword(username: String, newPassword: String, confirm: String): String? {
        val u = username.trim().lowercase()
        if (u !in knownUsernames) return "Unknown user."
        if (newPassword.length < 6) return "Use at least 6 characters."
        if (newPassword != confirm) return "Passwords do not match."
        passwords[u] = newPassword
        persist()
        notifyChange()
        return null
    }

    fun logout() {
        user = null
        persist()
        notifyChange()
    }

    fun nextRef(prefix: String): String {
        seq += 1
        return "$prefix-${seq.toString().padStart(4, '0')}"
    }

    fun nextEntityCode(entity: String, prefix: String): String {
        val codes = of(entity).map { if (it["code"].isNotEmpty()) it["code"] else it["reference"] }
        return nextCode(prefix, codes)
    }

    private fun appendAudit(action: String, entity: String, label: String) {
        audit.add(0, AuditEntry(newId(), Instant.now().toString(), action, entity, label, user?.name ?: ""))
    }

    fun addRecord(entity: String, fields: Map<String, String>): TraceRecord? {
        val role = role ?: return null
        if (!canCreateIn(role, entity)) return null
        val now = Instant.now().toString()
        val data = fields.toMutableMap()
        if (data["id"].isNullOrEmpty()) data["id"] = newId()
        data["createdAt"] = now
        data["updatedAt"] = now
        val row = TraceRecord(data)
        val next = mutableListOf(row)
        next.addAll(of(entity))
        records[entity] = next
        appendAudit("create", entity, row.label)
        persist()
        notifyChange()
        return row
    }

    fun updateRecord(entity: String, id: String, patch: Map<String, String>) {
        val role = role ?: return
        if (!canEditIn(role, entity)) return
        val rows = of(entity).toMutableList()
        val i = rows.indexOfFirst { it.id == id }
        if (i < 0) return
        val next = rows[i].data.toMutableMap()
        next.putAll(patch)
        next["id"] = id
        next["updatedAt"] = Instant.now().toString()
        val row = TraceRecord(next)
        rows[i] = row
        records[entity] = rows
        appendAudit("update", entity, row.label)
        persist()
        notifyChange()
    }

    fun deleteRecord(entity: String, id: String) {
        val role = role ?: return
        if (!canDeleteIn(role, entity)) return
        val row = byId(entity, id)
        records[entity] = of(entity).filter { it.id != id }.toMutableList()
        appendAudit("delete", entity, row?.label ?: id)
        persist()
        notifyChange()
    }

    fun addInspection(farmId: String, result: String, findings: String) {
        val role = role ?: return
        if (!canInspect(role)) return
        val farm = farmById(farmId)
        addRecord(
            "field-inspections",
            mapOf(
                "reference" to nextRef("INSP"),
                "date" to todayIsoDate(),
                "farm" to (farm?.get("name") ?: farmId),
                "farmId" to farmId,
                "inspector" to (user?.name ?: "Agent"),
                "result" to result,
                "findings" to findings,
                "status" to if (result == "Follow-up") "Draft" else "Complete",
            ),
        )
    }

    fun requestHarvest(volumeKg: Double, notes: String) {
        val role = role ?: return
        if (!canRequestHarvest(role)) return
        val me = myFarmer
        val volume = if (volumeKg == volumeKg.toLong().toDouble()) volumeKg.toLong().toString() else volumeKg.toString()
        addRecord(
            "harvest-requests",
            mapOf(
                "reference" to nextEntityCode("harvest-requests", "HR"),
                "date" to todayIsoDate(),
                "farmer" to (me?.get("name") ?: user?.name ?: "Supplier"),
                "village" to (me?.get("village") ?: ""),
                "volumeKg" to volume,
                "status" to "Pending",
                "notes" to notes,
            ),
        )
    }

    fun intakeFarmer(
        kind: String,
        name: String,
        phone: String,
        village: String,
        district: String,
        variety: String,
        acres: String,
        gps: String,
        bio: String,
        gross: String,
        tare: String,
        moisture: String,
    ): TraceRecord? {
        val role = role ?: return null
        if (!canIntake(role)) return null
        val farmerCode = nextEntityCode("farmers", "FRM")
        val farmCode = nextEntityCode("farms", "FARM")
        val batchCode = nextEntityCode("batches", "BATCH")
        val farmer = addRecord(
            "farmers",
            mapOf(
                "name" to name, "code" to farmerCode, "phone" to phone, "village" to village, "district" to district,
                "variety" to variety, "acres" to acres, "gps" to gps, "bio" to bio, "status" to "Active",
            ),
        )
        addRecord(
            "farms",
            mapOf(
                "name" to "$name farm", "code" to farmCode, "farmer" to name, "farmerId" to (farmer?.id ?: ""),
                "district" to district, "village" to village, "variety" to variety, "acres" to acres, "gps" to gps, "status" to "Active",
            ),
        )
        addRecord(
            "suppliers",
            mapOf("name" to name, "code" to "SUP-$farmerCode", "kind" to kind, "district" to district, "phone" to phone, "status" to "Active"),
        )
        addRecord(
            "batches",
            mapOf(
                "name" to "$name intake", "code" to batchCode, "farmer" to name, "farm" to "$name farm",
                "beanType" to "Arabica", "variety" to variety, "stage" to "Intake", "date" to todayIsoDate(),
                "grossKg" to gross, "tare" to tare.ifEmpty { "0" }, "netKg" to netKg(gross, tare),
                "moisture" to moisture, "status" to "Active",
            ),
        )
        return farmer
    }

    fun advanceBatch(id: String) {
        val role = role ?: return
        if (!canAdvanceBatch(role)) return
        val batch = byId("batches", id) ?: return
        val next = nextStage(batch["stage"]) ?: return
        val patch = mutableMapOf("stage" to next)
        if (next == "Exported") patch["status"] = "Exported"
        updateRecord("batches", id, patch)
    }

    fun publishQr(id: String): String? {
        val role = role ?: return "Your role cannot publish QR stories."
        if (!canPublishQr(role)) return "Your role cannot publish QR stories."
        val story = byId("qr-stories", id) ?: return "Story not found."
        val lot = of("export-lots").firstOrNull { it["code"] == story["lot"] || it["code"] == story["code"] }
            ?: return "Export lot is missing."
        if (!Regex("ready|ship", RegexOption.IGNORE_CASE).containsMatchIn(lot.status)) {
            return "Lot must be Ready or Shipped."
        }
        val ids = story["lot"].split(Regex("""[,\s]+""")).filter { it.isNotEmpty() }.toMutableSet()
        ids.addAll(lot["batches"].split(Regex("""[,\s]+""")).filter { it.isNotEmpty() })
        val related = batches.filter { it["code"] in ids || it.id in ids }
        if (related.any { !Regex("approved|export", RegexOption.IGNORE_CASE).containsMatchIn(it["stage"]) }) {
            return "Every batch in the lot must be Approved or Exported."
        }
        updateRecord("qr-stories", id, mapOf("status" to "Live"))
        return null
    }

    fun revokeQr(id: String) {
        updateRecord("qr-stories", id, mapOf("status" to "Revoked"))
    }

    fun lookup(raw: String): TraceHit? {
        val q = raw.trim().lowercase()
        if (q.isEmpty()) return null
        fun hitCode(row: TraceRecord): Boolean {
            for (key in listOf("code", "traceCode", "lot", "reference")) {
                if (row[key].trim().lowercase() == q) return true
            }
            return false
        }
        val stories = of("qr-stories").filter { hitCode(it) && !Regex("revok", RegexOption.IGNORE_CASE).containsMatchIn(it.status) }
        val codes = of("trace-codes").filter(::hitCode)
        val harvest = lots.filter(::hitCode)
        val exports = of("export-lots").filter(::hitCode)
        val batchHits = batches.filter(::hitCode)
        val match = harvest.firstOrNull() ?: codes.firstOrNull() ?: exports.firstOrNull() ?: stories.firstOrNull() ?: batchHits.firstOrNull()
            ?: return null
        val lot = harvest.firstOrNull() ?: lots.firstOrNull { it["code"] == match["lot"] || it["traceCode"].lowercase() == q }
        val farmerName = if (match["farmer"].isNotEmpty()) match["farmer"] else lot?.get("farmer").orEmpty()
        val farmer = farmers.firstOrNull { it["name"] == farmerName || it["code"] == farmerName || it.id == farmerName }
        val farmName = if (lot == null || lot["farm"].isEmpty()) match["farm"] else lot["farm"]
        val farm = farms.firstOrNull { it["name"] == farmName || it["code"] == farmName }
        val farmerLabel = farmer?.get("name") ?: farmerName
        val relatedCerts = certs.filter { it["farmer"] == farmerName || it["farmer"] == farmerLabel }
        val lotCode = lot?.get("code").orEmpty()
        val lotTrace = lot?.get("traceCode").orEmpty()
        val movements = of("chain-of-custody").filter { row ->
            val lotRef = row["lot"].lowercase()
            lotRef == q || lotRef == lotCode.lowercase() || lotRef == lotTrace.lowercase() ||
                lotRef == match["code"].lowercase() || lotRef == match["lot"].lowercase()
        }
        val story = stories.firstOrNull()
        val qtyRaw = if (lot != null && lot["quantity"].isNotEmpty()) lot["quantity"] else if (match["weightKg"].isNotEmpty()) match["weightKg"] else match["netKg"]
        val crop = if (lot != null && lot["crop"].isNotEmpty()) lot["crop"] else if (match["variety"].isNotEmpty()) match["variety"] else match["grade"]
        val harvestDate = if (lot != null && lot["date"].isNotEmpty()) lot["date"] else match["date"]
        val grade = if (lot != null && lot["grade"].isNotEmpty()) lot["grade"] else match["grade"]
        return TraceHit(
            code = if (match["traceCode"].isNotEmpty()) match["traceCode"] else match["code"],
            farmer = farmerLabel,
            district = if (farmer != null && farmer["district"].isNotEmpty()) farmer["district"] else farm?.get("district").orEmpty(),
            farm = farm?.get("name") ?: farmName,
            crop = crop,
            harvestDate = harvestDate,
            quantityKg = asNum(qtyRaw),
            grade = grade,
            certs = relatedCerts.map { "${it["scheme"]} · ${it.status}" },
            custody = movements.map { "${it["date"]}: ${it["from"]} → ${it["to"]}" },
            story = story?.get("notes").orEmpty(),
            stage = match["stage"],
            status = match.status,
        )
    }

    fun eudrPercent(): Int {
        if (farmers.isEmpty()) return 0
        var ok = 0
        for (farmer in farmers) {
            val farm = farms.firstOrNull { it["farmer"] == farmer["name"] || it["farmer"] == farmer["code"] }
            if (eudrStatus(farmer, farm).compliant) ok += 1
        }
        return ((ok.toDouble() / farmers.size) * 100).roundToInt()
    }

    fun recentActivity(): List<ActivityItem> {
        val rows = mutableListOf<ActivityItem>()
        fun push(entity: String, title: String, routeId: String, pick: (TraceRecord) -> String) {
            for (row in of(entity)) {
                rows.add(ActivityItem(title, pick(row), routeId, recordStamp(row)))
            }
        }
        push("batches", "Batch", "batches") { "${it["code"]} · ${it["stage"]}" }
        push("harvest-requests", "Harvest request", "harvest-requests") { "${it["farmer"]} · ${it.status}" }
        push("lab-results", "Lab", "lab") { "${it["batch"]} · ${it.status}" }
        push("farmer-payments", "Payout", "payments") { "${it["farmer"]} · ${ugx(asNum(it["amount"]))}" }
        push("custody-events", "Custody", "events") { "${it["lot"]}: ${it["from"]} → ${it["to"]}" }
        rows.sortByDescending { it.at }
        return rows.take(8)
    }
}

@Suppress("UNCHECKED_CAST")
private fun stringMap(raw: Map<*, *>): Map<String, Any?> {
    val out = linkedMapOf<String, Any?>()
    for ((k, v) in raw) out[k.toString()] = v
    return out
}
