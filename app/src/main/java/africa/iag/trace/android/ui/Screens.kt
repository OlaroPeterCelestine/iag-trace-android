package africa.iag.trace.android.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import africa.iag.trace.android.ui.theme.rememberStoreTick
import africa.iag.trace.android.ui.theme.statusColor
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

@Composable
fun OverviewScreen(
    store: TraceStore,
    onOpenRoute: (String) -> Unit,
) {
    rememberStoreTick(store)
    val first = store.user?.name?.split(" ")?.firstOrNull() ?: "there"
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Good morning, $first", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("${store.user?.name} · ${store.user?.role?.name?.lowercase()?.replaceFirstChar { it.titlecase() }}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiCard("Farmers", "${store.farmers.size}", Modifier.weight(1f))
                KpiCard("Lots", "${store.lots.size}", Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiCard("EUDR", "${store.eudrPercent()}%", Modifier.weight(1f))
                KpiCard("Inspections", "${store.pendingInspections.size}", Modifier.weight(1f))
            }
        }
        item { Text("Recent", fontWeight = FontWeight.Bold) }
        items(store.recentActivity(), key = { it.copy + it.routeId }) { item ->
            Text("${item.title}: ${item.copy}", style = MaterialTheme.typography.bodyMedium)
        }
        item {
            OutlinedButton(onClick = { onOpenRoute("lookup") }) { Text("Public lookup") }
            val role = store.role
            if (role != null && canIntake(role)) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { onOpenRoute("intake") }) { Text("Web intake") }
            }
        }
    }
}

