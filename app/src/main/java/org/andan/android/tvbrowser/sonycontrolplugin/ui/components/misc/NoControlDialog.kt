package org.andan.android.tvbrowser.sonycontrolplugin.ui.components.misc

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.navigation.NavigationActions

@Composable
fun NoControlDialog(navActions: NavigationActions) {
  AlertDialog(
      modifier = Modifier.wrapContentHeight().fillMaxWidth(0.8f),
      properties = DialogProperties(usePlatformDefaultWidth = false),
      onDismissRequest = { navActions.navigateUp() },
      dismissButton = {
        TextButton(onClick = { navActions.navigateToHelp() }) {
          Text(text = stringResource(id = R.string.open_help))
        }
      },
      confirmButton = {
        TextButton(onClick = { navActions.openAddControlDialog() }) {
          Text(text = stringResource(id = R.string.menu_add_control))
        }
      },
      title = { Text(text = stringResource(id = R.string.alert_no_active_control_title)) },
      text = { Text(text = stringResource(id = R.string.alert_no_active_control_message)) })
}
