package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.andan.android.tvbrowser.sonycontrolplugin.repository.PreferenceRepository
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.navigation.NavDestinations

@AndroidEntryPoint
class SonyControlMainActivity : ComponentActivity() {

  @Inject lateinit var prefRepository: PreferenceRepository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val startDestination =
        if (intent.getBooleanExtra("startedFromTVBrowser", false)) NavDestinations.ChannelMap.route
        else runBlocking { prefRepository.startScreenFlow.first() }
    setContent { SonyControlApp(startDestination, this::finish) }
  }
}
