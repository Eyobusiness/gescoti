package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TenantEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.MainViewModel
import com.example.ui.components.GescotiBrandHeader
import com.example.ui.components.StatKpiCard
import com.example.ui.theme.*
import com.example.utils.DateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminScreen(
    viewModel: MainViewModel,
    onSwitchUserClick: () -> Unit,
    onLogoutClick: () -> Unit = {}
) {
    val tenants by viewModel.allTenants.collectAsState()
    val users by viewModel.allUsers.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Tableau de bord, 1: Utilisateurs, 2: Créer un compte
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val screenTitle = when (selectedTab) {
        0 -> "🏠 Tableau de bord"
        1 -> "👥 Utilisateurs"
        2 -> "🏢 Créer un compte"
        else -> "Super Administrateur"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(310.dp)
                    .fillMaxHeight(),
                drawerContainerColor = Color.White
            ) {
                // EN-TÊTE SUPER ADMINISTRATEUR AVEC COULEURS DU LOGO OPPE
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GescotiMarianNavy)
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = GescotiMarianNavy,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row {
                                Text("G", color = GescotiSkyBlue, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                Text("E", color = GescotiMagentaPink, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                Text("S", color = GescotiSunlitGold, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                Text("C", color = GescotiCrimsonRed, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                Text("O", color = GescotiSkyBlue, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                Text("T", color = GescotiMagentaPink, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                Text("I", color = GescotiSunlitGold, fontWeight = FontWeight.Black, fontSize = 20.sp)
                            }
                            Text(
                                text = "SUPER ADMINISTRATEUR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Console Plateforme SaaS Église",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Surface(
                        color = GescotiSunlitGold,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "Accès Plateforme Globale",
                            color = Color.Black,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp
                        )
                    }
                }

                // MENU VERTICAL À 3 CHOIX STRICTS
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "MENU SUPER ADMIN",
                        style = MaterialTheme.typography.labelSmall,
                        color = GescotiTextSecondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                    )

                    // 1. 🏠 Tableau de bord
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Dashboard,
                                contentDescription = "Tableau de bord",
                                tint = if (selectedTab == 0) GescotiMarianNavy else Color.DarkGray
                            )
                        },
                        label = {
                            Text(
                                text = "🏠 Tableau de bord",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        },
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = GescotiNavyContainer,
                            selectedTextColor = GescotiMarianNavy,
                            unselectedTextColor = GescotiTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 2. 👥 Utilisateurs
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = "Utilisateurs",
                                tint = if (selectedTab == 1) GescotiMarianNavy else Color.DarkGray
                            )
                        },
                        label = {
                            Text(
                                text = "👥 Utilisateurs",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        },
                        badge = {
                            Surface(
                                color = GescotiSkyBlueContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${users.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GescotiMarianNavy,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        },
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = GescotiNavyContainer,
                            selectedTextColor = GescotiMarianNavy,
                            unselectedTextColor = GescotiTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 3. 🏢 Créer un compte
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AddBusiness,
                                contentDescription = "Créer un compte",
                                tint = if (selectedTab == 2) GescotiMarianNavy else Color.DarkGray
                            )
                        },
                        label = {
                            Text(
                                text = "🏢 Créer un compte",
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        },
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = GescotiNavyContainer,
                            selectedTextColor = GescotiMarianNavy,
                            unselectedTextColor = GescotiTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // PIED DU MENU VERTICAL
                Divider(color = GescotiBorderLight)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
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
            containerColor = GescotiPureWhite,
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
                                text = screenTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "GESCOTI • Plateforme SaaS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onSwitchUserClick) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Changer de rôle / profil",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = onLogoutClick) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Se déconnecter",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = GescotiMarianNavy
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GescotiPureWhite)
                    .padding(padding)
            ) {
                when (selectedTab) {
                    0 -> SuperAdminDashboard(tenants, users, viewModel)
                    1 -> SuperAdminUsersList(users, tenants, viewModel)
                    2 -> SuperAdminCreateTenant(viewModel) {
                        selectedTab = 0
                    }
                }
            }
        }
    }
}

