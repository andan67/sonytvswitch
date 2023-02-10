package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.domain.PlayingContentInfo
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayingContentInfoScreen(
    navActions: NavigationActions,
    viewModel: SonyControlViewModel
) {

    val playingContentInfoState =
        viewModel.playingContentInfo.observeAsState(initial = PlayingContentInfo())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(id = R.string.playing_content_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = {navActions.navigateUp()}) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                }
            )
        },
    ) { innerPadding ->
        PlayingContentInfoContent(modifier = Modifier.padding(innerPadding), playingContentInfoState = playingContentInfoState)
    }
}

@Composable
fun PlayingContentInfoContent(modifier: Modifier, playingContentInfoState: State<PlayingContentInfo>) {
    val playingContentInfo = playingContentInfoState.value

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