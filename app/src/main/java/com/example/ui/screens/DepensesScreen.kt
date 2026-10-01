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
import com.example.data.model.ActiviteEntity
import com.example.data.model.CategorieDepenseEntity
import com.example.data.model.DepenseEntity
import com.example.data.model.PaymentMode
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import com.example.utils.CurrencyFormatter
import com.example.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepensesScreen(
    viewModel: MainViewModel
) {
    val session by viewModel.session.collectAsState()
    val depenses by viewModel.depenses.collectAsState()
    val categories by viewModel.categoriesDepenses.collectAsState()
    val activites by viewModel.activites.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    val catMap = remember(categories) { categories.associateBy { it.id } }
    val totalDepenses = depenses.filter { it.valide }.sumOf { it.montant }

    Scaffold(
        floatingActionButton = {
            if (session.canRecordFinances()) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = OppeRedDebt,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Enregistrer une dépense")
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
            // Synthèse des Dépenses
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = OppeRedContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL DÉPENSES ENGAGÉES",
                            style = MaterialTheme.typography.labelSmall,
                            color = OppeRedDebt,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.formatFcfa(totalDepenses),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = OppeRedDebt
                        )
                    }
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${depenses.size} dépense(s)",
                            color = OppeRedDebt,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Historique des Dépenses & Sorties de Caisse",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OppeBluePrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (depenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune dépense enregistrée.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(depenses) { dep ->
                        val cat = catMap[dep.categorieId]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                        .background(OppeRedContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = OppeRedDebt,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dep.libelle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${cat?.libelle ?: "Général"} • ${DateUtils.formatDateTime(dep.dateDepense)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                    if (dep.justificatifRef.isNotEmpty()) {
                                        Text(
                                            text = "Réf. Justificatif: ${dep.justificatifRef}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.DarkGray
                                        )
                                    }
                                    Text(
                                        text = "Payé en ${dep.modePaiement.name} • Par: ${dep.enregistreParNom}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                                Text(
                                    text = "-${CurrencyFormatter.formatFcfa(dep.montant)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = OppeRedDebt,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOGUE AJOUT DÉPENSE
    if (showAddDialog) {
        AddDepenseDialog(
            categories = categories,
            activites = activites,
            onDismiss = { showAddDialog = false },
            onConfirm = { catId, catNom, actId, libelle, montant, mode, justif, obs ->
                viewModel.recordExpense(
                    categorieId = catId,
                    categorieNom = catNom,
                    activiteId = actId,
                    libelle = libelle,
                    montant = montant,
                    modePaiement = mode,
                    justificatifRef = justif,
                    observation = obs,
                    onSuccess = { showAddDialog = false }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDepenseDialog(
    categories: List<CategorieDepenseEntity>,
    activites: List<ActiviteEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        categorieId: String,
        categorieNom: String,
        activiteId: String?,
        libelle: String,
        montant: Double,
        mode: PaymentMode,
        justificatifRef: String,
        observation: String
    ) -> Unit
) {
    var selectedCat by remember { mutableStateOf(categories.firstOrNull()) }
    var libelle by remember { mutableStateOf("") }
    var montantStr by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf(PaymentMode.ESPECES) }
    var justificatifRef by remember { mutableStateOf("") }
    var observation by remember { mutableStateOf("") }
    var selectedActiviteId by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Enregistrer une Dépense", fontWeight = FontWeight.Bold, color = OppeRedDebt)
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMsg != null) {
                    item {
                        Text(text = errorMsg ?: "", color = Color.Red, fontSize = 13.sp)
                    }
                }

                item {
                    Text("Catégorie de Dépense * :", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        var expanded by remember { mutableStateOf(false) }
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedCat?.libelle ?: "Choisir une catégorie...",
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
                            categories.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.libelle) },
                                    onClick = {
                                        selectedCat = c
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = libelle,
                        onValueChange = { libelle = it },
                        label = { Text("Libellé de la dépense *") },
                        placeholder = { Text("ex: Achat fournitures pour enfants") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = montantStr,
                        onValueChange = { montantStr = it },
                        label = { Text("Montant (F) *") },
                        placeholder = { Text("ex: 5000") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Text("Mode de Paiement :", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(PaymentMode.ESPECES, PaymentMode.MOBILE_MONEY, PaymentMode.VIREMENT).forEach { mode ->
                            FilterChip(
                                selected = selectedMode == mode,
                                onClick = { selectedMode = mode },
                                label = { Text(mode.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = justificatifRef,
                        onValueChange = { justificatifRef = it },
                        label = { Text("N° Justificatif / Facture") },
                        placeholder = { Text("ex: FACT-2026-081") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = observation,
                        onValueChange = { observation = it },
                        label = { Text("Observation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val montant = montantStr.toDoubleOrNull() ?: 0.0
                    if (selectedCat == null || libelle.isBlank() || montant <= 0) {
                        errorMsg = "Veuillez renseigner la catégorie, le libellé et un montant valide"
                        return@Button
                    }
                    onConfirm(
                        selectedCat!!.id,
                        selectedCat!!.libelle,
                        selectedActiviteId,
                        libelle.trim(),
                        montant,
                        selectedMode,
                        justificatifRef.trim(),
                        observation.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = OppeRedDebt)
            ) {
                Text("Valider la Dépense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
