package org.andan.android.tvbrowser.sonycontrolplugin.ui.components.navigation

import androidx.navigation.NavHostController
import timber.log.Timber

enum class NavDestinations(val route: String, label: String = "") {
  ChannelList(route = "channel_list"),
  ChannelMap(route = "channel_map"),
  ChannelSingleMap(route = "channel_single_map/{channelKey}"),
  PlayingContentInfoDetails(route = "playing_content_info_details"),
  RemoteControl(route = "remote_control"),
  AddControl(route = "add_control"),
  ManageControl(route = "manage_control?isAdded={isAdded}"),
  Settings(route = "settings"),
  Help(route = "help"),
  NoControlDialog(route = "no_control")
}

/** Models the navigation actions in the app. */
class NavigationActions(
    private val navController: NavHostController,
    private val finish: () -> Unit
) {

  fun navigateToChannelList() {
    Timber.d("navigateToChannelList")
    navController.navigate(NavDestinations.ChannelList.route)
  }

  fun navigateToChannelMap() {
    Timber.d("navigateToChannelMap")
    navController.navigate(NavDestinations.ChannelMap.route)
  }

  fun navigateToChannelSingleMap(channelKey: String) {
    navController.navigate(
        NavDestinations.ChannelSingleMap.route.replace("{channelKey}", channelKey))
  }

  fun navigateToPlayingContentInfoDetails() {
    Timber.d("navigateToPlayingContentInfoDetails")
    navController.navigate(NavDestinations.PlayingContentInfoDetails.route)
  }

  fun navigateToRemoteControl() {
    Timber.d("navigateToRemoteControl")
    navController.navigate(NavDestinations.RemoteControl.route)
  }

  fun navigateToManageControl(isAdded: Boolean = false) {
    Timber.d("navigateToManageControl")
    navController.navigate(
        NavDestinations.ManageControl.route.replace("{isAdded}", isAdded.toString()))
  }

  fun navigateToManageControlPopUp(isAdded: Boolean = false) {
    Timber.d("navigateToManageControlPopUp")
    navController.navigate(
        NavDestinations.ManageControl.route.replace("{isAdded}", isAdded.toString())) {
          popUpTo(navController.graph.id) { inclusive = true }
        }
  }

  fun openAddControlDialog() {
    Timber.d("openAddControlDialog")
    navController.navigate(NavDestinations.AddControl.route)
  }

  fun navigateToSettings() {
    Timber.d("navigateToSettings")
    navController.navigate(NavDestinations.Settings.route)
  }

  fun navigateToHelp() {
    Timber.d("navigateToHelp")
    navController.navigate(NavDestinations.Help.route)
  }

  fun navigateToNoControl() {
    Timber.d("navigateToNoControl")
    navController.navigate(NavDestinations.NoControlDialog.route) {
      popUpTo(navController.graph.id) { inclusive = true }
    }
  }

  fun navigateUp() {
    Timber.d("navigateUp")
    navController.navigateUp()
  }

  fun navigateFinish() {
    finish()
  }
}
