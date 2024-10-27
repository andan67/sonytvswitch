package org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.remotecontrol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.network.Result
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyControlRepository
import org.andan.android.tvbrowser.sonycontrolplugin.ui.EventMessage
import org.andan.android.tvbrowser.sonycontrolplugin.ui.StringEventMessage
import timber.log.Timber

data class RemoteControlUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val message: EventMessage? = null
)

@HiltViewModel
class RemoteControlViewModel
@Inject
constructor(private val sonyControlRepository: SonyControlRepository) : ViewModel() {

  private val _remoteControlUiState = MutableStateFlow(RemoteControlUiState(isLoading = false))
  val remoteControlUiState: StateFlow<RemoteControlUiState> = _remoteControlUiState.asStateFlow()

  private var uiState: RemoteControlUiState
    get() = _remoteControlUiState.value
    set(newState) {
      _remoteControlUiState.update { newState }
    }

  fun sendCommand(command: String) {
    viewModelScope.launch(Dispatchers.IO) {
      uiState = uiState.copy(isLoading = true, isSuccess = false)
      when (val response = sonyControlRepository.sendCommand(command)) {
        is Result.Success -> {
          uiState = uiState.copy(isLoading = false, isSuccess = true)
        }
        is Result.Error -> {
          uiState =
              uiState.copy(
                  isLoading = false,
                  isSuccess = false,
                  message = StringEventMessage(response.message ?: "Unknown failure"))
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
