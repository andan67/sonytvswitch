package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.navigation.NavHostController

enum class NavDestinations(
    val route: String,
) {
    ChannelList(route = "channel_list"),
    ChannelMap(route = "channel_map"),
    PlayingContentInfoDetails(route = "playing_content_info_details"),
    RemoteControl(route = "remote_control"),
    AddControl(route = "add_control"),
    ManageControl(route = "manage_control")

}

/**
 * Models the navigation actions in the app.
 */
class NavigationActions(private val navController: NavHostController) {

    val navHostController:  NavHostController = navController

    fun navigateToChannelList() {
        navController.navigate(NavDestinations.ChannelList.route)
    }

    fun navigateToChannelMap() {
        navController.navigate(NavDestinations.ChannelMap.route)
    }
    fun navigateToPlayingContentInfoDetails() {
        navController.navigate(NavDestinations.PlayingContentInfoDetails.route)
    }

    fun navigateToRemoteControl() {
        navController.navigate(NavDestinations.RemoteControl.route)
    }

    fun navigateToManageControl() {
        navController.navigate(NavDestinations.ManageControl.route)
    }

    fun openAddControlDialog() {
        navController.navigate(NavDestinations.AddControl.route)
    }

    fun navigateUp() {
        navController.navigateUp()
    }
}