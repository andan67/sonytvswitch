package org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.channellist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.domain.PlayingContentInfo
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyChannel
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
        ChannelTopAppBar(
            openDrawer = { openDrawer() },
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
            })
      },
      modifier = modifier.fillMaxSize(),
      floatingActionButton = {
        FloatingActionButton(onClick = { Timber.d("Switch channel") }) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelTopAppBar(
    openDrawer: () -> Unit,
    searchText: String,
    onSearchTextChanged: (String) -> Unit = {},
    menuContent: @Composable() () -> Unit
) {
  val focusRequester = remember { FocusRequester() }
  var searchIsActive by rememberSaveable { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    if (searchIsActive) {
      focusRequester.requestFocus()
    }
  }

  TopAppBar(
      title = { Text(text = stringResource(id = R.string.menu_list_channels)) },
      navigationIcon = {
        IconButton(onClick = openDrawer) {
          Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer))
        }
      },
      actions = {
        if (searchIsActive) {
          SearchTextField(
              modifier =
                  Modifier
                      // .padding(start = 48.dp)
                      .padding(vertical = 2.dp)
                      .width(148.dp)
                      .focusRequester(focusRequester),
              searchText = searchText,
              onSearchTextChanged = onSearchTextChanged,
              onClose = { searchIsActive = false })
        } else {
          IconButton(modifier = Modifier, onClick = { searchIsActive = true }) {
            Icon(
                Icons.Filled.Search,
                contentDescription = stringResource(id = R.string.search_channels))
          }
        }
        menuContent()
      })
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchTextField(
    modifier: Modifier,
    searchText: String,
    placeholderText: String = "Search...",
    onSearchTextChanged: (String) -> Unit = {},
    onClose: () -> Unit = {}
) {
  var showClearButton by remember { mutableStateOf(true) }
  val keyboardController = LocalSoftwareKeyboardController.current
  // val focusRequester = remember { FocusRequester() }

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
      // TODO: Proper implementation
      // onValueChange = onSearchTextChanged,
      onValueChange = { onSearchTextChanged(it) },
      placeholder = { Text(text = placeholderText) },
      colors =
          TextFieldDefaults.textFieldColors(
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent,
              containerColor = Color.Transparent,
              cursorColor = LocalContentColor.current.copy(alpha = 0.4f)),
      trailingIcon = {
        AnimatedVisibility(visible = showClearButton, enter = fadeIn(), exit = fadeOut()) {
          IconButton(
              onClick = { if (searchText.isEmpty()) onClose() else onSearchTextChanged("") }) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription =
                        stringResource(id = R.string.search_clear_content_description))
              }
        }
      },
      maxLines = 1,
      singleLine = true,
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
      keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
  )
}

@Composable
private fun ChannelListContent(
    channelListState: State<List<Pair<SonyChannel, String?>>>,
    onChannelClick: (String) -> Unit
) {
  Timber.d("ChannelListContent")
  LazyColumn() {
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
  Row(modifier = Modifier.padding(vertical = 4.dp).clickable { onclick(channel) }) {
    Column {
      Text(
          modifier = Modifier.padding(end = 12.dp).width(40.dp),
          style = MaterialTheme.typography.bodyLarge,
          text = channel.dispNumber,
          textAlign = TextAlign.Right)
    }
    Column() {
      Text(
          modifier = Modifier.padding(horizontal = 0.dp),
          style = MaterialTheme.typography.bodyLarge,
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
