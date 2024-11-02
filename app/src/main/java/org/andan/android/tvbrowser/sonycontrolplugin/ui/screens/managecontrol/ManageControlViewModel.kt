package org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.managecontrol

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
import org.andan.android.tvbrowser.sonycontrolplugin.ProcessingEvent
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus
import org.andan.android.tvbrowser.sonycontrolplugin.network.Result
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyControlRepository
import org.andan.android.tvbrowser.sonycontrolplugin.ui.EventMessage
import org.andan.android.tvbrowser.sonycontrolplugin.ui.IntEventMessage
import org.andan.android.tvbrowser.sonycontrolplugin.ui.StringEventMessage
import timber.log.Timber

data class ManageControlUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val message: EventMessage? = null
)

@HiltViewModel
class ManageControlViewModel
@Inject
constructor(private val sonyControlRepository: SonyControlRepository) : ViewModel() {

  val activeControlStateFlow = sonyControlRepository.activeSonyControlStateFlow

  val eventMessageStateFlow = sonyControlRepository.eventMessageStateFlow

  private val _manageControlUiState = MutableStateFlow(ManageControlUiState(isLoading = false))
  val manageControlUiState: StateFlow<ManageControlUiState> = _manageControlUiState.asStateFlow()

  private var uiState: ManageControlUiState
    get() = _manageControlUiState.value
    set(newState) {
      _manageControlUiState.update { newState }
    }

  init {
    Timber.d("init")
    viewModelScope.launch {
      eventMessageStateFlow.collect { processingEvent ->
        when (processingEvent) {
          is ProcessingEvent.PostAddedFetchesPerformed ->
              uiState =
                  uiState.copy(
                      isSuccess = true, message = StringEventMessage(processingEvent.message))
          else -> {}
        }
      }
    }
  }

  fun fetchAllData() {
    Timber.d("fetchAllData: ${uiState.isLoading}")
    if (uiState.isLoading) return
    viewModelScope.launch() {
      Timber.d("fetchAllData: start")
      sonyControlRepository.fetchSystemInformation()
      sonyControlRepository.fetchWolMode()
      Timber.d("fetchAllData: end")
    }
  }

  fun fetchChannelList() {
    if (uiState.isLoading) return
    viewModelScope.launch() {
      uiState = uiState.copy(isLoading = true, isSuccess = false)
      val nFetchedChannels = sonyControlRepository.fetchChannelList()
      uiState =
          if (nFetchedChannels >= 0) {
            uiState.copy(
                isLoading = false,
                isSuccess = true,
                message = StringEventMessage("Fetched $nFetchedChannels channels from TV"))
          } else {
            uiState.copy(
                isLoading = false,
                isSuccess = false,
                message = StringEventMessage("Failed to fetch channels from TV"))
          }
    }
  }

  fun registerControl() {
    if (uiState.isLoading || activeControlStateFlow.value == null) return
    viewModelScope.launch(Dispatchers.IO) {
      uiState = uiState.copy(isLoading = true, isSuccess = false)
      val registrationStatus =
          sonyControlRepository.registerControl(activeControlStateFlow.value!!, null)
      when (registrationStatus.code) {
        RegistrationStatus.REGISTRATION_SUCCESSFUL -> {
          uiState =
              uiState.copy(
                  isLoading = false,
                  isSuccess = true,
                  message = StringEventMessage("Registration succeeded"))
        }
        else -> {
          uiState =
              uiState.copy(
                  isLoading = false,
                  isSuccess = false,
                  message = StringEventMessage("Registration failed"))
        }
      }
    }
  }

  fun wakeOnLan() = viewModelScope.launch(Dispatchers.IO) { sonyControlRepository.wakeOnLan() }

  fun checkAvailability() {
    if (uiState.isLoading || activeControlStateFlow.value == null) return
    viewModelScope.launch {
      uiState = uiState.copy(isLoading = true, isSuccess = false)
      val response = sonyControlRepository.fetchPowerStatus(activeControlStateFlow.value!!.ip)
      when (response) {
        is Result.Success -> {
          uiState =
              uiState.copy(
                  isLoading = false,
                  isSuccess = true,
                  message = IntEventMessage(R.string.add_control_host_success_msg))
        }
        is Result.Error -> {
          uiState =
              uiState.copy(
                  isLoading = false,
                  isSuccess = false,
                  message = IntEventMessage(R.string.add_control_host_failed_msg))
        }
        else -> {}
      }
    }
  }

  fun deleteControl() {
    activeControlStateFlow.value.let {
      viewModelScope.launch { sonyControlRepository.deleteControl(it) }
    }
  }

  fun onConsumedMessage() {
    uiState = uiState.copy(message = null)
    Timber.d("message consumed")
  }
}
