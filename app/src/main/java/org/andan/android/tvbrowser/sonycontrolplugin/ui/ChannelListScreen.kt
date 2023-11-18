package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.domain.PlayingContentInfo
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyChannel
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.ChannelListViewModel
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelListScreen(
    modifier: Modifier = Modifier,
    navActions: NavigationActions,
    viewModel: ChannelListViewModel = hiltViewModel(),
    openDrawer: () -> Unit
) {
    val channelListState = viewModel.filteredChannelList.collectAsStateWithLifecycle()
    /*val playingContentInfoState =
        viewModel.playingContentInfo.observeAsState(initial = PlayingContentInfo())*/
    var searchText by rememberSaveable { mutableStateOf("") }

/*    LaunchedEffect(Unit) {
        viewModel.fetchPlayingContentInfo()
    }*/

    Timber.d("ChannelListScreen")

    Scaffold(
        topBar = {
            ChannelTopAppBar(
                openDrawer = { openDrawer() },
                searchText = searchText,
                onSearchTextChanged = {
                    searchText = it
                    viewModel.filter = it
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
        Column(modifier = Modifier.padding(innerPadding)) {
            //PlayingContentInfoHeaderContent(playingContentInfoState = playingContentInfoState, onclick = {navActions.navigateToPlayingContentInfoDetails()} )
            ChannelListContent(channelListState = channelListState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelTopAppBar(
    openDrawer: () -> Unit,
    searchText: String,
    onSearchTextChanged: (String) -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    var searchIsActive by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if(searchIsActive) {
            focusRequester.requestFocus()
        }
    }

    TopAppBar(title = { Text(text = stringResource(id = R.string.menu_list_channels)) },
        navigationIcon = {
            IconButton(onClick = openDrawer) {
                Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer))
            }
        },
        actions = {
            if(searchIsActive) {
                SearchTextField(
                    modifier = Modifier
                        //.padding(start = 48.dp)
                        .padding(vertical = 2.dp)
                        .width(148.dp)
                        .focusRequester(focusRequester),
                    searchText = searchText,
                    onSearchTextChanged = onSearchTextChanged,
                    onClose = { searchIsActive = false }
                )
            } else {
                IconButton(
                    modifier = Modifier,
                    onClick = { searchIsActive = true }) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = stringResource(id = R.string.search_channels)
                    )
                }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchTextField(modifier: Modifier,
                    searchText: String,
                    placeholderText: String = "Search...",
                    onSearchTextChanged: (String) -> Unit = {},
                    onClose: () -> Unit = {}) {
    var showClearButton by remember { mutableStateOf(true) }
    val keyboardController = LocalSoftwareKeyboardController.current
    //val focusRequester = remember { FocusRequester() }

/*    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }*/

    OutlinedTextField(
        modifier = modifier,
/*            .onFocusChanged { focusState ->
                showClearButton = (focusState.isFocused)
            },
            .focusRequester(focusRequester),*/
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
                IconButton(onClick = { if(searchText.isEmpty()) onClose() else onSearchTextChanged("")}) {
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
        keyboardActions = KeyboardActions(onDone = {keyboardController?.hide()
        }),
    )
}

@Composable
private fun ChannelListContent(
    channelListState: State<List<SonyChannel>>
) {
    Timber.d("ChannelListContent")
    LazyColumn() {
        items(channelListState.value) { channel ->
            ChannelItem(
                channel = channel,
                onclick = { Timber.d("Clicked: $it") }
            )
        }
    }
}

@Composable
fun PlayingContentInfoHeaderContent(playingContentInfoState: State<PlayingContentInfo>, onclick : () -> Unit) {
    val playingContentInfo = playingContentInfoState.value
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.secondaryContainer)
            .clickable { Timber.d("Clicked: ${playingContentInfo.source}"); onclick() }) {
        Column {
            Text(
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
                    .width(56.dp),
                style = MaterialTheme.typography.titleLarge,
                text = playingContentInfo.dispNum,
                textAlign = TextAlign.Right
            )
        }
        Column() {
            Text(
                modifier = Modifier.padding(horizontal = 0.dp),
                style = MaterialTheme.typography.titleLarge,
                text = playingContentInfo.title
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.padding(end = 8.dp),
                    painter = painterResource(id = R.drawable.ic_play_arrow),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    text = playingContentInfo.programTitle,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.padding(end = 8.dp),
                    painter = painterResource(id = R.drawable.ic_access_time),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    text = playingContentInfo.getStartEndTimeFormatted()!!,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.padding(end = 8.dp),
                    painter = painterResource(id = R.drawable.ic_input),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    text = playingContentInfo.source,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
    Divider(modifier = Modifier.padding(vertical = 4.dp))
}


@Composable
fun ChannelItem(channel: SonyChannel, onclick: (SonyChannel) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onclick(channel) }) {
        Column {
            Text(
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
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
