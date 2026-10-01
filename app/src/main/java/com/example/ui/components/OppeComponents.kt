package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DuesStatus
import com.example.data.model.MemberStatus
import com.example.ui.theme.*
import com.example.utils.CurrencyFormatter
import com.example.utils.DateUtils

@Composable
fun StatKpiCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun DuesStatusBadge(status: DuesStatus) {
    val (bgColor, textColor, label) = when (status) {
        DuesStatus.PAYE -> Triple(OppeGreenContainer, OppeGreenSuccess, "Payé")
        DuesStatus.SOLDE_PAR_REMISE -> Triple(OppePurpleContainer, OppePurpleRemise, "Soldé par remise")
        DuesStatus.PARTIELLEMENT_PAYE -> Triple(OppeOrangeContainer, OppeOrangeWarning, "Partiel")
        DuesStatus.IMPAYE -> Triple(OppeRedContainer, OppeRedDebt, "Impayé")
        DuesStatus.A_VENIR -> Triple(Color(0xFFEDE7F6), Color(0xFF5E35B1), "À venir")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun MemberStatusBadge(status: MemberStatus) {
    val (bgColor, textColor) = when (status) {
        MemberStatus.ACTIF -> Pair(OppeGreenContainer, OppeGreenSuccess)
        MemberStatus.INACTIF -> Pair(Color(0xFFEEEEEE), Color(0xFF757575))
        MemberStatus.SUSPENDU -> Pair(OppeRedContainer, OppeRedDebt)
        MemberStatus.ANCIEN -> Pair(Color(0xFFE0F7FA), Color(0xFF00838F))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status.name,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
