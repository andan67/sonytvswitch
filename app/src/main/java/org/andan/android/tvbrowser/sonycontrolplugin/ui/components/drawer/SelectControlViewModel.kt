package org.andan.android.tvbrowser.sonycontrolplugin.ui.components.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyControlRepository
import timber.log.Timber

data class SelectControlUiState(
    val selectedControl: SonyControl? = null,
    val controlList: List<SonyControl> = emptyList()
)

@HiltViewModel
class SelectControlViewModel
@Inject
constructor(private val sonyControlRepository: SonyControlRepository) : ViewModel() {

  private val controlsFlow = sonyControlRepository.sonyControlsStateFlow
  private val activeControlFlow = sonyControlRepository.activeSonyControlMutableStateFlow

  private val _selectControlUiState = MutableStateFlow(SelectControlUiState())
  // val selectControlUiState: StateFlow<SelectControlUiState> = _selectControlUiState.asStateFlow()

  val selectControlUiState =
      combine(activeControlFlow, controlsFlow) { selectedControl, controls ->
            Timber.d("selectControlUiState")
            SelectControlUiState(selectedControl, controls)
          }
          .stateIn(
              viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = SelectControlUiState())

  fun getNumberOfControls() {
    controlsFlow.value.size
  }

  fun hasControls(): Boolean {
    return controlsFlow.value.isNotEmpty()
  }

  fun setActiveControl(uuid: String) {
    viewModelScope.launch { sonyControlRepository.setActiveControl(uuid) }
  }
}
