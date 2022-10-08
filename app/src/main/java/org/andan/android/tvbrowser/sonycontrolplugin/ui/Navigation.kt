package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

enum class NavPath(
    val route: String,
) {
    ChannelList(route = "channel_list"),
    ChannelListSearch(route = "channel_list_search"),
    ChannelDetail(route = "channel_detail"),
    RemoteControl(route = "remote_control")
}

/**
 * Models the navigation actions in the app.
 */
class NavigationActions(private val navController: NavHostController) {

    fun navigateToChannelList() {
        navController.navigate(NavPath.ChannelList.route)
    }

    fun navigateToChannelListSearch() {
        navController.navigate(NavPath.ChannelListSearch.route)
    }

    fun navigateToChannelDetail() {
        navController.navigate(NavPath.ChannelDetail.route)
    }

    fun navigateToRemoteControl() {
        navController.navigate(NavPath.RemoteControl.route)
    }
}