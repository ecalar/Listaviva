package com.ecalar.listaviva.ui.onboarding

import androidx.lifecycle.ViewModel
import com.ecalar.listaviva.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    fun completeOnboarding() {
        preferencesRepository.setFirstTimeCompleted()
    }
}