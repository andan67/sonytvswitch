package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel
import timber.log.Timber

@Composable
fun RemoteControlScreen(
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
            AppDrawer(
                closeDrawer = { coroutineScope.launch { scaffoldState.drawerState.close() } },
                navActions = navActions
            )
        },
        topBar = {
            RemoteControlTopAppBar(
                openDrawer = {
                    coroutineScope.launch { scaffoldState.drawerState.open() }; Timber.d(
                    "openDrawer"
                )
                },
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        RemoteControlContent(
            modifier = Modifier.padding(innerPadding)
        )
    }
}

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
            RemoteControlMenu({}, {}, {})
        })
}

@Composable
private fun RemoteControlMenu(
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

@SuppressLint("ResourceType")
@Composable
private fun RemoteControlContent(
    modifier: Modifier = Modifier.width(dimensionResource(id = R.dimen.rc_layout_width))
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp))
        {
            RemoteControlIconButton(
                command = "",
                painter = painterResource(id = R.drawable.ic_action_input)
            )
            RemoteControlTextButton(
                command = "",
                text = "GUIDE"
            )
            RemoteControlTextButton(
                command = "",
                backgroundColor = colorResource(id = R.color.buttonBlue),
                text = "SEN"
            )
            RemoteControlIconButton(
                command = "",
                backgroundColor = colorResource(id = R.color.buttonGreen),
                painter = painterResource(id = R.drawable.ic_action_input)
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
                command = "",
                text = "I+"
            )
            RemoteControlTextButton(
                modifier = Modifier.align(Alignment.TopEnd),
                backgroundColor = colorResource(id = R.color.buttonBlue),
                command = "",
                text = "HOME"
            )
            RemoteControlTextButton(
                modifier = Modifier.align(Alignment.BottomStart),
                command = "",
                text = "RETURN"
            )
            RemoteControlTextButton(
                modifier = Modifier.align(Alignment.BottomEnd),
                command = "",
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
                    command = "",
                    painter = painterResource(id = R.drawable.ic_keyboard_arrow_up)
                )
                RemoteControlIconButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    width = dimensionResource(id = R.dimen.rc_button_large_height),
                    height = 68.dp,
                    command = "",
                    painter = painterResource(id = R.drawable.ic_keyboard_arrow_left)
                )
                RemoteControlIconButton(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    width = 68.dp,
                    command = "",
                    painter = painterResource(id = R.drawable.ic_keyboard_arrow_down)
                )
                RemoteControlIconButton(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    width = dimensionResource(id = R.dimen.rc_button_large_height),
                    height = 68.dp,
                    command = "",
                    painter = painterResource(id = R.drawable.ic_keyboard_arrow_right)
                )
                RemoteControlTextButton(
                    modifier = Modifier.align(Alignment.Center),
                    width = 68.dp,
                    height = 68.dp,
                    command = "",
                    text = "OK"
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(36.dp))
        {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(24.dp))
                RemoteControlButton(
                    width = dimensionResource(id = R.dimen.rc_button_small_width),
                    onClick = { /*TODO*/ },
                    content = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_action_mute),
                            tint = Color.White,
                            contentDescription = null
                        )
                    }
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "VOLUME",
                    modifier = Modifier
                        .height(24.dp)
                        .padding(top = 6.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RemoteControlButton(
                        width = dimensionResource(id = R.dimen.rc_button_small_width),
                        onClick = { /*TODO*/ },
                        content = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_remove),
                                tint = Color.White,
                                contentDescription = null
                            )
                        }
                    )
                    RemoteControlButton(
                        width = dimensionResource(id = R.dimen.rc_button_small_width),
                        onClick = { /*TODO*/ },
                        content = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_add),
                                tint = Color.White,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "PROG",
                    modifier = Modifier
                        .height(24.dp)
                        .padding(top = 6.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RemoteControlButton(
                        width = dimensionResource(id = R.dimen.rc_button_small_width),
                        onClick = { /*TODO*/ },
                        content = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_remove),
                                tint = Color.White,
                                contentDescription = null
                            )
                        }
                    )
                    RemoteControlButton(
                        width = dimensionResource(id = R.dimen.rc_button_small_width),
                        onClick = { /*TODO*/ },
                        content = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_add),
                                tint = Color.White,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp))
        {
            RemoteControlButton(
                onClick = { /*TODO*/ },
                content = {
                    Text(
                        "SYNC\nMENU",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium
                    )
                }

            )
            RemoteControlButton(
                onClick = { /*TODO*/ },
                content = {
                    Text(
                        "ANALOG\nDIGITAL",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium
                    )
                }

            )
            RemoteControlButton(
                onClick = { /*TODO*/ },
                content = {
                    Text(
                        "EXIT",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium
                    )
                }

            )
            RemoteControlButton(
                onClick = { /*TODO*/ },
                content = {
                    Text(
                        "Radio\nTV",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium
                    )
                }

            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp))
        {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "ghi",
                    modifier = Modifier
                        .height(24.dp)
                        .padding(top = 6.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light
                )
                RemoteControlButton(
                    width = dimensionResource(id = R.dimen.rc_button_number_width),
                    onClick = { /*TODO*/ },
                    content = {
                        Text(
                            "4",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium
                        )
                    }

                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "jkl",
                    modifier = Modifier
                        .height(24.dp)
                        .padding(top = 6.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light
                )
                RemoteControlButton(
                    width = dimensionResource(id = R.dimen.rc_button_number_width),
                    onClick = { /*TODO*/ },
                    content = {
                        Text(
                            "5",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium
                        )
                    }

                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "mno",
                    modifier = Modifier
                        .height(24.dp)
                        .padding(top = 6.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light
                )
                RemoteControlButton(
                    width = dimensionResource(id = R.dimen.rc_button_number_width),
                    onClick = { /*TODO*/ },
                    content = {
                        Text(
                            "6",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium
                        )
                    }

                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp))
        {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "pqrs",
                    modifier = Modifier
                        .height(24.dp)
                        .padding(top = 6.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light
                )
                RemoteControlButton(
                    width = dimensionResource(id = R.dimen.rc_button_number_width),
                    onClick = { /*TODO*/ },
                    content = {
                        Text(
                            "7",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium
                        )
                    }

                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "tuv",
                    modifier = Modifier
                        .height(24.dp)
                        .padding(top = 6.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light
                )
                RemoteControlButton(
                    width = dimensionResource(id = R.dimen.rc_button_number_width),
                    onClick = { /*TODO*/ },
                    content = {
                        Text(
                            "8",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium
                        )
                    }

                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "wxyz",
                    modifier = Modifier
                        .height(24.dp)
                        .padding(top = 6.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light
                )
                RemoteControlButton(
                    width = dimensionResource(id = R.dimen.rc_button_number_width),
                    onClick = { /*TODO*/ },
                    content = {
                        Text(
                            "9",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium
                        )
                    }

                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp))
        {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    " ",
                    modifier = Modifier
                        .height(24.dp)
                        .padding(top = 6.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light
                )
                RemoteControlButton(
                    width = dimensionResource(id = R.dimen.rc_button_number_width),
                    onClick = { /*TODO*/ },
                    content = {
                        Text(
                            "I-MANUAL",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium
                        )
                    }

                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "\u2423",
                    modifier = Modifier
                        .height(24.dp)
                        .padding(top = 6.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light
                )
                RemoteControlButton(
                    width = dimensionResource(id = R.dimen.rc_button_number_width),
                    onClick = { /*TODO*/ },
                    content = {
                        Text(
                            "0",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium
                        )
                    }

                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    " ",
                    modifier = Modifier
                        .height(24.dp)
                        .padding(top = 6.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light
                )
                RemoteControlButton(
                    width = dimensionResource(id = R.dimen.rc_button_number_width),
                    onClick = { /*TODO*/ },
                    content = {
                        Text(
                            "ENTER",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium
                        )
                    }

                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp))
        {
            RemoteControlButton(
                onClick = { /*TODO*/ },
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
                onClick = { /*TODO*/ },
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
                onClick = { /*TODO*/ },
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
                onClick = { /*TODO*/ },
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
            RemoteControlButton(
                onClick = { /*TODO*/ },
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_skip_previous),
                        tint = Color.White,
                        contentDescription = null
                    )
                }
            )
            RemoteControlButton(
                onClick = { /*TODO*/ },
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_pause),
                        tint = Color.White,
                        contentDescription = null
                    )
                }
            )
            RemoteControlButton(
                onClick = { /*TODO*/ },
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_stop),
                        tint = Color.White,
                        contentDescription = null
                    )
                }
            )
            RemoteControlButton(
                onClick = { /*TODO*/ },
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_skip_next),
                        tint = Color.White,
                        contentDescription = null
                    )
                }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp))
        {
            RemoteControlButton(
                onClick = { /*TODO*/ },
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_fast_rewind),
                        tint = Color.White,
                        contentDescription = null
                    )
                }
            )
            RemoteControlButton(
                onClick = { /*TODO*/ },
                width = dimensionResource(id = R.dimen.rc_button_play_width),
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_play_arrow),
                        tint = Color.White,
                        contentDescription = null
                    )
                }
            )
            RemoteControlButton(
                onClick = { /*TODO*/ },
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_fast_forward),
                        tint = Color.White,
                        contentDescription = null
                    )
                }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp))
        {
            RemoteControlButton(
                onClick = { /*TODO*/ },
                content = {
                    Text(
                        "TV PAUSE",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium
                    )
                }
            )
            RemoteControlButton(
                onClick = { /*TODO*/ },
                content = {
                    Text(
                        "TITLE LIST",
                        fontSize = 14.sp,
                        //style = MaterialTheme.typography.button
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
                    )
                }
            )
            RemoteControlButton(
                onClick = { /*TODO*/ },
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
            RemoteControlButton(
                onClick = { /*TODO*/ },
                content = {
                    Text(
                        "3D",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium
                    )
                }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp))
        {
            RemoteControlButton(
                onClick = { /*TODO*/ },
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_action_aspect),
                        tint = Color.White,
                        contentDescription = null
                    )
                }
            )
            RemoteControlButton(
                onClick = { /*TODO*/ },
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_action_subtitle),
                        tint = Color.White,
                        contentDescription = null
                    )
                }
            )
            RemoteControlButton(
                onClick = { /*TODO*/ },
                content = {
                    Text(
                        "AUDIO",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium
                    )
                }
            )
            RemoteControlButton(
                onClick = { /*TODO*/ },
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_action_teletext),
                        tint = Color.Green,
                        contentDescription = null
                    )
                }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
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
    onClick: () -> Unit = {},
    backgroundColor: Color = colorResource(id = R.color.grey_900),
    content: @Composable () -> Unit,
    command: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Button(
        modifier = modifier
            .width(width)
            .height(height)
            .padding(all = 0.dp),
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isPressed) colorResource(id = R.color.grey_500) else backgroundColor
        )
    ) {
        content.invoke()
    }

}