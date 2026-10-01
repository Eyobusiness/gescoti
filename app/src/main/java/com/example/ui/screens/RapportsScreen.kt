package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CashMovementType
import com.example.ui.MainViewModel
import com.example.ui.components.StatKpiCard
import com.example.ui.theme.*
import com.example.utils.CurrencyFormatter

@Composable
fun RapportsScreen(
    viewModel: MainViewModel
) {
    val session by viewModel.session.collectAsState()
    val soldeCaisse by viewModel.soldeCaisse.collectAsState()
    val paiements by viewModel.paiements.collectAsState()
    val depenses by viewModel.depenses.collectAsState()
    val membres by viewModel.membres.collectAsState()
    val mouvements by viewModel.mouvementsCaisse.collectAsState()

    val totalVersement = paiements.filter { it.valide }.sumOf { it.montantVerse }
    val totalRemise = paiements.filter { it.valide }.sumOf { it.montantRemise }
    val totalImpute = paiements.filter { it.valide }.sumOf { it.montantTotalImpute }
    val totalDepense = depenses.filter { it.valide }.sumOf { it.montant }

    val totalEntrees = mouvements.filter { it.type == CashMovementType.ENTREE }.sumOf { it.montant }
    val totalSorties = mouvements.filter { it.type == CashMovementType.SORTIE }.sumOf { it.montant }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Rapports & États Financiers",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OppeBluePrimary
            )
            Text(
                text = "Bilan certifié pour l'organisation ${session.currentTenant?.sigle ?: ""} (${session.activePastoralYearLibelle})",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // BILAN DES COTISATIONS & REMISES (Règle Métier 13 : Remise ne réduit pas le dû, elle complète le paiement)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Bilan Cotisations & Remises",
                        fontWeight = FontWeight.Bold,
                        color = OppeBluePrimary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cotisations réellement encaissées en Caisse :", style = MaterialTheme.typography.bodySmall)
                        Text(CurrencyFormatter.formatFcfa(totalVersement), fontWeight = FontWeight.Bold, color = OppeGreenSuccess)
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total des Remises accordées :", style = MaterialTheme.typography.bodySmall)
                        Text(CurrencyFormatter.formatFcfa(totalRemise), fontWeight = FontWeight.Bold, color = OppePurpleRemise)
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Divider()
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total des Cotisations Soldées (Imputées) :", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text(CurrencyFormatter.formatFcfa(totalImpute), fontWeight = FontWeight.ExtraBold, color = OppeBluePrimary)
                    }
                }
            }
        }

        // BILAN DU JOURNAL DE CAISSE
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Bilan de Trésorerie & Caisse",
                        fontWeight = FontWeight.Bold,
                        color = OppeBluePrimary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total des Entrées en Caisse :", style = MaterialTheme.typography.bodySmall)
                        Text("+${CurrencyFormatter.formatFcfa(totalEntrees)}", fontWeight = FontWeight.Bold, color = OppeGreenSuccess)
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total des Dépenses Sorties :", style = MaterialTheme.typography.bodySmall)
                        Text("-${CurrencyFormatter.formatFcfa(totalSorties)}", fontWeight = FontWeight.Bold, color = OppeRedDebt)
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Divider()
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Solde Net Disponible en Caisse :", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text(
                            CurrencyFormatter.formatFcfa(soldeCaisse),
                            fontWeight = FontWeight.ExtraBold,
                            color = if (soldeCaisse >= 0) OppeGreenSuccess else OppeRedDebt,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // STATISTIQUES DES MEMBRES
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Effectifs & Engagement des Membres",
                        fontWeight = FontWeight.Bold,
                        color = OppeBluePrimary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total des Membres Inscrits :", style = MaterialTheme.typography.bodySmall)
                        Text("${membres.size}", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Membres avec statut Actif :", style = MaterialTheme.typography.bodySmall)
                        Text("${membres.count { it.statut == com.example.data.model.MemberStatus.ACTIF }}", fontWeight = FontWeight.Bold, color = OppeGreenSuccess)
                    }
                }
            }
        }
    }
}
