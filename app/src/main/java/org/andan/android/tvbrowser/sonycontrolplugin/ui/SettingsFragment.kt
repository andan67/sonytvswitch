package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import org.andan.android.tvbrowser.sonycontrolplugin.BuildConfig
import org.andan.android.tvbrowser.sonycontrolplugin.R
import java.io.File
import java.io.FileInputStream

/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        preferenceScreen.findPreference<Preference>("license")!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                // dialog code here
                val builder = AlertDialog.Builder(
                    requireContext(),
                    R.style.Theme_AppCompat_Dialog_Alert
                )
                builder.setTitle(resources.getString(R.string.pref_app_license_title))
                builder.setMessage(
                    HtmlCompat.fromHtml(
                        context?.assets?.open("license.html")?.bufferedReader()
                            .use { it!!.readText() }, HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                )
                builder.setPositiveButton(resources.getString(R.string.dialog_ok), null)
                val dialog = builder.create()
                dialog.show()
                val tv = dialog.findViewById<View>(android.R.id.message) as TextView
                tv.textSize = 12f
                true
            }

        preferenceScreen.findPreference<Preference>("third_party_license")!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                OssLicensesMenuActivity.setActivityTitle(getString(R.string.pref_app_third_party_license_title));
                startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                // dialog code here
                true
            }
        val applicationLogPreference =
            preferenceScreen.findPreference<Preference>("application_log")
        if (applicationLogPreference != null) {
            if (BuildConfig.DEBUG) {
                applicationLogPreference.onPreferenceClickListener =
                    Preference.OnPreferenceClickListener {
                        // dialog code here
                        val builder = AlertDialog.Builder(
                            requireContext(),
                            R.style.Theme_AppCompat_Dialog_Alert
                        )
                        builder.setTitle("Application debug log")
                        val folder =
                            context?.filesDir!!.absolutePath + File.separator + "logs"
                        val subFolder = File(folder)
                        FileInputStream(File(subFolder, "my-log-latest.html"))
                        val logUri = FileProvider.getUriForFile(
                            requireContext(),
                            requireContext().applicationContext.packageName + ".provider",
                            File(subFolder, "my-log-latest.html")
                        )
                        val intent = Intent(Intent.ACTION_VIEW, logUri)
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        startActivity(intent)
                        true
                    }
            } else {
                preferenceScreen.removePreference(applicationLogPreference)
            }
        }
    }
}

class GenericFileProvider : FileProvider()