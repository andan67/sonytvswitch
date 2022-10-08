package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import timber.log.Timber
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navHostController: NavHostController = rememberNavController(),
    navActions: NavigationActions = remember(navHostController) {
        NavigationActions(navHostController)}
)
    {
    NavHost(
        navController = navHostController,
        startDestination = NavPath.ChannelList.route
    ) {
        composable(NavPath.ChannelList.route) {
            ChannelListScreen(navActions = navActions)
        }

        composable(NavPath.ChannelListSearch.route) {
            ChannelSearchListScreen(navActions = navActions)
        }

        composable(NavPath.RemoteControl.route) {
            RemoteControlScreen(navActions = navActions)
        }
    }

}

@Composable
fun AppDrawer(
    closeDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    navActions: NavigationActions
) {
    Column(modifier = modifier.fillMaxSize()) {
        DrawerHeader()
        Divider()
        DrawerButton(
            painter = painterResource(id = R.drawable.ic_settings_remote),
            label = stringResource(id = R.string.menu_remote_control),
            action = {
                Timber.d("Called menu remote control")
                navActions.navigateToRemoteControl()
                closeDrawer()
            }
        )
        DrawerButton(
            painter = painterResource(id = R.drawable.ic_action_tv),
            label = stringResource(id = R.string.menu_show_channels),
            action = {
                Timber.d("Called menu remote control")
                navActions.navigateToChannelList()
                closeDrawer()
            }
        )
        Divider()
        DrawerButton(
            painter = painterResource(id = R.drawable.ic_action_add),
            label = stringResource(id = R.string.menu_add_control),
            //isSelected = currentRoute == TodoDestinations.STATISTICS_ROUTE,
            action = {
                Timber.d("Called menu remote control")
                //navigateToStatistics()
                closeDrawer()
            }
        )
        DrawerButton(
            painter = painterResource(id = R.drawable.ic_action_edit),
            label = stringResource(id = R.string.menu_manage_control),
            //isSelected = currentRoute == TodoDestinations.STATISTICS_ROUTE,
            action = {
                Timber.d("Called menu remote control")
                //navigateToStatistics()
                closeDrawer()
            }
        )
        DrawerButton(
            painter = painterResource(id = R.drawable.ic_action_arrow_right),
            label = stringResource(id = R.string.menu_channel_map),
            //isSelected = currentRoute == TodoDestinations.STATISTICS_ROUTE,
            action = {
                Timber.d("Called menu remote control")
                //navigateToStatistics()
                closeDrawer()
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
                closeDrawer()
            }
        )
        DrawerButton(
            painter = painterResource(id = R.drawable.ic_help_outline),
            label = stringResource(id = R.string.menu_help),
            //isSelected = currentRoute == TodoDestinations.STATISTICS_ROUTE,
            action = {
                Timber.d("Called menu remote control")
                //navigateToStatistics()
                closeDrawer()
            }
        )
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DrawerHeader(
    modifier: Modifier = Modifier
        .background(color = MaterialTheme.colors.primary)
        .fillMaxWidth(),
    viewModel: SonyControlViewModel = viewModel()
) {
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
            style = MaterialTheme.typography.h6
            //color = tintColor
        )
    }

    //val options = listOf("Option 1", "Option 2", "Option 3", "Option 4", "Option 5")
    val options = viewModel.sonyControls.value!!.controls
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(if (options.size > 0) options[0].nickname else "") }
// We want to react on tap/press on TextField to show menu
    ExposedDropdownMenuBox(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.primary),
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        TextField(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
            readOnly = true,
            value = selectedOptionText,
            onValueChange = { },
            label = { Text(stringResource(R.string.select_remote_controller_label)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                unfocusedLabelColor = Color.Gray,
                focusedLabelColor = Color.Gray,
                trailingIconColor = Color.White,
                focusedTrailingIconColor = Color.White,
                backgroundColor = MaterialTheme.colors.primary,
                textColor = MaterialTheme.colors.secondary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        ExposedDropdownMenu(
            modifier = modifier.fillMaxWidth(),
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            options.forEachIndexed {index,  selectionOption ->
                DropdownMenuItem(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    onClick = {

                        selectedOptionText = selectionOption.nickname
                        expanded = false
                        Timber.i("onItemSelected position:$index")
                        // check if new position/control index is set
                        if (viewModel.sonyControls.value!!.selected != index) {
                            viewModel.setSelectedControlIndex(index)
                            Timber.d("onItemSelected setSelectedControlIndex")
                        }
                    }
                ) {
                    Text(text = selectionOption.nickname)
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
    val tintColor = MaterialTheme.colors.secondary
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
                style = MaterialTheme.typography.body2,
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