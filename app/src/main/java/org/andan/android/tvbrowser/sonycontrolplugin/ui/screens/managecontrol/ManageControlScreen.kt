package org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.managecontrol

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.ui.IntEventMessage
import org.andan.android.tvbrowser.sonycontrolplugin.ui.StringEventMessage
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.navigation.TopAppBarDropdownMenu
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageControlScreen(
    modifier: Modifier = Modifier,
    isAdded: Boolean = false,
    navToNoControl: () -> Unit,
    openDrawer: () -> Unit
) {

  val snackbarHostState = remember { SnackbarHostState() }

  val manageControlViewModel: ManageControlViewModel = hiltViewModel()
  val uiState by manageControlViewModel.manageControlUiState.collectAsStateWithLifecycle()
  val activeControl by manageControlViewModel.activeControlStateFlow.collectAsStateWithLifecycle()

  val currentNavToNoControl by rememberUpdatedState(navToNoControl)

  LaunchedEffect(activeControl) {
    if (activeControl == null) {
      Timber.d("navActions.navigateToNoControl()")
      currentNavToNoControl()
    }
  }

  uiState.message?.let {
    val messageString =
        when (it) {
          is IntEventMessage -> stringResource(id = it.message)
          is StringEventMessage -> it.message
        }
    LaunchedEffect(uiState.message, uiState.isLoading) {
      Timber.d(messageString)
      snackbarHostState.showSnackbar(messageString)
      manageControlViewModel.onConsumedMessage()
    }
  }

  Timber.d("isAdded: $isAdded")

  Scaffold(
      snackbarHost = {
        SnackbarHost(snackbarHostState) { data ->
          Snackbar(
              containerColor =
                  if (uiState.isSuccess) SnackbarDefaults.color
                  else MaterialTheme.colorScheme.errorContainer,
              contentColor =
                  if (uiState.isSuccess) SnackbarDefaults.contentColor
                  else MaterialTheme.colorScheme.error) {
                Text(data.visuals.message)
              }
        }
      },
      topBar = {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.menu_manage_control)) },
            navigationIcon = {
              IconButton(onClick = openDrawer) {
                Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer))
              }
            },
            actions = {
              ManageControlMenu(
                  registerControlAction = { manageControlViewModel.registerControl() },
                  deleteControlAction = { manageControlViewModel.deleteControl() },
                  fetchAllDataAction = { manageControlViewModel.fetchAllData() },
                  requestChannelListAction = { manageControlViewModel.fetchChannelList() },
                  enableWOLAction = { manageControlViewModel.wakeOnLan() },
                  checkConnectivityAction = { manageControlViewModel.checkAvailability() },
                  enabled = activeControl != null)
            })
      },
      modifier = modifier.fillMaxSize()) { innerPadding ->
        ManageControlContent(
            modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp),
            sonyControl = activeControl)
      }
}

@Composable
private fun ManageControlContent(modifier: Modifier, sonyControl: SonyControl?) {
  val scrollState = rememberScrollState()
  Column(modifier = modifier.verticalScroll(scrollState)) {
    PropertyItem(
        label = stringResource(id = R.string.manage_control_host_name),
        value = sonyControl?.ip ?: "",
        showDivider = true)
    PropertyItem(
        label = stringResource(id = R.string.manage_control_nick_name),
        value = sonyControl?.nickname ?: "",
        showDivider = true)
    PropertyItem(
        label = stringResource(id = R.string.manage_control_device_name),
        value = sonyControl?.devicename ?: "",
        showDivider = true)
    PropertyItem(
        label = stringResource(id = R.string.manage_control_uuid),
        value = sonyControl?.uuid ?: "",
        showDivider = true)
    PropertyItem(
        label = stringResource(id = R.string.manage_control_model),
        value = sonyControl?.systemModel ?: "",
        showDivider = true)
    PropertyItem(
        label = stringResource(id = R.string.manage_control_mac),
        value = sonyControl?.systemMacAddr ?: "",
        showDivider = true)
    PropertyItem(
        label = stringResource(id = R.string.manage_control_wol_mode),
        value = sonyControl?.systemWolMode?.toString() ?: "",
        showDivider = true)
    PropertyItem(
        label = stringResource(id = R.string.manage_control_number_channels),
        value = sonyControl?.channelList?.size?.toString() ?: "",
        showDivider = true)
    PropertyItem(
        label = stringResource(id = R.string.manage_control_sources),
        value =
            sonyControl?.sourceList?.joinToString { source -> source.substringAfter("tv:") } ?: "",
        showDivider = false)
  }
}

@Composable
fun PropertyItem(label: String, value: String?, showDivider: Boolean = true) {
  ListItem(
      headlineContent = { Text(text = label) },
      supportingContent = { Text(text = if (value.isNullOrEmpty()) "" else value) })
  if (showDivider) HorizontalDivider()
}

@Composable
fun ManageControlMenu(
    registerControlAction: () -> Unit,
    deleteControlAction: () -> Unit,
    fetchAllDataAction: () -> Unit,
    requestChannelListAction: () -> Unit,
    enableWOLAction: () -> Unit,
    checkConnectivityAction: () -> Unit,
    enabled: Boolean
) {
  TopAppBarDropdownMenu(
      iconContent = { Icon(Icons.Filled.MoreVert, stringResource(id = R.string.menu_more)) }) {
          // Here closeMenu stands for the lambda expression parameter that is passed when this
          // trailing lambda expression is called as 'content' variable in the TopAppBarDropdownMenu
          // The specific expression is: {expanded = ! expanded}, which effectively closes the menu
          closeMenu ->
        val openDialog = remember { mutableStateOf(false) }
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.register_control_action)) },
            onClick = {
              registerControlAction()
              closeMenu()
            },
            enabled = enabled)
        DropdownMenuItem(
            text = {
              DeleteControlDialog(deleteControlAction, openDialog.value) {
                openDialog.value = false
                closeMenu()
              }
            },
            onClick = { openDialog.value = true /*closeMenu()*/ },
            enabled = enabled)
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.fetch_all_data_action)) },
            onClick = {
              fetchAllDataAction()
              closeMenu()
            },
            enabled = enabled)
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.get_tv_channel_list_action)) },
            onClick = {
              requestChannelListAction()
              closeMenu()
            },
            enabled = enabled)
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.enable_wol_action)) },
            onClick = {
              enableWOLAction()
              closeMenu()
            },
            enabled = enabled)
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.check_set_host)) },
            onClick = {
              checkConnectivityAction()
              closeMenu()
            },
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
        title = { Text(text = "Confirm delete") },
        text = { Text("Do you want to delete this control?") },
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
        dismissButton = { TextButton(onClick = { closeDialog() }) { Text("No") } })
  }
}
