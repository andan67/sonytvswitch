package org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.channelmap

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyChannel
import org.andan.android.tvbrowser.sonycontrolplugin.ui.IntEventMessage
import org.andan.android.tvbrowser.sonycontrolplugin.ui.StringEventMessage
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.misc.TopAppBarWithSearch
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.navigation.NavigationActions
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.navigation.TopAppBarDropdownMenu
import timber.log.Timber

@Composable
fun ChannelMapScreen(
    modifier: Modifier = Modifier,
    navActions: NavigationActions,
    viewModel: ChannelMapViewModel = hiltViewModel(),
    openDrawer: () -> Unit
) {
  // val viewModel: ChannelMapViewModel = hiltViewModel()
  val snackbarHostState = remember { SnackbarHostState() }
  val uiState by viewModel.channelMapUiState.collectAsStateWithLifecycle()
  val channelMapState = viewModel.filteredChannelMap.collectAsStateWithLifecycle()
  val onMapItemClick =
      remember(navActions) { { s: String -> navActions.navigateToChannelSingleMap(s) } }
  var searchText by rememberSaveable { mutableStateOf("") }

  uiState.message?.let {
    val messageString =
        when (it) {
          is IntEventMessage -> stringResource(id = it.message)
          is StringEventMessage -> it.message
        }
    LaunchedEffect(uiState.message, uiState.isLoading) {
      Timber.d(messageString)
      snackbarHostState.showSnackbar(messageString)
      viewModel.onConsumedMessage()
    }
  }

  if (uiState.isLoading) {
    Box(Modifier.fillMaxSize()) { CircularProgressIndicator(Modifier.align(Alignment.Center)) }
  }

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
        TopAppBarWithSearch(
            title = stringResource(id = R.string.channel_map_title),
            onIconClick = { openDrawer() },
            searchText = searchText,
            onSearchTextChanged = {
              searchText = it
              viewModel.filter = it
            },
            menuContent = {
              ChannelMapMenu(
                  { viewModel.matchChannels() },
                  { viewModel.clearChannelMatches() },
                  { viewModel.deleteChannelMap() })
            })
      },
      modifier = modifier.fillMaxSize(),
  ) { innerPadding ->
    Column(modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp)) {
      ChannelMapContent(channelMapState = channelMapState, onChannelClick = onMapItemClick)
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      if (uiState.isLoading) {
        CircularProgressIndicator()
      }
    }
  }
}

@Composable
private fun ChannelMapContent(
    channelMapState: State<Map<String, SonyChannel?>>,
    onChannelClick: (String) -> Unit
) {
  Timber.d("ChannelMapContent")
  LazyColumn {
    itemsIndexed(channelMapState.value.keys.toList()) { index, channelName ->
      ChannelMapItem(
          index + 1,
          tvbChannelName = channelName,
          channel = channelMapState.value[channelName],
          onChannelClick = { channelKey: String ->
            Timber.d("channelKey: $channelKey")
            onChannelClick(channelKey)
          })
    }
  }
}

@Composable
private fun ChannelMapItem(
    index: Int,
    tvbChannelName: String,
    channel: SonyChannel?,
    onChannelClick: (String) -> Unit
) {
  Row(
      modifier =
          Modifier.padding(vertical = 2.dp)
              // .fillMaxWidth()
              .clickable { onChannelClick(tvbChannelName) }) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.padding(end = 8.dp).width(48.dp),
                style = MaterialTheme.typography.titleLarge,
                text = index.toString(),
                textAlign = TextAlign.Right)
            Text(
                modifier = Modifier.padding(horizontal = 0.dp),
                style = MaterialTheme.typography.titleLarge,
                text = tvbChannelName)
          }
          Row(
              modifier = Modifier.horizontalScroll(rememberScrollState()),
              verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.padding(end = 4.dp, start = 56.dp).width(16.dp),
                    painter = painterResource(id = R.drawable.baseline_tv_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary)
                if (channel != null) {
                  Text(
                      style = MaterialTheme.typography.bodyLarge,
                      text = channel.title,
                      color = MaterialTheme.colorScheme.secondary)
                  Icon(
                      modifier = Modifier.padding(start = 8.dp, end = 4.dp).width(16.dp),
                      painter = painterResource(id = R.drawable.ic_action_input),
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.secondary)
                  Text(
                      style = MaterialTheme.typography.bodyLarge,
                      text = channel.shortSource,
                      color = MaterialTheme.colorScheme.secondary)
                } else {
                  Text(
                      style = MaterialTheme.typography.bodyLarge,
                      text = "--unmapped--",
                      color = MaterialTheme.colorScheme.secondary)
                }
              }
        }
      }
  // Divider(Modifier.height(1.dp))
}

@Composable
private fun ChannelMapMenu(
    onMatchChannels: () -> Unit,
    onClearMappings: () -> Unit,
    onDeleteChannelMap: () -> Unit
) {
  TopAppBarDropdownMenu(
      iconContent = { Icon(Icons.Filled.MoreVert, stringResource(id = R.string.menu_more)) }) {
          // Here closeMenu stands for the lambda expression parameter that is passed when this
          // trailing lambda expression is called as 'content' variable in the TopAppBarDropdownMenu
          // The specific expression is: {expanded = ! expanded}, which effectively closes the menu
          closeMenu ->
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.menu_match_channels)) },
            onClick = {
              onMatchChannels()
              closeMenu()
            })
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.menu_clear_mappings)) },
            onClick = {
              onClearMappings()
              closeMenu()
            })
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.menu_delete_map)) },
            onClick = {
              onDeleteChannelMap()
              closeMenu()
            })
      }
}
