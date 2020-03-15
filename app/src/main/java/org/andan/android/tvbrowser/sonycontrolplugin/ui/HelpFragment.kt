package org.andan.android.tvbrowser.sonycontrolplugin.ui


import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import org.andan.android.tvbrowser.sonycontrolplugin.R

/**
 * A simple [Fragment] subclass.
 */
class HelpFragment : Fragment() {

    private val TAG = HelpFragment::class.java.name

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_help, container, false)
        val helpWebView = view.findViewById<View>(R.id.helpWebView) as WebView
        helpWebView.setBackgroundColor(Color.parseColor("#212121"))
        val path = Uri.parse("file:///android_asset/help.html").toString()
        helpWebView.loadUrl(path)
        return view
    }
}