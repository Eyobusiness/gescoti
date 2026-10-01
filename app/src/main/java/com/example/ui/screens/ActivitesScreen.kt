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
import com.example.data.model.ActivityStatus
import com.example.data.model.ActivityType
import com.example.data.model.ActiviteEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.OppeBluePrimary
import com.example.ui.theme.OppeGreenContainer
import com.example.ui.theme.OppeGreenSuccess
import com.example.utils.CurrencyFormatter
import com.example.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitesScreen(
    viewModel: MainViewModel
) {
    val session by viewModel.session.collectAsState()
    val activites by viewModel.activites.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (session.isAdmin || session.isResponsable) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = OppeBluePrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Planifier une activité")
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
                text = "Activités Pastorales & Rassemblements",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OppeBluePrimary
            )
            Text(
                text = "Planification des messes, récollections, sorties et formations",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (activites.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune activité planifiée pour cette année pastorale.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(activites) { act ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.5.dp)
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
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(OppeBluePrimary.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = when (act.typeActivite) {
                                                    ActivityType.MESSE -> Icons.Default.Church
                                                    ActivityType.RECOLLECTION -> Icons.Default.SelfImprovement
                                                    ActivityType.SORTIE -> Icons.Default.DirectionsBus
                                                    ActivityType.ACTIVITE_ENFANTS -> Icons.Default.ChildCare
                                                    else -> Icons.Default.Event
                                                },
                                                contentDescription = null,
                                                tint = OppeBluePrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = act.titre,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = act.typeActivite.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    Surface(
                                        color = when (act.statut) {
                                            ActivityStatus.PLANIFIEE -> Color(0xFFEDE7F6)
                                            ActivityStatus.EN_COURS -> OppeGreenContainer
                                            ActivityStatus.REALISEE -> Color(0xFFE0F2F1)
                                            ActivityStatus.ANNULEE -> Color(0xFFFFEBEE)
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = act.statut.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when (act.statut) {
                                                ActivityStatus.PLANIFIEE -> Color(0xFF5E35B1)
                                                ActivityStatus.EN_COURS -> OppeGreenSuccess
                                                ActivityStatus.REALISEE -> Color(0xFF00796B)
                                                ActivityStatus.ANNULEE -> Color.Red
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = act.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Date & Lieu :", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text(
                                            "${DateUtils.formatDate(act.dateDebut)} • ${act.lieu}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Budget Prévisionnel :", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text(
                                            CurrencyFormatter.formatFcfa(act.budgetPrevisionnel),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = OppeBluePrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Responsable : ${act.responsableNom}",
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

    if (showCreateDialog) {
        CreateActiviteDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { titre, type, desc, date, lieu, resp, budget ->
                viewModel.createActivite(titre, type, desc, date, lieu, resp, budget) {
                    showCreateDialog = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateActiviteDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        titre: String,
        type: ActivityType,
        desc: String,
        date: Long,
        lieu: String,
        responsable: String,
        budget: Double
    ) -> Unit
) {
    var titre by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ActivityType.MESSE) }
    var desc by remember { mutableStateOf("") }
    var lieu by remember { mutableStateOf("") }
    var responsable by remember { mutableStateOf("") }
    var budgetStr by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Planifier une Activité", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMsg != null) {
                    item { Text(errorMsg ?: "", color = Color.Red, fontSize = 13.sp) }
                }

                item {
                    OutlinedTextField(
                        value = titre,
                        onValueChange = { titre = it },
                        label = { Text("Titre de l'activité *") },
                        placeholder = { Text("ex: Messe d'action de grâce") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Type d'Activité :", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(ActivityType.MESSE, ActivityType.RECOLLECTION, ActivityType.SORTIE, ActivityType.ACTIVITE_ENFANTS).forEach { t ->
                            FilterChip(
                                selected = selectedType == t,
                                onClick = { selectedType = t },
                                label = { Text(t.name, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = lieu,
                        onValueChange = { lieu = it },
                        label = { Text("Lieu *") },
                        placeholder = { Text("ex: Salle paroissiale") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = responsable,
                        onValueChange = { responsable = it },
                        label = { Text("Responsable") },
                        placeholder = { Text("ex: Marie Claire") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = budgetStr,
                        onValueChange = { budgetStr = it },
                        label = { Text("Budget prévisionnel (F)") },
                        placeholder = { Text("ex: 25000") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val budget = budgetStr.toDoubleOrNull() ?: 0.0
                    if (titre.isBlank() || lieu.isBlank()) {
                        errorMsg = "Veuillez renseigner le titre et le lieu"
                        return@Button
                    }
                    onConfirm(
                        titre.trim(),
                        selectedType,
                        desc.trim(),
                        System.currentTimeMillis() + (7L * 24 * 3600 * 1000),
                        lieu.trim(),
                        responsable.trim(),
                        budget
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = OppeBluePrimary)
            ) {
                Text("Enregistrer l'Activité")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
