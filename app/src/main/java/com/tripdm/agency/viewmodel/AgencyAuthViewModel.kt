package com.tripdm.agency.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripdm.agency.data.model.AgencyProfile
import com.tripdm.agency.data.repository.AgencyAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AgencyAuthState {
    object Idle : AgencyAuthState()
    object Loading : AgencyAuthState()
    data class Authenticated(val profile: AgencyProfile) : AgencyAuthState()
    data class PendingApproval(val profile: AgencyProfile) : AgencyAuthState()
    object Unauthenticated : AgencyAuthState()
    data class Error(val message: String) : AgencyAuthState()
}

class AgencyAuthViewModel(
    private val repository: AgencyAuthRepository = AgencyAuthRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<AgencyAuthState>(AgencyAuthState.Idle)
    val authState: StateFlow<AgencyAuthState> = _authState.asStateFlow()

    private val _currentProfile = MutableStateFlow<AgencyProfile?>(null)
    val currentProfile: StateFlow<AgencyProfile?> = _currentProfile.asStateFlow()

    init {
        checkCurrentAuth()
    }

    fun checkCurrentAuth() {
        val user = repository.currentFirebaseUser
        if (user != null) {
            viewModelScope.launch {
                _authState.value = AgencyAuthState.Loading
                val profile = repository.fetchAgencyProfile(user.uid)
                if (profile != null) {
                    _currentProfile.value = profile
                    if (profile.approvalStatus == "approved" || profile.approved) {
                        _authState.value = AgencyAuthState.Authenticated(profile)
                    } else {
                        _authState.value = AgencyAuthState.PendingApproval(profile)
                    }
                    observeProfileChanges(user.uid)
                } else {
                    _authState.value = AgencyAuthState.Unauthenticated
                }
            }
        } else {
            _authState.value = AgencyAuthState.Unauthenticated
        }
    }

    private fun observeProfileChanges(uid: String) {
        viewModelScope.launch {
            repository.observeAgencyProfile(uid).collect { profile ->
                if (profile != null) {
                    _currentProfile.value = profile
                    if (profile.approvalStatus == "approved" || profile.approved) {
                        _authState.value = AgencyAuthState.Authenticated(profile)
                    } else {
                        _authState.value = AgencyAuthState.PendingApproval(profile)
                    }
                }
            }
        }
    }

    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AgencyAuthState.Loading
            val result = repository.signIn(email, pass)
            result.fold(
                onSuccess = { profile ->
                    _currentProfile.value = profile
                    if (profile.approvalStatus == "approved" || profile.approved) {
                        _authState.value = AgencyAuthState.Authenticated(profile)
                    } else {
                        _authState.value = AgencyAuthState.PendingApproval(profile)
                    }
                    observeProfileChanges(profile.id)
                },
                onFailure = { err ->
                    _authState.value = AgencyAuthState.Error(err.message ?: "Sign-in failed")
                }
            )
        }
    }

    fun register(profile: AgencyProfile, pass: String) {
        viewModelScope.launch {
            _authState.value = AgencyAuthState.Loading
            val result = repository.registerAgency(profile, pass)
            result.fold(
                onSuccess = { created ->
                    _currentProfile.value = created
                    _authState.value = AgencyAuthState.PendingApproval(created)
                    observeProfileChanges(created.id)
                },
                onFailure = { err ->
                    _authState.value = AgencyAuthState.Error(err.message ?: "Registration failed")
                }
            )
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AgencyAuthState.Loading
            val result = repository.signInWithGoogleCredential(idToken)
            result.fold(
                onSuccess = { profile ->
                    _currentProfile.value = profile
                    if (profile.approvalStatus == "approved" || profile.approved) {
                        _authState.value = AgencyAuthState.Authenticated(profile)
                    } else {
                        _authState.value = AgencyAuthState.PendingApproval(profile)
                    }
                    observeProfileChanges(profile.id)
                },
                onFailure = { err ->
                    _authState.value = AgencyAuthState.Error(err.message ?: "Google Sign-in failed")
                }
            )
        }
    }

    fun signOut() {
        repository.signOut()
        _currentProfile.value = null
        _authState.value = AgencyAuthState.Unauthenticated
    }

    fun clearError() {
        if (_authState.value is AgencyAuthState.Error) {
            _authState.value = AgencyAuthState.Unauthenticated
        }
    }
}
