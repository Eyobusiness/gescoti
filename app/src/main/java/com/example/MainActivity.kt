package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MembreEntity
import com.example.data.model.UserRole
import com.example.ui.MainViewModel
import com.example.ui.components.AuthSwitcherDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: MainViewModel) {
    val session by viewModel.session.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    var showAuthSwitcher by remember { mutableStateOf(false) }

    // Auto-dismiss message banner after 4 seconds
    LaunchedEffect(actionMessage) {
        if (actionMessage != null) {
            delay(4000)
            viewModel.clearActionMessage()
        }
    }

    if (!session.isAuthenticated) {
        // Écran d'authentification principale (Email/Identifiant + Mot de passe)
        LoginScreen(viewModel = viewModel)
    } else if (session.isSuperAdmin) {
        // Console Super Administrateur SaaS Plateforme
        SuperAdminScreen(
            viewModel = viewModel,
            onSwitchUserClick = { showAuthSwitcher = true },
            onLogoutClick = { viewModel.logout() }
        )
    } else {
        // Console Tenant avec Menu Vertical (OPPE ou autre paroisse)
        TenantVerticalMenuApp(
            viewModel = viewModel,
            onSwitchUserClick = { showAuthSwitcher = true },
            onLogoutClick = { viewModel.logout() }
        )
    }

    // Modal de changement d'utilisateur / test multi-rôles
    if (showAuthSwitcher) {
        AuthSwitcherDialog(
            viewModel = viewModel,
            onDismiss = { showAuthSwitcher = false }
        )
    }
}

