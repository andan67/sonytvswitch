package org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.di.ApplicationScope
import org.andan.android.tvbrowser.sonycontrolplugin.repository.PreferenceRepository
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.navigation.NavDestinations

@HiltViewModel
class SettingsViewModel
@Inject
constructor(
    private val prefRepo: PreferenceRepository,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {

  val startScreenStateFlow =
      prefRepo.startScreenFlow.stateIn(
          viewModelScope,
          started = SharingStarted.WhileSubscribed(5000),
          NavDestinations.RemoteControl.route)

  fun setStartScreen(startDestination: String) {
    applicationScope.launch(Dispatchers.IO) { prefRepo.setStartScreen(startDestination) }
  }
}
