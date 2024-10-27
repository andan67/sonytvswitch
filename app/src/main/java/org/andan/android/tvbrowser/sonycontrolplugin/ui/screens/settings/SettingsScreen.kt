package org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.misc.WebView
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier, openDrawer: () -> Unit) {

  val startScreenPreferenceLabels = stringArrayResource(id = R.array.pref_start_screen_labels)
  val startScreenPreferenceValues = stringArrayResource(id = R.array.pref_start_screen_values)

  val startScreenPreferences = remember {
    startScreenPreferenceValues.zip(startScreenPreferenceLabels).toMap()
  }

  val settingsViewModel: SettingsViewModel = hiltViewModel()

  val startScreenState by settingsViewModel.startScreenStateFlow.collectAsStateWithLifecycle()

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.menu_settings)) },
            navigationIcon = {
              IconButton(onClick = openDrawer) {
                Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer))
              }
            })
      },
      modifier = modifier.fillMaxSize()) { innerPadding ->
        SettingsContent(
            modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp),
            startScreen = startScreenState,
            startScreenPreferences = startScreenPreferences,
            onValueChange = { v: String ->
              Timber.d("Changed value $v")
              settingsViewModel.setStartScreen(v)
            })
      }
}

@Composable
private fun SettingsContent(
    modifier: Modifier,
    startScreen: String,
    startScreenPreferences: Map<String, String>,
    onValueChange: (String) -> Unit
) {
  val scrollState = rememberScrollState()
  Column(modifier = modifier.verticalScroll(scrollState).fillMaxWidth()) {
    Text(
        modifier = Modifier.padding(start = 16.dp),
        style = MaterialTheme.typography.bodyMedium,
        text = stringResource(id = R.string.pref_basic),
        color = MaterialTheme.colorScheme.primary)
    StartScreenPreference(startScreen, startScreenPreferences, onValueChange)
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Text(
        modifier = Modifier.padding(start = 16.dp, top = 16.dp),
        style = MaterialTheme.typography.bodyMedium,
        text = stringResource(id = R.string.pref_about),
        color = MaterialTheme.colorScheme.primary)
    ListItem(
        headlineContent = { Text(stringResource(id = R.string.app_name_title)) },
        supportingContent = { Text(stringResource(id = R.string.app_name)) })
    ListItem(
        headlineContent = { Text(stringResource(id = R.string.pref_app_version_title)) },
        supportingContent = { Text(stringResource(id = R.string.app_version)) })
    ListItem(
        headlineContent = { Text(stringResource(id = R.string.pref_copyright_title)) },
        supportingContent = { Text(stringResource(id = R.string.pref_copyright)) })
    Box {
      val openDialog = remember { mutableStateOf(false) }
      ListItem(
          modifier =
              Modifier.clickable {
                Timber.d("clicked preference")
                openDialog.value = true
              },
          headlineContent = { Text(stringResource(id = R.string.pref_app_license_title)) },
          supportingContent = { Text(stringResource(id = R.string.pref_app_license_summary)) })
      if (openDialog.value) {
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { openDialog.value = false },
            confirmButton = {},
            dismissButton = {
              TextButton(onClick = { openDialog.value = false }) { Text("Dismiss") }
            },
            title = { Text(text = stringResource(id = R.string.pref_app_license_title)) },
            text = {
              WebView(
                  pageUrl = "file:///android_asset/license.html",
                  backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp))
            })
      }
    }
  }
}

@Composable
private fun StartScreenPreference(
    startScreen: String,
    startScreenPreferences: Map<String, String>,
    onValueChange: (String) -> Unit
) {
  val openDialog = remember { mutableStateOf(false) }
  Box {
    ListItem(
        modifier =
            Modifier.clickable {
              Timber.d("clicked preference")
              openDialog.value = true
            },
        headlineContent = { Text(stringResource(id = R.string.pref_start_screen_title)) },
        supportingContent = { Text(startScreenPreferences[startScreen]!!) })
    if (openDialog.value) {
      AlertDialog(
          // modifier = Modifier.wrapContentHeight().fillMaxWidth(0.8f),
          properties = DialogProperties(usePlatformDefaultWidth = false),
          onDismissRequest = { openDialog.value = false },
          confirmButton = {
            TextButton(onClick = { openDialog.value = false }) { Text("Confirm") }
          },
          dismissButton = {
            TextButton(onClick = { openDialog.value = false }) { Text("Dismiss") }
          },
          title = { Text(text = stringResource(id = R.string.pref_start_screen_title)) },
          text = {
            Column {
              startScreenPreferences.forEach { preference ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                  RadioButton(
                      selected = preference.key == startScreen,
                      onClick = {
                        Timber.d("onClick ${preference.key}")
                        onValueChange(preference.key)
                      })
                  Text(preference.value)
                }
              }
            }
          })
    }
  }
}
