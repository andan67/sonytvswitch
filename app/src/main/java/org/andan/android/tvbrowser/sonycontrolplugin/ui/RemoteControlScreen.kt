package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.RemoteControlViewModel
import timber.log.Timber

val RemoteButtonClickFunction = compositionLocalOf<(String) -> Unit> { {_ -> Unit}}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteControlScreen(
    modifier: Modifier = Modifier,
    navActions: NavigationActions,
    openDrawer: () -> Unit
) {

    val remoteControlViewModel: RemoteControlViewModel = hiltViewModel()

    Scaffold(
        topBar = {
            RemoteControlTopAppBar(
                openDrawer = { openDrawer() },

                )
        },
        content = { contentPadding ->
            CompositionLocalProvider(RemoteButtonClickFunction provides {command:String -> remoteControlViewModel.sendCommand(command) }) {
            //CompositionLocalProvider(RemoteButtonClickFunction provides {command:String -> Timber.d("Clicked command: $command") }) {
                    RemoteControlContent(
                        modifier = Modifier.padding(contentPadding)
                    )
                }
            }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteControlTopAppBar(
    openDrawer: () -> Unit,
) {
    TopAppBar(title = { Text(text = stringResource(id = R.string.menu_remote_control)) },
        navigationIcon = {
            IconButton(onClick = openDrawer) {
                Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer))
            }
        },
        actions = {
            ChannelListMenu({}, {}, {})
        })
}