@Composable
fun SuperAdminDashboard(
    tenants: List<TenantEntity>,
    users: List<UserEntity>,
    viewModel: MainViewModel
) {
    val totalTenants = tenants.size
    val tenantsActifs = tenants.count { it.actif }
    val tenantsInactifs = totalTenants - tenantsActifs
    val totalUtilisateurs = users.size

    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(GescotiPureWhite)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Tableau de Bord Super Administrateur",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = GescotiMarianNavy
            )
            Text(
                text = "Supervision centrale de la plateforme SaaS GESCOTI",
                style = MaterialTheme.typography.bodySmall,
                color = GescotiTextSecondary
            )
        }

        // CARTES STATISTIQUES GLOBALES
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatKpiCard(
                    title = "Organisations",
                    value = "$totalTenants",
                    subtitle = "$tenantsActifs actives • $tenantsInactifs désactivées",
                    icon = Icons.Default.CorporateFare,
                    containerColor = GescotiSkyBlueContainer,
                    contentColor = GescotiMarianNavy,
                    modifier = Modifier.weight(1f)
                )
                StatKpiCard(
                    title = "Utilisateurs",
                    value = "$totalUtilisateurs",
                    subtitle = "Sur l'ensemble de la plateforme",
                    icon = Icons.Default.PeopleAlt,
                    containerColor = GescotiMagentaContainer,
                    contentColor = GescotiMagentaPink,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatKpiCard(
                    title = "Comptes Actifs",
                    value = "$tenantsActifs",
                    subtitle = "${if (totalTenants > 0) (tenantsActifs * 100 / totalTenants) else 100}% du parc",
                    icon = Icons.Default.CheckCircle,
                    containerColor = GescotiGreenContainer,
                    contentColor = GescotiGreenSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatKpiCard(
                    title = "Comptes Désactivés",
                    value = "$tenantsInactifs",
                    subtitle = "Accès suspendus",
                    icon = Icons.Default.Block,
                    containerColor = GescotiRedContainer,
                    contentColor = GescotiCrimsonRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // RÈGLE DE SÉCURITÉ
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = GescotiGoldContainer),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFFF57F17),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Isolation Financière Garantie",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Le Super Administrateur gère l'infrastructure et les tenants, sans accès aux caisses internes ni aux opérations confidentielles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5D4037)
                        )
                    }
                }
            }
        }

        // DERNIERS COMPTES CRÉÉS
        item {
            Text(
                text = "Dernières Organisations Créées",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GescotiMarianNavy
            )
        }

        items(tenants) { tenant ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder(),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GescotiSkyBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Church,
                            contentDescription = null,
                            tint = GescotiMarianNavy,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tenant.nom,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = GescotiTextPrimary
                        )
                        Text(
                            text = "${tenant.sigle} • ${tenant.paroisse}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GescotiTextSecondary
                        )
                        Text(
                            text = "${tenant.email} • ${tenant.telephone}",
                            style = MaterialTheme.typography.labelSmall,
                            color = GescotiTextSecondary
                        )
                    }

                    // Bascule d'activation / désactivation du compte organisation
                    Switch(
                        checked = tenant.actif,
                        onCheckedChange = { checked ->
                            scope.launch {
                                viewModel.db.tenantDao().updateTenant(tenant.copy(actif = checked))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SuperAdminUsersList(
    users: List<UserEntity>,
    tenants: List<TenantEntity>,
    viewModel: MainViewModel
) {
    val tenantMap = tenants.associateBy { it.id }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedUserForDetail by remember { mutableStateOf<UserEntity?>(null) }
    var userToEdit by remember { mutableStateOf<UserEntity?>(null) }

    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) users else {
            users.filter { u ->
                u.nom.contains(searchQuery, ignoreCase = true) ||
                        u.email.contains(searchQuery, ignoreCase = true) ||
                        (u.tenantId != null && (tenantMap[u.tenantId]?.nom?.contains(searchQuery, ignoreCase = true) ?: false))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GescotiPureWhite)
            .padding(16.dp)
    ) {
        Text(
            text = "Gestion des Utilisateurs SaaS",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = GescotiMarianNavy
        )
        Text(
            text = "Rechercher, consulter, activer/désactiver ou modifier les utilisateurs",
            style = MaterialTheme.typography.bodySmall,
            color = GescotiTextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Barre de recherche
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Rechercher par nom, email, organisation...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredUsers) { user ->
                val orgNom = if (user.tenantId != null) tenantMap[user.tenantId]?.nom ?: "Tenant inconnu" else "Plateforme SaaS Universelle"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder(),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(GescotiNavyContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = GescotiMarianNavy
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.nom,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = GescotiTextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = if (user.actif) GescotiGreenContainer else GescotiRedContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (user.actif) "Actif" else "Désactivé",
                                        color = if (user.actif) GescotiGreenSuccess else GescotiCrimsonRed,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = GescotiTextSecondary
                            )
                            Text(
                                text = "Org: $orgNom • Rôle: ${user.role.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = GescotiMarianNavy,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Actions rapides : Voir profil, Modifier, Activer/Désactiver
                        IconButton(onClick = { selectedUserForDetail = user }) {
                            Icon(Icons.Default.Visibility, contentDescription = "Voir le profil", tint = GescotiMarianNavy)
                        }

                        IconButton(onClick = { userToEdit = user }) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = GescotiSunlitGold)
                        }

                        Switch(
                            checked = user.actif,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    viewModel.db.userDao().updateUser(user.copy(actif = checked))
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialogue Détails Profil Utilisateur
    if (selectedUserForDetail != null) {
        val u = selectedUserForDetail!!
        AlertDialog(
            onDismissRequest = { selectedUserForDetail = null },
            title = { Text(text = "Profil Utilisateur", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Nom : ${u.nom}", fontWeight = FontWeight.Bold)
                    Text("Email : ${u.email}")
                    Text("Téléphone : ${u.telephone.ifEmpty { "Non renseigné" }}")
                    Text("Rôle : ${u.role.name}", color = GescotiMarianNavy, fontWeight = FontWeight.Bold)
                    Text("Organisation : ${if (u.tenantId != null) tenantMap[u.tenantId]?.nom ?: "" else "Super Admin Universel"}")
                    Text("Date création : ${DateUtils.formatDate(u.dateCreation)}")
                    Text("Statut : ${if (u.actif) "Compte Actif" else "Compte Désactivé"}", color = if (u.actif) GescotiGreenSuccess else GescotiCrimsonRed, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(onClick = { selectedUserForDetail = null }) {
                    Text("Fermer")
                }
            }
        )
    }

    // Dialogue Modifier Informations Utilisateur
    if (userToEdit != null) {
        var editNom by remember { mutableStateOf(userToEdit!!.nom) }
        var editTel by remember { mutableStateOf(userToEdit!!.telephone) }
        var editRole by remember { mutableStateOf(userToEdit!!.role) }

        AlertDialog(
            onDismissRequest = { userToEdit = null },
            title = { Text("Modifier les informations", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editNom,
                        onValueChange = { editNom = it },
                        label = { Text("Nom complet") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editTel,
                        onValueChange = { editTel = it },
                        label = { Text("Téléphone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Rôle :", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(UserRole.ADMINISTRATEUR, UserRole.TRESORERIE, UserRole.RESPONSABLE).forEach { r ->
                            FilterChip(
                                selected = editRole == r,
                                onClick = { editRole = r },
                                label = { Text(r.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.db.userDao().updateUser(
                                userToEdit!!.copy(
                                    nom = editNom.trim(),
                                    telephone = editTel.trim(),
                                    role = editRole
                                )
                            )
                            userToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GescotiMarianNavy)
                ) {
                    Text("Enregistrer les modifications")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToEdit = null }) { Text("Annuler") }
            }
        )
    }
}

@Composable
fun SuperAdminCreateTenant(
    viewModel: MainViewModel,
    onCreated: () -> Unit
) {
    var nomOrg by remember { mutableStateOf("") }
    var sigle by remember { mutableStateOf("") }
    var paroisse by remember { mutableStateOf("") }
    var emailOrg by remember { mutableStateOf("") }
    var telephoneOrg by remember { mutableStateOf("") }
    var adresseOrg by remember { mutableStateOf("") }
    var adminNom by remember { mutableStateOf("") }
    var adminEmail by remember { mutableStateOf("") }
    var adminTelephone by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(GescotiPureWhite)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Créer une Nouvelle Organisation",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = GescotiMarianNavy
            )
            Text(
                text = "Renseignez les détails du nouveau tenant et de son administrateur principal",
                style = MaterialTheme.typography.bodySmall,
                color = GescotiTextSecondary
            )
        }

        if (errorMessage != null) {
            item {
                Surface(
                    color = GescotiRedContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = GescotiCrimsonRed,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Text(
                text = "1. Informations de l'Organisation",
                fontWeight = FontWeight.Bold,
                color = GescotiMarianNavy
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GescotiNavyContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(GescotiMarianNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Church,
                            contentDescription = "Logo de l'organisation",
                            tint = GescotiSunlitGold,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Logo de l'Organisation", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GescotiMarianNavy)
                        Text("Sceau ou emblème officiel de la structure", fontSize = 11.sp, color = GescotiTextSecondary)
                        Surface(
                            color = GescotiGoldContainer,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                "Format vectoriel & sceau actif",
                                fontSize = 10.sp,
                                color = Color(0xFFB78103),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = nomOrg,
                onValueChange = { nomOrg = it },
                label = { Text("Nom de l'organisation *") },
                placeholder = { Text("ex: Paroisse Saint Jean-Baptiste") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = sigle,
                    onValueChange = { sigle = it },
                    label = { Text("Sigle *") },
                    placeholder = { Text("ex: PSJB") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = paroisse,
                    onValueChange = { paroisse = it },
                    label = { Text("Paroisse / Église *") },
                    placeholder = { Text("ex: Cœur Immaculé de Marie") },
                    modifier = Modifier.weight(1.5f)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = emailOrg,
                    onValueChange = { emailOrg = it },
                    label = { Text("Email organisation *") },
                    placeholder = { Text("contact@paroisse.ci") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = telephoneOrg,
                    onValueChange = { telephoneOrg = it },
                    label = { Text("Téléphone") },
                    placeholder = { Text("+225 07 ...") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            OutlinedTextField(
                value = adresseOrg,
                onValueChange = { adresseOrg = it },
                label = { Text("Adresse / Ville") },
                placeholder = { Text("ex: Abidjan, Côte d'Ivoire") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "2. Administrateur Principal du Tenant",
                fontWeight = FontWeight.Bold,
                color = GescotiMarianNavy
            )
        }

        item {
            OutlinedTextField(
                value = adminNom,
                onValueChange = { adminNom = it },
                label = { Text("Nom complet de l'administrateur *") },
                placeholder = { Text("ex: Père André / Responsable Paul") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = adminEmail,
                    onValueChange = { adminEmail = it },
                    label = { Text("Email administrateur *") },
                    placeholder = { Text("admin@paroisse.ci") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = adminTelephone,
                    onValueChange = { adminTelephone = it },
                    label = { Text("Téléphone admin") },
                    placeholder = { Text("+225 05 ...") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Button(
                onClick = {
                    if (nomOrg.isBlank() || sigle.isBlank() || adminNom.isBlank() || adminEmail.isBlank()) {
                        errorMessage = "Veuillez renseigner tous les champs obligatoires (*)"
                        return@Button
                    }
                    errorMessage = null
                    viewModel.createTenantAndAdmin(
                        nomOrg = nomOrg.trim(),
                        sigle = sigle.trim().uppercase(),
                        paroisse = paroisse.trim(),
                        emailOrg = emailOrg.trim(),
                        telephoneOrg = telephoneOrg.trim(),
                        adresseOrg = adresseOrg.trim(),
                        adminNom = adminNom.trim(),
                        adminEmail = adminEmail.trim(),
                        adminTelephone = adminTelephone.trim()
                    )
                    onCreated()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GescotiMarianNavy)
            ) {
                Icon(Icons.Default.AddBusiness, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Créer l'Organisation et son Administrateur", fontWeight = FontWeight.Bold)
            }
        }
    }
}
