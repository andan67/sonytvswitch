package org.andan.android.tvbrowser.sonycontrolplugin.ui.components.drawer

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.navigation.NavDestinations
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.navigation.NavigationActions
import timber.log.Timber

@Composable
fun AppDrawer(
    currentRoute: String,
    navigationActions: NavigationActions,
    closeDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SelectControlViewModel = hiltViewModel()
) {
  ModalDrawerSheet(modifier) {
    val scrollState = rememberScrollState()
    Column(modifier = modifier.fillMaxSize().verticalScroll(scrollState)) {
      // DrawerHeader(noControls = noControls, onNoControlsChange =  {noControls = it} )
      DrawerHeader(viewModel = viewModel)
      HorizontalDivider(Modifier.padding(horizontal = 28.dp))
      NavigationDrawerItem(
          label = { Text(stringResource(id = R.string.menu_remote_control)) },
          icon = {
            Icon(
                painterResource(id = R.drawable.outline_settings_remote_24),
                null,
                modifier = Modifier.width(24.dp))
          },
          selected = currentRoute == NavDestinations.RemoteControl.route,
          onClick = {
            navigationActions.navigateToRemoteControl()
            closeDrawer()
          },
          modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding))
      NavigationDrawerItem(
          label = { Text(stringResource(id = R.string.menu_show_channels)) },
          icon = {
            Icon(
                painterResource(id = R.drawable.baseline_format_list_bulleted_24),
                null,
                modifier = Modifier.width(24.dp))
          },
          selected = currentRoute == NavDestinations.ChannelList.route,
          onClick = {
            navigationActions.navigateToChannelList()
            closeDrawer()
          },
          modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding))
      HorizontalDivider(Modifier.padding(horizontal = 28.dp))
      NavigationDrawerItem(
          label = { Text(stringResource(id = R.string.menu_add_control)) },
          icon = {
            Icon(
                painterResource(id = R.drawable.outline_add_24),
                null,
                modifier = Modifier.width(24.dp))
          },
          selected = currentRoute == NavDestinations.AddControl.route,
          onClick = {
            navigationActions.openAddControlDialog()
            closeDrawer()
          },
          modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding))
      NavigationDrawerItem(
          label = { Text(stringResource(id = R.string.menu_manage_control)) },
          icon = {
            Icon(
                painterResource(id = R.drawable.outline_edit_24),
                null,
                modifier = Modifier.width(24.dp))
          },
          selected = currentRoute == NavDestinations.ManageControl.route,
          onClick = {
            navigationActions.navigateToManageControl()
            closeDrawer()
          },
          modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding))
      NavigationDrawerItem(
          label = { Text(stringResource(id = R.string.menu_channel_map)) },
          icon = {
            Icon(painterResource(id = R.drawable.tvb_2), null, modifier = Modifier.width(24.dp))
          },
          selected = currentRoute == NavDestinations.ChannelMap.route,
          onClick = {
            navigationActions.navigateToChannelMap()
            closeDrawer()
          },
          modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding))
      HorizontalDivider(Modifier.padding(horizontal = 28.dp))
      NavigationDrawerItem(
          label = { Text(stringResource(id = R.string.menu_settings)) },
          icon = {
            Icon(
                painterResource(id = R.drawable.outline_settings_24),
                null,
                modifier = Modifier.width(24.dp))
          },
          selected = currentRoute == NavDestinations.Settings.route,
          onClick = {
            navigationActions.navigateToSettings()
            closeDrawer()
          },
          modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding))
      NavigationDrawerItem(
          label = { Text(stringResource(id = R.string.menu_help)) },
          icon = {
            Icon(
                painterResource(id = R.drawable.outline_help_outline_24),
                null,
                modifier = Modifier.width(24.dp))
          },
          selected = false,
          onClick = {
            navigationActions.navigateToHelp()
            closeDrawer()
          },
          modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerHeader(
    // modifier: Modifier = Modifier.background(color =
    // MaterialTheme.colorScheme.primary).fillMaxWidth(),
    modifier: Modifier = Modifier,
    viewModel: SelectControlViewModel = hiltViewModel()
) {
  Column {
    val drawable = AppCompatResources.getDrawable(LocalContext.current, R.mipmap.ic_launcher)

    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(top = 16.dp)) {
          Icon(
              painterResource(id = R.drawable.sony_tv_switch_icon_front),
              modifier = Modifier.padding(start = 16.dp).width(48.dp),
              tint = Color.Unspecified,
              contentDescription = null)
          Spacer(Modifier.width(16.dp))
          Text(
              text = stringResource(id = R.string.app_name_nav),
              style = MaterialTheme.typography.headlineSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

    val uiState: SelectControlUiState by
        viewModel.selectControlUiState.collectAsStateWithLifecycle()
    // Timber.d("DrawerHeader controlList.size: ${uiState.controlList.size}")

    var expanded by remember { mutableStateOf(false) }
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 28.dp)) {
          Icon(
              painterResource(id = R.drawable.baseline_tv_24),
              null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant)
          ExposedDropdownMenuBox(
              modifier = modifier.padding(start = 20.dp),
              expanded = expanded,
              onExpandedChange = { expanded = !expanded }) {
                TextField(
                    modifier = modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                    // .background(color = MaterialTheme.colorScheme.surfaceContainerLow),
                    // .padding(start = 16.dp),
                    readOnly = true,
                    value = uiState.selectedControl?.nickname ?: "",
                    onValueChange = {},
                    label = {
                      Text(
                          stringResource(R.string.select_remote_controller_label),
                          color = MaterialTheme.colorScheme.primary)
                    },
                    // leadingIcon = { Icon(painterResource(id = R.drawable.baseline_tv_24), null)
                    // },
                    trailingIcon = {
                      ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors =
                        TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent))
                ExposedDropdownMenu(
                    // modifier = modifier.fillMaxWidth().padding(start = 28.dp),
                    expanded = expanded,
                    onDismissRequest = { expanded = false }) {
                      uiState.controlList.forEach { control ->
                        DropdownMenuItem(
                            // modifier = modifier.fillMaxWidth().padding(start = 16.dp),
                            onClick = {
                              // selectedOptionText = selectionOption.nickname
                              expanded = false
                              // check if new position/control index is set
                              if (uiState.selectedControl?.uuid != control.uuid) {
                                // viewModel.setSelectedControlIndex(index)
                                viewModel.setActiveControl(control.uuid)
                                Timber.d("onItemSelected ${control.nickname}")
                              }
                            },
                            text = {
                              Text(
                                  text = control.nickname,
                                  color = MaterialTheme.colorScheme.onSurfaceVariant)
                            })
                      }
                    }
              }
        }
  }
}
