package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import org.andan.android.tvbrowser.sonycontrolplugin.SonyControlApplication

@AndroidEntryPoint
class SonyControlMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
                SonyControlApp()
        }
    }
}
