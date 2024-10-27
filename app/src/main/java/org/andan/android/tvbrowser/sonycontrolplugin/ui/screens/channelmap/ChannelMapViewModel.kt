package org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.channelmap

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
import org.andan.android.tvbrowser.sonycontrolplugin.domain.ChannelNameFuzzyMatch
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyChannel
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyControlRepository
import org.andan.android.tvbrowser.sonycontrolplugin.ui.EventMessage
import org.andan.android.tvbrowser.sonycontrolplugin.ui.StringEventMessage
import timber.log.Timber

data class ChannelMapUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val message: EventMessage? = null,
    val sonyControl: SonyControl? = null
)

@HiltViewModel
class ChannelMapViewModel
@Inject
constructor(private val sonyControlRepository: SonyControlRepository) : ViewModel() {

  private val _channelMapUiState = MutableStateFlow(ChannelMapUiState(isLoading = false))
  val channelMapUiState: StateFlow<ChannelMapUiState> = _channelMapUiState.asStateFlow()

  val activeControlStateFlow = sonyControlRepository.activeSonyControlStateFlow

  private val filterFlow = MutableStateFlow("")

  var filter: String
    get() = filterFlow.value
    set(value) {
      filterFlow.value = value
    }

  // private var controlChannelNames: MutableList<String> = ArrayList()

  @OptIn(FlowPreview::class)
  val filteredChannelMap =
      activeControlStateFlow
          .filterNotNull()
          .combine(filterFlow.debounce(500)) { activeControl, filter ->
            // Timber.d("activeControl.uriSonyChannelMap: ${activeControl.uriSonyChannelMap}")
            activeControl.channelMap
                .filter { channelMap -> channelMap.key.contains(filter, true) }
                .mapValues { activeControl.uriSonyChannelMap[it.value] }
          }
          .stateIn(
              viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              emptyMap<String, SonyChannel>())

  private var uiState: ChannelMapUiState
    get() = _channelMapUiState.value
    set(newState) {
      _channelMapUiState.update { newState }
    }

  internal fun matchChannels() {
    uiState = uiState.copy(isLoading = true, isSuccess = false)
    val channelMap = filteredChannelMap.value
    Timber.d("Matching $channelMap channels")
    if (channelMap.isNotEmpty()) {
      viewModelScope.launch(Dispatchers.Default) {
        val channelMatchResult =
            ChannelNameFuzzyMatch.matchAll(
                channelMap,
                activeControlStateFlow.value!!.sonyChannelTitleList,
                activeControlStateFlow.value!!.channelList,
                true)
        Timber.d("Matched $channelMatchResult")

        with(Dispatchers.IO) {
          sonyControlRepository.saveChannelMap(
              activeControlStateFlow.value!!.copy(channelMap = channelMatchResult))
        }

        uiState =
            uiState.copy(
                isLoading = false,
                isSuccess = true,
                message = StringEventMessage("Matched ${channelMatchResult.size} channels"))
      }
    }
  }

  internal fun clearChannelMatches() {
    val channelMap = filteredChannelMap.value
    Timber.d("Clearing ${channelMap.size} channel matches")
    if (channelMap.isNotEmpty()) {
      val channelMatchResult: LinkedHashMap<String, String> = LinkedHashMap()
      channelMap.keys.forEach { channelMatchResult[it] = "" }
      viewModelScope.launch {
        sonyControlRepository.saveChannelMap(
            activeControlStateFlow.value!!.copy(channelMap = channelMatchResult))
      }
    }
  }

  internal fun deleteChannelMap() {
    val channelMap = filteredChannelMap.value
    if (channelMap.isNotEmpty()) {
      Timber.d("Deleting ${channelMap.size} channel map items")
      viewModelScope.launch {
        sonyControlRepository.saveChannelMap(
            activeControlStateFlow.value!!.copy(channelMap = emptyMap()))
      }
    }
  }

  fun onConsumedMessage() {
    uiState = uiState.copy(message = null)
    Timber.d("message consumed")
  }
}
