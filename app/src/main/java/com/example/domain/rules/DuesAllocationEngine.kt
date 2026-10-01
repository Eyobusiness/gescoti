package com.example.domain.rules

import com.example.data.model.DuesStatus
import com.example.data.model.EcheanceCotisationEntity
import com.example.data.model.PaiementEcheanceCrossRef

data class AllocationResult(
    val updatedEcheances: List<EcheanceCotisationEntity>,
    val crossRefs: List<PaiementEcheanceCrossRef>,
    val totalImpute: Double,
    val reliquatRestant: Double
)

object DuesAllocationEngine {

    /**
     * Impute automatiquement le montant versé et la remise sur les échéances les plus anciennes non soldées.
     * Règle 1 : Un paiement peut être partiel (solde reste dû, statut = PARTIELLEMENT_PAYE).
     * Règle 2 : Un paiement peut couvrir plusieurs échéances.
     * Règle 3 : Les paiements sont affectés aux échéances les plus anciennes (FIFO).
     * Règle 4 : La remise s'ajoute au montant versé pour solder l'échéance sans modifier le montant dû originel.
     * Règle 5 : La remise est tracée distinctement du montant versé.
     */
    fun allouerPaiement(
        echeancesTrieesParMois: List<EcheanceCotisationEntity>,
        montantVerse: Double,
        montantRemise: Double,
        paiementId: String
    ): AllocationResult {
        var montantVerseRestant = montantVerse
        var montantRemiseRestant = montantRemise

        val updatedEcheances = mutableListOf<EcheanceCotisationEntity>()
        val crossRefs = mutableListOf<PaiementEcheanceCrossRef>()

        for (echeance in echeancesTrieesParMois) {
            val resteDuSurEcheance = echeance.solde
            if (resteDuSurEcheance <= 0.0) {
                // Déjà totalement soldée
                continue
            }

            if (montantVerseRestant <= 0.0 && montantRemiseRestant <= 0.0) {
                // Plus aucun fond à imputer
                break
            }

            // 1. Imputer d'abord le montant réellement versé sur cette échéance
            val partPayee = minOf(montantVerseRestant, resteDuSurEcheance)
            montantVerseRestant -= partPayee
            val soldeApresPaiement = resteDuSurEcheance - partPayee

            // 2. Si un solde subsiste sur l'échéance et qu'une remise est accordée, imputer la remise
            val partRemise = if (soldeApresPaiement > 0.0 && montantRemiseRestant > 0.0) {
                val remiseAppliquee = minOf(montantRemiseRestant, soldeApresPaiement)
                montantRemiseRestant -= remiseAppliquee
                remiseAppliquee
            } else {
                0.0
            }

            val nouveauMontantPaye = echeance.montantPaye + partPayee
            val nouveauMontantRemis = echeance.montantRemis + partRemise
            val nouveauSolde = maxOf(0.0, echeance.montantDu - (nouveauMontantPaye + nouveauMontantRemis))

            val nouveauStatut = when {
                nouveauSolde == 0.0 && nouveauMontantPaye == 0.0 && nouveauMontantRemis > 0.0 -> DuesStatus.SOLDE_PAR_REMISE
                nouveauSolde == 0.0 -> DuesStatus.PAYE
                nouveauMontantPaye > 0.0 || nouveauMontantRemis > 0.0 -> DuesStatus.PARTIELLEMENT_PAYE
                else -> DuesStatus.IMPAYE
            }

            val updatedEcheance = echeance.copy(
                montantPaye = nouveauMontantPaye,
                montantRemis = nouveauMontantRemis,
                solde = nouveauSolde,
                statut = nouveauStatut
            )
            updatedEcheances.add(updatedEcheance)

            if (partPayee > 0.0 || partRemise > 0.0) {
                crossRefs.add(
                    PaiementEcheanceCrossRef(
                        paiementId = paiementId,
                        echeanceId = echeance.id,
                        partPayee = partPayee,
                        partRemise = partRemise
                    )
                )
            }
        }

        val totalImpute = (montantVerse - montantVerseRestant) + (montantRemise - montantRemiseRestant)
        val reliquatNonImpute = montantVerseRestant + montantRemiseRestant

        return AllocationResult(
            updatedEcheances = updatedEcheances,
            crossRefs = crossRefs,
            totalImpute = totalImpute,
            reliquatRestant = reliquatNonImpute
        )
    }
}
