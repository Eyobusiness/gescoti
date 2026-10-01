package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.model.AuditLogEntity
import com.example.data.model.ProfilCotisationEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.OppeBluePrimary
import com.example.utils.CurrencyFormatter
import com.example.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParametresScreen(
    viewModel: MainViewModel
) {
    val session by viewModel.session.collectAsState()
    val profils by viewModel.profilsCotisation.collectAsState()
    val annees by viewModel.anneesPastorales.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    var editingProfil by remember { mutableStateOf<ProfilCotisationEntity?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Configuration, 1: Journal d'Audit

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Paramètres & Traçabilité SaaS",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = OppeBluePrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Paramètres Métier") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Journal d'Audit (${auditLogs.size})") }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Année Pastorale Active
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Année Pastorale Active",
                                fontWeight = FontWeight.Bold,
                                color = OppeBluePrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Actuelle : ${session.activePastoralYearLibelle}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Toutes les opérations de cotisation, caisse et dépenses sont strictement rattachées à cette année pastorale.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Profils Tarifaires de Cotisation (Modifiables par l'Admin)
                item {
                    Text(
                        text = "Profils de Cotisation Configurables",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OppeBluePrimary
                    )
                }

                items(profils) { profil ->
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = profil.nom, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    text = profil.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${CurrencyFormatter.formatFcfa(profil.montantMensuel)} / mois • ${CurrencyFormatter.formatFcfa(profil.montantAnnuel)} / an",
                                    fontWeight = FontWeight.SemiBold,
                                    color = OppeBluePrimary,
                                    fontSize = 13.sp
                                )
                            }

                            if (session.isAdmin) {
                                IconButton(onClick = { editingProfil = profil }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Modifier tarif", tint = OppeBluePrimary)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // JOURNAL D'AUDIT & TRAÇABILITÉ (Section 24)
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (auditLogs.isEmpty()) {
                    item {
                        Text(
                            text = "Aucune action enregistrée dans le journal d'audit.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                } else {
                    items(auditLogs) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${log.action.name} • ${log.module}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = OppeBluePrimary
                                    )
                                    Text(
                                        text = DateUtils.formatDateTime(log.dateHeure),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = log.details,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Par : ${log.userNom}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingProfil != null) {
        EditProfilDialog(
            profil = editingProfil!!,
            onDismiss = { editingProfil = null },
            onSave = { pId, m, a ->
                viewModel.updateProfilTarif(pId, m, a)
                editingProfil = null
            }
        )
    }
}

@Composable
fun EditProfilDialog(
    profil: ProfilCotisationEntity,
    onDismiss: () -> Unit,
    onSave: (profilId: String, montantMensuel: Double, montantAnnuel: Double) -> Unit
) {
    var mensuelStr by remember { mutableStateOf(profil.montantMensuel.toInt().toString()) }
    var annuelStr by remember { mutableStateOf(profil.montantAnnuel.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier Tarif : ${profil.nom}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = mensuelStr,
                    onValueChange = { mensuelStr = it },
                    label = { Text("Montant Mensuel (F) *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = annuelStr,
                    onValueChange = { annuelStr = it },
                    label = { Text("Montant Annuel (F) *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = mensuelStr.toDoubleOrNull() ?: profil.montantMensuel
                    val a = annuelStr.toDoubleOrNull() ?: (m * 12)
                    onSave(profil.id, m, a)
                },
                colors = ButtonDefaults.buttonColors(containerColor = OppeBluePrimary)
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
