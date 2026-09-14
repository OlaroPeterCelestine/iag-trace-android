package africa.iag.trace.core

private fun rec(fields: Map<String, String>): TraceRecord {
    val data = fields.toMutableMap()
    if (data["createdAt"] == null) data["createdAt"] = "2026-08-25T08:00:00.000Z"
    if (data["updatedAt"] == null) data["updatedAt"] = "2026-08-25T08:00:00.000Z"
    return TraceRecord(data)
}

fun seedTraceRecords(): MutableMap<String, MutableList<TraceRecord>> = mutableMapOf(
    "farmers" to mutableListOf(
        rec(mapOf("id" to "frm-1", "name" to "Namuli Sarah", "code" to "FRM-0001", "phone" to "0772123456", "village" to "Kyanamukaka", "district" to "Masaka", "variety" to "SL14 Arabica", "acres" to "1.8", "gps" to "-0.3412, 31.7361", "bio" to "Third-generation coffee farmer at Africa Coffee Park catchment.", "certs" to "Organic, Fairtrade", "dueDiligence" to "DDR-ACP-0041", "satellite" to "pass", "primaryAgent" to "Okello James", "status" to "Active")),
        rec(mapOf("id" to "frm-2", "name" to "Ssekandi Peter", "code" to "FRM-0002", "phone" to "0754988122", "village" to "Bukakata", "district" to "Masaka", "variety" to "KP423", "acres" to "0.9", "gps" to "", "bio" to "", "certs" to "", "status" to "Active")),
        rec(mapOf("id" to "frm-nakato", "name" to "Nakato Grace", "code" to "F-0142", "phone" to "+256 772 441 200", "village" to "Kyanamukaka", "district" to "Masaka", "variety" to "Arabica AA", "acres" to "2.4", "gps" to "-0.341, 31.736", "bio" to "Shaded arabica gardens near Masaka buying centre.", "certs" to "Organic", "dueDiligence" to "DDR-ACP-0142", "satellite" to "pass", "primaryAgent" to "Auma Inspector", "status" to "Active")),
        rec(mapOf("id" to "frm-okello", "name" to "Okello Peter", "code" to "F-0088", "phone" to "+256 701 220 019", "village" to "Bungokho", "district" to "Mbale", "variety" to "Arabica AA", "acres" to "1.8", "gps" to "1.078, 34.175", "status" to "Active")),
        rec(mapOf("id" to "frm-auma", "name" to "Auma Rose", "code" to "F-0210", "phone" to "+256 782 110 441", "village" to "Kyamuhunga", "district" to "Bushenyi", "variety" to "Robusta", "acres" to "3.1", "status" to "Active")),
    ),
    "farms" to mutableListOf(
        rec(mapOf("id" to "farm-1", "name" to "Namuli plot A", "code" to "FARM-0001", "farmer" to "Namuli Sarah", "farmerId" to "frm-1", "district" to "Masaka", "village" to "Kyanamukaka", "variety" to "SL14 Arabica", "acres" to "1.8", "hectares" to "0.73", "gps" to "-0.3412, 31.7361", "bio" to "Shaded arabica on a 1 ha EUDR square.", "satellite" to "pass", "dueDiligence" to "DDR-ACP-0041", "status" to "Active")),
        rec(mapOf("id" to "farm-2", "name" to "Ssekandi homestead", "code" to "FARM-0002", "farmer" to "Ssekandi Peter", "farmerId" to "frm-2", "district" to "Masaka", "village" to "Bukakata", "variety" to "KP423", "acres" to "0.9", "gps" to "", "status" to "Active")),
        rec(mapOf("id" to "farm-nakato", "name" to "Nakato Garden", "code" to "FM-0142-A", "farmer" to "Nakato Grace", "farmerId" to "frm-nakato", "district" to "Masaka", "hectares" to "2.4", "gps" to "-0.341, 31.736", "satellite" to "pass", "dueDiligence" to "DDR-ACP-0142", "bio" to "Upper slope and valley plots.", "status" to "Active")),
        rec(mapOf("id" to "farm-okello", "name" to "Hillside Arabica", "code" to "FM-0088-A", "farmer" to "Okello Peter", "farmerId" to "frm-okello", "district" to "Mbale", "hectares" to "1.8", "gps" to "1.078, 34.175", "status" to "Active")),
        rec(mapOf("id" to "farm-auma", "name" to "Kyamuhunga Plot", "code" to "FM-0210-A", "farmer" to "Auma Rose", "farmerId" to "frm-auma", "district" to "Bushenyi", "hectares" to "3.1", "status" to "Active")),
    ),
    "plots" to mutableListOf(
        rec(mapOf("id" to "plt-1", "name" to "Upper slope", "code" to "P-1", "farm" to "Nakato Garden", "crop" to "Arabica AA", "hectares" to "1.1", "status" to "Active")),
        rec(mapOf("id" to "plt-2", "name" to "Valley", "code" to "P-2", "farm" to "Nakato Garden", "crop" to "Robusta", "hectares" to "1.3", "status" to "Active")),
        rec(mapOf("id" to "plt-3", "name" to "Main block", "code" to "P-1", "farm" to "Hillside Arabica", "crop" to "Arabica AA", "hectares" to "1.8", "status" to "Active")),
    ),
    "crops" to mutableListOf(
        rec(mapOf("id" to "crop-1", "name" to "Arabica", "code" to "SL14", "season" to "2026 A", "status" to "Active")),
        rec(mapOf("id" to "crop-2", "name" to "Robusta", "code" to "KP423", "season" to "2026 A", "status" to "Active")),
    ),
    "cooperatives" to mutableListOf(
        rec(mapOf("id" to "coop-1", "name" to "Masaka Coffee Growers", "code" to "COOP-0001", "district" to "Masaka", "members" to "42", "status" to "Active")),
    ),
    "cooperative-members" to mutableListOf(
        rec(mapOf("id" to "cm-1", "name" to "Namuli Sarah", "cooperative" to "Masaka Coffee Growers", "farmer" to "FRM-0001", "phone" to "0772123456", "status" to "Active")),
        rec(mapOf("id" to "cm-2", "name" to "Nakato Grace", "cooperative" to "Masaka Coffee Growers", "farmer" to "F-0142", "phone" to "+256 772 441 200", "status" to "Active")),
    ),
    "harvest-requests" to mutableListOf(
        rec(mapOf("id" to "hr-1", "reference" to "HR-0008", "date" to "2026-08-24", "farmer" to "Namuli Sarah", "village" to "Kyanamukaka", "volumeKg" to "200", "status" to "Pending")),
    ),
    "suppliers" to mutableListOf(
        rec(mapOf("id" to "sup-1", "name" to "Namuli Sarah", "code" to "SUP-FRM-0001", "kind" to "Farm", "district" to "Masaka", "status" to "Active")),
        rec(mapOf("id" to "sup-2", "name" to "Masaka Coffee Growers", "code" to "SUP-COOP-0001", "kind" to "Cooperative", "district" to "Masaka", "status" to "Active")),
    ),
    "batches" to mutableListOf(
        rec(mapOf("id" to "batch-1", "name" to "Namuli cherry lot", "code" to "BATCH-0001", "farmer" to "Namuli Sarah", "farm" to "Namuli plot A", "beanType" to "Arabica", "variety" to "SL14 Arabica", "stage" to "Dry mill", "grossKg" to "420", "tare" to "8", "netKg" to "412.0", "moisture" to "12.4", "cup" to "84.2", "grade" to "AA", "status" to "Active")),
        rec(mapOf("id" to "batch-2", "name" to "Ssekandi intake", "code" to "BATCH-0002", "farmer" to "Ssekandi Peter", "farm" to "Ssekandi homestead", "beanType" to "Arabica", "stage" to "Intake", "grossKg" to "180", "tare" to "3", "netKg" to "177.0", "moisture" to "18.1", "status" to "Pending")),
        rec(mapOf("id" to "batch-3", "name" to "Nakato harvest", "code" to "BATCH-0003", "farmer" to "Nakato Grace", "farm" to "Nakato Garden", "beanType" to "Arabica", "variety" to "Arabica AA", "stage" to "Approved", "grossKg" to "848", "tare" to "8", "netKg" to "840.0", "moisture" to "11.8", "cup" to "85.0", "grade" to "AA", "status" to "Active")),
    ),
    "custody-events" to mutableListOf(
        rec(mapOf("id" to "evt-1", "reference" to "EVT-0001", "date" to "2026-08-12", "lot" to "BATCH-0001", "from" to "Namuli plot A", "to" to "ACP wet mill", "quantity" to "412", "status" to "Recorded")),
        rec(mapOf("id" to "evt-2", "reference" to "EVT-0002", "date" to "2026-08-18", "lot" to "BATCH-0001", "from" to "ACP wet mill", "to" to "ACP dry mill", "quantity" to "390", "status" to "Recorded")),
    ),
    "chain-of-custody" to mutableListOf(
        rec(mapOf("id" to "mov-1", "reference" to "MOV-0001", "date" to "2026-08-12", "lot" to "BATCH-0001", "from" to "Namuli plot A", "to" to "ACP wet mill", "quantity" to "412", "status" to "Received")),
        rec(mapOf("id" to "mov-2", "reference" to "MOV-0002", "date" to "2026-08-18", "lot" to "BATCH-0001", "from" to "ACP wet mill", "to" to "ACP dry mill", "quantity" to "390", "status" to "Received")),
        rec(mapOf("id" to "cus-1", "reference" to "COC-0088", "date" to "2026-08-12", "lot" to "IAG-LOT-00421", "from" to "Nakato Garden", "to" to "Masaka buying centre", "quantity" to "840", "status" to "Received")),
        rec(mapOf("id" to "cus-2", "reference" to "COC-0089", "date" to "2026-08-14", "lot" to "IAG-LOT-00421", "from" to "Masaka buying centre", "to" to "Namanve warehouse", "quantity" to "840", "status" to "In transit")),
    ),
    "export-lots" to mutableListOf(
        rec(mapOf("id" to "elot-1", "name" to "ACP Masaka AA", "code" to "LOT-00421", "batches" to "BATCH-0001,BATCH-0003", "grade" to "AA", "weightKg" to "390", "status" to "Ready")),
    ),
    "qr-stories" to mutableListOf(
        rec(mapOf("id" to "qr-1", "name" to "ACP Masaka AA story", "code" to "LOT-00421", "lot" to "LOT-00421", "farmer" to "Namuli Sarah", "status" to "Live", "notes" to "Shaded SL14 from Kyanamukaka. Cup 84.2. Organic + Fairtrade.")),
        rec(mapOf("id" to "qr-2", "name" to "Nakato Garden AA", "code" to "IAG-LOT-00421", "lot" to "LOT-00421", "farmer" to "Nakato Grace", "status" to "Live", "notes" to "Shade trees in place. No prohibited spray observed.")),
    ),
    "lab-results" to mutableListOf(
        rec(mapOf("id" to "lab-1", "reference" to "LAB-0091", "date" to "2026-08-20", "batch" to "BATCH-0001", "moisture" to "12.4", "cup" to "84.2", "grade" to "AA", "status" to "Complete")),
        rec(mapOf("id" to "lab-2", "reference" to "LAB-0092", "date" to "2026-08-25", "batch" to "BATCH-0002", "moisture" to "18.1", "status" to "Pending")),
    ),
    "certifications" to mutableListOf(
        rec(mapOf("id" to "cert-1", "name" to "Organic EU", "code" to "ORG-1182", "farmer" to "Namuli Sarah", "scheme" to "Organic", "issued" to "2025-03-01", "expires" to "2027-03-01", "status" to "Valid")),
        rec(mapOf("id" to "cert-2", "name" to "Fairtrade", "code" to "FT-441", "farmer" to "Namuli Sarah", "scheme" to "Fairtrade", "issued" to "2025-06-01", "expires" to "2026-12-01", "status" to "Valid")),
        rec(mapOf("id" to "cert-3", "name" to "Organic internal ICS", "code" to "ORG-0142", "farmer" to "Nakato Grace", "scheme" to "Organic", "issued" to "2025-04-01", "expires" to "2027-03-31", "status" to "Valid")),
        rec(mapOf("id" to "cert-4", "name" to "Fairtrade", "code" to "FT-0088", "farmer" to "Okello Peter", "scheme" to "Fairtrade", "issued" to "2025-01-15", "expires" to "2026-12-15", "status" to "Valid")),
    ),
    "field-agents" to mutableListOf(
        rec(mapOf("id" to "agt-1", "name" to "Okello James", "code" to "AGT-0004", "phone" to "0703112233", "district" to "Masaka", "farmers" to "18", "status" to "Approved")),
        rec(mapOf("id" to "agt-2", "name" to "Auma Inspector", "code" to "AGT-0005", "phone" to "0782001100", "district" to "Masaka", "farmers" to "12", "status" to "Approved")),
    ),
    "farmer-payments" to mutableListOf(
        rec(mapOf("id" to "pay-1", "reference" to "PAY-331", "date" to "2026-08-13", "farmer" to "Namuli Sarah", "amount" to "2472000", "status" to "Paid")),
        rec(mapOf("id" to "pay-2", "reference" to "PAY-332", "date" to "2026-08-13", "farmer" to "Nakato Grace", "amount" to "11760000", "status" to "Paid")),
    ),
    "farmgate-prices" to mutableListOf(
        rec(mapOf("id" to "price-1", "name" to "Arabica cherry", "district" to "Masaka", "date" to "2026-08-25", "priceKg" to "14000", "status" to "Published")),
        rec(mapOf("id" to "price-2", "name" to "Robusta cherry", "district" to "Bushenyi", "date" to "2026-08-25", "priceKg" to "4200", "status" to "Published")),
    ),
    "purchase-orders" to mutableListOf(
        rec(mapOf("id" to "po-1", "reference" to "PO-0188", "date" to "2026-08-10", "supplier" to "Masaka Coffee Growers", "amount" to "12000000", "status" to "Open")),
        rec(mapOf("id" to "po-2", "reference" to "PO-0189", "date" to "2026-08-11", "supplier" to "Namuli Sarah", "amount" to "2472000", "status" to "Received")),
    ),
    "sales-orders" to mutableListOf(
        rec(mapOf("id" to "so-1", "reference" to "SO-0044", "date" to "2026-08-22", "buyer" to "Nordic Roasters", "lot" to "LOT-00421", "amount" to "48000000", "status" to "Confirmed")),
    ),
    "buyers" to mutableListOf(
        rec(mapOf("id" to "buy-1", "name" to "Nordic Roasters", "code" to "BUY-0012", "country" to "Sweden", "status" to "Active")),
    ),
    "inventory-skus" to mutableListOf(
        rec(mapOf("id" to "sku-1", "name" to "Arabica AA green", "code" to "SKU-AA", "grade" to "AA", "stockKg" to "390", "status" to "Active")),
    ),
    "shipments" to mutableListOf(
        rec(mapOf("id" to "shp-1", "reference" to "SHP-0071", "date" to "2026-08-26", "customer" to "Nordic Roasters", "fromLocation" to "Namanve warehouse", "toLocation" to "Mombasa CFS", "vehicle" to "UAX 441T", "weightKg" to "390", "status" to "Scheduled")),
    ),
    "stock-sessions" to mutableListOf(
        rec(mapOf("id" to "ss-1", "reference" to "STK-0012", "date" to "2026-08-18", "vehicle" to "UAX 441T", "agent" to "Okello James", "sacks" to "12", "status" to "Sealed")),
    ),
    "harvest-lots" to mutableListOf(
        rec(mapOf("id" to "lot-421", "name" to "Nakato AA", "code" to "LOT-00421", "date" to "2026-08-12", "farmer" to "Nakato Grace", "farmerId" to "frm-nakato", "farm" to "Nakato Garden", "farmId" to "farm-nakato", "crop" to "Arabica AA", "quantity" to "840", "grade" to "AA", "traceCode" to "IAG-LOT-00421", "status" to "In store")),
        rec(mapOf("id" to "lot-418", "name" to "Okello AB", "code" to "LOT-00418", "date" to "2026-08-08", "farmer" to "Okello Peter", "farmerId" to "frm-okello", "farm" to "Hillside Arabica", "farmId" to "farm-okello", "crop" to "Arabica AA", "quantity" to "510", "grade" to "AB", "traceCode" to "IAG-LOT-00418", "status" to "Shipped")),
    ),
    "collections" to mutableListOf(
        rec(mapOf("id" to "col-1", "reference" to "COL-0019", "date" to "2026-08-12", "farmer" to "Nakato Grace", "farmerId" to "frm-nakato", "lot" to "IAG-LOT-00421", "lotId" to "lot-421", "buyingCentre" to "Masaka buying centre", "quantity" to "840", "amount" to "11760000", "status" to "Paid")),
    ),
    "trace-codes" to mutableListOf(
        rec(mapOf("id" to "tc-1", "code" to "IAG-LOT-00421", "name" to "Nakato AA public code", "lot" to "LOT-00421", "farmer" to "Nakato Grace", "date" to "2026-08-12", "status" to "Active")),
    ),
    "field-inspections" to mutableListOf(
        rec(mapOf("id" to "insp-1", "reference" to "INSP-0004", "date" to "2026-08-20", "farm" to "Nakato Garden", "farmId" to "farm-nakato", "inspector" to "Auma Inspector", "result" to "Pass", "findings" to "Shade trees in place.", "status" to "Complete")),
        rec(mapOf("id" to "insp-2", "reference" to "INSP-0005", "date" to "2026-08-22", "farm" to "Hillside Arabica", "farmId" to "farm-okello", "inspector" to "Auma Inspector", "result" to "Follow-up", "findings" to "Buffer zone incomplete on eastern edge.", "status" to "Draft")),
    ),
)

fun seedAudit(): MutableList<AuditEntry> = mutableListOf(
    AuditEntry("audit-seed", "2026-08-25T08:00:00.000Z", "seed", "farmers", "Demo TraceIAG records", "system"),
)
