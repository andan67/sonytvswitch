package org.andan.android.tvbrowser.sonycontrolplugin.ui.components.misc

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import org.andan.android.tvbrowser.sonycontrolplugin.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithSearch(
    title: String = "",
    iconImage: ImageVector = Icons.Filled.Menu,
    onIconClick: () -> Unit = {},
    searchText: String,
    onSearchTextChanged: (String) -> Unit = {},
    menuContent: @Composable () -> Unit = {}
) {
  var isSearchActive by rememberSaveable { mutableStateOf(false) }

  TopAppBar(
      title = {
        if (!isSearchActive) Text(text = title, style = MaterialTheme.typography.titleLarge)
        else
            SearchTextField(
                modifier = Modifier.fillMaxWidth(),
                searchText = searchText,
                onSearchTextChanged = onSearchTextChanged,
                onClose = { isSearchActive = false })
      },
      navigationIcon = {
        if (!isSearchActive) IconButton(onClick = { onIconClick() }) { Icon(iconImage, null) }
        else
            IconButton(onClick = {}) {
              Icon(Icons.Filled.Search, stringResource(id = R.string.search_content_description))
            }
      },
      actions = {
        if (!isSearchActive) {
          IconButton(modifier = Modifier, onClick = { isSearchActive = true }) {
            Icon(
                Icons.Filled.Search,
                contentDescription = stringResource(id = R.string.search_content_description))
          }
        } else {
          IconButton(
              onClick = {
                if (searchText.isEmpty()) isSearchActive = false else onSearchTextChanged("")
              }) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription =
                        stringResource(id = R.string.search_clear_content_description))
              }
        }
        menuContent()
      })
}
