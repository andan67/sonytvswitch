package org.andan.android.tvbrowser.sonycontrolplugin.ui.components.misc

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.navigation.TopAppBarDropdownMenu

@Composable
fun WOLScreenOnOffMenu(
    enableWOLAction: () -> Unit = {},
    screenOffAction: () -> Unit = {},
    screenOnAction: () -> Unit = {}
) {
  TopAppBarDropdownMenu(
      iconContent = { Icon(Icons.Filled.MoreVert, stringResource(id = R.string.menu_more)) }) {
          // Here closeMenu stands for the lambda expression parameter that is passed when this
          // trailing lambda expression is called as 'content' variable in the TopAppBarDropdownMenu
          // The specific expression is: {expanded = ! expanded}, which effectively closes the menu
          closeMenu ->
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.wol_action)) },
            onClick = {
              enableWOLAction()
              closeMenu()
            })
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.screen_off_action)) },
            onClick = {
              screenOffAction()
              closeMenu()
            })
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.screen_on_action)) },
            onClick = {
              screenOnAction()
              closeMenu()
            })
      }
}
