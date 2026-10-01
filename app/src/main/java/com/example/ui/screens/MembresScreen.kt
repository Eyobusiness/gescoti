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
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.components.DuesStatusBadge
import com.example.ui.components.MemberStatusBadge
import com.example.ui.theme.*
import com.example.utils.CurrencyFormatter
import com.example.utils.DateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembresScreen(
    viewModel: MainViewModel,
    onEnregistrerPaiementPourMembre: (MembreEntity) -> Unit
) {
    val session by viewModel.session.collectAsState()
    val membres by viewModel.membres.collectAsState()
    val profils by viewModel.profilsCotisation.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<MemberStatus?>(null) }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMembreForDetail by remember { mutableStateOf<MembreEntity?>(null) }

    val filteredMembres = remember(membres, searchQuery, selectedStatusFilter) {
        membres.filter { m ->
            val matchSearch = searchQuery.isBlank() ||
                    m.nom.contains(searchQuery, ignoreCase = true) ||
                    m.prenoms.contains(searchQuery, ignoreCase = true) ||
                    m.matricule.contains(searchQuery, ignoreCase = true)
            val matchStatus = selectedStatusFilter == null || m.statut == selectedStatusFilter
            matchSearch && matchStatus
        }
    }

    val profilMap = remember(profils) { profils.associateBy { it.id } }

    Scaffold(
        floatingActionButton = {
            if (session.isAdmin || session.isTresorier) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = OppeBluePrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Ajouter un membre")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Barre de recherche
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Rechercher par nom, prénom ou matricule...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filtres rapides de statuts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatusFilter == null,
                    onClick = { selectedStatusFilter = null },
                    label = { Text("Tous (${membres.size})") }
                )
                FilterChip(
                    selected = selectedStatusFilter == MemberStatus.ACTIF,
                    onClick = { selectedStatusFilter = MemberStatus.ACTIF },
                    label = { Text("Actifs") }
                )
                FilterChip(
                    selected = selectedStatusFilter == MemberStatus.INACTIF,
                    onClick = { selectedStatusFilter = MemberStatus.INACTIF },
                    label = { Text("Inactifs") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredMembres.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PersonOff,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "Aucun membre enregistré" else "Aucun membre ne correspond à la recherche",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMembres) { membre ->
                        val profil = profilMap[membre.profilId]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMembreForDetail = membre },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.5.dp)
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
                                        .clip(CircleShape)
                                        .background(OppeBluePrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${membre.nom.firstOrNull() ?: '?'}${membre.prenoms.firstOrNull() ?: ""}",
                                        fontWeight = FontWeight.Bold,
                                        color = OppeBluePrimary,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${membre.nom} ${membre.prenoms}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        MemberStatusBadge(membre.statut)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Matricule: ${membre.matricule} • ${profil?.nom ?: "Profil"} (${CurrencyFormatter.formatFcfa(profil?.montantMensuel ?: 0.0)}/mois)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    if (membre.telephone.isNotEmpty()) {
                                        Text(
                                            text = "Tél: ${membre.telephone}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.DarkGray
                                        )
                                    }
                                }

                                if (session.canRecordFinances()) {
                                    IconButton(
                                        onClick = { onEnregistrerPaiementPourMembre(membre) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddCard,
                                            contentDescription = "Paiement cotisation",
                                            tint = OppeGreenSuccess
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogue Détails Membre & Échéancier 12 mois
    if (selectedMembreForDetail != null) {
        MembreDetailDialog(
            membre = selectedMembreForDetail!!,
            profil = profilMap[selectedMembreForDetail!!.profilId],
            viewModel = viewModel,
            onDismiss = { selectedMembreForDetail = null },
            onPaiementClick = {
                val m = selectedMembreForDetail!!
                selectedMembreForDetail = null
                onEnregistrerPaiementPourMembre(m)
            }
        )
    }

    // Dialogue Ajout Membre
    if (showAddDialog) {
        AddMembreDialog(
            profils = profils,
            onDismiss = { showAddDialog = false },
            onConfirm = { nom, prenoms, sexe, tel, whatsapp, email, adr, type, profilId ->
                viewModel.createMember(
                    nom = nom,
                    prenoms = prenoms,
                    sexe = sexe,
                    telephone = tel,
                    whatsapp = whatsapp,
                    email = email,
                    adresse = adr,
                    typeMembre = type,
                    profilId = profilId,
                    onSuccess = { showAddDialog = false }
                )
            }
        )
    }
}

@Composable
fun MembreDetailDialog(
    membre: MembreEntity,
    profil: ProfilCotisationEntity?,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onPaiementClick: () -> Unit
) {
    val session by viewModel.session.collectAsState()
    val scope = rememberCoroutineScope()
    var echeances by remember { mutableStateOf<List<EcheanceCotisationEntity>>(emptyList()) }

    LaunchedEffect(membre.id) {
        val tid = membre.tenantId
        val aid = membre.anneePastoraleId
        echeances = viewModel.db.echeanceDao().getEcheancesByMembre(tid, membre.id, aid).sortedBy { it.moisIndex }
    }

    val totalDu = echeances.sumOf { it.montantDu }
    val totalPaye = echeances.sumOf { it.montantPaye }
    val totalRemis = echeances.sumOf { it.montantRemis }
    val soldeRestant = echeances.sumOf { it.solde }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = "${membre.nom} ${membre.prenoms}", fontWeight = FontWeight.Bold)
                Text(
                    text = "Matricule : ${membre.matricule} • Profil : ${profil?.nom ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
            ) {
                // Synthèse Financière du Membre
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Annuel Dû :", style = MaterialTheme.typography.bodySmall)
                            Text(CurrencyFormatter.formatFcfa(totalDu), fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Payé :", style = MaterialTheme.typography.bodySmall, color = OppeGreenSuccess)
                            Text(CurrencyFormatter.formatFcfa(totalPaye), fontWeight = FontWeight.Bold, color = OppeGreenSuccess)
                        }
                        if (totalRemis > 0.0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Remises :", style = MaterialTheme.typography.bodySmall, color = OppePurpleRemise)
                                Text(CurrencyFormatter.formatFcfa(totalRemis), fontWeight = FontWeight.Bold, color = OppePurpleRemise)
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Reste à Payer :", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(
                                CurrencyFormatter.formatFcfa(soldeRestant),
                                fontWeight = FontWeight.ExtraBold,
                                color = if (soldeRestant > 0) OppeRedDebt else OppeGreenSuccess
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Échéancier des 12 Mois (Année Pastorale)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = OppeBluePrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(echeances) { ech ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFAFAFA),
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = ech.libelleMois,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Dû: ${CurrencyFormatter.formatFcfa(ech.montantDu)} • Payé: ${CurrencyFormatter.formatFcfa(ech.montantPaye)}" +
                                                if (ech.montantRemis > 0) " (Remise: ${CurrencyFormatter.formatFcfa(ech.montantRemis)})" else "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.DarkGray
                                    )
                                }
                                DuesStatusBadge(ech.statut)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (session.canRecordFinances() && soldeRestant > 0) {
                Button(
                    onClick = onPaiementClick,
                    colors = ButtonDefaults.buttonColors(containerColor = OppeGreenSuccess)
                ) {
                    Icon(Icons.Default.AddCard, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Encaisser une Cotisation")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMembreDialog(
    profils: List<ProfilCotisationEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        nom: String,
        prenoms: String,
        sexe: String,
        telephone: String,
        whatsapp: String,
        email: String,
        adresse: String,
        typeMembre: MemberType,
        profilId: String
    ) -> Unit
) {
    var nom by remember { mutableStateOf("") }
    var prenoms by remember { mutableStateOf("") }
    var sexe by remember { mutableStateOf("M") }
    var telephone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var adresse by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(MemberType.ADULTE) }
    var selectedProfilId by remember { mutableStateOf(profils.firstOrNull()?.id ?: "") }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Nouveau Membre OPPE", fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMsg != null) {
                    item {
                        Text(text = errorMsg ?: "", color = Color.Red, fontSize = 13.sp)
                    }
                }

                item {
                    OutlinedTextField(
                        value = nom,
                        onValueChange = { nom = it },
                        label = { Text("Nom de famille *") },
                        placeholder = { Text("ex: KOUASSI") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = prenoms,
                        onValueChange = { prenoms = it },
                        label = { Text("Prénoms *") },
                        placeholder = { Text("ex: Jean-Marc") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sexe : ", fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = sexe == "M", onClick = { sexe = "M" })
                            Text("Homme (M)")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = sexe == "F", onClick = { sexe = "F" })
                            Text("Femme (F)")
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = telephone,
                        onValueChange = { telephone = it },
                        label = { Text("Téléphone") },
                        placeholder = { Text("+225 07 ...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = whatsapp,
                        onValueChange = { whatsapp = it },
                        label = { Text("WhatsApp (si différent)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = adresse,
                        onValueChange = { adresse = it },
                        label = { Text("Adresse / Quartier") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Profil Tarifaire de Cotisation :", fontWeight = FontWeight.Bold, color = OppeBluePrimary)
                    Column {
                        profils.forEach { profil ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedProfilId = profil.id }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedProfilId == profil.id,
                                    onClick = { selectedProfilId = profil.id }
                                )
                                Column {
                                    Text(text = profil.nom, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "${CurrencyFormatter.formatFcfa(profil.montantMensuel)} / mois (${CurrencyFormatter.formatFcfa(profil.montantAnnuel)} / an)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nom.isBlank() || prenoms.isBlank()) {
                        errorMsg = "Le nom et les prénoms sont obligatoires"
                        return@Button
                    }
                    onConfirm(
                        nom.trim(),
                        prenoms.trim(),
                        sexe,
                        telephone.trim(),
                        whatsapp.trim(),
                        email.trim(),
                        adresse.trim(),
                        selectedType,
                        selectedProfilId
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = OppeBluePrimary)
            ) {
                Text("Enregistrer le Membre")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
