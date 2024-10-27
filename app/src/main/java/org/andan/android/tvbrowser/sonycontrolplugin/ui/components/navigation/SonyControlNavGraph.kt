package org.andan.android.tvbrowser.sonycontrolplugin.ui.components.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.misc.NoControlDialog
import org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.addcontrol.AddControlDialog
import org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.channellist.ChannelListScreen
import org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.channellist.ChannelListViewModel
import org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.channellist.PlayingContentInfoScreen
import org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.channelmap.ChannelMapScreen
import org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.channelmap.ChannelSingleMapScreen
import org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.help.HelpScreen
import org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.managecontrol.ManageControlScreen
import org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.remotecontrol.RemoteControlScreen
import org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.settings.SettingsScreen
import timber.log.Timber

@Composable
fun SonyControlNavGraph(
    navController: NavHostController = rememberNavController(),
    navigationActions: NavigationActions,
    openDrawer: () -> Unit = {},
    startDestination: String = NavDestinations.RemoteControl.route,
) {

  Timber.d("startDestination: $startDestination")

  NavHost(
      navController = navController,
      startDestination = startDestination,
  ) {
    composable(NavDestinations.ChannelList.route) {
      ChannelListScreen(navActions = navigationActions, openDrawer = openDrawer)
    }

    composable(NavDestinations.PlayingContentInfoDetails.route) { backStackEntry ->
      val parentEntry =
          remember(backStackEntry) {
            navController.getBackStackEntry(NavDestinations.ChannelList.route)
          }
      val parentViewModel = hiltViewModel<ChannelListViewModel>(parentEntry)

      PlayingContentInfoScreen(navActions = navigationActions, viewModel = parentViewModel)
    }

    composable(NavDestinations.ChannelMap.route) {
      ChannelMapScreen(navActions = navigationActions, openDrawer = openDrawer)
    }

    composable(NavDestinations.ChannelSingleMap.route) { navBackStackEntry ->
      val channelKey = navBackStackEntry.arguments?.getString("channelKey")
      channelKey?.let {
        ChannelSingleMapScreen(navActions = navigationActions, channelKey = channelKey)
      }
    }

    composable(NavDestinations.RemoteControl.route) { RemoteControlScreen(openDrawer = openDrawer) }

    composable(
        NavDestinations.ManageControl.route,
        arguments =
            listOf(
                navArgument("isAdded") {
                  type = NavType.BoolType
                  defaultValue = false
                })) { navBackStackEntry ->
          val isAdded = navBackStackEntry.arguments?.getBoolean("isAdded") == true
          ManageControlScreen(
              isAdded = isAdded,
              navToNoControl = { navigationActions.navigateToNoControl() },
              // deleteSelectedControl = {viewModel.deleteSelectedControl()},
              // selectedSonyControlState = selectedSonyControlState,
              openDrawer = openDrawer)
        }

    dialog(
        route = NavDestinations.AddControl.route,
        dialogProperties =
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false),
    ) {
      AddControlDialog(
          navActions = navigationActions,
      )
    }

    composable(NavDestinations.Settings.route) { SettingsScreen(openDrawer = openDrawer) }

    composable(NavDestinations.Help.route) { HelpScreen(openDrawer = openDrawer) }

    dialog(
        route = NavDestinations.NoControlDialog.route,
        dialogProperties =
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false),
    ) {
      NoControlDialog(navActions = navigationActions)
    }
  }
}

@Composable
fun TopAppBarDropdownMenu(
    iconContent: @Composable () -> Unit,
    content: @Composable ColumnScope.(() -> Unit) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
    IconButton(onClick = { expanded = !expanded }) { iconContent() }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
          content { expanded = !expanded }
        }
  }
}
