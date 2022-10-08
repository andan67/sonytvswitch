package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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

@Composable
fun ChannelListScreen(
    modifier: Modifier = Modifier,
    viewModel: SonyControlViewModel = viewModel(),
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    navActions: NavigationActions,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    val channelList by viewModel.filteredChannelList.observeAsState(initial = emptyList())
    //AppNavHost(navHostController = navHostController, scaffoldState = scaffoldState )
    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = {
            AppDrawer(closeDrawer = {coroutineScope.launch { scaffoldState.drawerState.close()   }} , navActions = navActions)
        },
        topBar = {
            ChannelTopAppBar(
                openDrawer = {coroutineScope.launch { scaffoldState.drawerState.open()   }; Timber.d("openDrawer") },
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
private fun ChannelListMenu(
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
        DropdownMenuItem(onClick = { onWakeOnLan(); closeMenu() }) {
            Text(text = stringResource(id = R.string.wol_action))
        }
        DropdownMenuItem(onClick = { onScreenOff(); closeMenu() }) {
            Text(text = stringResource(id = R.string.screen_off_action))
        }
        DropdownMenuItem(onClick = { onScreenOn(); closeMenu() }) {
            Text(text = stringResource(id = R.string.screen_on_action))
        }
    }
}

@Composable
fun ChannelSearchListScreen(
    modifier: Modifier = Modifier,
    viewModel: SonyControlViewModel = viewModel(),
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    navActions: NavigationActions,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    val channelList by viewModel.filteredChannelList.observeAsState(initial = emptyList())
    //AppNavHost(navHostController = navHostController, scaffoldState = scaffoldState )
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ChannelSearchTopAppBar(
                onNavigateBack = {
                    navActions.navigateToChannelList()
                },
                searchText = "Search Text",
                onSearchTextChanged = {
                    Timber.d("onSearchTextChanged")
                },
                onClearClick = {
                    Timber.d("onClearClick")
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

@Composable
fun ChannelSearchTopAppBar(
    searchText: String,
    placeholderText: String = "",
    onSearchTextChanged: (String) -> Unit = {},
    onClearClick: () -> Unit = {},
    onNavigateBack: () -> Unit,
) {
    var showClearButton by remember { mutableStateOf(false) }
    val keyboardController = LocalTextInputService.current
    val focusRequester = remember { FocusRequester() }
    var searchText by rememberSaveable { mutableStateOf("") }

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
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .onFocusChanged { focusState ->
                        showClearButton = (focusState.isFocused)
                    }
                    .focusRequester(focusRequester),
                value = searchText,
                //TODO: Proper implementation
                //onValueChange = onSearchTextChanged,
                onValueChange = {searchText = it },
                placeholder = {
                    Text(text = placeholderText)
                },
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    backgroundColor = Color.Transparent,
                    cursorColor = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
                ),
                trailingIcon = {
                    AnimatedVisibility(
                        visible = showClearButton,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(onClick = { onClearClick() }) {
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
    LazyColumn {
        items(channelList) { channel ->
            ChannelItem(
                channel = channel
                //name = channel.title
                //name = channel.dispNumber
            )
        }
    }
}

@Preview()
@Composable
fun ChannelItem(@PreviewParameter(SampleUserProvider::class) channel: SonyChannel) {
    Row {
        Column {
            Text( modifier = Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.h6,
                text = channel.dispNumber)
        }
        Column() {
            Text(style = MaterialTheme.typography.h6,
                text = channel.title)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(modifier = Modifier.padding(end = 8.dp),
                    painter = painterResource(id = R.drawable.ic_input),
                    contentDescription = null,
                    tint = MaterialTheme.colors.secondary)
                Text(style = MaterialTheme.typography.subtitle1,
                    text = channel.shortSource,
                color = MaterialTheme.colors.secondary)}
        }
    }
}


class SampleUserProvider: PreviewParameterProvider<SonyChannel> {
    override val values = sequenceOf(
        SonyChannel(
            title = "Das Erste",
            dispNumber = "0001",
            source = "tv:dvbs",
            index = 0,
            uri = "tv:dvbs?trip\\u003d1.1019.10301\\u0026srvName\\u003dDas%20Erste%20HD",
            mediaType = "tv"
        ))
}
