package com.example.domain.session

import com.example.data.model.TenantEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SessionState(
    val currentUser: UserEntity? = null,
    val currentTenant: TenantEntity? = null,
    val activePastoralYearId: String? = null,
    val activePastoralYearLibelle: String = "2026-2027",
    val isAuthenticated: Boolean = false
) {
    val isSuperAdmin: Boolean
        get() = currentUser?.role == UserRole.SUPER_ADMIN

    val isAdmin: Boolean
        get() = currentUser?.role == UserRole.ADMINISTRATEUR

    val isTresorier: Boolean
        get() = currentUser?.role == UserRole.TRESORERIE

    val isResponsable: Boolean
        get() = currentUser?.role == UserRole.RESPONSABLE

    fun canRecordFinances(): Boolean {
        return currentUser?.role in listOf(UserRole.ADMINISTRATEUR, UserRole.TRESORERIE)
    }

    fun canDeleteMember(): Boolean {
        // Seul l'administrateur peut supprimer un membre non financier
        return currentUser?.role == UserRole.ADMINISTRATEUR
    }

    fun canManageSettings(): Boolean {
        return currentUser?.role == UserRole.ADMINISTRATEUR
    }

    fun canManageUsers(): Boolean {
        return currentUser?.role in listOf(UserRole.SUPER_ADMIN, UserRole.ADMINISTRATEUR)
    }

    fun canAuditLogs(): Boolean {
        return currentUser?.role in listOf(UserRole.ADMINISTRATEUR, UserRole.TRESORERIE, UserRole.RESPONSABLE)
    }
}

object SessionManager {
    private val _session = MutableStateFlow(SessionState())
    val session: StateFlow<SessionState> = _session.asStateFlow()

    fun login(user: UserEntity, tenant: TenantEntity?, activeYearId: String?, activeYearLibelle: String = "2026-2027") {
        _session.value = SessionState(
            currentUser = user,
            currentTenant = tenant,
            activePastoralYearId = activeYearId,
            activePastoralYearLibelle = activeYearLibelle,
            isAuthenticated = true
        )
    }

    fun switchActiveYear(yearId: String, libelle: String) {
        _session.value = _session.value.copy(
            activePastoralYearId = yearId,
            activePastoralYearLibelle = libelle
        )
    }

    fun logout() {
        _session.value = SessionState()
    }
}
