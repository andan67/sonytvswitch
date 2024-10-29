package org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.channellist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.domain.PlayingContentInfo
import org.andan.android.tvbrowser.sonycontrolplugin.network.Result
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyControlRepository
import org.andan.android.tvbrowser.sonycontrolplugin.ui.EventMessage
import org.andan.android.tvbrowser.sonycontrolplugin.ui.StringEventMessage
import timber.log.Timber

data class ChannelListUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val message: EventMessage? = null,
    val playingContentInfo: PlayingContentInfo = PlayingContentInfo(),
    val playingTvbChannel: String = ""
)

private const val LAST_URI_KEY = "LAST_URI"
private const val CURRENT_URI_KEY = "CURRENT_URI"

@HiltViewModel
class ChannelListViewModel
@Inject
constructor(
    private val sonyControlRepository: SonyControlRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

  private val _channelListUiState = MutableStateFlow(ChannelListUiState())
  val channelListUiState: StateFlow<ChannelListUiState> = _channelListUiState.asStateFlow()

  private var uiState: ChannelListUiState
    get() = _channelListUiState.value
    set(newState) {
      _channelListUiState.update { newState }
    }

  val activeControlFlow = sonyControlRepository.activeSonyControlStateFlow

  private val filterFlow = MutableStateFlow("")

  init {
    Timber.d("Init ChannelListViewModel")
  }

  var filter: String
    get() = filterFlow.value
    set(value) {
      filterFlow.value = value
    }

  @OptIn(FlowPreview::class)
  val filteredChannelList =
      activeControlFlow
          .filterNotNull()
          .combine(filterFlow.debounce(500)) { activeControl, filter ->
            Timber.d("filteredChannelList")
            activeControl.channelList
                .filter { channel -> channel.title.contains(filter, true) }
                .map { sonyChannel ->
                  Pair(sonyChannel, activeControl.channelReverseMap[sonyChannel.uri])
                }
          }
          .stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), emptyList())

  fun switchToChannel(uri: String) {
    viewModelScope.launch(Dispatchers.IO) {
      when (val result = sonyControlRepository.setPlayContent(uri)) {
        is Result.Success -> {
          fetchPlayingContentInfo()
          if (savedStateHandle.contains(CURRENT_URI_KEY)) {
            savedStateHandle[LAST_URI_KEY] = savedStateHandle.get<String>(CURRENT_URI_KEY)
          }
        }
        is Result.Error -> {
          uiState = uiState.copy(message = StringEventMessage(result.message ?: "'Unknown failure"))
        }
        else -> {}
      }
    }
  }

  fun switchToLastChannel() {
    savedStateHandle.get<String>(LAST_URI_KEY)?.let { switchToChannel(it) }
  }

  fun fetchPlayingContentInfo() =
      viewModelScope.launch(Dispatchers.IO) {
        uiState = uiState.copy(isLoading = true)
        _channelListUiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
          when (val result = sonyControlRepository.fetchPlayingContentInfo()) {
            is Result.Success -> {
              uiState =
                  uiState.copy(
                      isLoading = false, playingContentInfo = result.data!!.toPlayingContentInfo())
              savedStateHandle[CURRENT_URI_KEY] = uiState.playingContentInfo.uri
            }
            is Result.Error -> {
              uiState =
                  uiState.copy(
                      isLoading = false,
                      message = StringEventMessage(result.message ?: "'Unknown failure"))
            }
            else -> {}
          }
        }
      }

  fun setPowerSavingMode(mode: String) =
      viewModelScope.launch(Dispatchers.IO) { sonyControlRepository.setPowerSavingMode(mode) }

  fun wakeOnLan() = viewModelScope.launch(Dispatchers.IO) { sonyControlRepository.wakeOnLan() }

  fun onConsumedMessage() {
    uiState = uiState.copy(message = null)
    Timber.d("message consumed")
  }
}