@Composable
private fun KpiCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Text(value, fontWeight = FontWeight.Bold)
        }
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
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(store.myFarmer?.get("name") ?: store.user?.name ?: "", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text("${store.myFarmer?.get("district").orEmpty()} · ${store.myFarmer?.get("variety").orEmpty()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        val role = store.role
        if (role != null && canRequestHarvest(role)) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Request pickup", fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = volume, onValueChange = { volume = it }, label = { Text("Volume kg") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                        if (message != null) Text(message!!)
                        Button(onClick = {
                            store.requestHarvest(asNum(volume), notes)
                            message = "Request sent."
                            notes = ""
                        }) { Text("Send harvest request") }
                    }
                }
            }
        }
        items(store.scoped("harvest-lots"), key = { it.id }) { lot ->
            Card(Modifier.fillMaxWidth().clickable { onOpenRecord("harvest-lots", lot.id) }) {
                Column(Modifier.padding(12.dp)) {
                    Text(lot.label, fontWeight = FontWeight.Bold)
                    Text("${lot["crop"]} · ${kg(asNum(lot["quantity"]))} · ${lot["grade"]}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { OutlinedButton(onClick = { onOpenRoute("lookup") }) { Text("Trace a lot") } }
    }
}

@Composable
fun WorkCatalogScreen(store: TraceStore, onOpenRoute: (String) -> Unit) {
    rememberStoreTick(store)
    val groups = store.visibleRoutes().groupBy { it.group }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (group in africa.iag.trace.core.TraceGroup.entries) {
            val routes = groups[group].orEmpty()
            if (routes.isEmpty()) continue
            item { Text(groupLabels[group] ?: group.name, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)) }
            items(routes, key = { it.id }) { route ->
                Card(Modifier.fillMaxWidth().clickable { onOpenRoute(route.id) }) {
                    Column(Modifier.padding(12.dp)) {
                        Text(route.label, fontWeight = FontWeight.Bold)
                        if (route.copy.isNotEmpty()) Text(route.copy, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MoreScreen(store: TraceStore, onOpenRoute: (String) -> Unit) {
    rememberStoreTick(store)
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Registers", fontWeight = FontWeight.Bold) }
        items(moreRegisters, key = { it.entity }) { item ->
            Card(Modifier.fillMaxWidth().clickable { onOpenRoute("register:${item.entity}") }) {
                Text(item.label, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
        }
        item { OutlinedButton(onClick = { onOpenRoute("admin") }) { Text("Audit log") } }
    }
}

@Composable
fun RecordListScreen(store: TraceStore, entity: String, onOpenRecord: (String, String) -> Unit) {
    rememberStoreTick(store)
    var query by remember { mutableStateOf("") }
    val meta = entities[entity]
    val rows = store.scoped(entity).filter { matchesQuery(it, query) }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Search") }, modifier = Modifier.fillMaxWidth())
        }
        items(rows, key = { it.id }) { row ->
            Card(Modifier.fillMaxWidth().clickable { onOpenRecord(entity, row.id) }) {
                Column(Modifier.padding(12.dp)) {
                    Text(row.label, fontWeight = FontWeight.Bold)
                    Text(row.subtitle(meta?.columns ?: listOf("status")), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text(row.status, color = statusColor(row.status), fontSize = 12.sp)
                }
            }
        }
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
        Scaffold(topBar = {
            TopAppBar(title = { Text(entities[entity]?.label ?: entity) }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
            })
        }) { padding ->
            BoxPad(padding) { RecordListScreen(store, entity, onOpenRecord) }
        }
        return
    }
    val route = routeById(routeId)
    Scaffold(topBar = {
        TopAppBar(title = { Text(route?.label ?: routeId) }, navigationIcon = {
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
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Public lookup", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Lot / QR / batch code") }, modifier = Modifier.fillMaxWidth())
        if (hit != null) {
            Text(hit.code, fontWeight = FontWeight.Bold)
            Text("Farmer: ${hit.farmer}")
            Text("Farm: ${hit.farm}")
            Text("District: ${hit.district}")
            Text("Crop: ${hit.crop}")
            Text("Harvest: ${hit.harvestDate}")
            Text("Quantity: ${kg(hit.quantityKg)}")
            Text("Grade: ${hit.grade}")
            if (hit.certs.isNotEmpty()) {
                Text("Certifications", fontWeight = FontWeight.Bold)
                hit.certs.forEach { Text(it) }
            }
            if (hit.custody.isNotEmpty()) {
                Text("Custody", fontWeight = FontWeight.Bold)
                hit.custody.forEach { Text(it) }
            }
            if (hit.story.isNotEmpty()) Text(hit.story)
        } else if (code.isNotBlank()) {
            Text("No public story for that code.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(store.audit, key = { it.id }) { entry ->
            Column {
                Text("${entry.action} · ${entry.entity}", fontWeight = FontWeight.Bold)
                Text("${entry.label} · ${entry.actor}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun HubScreen() {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(hubSections, key = { it.id }) { section ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(section.label, fontWeight = FontWeight.Bold)
                    Text(section.copy, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ReportsScreen(store: TraceStore) {
    rememberStoreTick(store)
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Farmers: ${store.farmers.size}")
        Text("Batches: ${store.batches.size}")
        Text("Lots: ${store.lots.size}")
        Text("EUDR compliant: ${store.eudrPercent()}%")
        Text("Pending inspections: ${store.pendingInspections.size}")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailScreen(store: TraceStore, entity: String, id: String, onBack: () -> Unit) {
    rememberStoreTick(store)
    val row = store.byId(entity, id)
    Scaffold(topBar = {
        TopAppBar(title = { Text(entities[entity]?.singular ?: entity) }, navigationIcon = {
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
    Scaffold(topBar = {
        TopAppBar(title = { Text("Account") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
        })
    }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(store.user?.name ?: "", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text(store.user?.role?.name?.lowercase()?.replaceFirstChar { it.titlecase() } ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Trace Android $APP_VERSION", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = {
                store.logout()
                onSignedOut()
            }, modifier = Modifier.fillMaxWidth()) { Text("Sign out") }
            OutlinedButton(onClick = { store.resetDemo() }, modifier = Modifier.fillMaxWidth()) { Text("Reset demo data") }
        }
    }
}
