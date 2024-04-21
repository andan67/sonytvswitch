package org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.channellist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import org.andan.android.tvbrowser.sonycontrolplugin.domain.PlayingContentInfo
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyChannel
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.misc.SearchTextField
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.misc.TopAppBarWithSearch
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.misc.WOLScreenOnOffMenu
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.navigation.NavigationActions
import timber.log.Timber

@Composable
fun ChannelListScreen(
    modifier: Modifier = Modifier,
    navActions: NavigationActions,
    viewModel: ChannelListViewModel = hiltViewModel(),
    openDrawer: () -> Unit
) {
  val channelListState = viewModel.filteredChannelList.collectAsStateWithLifecycle()

  val channelListUIState = viewModel.channelListUiState.collectAsStateWithLifecycle()

  var searchText by rememberSaveable { mutableStateOf("") }

  LaunchedEffect(Unit) { viewModel.fetchPlayingContentInfo() }

  Timber.d("ChannelListScreen")

  Scaffold(
      topBar = {
        TopAppBarWithSearch(
          title = stringResource(id = R.string.channel_list_title),
          onIconClick = { openDrawer() },
          searchText = searchText,
          onSearchTextChanged = {
            searchText = it
            viewModel.filter = it
          },
          menuContent = {
            WOLScreenOnOffMenu(
              enableWOLAction = { viewModel.wakeOnLan() },
              screenOffAction = { viewModel.setPowerSavingMode("pictureOff") },
              screenOnAction = { viewModel.setPowerSavingMode("off") })
          }
        )
      },
      modifier = modifier.fillMaxSize(),
      floatingActionButton = {
        FloatingActionButton(onClick = { Timber.d("Switch channel"); viewModel.switchToLastChannel() }) {
          Icon(
              painter = painterResource(id = R.drawable.baseline_autorenew_24),
              contentDescription = stringResource(id = R.string.switch_channel))
        }
      }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp)) {
          PlayingContentInfoHeaderContent(
              playingContentInfo = channelListUIState.value.playingContentInfo,
              onclick = { (_) -> navActions.navigateToPlayingContentInfoDetails() })
          ChannelListContent(
              channelListState = channelListState, onChannelClick = viewModel::switchToChannel)
        }
      }
}

@Composable
private fun ChannelListContent(
    channelListState: State<List<Pair<SonyChannel, String?>>>,
    onChannelClick: (String) -> Unit
) {
  Timber.d("ChannelListContent")
  LazyColumn {
    items(channelListState.value) { channel ->
      ChannelItem(
          channel = channel.first,
          tvbChannelTitle = channel.second,
          onclick = {
            Timber.d("Clicked: $it")
            onChannelClick(it.uri)
          })
    }
  }
}

@Composable
fun PlayingContentInfoHeaderContent(
    playingContentInfo: PlayingContentInfo,
    onclick: (SonyChannel) -> Unit
) {
  ChannelItem(
      channel = SonyChannel(playingContentInfo),
      tvbChannelTitle = "",
      // onclick = {Timber.d("Clicked ${playingContentInfoState.value}"); onclick},
      onclick = onclick,
      isHeader = true,
      playingContentInfo = playingContentInfo)
  HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
private fun ChannelItem(
    channel: SonyChannel,
    tvbChannelTitle: String?,
    onclick: (SonyChannel) -> Unit,
    isHeader: Boolean = false,
    playingContentInfo: PlayingContentInfo = PlayingContentInfo()
) {
  Row(modifier = Modifier.padding(vertical = 2.dp).fillMaxWidth().clickable { onclick(channel) }) {
    Column {
      Text(
          modifier = Modifier.padding(end = 8.dp).width(40.dp),
          style = MaterialTheme.typography.titleSmall,
          text = channel.dispNumber,
          textAlign = TextAlign.Right)
    }
    Column {
      Text(
          modifier = Modifier.padding(horizontal = 0.dp),
          style = MaterialTheme.typography.titleSmall,
          text = channel.title)
      if (isHeader) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
              modifier = Modifier.padding(end = 4.dp).width(16.dp),
              painter = painterResource(id = R.drawable.outline_play_arrow_24),
              contentDescription = null,
              tint = MaterialTheme.colorScheme.secondary)
          Text(
              style = MaterialTheme.typography.bodyMedium,
              text = playingContentInfo.programTitle,
              color = MaterialTheme.colorScheme.secondary)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
              modifier = Modifier.padding(end = 4.dp).width(16.dp),
              painter = painterResource(id = R.drawable.outline_access_time_24),
              contentDescription = null,
              tint = MaterialTheme.colorScheme.secondary)
          Text(
              style = MaterialTheme.typography.bodyMedium,
              text = playingContentInfo.getStartEndTimeFormatted()!!,
              color = MaterialTheme.colorScheme.secondary)
        }
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier.padding(end = 4.dp).width(16.dp),
            painter = painterResource(id = R.drawable.ic_input),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary)
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = channel.shortSource,
            color = MaterialTheme.colorScheme.secondary)
        if (!tvbChannelTitle.isNullOrEmpty()) {
          Icon(
              modifier = Modifier.padding(start = 8.dp, end = 4.dp).width(16.dp),
              painter = painterResource(id = R.drawable.tvb_2),
              contentDescription = null,
              tint = MaterialTheme.colorScheme.secondary)
          Text(
              style = MaterialTheme.typography.bodyMedium,
              text = tvbChannelTitle,
              color = MaterialTheme.colorScheme.secondary)
        }
      }
    }
  }
}