@SuppressLint("ResourceType")
@Composable
private fun RemoteControlContent(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp))
        {
            RemoteControlIconButton(
                command = "Input",
                painter = painterResource(id = R.drawable.ic_action_input)
            )
            RemoteControlTextButton(
                command = "GGuide",
                text = "GUIDE"
            )
            RemoteControlTextButton(
                command = "SEN",
                backgroundColor = colorResource(id = R.color.buttonBlue),
                text = "SEN"
            )
            RemoteControlIconButton(
                command = "PowerOff",
                backgroundColor = colorResource(id = R.color.buttonGreen),
                painter = painterResource(id = R.drawable.ic_action_power)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                //.background(color = Color.LightGray)
                .width(332.dp)
                .height(148.dp)
        ) {
            RemoteControlTextButton(
                modifier = Modifier.align(Alignment.TopStart),
                command = "Display",
                text = "INFO"
            )
            RemoteControlTextButton(
                modifier = Modifier.align(Alignment.TopEnd),
                backgroundColor = colorResource(id = R.color.buttonBlue),
                command = "Home",
                text = "HOME"
            )
            RemoteControlTextButton(
                modifier = Modifier.align(Alignment.BottomStart),
                command = "Return",
                text = "RETURN"
            )
            RemoteControlTextButton(
                modifier = Modifier.align(Alignment.BottomEnd),
                command = "Options",
                text = "OPTIONS"
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(148.dp)
                    .clip(shape = CircleShape)
                    .background(color = colorResource(id = R.color.grey_900))
            ) {
                RemoteControlIconButton(
                    modifier = Modifier.align(Alignment.TopCenter),
                    width = 68.dp,
                    command = "Up",
                    painter = painterResource(id = R.drawable.ic_keyboard_arrow_up)
                )
                RemoteControlIconButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    width = dimensionResource(id = R.dimen.rc_button_large_height),
                    height = 68.dp,
                    command = "Left",
                    painter = painterResource(id = R.drawable.ic_keyboard_arrow_left)
                )
                RemoteControlIconButton(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    width = 68.dp,
                    command = "Down",
                    painter = painterResource(id = R.drawable.ic_keyboard_arrow_down)
                )
                RemoteControlIconButton(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    width = dimensionResource(id = R.dimen.rc_button_large_height),
                    height = 68.dp,
                    command = "Right",
                    painter = painterResource(id = R.drawable.ic_keyboard_arrow_right)
                )
                RemoteControlTextButton(
                    modifier = Modifier.align(Alignment.Center),
                    width = 68.dp,
                    height = 68.dp,
                    command = "Confirm",
                    text = "OK"
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(36.dp))
        {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(24.dp))
                RemoteControlIconButton(
                    width = dimensionResource(id = R.dimen.rc_button_small_width),
                    command = "Mute",
                    painter = painterResource(id = R.drawable.ic_action_mute)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RemoteControlLabel("VOLUME")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RemoteControlIconButton(
                        width = dimensionResource(id = R.dimen.rc_button_small_width),
                        command = "VolumeDown",
                        painter = painterResource(id = R.drawable.ic_remove)
                    )
                    RemoteControlIconButton(
                        width = dimensionResource(id = R.dimen.rc_button_small_width),
                        command = "VolumeUp",
                        painter = painterResource(id = R.drawable.ic_add)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RemoteControlLabel("PROG")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RemoteControlIconButton(
                        width = dimensionResource(id = R.dimen.rc_button_small_width),
                        command = "ChannelDown",
                        painter = painterResource(id = R.drawable.ic_remove)
                    )
                    RemoteControlIconButton(
                        width = dimensionResource(id = R.dimen.rc_button_small_width),
                        command = "ChannelUp",
                        painter = painterResource(id = R.drawable.ic_add)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp))
        {
            RemoteControlTextButton(
                command = "SyncMenu",
                text = "SYNC\nMENU"
            )
            RemoteControlTextButton(
                command = "Digital",
                text = "ANALOG\nDIGITAL"
            )
            RemoteControlTextButton(
                command = "Exit",
                text = "EXIT"
            )
            RemoteControlTextButton(
                command = "Tv_Radio",
                text = "Radio\nTV"
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp))
        {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RemoteControlLabel("/")
                RemoteControlTextButton(
                    width = dimensionResource(id = R.dimen.rc_button_number_width),
                    command = "Num1",
                    text = "1"
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RemoteControlLabel("abc")
                RemoteControlTextButton(
                    width = dimensionResource(id = R.dimen.rc_button_number_width),
                    command = "Num2",
                    text = "2"
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RemoteControlLabel("def")
                RemoteControlTextButton(
                    width = dimensionResource(id = R.dimen.rc_button_number_width),
                    command = "Num3",
                    text = "3"
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp))
        {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RemoteControlLabel("ghi")
                RemoteControlTextButton(
                    width = dimensionResource(id = R.dimen.rc_button_number_width),
                    command = "Num4",
                    text = "4"
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RemoteControlLabel("jkl")
                RemoteControlTextButton(
                    width = dimensionResource(id = R.dimen.rc_button_number_width),
                    command = "Num5",
                    text = "5"
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RemoteControlLabel("mno")
                RemoteControlTextButton(
                    width = dimensionResource(id = R.dimen.rc_button_number_width),
                    command = "Num6",
                    text = "6"
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp))
        {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RemoteControlLabel("pqrs")
                RemoteControlTextButton(
                    width = dimensionResource(id = R.dimen.rc_button_number_width),
                    command = "Num7",
                    text = "7"
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RemoteControlLabel("tuv")
                RemoteControlTextButton(
                    width = dimensionResource(id = R.dimen.rc_button_number_width),
                    command = "Num8",
                    text = "8"
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RemoteControlLabel("wxyz")
                RemoteControlTextButton(
                    width = dimensionResource(id = R.dimen.rc_button_number_width),
                    command = "Num9",
                    text = "9"
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp))
        {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RemoteControlLabel("")
                RemoteControlTextButton(
                    width = dimensionResource(id = R.dimen.rc_button_number_width),
                    command = "iManual",
                    text = "I-MANUAL"
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RemoteControlLabel("␣")
                RemoteControlTextButton(
                    width = dimensionResource(id = R.dimen.rc_button_number_width),
                    command = "Num0",
                    text = "0"
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RemoteControlLabel("")
                RemoteControlTextButton(
                    width = dimensionResource(id = R.dimen.rc_button_number_width),
                    command = "Enter",
                    text = "ENTER"
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp))
        {
            RemoteControlButton(
                command = "Red",
                content = {
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(48.dp)
                            .clip(RectangleShape)
                            .background(colorResource(id = R.color.commandRed))
                    )
                }
            )
            RemoteControlButton(
                command = "Green",
                content = {
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(48.dp)
                            .clip(RectangleShape)
                            .background(colorResource(id = R.color.commandGreen))
                    )
                }
            )
            RemoteControlButton(
                command = "Yellow",
                content = {
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(48.dp)
                            .clip(RectangleShape)
                            .background(colorResource(id = R.color.commandYellow))
                    )
                }
            )
            RemoteControlButton(
                command = "Blue",
                content = {
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(48.dp)
                            .clip(RectangleShape)
                            .background(colorResource(id = R.color.commandBlue))
                    )
                }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp))
        {
            RemoteControlIconButton(
                command = "Prev",
                painter = painterResource(id = R.drawable.ic_skip_previous)
            )
            RemoteControlIconButton(
                command = "Pause",
                painter = painterResource(id = R.drawable.ic_pause)
            )
            RemoteControlIconButton(
                command = "Stop",
                painter = painterResource(id = R.drawable.ic_stop)
            )
            RemoteControlIconButton(
                command = "Next",
                painter = painterResource(id = R.drawable.ic_skip_next)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp))
        {
            RemoteControlIconButton(
                command = "Rewind",
                painter = painterResource(id = R.drawable.ic_fast_rewind)
            )
            RemoteControlIconButton(
                command = "Play",
                width = dimensionResource(id = R.dimen.rc_button_play_width),
                painter = painterResource(id = R.drawable.ic_play_arrow)
            )
            RemoteControlIconButton(
                command = "Forward",
                painter = painterResource(id = R.drawable.ic_fast_forward)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp))
        {
            RemoteControlTextButton(
                command = "TvPause",
                text = "TV\nPAUSE",
            )
            RemoteControlTextButton(
                command = "",
                text = "TITLE\nLIST",
            )
            RemoteControlButton(
                command = "Rec",
                content = {
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(8.dp)
                            .clip(CircleShape)
                            .background(colorResource(id = R.color.commandRed))
                    )
                }
            )
            RemoteControlTextButton(
                command = "Mode3D",
                text = "3D",
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp))
        {
            RemoteControlIconButton(
                command = "Wide",
                painter = painterResource(id = R.drawable.ic_action_aspect)
            )
            RemoteControlIconButton(
                command = "ClosedCaption",
                painter = painterResource(id = R.drawable.ic_action_subtitle)
            )
            RemoteControlTextButton(
                command = "Audio",
                text = "AUDIO",
            )
            RemoteControlIconButton(
                command = "Teletext",
                tint = Color.Green,
                painter = painterResource(id = R.drawable.ic_action_teletext)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun RemoteControlLabel(
    text: String = ""
) {
    Text(
        text,
        modifier = Modifier
            .height(24.dp)
            .padding(top = 6.dp),
        textAlign = TextAlign.Center,
        fontSize = 12.sp,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light
    )
}

@Composable
private fun RemoteControlIconButton(
    modifier: Modifier = Modifier,
    width: Dp = dimensionResource(id = R.dimen.rc_button_large_width),
    height: Dp = dimensionResource(id = R.dimen.rc_button_large_height),
    painter: Painter,
    backgroundColor: Color = colorResource(id = R.color.grey_900),
    tint: Color = Color.White,
    contentDescription: String = "",
    command: String
) {
    RemoteControlButton(
        modifier = modifier,
        command = command,
        width = width,
        height = height,
        backgroundColor = backgroundColor,
        content = {
            Icon(
                painter = painter,
                tint = tint,
                contentDescription = contentDescription
            )
        }
    )
}

@Composable
private fun RemoteControlTextButton(
    modifier: Modifier = Modifier,
    width: Dp = dimensionResource(id = R.dimen.rc_button_large_width),
    height: Dp = dimensionResource(id = R.dimen.rc_button_large_height),
    text: String = "",
    backgroundColor: Color = colorResource(id = R.color.grey_900),
    fontSize: TextUnit = 14.sp,
    fontFamily: FontFamily = FontFamily.SansSerif,
    fontWeight: FontWeight = FontWeight.Medium,
    command: String = ""
) {
    RemoteControlButton(
        modifier = modifier,
        command = command,
        width = width,
        height = height,
        backgroundColor = backgroundColor,
        content = {
            Text(
                text,
                textAlign = TextAlign.Center,
                fontSize = fontSize,
                fontFamily = fontFamily,
                fontWeight = fontWeight
            )
        }

    )
}

@Composable
private fun RemoteControlButton(
    modifier: Modifier = Modifier,
    width: Dp = dimensionResource(id = R.dimen.rc_button_large_width),
    height: Dp = dimensionResource(id = R.dimen.rc_button_large_height),
    backgroundColor: Color = colorResource(id = R.color.grey_900),
    content: @Composable () -> Unit,
    command: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val onRemoteControlButtonClicked = RemoteButtonClickFunction.current


    Button(
        modifier = modifier
            .width(width)
            .height(height)
            .padding(all = 0.dp),
        onClick = {onRemoteControlButtonClicked(command)},
        contentPadding = PaddingValues(0.dp),
        interactionSource = interactionSource,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 20.dp,
            pressedElevation = 15.dp,
            disabledElevation = 0.dp,
            hoveredElevation = 15.dp,
            focusedElevation = 10.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPressed) colorResource(id = R.color.grey_500) else backgroundColor
        )
    ) {
        content.invoke()
    }

}