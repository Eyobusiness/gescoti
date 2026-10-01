package com.example.data.model

enum class UserRole {
    SUPER_ADMIN,
    ADMINISTRATEUR,
    RESPONSABLE,
    TRESORERIE
}

enum class MemberStatus {
    ACTIF,
    INACTIF,
    SUSPENDU,
    ANCIEN
}

enum class MemberType {
    ADULTE,
    JEUNE,
    ENFANT,
    BIENFAITEUR,
    HONORIFIQUE
}

enum class DuesStatus {
    A_VENIR,
    IMPAYE,
    PARTIELLEMENT_PAYE,
    PAYE,
    SOLDE_PAR_REMISE
}

enum class PaymentMode {
    ESPECES,
    MOBILE_MONEY,
    VIREMENT,
    CHEQUE,
    AUTRE
}

enum class CashMovementType {
    ENTREE,
    SORTIE
}

enum class CashMovementSource {
    COTISATION_MENSUELLE,
    COTISATION_EXCEPTIONNELLE,
    DEPENSE,
    DON_LIBRE,
    AJUSTEMENT_ADMIN
}

enum class CampaignStatus {
    EN_COURS,
    CLOTUREE,
    SUSPENDUE
}

enum class ActivityType {
    REUNION,
    RECOLLECTION,
    MESSE,
    FORMATION,
    SORTIE,
    ACTIVITE_ENFANTS,
    CELEBRATION,
    EVANGELISATION,
    AUTRE
}

enum class ActivityStatus {
    PLANIFIEE,
    EN_COURS,
    REALISEE,
    ANNULEE
}

enum class AuditAction {
    CREATION,
    MODIFICATION,
    ANNULATION,
    CONNEXION,
    SUPPRESSION_REFUSEE
}

enum class NotificationType {
    RAPPEL_COTISATION,
    PAIEMENT_ENREGISTRE,
    DEPENSE_EFFECTUEE,
    NOUVELLE_ACTIVITE,
    ALERTE_RETARD,
    ANNONCE
}
