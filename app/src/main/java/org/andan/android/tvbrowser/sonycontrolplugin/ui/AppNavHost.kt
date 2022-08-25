package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

enum class NavPath(
    val route: String,
) {
    ChannelList(route = "channel_list"),
    ChannelListSearch(route = "channel_list_search"),
    ChannelDetail(route = "channel_detail")
}

@Composable
fun AppNavHost(navHostController: NavHostController) {
    NavHost(
        navController = navHostController,
        startDestination = NavPath.ChannelList.route
    ) {
        composable(NavPath.ChannelList.route) {
            ChannelListScreen(navHostController = navHostController)
        }

        composable(NavPath.ChannelListSearch.route) {
            ChannelSearchListScreen(navHostController = navHostController)
        }

    }

}