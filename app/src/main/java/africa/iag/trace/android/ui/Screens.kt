package africa.iag.trace.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import africa.iag.trace.android.ui.theme.DeskRow
import africa.iag.trace.android.ui.theme.IagEmptyHint
import africa.iag.trace.android.ui.theme.IagGroupedCard
import africa.iag.trace.android.ui.theme.IagListRow
import africa.iag.trace.android.ui.theme.IagSearchField
import africa.iag.trace.android.ui.theme.SectionLabel
import africa.iag.trace.android.ui.theme.WelcomeCard
import africa.iag.trace.android.ui.theme.WelcomeStat
import africa.iag.trace.android.ui.theme.iagTopBarColors
import africa.iag.trace.android.ui.theme.rememberStoreTick
import africa.iag.trace.core.APP_NAME
import africa.iag.trace.core.APP_VERSION
import africa.iag.trace.core.TraceSpecial
import africa.iag.trace.core.TraceStore
import africa.iag.trace.core.asNum
import africa.iag.trace.core.canAdvanceBatch
import africa.iag.trace.core.canIntake
import africa.iag.trace.core.canPublishQr
import africa.iag.trace.core.canRequestHarvest
import africa.iag.trace.core.entities
import africa.iag.trace.core.groupLabels
import africa.iag.trace.core.hubSections
import africa.iag.trace.core.intakeTypes
import africa.iag.trace.core.kg
import africa.iag.trace.core.matchesQuery
import africa.iag.trace.core.moreRegisters
import africa.iag.trace.core.nextStage
import africa.iag.trace.core.routeById

private fun routeIcon(id: String): ImageVector = when (id) {
    "lookup", "qr" -> Icons.Outlined.QrCodeScanner
    "farms" -> Icons.Outlined.Map
    "farmers", "intake" -> Icons.Outlined.Person
    "batches", "export" -> Icons.Outlined.Inventory2
    else -> Icons.Outlined.WorkOutline
}

