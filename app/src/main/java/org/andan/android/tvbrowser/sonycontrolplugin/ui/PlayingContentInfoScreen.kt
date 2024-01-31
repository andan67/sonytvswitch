package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.domain.PlayingContentInfo
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.ChannelListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayingContentInfoScreen(
    navActions: NavigationActions,
    viewModel: ChannelListViewModel
    //= hiltViewModel()
    //playingContentInfoState: State<PlayingContentInfo> = mutableStateOf(PlayingContentInfo())
) {

    val channelListUIState = viewModel.channelListUiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchPlayingContentInfo()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(id = R.string.playing_content_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = { navActions.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                }
            )
        },
    ) { innerPadding ->
        PlayingContentInfoContent(
            modifier = Modifier.padding(innerPadding),
            playingContentInfo = channelListUIState.value.playingContentInfo
        )
    }
}

@Composable
fun PlayingContentInfoContent(
    modifier: Modifier,
    playingContentInfo: PlayingContentInfo
) {

    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .verticalScroll(scrollState)
            .fillMaxWidth()
    ) {
        PropertyItem(
            modifier,
            label = stringResource(id = R.string.active_program_channel_title),
            value = playingContentInfo.title
        )
        PropertyItem(
            modifier,
            label = stringResource(id = R.string.active_program_dispNumber),
            value = playingContentInfo.dispNum
        )
        PropertyItem(
            modifier,
            label = stringResource(id = R.string.active_program_program_title),
            value = playingContentInfo.programTitle
        )
        PropertyItem(
            modifier,
            label = stringResource(id = R.string.active_program_dispNumber),
            value = playingContentInfo.dispNum
        )
        PropertyItem(
            modifier,
            label = stringResource(id = R.string.active_program_start_time),
            value = playingContentInfo.getStartDateTimeFormatted(),
            showDivider = true
        )
        PropertyItem(
            modifier,
            label = stringResource(id = R.string.active_program_end_time),
            value = playingContentInfo.getEndDateTimeFormatted()
        )
        PropertyItem(
            modifier,
            label = stringResource(id = R.string.active_program_duration),
            value = (playingContentInfo.durationSec / 60).toString()
        )
        PropertyItem(
            modifier,
            label = stringResource(id = R.string.active_program_source),
            value = playingContentInfo.source
        )
        PropertyItem(
            modifier,
            label = stringResource(id = R.string.active_program_mediaType),
            value = playingContentInfo.programMediaType
        )
    }
}