package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SelectControlUiState
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SelectControlViewModel
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
        Column(modifier = modifier.fillMaxSize()) {
            //DrawerHeader(noControls = noControls, onNoControlsChange =  {noControls = it} )
            DrawerHeader(viewModel = viewModel)
            Divider()
            NavigationDrawerItem(
                label = { Text(stringResource(id = R.string.menu_remote_control)) },
                icon = {
                    Icon(
                        painterResource(id = R.drawable.ic_settings_remote),
                        null,
                        modifier = Modifier.width(24.dp)
                    )
                },
                selected = currentRoute == NavDestinations.RemoteControl.route,
                onClick = { navigationActions.navigateToRemoteControl(); closeDrawer() },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationDrawerItem(
                label = { Text(stringResource(id = R.string.menu_show_channels)) },
                icon = {
                    Icon(
                        painterResource(id = R.drawable.ic_action_tv),
                        null,
                        modifier = Modifier.width(24.dp)
                    )
                },
                selected = currentRoute == NavDestinations.ChannelList.route,
                onClick = { navigationActions.navigateToChannelList(); closeDrawer() },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            Divider()
            NavigationDrawerItem(
                label = { Text(stringResource(id = R.string.menu_add_control)) },
                icon = {
                    Icon(
                        painterResource(id = R.drawable.ic_action_add),
                        null,
                        modifier = Modifier.width(24.dp)
                    )
                },
                selected = currentRoute == NavDestinations.AddControl.route,
                onClick = { navigationActions.openAddControlDialog(); closeDrawer() },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationDrawerItem(
                label = { Text(stringResource(id = R.string.menu_manage_control)) },
                icon = {
                    Icon(
                        painterResource(id = R.drawable.ic_action_edit),
                        null,
                        modifier = Modifier.width(24.dp)
                    )
                },
                selected = currentRoute == NavDestinations.ManageControl.route,
                onClick = { navigationActions.navigateToManageControl(); closeDrawer() },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationDrawerItem(
                label = { Text(stringResource(id = R.string.menu_channel_map)) },
                icon = {
                    Icon(
                        painterResource(id = R.drawable.tvb_2),
                        null,
                        modifier = Modifier.width(24.dp)
                    )
                },
                selected = currentRoute == NavDestinations.ChannelMap.route,
                onClick = { navigationActions.navigateToChannelMap(); closeDrawer() },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            Divider()
            NavigationDrawerItem(
                label = { Text(stringResource(id = R.string.menu_settings)) },
                icon = {
                    Icon(
                        painterResource(id = R.drawable.ic_settings),
                        null,
                        modifier = Modifier.width(24.dp)
                    )
                },
                selected = false,
                onClick = { closeDrawer() },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationDrawerItem(
                label = { Text(stringResource(id = R.string.menu_help)) },
                icon = {
                    Icon(
                        painterResource(id = R.drawable.ic_help_outline),
                        null,
                        modifier = Modifier.width(24.dp)
                    )
                },
                selected = false,
                onClick = { closeDrawer() },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerHeader(
    modifier: Modifier = Modifier
        .background(color = MaterialTheme.colorScheme.primary)
        .fillMaxWidth(),
    viewModel: SelectControlViewModel = hiltViewModel()
) {
    Column()
    {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = null, // decorative
                //tint = tintColor,
                modifier = Modifier
                    .width(64.dp)
                    .padding(start = 16.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = stringResource(id = R.string.app_name_nav),
                style = MaterialTheme.typography.headlineMedium
                //color = tintColor
            )
        }

        val uiState: SelectControlUiState by viewModel.selectControlUiState.collectAsStateWithLifecycle()
        //Timber.d("DrawerHeader controlList.size: ${uiState.controlList.size}")

        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            modifier = modifier.padding(start = 16.dp),
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            TextField(
                modifier = modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                //.padding(start = 16.dp),
                readOnly = true,
                value = uiState.selectedControl?.nickname ?: "",
                onValueChange = { },
                label = { Text(stringResource(R.string.select_remote_controller_label)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
            )
            ExposedDropdownMenu(
                modifier = modifier.fillMaxWidth(),
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                uiState.controlList.forEach { control ->
                    DropdownMenuItem(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                        onClick = {
                            //selectedOptionText = selectionOption.nickname
                            expanded = false
                            // check if new position/control index is set
                            if (uiState.selectedControl?.uuid != control.uuid) {
                                //viewModel.setSelectedControlIndex(index)
                                viewModel.setActiveControl(control.uuid)
                                Timber.d("onItemSelected ${control.nickname}")
                            }
                        },
                        text = { Text(text = control.nickname) }
                    )
                }
            }
        }
    }
}