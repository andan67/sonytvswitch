package org.andan.android.tvbrowser.sonycontrolplugin.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyControlRepository
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class ChannelListViewModel @Inject constructor(private val sonyControlRepository: SonyControlRepository) :
    ViewModel() {

    private val activeControlFlow = sonyControlRepository.activeSonyControlWithChannels

    private val filterFlow = MutableStateFlow("")

    private val mappedChannels = HashMap<String, String>();

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


}