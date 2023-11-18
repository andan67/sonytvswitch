package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.ChannelListViewModel
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.ChannelMapViewModel
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel
import timber.log.Timber

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SonyControlNavGraph(
    navController: NavHostController = rememberNavController(),
    navigationActions: NavigationActions = rememberSaveable(navController) { NavigationActions(navController)},
    openDrawer: () -> Unit = {},
    startDestination: String = NavDestinations.RemoteControl.route,
    viewModel: SonyControlViewModel = hiltViewModel()
) {

    NavHost(
        navController = navController,
        startDestination = NavDestinations.ChannelList.route,
    ) {
        composable(NavDestinations.ChannelList.route) {
            ChannelListScreen(navActions = navigationActions, openDrawer = openDrawer)
        }

        composable(NavDestinations.ChannelMap.route) {
            ChannelMapScreen(
                navActions = navigationActions,
                openDrawer = openDrawer)
        }

        composable(NavDestinations.ChannelSingleMap.route) {navBackStackEntry ->
            val channelKey = navBackStackEntry.arguments?.getString("channelKey")
            channelKey?.let { ChannelSingleMapScreen(navActions = navigationActions, channelKey = channelKey)}
        }

        composable(NavDestinations.RemoteControl.route) {
            RemoteControlScreen(navActions = navigationActions, viewModel = viewModel, openDrawer = openDrawer)
        }

        composable(NavDestinations.ManageControl.route) {
            ManageControlScreen(navActions = navigationActions,
                // deleteSelectedControl = {viewModel.deleteSelectedControl()},
                //selectedSonyControlState = selectedSonyControlState,
                openDrawer = openDrawer)
        }

        composable(NavDestinations.PlayingContentInfoDetails.route) {
            PlayingContentInfoScreen(navActions = navigationActions, viewModel = viewModel)
        }

        dialog(
            route = NavDestinations.AddControl.route,
            dialogProperties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            ),
        ) {
            AddControlDialog(
                navActions = navigationActions,
                //initialAddControlStata = AddControlState.SPECIFY_HOST,
                //viewModel = viewModel
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