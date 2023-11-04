package org.andan.android.tvbrowser.sonycontrolplugin.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyControlRepository
import org.andan.android.tvbrowser.sonycontrolplugin.ui.EventMessage
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class ChannelListViewModel @Inject constructor(private val sonyControlRepository: SonyControlRepository) :
    ViewModel() {

    private val activeControlFlow = sonyControlRepository.activeSonyControlWithChannels

    private val filterFlow = MutableStateFlow("")

    var filter: String
        get() = filterFlow.value
        set(value) {
            filterFlow.value = value
        }

    val filteredChannelList =
        activeControlFlow.combine(filterFlow.
        debounce(500)) { activeControl, filter ->
            Timber.d("filteredChannelList")
            activeControl.channelList.filter { channel ->
                channel.title.contains(filter, true)
            }

        }.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), emptyList())
}