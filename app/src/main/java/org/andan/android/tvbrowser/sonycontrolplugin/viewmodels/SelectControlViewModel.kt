package org.andan.android.tvbrowser.sonycontrolplugin.viewmodels

import androidx.lifecycle.ViewModel
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
import timber.log.Timber
import javax.inject.Inject

data class SelectControlUiState(
    val selectedControl: SonyControl? = null,
    val controlList:List<SonyControl> = emptyList()
)

@HiltViewModel
class SelectControlViewModel  @Inject constructor(private val sonyControlRepository: SonyControlRepository): ViewModel() {

    private val controlsFlow = sonyControlRepository.sonyControls
    private val selectedControlFlow = sonyControlRepository.selectedSonyControl

    private val _selectControlUiState = MutableStateFlow(SelectControlUiState())
    //val selectControlUiState: StateFlow<SelectControlUiState> = _selectControlUiState.asStateFlow()
    val selectControlUiState = combine(selectedControlFlow, controlsFlow) {selectedControl, controls ->
        SelectControlUiState(selectedControl, controls)
    }.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000),
        initialValue = SelectControlUiState())

}