package org.andan.android.tvbrowser.sonycontrolplugin.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.domain.ChannelNameFuzzyMatch
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyChannel
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyControlRepository
import org.andan.android.tvbrowser.sonycontrolplugin.ui.EventMessage
import timber.log.Timber
import java.util.LinkedHashMap
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

    private val activeControlStateFlow = sonyControlRepository.activeSonyControlWithChannels.stateIn(
        viewModelScope, started = SharingStarted.WhileSubscribed(5000), SonyControl()
    )

    private val filterFlow = MutableStateFlow("")

    private var controlChannelNames: MutableList<String> = ArrayList()

    var filter: String
        get() = filterFlow.value
        set(value) {
            filterFlow.value = value
        }

    val filteredChannelMap =
        activeControlStateFlow.combine(filterFlow.
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
            activeControlStateFlow.collect { sonyControl ->
                controlChannelNames.clear()
                uiState = uiState.copy(sonyControl = sonyControl)
                Timber.d("collect flow $sonyControl")
                sonyControl.channelList.forEach {channel -> controlChannelNames.add(channel.title) }
                }
            }
        }

    internal fun matchChannels() {
        val channelMap = filteredChannelMap.value
        Timber.d("Matching ${channelMap} channels")
        if(channelMap.isNotEmpty()) {
            var channelMatchResult: LinkedHashMap<String, String> =  LinkedHashMap()
            channelMap.keys.forEach {
                val index1 = ChannelNameFuzzyMatch.matchOne(it, controlChannelNames, true)
                if(index1 >= 0) {
                    channelMatchResult[it] = activeControlStateFlow.value.channelList[index1].uri
                    // control.channelMap[channelName] = control.channelList[index1].uri
                }
            }
            viewModelScope.launch {
                sonyControlRepository.saveChannelMap(
                    activeControlStateFlow.value.uuid,
                    channelMatchResult
                )
            }
        }

    }

    fun onConsumedMessage() {
        uiState = uiState.copy(message = null)
        Timber.d("message consumed")
    }
}