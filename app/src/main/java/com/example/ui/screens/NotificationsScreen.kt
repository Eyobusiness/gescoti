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
import com.example.data.model.NotificationEntity
import com.example.data.model.NotificationType
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import com.example.utils.DateUtils
import kotlinx.coroutines.launch

@Composable
fun NotificationsScreen(
    viewModel: MainViewModel
) {
    val session by viewModel.session.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val scope = rememberCoroutineScope()

    val tenantId = session.currentTenant?.id ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Centre de Notifications",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = OppeBluePrimary
                )
                Text(
                    text = "Alertes de cotisations, réceptions de paiements et annonces",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            val unreadCount = notifications.count { !it.lu }
            if (unreadCount > 0) {
                Surface(
                    color = OppeRedContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$unreadCount non lue(s)",
                        color = OppeRedDebt,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aucune notification pour le moment.",
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
                items(notifications) { notif ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (notif.lu) Color.White else Color(0xFFF0F4FF)
                        ),
                        elevation = CardDefaults.cardElevation(if (notif.lu) 1.dp else 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (notif.type) {
                                            NotificationType.PAIEMENT_ENREGISTRE -> OppeGreenContainer
                                            NotificationType.DEPENSE_EFFECTUEE -> OppeRedContainer
                                            NotificationType.ALERTE_RETARD -> OppeOrangeContainer
                                            else -> Color(0xFFEDE7F6)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (notif.type) {
                                        NotificationType.PAIEMENT_ENREGISTRE -> Icons.Default.Payments
                                        NotificationType.DEPENSE_EFFECTUEE -> Icons.Default.ReceiptLong
                                        NotificationType.ALERTE_RETARD -> Icons.Default.Warning
                                        NotificationType.NOUVELLE_ACTIVITE -> Icons.Default.Event
                                        else -> Icons.Default.Campaign
                                    },
                                    contentDescription = null,
                                    tint = when (notif.type) {
                                        NotificationType.PAIEMENT_ENREGISTRE -> OppeGreenSuccess
                                        NotificationType.DEPENSE_EFFECTUEE -> OppeRedDebt
                                        NotificationType.ALERTE_RETARD -> OppeOrangeWarning
                                        else -> Color(0xFF5E35B1)
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = notif.titre,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (notif.lu) Color.Black else OppeBluePrimary
                                    )
                                    Text(
                                        text = DateUtils.formatDateTime(notif.dateCreation),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = notif.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                            }
                            if (!notif.lu) {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            viewModel.db.notificationDao().marquerCommeLue(tenantId, notif.id)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = "Marquer comme lu",
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
