package org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.remotecontrol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyControlRepository

@HiltViewModel
class RemoteControlViewModel
@Inject
constructor(private val sonyControlRepository: SonyControlRepository) : ViewModel() {

  private var _activeControl = SonyControl()

  val activeSonyControlFlow = sonyControlRepository.activeSonyControlFlow

  fun sendCommand(command: String) {
    viewModelScope.launch(Dispatchers.IO) {
      sonyControlRepository.sendCommand(_activeControl, command)
    }
  }

  init {
    viewModelScope.launch {
      activeSonyControlFlow.collect { sonyControl -> _activeControl = sonyControl }
    }
  }
}
