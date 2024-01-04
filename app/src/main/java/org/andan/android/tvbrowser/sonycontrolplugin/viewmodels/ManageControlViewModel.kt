package org.andan.android.tvbrowser.sonycontrolplugin.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus
import org.andan.android.tvbrowser.sonycontrolplugin.network.Resource
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyControlRepository
import org.andan.android.tvbrowser.sonycontrolplugin.ui.EventMessage
import org.andan.android.tvbrowser.sonycontrolplugin.ui.IntEventMessage
import org.andan.android.tvbrowser.sonycontrolplugin.ui.StringEventMessage
import timber.log.Timber
import javax.inject.Inject

data class ManageControlUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val message: EventMessage? = null,
    val sonyControl: SonyControl? = null
    //val isHostAvailable: Boolean = false,
    //val registrationStatus: Int = RegistrationStatus.REGISTRATION_SUCCESSFUL
)

@HiltViewModel
class ManageControlViewModel @Inject constructor(private val sonyControlRepository: SonyControlRepository) :
    ViewModel() {
    //TODO Inject repository

    val activeSonyControlFlow = sonyControlRepository.activeSonyControlFlow

    private val _manageControlUiState = MutableStateFlow(ManageControlUiState(isLoading = false))
    val manageControlUiState: StateFlow<ManageControlUiState> = _manageControlUiState.asStateFlow()

    private var uiState: ManageControlUiState
        get() = _manageControlUiState.value
        set(newState) {
            _manageControlUiState.update { newState }
        }

    init {
        viewModelScope.launch {
            activeSonyControlFlow.collect { sonyControl ->
                uiState = uiState.copy(sonyControl = sonyControl)
                Timber.d("collect flow $sonyControl")
            }
        }
    }

    fun fetchChannelList() {
        if (uiState.isLoading) return
        viewModelScope.launch(Dispatchers.IO) {
            uiState = uiState.copy(isLoading = true, isSuccess = false)
            val isSuccessResponse = sonyControlRepository.fetchChannelList()
            when (isSuccessResponse) {
                true -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        isSuccess = true,
                        message = StringEventMessage("Fetched ${sonyControlRepository.activeSonyControlState.value.channelList.size} channels from TV")
                    )
                }

                false -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        isSuccess = false,
                        message = StringEventMessage("Failed to fetch channels from TV")
                    )
                }
            }
        }
    }

    fun registerControl() {
        if (uiState.isLoading) return
        sonyControlRepository.getSelectedControl().let {
            viewModelScope.launch {
                uiState = uiState.copy(isLoading = true, isSuccess = false)
                viewModelScope.launch(Dispatchers.IO) {
                    val registrationStatus = sonyControlRepository.registerControl(
                        sonyControlRepository.getSelectedControl()!!,
                        null
                    )
                    when (registrationStatus.code) {
                        RegistrationStatus.REGISTRATION_SUCCESSFUL -> {
                            uiState = uiState.copy(
                                isLoading = false,
                                isSuccess = true,
                                message = StringEventMessage("Registration succeeded")
                            )
                        }

                        else -> {
                            uiState = uiState.copy(
                                isLoading = false,
                                isSuccess = false,
                                message = StringEventMessage("Registration failed")
                            )
                        }
                    }
                }
            }
        }
    }

    fun wakeOnLan() = viewModelScope.launch(Dispatchers.IO) {
        sonyControlRepository.wakeOnLan()
    }

    fun checkAvailability() {
        Timber.d("checkAvailability: $sonyControlRepository ${sonyControlRepository.getSelectedControl()}")
        if (uiState.isLoading) return
        sonyControlRepository.getSelectedControl()!!.ip.let {
            viewModelScope.launch {
                uiState = uiState.copy(isLoading = true, isSuccess = false)
                val response =
                    sonyControlRepository.getPowerStatus(sonyControlRepository.getSelectedControl()!!.ip)
                when (response) {
                    is Resource.Success -> {
                        uiState = uiState.copy(
                            isLoading = false,
                            isSuccess = true,
                            message = IntEventMessage(R.string.add_control_host_success_msg)
                        )
                    }

                    is Resource.Error -> {
                        uiState = uiState.copy(
                            isLoading = false,
                            isSuccess = false,
                            message = IntEventMessage(R.string.add_control_host_failed_msg)
                        )
                    }

                    else -> {}
                }
            }
        }
    }

    fun deleteControl() {
        uiState.sonyControl?.let {
            viewModelScope.launch {
                sonyControlRepository.deleteControl(it)
            }
        }
    }

    fun onConsumedMessage() {
        uiState = uiState.copy(message = null)
        Timber.d("message consumed")
    }
}