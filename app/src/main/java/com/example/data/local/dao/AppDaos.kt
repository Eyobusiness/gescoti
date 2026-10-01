package com.example.data.local.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantDao {
    @Query("SELECT * FROM tenants ORDER BY nom ASC")
    fun getAllTenants(): Flow<List<TenantEntity>>

    @Query("SELECT * FROM tenants WHERE id = :tenantId LIMIT 1")
    fun getTenantByIdFlow(tenantId: String): Flow<TenantEntity?>

    @Query("SELECT * FROM tenants WHERE id = :tenantId LIMIT 1")
    suspend fun getTenantById(tenantId: String): TenantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTenant(tenant: TenantEntity)

    @Update
    suspend fun updateTenant(tenant: TenantEntity)

    @Query("SELECT COUNT(*) FROM tenants")
    fun countTenants(): Flow<Int>
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE tenantId = :tenantId ORDER BY nom ASC")
    fun getUsersByTenant(tenantId: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY nom ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)
}

@Dao
interface AnneePastoraleDao {
    @Query("SELECT * FROM annees_pastorales WHERE tenantId = :tenantId ORDER BY dateDebut DESC")
    fun getAnneesByTenant(tenantId: String): Flow<List<AnneePastoraleEntity>>

    @Query("SELECT * FROM annees_pastorales WHERE tenantId = :tenantId AND estActive = 1 LIMIT 1")
    fun getAnneeActiveFlow(tenantId: String): Flow<AnneePastoraleEntity?>

    @Query("SELECT * FROM annees_pastorales WHERE tenantId = :tenantId AND estActive = 1 LIMIT 1")
    suspend fun getAnneeActive(tenantId: String): AnneePastoraleEntity?

    @Query("SELECT * FROM annees_pastorales WHERE id = :id LIMIT 1")
    suspend fun getAnneeById(id: String): AnneePastoraleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnee(annee: AnneePastoraleEntity)

    @Update
    suspend fun updateAnnee(annee: AnneePastoraleEntity)

    @Query("UPDATE annees_pastorales SET estActive = 0 WHERE tenantId = :tenantId")
    suspend fun desactiverToutes(tenantId: String)
}

@Dao
interface ProfilCotisationDao {
    @Query("SELECT * FROM profils_cotisation WHERE tenantId = :tenantId ORDER BY nom ASC")
    fun getProfilsByTenant(tenantId: String): Flow<List<ProfilCotisationEntity>>

    @Query("SELECT * FROM profils_cotisation WHERE id = :id LIMIT 1")
    suspend fun getProfilById(id: String): ProfilCotisationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfil(profil: ProfilCotisationEntity)

    @Update
    suspend fun updateProfil(profil: ProfilCotisationEntity)
}

@Dao
interface MembreDao {
    @Query("SELECT * FROM membres WHERE tenantId = :tenantId ORDER BY nom ASC, prenoms ASC")
    fun getMembresByTenant(tenantId: String): Flow<List<MembreEntity>>

    @Query("SELECT * FROM membres WHERE tenantId = :tenantId AND profilId = :profilId")
    fun getMembresByProfil(tenantId: String, profilId: String): Flow<List<MembreEntity>>

    @Query("SELECT * FROM membres WHERE tenantId = :tenantId AND id = :id LIMIT 1")
    fun getMembreByIdFlow(tenantId: String, id: String): Flow<MembreEntity?>

    @Query("SELECT * FROM membres WHERE tenantId = :tenantId AND id = :id LIMIT 1")
    suspend fun getMembreById(tenantId: String, id: String): MembreEntity?