enum class TenantNavigationItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val category: String
) {
    DASHBOARD("🏠 Tableau de bord", Icons.Default.Dashboard, "FINANCES & CAISSE"),
    MEMBRES("👥 Membres", Icons.Default.Group, "MEMBRES & COTISATIONS"),
    COTISATIONS("💰 Cotisations", Icons.Default.Payments, "MEMBRES & COTISATIONS"),
    CAISSE("💵 Caisse", Icons.Default.AccountBalanceWallet, "FINANCES & CAISSE"),
    DEPENSES("💸 Dépenses", Icons.Default.ReceiptLong, "FINANCES & CAISSE"),
    ACTIVITES("📅 Activités", Icons.Default.Event, "PASTORALE"),
    RAPPORTS("📊 Rapports", Icons.Default.BarChart, "PILOTAGE"),
    NOTIFICATIONS("🔔 Notifications", Icons.Default.Notifications, "PILOTAGE"),
    UTILISATEURS("👤 Utilisateurs", Icons.Default.ManageAccounts, "ADMINISTRATION"),
    PARAMETRES("⚙️ Paramètres", Icons.Default.Settings, "ADMINISTRATION")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantVerticalMenuApp(
    viewModel: MainViewModel,
    onSwitchUserClick: () -> Unit,
    onLogoutClick: () -> Unit = {}
) {
    val session by viewModel.session.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedItem by remember { mutableStateOf(TenantNavigationItem.DASHBOARD) }
    var preselectedMembreForCotisation by remember { mutableStateOf<MembreEntity?>(null) }

    val unreadNotifs = remember(notifications) { notifications.count { !it.lu } }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight(),
                drawerContainerColor = Color.White
            ) {
                // EN-TÊTE DU MENU VERTICAL : GESCOTI & COULEURS DU LOGO OPPE
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GescotiMarianNavy)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(GescotiSkyBlue))
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(GescotiMagentaPink))
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(GescotiSunlitGold))
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(GescotiCrimsonRed))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row {
                                Text("G", color = GescotiSkyBlue, fontWeight = FontWeight.Black, fontSize = 22.sp)
                                Text("E", color = GescotiMagentaPink, fontWeight = FontWeight.Black, fontSize = 22.sp)
                                Text("S", color = GescotiSunlitGold, fontWeight = FontWeight.Black, fontSize = 22.sp)
                                Text("C", color = GescotiCrimsonRed, fontWeight = FontWeight.Black, fontSize = 22.sp)
                                Text("O", color = GescotiSkyBlue, fontWeight = FontWeight.Black, fontSize = 22.sp)
                                Text("T", color = GescotiMagentaPink, fontWeight = FontWeight.Black, fontSize = 22.sp)
                                Text("I", color = GescotiSunlitGold, fontWeight = FontWeight.Black, fontSize = 22.sp)
                            }
                            Surface(
                                color = GescotiSunlitGold,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = session.currentUser?.role?.name ?: "",
                                    color = Color.Black,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = session.currentTenant?.nom ?: "Organisation",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!session.currentTenant?.paroisse.isNullOrBlank()) {
                        Text(
                            text = session.currentTenant?.paroisse ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Utilisateur : ${session.currentUser?.nom ?: ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "Année Pastorale : ${session.activePastoralYearLibelle}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GescotiSunlitGold,
                        fontWeight = FontWeight.Bold
                    )
                }

                // FILTRAGE STRICT DU MENU VERTICAL SELON LE RÔLE
                val allowedItems = remember(session.currentUser?.role) {
                    when (session.currentUser?.role) {
                        UserRole.ADMINISTRATEUR -> listOf(
                            TenantNavigationItem.DASHBOARD,
                            TenantNavigationItem.MEMBRES,
                            TenantNavigationItem.COTISATIONS,
                            TenantNavigationItem.CAISSE,
                            TenantNavigationItem.DEPENSES,
                            TenantNavigationItem.ACTIVITES,
                            TenantNavigationItem.RAPPORTS,
                            TenantNavigationItem.NOTIFICATIONS,
                            TenantNavigationItem.UTILISATEURS,
                            TenantNavigationItem.PARAMETRES
                        )
                        UserRole.RESPONSABLE -> listOf(
                            TenantNavigationItem.DASHBOARD,
                            TenantNavigationItem.MEMBRES,
                            TenantNavigationItem.COTISATIONS,
                            TenantNavigationItem.ACTIVITES,
                            TenantNavigationItem.RAPPORTS,
                            TenantNavigationItem.NOTIFICATIONS
                        )
                        UserRole.TRESORERIE -> listOf(
                            TenantNavigationItem.DASHBOARD,
                            TenantNavigationItem.MEMBRES,
                            TenantNavigationItem.COTISATIONS,
                            TenantNavigationItem.CAISSE,
                            TenantNavigationItem.DEPENSES,
                            TenantNavigationItem.RAPPORTS,
                            TenantNavigationItem.NOTIFICATIONS
                        )
                        else -> listOf(
                            TenantNavigationItem.DASHBOARD,
                            TenantNavigationItem.MEMBRES,
                            TenantNavigationItem.COTISATIONS
                        )
                    }
                }

                // LISTE DES ÉLÉMENTS DU MENU VERTICAL
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(GescotiPureWhite)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    val groupedItems = allowedItems.groupBy { it.category }

                    groupedItems.forEach { (category, items) ->
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelSmall,
                            color = GescotiTextSecondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 12.dp, top = 14.dp, bottom = 6.dp)
                        )

                        items.forEach { item ->
                            val isSelected = selectedItem == item
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = if (isSelected) GescotiMarianNavy else Color.DarkGray
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                },
                                badge = {
                                    if (item == TenantNavigationItem.NOTIFICATIONS && unreadNotifs > 0) {
                                        Surface(
                                            color = GescotiCrimsonRed,
                                            shape = CircleShape
                                        ) {
                                            Text(
                                                text = "$unreadNotifs",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                },
                                selected = isSelected,
                                onClick = {
                                    selectedItem = item
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = GescotiNavyContainer,
                                    selectedTextColor = GescotiMarianNavy,
                                    unselectedTextColor = GescotiTextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }

                // PIED DU MENU VERTICAL : SWITCH DE PROFIL & DÉCONNEXION
                Divider(color = GescotiBorderLight)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GescotiPureWhite)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch { drawerState.close() }
                            onSwitchUserClick()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.SwitchAccount, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Changer de Rôle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            scope.launch { drawerState.close() }
                            onLogoutClick()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GescotiCrimsonRed)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Se Déconnecter", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Ouvrir le menu vertical",
                                tint = Color.White
                            )
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = selectedItem.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${session.currentTenant?.sigle ?: (session.currentTenant?.nom ?: "Organisation")} • ${session.activePastoralYearLibelle}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    },
                    actions = {
                        // Accès rapide notifications avec badge
                        IconButton(onClick = { selectedItem = TenantNavigationItem.NOTIFICATIONS }) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifs > 0) {
                                        Badge(containerColor = OppeGoldAccent) {
                                             Text("$unreadNotifs", color = Color.Black)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.White
                                )
                            }
                        }

                        // Bouton Profil / Rôle
                        IconButton(onClick = onSwitchUserClick) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Changer de rôle",
                                tint = Color.White
                            )
                        }

                        // Bouton Déconnexion
                        IconButton(onClick = onLogoutClick) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Se déconnecter",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = OppeBluePrimary)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Bandeau temporaire de feedback
                if (actionMessage != null) {
                    Surface(
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = actionMessage ?: "",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            IconButton(
                                onClick = { viewModel.clearActionMessage() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // CONTENU PRINCIPAL SELON L'ONGLET VERTICAL CHOISI
                Box(modifier = Modifier.fillMaxSize()) {
                    when (selectedItem) {
                        TenantNavigationItem.DASHBOARD -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToCotisations = { selectedItem = TenantNavigationItem.COTISATIONS },
                            onNavigateToCaisse = { selectedItem = TenantNavigationItem.CAISSE },
                            onNavigateToMembres = { selectedItem = TenantNavigationItem.MEMBRES },
                            onNavigateToDepenses = { selectedItem = TenantNavigationItem.DEPENSES }
                        )
                        TenantNavigationItem.MEMBRES -> MembresScreen(
                            viewModel = viewModel,
                            onEnregistrerPaiementPourMembre = { m ->
                                preselectedMembreForCotisation = m
                                selectedItem = TenantNavigationItem.COTISATIONS
                            }
                        )
                        TenantNavigationItem.COTISATIONS -> CotisationsScreen(
                            viewModel = viewModel,
                            preselectedMembre = preselectedMembreForCotisation,
                            onClearPreselection = { preselectedMembreForCotisation = null }
                        )
                        TenantNavigationItem.CAISSE -> CaisseScreen(viewModel = viewModel)
                        TenantNavigationItem.DEPENSES -> DepensesScreen(viewModel = viewModel)
                        TenantNavigationItem.ACTIVITES -> ActivitesScreen(viewModel = viewModel)
                        TenantNavigationItem.RAPPORTS -> RapportsScreen(viewModel = viewModel)
                        TenantNavigationItem.NOTIFICATIONS -> NotificationsScreen(viewModel = viewModel)
                        TenantNavigationItem.UTILISATEURS -> TenantUsersScreen(viewModel = viewModel)
                        TenantNavigationItem.PARAMETRES -> ParametresScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
