package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel

@Composable
fun ChannelListScreen(
    viewModel: SonyControlViewModel = viewModel()
) {
    ChannelListContent(
        name = viewModel.channelNameList.get(0)
    )
}

@Composable
private fun ChannelListContent(
    name: String
) {
    Text(text = "Hello $name!")
}