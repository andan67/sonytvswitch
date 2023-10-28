package org.andan.android.tvbrowser.sonycontrolplugin.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyChannel
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyControlRepository
import org.andan.android.tvbrowser.sonycontrolplugin.ui.EventMessage
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

    private val _channelMapUiState = MutableStateFlow(ChannelMapUiState(isLoading = false))
    val channelMapUiState: StateFlow<ChannelMapUiState> = _channelMapUiState.asStateFlow()

    private val activeControlFlow = sonyControlRepository.activeSonyControlWithChannels

    private val filterFlow = MutableStateFlow("")

    var filter: String
        get() = filterFlow.value
        set(value) {
            filterFlow.value = value
        }

    val filteredChannelMap =
        activeControlFlow.combine(filterFlow.
        debounce(500)) { activeControl, filter ->
            activeControl.channelMap.filter { channelMap ->
                channelMap.key.contains(filter, true)
            }.mapValues {activeControl.uriSonyChannelMap[it.key]}
        }.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), emptyMap<String, SonyChannel>())

    private var uiState: ChannelMapUiState
        get() = _channelMapUiState.value
        set(newState) {
            _channelMapUiState.update { newState }
        }

    init {
        viewModelScope.launch {
            activeControlFlow.collect { sonyControl ->
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