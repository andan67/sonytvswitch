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
import org.andan.android.tvbrowser.sonycontrolplugin.ui.theme.SonyTVSwitchTheme
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SelectControlViewModel

@Composable
fun SonyControlApp() {
    SonyTVSwitchTheme {
        val navController = rememberNavController()
        val navigationActions = remember(navController) {
            NavigationActions(navController)
        }

        val coroutineScope = rememberCoroutineScope()

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute =
            navBackStackEntry?.destination?.route ?: NavDestinations.ChannelList.route

        val drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        val selectControlViewModel: SelectControlViewModel = hiltViewModel()

        ModalNavigationDrawer(
            drawerContent = {
                AppDrawer(
                    currentRoute = currentRoute,
                    navigationActions = navigationActions,
                    closeDrawer = { coroutineScope.launch { drawerState.close() } },
                    viewModel = selectControlViewModel
                )
            },
            drawerState = drawerState,
        ) {
            SonyControlNavGraph(
                navController = navController,
                navigationActions = navigationActions,
                openDrawer = { coroutineScope.launch { drawerState.open() } }
            )
        }
    }
}