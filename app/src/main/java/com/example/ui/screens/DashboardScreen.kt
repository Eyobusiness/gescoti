package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityStatus
import com.example.data.model.CampaignStatus
import com.example.data.model.CashMovementType
import com.example.ui.MainViewModel
import com.example.ui.components.StatKpiCard
import com.example.ui.theme.*
import com.example.utils.CurrencyFormatter
import com.example.utils.DateUtils

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToCotisations: () -> Unit,
    onNavigateToCaisse: () -> Unit,
    onNavigateToMembres: () -> Unit,
    onNavigateToDepenses: () -> Unit
) {
    val session by viewModel.session.collectAsState()
    val soldeCaisse by viewModel.soldeCaisse.collectAsState()
    val paiements by viewModel.paiements.collectAsState()
    val depenses by viewModel.depenses.collectAsState()
    val membres by viewModel.membres.collectAsState()
    val activites by viewModel.activites.collectAsState()
    val campagnes by viewModel.campagnesExceptionnelles.collectAsState()
    val mouvements by viewModel.mouvementsCaisse.collectAsState()

    // Calculs financiers réactifs
    val totalCotisationsVersees = paiements.filter { it.valide }.sumOf { it.montantVerse }
    val totalRemises = paiements.filter { it.valide }.sumOf { it.montantRemise }
    val totalCotisationsImputees = paiements.filter { it.valide }.sumOf { it.montantTotalImpute }
    val totalDepensesValidees = depenses.filter { it.valide }.sumOf { it.montant }

    val totalEntreesCaisse = mouvements.filter { it.type == CashMovementType.ENTREE }.sumOf { it.montant }
    val totalSortiesCaisse = mouvements.filter { it.type == CashMovementType.SORTIE }.sumOf { it.montant }

    val activeCampagne = campagnes.firstOrNull { it.statut == CampaignStatus.EN_COURS }
    val prochainesActivites = activites.filter { it.statut == ActivityStatus.PLANIFIEE }.take(2)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // En-tête Organisation & Année Pastorale
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = OppeBluePrimary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Church,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = session.currentTenant?.nom ?: "Organisation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${session.currentTenant?.paroisse ?: ""} • Année ${session.activePastoralYearLibelle}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // CARTE PRINCIPALE : Solde de Caisse (Calculé automatiquement = Entrées - Sorties)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SOLDE DISPONIBLE EN CAISSE",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                        Surface(
                            color = OppeGreenContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Temps Réel",
                                color = OppeGreenSuccess,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = CurrencyFormatter.formatFcfa(soldeCaisse),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (soldeCaisse >= 0) OppeBluePrimary else OppeRedDebt
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Entrées", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                text = "+${CurrencyFormatter.formatFcfa(totalEntreesCaisse)}",
                                color = OppeGreenSuccess,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Dépenses", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                text = "-${CurrencyFormatter.formatFcfa(totalSortiesCaisse)}",
                                color = OppeRedDebt,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // ACTIONS RAPIDES (Grands Boutons Adaptés aux Responsables)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNavigateToCotisations,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OppeBluePrimary)
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cotisations", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onNavigateToDepenses,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Dépense", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // CARTES STATISTIQUES FINANCIÈRES
        item {
            Text(
                text = "Indicateurs Financiers de l'Année",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OppeBluePrimary
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatKpiCard(
                    title = "Cotisations Encaissées",
                    value = CurrencyFormatter.formatFcfa(totalCotisationsVersees),
                    subtitle = "${paiements.filter { it.valide }.size} paiements",
                    icon = Icons.Default.Savings,
                    containerColor = OppeGreenContainer,
                    contentColor = OppeGreenSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatKpiCard(
                    title = "Remises Accordées",
                    value = CurrencyFormatter.formatFcfa(totalRemises),
                    subtitle = "Tracabilité comptable",
                    icon = Icons.Default.Discount,
                    containerColor = OppePurpleContainer,
                    contentColor = OppePurpleRemise,
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
                    title = "Dette Épurée",
                    value = CurrencyFormatter.formatFcfa(totalCotisationsImputees),
                    subtitle = "Versé + Remises",
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
                StatKpiCard(
                    title = "Membres Enregistrés",
                    value = "${membres.size}",
                    subtitle = "${membres.count { it.statut == com.example.data.model.MemberStatus.ACTIF }} actifs",
                    icon = Icons.Default.People,
                    containerColor = Color(0xFFFFF3CD),
                    contentColor = Color(0xFF856404),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // GRAPHIQUE COMPOSE : Répartition Entrées / Sorties / Remises
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Équilibre Financier Global",
                        fontWeight = FontWeight.Bold,
                        color = OppeBluePrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val totalVolume = totalEntreesCaisse + totalSortiesCaisse + totalRemises
                    if (totalVolume > 0) {
                        val entreeRatio = (totalEntreesCaisse / totalVolume).toFloat()
                        val sortieRatio = (totalSortiesCaisse / totalVolume).toFloat()
                        val remiseRatio = (totalRemises / totalVolume).toFloat()

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                                .clip(RoundedCornerShape(11.dp))
                        ) {
                            val w = size.width
                            var currentX = 0f

                            // Barre Entrées
                            val wEntree = w * entreeRatio
                            drawRect(
                                color = OppeGreenSuccess,
                                topLeft = Offset(currentX, 0f),
                                size = Size(wEntree, size.height)
                            )
                            currentX += wEntree

                            // Barre Sorties
                            val wSortie = w * sortieRatio
                            drawRect(
                                color = OppeRedDebt,
                                topLeft = Offset(currentX, 0f),
                                size = Size(wSortie, size.height)
                            )
                            currentX += wSortie

                            // Barre Remises
                            val wRemise = w * remiseRatio
                            drawRect(
                                color = OppePurpleRemise,
                                topLeft = Offset(currentX, 0f),
                                size = Size(wRemise, size.height)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            LegendItem(color = OppeGreenSuccess, label = "Entrées (${(entreeRatio * 100).toInt()}%)")
                            LegendItem(color = OppeRedDebt, label = "Dépenses (${(sortieRatio * 100).toInt()}%)")
                            LegendItem(color = OppePurpleRemise, label = "Remises (${(remiseRatio * 100).toInt()}%)")
                        }
                    } else {
                        Text(
                            text = "Aucun mouvement financier enregistré pour le moment.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // CAMPAGNE EXCEPTIONNELLE ACTIVE
        if (activeCampagne != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Campagne Exceptionnelle",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF57F17)
                            )
                            Surface(
                                color = Color(0xFFFFECB3),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "En cours",
                                    color = Color(0xFFE65100),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = activeCampagne.titre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = activeCampagne.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Objectif : ${CurrencyFormatter.formatFcfa(activeCampagne.objectifFinancier)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // PROCHAINES ACTIVITÉS PASTORALES
        if (prochainesActivites.isNotEmpty()) {
            item {
                Text(
                    text = "Prochaines Activités",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OppeBluePrimary
                )
            }

            items(prochainesActivites.size) { index ->
                val act = prochainesActivites[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(OppeBluePrimary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Event, contentDescription = null, tint = OppeBluePrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = act.titre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = "${DateUtils.formatDate(act.dateDebut)} • ${act.lieu}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
    }
}
