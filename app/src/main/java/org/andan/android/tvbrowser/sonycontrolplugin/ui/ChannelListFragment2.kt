package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.material.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.google.accompanist.appcompattheme.AppCompatTheme

/**
 * A simple [Fragment] subclass.
 */
class ChannelListFragment2 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppCompatTheme {
                    Surface(Modifier.fillMaxSize()) {
                        ChannelListScreen()
                    }
                }
            }
        }
    }
}