package com.example.data.seed

import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.FinanceRepository
import java.util.UUID

object DemoDataSeeder {

    const val TENANT_OPPE_ID = "tenant-paroisse-001"
    const val TENANT_ST_PIERRE_ID = "tenant-st-pierre-002"

    const val USER_SUPER_ADMIN_ID = "user-super-admin"
    const val USER_OPPE_ADMIN_ID = "user-paroisse-admin"
    const val USER_OPPE_TRESORIER_ID = "user-paroisse-tresorier"
    const val USER_OPPE_RESPONSABLE_ID = "user-paroisse-responsable"

    // IDENTIFIANTS OFFICIELS DE CONNEXION GESCOTI
    const val CREDENTIAL_SUPERADMIN_EMAIL = "superadmin@saas-eglise.com"
    const val CREDENTIAL_SUPERADMIN_PASS = "SuperAdmin@2026"

    const val CREDENTIAL_ADMIN_EMAIL = "admin@paroisse.ci"
    const val CREDENTIAL_ADMIN_PASS = "Admin@2026"

    const val CREDENTIAL_TRESORIER_EMAIL = "tresorier@paroisse.ci"
    const val CREDENTIAL_TRESORIER_PASS = "Tresorier@2026"

    const val CREDENTIAL_RESPONSABLE_EMAIL = "responsable@paroisse.ci"
    const val CREDENTIAL_RESPONSABLE_PASS = "Responsable@2026"

    const val ANNEE_PASTORALE_2026_ID = "annee-2026-2027"

