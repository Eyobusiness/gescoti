package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.domain.rules.DuesAllocationEngine
import com.example.domain.rules.ReceiptNumberGenerator
import java.util.UUID

class FinanceRepository(private val db: AppDatabase) {

    private val echeanceDao = db.echeanceDao()
    private val paiementDao = db.paiementDao()
    private val caisseDao = db.caisseDao()
    private val depenseDao = db.depenseDao()
    private val participationDao = db.cotisationExceptionnelleDao()
    private val auditDao = db.auditDao()
    private val notificationDao = db.notificationDao()
    private val membreDao = db.membreDao()

    /**
     * Enregistre un paiement de cotisation mensuelle.
     * Règle 1 : Montant peut être partiel.
     * Règle 2 : Paiement peut couvrir plusieurs mois.
     * Règle 3 : Affectation automatique FIFO (échéances les plus anciennes).
     * Règle 4 : Remise possible et tracée distinctement.
     * Règle 5 : Entrée de caisse automatique = UNIQUEMENT le montant réellement versé.
     * Règle 6 : Audit log immuable.
     */
    suspend fun enregistrerPaiementCotisation(
        tenantId: String,
        anneeId: String,
        anneeLibelle: String,
        tenantSigle: String,
        membreId: String,
        montantVerse: Double,
        montantRemise: Double,
        modePaiement: PaymentMode,
        referenceExterne: String,
        observation: String,
        userId: String,
        userNom: String
    ): Result<PaiementEntity> {
        return try {
            val membre = membreDao.getMembreById(tenantId, membreId)
                ?: return Result.failure(IllegalArgumentException("Membre introuvable"))

            val totalImpute = montantVerse + montantRemise
            if (totalImpute <= 0.0) {
                return Result.failure(IllegalArgumentException("Le montant versé ou la remise doit être supérieur à zéro"))
            }

            db.withTransaction {
                // 1. Récupération des échéances triées chronologiquement
                val echeances = echeanceDao.getEcheancesByMembre(tenantId, membreId, anneeId)
                    .sortedBy { it.moisIndex }

                val paiementId = UUID.randomUUID().toString()
                val sequence = paiementDao.countPaiements(tenantId) + 1
                val numeroRecu = ReceiptNumberGenerator.generate(tenantSigle, anneeLibelle, sequence)

                // 2. Allocation FIFO
                val allocation = DuesAllocationEngine.allouerPaiement(
                    echeancesTrieesParMois = echeances,
                    montantVerse = montantVerse,
                    montantRemise = montantRemise,
                    paiementId = paiementId
                )

                // 3. Mise à jour des échéances en base
                if (allocation.updatedEcheances.isNotEmpty()) {
                    echeanceDao.updateEcheances(allocation.updatedEcheances)
                }

                // 4. Enregistrement des lignes d'allocation
                if (allocation.crossRefs.isNotEmpty()) {
                    paiementDao.insertAllocations(allocation.crossRefs)
                }

                // 5. Création de l'entité Paiement
                val paiement = PaiementEntity(
                    id = paiementId,
                    tenantId = tenantId,
                    anneeId = anneeId,
                    numeroRecu = numeroRecu,
                    membreId = membreId,
                    montantVerse = montantVerse,
                    montantRemise = montantRemise,
                    montantTotalImpute = totalImpute,
                    modePaiement = modePaiement,
                    datePaiement = System.currentTimeMillis(),
                    referenceExterne = referenceExterne,
                    observation = observation,
                    enregistreParUserId = userId,
                    enregistreParNom = userNom,
                    valide = true
                )
                paiementDao.insertPaiement(paiement)

                // 6. Automatisation Caisse : Seul le montant réellement versé entre en caisse physique
                if (montantVerse > 0.0) {
                    val mouvementCaisse = MouvementCaisseEntity(
                        id = UUID.randomUUID().toString(),
                        tenantId = tenantId,
                        anneeId = anneeId,
                        dateMouvement = System.currentTimeMillis(),
                        type = CashMovementType.ENTREE,
                        categorie = "Cotisations Mensuelles",
                        montant = montantVerse,
                        libelle = "Cotisation $numeroRecu - ${membre.nom} ${membre.prenoms}",
                        referenceSource = paiementId,
                        typeSource = CashMovementSource.COTISATION_MENSUELLE,
                        enregistreParUserId = userId,
                        enregistreParNom = userNom,
                        observation = if (montantRemise > 0.0) "Remise accordée: $montantRemise F. $observation" else observation
                    )
                    caisseDao.insertMouvement(mouvementCaisse)
                }

                // 7. Audit Log immuable
                val detailsAudit = "Paiement ${numeroRecu}: Versé = ${montantVerse} F, Remise = ${montantRemise} F pour ${membre.nom} ${membre.prenoms} (${modePaiement.name})"
                auditDao.insertLog(
                    AuditLogEntity(
                        id = UUID.randomUUID().toString(),
                        tenantId = tenantId,
                        userId = userId,
                        userNom = userNom,
                        action = AuditAction.CREATION,
                        module = "COTISATIONS",
                        elementId = paiementId,
                        details = detailsAudit,
                        nouvelleValeur = "$montantVerse F"
                    )
                )

                // 8. Notification système
                notificationDao.insertNotification(
                    NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        tenantId = tenantId,
                        titre = "Paiement Reçu ($numeroRecu)",
                        message = "${membre.nom} ${membre.prenoms} a versé ${montantVerse} F (${modePaiement.name}).",
                        type = NotificationType.PAIEMENT_ENREGISTRE
                    )
                )

                Result.success(paiement)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Enregistre une participation à une cotisation exceptionnelle.
     * Génère automatiquement une entrée de caisse et un audit log.
     */
    suspend fun enregistrerParticipationExceptionnelle(
        tenantId: String,
        anneeId: String,
        anneeLibelle: String,
        tenantSigle: String,
        campagneId: String,
        campagneTitre: String,
        membreId: String,
        montantVerse: Double,
        montantRemise: Double,
        modePaiement: PaymentMode,
        observation: String,
        userId: String,
        userNom: String
    ): Result<ParticipationExceptionnelleEntity> {
        return try {
            val membre = membreDao.getMembreById(tenantId, membreId)
                ?: return Result.failure(IllegalArgumentException("Membre introuvable"))

            db.withTransaction {
                val count = participationDao.countParticipations(tenantId) + 1
                val recuNum = ReceiptNumberGenerator.generate("${tenantSigle}-EX", anneeLibelle, count)

                val participation = ParticipationExceptionnelleEntity(
                    id = UUID.randomUUID().toString(),
                    tenantId = tenantId,
                    campagneId = campagneId,
                    membreId = membreId,
                    montantVerse = montantVerse,
                    montantRemise = montantRemise,
                    dateParticipation = System.currentTimeMillis(),
                    modePaiement = modePaiement,
                    numeroRecu = recuNum,
                    enregistreParUserId = userId,
                    enregistreParNom = userNom,
                    observation = observation
                )
                participationDao.insertParticipation(participation)

                // Entrée de caisse automatique
                if (montantVerse > 0.0) {
                    val mouvementCaisse = MouvementCaisseEntity(
                        id = UUID.randomUUID().toString(),
                        tenantId = tenantId,
                        anneeId = anneeId,
                        dateMouvement = System.currentTimeMillis(),
                        type = CashMovementType.ENTREE,
                        categorie = "Cotisations Exceptionnelles",
                        montant = montantVerse,
                        libelle = "Except. $campagneTitre - ${membre.nom} ${membre.prenoms}",
                        referenceSource = participation.id,
                        typeSource = CashMovementSource.COTISATION_EXCEPTIONNELLE,
                        enregistreParUserId = userId,
                        enregistreParNom = userNom,
                        observation = observation
                    )
                    caisseDao.insertMouvement(mouvementCaisse)
                }

                auditDao.insertLog(
                    AuditLogEntity(
                        id = UUID.randomUUID().toString(),
                        tenantId = tenantId,
                        userId = userId,
                        userNom = userNom,
                        action = AuditAction.CREATION,
                        module = "COTISATIONS_EXCEPTIONNELLES",
                        elementId = participation.id,
                        details = "Participation de ${montantVerse} F à $campagneTitre par ${membre.nom}",
                        nouvelleValeur = "$montantVerse F"
                    )
                )

                Result.success(participation)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Enregistre une dépense.
     * Génère automatiquement une sortie de caisse et un audit log.
     */
    suspend fun enregistrerDepense(
        tenantId: String,
        anneeId: String,
        categorieId: String,
        categorieNom: String,
        activiteId: String?,
        libelle: String,
        montant: Double,
        modePaiement: PaymentMode,
        justificatifRef: String,
        observation: String,
        userId: String,
        userNom: String
    ): Result<DepenseEntity> {
        return try {
            if (montant <= 0.0) {
                return Result.failure(IllegalArgumentException("Le montant de la dépense doit être positif"))
            }

            db.withTransaction {
                val depenseId = UUID.randomUUID().toString()
                val depense = DepenseEntity(
                    id = depenseId,
                    tenantId = tenantId,
                    anneeId = anneeId,
                    categorieId = categorieId,
                    activiteId = activiteId,
                    libelle = libelle,
                    montant = montant,
                    dateDepense = System.currentTimeMillis(),
                    modePaiement = modePaiement,
                    justificatifRef = justificatifRef,
                    enregistreParUserId = userId,
                    enregistreParNom = userNom,
                    valide = true,
                    observation = observation
                )
                depenseDao.insertDepense(depense)

                // Sortie de caisse automatique
                val mouvementCaisse = MouvementCaisseEntity(
                    id = UUID.randomUUID().toString(),
                    tenantId = tenantId,
                    anneeId = anneeId,
                    dateMouvement = System.currentTimeMillis(),
                    type = CashMovementType.SORTIE,
                    categorie = categorieNom,
                    montant = montant,
                    libelle = "Dépense: $libelle ($categorieNom)",
                    referenceSource = depenseId,
                    typeSource = CashMovementSource.DEPENSE,
                    enregistreParUserId = userId,
                    enregistreParNom = userNom,
                    observation = observation
                )
                caisseDao.insertMouvement(mouvementCaisse)

                auditDao.insertLog(
                    AuditLogEntity(
                        id = UUID.randomUUID().toString(),
                        tenantId = tenantId,
                        userId = userId,
                        userNom = userNom,
                        action = AuditAction.CREATION,
                        module = "DEPENSES",
                        elementId = depenseId,
                        details = "Dépense de ${montant} F ($libelle) enregistrée par $userNom",
                        nouvelleValeur = "$montant F"
                    )
                )

                Result.success(depense)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Annule une opération financière (Uniquement Administrateur).
     * Les trésoriers et responsables n'ont PAS le droit d'annuler ou de supprimer.
     * Cette méthode applique une contre-passation comptable et un audit log.
     */
    suspend fun annulerPaiementAdmin(
        tenantId: String,
        paiementId: String,
        motif: String,
        adminUserId: String,
        adminUserNom: String
    ): Result<Unit> {
        return try {
            val paiement = paiementDao.getPaiementById(paiementId)
                ?: return Result.failure(IllegalArgumentException("Paiement non trouvé"))

            if (!paiement.valide) {
                return Result.failure(IllegalStateException("Ce paiement est déjà annulé"))
            }

            db.withTransaction {
                // 1. Marquer paiement invalide
                paiementDao.updatePaiement(paiement.copy(valide = false))

                // 2. Annuler les allocations sur les échéances
                val allocations = paiementDao.getAllocationsForPaiement(paiementId)
                for (alloc in allocations) {
                    val echeances = echeanceDao.getEcheancesByMembre(tenantId, paiement.membreId, paiement.anneeId)
                    val echeance = echeances.find { it.id == alloc.echeanceId }
                    if (echeance != null) {
                        val newMontantPaye = maxOf(0.0, echeance.montantPaye - alloc.partPayee)
                        val newMontantRemis = maxOf(0.0, echeance.montantRemis - alloc.partRemise)
                        val newSolde = maxOf(0.0, echeance.montantDu - (newMontantPaye + newMontantRemis))
                        val newStatut = when {
                            newSolde == 0.0 -> DuesStatus.PAYE
                            newMontantPaye > 0.0 || newMontantRemis > 0.0 -> DuesStatus.PARTIELLEMENT_PAYE
                            else -> DuesStatus.IMPAYE
                        }
                        echeanceDao.updateEcheance(
                            echeance.copy(
                                montantPaye = newMontantPaye,
                                montantRemis = newMontantRemis,
                                solde = newSolde,
                                statut = newStatut
                            )
                        )
                    }
                }

                // 3. Contre-passation de caisse (SORTIE pour annuler l'entrée)
                if (paiement.montantVerse > 0.0) {
                    val mouvementAnnulation = MouvementCaisseEntity(
                        id = UUID.randomUUID().toString(),
                        tenantId = tenantId,
                        anneeId = paiement.anneeId,
                        dateMouvement = System.currentTimeMillis(),
                        type = CashMovementType.SORTIE,
                        categorie = "Annulation Paiement",
                        montant = paiement.montantVerse,
                        libelle = "ANNULATION ${paiement.numeroRecu} - Motif: $motif",
                        referenceSource = paiement.id,
                        typeSource = CashMovementSource.AJUSTEMENT_ADMIN,
                        enregistreParUserId = adminUserId,
                        enregistreParNom = adminUserNom,
                        observation = motif
                    )
                    caisseDao.insertMouvement(mouvementAnnulation)
                }

                // 4. Audit Log
                auditDao.insertLog(
                    AuditLogEntity(
                        id = UUID.randomUUID().toString(),
                        tenantId = tenantId,
                        userId = adminUserId,
                        userNom = adminUserNom,
                        action = AuditAction.ANNULATION,
                        module = "COTISATIONS",
                        elementId = paiementId,
                        details = "Annulation administrative du paiement ${paiement.numeroRecu}. Motif: $motif",
                        ancienneValeur = "VALIDE (${paiement.montantVerse} F)",
                        nouvelleValeur = "ANNULE"
                    )
                )

                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Génère automatiquement l'échéancier de 12 mois pour un membre dans une année pastorale.
     * Exemple 2026-2027 : Octobre 2026 à Septembre 2027.
     */
    suspend fun genererEcheances12MoisPourMembre(
        tenantId: String,
        membreId: String,
        anneeId: String,
        anneeLibelle: String,
        montantMensuel: Double
    ) {
        val moisPastoraux = listOf(
            "Octobre", "Novembre", "Décembre",
            "Janvier", "Février", "Mars",
            "Avril", "Mai", "Juin",
            "Juillet", "Août", "Septembre"
        )

        val annees = if (anneeLibelle.contains("-")) {
            anneeLibelle.split("-")
        } else {
            listOf("2026", "2027")
        }
        val annee1 = annees[0].trim()
        val annee2 = if (annees.size > 1) annees[1].trim() else annee1

        val echeances = moisPastoraux.mapIndexed { index, nomMois ->
            val anneeCal = if (index < 3) annee1 else annee2
            val libelleMois = "$nomMois $anneeCal"
            EcheanceCotisationEntity(
                id = UUID.randomUUID().toString(),
                tenantId = tenantId,
                membreId = membreId,
                anneeId = anneeId,
                moisIndex = index + 1,
                libelleMois = libelleMois,
                dateEcheance = System.currentTimeMillis() + (index * 30L * 24 * 3600 * 1000),
                montantDu = montantMensuel,
                montantPaye = 0.0,
                montantRemis = 0.0,
                solde = montantMensuel,
                statut = if (index == 0) DuesStatus.IMPAYE else DuesStatus.A_VENIR
            )
        }

        echeanceDao.insertEcheances(echeances)
    }
}