@Composable
fun OverviewScreen(
    store: TraceStore,
    onOpenRoute: (String) -> Unit,
) {
    rememberStoreTick(store)
    val first = store.user?.name?.split(" ")?.firstOrNull() ?: "there"
    val recent = store.recentActivity()
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            WelcomeCard(
                name = first,
                subtitle = "${store.user?.name} · ${store.user?.role?.name?.lowercase()?.replaceFirstChar { it.titlecase() }}",
                stats = listOf(
                    WelcomeStat("farmers", "Farmers", "${store.farmers.size}"),
                    WelcomeStat("lots", "Lots", "${store.lots.size}"),
                    WelcomeStat("eudr", "EUDR", "${store.eudrPercent()}%"),
                    WelcomeStat("inspect", "Checks", "${store.pendingInspections.size}"),
                ),
            )
        }
        item { SectionLabel("Recent") }
        item {
            IagGroupedCard {
                if (recent.isEmpty()) {
                    IagEmptyHint("No recent activity.")
                } else {
                    recent.forEachIndexed { index, item ->
                        IagListRow(divider = index < recent.lastIndex) {
                            Column {
                                Text(item.title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                Text(item.copy, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
        item { SectionLabel("Shortcuts") }
        item {
            IagGroupedCard {
                IagListRow(onClick = { onOpenRoute("lookup") }) {
                    DeskRow("Public lookup", "Trace a lot or QR code", Icons.Outlined.QrCodeScanner)
                }
                val role = store.role
                if (role != null && canIntake(role)) {
                    IagListRow(onClick = { onOpenRoute("intake") }) {
                        DeskRow("Web intake", "Onboard a farmer and first batch", Icons.Outlined.Person)
                    }
                }
                IagListRow(onClick = { onOpenRoute("admin") }, divider = false) {
                    DeskRow("Audit log", "Who changed what", Icons.AutoMirrored.Outlined.ListAlt)
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
fun PortalHomeScreen(
    store: TraceStore,
    onOpenRecord: (String, String) -> Unit,
    onOpenRoute: (String) -> Unit,
) {
    rememberStoreTick(store)
    var volume by remember { mutableStateOf("50") }
    var notes by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val lots = store.scoped("harvest-lots")
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            WelcomeCard(
                name = store.user?.name?.split(" ")?.firstOrNull() ?: "there",
                subtitle = "${store.myFarmer?.get("district").orEmpty()} · ${store.myFarmer?.get("variety").orEmpty()}",
                stats = listOf(
                    WelcomeStat("lots", "Lots", "${lots.size}"),
                    WelcomeStat("farm", "Farm", store.myFarmer?.get("name") ?: "Yours"),
                ),
            )
        }
        val role = store.role
        if (role != null && canRequestHarvest(role)) {
            item { SectionLabel("Request pickup") }
            item {
                IagGroupedCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = volume, onValueChange = { volume = it }, label = { Text("Volume kg") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
                        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
                        if (message != null) Text(message!!, color = MaterialTheme.colorScheme.primary)
                        Button(onClick = {
                            store.requestHarvest(asNum(volume), notes)
                            message = "Request sent."
                            notes = ""
                        }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Send harvest request") }
                    }
                }
            }
        }
        item { SectionLabel("Your lots") }
        item {
            IagGroupedCard {
                if (lots.isEmpty()) {
                    IagEmptyHint("No lots on your farm yet.")
                } else {
                    lots.forEachIndexed { index, lot ->
                        IagListRow(onClick = { onOpenRecord("harvest-lots", lot.id) }, divider = index < lots.lastIndex) {
                            DeskRow(lot.label, "${lot["crop"]} · ${kg(asNum(lot["quantity"]))} · ${lot["grade"]}", Icons.Outlined.Inventory2, lot.status.ifEmpty { null })
                        }
                    }
                }
            }
        }
        item {
            IagGroupedCard {
                IagListRow(onClick = { onOpenRoute("lookup") }, divider = false) {
                    DeskRow("Trace a lot", "Public lot story", Icons.Outlined.QrCodeScanner)
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
fun WorkCatalogScreen(store: TraceStore, onOpenRoute: (String) -> Unit) {
    rememberStoreTick(store)
    val groups = store.visibleRoutes().groupBy { it.group }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        item { Spacer(Modifier.height(4.dp)) }
        for (group in africa.iag.trace.core.TraceGroup.entries) {
            val routes = groups[group].orEmpty()
            if (routes.isEmpty()) continue
            item { SectionLabel(groupLabels[group] ?: group.name) }
            item {
                IagGroupedCard {
                    routes.forEachIndexed { index, route ->
                        IagListRow(onClick = { onOpenRoute(route.id) }, divider = index < routes.lastIndex) {
                            DeskRow(route.label, route.copy, routeIcon(route.id))
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
fun MoreScreen(store: TraceStore, onOpenRoute: (String) -> Unit) {
    rememberStoreTick(store)
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        item { Spacer(Modifier.height(8.dp)); SectionLabel("Registers") }
        item {
            IagGroupedCard {
                moreRegisters.forEachIndexed { index, item ->
                    IagListRow(onClick = { onOpenRoute("register:${item.entity}") }, divider = index < moreRegisters.lastIndex) {
                        DeskRow(item.label, "Open register", Icons.Outlined.Archive)
                    }
                }
            }
        }
        item { SectionLabel("Admin") }
        item {
            IagGroupedCard {
                IagListRow(onClick = { onOpenRoute("admin") }, divider = false) {
                    DeskRow("Audit log", "Who changed what", Icons.AutoMirrored.Outlined.ListAlt)
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
fun RecordListScreen(store: TraceStore, entity: String, onOpenRecord: (String, String) -> Unit) {
    rememberStoreTick(store)
    var query by remember { mutableStateOf("") }
    val meta = entities[entity]
    val rows = store.scoped(entity).filter { matchesQuery(it, query) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Spacer(Modifier.height(8.dp))
            IagSearchField(query, { query = it }, "Search")
        }
        item {
            IagGroupedCard {
                if (rows.isEmpty()) {
                    IagEmptyHint("No records.")
                } else {
                    rows.forEachIndexed { index, row ->
                        IagListRow(onClick = { onOpenRecord(entity, row.id) }, divider = index < rows.lastIndex) {
                            DeskRow(row.label, row.subtitle(meta?.columns ?: listOf("status")), Icons.Outlined.Inventory2, row.status.ifEmpty { null })
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(
    store: TraceStore,
    routeId: String,
    onBack: () -> Unit,
    onOpenRecord: (String, String) -> Unit,
) {
    rememberStoreTick(store)
    if (routeId.startsWith("register:")) {
        val entity = routeId.removePrefix("register:")
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
            TopAppBar(
                title = { Text(entities[entity]?.label ?: entity) },
                colors = iagTopBarColors(),
                navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
            })
        }) { padding ->
            BoxPad(padding) { RecordListScreen(store, entity, onOpenRecord) }
        }
        return
    }
    val route = routeById(routeId)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
        TopAppBar(
            title = { Text(route?.label ?: routeId) },
            colors = iagTopBarColors(),
            navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
        })
    }) { padding ->
        BoxPad(padding) {
            when (route?.special) {
                TraceSpecial.LOOKUP -> LookupScreen(store)
                TraceSpecial.INTAKE -> IntakeScreen(store)
                TraceSpecial.AUDIT -> AuditScreen(store)
                TraceSpecial.HUB -> HubScreen()
                TraceSpecial.MAP -> RecordListScreen(store, "farms", onOpenRecord)
                TraceSpecial.REPORTS -> ReportsScreen(store)
                TraceSpecial.PORTAL -> PortalHomeScreen(store, onOpenRecord, onOpenRoute = {})
                else -> RecordListScreen(store, route?.entity ?: "farmers", onOpenRecord)
            }
        }
    }
}

@Composable
private fun BoxPad(padding: androidx.compose.foundation.layout.PaddingValues, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Box(Modifier.padding(padding)) { content() }
}

@Composable
fun LookupScreen(store: TraceStore) {
    rememberStoreTick(store)
    var code by remember { mutableStateOf("IAG-LOT-00421") }
    val hit = store.lookup(code)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Lot / QR / batch code") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        )
        if (hit != null) {
            IagGroupedCard {
                IagListRow(divider = true) { DeskRow("Farmer", hit.farmer, Icons.Outlined.Person) }
                IagListRow(divider = true) { DeskRow("Farm", hit.farm, Icons.Outlined.Map) }
                IagListRow(divider = true) { DeskRow("District", hit.district, Icons.Outlined.Map) }
                IagListRow(divider = true) { DeskRow("Crop", hit.crop, Icons.Outlined.Inventory2) }
                IagListRow(divider = true) { DeskRow("Harvest", hit.harvestDate, Icons.Outlined.Inventory2) }
                IagListRow(divider = true) { DeskRow("Quantity", kg(hit.quantityKg), Icons.Outlined.Inventory2) }
                IagListRow(divider = false) { DeskRow("Grade", hit.grade, Icons.Outlined.Inventory2) }
            }
            if (hit.certs.isNotEmpty()) {
                SectionLabel("Certifications")
                IagGroupedCard {
                    hit.certs.forEachIndexed { index, cert ->
                        IagListRow(divider = index < hit.certs.lastIndex) { Text(cert) }
                    }
                }
            }
            if (hit.custody.isNotEmpty()) {
                SectionLabel("Custody")
                IagGroupedCard {
                    hit.custody.forEachIndexed { index, step ->
                        IagListRow(divider = index < hit.custody.lastIndex) { Text(step) }
                    }
                }
            }
            if (hit.story.isNotEmpty()) {
                SectionLabel("Story")
                IagGroupedCard { IagEmptyHint(hit.story) }
            }
        } else if (code.isNotBlank()) {
            IagEmptyHint("No public story for that code.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntakeScreen(store: TraceStore) {
    rememberStoreTick(store)
    var kind by remember { mutableStateOf("Farm") }
    var expanded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("Masaka") }
    var variety by remember { mutableStateOf("SL14") }
    var acres by remember { mutableStateOf("1") }
    var gps by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var gross by remember { mutableStateOf("100") }
    var tare by remember { mutableStateOf("0") }
    var moisture by remember { mutableStateOf("12") }
    var message by remember { mutableStateOf<String?>(null) }
    val role = store.role
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (role == null || !canIntake(role)) {
            Text("Your role cannot onboard farmers.")
            return@Column
        }
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = kind,
                onValueChange = {},
                readOnly = true,
                label = { Text("Kind") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                intakeTypes.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { kind = option; expanded = false })
                }
            }
        }
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Farmer name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text("Village") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = variety, onValueChange = { variety = it }, label = { Text("Variety") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = acres, onValueChange = { acres = it }, label = { Text("Acres") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = gps, onValueChange = { gps = it }, label = { Text("GPS") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = gross, onValueChange = { gross = it }, label = { Text("Gross kg") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = tare, onValueChange = { tare = it }, label = { Text("Tare") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = moisture, onValueChange = { moisture = it }, label = { Text("Moisture") }, modifier = Modifier.fillMaxWidth())
        if (message != null) Text(message!!)
        Button(
            onClick = {
                val farmer = store.intakeFarmer(kind, name, phone, village, district, variety, acres, gps, bio, gross, tare, moisture)
                message = if (farmer == null) "Could not onboard." else "Created ${farmer["code"]}."
            },
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Onboard") }
    }
}

@Composable
fun AuditScreen(store: TraceStore) {
    rememberStoreTick(store)
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item {
            IagGroupedCard {
                store.audit.forEachIndexed { index, entry ->
                    IagListRow(divider = index < store.audit.lastIndex) {
                        Column {
                            Text("${entry.action} · ${entry.entity}", fontWeight = FontWeight.SemiBold)
                            Text("${entry.label} · ${entry.actor}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HubScreen() {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item {
            IagGroupedCard {
                hubSections.forEachIndexed { index, section ->
                    IagListRow(divider = index < hubSections.lastIndex) {
                        DeskRow(section.label, section.copy, Icons.Outlined.WorkOutline)
                    }
                }
            }
        }
    }
}

@Composable
fun ReportsScreen(store: TraceStore) {
    rememberStoreTick(store)
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IagGroupedCard {
            IagListRow { DeskRow("Farmers", "${store.farmers.size}", Icons.Outlined.Person) }
            IagListRow { DeskRow("Batches", "${store.batches.size}", Icons.Outlined.Inventory2) }
            IagListRow { DeskRow("Lots", "${store.lots.size}", Icons.Outlined.Inventory2) }
            IagListRow { DeskRow("EUDR compliant", "${store.eudrPercent()}%", Icons.AutoMirrored.Outlined.ListAlt) }
            IagListRow(divider = false) { DeskRow("Pending inspections", "${store.pendingInspections.size}", Icons.AutoMirrored.Outlined.ListAlt) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailScreen(store: TraceStore, entity: String, id: String, onBack: () -> Unit) {
    rememberStoreTick(store)
    val row = store.byId(entity, id)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
        TopAppBar(
            title = { Text(entities[entity]?.singular ?: entity) },
            colors = iagTopBarColors(),
            navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
        })
    }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (row == null) {
                Text("Record not found.")
                return@Column
            }
            Text(row.label, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            row.data.keys.sorted().forEach { key ->
                if (key != "id") Text("$key: ${row[key]}")
            }
            val role = store.role
            if (entity == "batches" && role != null && canAdvanceBatch(role) && nextStage(row["stage"]) != null) {
                Button(onClick = { store.advanceBatch(id) }) { Text("Advance to ${nextStage(row["stage"])}") }
            }
            if (entity == "qr-stories" && role != null && canPublishQr(role)) {
                Button(onClick = { store.publishQr(id) }) { Text("Publish QR") }
                TextButton(onClick = { store.revokeQr(id) }) { Text("Revoke") }
            }
            if (store.isAdmin) {
                TextButton(onClick = { store.deleteRecord(entity, id); onBack() }) { Text("Delete") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(store: TraceStore, onBack: () -> Unit, onSignedOut: () -> Unit) {
    rememberStoreTick(store)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
        TopAppBar(
            title = { Text("Account") },
            colors = iagTopBarColors(),
            navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
        })
    }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            IagGroupedCard {
                IagListRow { DeskRow(store.user?.name ?: "", store.user?.role?.name?.lowercase()?.replaceFirstChar { it.titlecase() } ?: "", Icons.Outlined.Person) }
                IagListRow(divider = false) { DeskRow(APP_NAME, APP_VERSION, Icons.Outlined.WorkOutline) }
            }
            Button(onClick = {
                store.logout()
                onSignedOut()
            }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Sign out") }
            OutlinedButton(onClick = { store.resetDemo() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Reset demo data") }
        }
    }
}
