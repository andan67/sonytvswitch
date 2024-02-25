package org.andan.android.tvbrowser.sonycontrolplugin.ui.components.misc

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebView(
    modifier: Modifier = Modifier,
    pageUrl: String,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    linkColor: Color = MaterialTheme.colorScheme.primary
) {
  AndroidView(
      modifier = modifier,
      factory = { context ->
        return@AndroidView WebView(context).apply {
          settings.javaScriptEnabled = true
          webViewClient =
              object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                  super.onPageFinished(view, url)
                  val code =
                      """javascript:(function() {

                        var node = document.createElement('style');

                        node.type = 'text/css';
                        node.innerHTML = 'body {
                            color: ${textColor.toRGBHex()};
                            background-color: ${backgroundColor.toRGBHex()};
                        }
                         a {
                            color: ${linkColor.toRGBHex()};
                          }
                      ';

                        document.head.appendChild(node);

                    })()"""
                          .trimIndent()

                  loadUrl(code)
                }
              }

          // settings.loadWithOverviewMode = true
          // settings.useWideViewPort = true
          // settings.setSupportZoom(false)
          layoutParams =
              FrameLayout.LayoutParams(
                  ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
      },
      update = { it.loadUrl(pageUrl) })
}

fun Color.toRGBHex(): String {
  return String.format("#%06X", (0xFFFFFF and this.toArgb()))
}
