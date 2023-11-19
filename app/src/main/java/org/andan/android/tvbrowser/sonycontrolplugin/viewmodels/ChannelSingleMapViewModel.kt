package org.andan.android.tvbrowser.sonycontrolplugin.viewmodels

import androidx.lifecycle.SavedStateHandle
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
import javax.inject.Inject
import kotlin.collections.LinkedHashMap

data class ChannelSingleMapUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val message: EventMessage? = null,
    val channelMapItem: Pair<String, SonyChannel?>? = null,
)



@HiltViewModel
class ChannelSingleMapViewModel @Inject constructor(private val sonyControlRepository: SonyControlRepository,
                                                    savedStateHandle: SavedStateHandle
) :
    ViewModel() {

    private val channelKey = checkNotNull(savedStateHandle.get<String>("channelKey"))

    private val _channelSingleMapUiState = MutableStateFlow(ChannelSingleMapUiState(isLoading = false))
    val channelSingleMapUiState: StateFlow<ChannelSingleMapUiState> = _channelSingleMapUiState.asStateFlow()

    private val activeControlStateFlow =
        sonyControlRepository.activeSonyControlWithChannels.stateIn(
            viewModelScope, started = SharingStarted.WhileSubscribed(5000), SonyControl()
        )

    private val filterFlow = MutableStateFlow("")

    var filter: String
        get() = filterFlow.value
        set(value) {
            filterFlow.value = value
        }

    private var controlChannelNames: MutableList<String> = ArrayList()

    val filteredChannelMap =
        activeControlStateFlow.combine(filterFlow.
        debounce(500)) { activeControl, filter ->
            //Timber.d("activeControl.uriSonyChannelMap: ${activeControl.uriSonyChannelMap}")
            activeControl.channelMap.filter { channelMap ->
                channelMap.key.contains(filter, true)
            }.mapValues {
                //Timber.d("it: $it"); Timber.d("activeControl.uriSonyChannelMap[it.value]: ${activeControl.uriSonyChannelMap[it.value]}");
                activeControl.uriSonyChannelMap[it.value]}
        }.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), emptyMap<String, SonyChannel>())


    private var uiState: ChannelSingleMapUiState
        get() = _channelSingleMapUiState.value
        set(newState) {
            _channelSingleMapUiState.update { newState }
        }

    init {
        viewModelScope.launch {
            activeControlStateFlow.collect { sonyControl ->
                controlChannelNames.clear()
                uiState = uiState.copy(channelMapItem =
                    Pair(channelKey, sonyControl.uriSonyChannelMap[sonyControl.channelMap[channelKey]]))
                Timber.d("collect flow $sonyControl")
                sonyControl.channelList.forEach { channel ->
                    controlChannelNames.add(channel.title)
                }
                Timber.d("controlChannelNames: ${controlChannelNames.size}")
                //Timber.d("filteredChannelMap: ${filteredChannelMap.value}")
            }
        }
    }

    internal fun matchChannels() {
        val channelMap = filteredChannelMap.value
        //Timber.d("Matching ${channelMap} channels")
        if (channelMap.isNotEmpty()) {
            viewModelScope.launch {
                val channelMap = filteredChannelMap.value
                //Timber.d("Matching ${channelMap} channels")
                if(channelMap.isNotEmpty()) {
                    val channelMatchResult: LinkedHashMap<String, String> =  LinkedHashMap()
                    channelMap.keys.forEach {
                        val index1 = ChannelNameFuzzyMatch.matchOne(it, controlChannelNames, true)
                        if(index1 >= 0) {
                            channelMatchResult[it] = activeControlStateFlow.value.channelList[index1].uri
                            // control.channelMap[channelName] = control.channelList[index1].uri
                        }
                    }
                    // Timber.d("Matched $channelMatchResult")
                    sonyControlRepository.saveChannelMap(
                        activeControlStateFlow.value.uuid,
                        channelMatchResult
                    )
                }
            }
        }
    }

    internal fun clearChannelMatches() {
        val channelMap = filteredChannelMap.value
        Timber.d("Clearing ${channelMap.size} channel matches")
        if (channelMap.isNotEmpty()) {
            val channelMatchResult: LinkedHashMap<String, String> = LinkedHashMap()
            channelMap.keys.forEach {
                channelMatchResult[it] = ""
            }
            viewModelScope.launch {
/*            val channelMap = filteredChannelMap.value
            Timber.d("Clearing ${channelMap.size} channel matches")
            if(channelMap.isNotEmpty()) {
                val channelMatchResult: LinkedHashMap<String, String> =  LinkedHashMap()
                channelMap.keys.forEach { channelMatchResult[it] = ""
            }*/
                // Timber.d("Cleared $channelMatchResult")
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