package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyChannel
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.ChannelMapViewModel
import timber.log.Timber

@Composable
fun ChannelMapScreen(
    modifier: Modifier = Modifier,
    navActions: NavigationActions,
    viewModel: ChannelMapViewModel,
    openDrawer: () -> Unit
) {
    val channelMapState = viewModel.filteredChannelMap.collectAsStateWithLifecycle()

    var searchText by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            ChannelMapTopAppBar(
                openDrawer = { openDrawer() },
                searchText = searchText,
                onSearchTextChanged = {
                    searchText = it
                    viewModel.filter = it
                },
                onMatchChannels = { viewModel.matchChannels() },
                onClearMatches = { viewModel.clearChannelMatches() }
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            ChannelMapContent(channelMapState = channelMapState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelMapTopAppBar(
    openDrawer: () -> Unit,
    searchText: String,
    onSearchTextChanged: (String) -> Unit = {},
    onMatchChannels: () -> Unit,
    onClearMatches: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var searchIsActive by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if(searchIsActive) {
            focusRequester.requestFocus()
        }
    }

    TopAppBar(title = { Text(text = stringResource(id = R.string.menu_channel_map)) },
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
            ChannelMapMenu(onMatchChannels, onClearMatches)
        })
}

@Composable
private fun ChannelMapContent(
    channelMapState: State<Map<String, SonyChannel?>>
) {
    LazyColumn() {
        itemsIndexed(channelMapState.value.keys.toList()) {index,  channelName ->
            ChannelMapItem(
                index + 1 ,
                tvbChannelName = channelName,
                channel = channelMapState.value[channelName],
                onclick = { Timber.d("Clicked: $channelName") }
            )
        }
    }
}



@Composable
fun ChannelMapItem(
    index: Int,
    tvbChannelName: String,
    channel: SonyChannel?,
    onclick: () -> Unit
)
{
    Row(
        modifier = Modifier
            //.fillMaxWidth()
            .clickable { onclick() }) {
        Column {
            Text(
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
                    .width(56.dp),
                style = MaterialTheme.typography.titleLarge,
                text = index.toString(),
                textAlign = TextAlign.Right
            )
        }
        Column() {
            Text(
                modifier = Modifier
                    .padding(horizontal = 0.dp),
                style = MaterialTheme.typography.titleLarge,
                text = tvbChannelName
            )
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically) {
            //Row {
                Icon(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .width(20.dp),
                    painter = painterResource(id = R.drawable.baseline_tv_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                if(channel != null) {
                    Text(
                        style = MaterialTheme.typography.titleMedium,
                        text = channel.title,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Icon(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .width(20.dp),
                        painter = painterResource(id = R.drawable.ic_action_input),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        style = MaterialTheme.typography.titleMedium,
                        text = channel.source,
                        color = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    Text(
                        style = MaterialTheme.typography.titleMedium,
                        text = "--unmapped--",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun ChannelMapMenu(
    onMatchChannels: () -> Unit,
    onClearMappings: () -> Unit
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
            text = { Text(text = stringResource(id = R.string.menu_match_channels)) },
            onClick = { onMatchChannels(); closeMenu() })
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.menu_clear_mappings)) },
            onClick = { onClearMappings(); closeMenu() })
    }
}