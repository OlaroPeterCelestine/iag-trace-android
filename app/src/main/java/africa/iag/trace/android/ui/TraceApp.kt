package africa.iag.trace.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import africa.iag.trace.android.ui.theme.iagTopBarColors
import africa.iag.trace.android.ui.theme.rememberStoreTick
import africa.iag.trace.core.TraceStore

@Composable
fun TraceApp(store: TraceStore) {
    rememberStoreTick(store)
    val nav = rememberNavController()
    val start = if (store.isSignedIn) "shell" else "login"
    NavHost(navController = nav, startDestination = start) {
        composable("login") {
            LoginScreen(
                store = store,
                onSignedIn = {
                    nav.navigate("shell") {
                        popUpTo("login") { inclusive = true }
                    }
                },
            )
        }
        composable("shell") {
            ShellScreen(
                store = store,
                onOpenRoute = { nav.navigate("route/$it") },
                onOpenRecord = { entity, id -> nav.navigate("record/$entity/$id") },
                onOpenProfile = { nav.navigate("profile") },
            )
        }
        composable(
            "route/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("id") ?: return@composable
            RouteScreen(
                store = store,
                routeId = id,
                onBack = { nav.popBackStack() },
                onOpenRecord = { entity, recId -> nav.navigate("record/$entity/$recId") },
            )
        }
        composable(
            "record/{entity}/{id}",
            arguments = listOf(
                navArgument("entity") { type = NavType.StringType },
                navArgument("id") { type = NavType.StringType },
            ),
        ) { entry ->
            val entity = entry.arguments?.getString("entity") ?: return@composable
            val id = entry.arguments?.getString("id") ?: return@composable
            RecordDetailScreen(store, entity, id, onBack = { nav.popBackStack() })
        }
        composable("profile") {
            ProfileScreen(
                store = store,
                onBack = { nav.popBackStack() },
                onSignedOut = {
                    nav.navigate("login") {
                        popUpTo("shell") { inclusive = true }
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShellScreen(
    store: TraceStore,
    onOpenRoute: (String) -> Unit,
    onOpenRecord: (String, String) -> Unit,
    onOpenProfile: () -> Unit,
) {
    rememberStoreTick(store)
    var index by remember { mutableIntStateOf(0) }
    val portal = store.isPortal
    val titles = if (portal) listOf("My farm", "Farms", "Lots", "Trace") else listOf("Overview", "Work", "Lots", "More")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(titles[index], fontWeight = FontWeight.SemiBold) },
                colors = iagTopBarColors(),
                actions = {
                    IconButton(onClick = onOpenProfile) {
                        Icon(Icons.Outlined.Person, contentDescription = "Account")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                if (portal) {
                    NavigationBarItem(selected = index == 0, onClick = { index = 0 }, icon = { Icon(Icons.Outlined.Home, null) }, label = { Text("My farm") })
                    NavigationBarItem(selected = index == 1, onClick = { index = 1 }, icon = { Icon(Icons.Outlined.Map, null) }, label = { Text("Farms") })
                    NavigationBarItem(selected = index == 2, onClick = { index = 2 }, icon = { Icon(Icons.Outlined.ViewInAr, null) }, label = { Text("Lots") })
                    NavigationBarItem(selected = index == 3, onClick = { index = 3 }, icon = { Icon(Icons.Outlined.GridView, null) }, label = { Text("Trace") })
                } else {
                    NavigationBarItem(selected = index == 0, onClick = { index = 0 }, icon = { Icon(Icons.Outlined.Home, null) }, label = { Text("Overview") })
                    NavigationBarItem(selected = index == 1, onClick = { index = 1 }, icon = { Icon(Icons.Outlined.WorkOutline, null) }, label = { Text("Work") })
                    NavigationBarItem(selected = index == 2, onClick = { index = 2 }, icon = { Icon(Icons.Outlined.ViewInAr, null) }, label = { Text("Lots") })
                    NavigationBarItem(selected = index == 3, onClick = { index = 3 }, icon = { Icon(Icons.Outlined.MoreHoriz, null) }, label = { Text("More") })
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding)) {
            if (portal) {
                when (index) {
                    0 -> PortalHomeScreen(store, onOpenRecord, onOpenRoute)
                    1 -> RecordListScreen(store, "farms", onOpenRecord)
                    2 -> RecordListScreen(store, "harvest-lots", onOpenRecord)
                    else -> LookupScreen(store)
                }
            } else {
                when (index) {
                    0 -> OverviewScreen(store, onOpenRoute)
                    1 -> WorkCatalogScreen(store, onOpenRoute)
                    2 -> RecordListScreen(store, "harvest-lots", onOpenRecord)
                    else -> MoreScreen(store, onOpenRoute)
                }
            }
        }
    }
}
