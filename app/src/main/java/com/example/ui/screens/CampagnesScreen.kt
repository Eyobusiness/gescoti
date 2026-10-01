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
import com.example.ui.theme.*
import com.example.utils.CurrencyFormatter
import com.example.utils.DateUtils
import kotlinx.coroutines.flow.firstOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampagnesScreen(
    viewModel: MainViewModel
) {
    val session by viewModel.session.collectAsState()
    val campagnes by viewModel.campagnesExceptionnelles.collectAsState()
    val membres by viewModel.membres.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedCampagneForParticipation by remember { mutableStateOf<CotisationExceptionnelleEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            if (session.isAdmin) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = OppeBluePrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nouvelle campagne")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Cotisations Exceptionnelles & Levées de Fonds",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OppeBluePrimary
            )
            Text(
                text = "Campagnes spéciales pour les récollections, sorties et projets pastoraux",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (campagnes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune cotisation exceptionnelle en cours.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(campagnes) { c ->
                        CampagneCard(
                            campagne = c,
                            viewModel = viewModel,
                            canParticipate = session.canRecordFinances(),
                            onParticipateClick = { selectedCampagneForParticipation = c }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateCampagneDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { titre, desc, obj, rec, dateFin ->
                viewModel.createCampagne(titre, desc, obj, rec, dateFin) {
                    showCreateDialog = false
                }
            }
        )
    }

    if (selectedCampagneForParticipation != null) {
        RecordParticipationDialog(
            campagne = selectedCampagneForParticipation!!,
            membres = membres,
            onDismiss = { selectedCampagneForParticipation = null },
            onConfirm = { mId, mVerse, mRemise, mode, obs ->
                viewModel.recordParticipation(
                    campagneId = selectedCampagneForParticipation!!.id,
                    campagneTitre = selectedCampagneForParticipation!!.titre,
                    membreId = mId,
                    montantVerse = mVerse,
                    montantRemise = mRemise,
                    modePaiement = mode,
                    observation = obs,
                    onSuccess = { selectedCampagneForParticipation = null }
                )
            }
        )
    }
}

@Composable
fun CampagneCard(
    campagne: CotisationExceptionnelleEntity,
    viewModel: MainViewModel,
    canParticipate: Boolean,
    onParticipateClick: () -> Unit
) {
    var participations by remember { mutableStateOf<List<ParticipationExceptionnelleEntity>>(emptyList()) }

    LaunchedEffect(campagne.id) {
        viewModel.db.cotisationExceptionnelleDao()
            .getParticipationsByCampagne(campagne.tenantId, campagne.id)
            .collect { list ->
                participations = list
            }
    }

    val totalCollecte = participations.sumOf { it.montantVerse }
    val nbParticipants = participations.map { it.membreId }.distinct().size
    val tauxRealisation = if (campagne.objectifFinancier > 0) {
        minOf(1f, (totalCollecte / campagne.objectifFinancier).toFloat())
    } else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = campagne.titre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OppeBluePrimary
                )
                Surface(
                    color = if (campagne.statut == CampaignStatus.EN_COURS) OppeGreenContainer else Color.LightGray,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (campagne.statut == CampaignStatus.EN_COURS) "En cours" else "Clôturée",
                        color = if (campagne.statut == CampaignStatus.EN_COURS) OppeGreenSuccess else Color.DarkGray,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = campagne.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Jauge d'avancement
            LinearProgressIndicator(
                progress = { tauxRealisation },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = OppeGreenSuccess,
                trackColor = Color(0xFFEEEEEE)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Collecté :", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        CurrencyFormatter.formatFcfa(totalCollecte),
                        fontWeight = FontWeight.Bold,
                        color = OppeGreenSuccess
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Objectif :", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(CurrencyFormatter.formatFcfa(campagne.objectifFinancier), fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Taux :", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("${(tauxRealisation * 100).toInt()}%", fontWeight = FontWeight.Bold, color = OppeBluePrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$nbParticipants participant(s) • Rec. ${CurrencyFormatter.formatFcfa(campagne.montantRecommande)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )

                if (canParticipate && campagne.statut == CampaignStatus.EN_COURS) {
                    Button(
                        onClick = onParticipateClick,
                        colors = ButtonDefaults.buttonColors(containerColor = OppeGreenSuccess),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Participer", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCampagneDialog(
    onDismiss: () -> Unit,
    onConfirm: (titre: String, desc: String, obj: Double, rec: Double, dateFin: Long) -> Unit
) {
    var titre by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var objStr by remember { mutableStateOf("") }
    var recStr by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle Cotisation Exceptionnelle", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (errorMsg != null) {
                    Text(errorMsg ?: "", color = Color.Red, fontSize = 13.sp)
                }
                OutlinedTextField(
                    value = titre,
                    onValueChange = { titre = it },
                    label = { Text("Titre de la campagne *") },
                    placeholder = { Text("ex: Fête Patronale OPPE 2026") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = objStr,
                    onValueChange = { objStr = it },
                    label = { Text("Objectif Financier (F) *") },
                    placeholder = { Text("ex: 500000") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = recStr,
                    onValueChange = { recStr = it },
                    label = { Text("Montant recommandé (F)") },
                    placeholder = { Text("ex: 5000") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val obj = objStr.toDoubleOrNull() ?: 0.0
                    val rec = recStr.toDoubleOrNull() ?: 0.0
                    if (titre.isBlank() || obj <= 0) {
                        errorMsg = "Veuillez indiquer un titre et un objectif valide"
                        return@Button
                    }
                    onConfirm(titre.trim(), desc.trim(), obj, rec, System.currentTimeMillis() + (60L * 24 * 3600 * 1000))
                },
                colors = ButtonDefaults.buttonColors(containerColor = OppeBluePrimary)
            ) {
                Text("Lancer la Campagne")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordParticipationDialog(
    campagne: CotisationExceptionnelleEntity,
    membres: List<MembreEntity>,
    onDismiss: () -> Unit,
    onConfirm: (membreId: String, montantVerse: Double, montantRemise: Double, mode: PaymentMode, observation: String) -> Unit
) {
    var selectedMembre by remember { mutableStateOf(membres.firstOrNull()) }
    var montantVerseStr by remember { mutableStateOf(campagne.montantRecommande.toInt().toString()) }
    var montantRemiseStr by remember { mutableStateOf("0") }
    var selectedMode by remember { mutableStateOf(PaymentMode.ESPECES) }
    var observation by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Participer à \"${campagne.titre}\"", fontWeight = FontWeight.Bold, color = OppeGreenSuccess) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (errorMsg != null) {
                    Text(errorMsg ?: "", color = Color.Red, fontSize = 13.sp)
                }

                Text("Sélectionner le Membre :", fontWeight = FontWeight.SemiBold)
                Box(modifier = Modifier.fillMaxWidth()) {
                    var expanded by remember { mutableStateOf(false) }
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedMembre != null) "${selectedMembre!!.nom} ${selectedMembre!!.prenoms}" else "Sélectionner...",
                                fontWeight = FontWeight.Medium
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        membres.forEach { m ->
                            DropdownMenuItem(
                                text = { Text("${m.nom} ${m.prenoms}") },
                                onClick = {
                                    selectedMembre = m
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = montantVerseStr,
                    onValueChange = { montantVerseStr = it },
                    label = { Text("Montant Versé (F) *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = montantRemiseStr,
                    onValueChange = { montantRemiseStr = it },
                    label = { Text("Remise / Subvention (F)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Mode de Paiement :", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(PaymentMode.ESPECES, PaymentMode.MOBILE_MONEY).forEach { m ->
                        FilterChip(
                            selected = selectedMode == m,
                            onClick = { selectedMode = m },
                            label = { Text(m.name, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = observation,
                    onValueChange = { observation = it },
                    label = { Text("Observation / Intention de prière") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val mVerse = montantVerseStr.toDoubleOrNull() ?: 0.0
                    val mRemise = montantRemiseStr.toDoubleOrNull() ?: 0.0
                    if (selectedMembre == null || (mVerse <= 0 && mRemise <= 0)) {
                        errorMsg = "Veuillez sélectionner un membre et indiquer un montant"
                        return@Button
                    }
                    onConfirm(selectedMembre!!.id, mVerse, mRemise, selectedMode, observation.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = OppeGreenSuccess)
            ) {
                Text("Enregistrer le Don")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
