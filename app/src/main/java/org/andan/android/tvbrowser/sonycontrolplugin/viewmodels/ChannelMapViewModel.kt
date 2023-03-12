package org.andan.android.tvbrowser.sonycontrolplugin.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.SonyControlApplication
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus
import org.andan.android.tvbrowser.sonycontrolplugin.network.Resource
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyControlRepository
import org.andan.android.tvbrowser.sonycontrolplugin.ui.EventMessage
import org.andan.android.tvbrowser.sonycontrolplugin.ui.IntEventMessage
import org.andan.android.tvbrowser.sonycontrolplugin.ui.StringEventMessage
import timber.log.Timber
import javax.inject.Inject

data class ChannelMapUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val message: EventMessage? = null,
    val sonyControl:SonyControl? = null
)

@HiltViewModel
class ChannelMapViewModel @Inject constructor(private val sonyControlRepository: SonyControlRepository): ViewModel() {
    //TODO Inject repository

    val selectedSonyControlFlow = sonyControlRepository.selectedSonyControl.asFlow()

    private val _channelMapUiState = MutableStateFlow(ChannelMapUiState(isLoading = false))
    val channelMapUiState: StateFlow<ChannelMapUiState> = _channelMapUiState.asStateFlow()

    private var uiState: ChannelMapUiState
        get() = _channelMapUiState.value
        set(newState) {
            _channelMapUiState.update { newState }
        }

    init {
        viewModelScope.launch {
            selectedSonyControlFlow.collect { sonyControl ->
                uiState = uiState.copy(sonyControl = sonyControl)
                Timber.d("collect flow $sonyControl")
                }
            }
        }

    fun onConsumedMessage() {
        uiState = uiState.copy(message = null)
        Timber.d("message consumed")
    }
}