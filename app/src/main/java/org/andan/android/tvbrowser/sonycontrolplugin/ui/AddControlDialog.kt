package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.andan.android.tvbrowser.sonycontrolplugin.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddControlDialogContent() {
    val dialogWidth = 400.dp
    val dialogHeight = 300.dp

    var host by rememberSaveable { mutableStateOf("") }
    Card(
        modifier = Modifier
            .fillMaxHeight(0.9f)
            .fillMaxWidth(1f),
        shape = MaterialTheme.shapes.medium
        //backgroundColor = MaterialTheme.colors.surface,
        //contentColor = contentColorFor(backgroundColor)
    ) {
        Column {
            Text(text = stringResource(id = R.string.add_control_host_instructions))
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 0.dp),
                value = host,
                onValueChange = {  host = it},
                label = { Text(stringResource(id = R.string.add_control_host_title)) },
                placeholder = { Text(stringResource(id = R.string.add_control_ip_hint)) }
            )
        }
    }

}