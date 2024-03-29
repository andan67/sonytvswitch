package org.andan.android.tvbrowser.sonycontrolplugin.ui.components.navigation

import androidx.navigation.NavHostController

enum class NavDestinations(val route: String, label: String = "") {
  ChannelList(route = "channel_list"),
  ChannelMap(route = "channel_map"),
  ChannelSingleMap(route = "channel_single_map/{channelKey}"),
  PlayingContentInfoDetails(route = "playing_content_info_details"),
  RemoteControl(route = "remote_control"),
  AddControl(route = "add_control"),
  ManageControl(route = "manage_control"),
  Settings(route = "settings"),
  Help(route = "help"),
  NoControlDialog(route = "no_control")
}

/** Models the navigation actions in the app. */
class NavigationActions(
    private val navController: NavHostController,
    private val hasControls: () -> Boolean,
    private val finish: () -> Unit
) {

  fun navigateToChannelList() {
    if (hasControls()) {
      navController.navigate(NavDestinations.ChannelList.route)
    } else {
      navController.navigate(NavDestinations.NoControlDialog.route)
    }
  }

  fun navigateToChannelMap() {
    if (hasControls()) {
      navController.navigate(NavDestinations.ChannelMap.route)
    } else {
      navController.navigate(NavDestinations.NoControlDialog.route)
    }
  }

  fun navigateToChannelSingleMap(channelKey: String) {
    navController.navigate(
        NavDestinations.ChannelSingleMap.route.replace("{channelKey}", channelKey))
  }

  fun navigateToPlayingContentInfoDetails() {
    navController.navigate(NavDestinations.PlayingContentInfoDetails.route)
  }

  fun navigateToRemoteControl() {
    if (hasControls()) {
      navController.navigate(NavDestinations.RemoteControl.route)
    } else {
      navController.navigate(NavDestinations.NoControlDialog.route)
    }
  }

  fun navigateToManageControl() {
    if (hasControls()) {
      navController.navigate(NavDestinations.ManageControl.route)
    } else {
      navController.navigate(NavDestinations.NoControlDialog.route)
    }
  }

  fun openAddControlDialog() {
    navController.navigate(NavDestinations.AddControl.route)
  }

  fun navigateToSettings() {
    navController.navigate(NavDestinations.Settings.route)
  }

  fun navigateToHelp() {
    navController.navigate(NavDestinations.Help.route)
  }

  fun navigateToNoControl() {
    navController.navigate(NavDestinations.NoControlDialog.route)
  }

  fun navigateUp() {
    navController.navigateUp()
  }

  fun navigateFinish() {
    finish()
  }
}
