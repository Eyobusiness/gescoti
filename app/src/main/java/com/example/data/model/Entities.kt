package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tenants",
    indices = [Index(value = ["nom"], unique = true)]
)
data class TenantEntity(
    @PrimaryKey val id: String, // UUID
    val nom: String,
    val sigle: String,
    val paroisse: String,
    val logoUri: String? = null,
    val email: String,
    val telephone: String,
    val adresse: String,
    val actif: Boolean = true,
    val dateCreation: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["tenantId", "role"])
    ]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val tenantId: String?, // Null pour SUPER_ADMIN
    val nom: String,
    val email: String,
    val motDePasse: String = "admin123", // Mot de passe d'authentification
    val role: UserRole,
    val telephone: String = "",
    val photoUrl: String? = null,
    val actif: Boolean = true,
    val dateCreation: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "annees_pastorales",
    indices = [Index(value = ["tenantId", "estActive"])]
)
data class AnneePastoraleEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val libelle: String, // ex: "2026-2027"
    val dateDebut: Long,
    val dateFin: Long,
    val estActive: Boolean = false
)

@Entity(
    tableName = "profils_cotisation",
    indices = [Index(value = ["tenantId", "nom"])]
)
data class ProfilCotisationEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val nom: String, // ex: "Adulte", "Jeune", "Étudiant"
    val description: String,
    val montantMensuel: Double, // ex: 500.0 F
    val montantAnnuel: Double,  // ex: 6000.0 F
    val actif: Boolean = true
)

@Entity(
    tableName = "membres",
    indices = [
        Index(value = ["tenantId", "matricule"], unique = true),
        Index(value = ["tenantId", "statut"]),
        Index(value = ["tenantId", "profilId"])
    ]
)
data class MembreEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val matricule: String, // ex: "OPPE-2026-001"
    val nom: String,
    val prenoms: String,
    val sexe: String, // "M" ou "F"
    val dateNaissance: String = "",
    val telephone: String = "",
    val whatsapp: String = "",
    val email: String = "",
    val adresse: String = "",
    val dateAdhesion: Long = System.currentTimeMillis(),
    val statut: MemberStatus = MemberStatus.ACTIF,
    val typeMembre: MemberType = MemberType.ADULTE,
    val profilId: String,
    val anneePastoraleId: String,
    val photoUri: String? = null
)

@Entity(
    tableName = "echeances_cotisation",
    indices = [
        Index(value = ["tenantId", "membreId", "anneeId", "moisIndex"], unique = true),
        Index(value = ["tenantId", "anneeId", "statut"]),
        Index(value = ["membreId", "statut"])
    ]
)
data class EcheanceCotisationEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val membreId: String,
    val anneeId: String,
    val moisIndex: Int, // 1 (ex: Octobre) à 12 (ex: Septembre)
    val libelleMois: String, // ex: "Octobre 2026"
    val dateEcheance: Long,
    val montantDu: Double,
    val montantPaye: Double = 0.0,
    val montantRemis: Double = 0.0,
    val solde: Double = montantDu,
    val statut: DuesStatus = DuesStatus.A_VENIR
)

@Entity(
    tableName = "paiements",
    indices = [
        Index(value = ["tenantId", "numeroRecu"], unique = true),
        Index(value = ["tenantId", "anneeId", "datePaiement"]),
        Index(value = ["membreId"])
    ]
)
data class PaiementEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val anneeId: String,
    val numeroRecu: String, // ex: "OPPE-2026-000001"
    val membreId: String,
    val montantVerse: Double, // Montant réel encaissé en caisse
    val montantRemise: Double = 0.0, // Remise accordée
    val montantTotalImpute: Double, // montantVerse + montantRemise
    val modePaiement: PaymentMode,
    val datePaiement: Long = System.currentTimeMillis(),
    val referenceExterne: String = "",
    val observation: String = "",
    val enregistreParUserId: String,
    val enregistreParNom: String,
    val valide: Boolean = true // Annulation possible par Admin, jamais de suppression physique par Trésorerie
)

