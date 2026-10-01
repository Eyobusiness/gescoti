package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.seed.DemoDataSeeder
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MainViewModel
) {
    var identifier by remember { mutableStateOf(DemoDataSeeder.CREDENTIAL_ADMIN_EMAIL) }
    var password by remember { mutableStateOf(DemoDataSeeder.CREDENTIAL_ADMIN_PASS) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GescotiPureWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // LOGO ET EN-TÊTE OFFICIEL OPPE & GESCOTI
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(GescotiMarianNavy),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_oppe_logo),
                    contentDescription = "Logo GESCOTI",
                    modifier = Modifier.size(68.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // TYPOGRAPHIE GESCOTI AUX COULEURS DE L'EMBLÈME
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("G", color = GescotiSkyBlue, fontWeight = FontWeight.Black, fontSize = 28.sp)
                Text("E", color = GescotiMagentaPink, fontWeight = FontWeight.Black, fontSize = 28.sp)
                Text("S", color = GescotiSunlitGold, fontWeight = FontWeight.Black, fontSize = 28.sp)
                Text("C", color = GescotiCrimsonRed, fontWeight = FontWeight.Black, fontSize = 28.sp)
                Text("O", color = GescotiSkyBlue, fontWeight = FontWeight.Black, fontSize = 28.sp)
                Text("T", color = GescotiMagentaPink, fontWeight = FontWeight.Black, fontSize = 28.sp)
                Text("I", color = GescotiSunlitGold, fontWeight = FontWeight.Black, fontSize = 28.sp)
            }

            Text(
                text = "GESCOTI • Plateforme SaaS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GescotiMarianNavy,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Gestion des cotisations, caisse & activités",
                style = MaterialTheme.typography.bodySmall,
                color = GescotiTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // FORMULAIRE DE CONNEXION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Authentification",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GescotiMarianNavy
                    )

                    if (errorMessage != null) {
                        Surface(
                            color = GescotiRedContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = GescotiCrimsonRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = GescotiCrimsonRed,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Champ Identifiant / Email
                    OutlinedTextField(
                        value = identifier,
                        onValueChange = {
                            identifier = it
                            errorMessage = null
                        },
                        label = { Text("Email ou Identifiant") },
                        placeholder = { Text("ex: admin.oppe@eglise.ci ou admin") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = GescotiMarianNavy
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    // Champ Mot de passe
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Mot de passe") },
                        placeholder = { Text("Votre mot de passe sécurisé") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = GescotiMarianNavy
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isPasswordVisible) "Masquer le mot de passe" else "Afficher le mot de passe",
                                    tint = Color.Gray
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                performLogin(identifier, password, viewModel, { isLoading = it }, { errorMessage = it })
                            }
                        )
                    )

                    // Bouton de Connexion
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            performLogin(identifier, password, viewModel, { isLoading = it }, { errorMessage = it })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GescotiMarianNavy),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        } else {
                            Icon(Icons.Default.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isLoading) "Vérification en cours..." else "Se Connecter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // SECTION COMPTES DE DÉMONSTRATION RAPIDES
            Text(
                text = "COMPTES DE DÉMONSTRATION DISPONIBLES",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = GescotiTextSecondary,
                letterSpacing = 1.sp
            )
            Text(
                text = "Cliquez sur un profil pour remplir automatiquement les identifiants :",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Super Administrateur SaaS
                DemoAccountCard(
                    title = "Super Administrateur SaaS",
                    subtitle = "Console centrale plateforme & création de paroisses",
                    email = DemoDataSeeder.CREDENTIAL_SUPERADMIN_EMAIL,
                    password = DemoDataSeeder.CREDENTIAL_SUPERADMIN_PASS,
                    badge = "SUPER ADMIN",
                    badgeColor = GescotiMarianNavy,
                    badgeContainer = GescotiNavyContainer,
                    icon = Icons.Default.Shield,
                    onSelect = {
                        identifier = DemoDataSeeder.CREDENTIAL_SUPERADMIN_EMAIL
                        password = DemoDataSeeder.CREDENTIAL_SUPERADMIN_PASS
                        errorMessage = null
                    }
                )

                // 2. Administrateur Paroisse
                DemoAccountCard(
                    title = "Père André (Curé / Administrateur)",
                    subtitle = "Accès intégral à l'organisation paroissiale",
                    email = DemoDataSeeder.CREDENTIAL_ADMIN_EMAIL,
                    password = DemoDataSeeder.CREDENTIAL_ADMIN_PASS,
                    badge = "ADMINISTRATEUR",
                    badgeColor = GescotiSunlitGold,
                    badgeContainer = GescotiGoldContainer,
                    icon = Icons.Default.CorporateFare,
                    onSelect = {
                        identifier = DemoDataSeeder.CREDENTIAL_ADMIN_EMAIL
                        password = DemoDataSeeder.CREDENTIAL_ADMIN_PASS
                        errorMessage = null
                    }
                )

                // 3. Trésorier
                DemoAccountCard(
                    title = "Jean Kouassi (Trésorier)",
                    subtitle = "Gestion des cotisations, caisse et reçus",
                    email = DemoDataSeeder.CREDENTIAL_TRESORIER_EMAIL,
                    password = DemoDataSeeder.CREDENTIAL_TRESORIER_PASS,
                    badge = "TRÉSORERIE",
                    badgeColor = GescotiGreenSuccess,
                    badgeContainer = GescotiGreenContainer,
                    icon = Icons.Default.AccountBalanceWallet,
                    onSelect = {
                        identifier = DemoDataSeeder.CREDENTIAL_TRESORIER_EMAIL
                        password = DemoDataSeeder.CREDENTIAL_TRESORIER_PASS
                        errorMessage = null
                    }
                )

                // 4. Responsable
                DemoAccountCard(
                    title = "Marie Claire (Responsable)",
                    subtitle = "Suivi des membres, activités et pastorale",
                    email = DemoDataSeeder.CREDENTIAL_RESPONSABLE_EMAIL,
                    password = DemoDataSeeder.CREDENTIAL_RESPONSABLE_PASS,
                    badge = "RESPONSABLE",
                    badgeColor = GescotiMagentaPink,
                    badgeContainer = GescotiMagentaContainer,
                    icon = Icons.Default.Group,
                    onSelect = {
                        identifier = DemoDataSeeder.CREDENTIAL_RESPONSABLE_EMAIL
                        password = DemoDataSeeder.CREDENTIAL_RESPONSABLE_PASS
                        errorMessage = null
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                color = GescotiNavyContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = GescotiMarianNavy,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Astuce : Vous pouvez aussi saisir simplement \"superadmin\" ou \"admin\" comme identifiant, avec le mot de passe universel \"admin123\".",
                        style = MaterialTheme.typography.bodySmall,
                        color = GescotiMarianNavy
                    )
                }
            }
        }
    }
}

private fun performLogin(
    identifier: String,
    pass: String,
    viewModel: MainViewModel,
    onLoading: (Boolean) -> Unit,
    onError: (String?) -> Unit
) {
    if (identifier.isBlank()) {
        onError("Veuillez renseigner votre email ou identifiant.")
        return
    }
    if (pass.isBlank()) {
        onError("Veuillez renseigner votre mot de passe.")
        return
    }

    onLoading(true)
    onError(null)

    viewModel.loginWithCredentials(
        identifier = identifier,
        motDePasse = pass
    ) { success, err ->
        onLoading(false)
        if (!success) {
            onError(err ?: "Échec de l'authentification.")
        }
    }
}

@Composable
private fun DemoAccountCard(
    title: String,
    subtitle: String,
    email: String,
    password: String,
    badge: String,
    badgeColor: Color,
    badgeContainer: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder(),
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
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(badgeContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = GescotiTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = badgeContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = badge,
                            color = badgeColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            fontSize = 9.sp
                        )
                    }
                }
                Text(
                    text = "Email : $email",
                    style = MaterialTheme.typography.bodySmall,
                    color = GescotiTextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "Mot de passe : $password",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = "Sélectionner",
                tint = GescotiMarianNavy,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
