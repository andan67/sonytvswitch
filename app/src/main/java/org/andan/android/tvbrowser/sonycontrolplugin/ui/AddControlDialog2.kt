package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.network.PowerStatusResponse
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus
import org.andan.android.tvbrowser.sonycontrolplugin.network.Resource
import org.andan.android.tvbrowser.sonycontrolplugin.network.Status
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.AddControlViewModel
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel
import timber.log.Timber

enum class AddControlState() {
    SPECIFY_HOST,
    SPECIFY_HOST_VALIDATING,
    REGISTER,
    REGISTER_VALIDATING
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AddControlDialog2(
    navActions: NavigationActions,
    viewModel: AddControlViewModel
) {
    var addControlState by rememberSaveable { mutableStateOf(AddControlState.SPECIFY_HOST) }
    var host by rememberSaveable { mutableStateOf("") }

    var nickname by rememberSaveable { mutableStateOf("") }
    var devicename by rememberSaveable { mutableStateOf("") }
    var registrationCode  by rememberSaveable { mutableStateOf("") }

    // init powerStatus for null safety reasons
    val powerStatus by viewModel.powerStatus2.observeAsState(Resource.Init())
    val registrationStatus by viewModel.registrationResult.observeAsState(RegistrationStatus.REGISTRATION_INIT)
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    Timber.d("addControlState: $addControlState")
    Timber.d("host: $host")
    Timber.d("powerStatus: $powerStatus")

    if (addControlState == AddControlState.SPECIFY_HOST_VALIDATING && powerStatus.status == Status.SUCCESS) {
        addControlState = AddControlState.REGISTER
        Timber.d("set state to register")
    }

    AlertDialog(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth(0.8f),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = {
            navActions.navigateUp()
        },
        title = {
            when (addControlState) {
                AddControlState.REGISTER -> {
                    Text(text = stringResource(id = R.string.add_control_register_title))
                }
                else -> {
                    Text(text = stringResource(id = R.string.add_control_specify_host_title))
                }
            }
        },
        confirmButton = {
            when (addControlState) {
                AddControlState.SPECIFY_HOST, AddControlState.SPECIFY_HOST_VALIDATING -> {
                    TextButton(
                        onClick =
                        {

                            if (host.contains("sample")) {
                                viewModel.addSampleControl(context, host)
                                navActions.navigateUp()
                            } else {
                                viewModel.addedControlHostAddress = host
                                // fetch power status to check for host validity/connectivity
                                coroutineScope.launch {
                                    viewModel.fetchPowerStatus(viewModel.addedControlHostAddress)
                                }
                                addControlState = AddControlState.SPECIFY_HOST_VALIDATING
                            }

                        },
                        enabled = host.isNotBlank() && powerStatus.status != Status.LOADING
                    ) {
                        Text(stringResource(id = R.string.add_control_host_confirm))
                    }

                }
                AddControlState.REGISTER -> {
                    TextButton(
                        onClick = {
                            navActions.navigateUp()
                        }
                    ) {
                        Text(stringResource(id = R.string.add_control_register_pos))
                    }
                }
                else -> {}
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    navActions.navigateUp()
                }
            ) {
                Text("Dismiss")
            }
        },
        text = {
            Column {
                when (addControlState) {
                    AddControlState.SPECIFY_HOST, AddControlState.SPECIFY_HOST_VALIDATING -> {
                        LaunchedEffect(true) {
                            viewModel.fetchSonyIpAndDeviceList()
                        }
                        var expanded by remember { mutableStateOf(false) }
                        val sonyIpAndDeviceList by viewModel.sonyIpAndDeviceList.observeAsState()
                        Text(text = stringResource(id = R.string.add_control_host_instructions))
                        ExposedDropdownMenuBox(
                            modifier = Modifier.padding(top = 16.dp),
                            expanded = expanded,
                            onExpandedChange = {
                                expanded = !expanded
                            }
                        ) {
                            TextField(
                                modifier = Modifier.menuAnchor(),
                                value = host,
                                onValueChange = { host = it },
                                label = { Text(stringResource(id = R.string.add_control_host_title)) },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = expanded
                                    )
                                },
                                singleLine = true,
                                isError = powerStatus.status == Status.ERROR,
                                supportingText = {
                                    when (powerStatus.status) {
                                        Status.ERROR -> Text(text = stringResource(id = R.string.add_control_host_failed_msg))
                                        Status.LOADING -> Text(text = stringResource(id = R.string.add_control_host_testing_msg))
                                        else -> Text("")
                                    }
                                }
                                //keyboardActions = KeyboardActions(onDone = {validateHost(host)})
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = {
                                    expanded = false
                                }
                            ) {
                                sonyIpAndDeviceList?.forEach {
                                    DropdownMenuItem(
                                        onClick = {
                                            host = it.ip
                                            expanded = false
                                        },
                                        text = { Text(text = it.device) }
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        Text(text = stringResource(id = R.string.add_control_register_instructions))
                        TextField(
                            modifier = Modifier.padding(top = 16.dp),
                            value = nickname,
                            onValueChange = { nickname = it },
                            label = { Text(stringResource(id = R.string.add_control_nick)) },
                            singleLine = true,
                        )
                        TextField(
                            modifier = Modifier.padding(top = 16.dp),
                            value = devicename,
                            onValueChange = { devicename = it },
                            label = { Text(stringResource(id = R.string.add_control_device)) },
                            singleLine = true,
                        )
                        TextField(
                            modifier = Modifier.padding(top = 16.dp),
                            value = registrationCode,
                            onValueChange = { registrationCode = it },
                            label = { Text(stringResource(id = R.string.add_control_psk)) },
                            singleLine = true,
                        )
                    }
                }
            }
        }
    )
}