    suspend fun seedIfNeeded(db: AppDatabase) {
        val tenantDao = db.tenantDao()
        val userDao = db.userDao()

        val existing = tenantDao.getTenantById(TENANT_OPPE_ID)
        if (existing != null) {
            // Mettre à jour l'organisation et les comptes pour refléter le modèle générique multi-tenant
            tenantDao.updateTenant(existing.copy(
                nom = "Paroisse Cœur Immaculé",
                sigle = "PCI",
                paroisse = "Paroisse Cœur Immaculé",
                email = "contact@paroisse.ci"
            ))
            val sa = userDao.getUserById(USER_SUPER_ADMIN_ID)
            if (sa != null) userDao.insertUser(sa.copy(motDePasse = CREDENTIAL_SUPERADMIN_PASS))
            val oa = userDao.getUserById(USER_OPPE_ADMIN_ID)
            if (oa != null) userDao.insertUser(oa.copy(email = CREDENTIAL_ADMIN_EMAIL, motDePasse = CREDENTIAL_ADMIN_PASS, nom = "Père André (Curé / Admin)"))
            val ot = userDao.getUserById(USER_OPPE_TRESORIER_ID)
            if (ot != null) userDao.insertUser(ot.copy(email = CREDENTIAL_TRESORIER_EMAIL, motDePasse = CREDENTIAL_TRESORIER_PASS, nom = "Jean Kouassi (Trésorier)"))
            val or = userDao.getUserById(USER_OPPE_RESPONSABLE_ID)
            if (or != null) userDao.insertUser(or.copy(email = CREDENTIAL_RESPONSABLE_EMAIL, motDePasse = CREDENTIAL_RESPONSABLE_PASS, nom = "Marie Claire (Responsable)"))
            return
        }

        // 1. ORGANISATIONS (TENANTS)
        val tenantPrincipal = TenantEntity(
            id = TENANT_OPPE_ID,
            nom = "Paroisse Cœur Immaculé",
            sigle = "PCI",
            paroisse = "Paroisse Cœur Immaculé",
            logoUri = "drawable/img_oppe_logo",
            email = "contact@paroisse.ci",
            telephone = "+225 07 00 11 22 33",
            adresse = "Abidjan, Côte d'Ivoire",
            actif = true
        )
        val tenantStPierre = TenantEntity(
            id = TENANT_ST_PIERRE_ID,
            nom = "Groupe Pastoral Saint Pierre",
            sigle = "GPSP",
            paroisse = "Paroisse Saint Pierre",
            logoUri = null,
            email = "saintpierre@eglise.ci",
            telephone = "+225 05 44 55 66 77",
            adresse = "Yopougon, Abidjan",
            actif = true
        )
        tenantDao.insertTenant(tenantPrincipal)
        tenantDao.insertTenant(tenantStPierre)

        // 2. UTILISATEURS
        val superAdmin = UserEntity(
            id = USER_SUPER_ADMIN_ID,
            tenantId = null, // Super Admin SaaS universel
            nom = "Super Administrateur SaaS",
            email = CREDENTIAL_SUPERADMIN_EMAIL,
            motDePasse = CREDENTIAL_SUPERADMIN_PASS,
            role = UserRole.SUPER_ADMIN,
            telephone = "+225 01 02 03 04 05"
        )
        val paroisseAdmin = UserEntity(
            id = USER_OPPE_ADMIN_ID,
            tenantId = TENANT_OPPE_ID,
            nom = "Père André (Curé / Admin)",
            email = CREDENTIAL_ADMIN_EMAIL,
            motDePasse = CREDENTIAL_ADMIN_PASS,
            role = UserRole.ADMINISTRATEUR,
            telephone = "+225 07 11 22 33 44"
        )
        val paroisseTresorier = UserEntity(
            id = USER_OPPE_TRESORIER_ID,
            tenantId = TENANT_OPPE_ID,
            nom = "Jean Kouassi (Trésorier)",
            email = CREDENTIAL_TRESORIER_EMAIL,
            motDePasse = CREDENTIAL_TRESORIER_PASS,
            role = UserRole.TRESORERIE,
            telephone = "+225 07 22 33 44 55"
        )
        val paroisseResponsable = UserEntity(
            id = USER_OPPE_RESPONSABLE_ID,
            tenantId = TENANT_OPPE_ID,
            nom = "Marie Claire (Responsable)",
            email = CREDENTIAL_RESPONSABLE_EMAIL,
            motDePasse = CREDENTIAL_RESPONSABLE_PASS,
            role = UserRole.RESPONSABLE,
            telephone = "+225 07 33 44 55 66"
        )
        userDao.insertUser(superAdmin)
        userDao.insertUser(paroisseAdmin)
        userDao.insertUser(paroisseTresorier)
        userDao.insertUser(paroisseResponsable)

        // 3. ANNÉE PASTORALE (2026-2027)
        val anneeDao = db.anneePastoraleDao()
        val annee2026 = AnneePastoraleEntity(
            id = ANNEE_PASTORALE_2026_ID,
            tenantId = TENANT_OPPE_ID,
            libelle = "2026-2027",
            dateDebut = 1790812800000L, // Octobre 2026
            dateFin = 1822348800000L,   // Septembre 2027
            estActive = true
        )
        anneeDao.insertAnnee(annee2026)

        // 4. PROFILS DE COTISATION CONFIGURABLES
        val profilDao = db.profilCotisationDao()
        val profilAdulte = ProfilCotisationEntity(
            id = "profil-adulte",
            tenantId = TENANT_OPPE_ID,
            nom = "Adulte",
            description = "Cotisation standard mensuelle adulte",
            montantMensuel = 500.0,
            montantAnnuel = 6000.0,
            actif = true
        )
        val profilJeune = ProfilCotisationEntity(
            id = "profil-jeune",
            tenantId = TENANT_OPPE_ID,
            nom = "Jeune",
            description = "Cotisation solidaire pour jeunes et ados",
            montantMensuel = 200.0,
            montantAnnuel = 2400.0,
            actif = true
        )
        val profilEtudiant = ProfilCotisationEntity(
            id = "profil-etudiant",
            tenantId = TENANT_OPPE_ID,
            nom = "Étudiant",
            description = "Tarif réduit pour étudiants et stagiaires",
            montantMensuel = 300.0,
            montantAnnuel = 3600.0,
            actif = true
        )
        profilDao.insertProfil(profilAdulte)
        profilDao.insertProfil(profilJeune)
        profilDao.insertProfil(profilEtudiant)

        // 5. CATÉGORIES DE DÉPENSES
        val catDao = db.categorieDepenseDao()
        val categories = listOf(
            "Transport", "Nourriture", "Communication",
            "Impression", "Matériel", "Activité", "Autre"
        ).map { nom ->
            CategorieDepenseEntity(
                id = "cat-${nom.lowercase()}",
                tenantId = TENANT_OPPE_ID,
                libelle = nom,
                description = "Dépenses liées à $nom",
                icone = when (nom) {
                    "Transport" -> "commute"
                    "Nourriture" -> "restaurant"
                    "Communication" -> "phone"
                    "Impression" -> "print"
                    "Matériel" -> "build"
                    "Activité" -> "event"
                    else -> "payments"
                },
                actif = true
            )
        }
        catDao.insertCategories(categories)

        // 6. MEMBRES & ÉCHÉANCIERS 12 MOIS
        val membreDao = db.membreDao()
        val financeRepo = FinanceRepository(db)

        val membre1 = MembreEntity(
            id = "membre-001",
            tenantId = TENANT_OPPE_ID,
            matricule = "PCI-2026-001",
            nom = "KOUASSI",
            prenoms = "Jean-Marc",
            sexe = "M",
            dateNaissance = "14/05/1988",
            telephone = "+225 07 48 12 34 56",
            whatsapp = "+225 07 48 12 34 56",
            email = "jm.kouassi@email.com",
            adresse = "Cocody Angré, Abidjan",
            statut = MemberStatus.ACTIF,
            typeMembre = MemberType.ADULTE,
            profilId = profilAdulte.id,
            anneePastoraleId = annee2026.id
        )
        val membre2 = MembreEntity(
            id = "membre-002",
            tenantId = TENANT_OPPE_ID,
            matricule = "PCI-2026-002",
            nom = "KONAN",
            prenoms = "Marie-Estelle",
            sexe = "F",
            dateNaissance = "22/09/1992",
            telephone = "+225 05 12 34 56 78",
            whatsapp = "+225 05 12 34 56 78",
            email = "m.konan@email.com",
            adresse = "Plateau Dokui, Abidjan",
            statut = MemberStatus.ACTIF,
            typeMembre = MemberType.ADULTE,
            profilId = profilAdulte.id,
            anneePastoraleId = annee2026.id
        )
        val membre3 = MembreEntity(
            id = "membre-003",
            tenantId = TENANT_OPPE_ID,
            matricule = "PCI-2026-003",
            nom = "YAO",
            prenoms = "Paul",
            sexe = "M",
            dateNaissance = "03/11/1985",
            telephone = "+225 01 23 45 67 89",
            whatsapp = "+225 01 23 45 67 89",
            email = "paul.yao@email.com",
            adresse = "Riviera Palmeraie, Abidjan",
            statut = MemberStatus.ACTIF,
            typeMembre = MemberType.ADULTE,
            profilId = profilAdulte.id,
            anneePastoraleId = annee2026.id
        )
        val membre4 = MembreEntity(
            id = "membre-004",
            tenantId = TENANT_OPPE_ID,
            matricule = "PCI-2026-004",
            nom = "BÉDIÉ",
            prenoms = "Patrick",
            sexe = "M",
            dateNaissance = "19/02/2004",
            telephone = "+225 07 99 88 77 66",
            whatsapp = "+225 07 99 88 77 66",
            email = "patrick.b@email.com",
            adresse = "Yopougon Maroc, Abidjan",
            statut = MemberStatus.ACTIF,
            typeMembre = MemberType.JEUNE,
            profilId = profilJeune.id,
            anneePastoraleId = annee2026.id
        )

        membreDao.insertMembre(membre1)
        membreDao.insertMembre(membre2)
        membreDao.insertMembre(membre3)
        membreDao.insertMembre(membre4)

        // Génération automatique des échéanciers 12 mois pour chaque membre
        financeRepo.genererEcheances12MoisPourMembre(TENANT_OPPE_ID, membre1.id, annee2026.id, annee2026.libelle, profilAdulte.montantMensuel)
        financeRepo.genererEcheances12MoisPourMembre(TENANT_OPPE_ID, membre2.id, annee2026.id, annee2026.libelle, profilAdulte.montantMensuel)
        financeRepo.genererEcheances12MoisPourMembre(TENANT_OPPE_ID, membre3.id, annee2026.id, annee2026.libelle, profilAdulte.montantMensuel)
        financeRepo.genererEcheances12MoisPourMembre(TENANT_OPPE_ID, membre4.id, annee2026.id, annee2026.libelle, profilJeune.montantMensuel)

        // 7. PAIEMENTS DÉMONSTRATION
        // Cas A : Jean-Marc paie 2 000 F (solde 4 mois à 500 F)
        financeRepo.enregistrerPaiementCotisation(
            tenantId = TENANT_OPPE_ID,
            anneeId = annee2026.id,
            anneeLibelle = annee2026.libelle,
            tenantSigle = "PCI",
            membreId = membre1.id,
            montantVerse = 2000.0,
            montantRemise = 0.0,
            modePaiement = PaymentMode.ESPECES,
            referenceExterne = "REC-JM-01",
            observation = "Versement 4 mois (Octobre à Janvier)",
            userId = USER_OPPE_TRESORIER_ID,
            userNom = paroisseTresorier.nom
        )

        // Cas B : Marie-Estelle fait un paiement partiel de 300 F (sur 500 F dus)
        financeRepo.enregistrerPaiementCotisation(
            tenantId = TENANT_OPPE_ID,
            anneeId = annee2026.id,
            anneeLibelle = annee2026.libelle,
            tenantSigle = "PCI",
            membreId = membre2.id,
            montantVerse = 300.0,
            montantRemise = 0.0,
            modePaiement = PaymentMode.MOBILE_MONEY,
            referenceExterne = "WAVE-09283471",
            observation = "Acompte partiel Octobre",
            userId = USER_OPPE_TRESORIER_ID,
            userNom = paroisseTresorier.nom
        )

        // Cas C : Paul paie l'année entière (6 000 F) avec 5 000 F versés et 1 000 F de remise accordée
        financeRepo.enregistrerPaiementCotisation(
            tenantId = TENANT_OPPE_ID,
            anneeId = annee2026.id,
            anneeLibelle = annee2026.libelle,
            tenantSigle = "PCI",
            membreId = membre3.id,
            montantVerse = 5000.0,
            montantRemise = 1000.0,
            modePaiement = PaymentMode.VIREMENT,
            referenceExterne = "VIR-BQ-202609",
            observation = "Règlement annuel avec remise fidélité bienfaiteur 1 000 F",
            userId = USER_OPPE_ADMIN_ID,
            userNom = paroisseAdmin.nom
        )

        // 8. COTISATION EXCEPTIONNELLE & PARTICIPATION
        val campagneDao = db.cotisationExceptionnelleDao()
        val campagneRecollection = CotisationExceptionnelleEntity(
            id = "campagne-recollection-2026",
            tenantId = TENANT_OPPE_ID,
            anneeId = annee2026.id,
            titre = "Récollection Paroissiale 2026",
            description = "Campagne de soutien pour la grande récollection spirituelle paroissiale",
            objectifFinancier = 500000.0,
            montantRecommande = 5000.0,
            dateDebut = System.currentTimeMillis() - (15L * 24 * 3600 * 1000),
            dateFin = System.currentTimeMillis() + (45L * 24 * 3600 * 1000),
            statut = CampaignStatus.EN_COURS
        )
        campagneDao.insertCampagne(campagneRecollection)

        financeRepo.enregistrerParticipationExceptionnelle(
            tenantId = TENANT_OPPE_ID,
            anneeId = annee2026.id,
            anneeLibelle = annee2026.libelle,
            tenantSigle = "PCI",
            campagneId = campagneRecollection.id,
            campagneTitre = campagneRecollection.titre,
            membreId = membre1.id,
            montantVerse = 10000.0,
            montantRemise = 0.0,
            modePaiement = PaymentMode.ESPECES,
            observation = "Don parrainage pour les participants",
            userId = USER_OPPE_TRESORIER_ID,
            userNom = paroisseTresorier.nom
        )

        // 9. DÉPENSES
        val catNourriture = categories.find { it.libelle == "Nourriture" } ?: categories.first()
        val catImpression = categories.find { it.libelle == "Impression" } ?: categories.first()

        financeRepo.enregistrerDepense(
            tenantId = TENANT_OPPE_ID,
            anneeId = annee2026.id,
            categorieId = catImpression.id,
            categorieNom = catImpression.libelle,
            activiteId = null,
            libelle = "Impression des carnets de chants et livrets",
            montant = 4500.0,
            modePaiement = PaymentMode.ESPECES,
            justificatifRef = "FACT-IMP-092",
            observation = "50 livrets",
            userId = USER_OPPE_TRESORIER_ID,
            userNom = paroisseTresorier.nom
        )

        financeRepo.enregistrerDepense(
            tenantId = TENANT_OPPE_ID,
            anneeId = annee2026.id,
            categorieId = catNourriture.id,
            categorieNom = catNourriture.libelle,
            activiteId = null,
            libelle = "Goûter de rentrée paroissiale",
            montant = 8000.0,
            modePaiement = PaymentMode.ESPECES,
            justificatifRef = "TICKET-SUP-114",
            observation = "Rafraîchissements et collations",
            userId = USER_OPPE_TRESORIER_ID,
            userNom = paroisseTresorier.nom
        )

        // 10. ACTIVITÉS PASTORALES
        val activiteDao = db.activiteDao()
        val activite1 = ActiviteEntity(
            id = "act-001",
            tenantId = TENANT_OPPE_ID,
            anneeId = annee2026.id,
            titre = "Messe d'Ouverture et Bénédiction",
            typeActivite = ActivityType.MESSE,
            description = "Célébration eucharistique solennelle avec animation chorale",
            dateDebut = System.currentTimeMillis() + (7L * 24 * 3600 * 1000),
            lieu = "Église Cœur Immaculé de Marie",
            responsableNom = "Marie Claire",
            budgetPrevisionnel = 15000.0,
            statut = ActivityStatus.PLANIFIEE
        )
        val activite2 = ActiviteEntity(
            id = "act-002",
            tenantId = TENANT_OPPE_ID,
            anneeId = annee2026.id,
            titre = "Grande Récollection Spirituelle 2026",
            typeActivite = ActivityType.RECOLLECTION,
            description = "Journée de prière, réconciliation et enseignement",
            dateDebut = System.currentTimeMillis() + (30L * 24 * 3600 * 1000),
            lieu = "Centre Spirituel Sainte Thérèse",
            responsableNom = "Père André",
            budgetPrevisionnel = 250000.0,
            statut = ActivityStatus.PLANIFIEE
        )
        activiteDao.insertActivite(activite1)
        activiteDao.insertActivite(activite2)
    }
}
