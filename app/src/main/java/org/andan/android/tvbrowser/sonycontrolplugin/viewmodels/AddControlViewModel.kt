package org.andan.android.tvbrowser.sonycontrolplugin.viewmodels

import android.content.Context
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
import java.util.UUID
import javax.inject.Inject

enum class AddControlStatus() {
    SPECIFY_HOST,
    SPECIFY_HOST_NOT_AVAILABE,
    REGISTER,
    REGISTER_ERROR,
    REGISTER_CHALLENGE,
    REGISTER_CHALLENGE_ERROR,
    REGISTER_SUCCESS,
    REGISTER_ERROR_FATAL
}

data class AddControlUiState(
    val isLoading: Boolean = false,
    val status: AddControlStatus = AddControlStatus.SPECIFY_HOST,
    val messageId: Int? = null,
    val message: String? = null
    //val isHostAvailable: Boolean = false,
    //val registrationStatus: Int = RegistrationStatus.REGISTRATION_SUCCESSFUL
)

@HiltViewModel
class AddControlViewModel  @Inject constructor(private val sonyControlRepository: SonyControlRepository): ViewModel() {

    private val _addControlUiState = MutableStateFlow(AddControlUiState(isLoading = false))
    val addControlUiState: StateFlow<AddControlUiState> = _addControlUiState.asStateFlow()

    private val status: AddControlStatus
        get() = _addControlUiState.value.status

    private var uiState: AddControlUiState
        get() = _addControlUiState.value
        set(newState) {
            _addControlUiState.update { newState }
        }

    var addedControl: SonyControl = SonyControl()

    init {
        Timber.d("init")
    }
    fun checkAvailabilityOfHost(host: String) {
        //Timber.d("checkAvailabilityOfHost")
        _addControlUiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val response = sonyControlRepository.getPowerStatus(host)
            when (response) {
                is Resource.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        //isHostAvailable = true
                        status = AddControlStatus.REGISTER
                    )
                    addedControl.ip = host
                }
                is Resource.Error -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        //isHostAvailable = true
                        status = AddControlStatus.SPECIFY_HOST_NOT_AVAILABE
                    )
                }
                else -> {}
            }
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
        addedControl.nickname = nickname
        addedControl.devicename = devicename
        addedControl.preSharedKey = preSharedKey
        _addControlUiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val registrationStatus = sonyControlRepository.registerControl(addedControl, challengeCode)
            when (registrationStatus.code) {
                RegistrationStatus.REGISTRATION_REQUIRES_CHALLENGE_CODE -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        status = AddControlStatus.REGISTER_CHALLENGE,
                        messageId = R.string.dialog_enter_challenge_code_title
                    )
                }
                RegistrationStatus.REGISTRATION_SUCCESSFUL -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        status = AddControlStatus.REGISTER_SUCCESS
                    )
                }
                RegistrationStatus.REGISTRATION_UNAUTHORIZED -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        status = AddControlStatus.REGISTER_ERROR,
                        messageId = R.string.add_control_register_unauthorized_challenge_message
                    )
                }
                RegistrationStatus.REGISTRATION_ERROR_NON_FATAL -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        status = if (status >= AddControlStatus.REGISTER_CHALLENGE)
                            AddControlStatus.REGISTER_CHALLENGE_ERROR
                        else AddControlStatus.REGISTER_ERROR,
                        message = registrationStatus.message
                    )
                }
                RegistrationStatus.REGISTRATION_ERROR_FATAL -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        status = AddControlStatus.REGISTER_ERROR_FATAL,
                        message = registrationStatus.message
                    )
                }
            }

            fun onMessageConsumed() {
                uiState = uiState.copy(message = null, messageId = null)
            }
        }
    }

    fun addControl() {
        viewModelScope.launch(Dispatchers.IO) {
            sonyControlRepository.addControl(addedControl)
        }
    }

    fun addSampleControl(context: Context, host : String) {
        addedControl = SonyControl.fromJson(
            context.assets.open("SonyControl_sample.json").bufferedReader()
                .use { it.readText() }
                .replace("android sample",host)
                .replace("sample uuid", UUID.randomUUID().toString()))
        addControl()
    }



}