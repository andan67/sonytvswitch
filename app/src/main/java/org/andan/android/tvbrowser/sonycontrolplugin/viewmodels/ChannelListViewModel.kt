package org.andan.android.tvbrowser.sonycontrolplugin.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.domain.PlayingContentInfo
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.network.Resource
import org.andan.android.tvbrowser.sonycontrolplugin.network.asDomainModel
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyControlRepository
import org.andan.android.tvbrowser.sonycontrolplugin.ui.EventMessage
import timber.log.Timber
import javax.inject.Inject

data class ChannelListUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val message: String? = null,
    val playingContentInfo: PlayingContentInfo = PlayingContentInfo()
)

@HiltViewModel
class ChannelListViewModel @Inject constructor(private val sonyControlRepository: SonyControlRepository) :
    ViewModel() {

    private val _channelListUiState = MutableStateFlow(ChannelListUiState())
    val channelListUiState: StateFlow<ChannelListUiState> = _channelListUiState.asStateFlow()

    private var uiState: ChannelListUiState
        get() = _channelListUiState.value
        set(newState) {
            _channelListUiState.update { newState }
        }

    private val activeControlFlow = sonyControlRepository.activeSonyControlWithChannelsFlow

    private val filterFlow = MutableStateFlow("")

    private val mappedChannels = HashMap<String, String>();

    init {
        Timber.d("Init ChannelListViewModel")

    }

    var filter: String
        get() = filterFlow.value
        set(value) {
            filterFlow.value = value
        }

    val filteredChannelList =
        activeControlFlow.combine(
            filterFlow.debounce(500)
        ) { activeControl, filter ->
            Timber.d("filteredChannelList")
            activeControl.channelList.filter { channel ->
                channel.title.contains(filter, true)
            }.map { sonyChannel ->
                Pair(
                    sonyChannel,
                    activeControl.channelReverseMap[sonyChannel.uri]
                )
            }

        }.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), emptyList())

    fun switchToChannel(uri: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if(sonyControlRepository.setPlayContent(uri) == SonyControlRepository.SUCCESS_CODE) {
                fetchPlayingContentInfo()
            }
        }
    }

    fun fetchPlayingContentInfo() = viewModelScope.launch(Dispatchers.IO) {
        uiState = uiState.copy(isLoading = true)
        _channelListUiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val response = sonyControlRepository.getPlayingContentInfo()
            when (response) {
                is Resource.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        playingContentInfo = response.data!!.asDomainModel()
                    )
                }

                is Resource.Error -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        message = response.message
                    )
                }

                else -> {}
            }
        }
    }
}