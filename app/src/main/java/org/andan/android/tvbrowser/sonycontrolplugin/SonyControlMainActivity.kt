package org.andan.android.tvbrowser.sonycontrolplugin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.accompanist.appcompattheme.AppCompatTheme
import org.andan.android.tvbrowser.sonycontrolplugin.ui.AppNavHost

class SonyControlMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //AppCompatTheme() {
                AppNavHost()
            //}
        }
    }
}
