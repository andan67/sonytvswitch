package org.andan.android.tvbrowser.sonycontrolplugin.ui.components.misc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import org.andan.android.tvbrowser.sonycontrolplugin.R

@Composable
fun SearchTextField(
    modifier: Modifier = Modifier,
    searchText: String,
    onSearchTextChanged: (String) -> Unit = {},
    onClose: () -> Unit = {},
    showClearButton: Boolean = false
) {
  val keyboardController = LocalSoftwareKeyboardController.current
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(Unit) { focusRequester.requestFocus() }

  OutlinedTextField(
      modifier = modifier.focusRequester(focusRequester),
      textStyle = MaterialTheme.typography.titleMedium,
      value = searchText,
      // TODO: Proper implementation
      // onValueChange = onSearchTextChanged,
      onValueChange = { onSearchTextChanged(it) },
      placeholder = { Text(text = stringResource(id = R.string.search_placeholder)) },
      colors =
          TextFieldDefaults.colors(
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent,
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent,
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