    @Query("SELECT COUNT(*) FROM membres WHERE tenantId = :tenantId AND statut = 'ACTIF'")
    fun countMembresActifs(tenantId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembre(membre: MembreEntity)

    @Update
    suspend fun updateMembre(membre: MembreEntity)

    @Query("DELETE FROM membres WHERE tenantId = :tenantId AND id = :id")
    suspend fun deleteMembre(tenantId: String, id: String)
}

@Dao
interface EcheanceDao {
    @Query("SELECT * FROM echeances_cotisation WHERE tenantId = :tenantId AND membreId = :membreId AND anneeId = :anneeId ORDER BY moisIndex ASC")
    fun getEcheancesByMembreFlow(tenantId: String, membreId: String, anneeId: String): Flow<List<EcheanceCotisationEntity>>

    @Query("SELECT * FROM echeances_cotisation WHERE tenantId = :tenantId AND membreId = :membreId AND anneeId = :anneeId ORDER BY moisIndex ASC")
    suspend fun getEcheancesByMembre(tenantId: String, membreId: String, anneeId: String): List<EcheanceCotisationEntity>

    @Query("SELECT * FROM echeances_cotisation WHERE tenantId = :tenantId AND anneeId = :anneeId AND solde > 0 ORDER BY moisIndex ASC")
    fun getEcheancesImpayeesByAnnee(tenantId: String, anneeId: String): Flow<List<EcheanceCotisationEntity>>

    @Query("SELECT * FROM echeances_cotisation WHERE tenantId = :tenantId AND anneeId = :anneeId")
    fun getAllEcheancesByAnnee(tenantId: String, anneeId: String): Flow<List<EcheanceCotisationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEcheances(echeances: List<EcheanceCotisationEntity>)

    @Update
    suspend fun updateEcheance(echeance: EcheanceCotisationEntity)

    @Update
    suspend fun updateEcheances(echeances: List<EcheanceCotisationEntity>)
}

@Dao
interface PaiementDao {
    @Query("SELECT * FROM paiements WHERE tenantId = :tenantId AND anneeId = :anneeId ORDER BY datePaiement DESC")
    fun getPaiementsByAnnee(tenantId: String, anneeId: String): Flow<List<PaiementEntity>>

    @Query("SELECT * FROM paiements WHERE tenantId = :tenantId AND membreId = :membreId ORDER BY datePaiement DESC")
    fun getPaiementsByMembre(tenantId: String, membreId: String): Flow<List<PaiementEntity>>

    @Query("SELECT * FROM paiements WHERE id = :id LIMIT 1")
    suspend fun getPaiementById(id: String): PaiementEntity?

    @Query("SELECT * FROM paiements WHERE tenantId = :tenantId AND numeroRecu = :numeroRecu LIMIT 1")
    suspend fun getPaiementByRecu(tenantId: String, numeroRecu: String): PaiementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaiement(paiement: PaiementEntity)

    @Update
    suspend fun updatePaiement(paiement: PaiementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocations(allocations: List<PaiementEcheanceCrossRef>)

    @Query("SELECT * FROM paiement_echeance_allocations WHERE paiementId = :paiementId")
    suspend fun getAllocationsForPaiement(paiementId: String): List<PaiementEcheanceCrossRef>

    @Query("SELECT COUNT(*) FROM paiements WHERE tenantId = :tenantId")
    suspend fun countPaiements(tenantId: String): Int
}

@Dao
interface CotisationExceptionnelleDao {
    @Query("SELECT * FROM cotisations_exceptionnelles WHERE tenantId = :tenantId AND anneeId = :anneeId ORDER BY dateDebut DESC")
    fun getCampagnesByAnnee(tenantId: String, anneeId: String): Flow<List<CotisationExceptionnelleEntity>>

    @Query("SELECT * FROM cotisations_exceptionnelles WHERE id = :id LIMIT 1")
    suspend fun getCampagneById(id: String): CotisationExceptionnelleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampagne(campagne: CotisationExceptionnelleEntity)

    @Update
    suspend fun updateCampagne(campagne: CotisationExceptionnelleEntity)

