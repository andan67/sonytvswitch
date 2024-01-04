package org.andan.android.tvbrowser.sonycontrolplugin.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.domain.ChannelNameFuzzyMatch
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyChannel
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyControlRepository
import org.andan.android.tvbrowser.sonycontrolplugin.ui.EventMessage
import timber.log.Timber
import javax.inject.Inject

data class ChannelSingleMapUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val message: EventMessage? = null,
    val channelMapItem: Pair<String, SonyChannel?>? = null,
)


@HiltViewModel
class ChannelSingleMapViewModel @Inject constructor(
    private val sonyControlRepository: SonyControlRepository,
    savedStateHandle: SavedStateHandle
) :
    ViewModel() {

    private val channelKey = checkNotNull(savedStateHandle.get<String>("channelKey"))

    private val _channelSingleMapUiState =
        MutableStateFlow(ChannelSingleMapUiState(isLoading = false))
    val channelSingleMapUiState: StateFlow<ChannelSingleMapUiState> =
        _channelSingleMapUiState.asStateFlow()

    private val activeControlStateFlow =
        sonyControlRepository.activeSonyControlWithChannelsFlow.stateIn(
            viewModelScope, started = SharingStarted.WhileSubscribed(5000), SonyControl()
        )

    private val filterFlow = MutableStateFlow("")

    var filter: String
        get() = filterFlow.value
        set(value) {
            filterFlow.value = value
        }

    val matchedChannels =
        activeControlStateFlow.combine(
            filterFlow.debounce(500)
        ) { activeControl, filter ->
            matchSingleChannel(activeControl, filter).map {
                //Timber.d("activeControl.uriSonyChannelMap[it] ${activeControl.uriSonyChannelMap[it]}")
                activeControl.uriSonyChannelMap[it]
            }
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            emptyList<SonyChannel>()
        )


    private var uiState: ChannelSingleMapUiState
        get() = _channelSingleMapUiState.value
        set(newState) {
            _channelSingleMapUiState.update { newState }
        }

    init {
        viewModelScope.launch {
            activeControlStateFlow.collect { sonyControl ->
                uiState = uiState.copy(
                    channelMapItem =
                    Pair(
                        channelKey,
                        sonyControl.uriSonyChannelMap[sonyControl.channelMap[channelKey]]
                    )
                )
            }
        }
    }

    internal fun matchSingleChannel(
        activeControl: SonyControl,
        channelTitleFilter: String
    ): ArrayList<String> {
        val channelUriMatchList: ArrayList<String> = ArrayList()
        val channelTitleList = activeControl.sonyChannelTitleList
        if (channelTitleList.isNotEmpty()) {
            val matchTopSet: MutableSet<Int> = LinkedHashSet()
            if (channelTitleFilter.isEmpty()) {
                matchTopSet.addAll(
                    ChannelNameFuzzyMatch.matchTop(
                        channelKey,
                        channelTitleList,
                        30,
                        true
                    )
                )
            } else {
                for (i in channelTitleList.indices) {
                    val channelTitle = channelTitleList[i]
                    if (channelTitle.lowercase().contains(channelTitleFilter.lowercase())) {
                        matchTopSet.add(i)
                        if (matchTopSet.size == 30) break
                    }
                }
            }
            matchTopSet.forEach { channelUriMatchList.add(activeControl.channelList[it].uri) }
        }
        return channelUriMatchList
    }

    internal fun saveNewMap(channel: SonyChannel?) {
        if (channel != null) {
            var channelMap = activeControlStateFlow.value.channelMap.toMutableMap()
            if (channelMap.isNotEmpty()) {
                viewModelScope.launch {
                    channelMap[channelKey] = channel.uri
                    sonyControlRepository.saveChannelMap(
                        activeControlStateFlow.value.uuid,
                        channelMap
                    )
                }
            }
        }
    }

    fun onConsumedMessage() {
        uiState = uiState.copy(message = null)
        Timber.d("message consumed")
    }
}