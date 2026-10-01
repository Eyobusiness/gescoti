package com.example.domain

import com.example.data.model.*
import com.example.domain.rules.DuesAllocationEngine
import com.example.domain.rules.ReceiptNumberGenerator
import com.example.domain.session.SessionState
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class FinancialBusinessRulesTest {

    private fun createEcheance(moisIndex: Int, montantDu: Double = 500.0): EcheanceCotisationEntity {
        return EcheanceCotisationEntity(
            id = "ech-$moisIndex",
            tenantId = "tenant-001",
            membreId = "membre-001",
            anneeId = "annee-2026",
            moisIndex = moisIndex,
            libelleMois = "Mois $moisIndex",
            dateEcheance = System.currentTimeMillis() + (moisIndex * 30L * 24 * 3600 * 1000),
            montantDu = montantDu,
            montantPaye = 0.0,
            montantRemis = 0.0,
            solde = montantDu,
            statut = DuesStatus.IMPAYE
        )
    }

    @Test
    fun testPaiementPartiel() {
        // Échéance de 500 FCFA, membre paie 300 FCFA
        val echeances = listOf(createEcheance(1, 500.0))
        val result = DuesAllocationEngine.allouerPaiement(
            echeancesTrieesParMois = echeances,
            montantVerse = 300.0,
            montantRemise = 0.0,
            paiementId = "p-01"
        )

        assertEquals(1, result.updatedEcheances.size)
        val ech = result.updatedEcheances[0]
        assertEquals(300.0, ech.montantPaye, 0.001)
        assertEquals(0.0, ech.montantRemis, 0.001)
        assertEquals(200.0, ech.solde, 0.001)
        assertEquals(DuesStatus.PARTIELLEMENT_PAYE, ech.statut)
        assertEquals(300.0, result.totalImpute, 0.001)
        assertEquals(0.0, result.reliquatRestant, 0.001)
    }

    @Test
    fun testPaiementCouvrantPlusieursMoisFIFO() {
        // 4 échéances de 500 FCFA, membre verse 2 000 FCFA
        val echeances = (1..4).map { createEcheance(it, 500.0) }
        val result = DuesAllocationEngine.allouerPaiement(
            echeancesTrieesParMois = echeances,
            montantVerse = 2000.0,
            montantRemise = 0.0,
            paiementId = "p-02"
        )

        assertEquals(4, result.updatedEcheances.size)
        result.updatedEcheances.forEach { ech ->
            assertEquals(500.0, ech.montantPaye, 0.001)
            assertEquals(0.0, ech.solde, 0.001)
            assertEquals(DuesStatus.PAYE, ech.statut)
        }
        assertEquals(2000.0, result.totalImpute, 0.001)
    }

    @Test
    fun testPaiementAnnuelAvecRemise() {
        // 12 mois de 500 FCFA = 6 000 FCFA. Membre paie 5 000 FCFA avec 1 000 FCFA de remise
        val echeances = (1..12).map { createEcheance(it, 500.0) }
        val result = DuesAllocationEngine.allouerPaiement(
            echeancesTrieesParMois = echeances,
            montantVerse = 5000.0,
            montantRemise = 1000.0,
            paiementId = "p-03"
        )

        assertEquals(12, result.updatedEcheances.size)
        // Les 12 échéances doivent avoir un solde de 0
        result.updatedEcheances.forEach { ech ->
            assertEquals(0.0, ech.solde, 0.001)
            assertTrue(ech.statut == DuesStatus.PAYE || ech.statut == DuesStatus.SOLDE_PAR_REMISE)
        }

        // Total payé + remis
        val totalPaye = result.updatedEcheances.sumOf { it.montantPaye }
        val totalRemis = result.updatedEcheances.sumOf { it.montantRemis }
        assertEquals(5000.0, totalPaye, 0.001)
        assertEquals(1000.0, totalRemis, 0.001)
        assertEquals(6000.0, result.totalImpute, 0.001)
        assertEquals(0.0, result.reliquatRestant, 0.001)
    }

    @Test
    fun testGenerateurNumeroRecu() {
        val numero = ReceiptNumberGenerator.generate("OPPE", "2026-2027", 1)
        assertEquals("OPPE-2026-000001", numero)

        val numero15 = ReceiptNumberGenerator.generate("OPPE", "2026", 15)
        assertEquals("OPPE-2026-000015", numero15)
    }

    @Test
    fun testPermissionsRoles() {
        val userTresorier = UserEntity(
            id = "u-tresorier",
            tenantId = "tenant-001",
            nom = "Jean Trésorier",
            email = "tresorier@oppe.ci",
            role = UserRole.TRESORERIE
        )
        val stateTresorier = SessionState(currentUser = userTresorier, isAuthenticated = true)
        assertTrue(stateTresorier.canRecordFinances())
        assertFalse(stateTresorier.canDeleteMember()) // Trésorier NE PEUT PAS supprimer
        assertFalse(stateTresorier.canManageSettings())

        val userAdmin = UserEntity(
            id = "u-admin",
            tenantId = "tenant-001",
            nom = "Père André",
            email = "admin@oppe.ci",
            role = UserRole.ADMINISTRATEUR
        )
        val stateAdmin = SessionState(currentUser = userAdmin, isAuthenticated = true)
        assertTrue(stateAdmin.canRecordFinances())
        assertTrue(stateAdmin.canDeleteMember())
        assertTrue(stateAdmin.canManageSettings())

        val userResponsable = UserEntity(
            id = "u-resp",
            tenantId = "tenant-001",
            nom = "Marie Claire",
            email = "responsable@oppe.ci",
            role = UserRole.RESPONSABLE
        )
        val stateResp = SessionState(currentUser = userResponsable, isAuthenticated = true)
        assertFalse(stateResp.canRecordFinances())
        assertFalse(stateResp.canDeleteMember())
    }
}