    @Query("SELECT * FROM participations_exceptionnelles WHERE tenantId = :tenantId AND campagneId = :campagneId ORDER BY dateParticipation DESC")
    fun getParticipationsByCampagne(tenantId: String, campagneId: String): Flow<List<ParticipationExceptionnelleEntity>>

    @Query("SELECT * FROM participations_exceptionnelles WHERE tenantId = :tenantId AND membreId = :membreId ORDER BY dateParticipation DESC")
    fun getParticipationsByMembre(tenantId: String, membreId: String): Flow<List<ParticipationExceptionnelleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipation(participation: ParticipationExceptionnelleEntity)

    @Query("SELECT COUNT(*) FROM participations_exceptionnelles WHERE tenantId = :tenantId")
    suspend fun countParticipations(tenantId: String): Int
}

@Dao
interface CategorieDepenseDao {
    @Query("SELECT * FROM categories_depenses WHERE tenantId = :tenantId ORDER BY libelle ASC")
    fun getCategoriesByTenant(tenantId: String): Flow<List<CategorieDepenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategorie(categorie: CategorieDepenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategorieDepenseEntity>)
}

@Dao
interface DepenseDao {
    @Query("SELECT * FROM depenses WHERE tenantId = :tenantId AND anneeId = :anneeId AND valide = 1 ORDER BY dateDepense DESC")
    fun getDepensesByAnnee(tenantId: String, anneeId: String): Flow<List<DepenseEntity>>

    @Query("SELECT * FROM depenses WHERE tenantId = :tenantId AND activiteId = :activiteId AND valide = 1")
    fun getDepensesByActivite(tenantId: String, activiteId: String): Flow<List<DepenseEntity>>

    @Query("SELECT * FROM depenses WHERE id = :id LIMIT 1")
    suspend fun getDepenseById(id: String): DepenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepense(depense: DepenseEntity)

    @Update
    suspend fun updateDepense(depense: DepenseEntity)
}

@Dao
interface CaisseDao {
    @Query("SELECT * FROM mouvements_caisse WHERE tenantId = :tenantId AND anneeId = :anneeId ORDER BY dateMouvement DESC")
    fun getMouvementsByAnnee(tenantId: String, anneeId: String): Flow<List<MouvementCaisseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMouvement(mouvement: MouvementCaisseEntity)

    @Query("SELECT COALESCE(SUM(CASE WHEN type = 'ENTREE' THEN montant ELSE -montant END), 0.0) FROM mouvements_caisse WHERE tenantId = :tenantId AND anneeId = :anneeId")
    fun getSoldeCaisseFlow(tenantId: String, anneeId: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(CASE WHEN type = 'ENTREE' THEN montant ELSE -montant END), 0.0) FROM mouvements_caisse WHERE tenantId = :tenantId AND anneeId = :anneeId")
    suspend fun getSoldeCaisse(tenantId: String, anneeId: String): Double
}

@Dao
interface ActiviteDao {
    @Query("SELECT * FROM activites WHERE tenantId = :tenantId AND anneeId = :anneeId ORDER BY dateDebut ASC")
    fun getActivitesByAnnee(tenantId: String, anneeId: String): Flow<List<ActiviteEntity>>

    @Query("SELECT * FROM activites WHERE id = :id LIMIT 1")
    suspend fun getActiviteById(id: String): ActiviteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivite(activite: ActiviteEntity)

    @Update
    suspend fun updateActivite(activite: ActiviteEntity)

    @Query("DELETE FROM activites WHERE tenantId = :tenantId AND id = :id")
    suspend fun deleteActivite(tenantId: String, id: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE tenantId = :tenantId ORDER BY dateCreation DESC")
    fun getNotificationsByTenant(tenantId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET lu = 1 WHERE tenantId = :tenantId AND id = :id")
    suspend fun marquerCommeLue(tenantId: String, id: String)
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_logs WHERE tenantId = :tenantId ORDER BY dateHeure DESC")
    fun getLogsByTenant(tenantId: String): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity)
}
