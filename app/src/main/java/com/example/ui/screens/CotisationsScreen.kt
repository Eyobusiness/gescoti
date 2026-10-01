package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.components.DuesStatusBadge
import com.example.ui.theme.*
import com.example.utils.CurrencyFormatter
import com.example.utils.DateUtils
import com.example.utils.PdfReceiptExporter
import kotlinx.coroutines.flow.firstOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CotisationsScreen(
    viewModel: MainViewModel,
    preselectedMembre: MembreEntity? = null,
    onClearPreselection: () -> Unit = {}
) {
    val context = LocalContext.current
    val session by viewModel.session.collectAsState()
    val paiements by viewModel.paiements.collectAsState()
    val membres by viewModel.membres.collectAsState()
    val profils by viewModel.profilsCotisation.collectAsState()
    val campagnes by viewModel.campagnesExceptionnelles.collectAsState()

    var selectedSubTab by remember { mutableIntStateOf(0) }
    // 0: Paiements & Reçus, 1: Cotisations Mensuelles & Échéances, 2: Retards & Impayés, 3: Cotisations Exceptionnelles

    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedMembreForPayment by remember { mutableStateOf<MembreEntity?>(preselectedMembre) }
    var lastRecordedPaiement by remember { mutableStateOf<PaiementEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var showCancelDialogForPaiement by remember { mutableStateOf<PaiementEntity?>(null) }
    var cancelReason by remember { mutableStateOf("") }

    val membreMap = remember(membres) { membres.associateBy { it.id } }
    val profilMap = remember(profils) { profils.associateBy { it.id } }

    LaunchedEffect(preselectedMembre) {
        if (preselectedMembre != null) {
            selectedMembreForPayment = preselectedMembre
            showPaymentDialog = true
            selectedSubTab = 0
        }
    }

    Scaffold(
        containerColor = GescotiPureWhite,
        floatingActionButton = {
            if (session.canRecordFinances() && selectedSubTab in listOf(0, 1)) {
                FloatingActionButton(
                    onClick = {
                        selectedMembreForPayment = membres.firstOrNull()
                        showPaymentDialog = true
                    },
                    containerColor = GescotiGreenSuccess,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.AddCard, contentDescription = "Nouveau paiement")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GescotiPureWhite)
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // SOUS-ONGLETS DU MODULE COTISATIONS
            ScrollableTabRow(
                selectedTabIndex = selectedSubTab,
                edgePadding = 0.dp,
                containerColor = Color.White,
                contentColor = GescotiMarianNavy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = { Text("Paiements & Reçus", fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = { Text("Mensuelles & Échéances", fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedSubTab == 2,
                    onClick = { selectedSubTab = 2 },
                    text = { Text("Retards & Impayés", fontWeight = if (selectedSubTab == 2) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedSubTab == 3,
                    onClick = { selectedSubTab = 3 },
                    text = { Text("Exceptionnelles", fontWeight = if (selectedSubTab == 3) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedSubTab) {
                0 -> {
                    // SOUS-MODULE 1 : PAIEMENTS & REÇUS (Journal, nouveau paiement, reçus PDF)
                    PaiementsListSection(
                        paiements = paiements,
                        membreMap = membreMap,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        canCancel = session.isAdmin,
                        onPrintReceipt = { p, m ->
                            PdfReceiptExporter.generateAndShareReceipt(
                                context = context,
                                tenantNom = session.currentTenant?.nom ?: "OPPE",
                                tenantParoisse = session.currentTenant?.paroisse ?: "",
                                paiement = p,
                                membreNom = "${m?.nom ?: ""} ${m?.prenoms ?: ""}",
                                membreMatricule = m?.matricule ?: ""
                            )
                        },
                        onCancelClick = { p ->
                            showCancelDialogForPaiement = p
                            cancelReason = ""
                        }
                    )
                }

                1 -> {
                    // SOUS-MODULE 2 : COTISATIONS MENSUELLES & ÉCHÉANCES
                    MensuellesSection(
                        membres = membres,
                        profilMap = profilMap,
                        viewModel = viewModel,
                        onEnregistrerPaiement = { m ->
                            selectedMembreForPayment = m
                            showPaymentDialog = true
                        }
                    )
                }

                2 -> {
                    // SOUS-MODULE 3 : RETARDS & IMPAYÉS
                    RetardsSection(
                        membres = membres,
                        profilMap = profilMap,
                        viewModel = viewModel,
                        onRelancerMembre = { m ->
                            selectedMembreForPayment = m
                            showPaymentDialog = true
                        }
                    )
                }

                3 -> {
                    // SOUS-MODULE 4 : COTISATIONS EXCEPTIONNELLES
                    CampagnesScreen(viewModel = viewModel)
                }
            }
        }
    }

    // DIALOGUE D'ENREGISTREMENT DE PAIEMENT
    if (showPaymentDialog) {
        RecordPaymentDialog(
            membres = membres,
            initialMembre = selectedMembreForPayment,
            viewModel = viewModel,
            onDismiss = {
                showPaymentDialog = false
                onClearPreselection()
            },
            onSuccess = { paiement ->
                showPaymentDialog = false
                onClearPreselection()
                lastRecordedPaiement = paiement
            }
        )
    }

    // DIALOGUE D'ANNULATION ADMINISTRATIVE
    if (showCancelDialogForPaiement != null) {
        AlertDialog(
            onDismissRequest = { showCancelDialogForPaiement = null },
            title = {
                Text("Annuler le Paiement ${showCancelDialogForPaiement?.numeroRecu}", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Cette opération comptable va annuler l'imputation sur les échéances et générer une contre-passation en caisse. Veuillez obligatoirement indiquer un motif d'audit :",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Motif de l'annulation *") },
                        placeholder = { Text("ex: Erreur de saisie / Chèque sans provision") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cancelReason.isNotBlank()) {
                            viewModel.cancelPayment(showCancelDialogForPaiement!!.id, cancelReason.trim())
                            showCancelDialogForPaiement = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GescotiCrimsonRed)
                ) {
                    Text("Confirmer l'Annulation")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialogForPaiement = null }) {
                    Text("Retour")
                }
            }
        )
    }

    // POPUP SUCCÈS APRÈS PAIEMENT : IMPRIMER LE REÇU OFFICIEL PDF
    if (lastRecordedPaiement != null) {
        val p = lastRecordedPaiement!!
        val membre = membreMap[p.membreId]
        AlertDialog(
            onDismissRequest = { lastRecordedPaiement = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GescotiGreenSuccess)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Paiement Enregistré !", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        "Le paiement ${p.numeroRecu} d'un montant de ${CurrencyFormatter.formatFcfa(p.montantVerse)}" +
                                if (p.montantRemise > 0) " avec une remise de ${CurrencyFormatter.formatFcfa(p.montantRemise)}" else "" +
                                        " a été enregistré en caisse avec succès.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Le reçu officiel est généré au format PDF certifié GESCOTI / OPPE.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        PdfReceiptExporter.generateAndShareReceipt(
                            context = context,
                            tenantNom = session.currentTenant?.nom ?: "OPPE",
                            tenantParoisse = session.currentTenant?.paroisse ?: "",
                            paiement = p,
                            membreNom = "${membre?.nom ?: ""} ${membre?.prenoms ?: ""}",
                            membreMatricule = membre?.matricule ?: ""
                        )
                        lastRecordedPaiement = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GescotiMarianNavy)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Imprimer / Partager le Reçu")
                }
            },
            dismissButton = {
                TextButton(onClick = { lastRecordedPaiement = null }) {
                    Text("Fermer")
                }
            }
        )
    }
}

@Composable
fun PaiementsListSection(
    paiements: List<PaiementEntity>,
    membreMap: Map<String, MembreEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    canCancel: Boolean,
    onPrintReceipt: (PaiementEntity, MembreEntity?) -> Unit,
    onCancelClick: (PaiementEntity) -> Unit
) {
    val filtered = remember(paiements, searchQuery) {
        if (searchQuery.isBlank()) paiements else {
            paiements.filter { p ->
                val m = membreMap[p.membreId]
                p.numeroRecu.contains(searchQuery, ignoreCase = true) ||
                        (m?.nom?.contains(searchQuery, ignoreCase = true) ?: false) ||
                        (m?.prenoms?.contains(searchQuery, ignoreCase = true) ?: false)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Rechercher par reçu ou nom de membre...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucun paiement trouvé.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { p ->
                    val membre = membreMap[p.membreId]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (p.valide) Color.White else Color(0xFFFFEBEE)
                        ),
                        border = CardDefaults.outlinedCardBorder(),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (p.valide) GescotiGreenContainer else Color.LightGray),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (p.valide) Icons.Default.Receipt else Icons.Default.Block,
                                            contentDescription = null,
                                            tint = if (p.valide) GescotiGreenSuccess else GescotiCrimsonRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = p.numeroRecu,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (p.valide) GescotiMarianNavy else GescotiCrimsonRed
                                        )
                                        Text(
                                            text = DateUtils.formatDateTime(p.datePaiement),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Surface(
                                    color = if (p.valide) GescotiGreenContainer else GescotiRedContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (p.valide) "Validé" else "Annulé",
                                        color = if (p.valide) GescotiGreenSuccess else GescotiCrimsonRed,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Membre : ${membre?.nom ?: "Inconnu"} ${membre?.prenoms ?: ""} (${membre?.matricule ?: ""})",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Montant Versé :", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(
                                        text = CurrencyFormatter.formatFcfa(p.montantVerse),
                                        fontWeight = FontWeight.Bold,
                                        color = GescotiGreenSuccess,
                                        fontSize = 15.sp
                                    )
                                }
                                if (p.montantRemise > 0.0) {
                                    Column {
                                        Text("Remise :", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text(
                                            text = CurrencyFormatter.formatFcfa(p.montantRemise),
                                            fontWeight = FontWeight.Bold,
                                            color = GescotiMagentaPink,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Total Imputé :", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(
                                        text = CurrencyFormatter.formatFcfa(p.montantTotalImpute),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = GescotiBorderLight)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mode: ${p.modePaiement.name} • Par: ${p.enregistreParNom}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )

                                Row {
                                    TextButton(onClick = { onPrintReceipt(p, membre) }) {
                                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reçu PDF", fontSize = 12.sp)
                                    }

                                    if (canCancel && p.valide) {
                                        TextButton(onClick = { onCancelClick(p) }) {
                                            Text("Annuler", color = GescotiCrimsonRed, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MensuellesSection(
    membres: List<MembreEntity>,
    profilMap: Map<String, ProfilCotisationEntity>,
    viewModel: MainViewModel,
    onEnregistrerPaiement: (MembreEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Situation Mensuelle des Membres",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = GescotiMarianNavy
            )
            Text(
                text = "Consultez l'état d'avancement des cotisations pour chaque membre",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(membres) { membre ->
            val profil = profilMap[membre.profilId]
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${membre.nom} ${membre.prenoms}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Matricule: ${membre.matricule} • ${profil?.nom ?: "Adulte"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = "Cotisation : ${CurrencyFormatter.formatFcfa(profil?.montantMensuel ?: 500.0)} / mois",
                            style = MaterialTheme.typography.labelSmall,
                            color = GescotiMarianNavy,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { onEnregistrerPaiement(membre) },
                        colors = ButtonDefaults.buttonColors(containerColor = GescotiGreenSuccess),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Payer", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RetardsSection(
    membres: List<MembreEntity>,
    profilMap: Map<String, ProfilCotisationEntity>,
    viewModel: MainViewModel,
    onRelancerMembre: (MembreEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GescotiRedContainer)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = GescotiCrimsonRed,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Suivi des Retards & Échéances Échues",
                            fontWeight = FontWeight.Bold,
                            color = GescotiCrimsonRed,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Membres ayant des échéances mensuelles passées non soldées",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7F0000)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(membres) { membre ->
            val profil = profilMap[membre.profilId]
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${membre.nom} ${membre.prenoms}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Tél: ${membre.telephone} • WhatsApp: ${membre.whatsapp}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Surface(
                            color = GescotiOrangeContainer,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "Échéances en attente de régularisation",
                                color = Color(0xFFE65100),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Button(
                        onClick = { onRelancerMembre(membre) },
                        colors = ButtonDefaults.buttonColors(containerColor = GescotiSunlitGold),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Encaisser", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentDialog(
    membres: List<MembreEntity>,
    initialMembre: MembreEntity?,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onSuccess: (PaiementEntity) -> Unit
) {
    var selectedMembre by remember { mutableStateOf<MembreEntity?>(initialMembre) }
    var searchMembreText by remember { mutableStateOf("") }
    var isSelectingMembre by remember { mutableStateOf(initialMembre == null) }

    var montantVerseStr by remember { mutableStateOf("500") }
    var montantRemiseStr by remember { mutableStateOf("0") }
    var selectedMode by remember { mutableStateOf(PaymentMode.ESPECES) }
    var referenceExterne by remember { mutableStateOf("") }
    var observation by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    val profils by viewModel.profilsCotisation.collectAsState()
    val profilMap = remember(profils) { profils.associateBy { it.id } }

    val activeProfil = remember(selectedMembre, profilMap) {
        selectedMembre?.let { profilMap[it.profilId] }
    }
    val defaultTarifMensuel = activeProfil?.montantMensuel ?: 500.0

    val filteredMembres = remember(membres, searchMembreText) {
        if (searchMembreText.isBlank()) membres.take(8)
        else membres.filter {
            it.nom.contains(searchMembreText, ignoreCase = true) ||
                    it.prenoms.contains(searchMembreText, ignoreCase = true) ||
                    it.matricule.contains(searchMembreText, ignoreCase = true) ||
                    it.telephone.contains(searchMembreText)
        }.take(8)
    }

    val montantVerse = montantVerseStr.toDoubleOrNull() ?: 0.0
    val montantRemise = montantRemiseStr.toDoubleOrNull() ?: 0.0
    val montantImputeTotal = montantVerse + montantRemise

    val echeancesCouvertesEstimees = if (defaultTarifMensuel > 0) {
        (montantImputeTotal / defaultTarifMensuel).toInt()
    } else 0

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GescotiMarianNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = GescotiSunlitGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Nouveau Paiement", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Cotisation pastorale", fontSize = 12.sp, color = Color.Gray)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (formError != null) {
                    Surface(
                        color = GescotiRedContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = formError ?: "",
                            color = GescotiCrimsonRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // SÉLECTION DU MEMBRE
                if (selectedMembre == null || isSelectingMembre) {
                    Text("1. Rechercher et sélectionner le membre *", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = searchMembreText,
                        onValueChange = { searchMembreText = it },
                        placeholder = { Text("Nom, matricule, téléphone...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GescotiBackgroundLight, RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        filteredMembres.forEach { m ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        selectedMembre = m
                                        isSelectingMembre = false
                                        val p = profilMap[m.profilId]
                                        val tarif = p?.montantMensuel ?: 500.0
                                        montantVerseStr = tarif.toInt().toString()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${m.nom} ${m.prenoms}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${m.matricule} • ${m.telephone}", fontSize = 11.sp, color = Color.Gray)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GescotiMarianNavy)
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = GescotiNavyContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${selectedMembre!!.nom} ${selectedMembre!!.prenoms}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = GescotiMarianNavy
                                )
                                Text(
                                    text = "Matricule: ${selectedMembre!!.matricule}",
                                    fontSize = 12.sp,
                                    color = GescotiTextSecondary
                                )
                                Text(
                                    text = "Profil: ${activeProfil?.nom ?: "Standard"} (${CurrencyFormatter.formatFcfa(defaultTarifMensuel)} / mois)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GescotiMarianNavy
                                )
                            }
                            TextButton(onClick = { isSelectingMembre = true }) {
                                Text("Changer", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // MONTANT VERSÉ ET REMISE
                Text("2. Montants de l'opération *", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                // Raccourcis de cotisation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val m1 = defaultTarifMensuel.toInt()
                    val m3 = (defaultTarifMensuel * 3).toInt()
                    val m6 = (defaultTarifMensuel * 6).toInt()
                    val m12 = (defaultTarifMensuel * 12).toInt()

                    listOf(
                        "1M ($m1)" to m1.toString(),
                        "3M ($m3)" to m3.toString(),
                        "6M ($m6)" to m6.toString(),
                        "1 An ($m12)" to m12.toString()
                    ).forEach { (label, value) ->
                        OutlinedButton(
                            onClick = {
                                montantVerseStr = value
                                montantRemiseStr = "0"
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = montantVerseStr,
                        onValueChange = { montantVerseStr = it },
                        label = { Text("Montant versé (F) *", fontSize = 12.sp) },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = montantRemiseStr,
                        onValueChange = { montantRemiseStr = it },
                        label = { Text("Remise (F)", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // RÉCAPITULATIF DU TOTAL IMPUTÉ ET ÉCHÉANCES
                Surface(
                    color = GescotiGoldContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total imputé sur les cotisations :", fontSize = 12.sp, color = Color.DarkGray)
                            Text(
                                CurrencyFormatter.formatFcfa(montantImputeTotal),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = GescotiMarianNavy
                            )
                        }
                        if (echeancesCouvertesEstimees > 0) {
                            Text(
                                "≈ Couvre environ $echeancesCouvertesEstimees échéance(s) mensuelle(s)",
                                fontSize = 11.sp,
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // MODE DE PAIEMENT
                Text("3. Mode de paiement *", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        PaymentMode.ESPECES to "Espèces",
                        PaymentMode.MOBILE_MONEY to "Mobile Money",
                        PaymentMode.VIREMENT to "Virement",
                        PaymentMode.CHEQUE to "Chèque"
                    ).forEach { (mode, label) ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                if (selectedMode != PaymentMode.ESPECES) {
                    OutlinedTextField(
                        value = referenceExterne,
                        onValueChange = { referenceExterne = it },
                        label = { Text("Référence (N° Wave, Chèque, etc.)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // OBSERVATION
                OutlinedTextField(
                    value = observation,
                    onValueChange = { observation = it },
                    label = { Text("Observation / Note (facultatif)", fontSize = 12.sp) },
                    placeholder = { Text("ex: Versement en espèces au bureau", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedMembre == null) {
                        formError = "Veuillez d'abord sélectionner un membre."
                        return@Button
                    }
                    if (montantVerse <= 0.0 && montantRemise <= 0.0) {
                        formError = "Le montant versé ou la remise doit être supérieur à 0."
                        return@Button
                    }
                    formError = null
                    isSubmitting = true
                    viewModel.recordPayment(
                        membreId = selectedMembre!!.id,
                        montantVerse = montantVerse,
                        montantRemise = montantRemise,
                        modePaiement = selectedMode,
                        referenceExterne = referenceExterne.trim(),
                        observation = observation.trim(),
                        onSuccess = { paiement ->
                            isSubmitting = false
                            onSuccess(paiement)
                        }
                    )
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = GescotiGreenSuccess),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Valider & Émettre le Reçu", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("Annuler")
            }
        }
    )
}
