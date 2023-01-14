package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.navigation.NavHostController

enum class NavDestinations(
    val route: String,
) {
    ChannelList(route = "channel_list"),
    ChannelListSearch(route = "channel_list_search"),
    ChannelDetail(route = "channel_detail"),
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

    fun navigateToChannelListSearch() {
        navController.navigate(NavDestinations.ChannelListSearch.route)
    }

    fun navigateToChannelDetail() {
        navController.navigate(NavDestinations.ChannelDetail.route)
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