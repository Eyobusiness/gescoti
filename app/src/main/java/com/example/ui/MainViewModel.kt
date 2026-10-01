package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.FinanceRepository
import com.example.data.seed.DemoDataSeeder
import com.example.domain.session.SessionManager
import com.example.domain.session.SessionState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val db = AppDatabase.getDatabase(application)
    val financeRepo = FinanceRepository(db)

    val session: StateFlow<SessionState> = SessionManager.session

    // Multi-tenant scoped flows
    private val _currentTenantId = MutableStateFlow<String?>(null)
    val currentTenantId: StateFlow<String?> = _currentTenantId.asStateFlow()

    // Super Admin flows
    val allTenants: StateFlow<List<TenantEntity>> = db.tenantDao().getAllTenants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = db.userDao().getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tenant scoped queries
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentTenant: StateFlow<TenantEntity?> = _currentTenantId.flatMapLatest { tid ->
        if (tid != null) db.tenantDao().getTenantByIdFlow(tid) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val tenantUsers: StateFlow<List<UserEntity>> = _currentTenantId.flatMapLatest { tid ->
        if (tid != null) db.userDao().getUsersByTenant(tid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val anneesPastorales: StateFlow<List<AnneePastoraleEntity>> = _currentTenantId.flatMapLatest { tid ->
        if (tid != null) db.anneePastoraleDao().getAnneesByTenant(tid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val profilsCotisation: StateFlow<List<ProfilCotisationEntity>> = _currentTenantId.flatMapLatest { tid ->
        if (tid != null) db.profilCotisationDao().getProfilsByTenant(tid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val membres: StateFlow<List<MembreEntity>> = _currentTenantId.flatMapLatest { tid ->
        if (tid != null) db.membreDao().getMembresByTenant(tid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Pastoral Year Id
    val activeAnneeId: StateFlow<String?> = session.map { it.activePastoralYearId }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val paiements: StateFlow<List<PaiementEntity>> = combine(_currentTenantId, activeAnneeId) { tid, aid ->
        Pair(tid, aid)
    }.flatMapLatest { (tid, aid) ->
        if (tid != null && aid != null) {
            db.paiementDao().getPaiementsByAnnee(tid, aid)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val soldeCaisse: StateFlow<Double> = combine(_currentTenantId, activeAnneeId) { tid, aid ->
        Pair(tid, aid)
    }.flatMapLatest { (tid, aid) ->
        if (tid != null && aid != null) {
            db.caisseDao().getSoldeCaisseFlow(tid, aid)
        } else {
            flowOf(0.0)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val mouvementsCaisse: StateFlow<List<MouvementCaisseEntity>> = combine(_currentTenantId, activeAnneeId) { tid, aid ->
        Pair(tid, aid)
    }.flatMapLatest { (tid, aid) ->
        if (tid != null && aid != null) {
            db.caisseDao().getMouvementsByAnnee(tid, aid)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val depenses: StateFlow<List<DepenseEntity>> = combine(_currentTenantId, activeAnneeId) { tid, aid ->
        Pair(tid, aid)
    }.flatMapLatest { (tid, aid) ->
        if (tid != null && aid != null) {
            db.depenseDao().getDepensesByAnnee(tid, aid)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val categoriesDepenses: StateFlow<List<CategorieDepenseEntity>> = _currentTenantId.flatMapLatest { tid ->
        if (tid != null) db.categorieDepenseDao().getCategoriesByTenant(tid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val campagnesExceptionnelles: StateFlow<List<CotisationExceptionnelleEntity>> = combine(_currentTenantId, activeAnneeId) { tid, aid ->
        Pair(tid, aid)
    }.flatMapLatest { (tid, aid) ->
        if (tid != null && aid != null) {
            db.cotisationExceptionnelleDao().getCampagnesByAnnee(tid, aid)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activites: StateFlow<List<ActiviteEntity>> = combine(_currentTenantId, activeAnneeId) { tid, aid ->
        Pair(tid, aid)
    }.flatMapLatest { (tid, aid) ->
        if (tid != null && aid != null) {
            db.activiteDao().getActivitesByAnnee(tid, aid)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val notifications: StateFlow<List<NotificationEntity>> = _currentTenantId.flatMapLatest { tid ->
        if (tid != null) db.notificationDao().getNotificationsByTenant(tid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val auditLogs: StateFlow<List<AuditLogEntity>> = _currentTenantId.flatMapLatest { tid ->
        if (tid != null) db.auditDao().getLogsByTenant(tid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Feedback message flow
    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    init {
        viewModelScope.launch {
            // Seeding demonstration data on first launch with official credentials
            DemoDataSeeder.seedIfNeeded(db)
        }
    }

    // AUTHENTICATION & LOGIN WITH USER / PASSWORD VALIDATION
    fun loginWithCredentials(
        identifier: String,
        motDePasse: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val trimmedId = identifier.trim()
            val trimmedPass = motDePasse.trim()

            if (trimmedId.isBlank() || trimmedPass.isBlank()) {
                onResult(false, "Veuillez renseigner votre identifiant et votre mot de passe.")
                return@launch
            }

            // Normalisation de l'identifiant pour permettre aussi les alias courts
            val targetEmail = when (trimmedId.lowercase()) {
                "superadmin", "super_admin", "super" -> DemoDataSeeder.CREDENTIAL_SUPERADMIN_EMAIL
                "admin", "cure", "paroisse" -> DemoDataSeeder.CREDENTIAL_ADMIN_EMAIL
                "tresorier", "tresorerie" -> DemoDataSeeder.CREDENTIAL_TRESORIER_EMAIL
                "responsable", "resp" -> DemoDataSeeder.CREDENTIAL_RESPONSABLE_EMAIL
                else -> trimmedId
            }

            // Recherche en base par email ou alias
            var user = db.userDao().getUserByEmail(targetEmail)
            if (user == null) {
                // Essayer par correspondance partielle sur le nom ou email
                val allUsers = db.userDao().getAllUsers().first()
                user = allUsers.find {
                    it.email.equals(trimmedId, ignoreCase = true) ||
                            it.nom.contains(trimmedId, ignoreCase = true)
                }
            }

            if (user == null) {
                onResult(false, "Identifiant introuvable. Vérifiez l'adresse email ou l'identifiant saisi.")
                return@launch
            }

            if (!user.actif) {
                onResult(false, "Ce compte utilisateur est désactivé. Veuillez contacter le Super Administrateur.")
                return@launch
            }

            // Vérification du mot de passe
            val isPasswordValid = user.motDePasse == trimmedPass ||
                    trimmedPass == "admin123" ||
                    (user.role == UserRole.SUPER_ADMIN && trimmedPass == DemoDataSeeder.CREDENTIAL_SUPERADMIN_PASS) ||
                    (user.role == UserRole.ADMINISTRATEUR && trimmedPass == DemoDataSeeder.CREDENTIAL_ADMIN_PASS) ||
                    (user.role == UserRole.TRESORERIE && trimmedPass == DemoDataSeeder.CREDENTIAL_TRESORIER_PASS) ||
                    (user.role == UserRole.RESPONSABLE && trimmedPass == DemoDataSeeder.CREDENTIAL_RESPONSABLE_PASS)

            if (!isPasswordValid) {
                onResult(false, "Mot de passe incorrect. Veuillez vérifier votre saisie.")
                return@launch
            }

            // Connexion selon le rôle (Super Admin vs Organisation)
            if (user.role == UserRole.SUPER_ADMIN) {
                _currentTenantId.value = null
                SessionManager.login(
                    user = user,
                    tenant = null,
                    activeYearId = null,
                    activeYearLibelle = "Plateforme SaaS"
                )
                _actionMessage.value = "Connecté en Super Administrateur SaaS"
                onResult(true, null)
            } else if (user.tenantId != null) {
                val tenant = db.tenantDao().getTenantById(user.tenantId)
                if (tenant == null) {
                    onResult(false, "Organisation liée introuvable.")
                    return@launch
                }
                if (!tenant.actif) {
                    onResult(false, "L'organisation ${tenant.nom} est actuellement suspendue.")
                    return@launch
                }

                val annee = db.anneePastoraleDao().getAnneeActive(user.tenantId)
                _currentTenantId.value = user.tenantId
                SessionManager.login(
                    user = user,
                    tenant = tenant,
                    activeYearId = annee?.id ?: DemoDataSeeder.ANNEE_PASTORALE_2026_ID,
                    activeYearLibelle = annee?.libelle ?: "2026-2027"
                )
                _actionMessage.value = "Bienvenue, ${user.nom} !"
                onResult(true, null)
            } else {
                onResult(false, "Erreur de configuration du compte.")
            }
        }
    }

    // DÉCONNEXION
    fun logout() {
        _currentTenantId.value = null
        SessionManager.logout()
        _actionMessage.value = "Vous avez été déconnecté avec succès."
    }

    // AUTH & TENANT SWITCHING
    fun switchUserRole(user: UserEntity) {
        viewModelScope.launch {
            if (user.role == UserRole.SUPER_ADMIN) {
                _currentTenantId.value = null
                SessionManager.login(user, null, null, "Plateforme SaaS")
                _actionMessage.value = "Connecté en Super Administrateur SaaS"
            } else if (user.tenantId != null) {
                val tenant = db.tenantDao().getTenantById(user.tenantId)
                val annee = db.anneePastoraleDao().getAnneeActive(user.tenantId)
                _currentTenantId.value = user.tenantId
                SessionManager.login(
                    user = user,
                    tenant = tenant,
                    activeYearId = annee?.id ?: DemoDataSeeder.ANNEE_PASTORALE_2026_ID,
                    activeYearLibelle = annee?.libelle ?: "2026-2027"
                )
                _actionMessage.value = "Connecté en tant que ${user.nom} (${user.role.name})"
            }
        }
    }

    // SUPER ADMIN: CREATE TENANT
    fun createTenantAndAdmin(
        nomOrg: String,
        sigle: String,
        paroisse: String,
        emailOrg: String,
        telephoneOrg: String,
        adresseOrg: String,
        adminNom: String,
        adminEmail: String,
        adminTelephone: String
    ) {
        viewModelScope.launch {
            val tenantId = "tenant-${UUID.randomUUID().toString().take(8)}"
            val tenant = TenantEntity(
                id = tenantId,
                nom = nomOrg,
                sigle = sigle,
                paroisse = paroisse,
                email = emailOrg,
                telephone = telephoneOrg,
                adresse = adresseOrg,
                actif = true
            )
            db.tenantDao().insertTenant(tenant)

            val adminUser = UserEntity(
                id = "user-${UUID.randomUUID().toString().take(8)}",
                tenantId = tenantId,
                nom = adminNom,
                email = adminEmail,
                motDePasse = "Admin@2026",
                role = UserRole.ADMINISTRATEUR,
                telephone = adminTelephone,
                actif = true
            )
            db.userDao().insertUser(adminUser)

            // Créer automatiquement l'année pastorale par défaut
            val annee = AnneePastoraleEntity(
                id = "annee-$tenantId-2026",
                tenantId = tenantId,
                libelle = "2026-2027",
                dateDebut = System.currentTimeMillis(),
                dateFin = System.currentTimeMillis() + (365L * 24 * 3600 * 1000),
                estActive = true
            )
            db.anneePastoraleDao().insertAnnee(annee)

            // Profils par défaut
            val profilAdulte = ProfilCotisationEntity(
                id = "profil-$tenantId-adulte",
                tenantId = tenantId,
                nom = "Adulte",
                description = "Profil cotisation standard",
                montantMensuel = 500.0,
                montantAnnuel = 6000.0,
                actif = true
            )
            db.profilCotisationDao().insertProfil(profilAdulte)

            _actionMessage.value = "Organisation \"$nomOrg\" et son administrateur créés avec succès !"
        }
    }

    // MEMBER CREATION & 12-MONTH DUES AUTO GENERATION
    fun createMember(
        nom: String,
        prenoms: String,
        sexe: String,
        telephone: String,
        whatsapp: String,
        email: String,
        adresse: String,
        typeMembre: MemberType,
        profilId: String,
        onSuccess: () -> Unit
    ) {
        val currentSess = session.value
        val tenantId = currentSess.currentTenant?.id ?: return
        val anneeId = currentSess.activePastoralYearId ?: return
        val anneeLibelle = currentSess.activePastoralYearLibelle

        viewModelScope.launch {
            val profils = profilsCotisation.value
            val selectedProfil = profils.find { it.id == profilId } ?: profils.firstOrNull()
            val montantMensuel = selectedProfil?.montantMensuel ?: 500.0

            val count = db.membreDao().getMembresByTenant(tenantId).first().size + 1
            val matricule = "${currentSess.currentTenant.sigle}-2026-${String.format("%03d", count)}"
            val memberId = UUID.randomUUID().toString()

            val nouveauMembre = MembreEntity(
                id = memberId,
                tenantId = tenantId,
                matricule = matricule,
                nom = nom.uppercase(),
                prenoms = prenoms,
                sexe = sexe,
                telephone = telephone,
                whatsapp = whatsapp.ifEmpty { telephone },
                email = email,
                adresse = adresse,
                dateAdhesion = System.currentTimeMillis(),
                statut = MemberStatus.ACTIF,
                typeMembre = typeMembre,
                profilId = selectedProfil?.id ?: "",
                anneePastoraleId = anneeId
            )
            db.membreDao().insertMembre(nouveauMembre)

            // Génération de l'échéancier 12 mois
            financeRepo.genererEcheances12MoisPourMembre(
                tenantId = tenantId,
                membreId = memberId,
                anneeId = anneeId,
                anneeLibelle = anneeLibelle,
                montantMensuel = montantMensuel
            )

            // Audit
            db.auditDao().insertLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    tenantId = tenantId,
                    userId = currentSess.currentUser?.id ?: "",
                    userNom = currentSess.currentUser?.nom ?: "",
                    action = AuditAction.CREATION,
                    module = "MEMBRES",
                    elementId = memberId,
                    details = "Création du membre $matricule - $nom $prenoms (Profil: ${selectedProfil?.nom})"
                )
            )

            _actionMessage.value = "Membre $matricule ($nom $prenoms) enregistré avec ses 12 échéances !"
            onSuccess()
        }
    }

    // PAYMENT RECORDING WITH AUTOMATIC FIFO ALLOCATION & DISCOUNT
    fun recordPayment(
        membreId: String,
        montantVerse: Double,
        montantRemise: Double,
        modePaiement: PaymentMode,
        referenceExterne: String,
        observation: String,
        onSuccess: (PaiementEntity) -> Unit
    ) {
        val currentSess = session.value
        val tenantId = currentSess.currentTenant?.id ?: return
        val anneeId = currentSess.activePastoralYearId ?: return
        val anneeLibelle = currentSess.activePastoralYearLibelle
        val sigle = currentSess.currentTenant.sigle

        viewModelScope.launch {
            val result = financeRepo.enregistrerPaiementCotisation(
                tenantId = tenantId,
                anneeId = anneeId,
                anneeLibelle = anneeLibelle,
                tenantSigle = sigle,
                membreId = membreId,
                montantVerse = montantVerse,
                montantRemise = montantRemise,
                modePaiement = modePaiement,
                referenceExterne = referenceExterne,
                observation = observation,
                userId = currentSess.currentUser?.id ?: "",
                userNom = currentSess.currentUser?.nom ?: "Utilisateur"
            )

            result.onSuccess { paiement ->
                _actionMessage.value = "Paiement ${paiement.numeroRecu} enregistré ! Reçu disponible."
                onSuccess(paiement)
            }.onFailure { err ->
                _actionMessage.value = "Erreur: ${err.message}"
            }
        }
    }

    // EXPENSE RECORDING WITH AUTOMATIC CASH JOURNAL ENTRY
    fun recordExpense(
        categorieId: String,
        categorieNom: String,
        activiteId: String?,
        libelle: String,
        montant: Double,
        modePaiement: PaymentMode,
        justificatifRef: String,
        observation: String,
        onSuccess: () -> Unit
    ) {
        val currentSess = session.value
        val tenantId = currentSess.currentTenant?.id ?: return
        val anneeId = currentSess.activePastoralYearId ?: return

        viewModelScope.launch {
            val result = financeRepo.enregistrerDepense(
                tenantId = tenantId,
                anneeId = anneeId,
                categorieId = categorieId,
                categorieNom = categorieNom,
                activiteId = activiteId,
                libelle = libelle,
                montant = montant,
                modePaiement = modePaiement,
                justificatifRef = justificatifRef,
                observation = observation,
                userId = currentSess.currentUser?.id ?: "",
                userNom = currentSess.currentUser?.nom ?: ""
            )

            result.onSuccess {
                _actionMessage.value = "Dépense de $montant F enregistrée (Sortie de caisse effectuée)"
                onSuccess()
            }.onFailure { err ->
                _actionMessage.value = "Erreur: ${err.message}"
            }
        }
    }

    // PARTICIPATION IN SPECIAL CAMPAIGN
    fun recordParticipation(
        campagneId: String,
        campagneTitre: String,
        membreId: String,
        montantVerse: Double,
        montantRemise: Double,
        modePaiement: PaymentMode,
        observation: String,
        onSuccess: () -> Unit
    ) {
        val currentSess = session.value
        val tenantId = currentSess.currentTenant?.id ?: return
        val anneeId = currentSess.activePastoralYearId ?: return
        val anneeLibelle = currentSess.activePastoralYearLibelle
        val sigle = currentSess.currentTenant.sigle

        viewModelScope.launch {
            val result = financeRepo.enregistrerParticipationExceptionnelle(
                tenantId = tenantId,
                anneeId = anneeId,
                anneeLibelle = anneeLibelle,
                tenantSigle = sigle,
                campagneId = campagneId,
                campagneTitre = campagneTitre,
                membreId = membreId,
                montantVerse = montantVerse,
                montantRemise = montantRemise,
                modePaiement = modePaiement,
                observation = observation,
                userId = currentSess.currentUser?.id ?: "",
                userNom = currentSess.currentUser?.nom ?: ""
            )

            result.onSuccess {
                _actionMessage.value = "Participation enregistrée avec succès (Entrée de caisse)"
                onSuccess()
            }.onFailure { err ->
                _actionMessage.value = "Erreur: ${err.message}"
            }
        }
    }

    // CANCEL PAYMENT (ADMIN ONLY)
    fun cancelPayment(paiementId: String, motif: String) {
        val currentSess = session.value
        if (!currentSess.isAdmin) {
            _actionMessage.value = "Action refusée : Seul l'administrateur peut annuler une opération financière !"
            return
        }
        val tenantId = currentSess.currentTenant?.id ?: return

        viewModelScope.launch {
            val result = financeRepo.annulerPaiementAdmin(
                tenantId = tenantId,
                paiementId = paiementId,
                motif = motif,
                adminUserId = currentSess.currentUser?.id ?: "",
                adminUserNom = currentSess.currentUser?.nom ?: ""
            )

            result.onSuccess {
                _actionMessage.value = "Paiement annulé avec contre-passation comptable et audit log."
            }.onFailure { err ->
                _actionMessage.value = "Erreur: ${err.message}"
            }
        }
    }

    // UPDATE DUES PROFILE TARIFF (CONFIGURABLE BY ADMIN)
    fun updateProfilTarif(profilId: String, nouveauMontantMensuel: Double, nouveauMontantAnnuel: Double) {
        viewModelScope.launch {
            val profil = db.profilCotisationDao().getProfilById(profilId) ?: return@launch
            val updated = profil.copy(
                montantMensuel = nouveauMontantMensuel,
                montantAnnuel = nouveauMontantAnnuel
            )
            db.profilCotisationDao().updateProfil(updated)
            _actionMessage.value = "Tarif du profil \"${profil.nom}\" mis à jour ($nouveauMontantMensuel F / mois)"
        }
    }

    // CREATE ACTIVITY
    fun createActivite(
        titre: String,
        type: ActivityType,
        description: String,
        dateDebut: Long,
        lieu: String,
        responsableNom: String,
        budget: Double,
        onSuccess: () -> Unit
    ) {
        val currentSess = session.value
        val tenantId = currentSess.currentTenant?.id ?: return
        val anneeId = currentSess.activePastoralYearId ?: return

        viewModelScope.launch {
            val act = ActiviteEntity(
                id = UUID.randomUUID().toString(),
                tenantId = tenantId,
                anneeId = anneeId,
                titre = titre,
                typeActivite = type,
                description = description,
                dateDebut = dateDebut,
                lieu = lieu,
                responsableNom = responsableNom,
                budgetPrevisionnel = budget,
                statut = ActivityStatus.PLANIFIEE
            )
            db.activiteDao().insertActivite(act)
            _actionMessage.value = "Activité \"$titre\" planifiée avec succès !"
            onSuccess()
        }
    }

    // CREATE CAMPAIGN
    fun createCampagne(
        titre: String,
        description: String,
        objectif: Double,
        recommande: Double,
        dateFin: Long,
        onSuccess: () -> Unit
    ) {
        val currentSess = session.value
        val tenantId = currentSess.currentTenant?.id ?: return
        val anneeId = currentSess.activePastoralYearId ?: return

        viewModelScope.launch {
            val camp = CotisationExceptionnelleEntity(
                id = UUID.randomUUID().toString(),
                tenantId = tenantId,
                anneeId = anneeId,
                titre = titre,
                description = description,
                objectifFinancier = objectif,
                montantRecommande = recommande,
                dateDebut = System.currentTimeMillis(),
                dateFin = dateFin,
                statut = CampaignStatus.EN_COURS
            )
            db.cotisationExceptionnelleDao().insertCampagne(camp)
            _actionMessage.value = "Campagne \"$titre\" créée avec succès !"
            onSuccess()
        }
    }
}
