package org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.channelmap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.misc.TopAppBarWithSearch
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.navigation.NavigationActions
import timber.log.Timber

@Composable
fun ChannelSingleMapScreen(
    modifier: Modifier = Modifier,
    navActions: NavigationActions,
    viewModel: ChannelSingleMapViewModel = hiltViewModel(),
    channelKey: String = ""
) {
  val matchedChannelsState = viewModel.matchedChannels.collectAsStateWithLifecycle()

  var searchText by rememberSaveable { mutableStateOf("") }

  val uiState by viewModel.channelSingleMapUiState.collectAsStateWithLifecycle()

  Timber.d("ChannelSingleMapScreen")

  Scaffold(
      topBar = {
        TopAppBarWithSearch(
            title = stringResource(id = R.string.menu_match_single_channel),
            onIconClick = { navActions.navigateUp() },
            iconImage = Icons.AutoMirrored.Filled.ArrowBack,
            searchText = searchText,
            onSearchTextChanged = {
              searchText = it
              viewModel.filter = it
            },
            menuContent = {
              IconButton(onClick = { viewModel.saveNewMap(null) }) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_clear_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary)
              }
            })
      },
      modifier = modifier.fillMaxSize(),
  ) { innerPadding ->
    Column(modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp)) {
      ChannelSingleMapContent(
          channelMapItem = uiState.channelMapItem,
          matchedChannelsState = matchedChannelsState,
          onChannelClick = { channel: SonyChannel? ->
            Timber.d("Clicked: ${channel?.title ?: ""}")
            viewModel.saveNewMap(channel)
          })
    }
  }
}

@Composable
private fun ChannelSingleMapContent(
    channelMapItem: Pair<String, SonyChannel?>?,
    matchedChannelsState: State<List<SonyChannel?>>,
    onChannelClick: (SonyChannel?) -> Unit
) {
  if (channelMapItem != null) {
    Column {
      Column(modifier = Modifier.background(color = MaterialTheme.colorScheme.surfaceVariant)) {
        ChannelSingleMapItem(
            tvbChannelName = channelMapItem.first,
            channel = channelMapItem.second,
            onChannelClick = {})
        Text(
            modifier = Modifier.padding(all = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            text = stringResource(id = R.string.channel_map_select_channel_text),
        )
        HorizontalDivider(
            modifier = Modifier.background(color = MaterialTheme.colorScheme.outline),
            thickness = 1.dp)
      }
      LazyColumn {
        itemsIndexed(matchedChannelsState.value) { index, channel ->
          ChannelMatchItem(index = index + 1, channel = channel, onChannelClick = onChannelClick)
          // Divider(Modifier.height(1.dp))
        }
      }
    }
  }
}

@Composable
private fun ChannelSingleMapItem(
    tvbChannelName: String,
    channel: SonyChannel?,
    onChannelClick: (SonyChannel?) -> Unit
) {
  Row {
    Column {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
        Icon(
            modifier = Modifier.padding(start = 24.dp, end = 8.dp).requiredWidth(24.dp),
            painter = painterResource(id = R.drawable.tvb_2),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary)
        Text(
            modifier = Modifier.padding(horizontal = 0.dp),
            style = MaterialTheme.typography.titleLarge,
            text = tvbChannelName)
      }
      Row { ChannelMatchItem(channel = channel, onChannelClick = onChannelClick) }
    }
  }
}

@Composable
private fun ChannelMatchItem(
    index: Int = -1,
    channel: SonyChannel?,
    onChannelClick: (SonyChannel?) -> Unit
) {
  Row(
      modifier =
          Modifier.padding(bottom = 4.dp).horizontalScroll(rememberScrollState()).clickable {
            onChannelClick(channel)
          },
      verticalAlignment = Alignment.CenterVertically) {
        // Row {
        Text(
            modifier = Modifier.padding(end = 8.dp).width(48.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
            text = if (index >= 0) index.toString() else "",
            textAlign = TextAlign.Right)
        Icon(
            modifier = Modifier.padding(end = 4.dp).width(16.dp),
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
              text = "<unmapped>",
              color = MaterialTheme.colorScheme.secondary)
        }
      }
}
