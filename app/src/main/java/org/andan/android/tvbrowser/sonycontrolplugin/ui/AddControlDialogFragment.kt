package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel

/**
 * A simple [Fragment] subclass.
 */
class AddControlDialogFragment : DialogFragment() {

    private val TAG = AddControlDialogFragment::class.java.name
    private val sonyControlViewModel: SonyControlViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(context!!)
        dialogBuilder.setMessage("Add control")
        val dialogView:View = this.activity!!.layoutInflater.inflate(R.layout.fragment_add_control_dialog, null, false)
        dialogBuilder.setView(dialogView)
        dialogBuilder.setPositiveButton("Add"
        ) { _, _ ->
            // Write your code here to execute after dialog
            val ip = (dialogView.findViewById(R.id.addControlIPEditText) as EditText).text.toString()
            val nickname = (dialogView.findViewById(R.id.addControlNicknameEditText) as EditText).text.toString()
            val devicename = (dialogView.findViewById(R.id.addControlDevicenameEditView) as EditText).text.toString()
            val sonyControl =
                when {
                    nickname.contains("#android", true) -> SonyControl("192.168.178.27", "android", "sony")
                    nickname.contains("sample", true) -> SonyControl.fromJson(context!!.assets.open("SonyControl_sample.json").bufferedReader().use { it.readText() })
                    else -> SonyControl(ip, nickname, devicename)
                }
            sonyControlViewModel.addControl(sonyControl)
            val navController = activity!!.findNavController(R.id.nav_host_fragment)
            navController.navigate(R.id.nav_manage_control)
        }
        dialogBuilder.setNegativeButton("Cancel"
        ) { dialog, _ -> dialog.cancel() }
        return dialogBuilder.create()
    }
}
