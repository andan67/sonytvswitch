package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.ManageControlViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageControlScreen(
    modifier: Modifier = Modifier,
    navActions: NavigationActions,
    deleteSelectedControl: () -> Unit,
    //selectedSonyControlState: State<SonyControl?>,
    openDrawer: () -> Unit
) {
/*    val sonyControl by remember {
        selectedSonyControlState
    }*/
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val manageControlViewModel: ManageControlViewModel = hiltViewModel()
    val uiState by manageControlViewModel.manageControlUiState.collectAsStateWithLifecycle()

    Timber.d(uiState.toString())
    uiState.message?.let {
        val messageString = when (it) {
            is IntEventMessage -> stringResource(id = it.message)
            is StringEventMessage -> it.message
        }
        LaunchedEffect(uiState.message, uiState.isLoading) {
            Timber.d(messageString)
            snackbarHostState.showSnackbar(messageString)
            manageControlViewModel.onConsumedMessage()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) {data ->
                Snackbar(
                    containerColor = if(uiState.isSuccess) SnackbarDefaults.color else MaterialTheme.colorScheme.errorContainer,
                    contentColor = if(uiState.isSuccess) SnackbarDefaults.contentColor else MaterialTheme.colorScheme.error
                ) {
                    Text(data.visuals.message)
                }
            } },
        topBar = {
            TopAppBar(title = { Text(text = stringResource(id = R.string.menu_manage_control)) },
                navigationIcon = {
                    IconButton(onClick = openDrawer) {
                        Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer))
                    }
                },
                actions = {
                    ManageControlMenu(
                        registerControlAction = {manageControlViewModel.registerControl()},
                        deleteControlAction = { deleteSelectedControl() },
                        requestChannelListAction = { manageControlViewModel.fetchChannelList() },
                        enableWOLAction = {manageControlViewModel.wakeOnLan()},
                        checkConnectivityAction = { manageControlViewModel.checkAvailability() },
                        enabled = uiState.sonyControl != null )
                })
        },
        modifier = modifier.fillMaxSize()
    )
    { innerPadding ->
        ManageControlContent(
            modifier = Modifier.padding(innerPadding),
            //sonyControl = uiState.sonyControl
            sonyControl = uiState.sonyControl
        )
    }
}

@Composable
private fun ManageControlContent(
    modifier: Modifier,
    sonyControl: SonyControl?
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .verticalScroll(scrollState)
            .fillMaxWidth()
    ) {
        ManageControlItem(
            modifier,
            label = stringResource(id = R.string.manage_control_host_name),
            value = sonyControl?.ip,
            showDivider = true
        )
        ManageControlItem(
            modifier,
            label = stringResource(id = R.string.manage_control_nick_name),
            value = sonyControl?.nickname,
            showDivider = true
        )
        ManageControlItem(
            modifier,
            label = stringResource(id = R.string.manage_control_device_name),
            value = sonyControl?.devicename,
            showDivider = true
        )
        ManageControlItem(
            modifier,
            label = stringResource(id = R.string.manage_control_uuid),
            value = sonyControl?.uuid,
            showDivider = true
        )
        ManageControlItem(
            modifier,
            label = stringResource(id = R.string.manage_control_model),
            value = sonyControl?.systemModel,
            showDivider = true
        )
        ManageControlItem(
            modifier,
            label = stringResource(id = R.string.manage_control_mac),
            value = sonyControl?.systemMacAddr,
            showDivider = true
        )
        ManageControlItem(
            modifier,
            label = stringResource(id = R.string.manage_control_wol_mode),
            value = sonyControl?.systemWolMode.toString(),
            showDivider = true
        )
        ManageControlItem(
            modifier,
            label = stringResource(id = R.string.manage_control_number_channels),
            value = sonyControl?.channelList?.size.toString(),
            showDivider = true
        )
        ManageControlItem(
            modifier,
            label = stringResource(id = R.string.manage_control_sources),
            value = sonyControl?.sourceList.toString(),
            showDivider = false
        )
    }
}

@Composable
fun ManageControlItem(
    modifier: Modifier,
    label: String,
    value: String?,
    showDivider: Boolean
) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(text = label, style = MaterialTheme.typography.titleLarge)
    Text(
        text = if (value.isNullOrEmpty()) "" else {
            value
        }, style = MaterialTheme.typography.bodyLarge
    )
    Spacer(modifier = Modifier.height(8.dp))
    if (showDivider) Divider()

}

@Composable
fun ManageControlMenu(
    registerControlAction: () -> Unit,
    deleteControlAction: () -> Unit,
    requestChannelListAction: () -> Unit,
    enableWOLAction: () -> Unit,
    checkConnectivityAction: () -> Unit,
    enabled: Boolean
) {
    TopAppBarDropdownMenu(
        iconContent = {
            Icon(Icons.Filled.MoreVert, stringResource(id = R.string.menu_more))
        }
    ) {
        // Here closeMenu stands for the lambda expression parameter that is passed when this
        // trailing lambda expression is called as 'content' variable in the TopAppBarDropdownMenu
        // The specific expression is: {expanded = ! expanded}, which effectively closes the menu
            closeMenu ->
        val openDialog = remember { mutableStateOf(false) }
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.register_control_action)) },
            onClick = { registerControlAction(); closeMenu() },
            enabled = enabled)
        DropdownMenuItem(
            text = {
                DeleteControlDialog(
                    deleteControlAction,
                    openDialog.value,
                    { openDialog.value = false; closeMenu() })
            },
            onClick = { openDialog.value = true; /*closeMenu()*/ },
            enabled = enabled)
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.get_tv_channel_list_action)) },
            onClick = { requestChannelListAction(); closeMenu() },
            enabled = enabled )
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.enable_wol_action)) },
            onClick = { enableWOLAction(); closeMenu() },
            enabled = enabled)
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.check_set_host)) },
            onClick = { checkConnectivityAction(); closeMenu() },
            enabled = enabled)
    }
}

@Composable
fun DeleteControlDialog(
    deleteControlAction: () -> Unit,
    openDialog: Boolean,
    closeDialog: () -> Unit
) {
    Text(text = stringResource(id = R.string.delete_control_action))
    if (openDialog) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onCloseRequest.
                closeDialog()
            },
            title = {
                Text(text = "Confirm delete")
            },
            text = {
                Text("Do you want to delete this control?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteControlAction()
                        closeDialog()
                    },
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        closeDialog()
                    }) {
                    Text("No")
                }
            }
        )
    }

}

