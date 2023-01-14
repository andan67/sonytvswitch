package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.AddControlStatus
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.AddControlViewModel
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel
import timber.log.Timber

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalLifecycleComposeApi::class
)
@Composable
fun AddControlDialog(
    navActions: NavigationActions,
    viewModel: SonyControlViewModel
) {
    //var addControlState by rememberSaveable { mutableStateOf(AddControlState.SPECIFY_HOST) }
    var host by rememberSaveable { mutableStateOf("") }

    var nickname by rememberSaveable { mutableStateOf("") }
    var devicename by rememberSaveable { mutableStateOf("") }
    var preSharedKey by rememberSaveable { mutableStateOf("") }
    var challengeCode by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current

    val addControlViewModel: AddControlViewModel = viewModel()

    val uiState by addControlViewModel.addControlUiState.collectAsStateWithLifecycle()


    //Timber.d("addControlState: $addControlState")
    Timber.d("host: $host")
    Timber.d("uiState: $uiState")

    if(uiState.status == AddControlStatus.REGISTER_SUCCESS) {
        viewModel.addControl(addControlViewModel.addedControl)
        viewModel.postRegistrationFetches()
        navActions.navigateUp()
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
            when (uiState.status) {
                AddControlStatus.REGISTER -> {
                    Text(text = stringResource(id = R.string.add_control_register_title))
                }
                else -> {
                    Text(text = stringResource(id = R.string.add_control_specify_host_title))
                }
            }
        },
        confirmButton = {
            when (uiState.status) {
                AddControlStatus.SPECIFY_HOST, AddControlStatus.SPECIFY_HOST_NOT_AVAILABE -> {
                    TextButton(
                        onClick =
                        {

                            if (host.contains("sample")) {
                                viewModel.addSampleControl(context, host)
                                navActions.navigateUp()
                            } else {
                                //viewModel.addedControlHostAddress = host
                                // fetch power status to check for host validity/connectivity
                                addControlViewModel.checkAvailabilityOfHost(host)
                            }

                        },
                        enabled = host.isNotBlank() && !uiState.isLoading // && powerStatus.status != Status.LOADING
                    ) {
                        Text(stringResource(id = R.string.add_control_host_confirm))
                    }

                }
                AddControlStatus.REGISTER, AddControlStatus.REGISTER_ERROR -> {
                    TextButton(
                        onClick = {
                            addControlViewModel.registerControl(
                                nickname,
                                devicename,
                                preSharedKey,
                                ""
                            )
                        },
                        enabled = nickname.isNotBlank() && devicename.isNotBlank() && !uiState.isLoading
                    ) {
                        Text(stringResource(id = R.string.add_control_register_pos))
                    }
                }
                AddControlStatus.REGISTER_CHALLENGE, AddControlStatus.REGISTER_CHALLENGE_ERROR -> {
                    TextButton(
                        onClick = {
                            addControlViewModel.registerControl(
                                nickname,
                                devicename,
                                "",
                                challengeCode
                            )
                        },
                        enabled = challengeCode.length == 4 && !uiState.isLoading
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
                when (uiState.status) {
                    AddControlStatus.SPECIFY_HOST, AddControlStatus.SPECIFY_HOST_NOT_AVAILABE -> {
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
                                isError = !uiState.isLoading && uiState.status == AddControlStatus.SPECIFY_HOST_NOT_AVAILABE,
                                supportingText = {
                                    if (uiState.isLoading) {
                                        Text(text = stringResource(id = R.string.add_control_host_testing_msg))
                                    } else if (uiState.status == AddControlStatus.SPECIFY_HOST_NOT_AVAILABE) {
                                        Text(text = stringResource(id = R.string.add_control_host_failed_msg))
                                    } else {
                                        Text("")
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
                            enabled = uiState.status < AddControlStatus.REGISTER_CHALLENGE
                        )
                        TextField(
                            modifier = Modifier.padding(top = 16.dp),
                            value = devicename,
                            onValueChange = { devicename = it },
                            label = { Text(stringResource(id = R.string.add_control_device)) },
                            singleLine = true,
                            enabled = uiState.status < AddControlStatus.REGISTER_CHALLENGE
                        )
                        if (uiState.status < AddControlStatus.REGISTER_CHALLENGE) {
                            TextField(
                                modifier = Modifier.padding(top = 16.dp),
                                value = preSharedKey,
                                onValueChange = { preSharedKey = it },
                                label = { Text(stringResource(id = R.string.add_control_psk)) },
                                singleLine = true
                            )
                        } else {
                            TextField(
                                modifier = Modifier.padding(top = 16.dp),
                                value = challengeCode,
                                onValueChange = { challengeCode = it },
                                label = { Text(stringResource(id = R.string.add_control_challenge_hint)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = !uiState.isLoading && uiState.status == AddControlStatus.REGISTER_CHALLENGE_ERROR,
                                supportingText = {
                                    if (uiState.isLoading) {
                                        Text(text = stringResource(id = R.string.add_control_host_testing_msg))
                                    } else if (uiState.status == AddControlStatus.REGISTER_CHALLENGE_ERROR && uiState.message != null) {
                                        Text(text = uiState.message!!)
                                    } else if (uiState.status == AddControlStatus.REGISTER_CHALLENGE_ERROR && uiState.messageId != null) {
                                        Text(text = stringResource(uiState.messageId!!))
                                    } else {
                                        Text("")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}