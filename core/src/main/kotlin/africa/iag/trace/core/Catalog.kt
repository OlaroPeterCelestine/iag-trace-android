package africa.iag.trace.core

enum class TraceGroup { WORKSPACE, NETWORK, TRACE, QUALITY, COMMERCE, FIELD, HUB, ACCESS, MORE }

enum class TraceSpecial {
    DASHBOARD, INTAKE, BATCHES, CHAIN, QR, COMPLIANCE, REPORTS, MAP, HUB, PORTAL, AUDIT, FARMERS, LOOKUP, REGISTER
}

data class TraceRoute(
    val id: String,
    val label: String,
    val group: TraceGroup,
    val entity: String? = null,
    val staff: Boolean = false,
    val portal: Boolean = false,
    val open: Boolean = false,
    val special: TraceSpecial? = null,
    val copy: String = "",
)

data class TraceEntity(
    val key: String,
    val label: String,
    val singular: String,
    val columns: List<String>,
)

const val TRACE_VERSION = "1.0.5"

val groupLabels = mapOf(
    TraceGroup.WORKSPACE to "Workspace",
    TraceGroup.NETWORK to "Producer network",
    TraceGroup.TRACE to "Traceability",
    TraceGroup.QUALITY to "Quality & insight",
    TraceGroup.COMMERCE to "Commerce",
    TraceGroup.FIELD to "Field & HQ",
    TraceGroup.HUB to "TraceAG Hub",
    TraceGroup.ACCESS to "Admin",
    TraceGroup.MORE to "More registers",
)

