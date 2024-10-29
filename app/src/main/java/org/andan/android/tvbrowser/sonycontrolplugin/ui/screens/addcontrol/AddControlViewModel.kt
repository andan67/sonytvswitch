package org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.addcontrol

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus
import org.andan.android.tvbrowser.sonycontrolplugin.network.Result
import org.andan.android.tvbrowser.sonycontrolplugin.network.SSDP
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyControlRepository
import timber.log.Timber

enum class AddControlStatus {
  SPECIFY_HOST,
  SPECIFY_HOST_NOT_AVAILABE,
  REGISTER,
  REGISTER_ERROR,
  REGISTER_CHALLENGE,
  REGISTER_CHALLENGE_ERROR,
  REGISTER_SUCCESS,
  REGISTER_ERROR_FATAL,
  ADDED
}

data class AddControlUiState(
    val isLoading: Boolean = false,
    val status: AddControlStatus = AddControlStatus.SPECIFY_HOST,
    val messageId: Int? = null,
    val message: String? = null,
    val discoveredDevices: List<SSDP.IpDeviceItem> = emptyList()
)

@HiltViewModel
class AddControlViewModel
@Inject
constructor(private val sonyControlRepository: SonyControlRepository) : ViewModel() {

  private val _addControlUiState = MutableStateFlow(AddControlUiState(isLoading = false))
  val addControlUiState: StateFlow<AddControlUiState> = _addControlUiState.asStateFlow()

  private val status: AddControlStatus
    get() = _addControlUiState.value.status

  private var uiState: AddControlUiState
    get() = _addControlUiState.value
    set(newState) {
      _addControlUiState.update { newState }
    }

  private var addedControl: SonyControl = SonyControl()

  init {
    Timber.d("init")
    viewModelScope.launch(Dispatchers.IO) {
      val discoveredDevices = sonyControlRepository.getSonyIpAndDeviceList()
      Timber.d("discovered ${discoveredDevices.size} devices")
      uiState = uiState.copy(discoveredDevices = discoveredDevices)
    }
  }

  fun checkAvailabilityOfHost(host: String) {
    // Timber.d("checkAvailabilityOfHost")
    _addControlUiState.update { it.copy(isLoading = true) }
    viewModelScope.launch(Dispatchers.IO) {
      val response = sonyControlRepository.fetchPowerStatus(host)
      when (response) {
        is Result.Success -> {
          uiState = uiState.copy(isLoading = false, status = AddControlStatus.REGISTER)
          addedControl = addedControl.copy(ip = host)
        }
        is Result.Error -> {
          uiState =
              uiState.copy(isLoading = false, status = AddControlStatus.SPECIFY_HOST_NOT_AVAILABE)
        }
        else -> {}
      }
    }
  }

  suspend fun fetchSonyIpAndDeviceList() {
    viewModelScope.launch(Dispatchers.IO) {
      uiState = uiState.copy(discoveredDevices = sonyControlRepository.getSonyIpAndDeviceList())
    }
  }

  fun registerControl(
      nickname: String,
      devicename: String,
      preSharedKey: String,
      challengeCode: String
  ) {

    if (status < AddControlStatus.REGISTER) {
      return
    }
    addedControl =
        addedControl.copy(nickname = nickname, devicename = devicename, preSharedKey = preSharedKey)
    _addControlUiState.update { it.copy(isLoading = true) }
    viewModelScope.launch(Dispatchers.IO) {
      val registrationStatus = sonyControlRepository.registerControl(addedControl, challengeCode)
      when (registrationStatus.code) {
        RegistrationStatus.REGISTRATION_REQUIRES_CHALLENGE_CODE -> {
          uiState =
              uiState.copy(
                  isLoading = false,
                  status = AddControlStatus.REGISTER_CHALLENGE,
                  messageId = R.string.dialog_enter_challenge_code_title)
        }
        RegistrationStatus.REGISTRATION_SUCCESSFUL -> {
          uiState = uiState.copy(isLoading = false, status = AddControlStatus.REGISTER_SUCCESS)
        }
        RegistrationStatus.REGISTRATION_UNAUTHORIZED -> {
          uiState =
              uiState.copy(
                  isLoading = false,
                  status = AddControlStatus.REGISTER_ERROR,
                  messageId = R.string.add_control_register_unauthorized_challenge_message)
        }
        RegistrationStatus.REGISTRATION_ERROR_NON_FATAL -> {
          uiState =
              uiState.copy(
                  isLoading = false,
                  status =
                      if (status >= AddControlStatus.REGISTER_CHALLENGE)
                          AddControlStatus.REGISTER_CHALLENGE_ERROR
                      else AddControlStatus.REGISTER_ERROR,
                  message = registrationStatus.message)
        }
        RegistrationStatus.REGISTRATION_ERROR_FATAL -> {
          uiState =
              uiState.copy(
                  isLoading = false,
                  status = AddControlStatus.REGISTER_ERROR_FATAL,
                  message = registrationStatus.message)
        }
      }

      fun onMessageConsumed() {
        uiState = uiState.copy(message = null, messageId = null)
      }
    }
  }

  fun addControl(performFetches: Boolean = true) {
    viewModelScope.launch() {
      sonyControlRepository.addControl(addedControl, performFetches)
      uiState = uiState.copy(status = AddControlStatus.ADDED)
    }
    /*    externalScope.launch(Dispatchers.IO) {
      sonyControlRepository.addControl(addedControl)
      if (performFetches) {
        sonyControlRepository.fetchSystemInformation()
        sonyControlRepository.fetchRemoteControllerInfo()
        sonyControlRepository.setWolMode(true)
        sonyControlRepository.fetchWolMode()
        sonyControlRepository.fetchChannelList()
      }
    }*/
  }

  fun addSampleControl(context: Context, host: String) {
    addedControl =
        SonyControl.fromJson(
            context.assets
                .open("SonyControl_sample.json")
                .bufferedReader()
                .use { it.readText() }
                .replace("android sample", host)
                .replace("sample uuid", UUID.randomUUID().toString()))
    addControl(false)
  }
}
