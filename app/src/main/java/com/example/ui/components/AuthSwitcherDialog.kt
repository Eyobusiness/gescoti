package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.OppeBluePrimary

@Composable
fun AuthSwitcherDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val users by viewModel.allUsers.collectAsState()
    val session by viewModel.session.collectAsState()
    val tenants by viewModel.allTenants.collectAsState()
    val tenantMap = tenants.associateBy { it.id }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = OppeBluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Changer d'Utilisateur / Rôle", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "Basculez entre les rôles pour tester les permissions réelles (Super Admin SaaS, Admin, Trésorier, Responsable) et l'isolation multi-tenant :",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users) { user ->
                        val isSelected = session.currentUser?.id == user.id
                        val tenant = if (user.tenantId != null) tenantMap[user.tenantId] else null
                        val orgLabel = tenant?.nom ?: "Plateforme SaaS Universelle"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.switchUserRole(user)
                                    onDismiss()
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFE8EAF6) else Color(0xFFF9F9F9)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.nom,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Rôle : ${user.role.name}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OppeBluePrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = orgLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Actif",
                                        tint = OppeBluePrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}