val traceRoutes = listOf(
    TraceRoute("overview", "Overview", TraceGroup.WORKSPACE, open = true, special = TraceSpecial.DASHBOARD, copy = "KPIs, EUDR coverage, and recent activity."),
    TraceRoute("farmers", "Farmers", TraceGroup.NETWORK, entity = "farmers", special = TraceSpecial.FARMERS, copy = "Registry, GPS, variety, and certifications."),
    TraceRoute("intake", "Web intake", TraceGroup.NETWORK, open = true, special = TraceSpecial.INTAKE, copy = "Onboard a farmer, farm, and first batch."),
    TraceRoute("cooperatives", "Cooperatives", TraceGroup.NETWORK, entity = "cooperatives", copy = "Co-ops and membership counts."),
    TraceRoute("harvest-requests", "Harvest requests", TraceGroup.NETWORK, entity = "harvest-requests", copy = "Pickup inbox from the supplier portal."),
    TraceRoute("suppliers", "Suppliers", TraceGroup.NETWORK, entity = "suppliers", copy = "Farms, co-ops, shops, and factories."),
    TraceRoute("farms", "Farms", TraceGroup.NETWORK, entity = "farms", copy = "Plots, GPS, acres, and variety."),
    TraceRoute("batches", "Batches", TraceGroup.TRACE, entity = "batches", special = TraceSpecial.BATCHES, copy = "Pipeline: intake → wet mill → drying → dry mill → export."),
    TraceRoute("chain", "Chain of custody", TraceGroup.TRACE, entity = "chain-of-custody", special = TraceSpecial.CHAIN, copy = "Movements from farm to mill to warehouse."),
    TraceRoute("events", "Event ledger", TraceGroup.TRACE, entity = "custody-events", copy = "Append-only custody events."),
    TraceRoute("export", "Export lots", TraceGroup.TRACE, entity = "export-lots", copy = "Ready, pending, and shipped lots."),
    TraceRoute("qr", "QR stories", TraceGroup.TRACE, entity = "qr-stories", special = TraceSpecial.QR, copy = "Publish, preview, or revoke consumer stories."),
    TraceRoute("lab", "Lab results", TraceGroup.QUALITY, entity = "lab-results", copy = "Moisture, cupping, and grade."),
    TraceRoute("compliance", "Compliance", TraceGroup.QUALITY, entity = "certifications", special = TraceSpecial.COMPLIANCE, copy = "EUDR GPS, satellite, bio, and due diligence."),
    TraceRoute("reports", "Reports", TraceGroup.QUALITY, open = true, special = TraceSpecial.REPORTS, copy = "CSV packs for farmers, batches, lab, chain, payouts."),
    TraceRoute("procurement", "Procurement", TraceGroup.COMMERCE, entity = "purchase-orders", copy = "Purchase orders with suppliers."),
    TraceRoute("sales", "Sales orders", TraceGroup.COMMERCE, entity = "sales-orders", copy = "Buyer orders against export lots."),
    TraceRoute("inventory", "Inventory", TraceGroup.COMMERCE, entity = "inventory-skus", copy = "SKU stock by grade."),
    TraceRoute("payments", "Farmer payments", TraceGroup.COMMERCE, entity = "farmer-payments", copy = "Cherry payouts in UGX."),
    TraceRoute("logistics", "Logistics", TraceGroup.COMMERCE, entity = "shipments", copy = "Shipments of export lots."),
    TraceRoute("agents", "Field agents", TraceGroup.FIELD, staff = true, entity = "field-agents", copy = "Roster and catchment."),
    TraceRoute("stock", "Stock & sacks", TraceGroup.FIELD, staff = true, entity = "stock-sessions", copy = "Loading sessions and RFID / QR sacks."),
    TraceRoute("map", "Farm map", TraceGroup.FIELD, open = true, special = TraceSpecial.MAP, copy = "GPS pins and EUDR fences."),
    TraceRoute("marketplace", "Marketplace", TraceGroup.FIELD, entity = "marketplace-listings", copy = "Listings, price, and quantity."),
    TraceRoute("prices", "Farmgate prices", TraceGroup.FIELD, entity = "farmgate-prices", copy = "Published UGX / kg by district."),
    TraceRoute("surveys", "Surveys", TraceGroup.FIELD, staff = true, entity = "surveys", copy = "Quizzes, notices, and engagement."),
    TraceRoute("rewards", "Rewards", TraceGroup.FIELD, staff = true, entity = "rewards", copy = "Loyalty points and badges."),
    TraceRoute("notifications", "Notifications", TraceGroup.FIELD, staff = true, entity = "sms-blasts", copy = "SMS, email, and USSD blasts."),
    TraceRoute("portal", "Portal home", TraceGroup.ACCESS, portal = true, open = true, special = TraceSpecial.PORTAL, copy = "Your batches, harvest requests, and GPS."),
    TraceRoute("portal-harvest", "Request pickup", TraceGroup.ACCESS, portal = true, entity = "harvest-requests", copy = "Ask staff to collect cherry."),
    TraceRoute("portal-batches", "My batches", TraceGroup.ACCESS, portal = true, entity = "batches", copy = "Lots in the mill pipeline."),
    TraceRoute("portal-farms", "My farms", TraceGroup.ACCESS, portal = true, entity = "farms", copy = "Plots and GPS on file."),
    TraceRoute("portal-payments", "My payments", TraceGroup.ACCESS, portal = true, entity = "farmer-payments", copy = "Cherry payouts in UGX."),
    TraceRoute("portal-orders", "My orders", TraceGroup.ACCESS, portal = true, entity = "purchase-orders", copy = "Orders placed with you."),
    TraceRoute("hub", "Hub overview", TraceGroup.HUB, open = true, special = TraceSpecial.HUB, copy = "ACP snapshot, tracker, ledger, loans, housing."),
    TraceRoute("lookup", "Public lookup", TraceGroup.WORKSPACE, open = true, special = TraceSpecial.LOOKUP, copy = "Same story as the desk app /trace?code=."),
    TraceRoute("admin", "Audit log", TraceGroup.ACCESS, staff = true, open = true, special = TraceSpecial.AUDIT, copy = "Creates, patches, and service health."),
    TraceRoute("register", "All registers", TraceGroup.MORE, staff = true, special = TraceSpecial.REGISTER, copy = "Plots, crops, inspections, sacks, disputes, tickets."),
)

data class HubSection(val id: String, val label: String, val copy: String)
data class RegisterRef(val label: String, val entity: String)

val hubSections = listOf(
    HubSection("overview", "Overview & analytics", "ACP snapshot, batches, EUDR, and payouts."),
    HubSection("tracker", "Supply-chain tracker", "Follow cherry from farm to export lot."),
    HubSection("ledger", "Blockchain ledger", "Local custody hashes and event history."),
    HubSection("farmers", "Farmer profiles", "Identity, GPS, variety, and certs."),
    HubSection("health", "Health & insurance", "Membership cover and clinic desk."),
    HubSection("market", "Crop market", "Farmgate board and marketplace GMV."),
    HubSection("contracts", "E-contracts", "Draft offtake and membership agreements."),
    HubSection("payments", "Payments portal", "Cherry payouts and wallet status."),
    HubSection("loans", "Loans & scoring", "Credit desk and simple score cards."),
    HubSection("housing", "Rent-to-own housing", "ACP housing benefit tracker."),
    HubSection("apps", "Mobile apps", "Farmer City and Field Agent surfaces in this app."),
)

