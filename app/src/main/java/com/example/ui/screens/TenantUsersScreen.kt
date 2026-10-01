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
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.MainViewModel
import com.example.ui.theme.OppeBluePrimary
import com.example.ui.theme.OppeGreenContainer
import com.example.ui.theme.OppeGreenSuccess
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantUsersScreen(
    viewModel: MainViewModel
) {
    val session by viewModel.session.collectAsState()
    val users by viewModel.tenantUsers.collectAsState()
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (session.isAdmin) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = OppeBluePrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Ajouter un utilisateur")
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
                text = "Utilisateurs de l'Organisation",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OppeBluePrimary
            )
            Text(
                text = "Gestion des comptes Administrateur, Trésorier et Responsable pour ${session.currentTenant?.nom ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(users) { u ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (u.role) {
                                            UserRole.ADMINISTRATEUR -> OppeBluePrimary.copy(alpha = 0.12f)
                                            UserRole.TRESORERIE -> OppeGreenContainer
                                            UserRole.RESPONSABLE -> Color(0xFFEDE7F6)
                                            else -> Color.LightGray
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (u.role) {
                                        UserRole.ADMINISTRATEUR -> Icons.Default.AdminPanelSettings
                                        UserRole.TRESORERIE -> Icons.Default.AccountBalance
                                        UserRole.RESPONSABLE -> Icons.Default.SupervisorAccount
                                        else -> Icons.Default.Person
                                    },
                                    contentDescription = null,
                                    tint = when (u.role) {
                                        UserRole.ADMINISTRATEUR -> OppeBluePrimary
                                        UserRole.TRESORERIE -> OppeGreenSuccess
                                        UserRole.RESPONSABLE -> Color(0xFF5E35B1)
                                        else -> Color.Black
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = u.nom,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = u.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                if (u.telephone.isNotEmpty()) {
                                    Text(
                                        text = "Tél: ${u.telephone}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.DarkGray
                                    )
                                }
                            }

                            Surface(
                                color = when (u.role) {
                                    UserRole.ADMINISTRATEUR -> Color(0xFFE8EAF6)
                                    UserRole.TRESORERIE -> OppeGreenContainer
                                    UserRole.RESPONSABLE -> Color(0xFFEDE7F6)
                                    else -> Color(0xFFEEEEEE)
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = u.role.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (u.role) {
                                        UserRole.ADMINISTRATEUR -> OppeBluePrimary
                                        UserRole.TRESORERIE -> OppeGreenSuccess
                                        UserRole.RESPONSABLE -> Color(0xFF5E35B1)
                                        else -> Color.Black
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTenantUserDialog(
            tenantId = session.currentTenant?.id ?: "",
            onDismiss = { showAddDialog = false },
            onConfirm = { newUser ->
                scope.launch {
                    viewModel.db.userDao().insertUser(newUser)
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun AddTenantUserDialog(
    tenantId: String,
    onDismiss: () -> Unit,
    onConfirm: (UserEntity) -> Unit
) {
    var nom by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.RESPONSABLE) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un Utilisateur au Tenant", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (errorMsg != null) {
                    Text(errorMsg ?: "", color = Color.Red, fontSize = 13.sp)
                }

                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom complet *") },
                    placeholder = { Text("ex: Paul Koffi") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email *") },
                    placeholder = { Text("paul.koffi@paroisse.ci") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = telephone,
                    onValueChange = { telephone = it },
                    label = { Text("Téléphone") },
                    placeholder = { Text("+225 07 ...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Rôle dans l'organisation :", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                listOf(UserRole.ADMINISTRATEUR, UserRole.TRESORERIE, UserRole.RESPONSABLE).forEach { role ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (role) {
                                UserRole.ADMINISTRATEUR -> "Administrateur (Tous droits)"
                                UserRole.TRESORERIE -> "Trésorerie (Finances, Caisse, Reçus)"
                                UserRole.RESPONSABLE -> "Responsable (Consultation, Activités)"
                                else -> role.name
                            },
                            fontSize = 13.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nom.isBlank() || email.isBlank()) {
                        errorMsg = "Veuillez renseigner le nom et l'email"
                        return@Button
                    }
                    val user = UserEntity(
                        id = "user-${UUID.randomUUID().toString().take(8)}",
                        tenantId = tenantId,
                        nom = nom.trim(),
                        email = email.trim(),
                        role = selectedRole,
                        telephone = telephone.trim(),
                        actif = true
                    )
                    onConfirm(user)
                },
                colors = ButtonDefaults.buttonColors(containerColor = OppeBluePrimary)
            ) {
                Text("Créer l'Utilisateur")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
