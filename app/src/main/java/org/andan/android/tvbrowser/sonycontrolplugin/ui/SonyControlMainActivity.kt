package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.andan.android.tvbrowser.sonycontrolplugin.SonyControlApplication

class SonyControlMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appComponent = (application as SonyControlApplication).appComponent
        setContent {
                SonyControlApp(appComponent)
        }
    }
}