val moreRegisters = listOf(
    RegisterRef("Plots", "plots"),
    RegisterRef("Crops", "crops"),
    RegisterRef("Harvest lots", "harvest-lots"),
    RegisterRef("Collections", "collections"),
    RegisterRef("Trace codes", "trace-codes"),
    RegisterRef("Field inspections", "field-inspections"),
    RegisterRef("Co-op members", "cooperative-members"),
    RegisterRef("Buyers", "buyers"),
    RegisterRef("Sacks", "sacks"),
    RegisterRef("Disputes", "disputes"),
    RegisterRef("Support tickets", "support-tickets"),
    RegisterRef("Notices", "notices"),
)

val entities: Map<String, TraceEntity> = listOf(
    TraceEntity("farmers", "Farmers", "Farmer", listOf("name", "code", "district", "status")),
    TraceEntity("farms", "Farms", "Farm", listOf("name", "code", "farmer", "status")),
    TraceEntity("plots", "Plots", "Plot", listOf("name", "code", "farm", "status")),
    TraceEntity("crops", "Crops", "Crop", listOf("name", "code", "season", "status")),
    TraceEntity("cooperatives", "Cooperatives", "Cooperative", listOf("name", "code", "district", "status")),
    TraceEntity("cooperative-members", "Co-op members", "Member", listOf("name", "cooperative", "farmer", "status")),
    TraceEntity("harvest-requests", "Harvest requests", "Harvest request", listOf("reference", "farmer", "volumeKg", "status")),
    TraceEntity("suppliers", "Suppliers", "Supplier", listOf("name", "code", "kind", "status")),
    TraceEntity("batches", "Batches", "Batch", listOf("code", "farmer", "stage", "status")),
    TraceEntity("custody-events", "Custody events", "Event", listOf("reference", "lot", "from", "status")),
    TraceEntity("chain-of-custody", "Chain of custody", "Movement", listOf("reference", "lot", "from", "status")),
    TraceEntity("export-lots", "Export lots", "Export lot", listOf("name", "code", "grade", "status")),
    TraceEntity("qr-stories", "QR stories", "QR story", listOf("name", "code", "farmer", "status")),
    TraceEntity("lab-results", "Lab results", "Lab result", listOf("reference", "batch", "grade", "status")),
    TraceEntity("certifications", "Certifications", "Certification", listOf("name", "farmer", "scheme", "status")),
    TraceEntity("purchase-orders", "Purchase orders", "Purchase order", listOf("reference", "supplier", "amount", "status")),
    TraceEntity("sales-orders", "Sales orders", "Sales order", listOf("reference", "buyer", "lot", "status")),
    TraceEntity("buyers", "Buyers", "Buyer", listOf("name", "code", "country", "status")),
    TraceEntity("inventory-skus", "Inventory", "SKU", listOf("name", "code", "grade", "status")),
    TraceEntity("farmer-payments", "Farmer payments", "Payment", listOf("reference", "farmer", "amount", "status")),
    TraceEntity("field-agents", "Field agents", "Agent", listOf("name", "code", "district", "status")),
    TraceEntity("stock-sessions", "Stock sessions", "Session", listOf("reference", "vehicle", "agent", "status")),
    TraceEntity("sacks", "Sacks", "Sack", listOf("code", "lot", "status")),
    TraceEntity("marketplace-listings", "Marketplace", "Listing", listOf("name", "priceKg", "status")),
    TraceEntity("surveys", "Surveys", "Survey", listOf("name", "status")),
    TraceEntity("rewards", "Rewards", "Reward", listOf("name", "status")),
    TraceEntity("notices", "Notices", "Notice", listOf("name", "status")),
    TraceEntity("farmgate-prices", "Farmgate prices", "Price", listOf("name", "district", "priceKg", "status")),
    TraceEntity("disputes", "Disputes", "Dispute", listOf("reference", "status")),
    TraceEntity("support-tickets", "Support tickets", "Ticket", listOf("subject", "status")),
    TraceEntity("sms-blasts", "Notifications", "Blast", listOf("name", "status")),
    TraceEntity("harvest-lots", "Harvest lots", "Harvest lot", listOf("name", "traceCode", "farmer", "status")),
    TraceEntity("collections", "Collections", "Collection", listOf("reference", "farmer", "quantity", "status")),
    TraceEntity("trace-codes", "Trace codes", "Trace code", listOf("code", "farmer", "lot", "status")),
    TraceEntity("field-inspections", "Field inspections", "Inspection", listOf("reference", "farm", "result", "status")),
    TraceEntity("shipments", "Shipments", "Shipment", listOf("reference", "customer", "status")),
).associateBy { it.key }

fun routeById(id: String): TraceRoute? = traceRoutes.firstOrNull { it.id == id }
