package org.andan.android.tvbrowser.sonycontrolplugin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.google.accompanist.appcompattheme.AppCompatTheme
import org.andan.android.tvbrowser.sonycontrolplugin.ui.AppNavHost
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel
import timber.log.Timber

class SonyControlMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sonyControlViewModel: SonyControlViewModel by viewModels()
        sonyControlViewModel.powerStatus.observeForever(Observer {
            Timber.d("observed change of powerStatus ${sonyControlViewModel.powerStatus}")})
        setContent {
            //AppCompatTheme() {
                AppNavHost(viewModel = sonyControlViewModel)
            //}
        }
    }
}
