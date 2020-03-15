package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.andan.android.tvbrowser.sonycontrolplugin.R

/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        preferenceScreen.findPreference<Preference>("license")!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                // dialog code here
                val builder = AlertDialog.Builder(context!!,
                    R.style.Theme_AppCompat_Dialog_Alert
                )
                builder.setTitle(resources.getString(R.string.pref_app_license_title))
                builder.setMessage(HtmlCompat.fromHtml(context?.assets?.open("license.html")?.bufferedReader().use { it!!.readText() }, HtmlCompat.FROM_HTML_MODE_LEGACY))
                builder.setPositiveButton(resources.getString(R.string.dialog_ok), null)
                val dialog = builder.create()
                dialog.show()
                val tv = dialog.findViewById<View>(android.R.id.message) as TextView
                tv.textSize = 12f
                true
            }

    }
}
