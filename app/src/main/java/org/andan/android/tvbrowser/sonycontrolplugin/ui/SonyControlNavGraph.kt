package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.ChannelListViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SonyControlNavGraph(
    navController: NavHostController = rememberNavController(),
    navigationActions: NavigationActions = rememberSaveable(navController) {
        NavigationActions(
            navController
        )
    },
    openDrawer: () -> Unit = {},
    startDestination: String = NavDestinations.RemoteControl.route,
) {

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(NavDestinations.ChannelList.route) {
            ChannelListScreen(navActions = navigationActions, openDrawer = openDrawer)
        }

        composable(NavDestinations.PlayingContentInfoDetails.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(NavDestinations.ChannelList.route)
            }
            val parentViewModel = hiltViewModel<ChannelListViewModel>(parentEntry)

            PlayingContentInfoScreen(navActions = navigationActions, viewModel = parentViewModel)
        }

        composable(NavDestinations.ChannelMap.route) {
            ChannelMapScreen(
                navActions = navigationActions,
                openDrawer = openDrawer
            )
        }

        composable(NavDestinations.ChannelSingleMap.route) { navBackStackEntry ->
            val channelKey = navBackStackEntry.arguments?.getString("channelKey")
            channelKey?.let {
                ChannelSingleMapScreen(
                    navActions = navigationActions,
                    channelKey = channelKey
                )
            }
        }

        composable(NavDestinations.RemoteControl.route) {
            RemoteControlScreen(
                navActions = navigationActions,
                openDrawer = openDrawer
            )
        }

        composable(NavDestinations.ManageControl.route) {
            ManageControlScreen(
                navActions = navigationActions,
                // deleteSelectedControl = {viewModel.deleteSelectedControl()},
                //selectedSonyControlState = selectedSonyControlState,
                openDrawer = openDrawer
            )
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