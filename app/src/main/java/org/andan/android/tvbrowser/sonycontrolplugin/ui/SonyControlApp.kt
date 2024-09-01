package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.drawer.AppDrawer
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.drawer.SelectControlViewModel
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.navigation.NavDestinations
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.navigation.NavigationActions
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.navigation.SonyControlNavGraph
import org.andan.android.tvbrowser.sonycontrolplugin.ui.theme.SonyTVSwitchTheme

@Composable
fun SonyControlApp(startDestination: String, finish: () -> Unit) {
  SonyTVSwitchTheme {
    val navController = rememberNavController()

    val coroutineScope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavDestinations.ChannelList.route

    val drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val selectControlViewModel: SelectControlViewModel = hiltViewModel()

    val navigationActions = remember(navController) { NavigationActions(navController, finish) }

    ModalNavigationDrawer(
        drawerContent = {
          AppDrawer(
              currentRoute = currentRoute,
              navigationActions = navigationActions,
              closeDrawer = { coroutineScope.launch { drawerState.close() } },
              viewModel = selectControlViewModel)
        },
        drawerState = drawerState,
        // this to prevent conflicts when scrolling the WebView on the Help screen
        gesturesEnabled = currentRoute != NavDestinations.Help.route || drawerState.isOpen) {
          SonyControlNavGraph(
              navController = navController,
              navigationActions = navigationActions,
              startDestination = startDestination,
              openDrawer = { coroutineScope.launch { drawerState.open() } })
        }
  }
}
