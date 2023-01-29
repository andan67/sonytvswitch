package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.di.ApplicationComponent
import org.andan.android.tvbrowser.sonycontrolplugin.ui.theme.SonyTVSwitchTheme
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SonyControlApp(appComponent: ApplicationComponent) {
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

        val sonyControlViewModel: SonyControlViewModel = viewModel()

        ModalNavigationDrawer(
            drawerContent = {
                AppDrawer(
                    currentRoute = currentRoute,
                    navigationActions = navigationActions,
                    closeDrawer = { coroutineScope.launch { drawerState.close() } },
                    viewModel = sonyControlViewModel
                )
            },
            drawerState = drawerState,
        ) {
            SonyControlNavGraph(
                appComponent = appComponent,
                navController = navController,
                navigationActions = navigationActions,
                openDrawer = { coroutineScope.launch { drawerState.open() } },
                viewModel = sonyControlViewModel
            )
        }
    }
}