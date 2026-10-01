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
import com.example.data.model.CashMovementType
import com.example.data.model.MouvementCaisseEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import com.example.utils.CurrencyFormatter
import com.example.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaisseScreen(
    viewModel: MainViewModel
) {
    val soldeCaisse by viewModel.soldeCaisse.collectAsState()
    val mouvements by viewModel.mouvementsCaisse.collectAsState()

    var selectedFilter by remember { mutableStateOf<CashMovementType?>(null) } // null = tous

    val filteredMouvements = remember(mouvements, selectedFilter) {
        if (selectedFilter == null) mouvements else mouvements.filter { it.type == selectedFilter }
    }

    val totalEntrees = mouvements.filter { it.type == CashMovementType.ENTREE }.sumOf { it.montant }
    val totalSorties = mouvements.filter { it.type == CashMovementType.SORTIE }.sumOf { it.montant }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // CARTE DU SOLDE CALCULÉ
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = OppeBluePrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "JOURNAL DE CAISSE OFFICIEL",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Solde protégé et calculé",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = CurrencyFormatter.formatFcfa(soldeCaisse),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Text(
                        text = "Solde calculé automatiquement : Total Entrées - Total Sorties",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Entrées Encaissées", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                            Text(
                                text = "+${CurrencyFormatter.formatFcfa(totalEntrees)}",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA5D6A7),
                                fontSize = 15.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Dépenses Sorties", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                            Text(
                                text = "-${CurrencyFormatter.formatFcfa(totalSorties)}",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF9A9A),
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // FILTRES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("Tous les flux (${mouvements.size})") }
                )
                FilterChip(
                    selected = selectedFilter == CashMovementType.ENTREE,
                    onClick = { selectedFilter = CashMovementType.ENTREE },
                    label = { Text("Entrées") }
                )
                FilterChip(
                    selected = selectedFilter == CashMovementType.SORTIE,
                    onClick = { selectedFilter = CashMovementType.SORTIE },
                    label = { Text("Sorties") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredMouvements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun mouvement de caisse enregistré.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMouvements) { m ->
                        MouvementCaisseItem(m)
                    }
                }
            }
        }
    }
}

@Composable
fun MouvementCaisseItem(m: MouvementCaisseEntity) {
    val isEntree = m.type == CashMovementType.ENTREE

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
                    .background(if (isEntree) OppeGreenContainer else OppeRedContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isEntree) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (isEntree) OppeGreenSuccess else OppeRedDebt,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = m.libelle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "${m.categorie} • ${DateUtils.formatDateTime(m.dateMouvement)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = "Auteur: ${m.enregistreParNom} • Source: ${m.typeSource.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.DarkGray
                )
                if (m.observation.isNotEmpty()) {
                    Text(
                        text = "Note: ${m.observation}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
            Text(
                text = "${if (isEntree) "+" else "-"}${CurrencyFormatter.formatFcfa(m.montant)}",
                fontWeight = FontWeight.ExtraBold,
                color = if (isEntree) OppeGreenSuccess else OppeRedDebt,
                fontSize = 15.sp
            )
        }
    }
}
