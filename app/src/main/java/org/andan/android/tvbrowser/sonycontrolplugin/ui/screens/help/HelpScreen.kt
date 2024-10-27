package org.andan.android.tvbrowser.sonycontrolplugin.ui.screens.help

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.misc.WebView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(modifier: Modifier = Modifier, openDrawer: () -> Unit) {

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.menu_help)) },
            navigationIcon = {
              IconButton(onClick = openDrawer) {
                Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer))
              }
            })
      },
      modifier = modifier.fillMaxSize(),
  ) { innerPadding ->
    HelpContent(modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp))
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HelpContent(modifier: Modifier) {
  WebView(modifier = modifier, pageUrl = "file:///android_asset/help.html")
}
