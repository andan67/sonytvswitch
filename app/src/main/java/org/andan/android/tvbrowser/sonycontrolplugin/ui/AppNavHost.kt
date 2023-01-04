package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.ui.theme.SonyTVSwitchTheme
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navHostController: NavHostController = rememberNavController(),
    navActions: NavigationActions = remember(navHostController) {
        NavigationActions(navHostController)
    },
    viewModel: SonyControlViewModel
) {
    NavHost(
        navController = navHostController,
        startDestination = NavPath.ChannelList.route
    ) {
        composable(NavPath.ChannelList.route) {
            ChannelListScreen(navActions = navActions, viewModel = viewModel)
        }

        composable(NavPath.ChannelListSearch.route) {
            ChannelSearchListScreen(navActions = navActions, viewModel = viewModel)
        }

        composable(NavPath.RemoteControl.route) {
            RemoteControlScreen(navActions = navActions, viewModel = viewModel)
        }

        dialog(
            route = NavPath.AddControl.route,
            dialogProperties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            ),
        ) {
            AddControlDialog(
                navActions = navActions,
                //initialAddControlStata = AddControlState.SPECIFY_HOST,
                viewModel = viewModel
            )
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    modifier: Modifier = Modifier,
    navActions: NavigationActions,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    coroutineScope: CoroutineScope,
    viewModel: SonyControlViewModel,
    content: @Composable () -> Unit
) {
    var noControls by remember { mutableStateOf(viewModel.sonyControls.value!!.controls.size) }

    SonyTVSwitchTheme() {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Column(modifier = modifier.fillMaxSize()) {
                        //DrawerHeader(noControls = noControls, onNoControlsChange =  {noControls = it} )
                        DrawerHeader(viewModel = viewModel)
                        Divider()
                        DrawerButton(
                            painter = painterResource(id = R.drawable.ic_settings_remote),
                            label = stringResource(id = R.string.menu_remote_control),
                            action = {
                                Timber.d("Called menu remote control")
                                navActions.navigateToRemoteControl()
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                        DrawerButton(
                            painter = painterResource(id = R.drawable.ic_action_tv),
                            label = stringResource(id = R.string.menu_show_channels),
                            action = {
                                Timber.d("Called menu remote control")
                                navActions.navigateToChannelList()
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                        Divider()
                        DrawerButton(
                            painter = painterResource(id = R.drawable.ic_action_add),
                            label = stringResource(id = R.string.menu_add_control),
                            //isSelected = currentRoute == TodoDestinations.STATISTICS_ROUTE,
                            action = {
                                Timber.d("Called menu remote control")
                                navActions.openAddControlDialog()
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                        DrawerButton(
                            painter = painterResource(id = R.drawable.ic_action_edit),
                            label = stringResource(id = R.string.menu_manage_control),
                            //isSelected = currentRoute == TodoDestinations.STATISTICS_ROUTE,
                            action = {
                                Timber.d("Called menu remote control")
                                //navigateToStatistics()
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                        DrawerButton(
                            painter = painterResource(id = R.drawable.ic_action_arrow_right),
                            label = stringResource(id = R.string.menu_channel_map),
                            //isSelected = currentRoute == TodoDestinations.STATISTICS_ROUTE,
                            action = {
                                Timber.d("Called menu remote control")
                                //navigateToStatistics()
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                        Divider()
                        DrawerButton(
                            painter = painterResource(id = R.drawable.ic_settings),
                            label = stringResource(id = R.string.menu_settings),
                            //isSelected = currentRoute == TodoDestinations.STATISTICS_ROUTE,
                            action = {
                                Timber.d("Called menu remote control")
                                //navigateToStatistics()
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                        DrawerButton(
                            painter = painterResource(id = R.drawable.ic_help_outline),
                            label = stringResource(id = R.string.menu_help),
                            //isSelected = currentRoute == TodoDestinations.STATISTICS_ROUTE,
                            action = {
                                Timber.d("Called menu remote control")
                                //navigateToStatistics()
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                    }
                }
            },
            content = content
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerHeader(
    modifier: Modifier = Modifier
        .background(color = MaterialTheme.colorScheme.primary)
        .fillMaxWidth(),
    viewModel: SonyControlViewModel
    //noControls : Int,
    //onNoControlsChange: (Int) -> Unit
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

        var expanded by remember { mutableStateOf(false) }
        //val noControls by viewModel.noControls.observeAsState()
        val controls by viewModel.sonyControls.observeAsState()
        val controlList = controls!!.controls
        Timber.d("DrawerHeader controlList.size: ${controlList.size}")
        //Timber.d("DrawerHeader noControls: $noControls")
        //var selectedOptionText by remember { mutableStateOf(if (controlList.size > 0) controlList[0].nickname else "") }
        val selectedControlIndex =  controls!!.selected
        val selectedControlName = if (selectedControlIndex >= 0) controlList[selectedControlIndex].nickname else ""
// We want to react on tap/press on TextField to show menu
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
                value = selectedControlName,
                onValueChange = { },
                label = { Text(stringResource(R.string.select_remote_controller_label)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
                /*
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                unfocusedLabelColor = Color.Gray,
                focusedLabelColor = Color.Gray,
                unfocusedTrailingIconColor = Color.White,
                focusedTrailingIconColor = Color.White,
                containerColor = MaterialTheme.colorScheme.primary,
                textColor = MaterialTheme.colorScheme.secondary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent

            )
             */
            )
            ExposedDropdownMenu(
                modifier = modifier.fillMaxWidth(),
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                controlList.forEachIndexed { index, selectionOption ->
                    DropdownMenuItem(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                        onClick = {
                            //selectedOptionText = selectionOption.nickname
                            expanded = false
                            Timber.i("onItemSelected position:$index")
                            // check if new position/control index is set
                            if (viewModel.sonyControls.value!!.selected != index) {
                                viewModel.setSelectedControlIndex(index)
                                Timber.d("onItemSelected setSelectedControlIndex")
                            }
                        },
                        text = { Text(text = selectionOption.nickname) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerButton(
    painter: Painter,
    label: String,
    //isSelected: Boolean,
    action: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tintColor = MaterialTheme.colorScheme.secondary
    /*val tintColor = if (isSelected) {
        MaterialTheme.colors.secondary
    } else {
        MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
    }*/

    TextButton(
        onClick = action,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.activity_horizontal_margin))
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painter,
                contentDescription = null, // decorative
                tint = tintColor,
                modifier = Modifier.width(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = tintColor
            )
        }
    }
}

@Composable
public fun TopAppBarDropdownMenu(
    iconContent: @Composable () -> Unit,
    content: @Composable ColumnScope.(() -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
        IconButton(onClick = { expanded = !expanded }) {
            iconContent()
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.wrapContentSize(Alignment.TopEnd)
        ) {
            content { expanded = !expanded }
        }
    }
}