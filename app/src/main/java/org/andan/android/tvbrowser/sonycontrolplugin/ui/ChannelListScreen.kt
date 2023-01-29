package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyChannel
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelListScreen(
    modifier: Modifier = Modifier,
    navActions: NavigationActions,
    viewModel: SonyControlViewModel,
    openDrawer: () -> Unit
) {
    val channelList by viewModel.filteredChannelList.observeAsState(initial = emptyList())
    Scaffold(
        topBar = {
            ChannelTopAppBar(
                openDrawer = { openDrawer() },
                onSearchBarClick = {
                    navActions.navigateToChannelListSearch()
                    Timber.d("Clicked search bar")
                }
            )
        },
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = {
                Timber.d("Switch channel")
            })
            {
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_autorenew_24),
                    contentDescription = stringResource(id = R.string.switch_channel)
                )
            }
        }
    ) { innerPadding ->
        ChannelListContent(
            modifier = Modifier.padding(innerPadding),
            channelNameList = viewModel.channelNameList,
            channelList = channelList
            //channelListState = channelListState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelTopAppBar(
    openDrawer: () -> Unit,
    onSearchBarClick: () -> Unit
) {
    TopAppBar(title = { Text(text = stringResource(id = R.string.menu_list_channels)) },
        navigationIcon = {
            IconButton(onClick = openDrawer) {
                Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer))
            }
        },
        actions = {
            IconButton(
                modifier = Modifier,
                onClick = { onSearchBarClick() }) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = stringResource(id = R.string.search_channels)
                )
            }
            ChannelListMenu({}, {}, {})
        })
}

@Composable
fun ChannelListMenu(
    onWakeOnLan: () -> Unit,
    onScreenOff: () -> Unit,
    onScreenOn: () -> Unit
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
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.wol_action)) },
            onClick = { onWakeOnLan(); closeMenu() })
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.screen_off_action)) },
            onClick = { onScreenOff(); closeMenu() })
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.screen_on_action)) },
            onClick = { onScreenOn(); closeMenu() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelSearchListScreen(
    modifier: Modifier = Modifier,
    navActions: NavigationActions,
    viewModel: SonyControlViewModel
) {
    val channelList by viewModel.filteredChannelList.observeAsState(initial = emptyList())
    var searchText by rememberSaveable { mutableStateOf("") }
    Scaffold(
        topBar = {
            ChannelSearchTopAppBar(
                onNavigateBack = {
                    navActions.navigateToChannelList()
                },
                searchText = searchText,
                onSearchTextChanged = {
                    Timber.d("onSearchTextChanged: $it")
                    searchText = it
                    viewModel.filterChannelList(searchText)
                }
            )
        },
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = {
                Timber.d("Switch channel")
            })
            {
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_autorenew_24),
                    contentDescription = stringResource(id = R.string.switch_channel)
                )
            }
        }
    ) { innerPadding ->
        ChannelListContent(
            modifier = Modifier.padding(innerPadding),
            channelNameList = viewModel.channelNameList,
            channelList = channelList,
            //channelListState = channelListState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelSearchTopAppBar(
    searchText: String,
    placeholderText: String = "Search...",
    onSearchTextChanged: (String) -> Unit = {},
    onNavigateBack: () -> Unit,
) {
    var showClearButton by remember { mutableStateOf(false) }
    val keyboardController = LocalTextInputService.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    TopAppBar(
        title = { Text("") },
        navigationIcon = {
            IconButton(onClick = { onNavigateBack() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    modifier = Modifier,
                    contentDescription = stringResource(id = R.string.back_search_channels)
                )
            }
        },
        actions = {
            OutlinedTextField(
                modifier = Modifier
                    .padding(start = 48.dp)
                    .padding(vertical = 2.dp)
                    .onFocusChanged { focusState ->
                        showClearButton = (focusState.isFocused)
                    }
                    .focusRequester(focusRequester),
                value = searchText,
                //TODO: Proper implementation
                //onValueChange = onSearchTextChanged,
                onValueChange = { onSearchTextChanged(it) },
                placeholder = {
                    Text(text = placeholderText)
                },
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    containerColor = Color.Transparent,
                    cursorColor = LocalContentColor.current.copy(alpha = 0.4f)
                ),
                trailingIcon = {
                    AnimatedVisibility(
                        visible = showClearButton,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(onClick = { onSearchTextChanged("") }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(id = R.string.search_clear_content_description)
                            )
                        }

                    }
                },
                maxLines = 1,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hideSoftwareKeyboard()
                }),
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}


@Composable
private fun ChannelListContent(
    modifier: Modifier,
    channelNameList: List<String>,
    channelList: List<SonyChannel>
    //channelListState: State<List<SonyChannel>>
) {
    //SelectedChannelHeader()
    LazyColumn(modifier = modifier) {
        items(channelList) { channel ->
            ChannelItem(
                channel = channel,
                onclick = {Timber.d ("Clicked: $it")}
            )
        }
    }
}

@Composable
fun ChannelItem(channel: SonyChannel, onclick: (SonyChannel) -> Unit) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onclick(channel) }){
        Column {
            Text(
                modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                    .width(56.dp),
                style = MaterialTheme.typography.titleLarge,
                text = channel.dispNumber,
                textAlign = TextAlign.Right
            )
        }
        Column() {
            Text(
                modifier = Modifier.padding(horizontal = 0.dp),
                style = MaterialTheme.typography.titleLarge,
                text = channel.title
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.padding(end = 8.dp),
                    painter = painterResource(id = R.drawable.ic_input),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    text = channel.shortSource,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}


class SampleUserProvider : PreviewParameterProvider<SonyChannel> {
    override val values = sequenceOf(
        SonyChannel(
            title = "Das Erste",
            dispNumber = "0001",
            source = "tv:dvbs",
            index = 0,
            uri = "tv:dvbs?trip\\u003d1.1019.10301\\u0026srvName\\u003dDas%20Erste%20HD",
            mediaType = "tv"
        )
    )
}