@Entity(
    tableName = "paiement_echeance_allocations",
    indices = [
        Index(value = ["paiementId"]),
        Index(value = ["echeanceId"])
    ]
)
data class PaiementEcheanceCrossRef(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val paiementId: String,
    val echeanceId: String,
    val partPayee: Double,
    val partRemise: Double
)

@Entity(
    tableName = "cotisations_exceptionnelles",
    indices = [Index(value = ["tenantId", "statut"])]
)
data class CotisationExceptionnelleEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val anneeId: String,
    val titre: String, // ex: "Récollection OPPE 2026"
    val description: String,
    val objectifFinancier: Double,
    val montantRecommande: Double = 0.0,
    val dateDebut: Long,
    val dateFin: Long,
    val statut: CampaignStatus = CampaignStatus.EN_COURS
)

@Entity(
    tableName = "participations_exceptionnelles",
    indices = [
        Index(value = ["tenantId", "campagneId"]),
        Index(value = ["membreId"])
    ]
)
data class ParticipationExceptionnelleEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val campagneId: String,
    val membreId: String,
    val montantVerse: Double,
    val montantRemise: Double = 0.0,
    val dateParticipation: Long = System.currentTimeMillis(),
    val modePaiement: PaymentMode,
    val numeroRecu: String,
    val enregistreParUserId: String,
    val enregistreParNom: String,
    val observation: String = ""
)

@Entity(
    tableName = "categories_depenses",
    indices = [Index(value = ["tenantId", "libelle"])]
)
data class CategorieDepenseEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val libelle: String, // Transport, Nourriture, Communication, Impression, Matériel, Activité, Aide, Autre
    val icone: String = "category",
    val actif: Boolean = true
)

@Entity(
    tableName = "depenses",
    indices = [
        Index(value = ["tenantId", "anneeId", "dateDepense"]),
        Index(value = ["categorieId"]),
        Index(value = ["activiteId"])
    ]
)
data class DepenseEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val anneeId: String,
    val categorieId: String,
    val activiteId: String? = null,
    val libelle: String,
    val montant: Double,
    val dateDepense: Long = System.currentTimeMillis(),
    val modePaiement: PaymentMode,
    val justificatifRef: String = "",
    val enregistreParUserId: String,
    val enregistreParNom: String,
    val valide: Boolean = true,
    val observation: String = ""
)

@Entity(
    tableName = "mouvements_caisse",
    indices = [
        Index(value = ["tenantId", "anneeId", "dateMouvement"]),
        Index(value = ["referenceSource"])
    ]
)
data class MouvementCaisseEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val anneeId: String,
    val dateMouvement: Long = System.currentTimeMillis(),
    val type: CashMovementType, // ENTREE ou SORTIE
    val categorie: String,
    val montant: Double, // Montant réel entré ou sorti
    val libelle: String,
    val referenceSource: String, // ID paiement, participation ou dépense
    val typeSource: CashMovementSource,
    val enregistreParUserId: String,
    val enregistreParNom: String,
    val observation: String = ""
)

@Entity(
    tableName = "activites",
    indices = [Index(value = ["tenantId", "anneeId", "dateDebut"])]
)
data class ActiviteEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val anneeId: String,
    val titre: String,
    val typeActivite: ActivityType,
    val description: String,
    val dateDebut: Long,
    val lieu: String,
    val responsableNom: String,
    val budgetPrevisionnel: Double = 0.0,
    val statut: ActivityStatus = ActivityStatus.PLANIFIEE
)

@Entity(
    tableName = "notifications",
    indices = [Index(value = ["tenantId", "lu"])]
)
data class NotificationEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val titre: String,
    val message: String,
    val type: NotificationType,
    val dateCreation: Long = System.currentTimeMillis(),
    val lu: Boolean = false
)

@Entity(
    tableName = "audit_logs",
    indices = [Index(value = ["tenantId", "dateHeure"])]
)
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val userId: String,
    val userNom: String,
    val action: AuditAction,
    val module: String,
    val elementId: String,
    val dateHeure: Long = System.currentTimeMillis(),
    val details: String,
    val ancienneValeur: String? = null,
    val nouvelleValeur: String? = null
)
