package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.*
import com.example.data.model.*

@Database(
    entities = [
        TenantEntity::class,
        UserEntity::class,
        AnneePastoraleEntity::class,
        ProfilCotisationEntity::class,
        MembreEntity::class,
        EcheanceCotisationEntity::class,
        PaiementEntity::class,
        PaiementEcheanceCrossRef::class,
        CotisationExceptionnelleEntity::class,
        ParticipationExceptionnelleEntity::class,
        CategorieDepenseEntity::class,
        DepenseEntity::class,
        MouvementCaisseEntity::class,
        ActiviteEntity::class,
        NotificationEntity::class,
        AuditLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tenantDao(): TenantDao
    abstract fun userDao(): UserDao
    abstract fun anneePastoraleDao(): AnneePastoraleDao
    abstract fun profilCotisationDao(): ProfilCotisationDao
    abstract fun membreDao(): MembreDao
    abstract fun echeanceDao(): EcheanceDao
    abstract fun paiementDao(): PaiementDao
    abstract fun cotisationExceptionnelleDao(): CotisationExceptionnelleDao
    abstract fun categorieDepenseDao(): CategorieDepenseDao
    abstract fun depenseDao(): DepenseDao
    abstract fun caisseDao(): CaisseDao
    abstract fun activiteDao(): ActiviteDao
    abstract fun notificationDao(): NotificationDao
    abstract fun auditDao(): AuditDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oppe_gestion_saas.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
