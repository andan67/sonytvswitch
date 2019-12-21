package org.andan.android.tvbrowser.sonycontrolplugin

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController

import org.andan.av.sony.SonyIPControl

/**
 * A simple [Fragment] subclass.
 */
class AddControlDialogFragment : DialogFragment() {

    private val TAG = AddControlDialogFragment::class.java.name

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(context!!)
        dialogBuilder.setMessage("Add control")
        val dialogView:View = this.activity!!.layoutInflater.inflate(R.layout.fragment_add_control_dialog, null, false)
        dialogBuilder.setView(dialogView)
        val controlViewModel = ViewModelProviders.of(activity!!).get(ControlViewModel::class.java)
        dialogBuilder.setPositiveButton("Add",
            DialogInterface.OnClickListener { dialog, which ->
                // Write your code here to execute after dialog
                val ip = (dialogView!!.findViewById(R.id.addControlIPEditText) as EditText).text.toString()
                val nickname = (dialogView!!.findViewById(R.id.addControlNicknameEditText) as EditText).text.toString()
                val devicename = (dialogView!!.findViewById(R.id.addControlDevicenameEditView) as EditText).text.toString()
                val sonyIPControl =
                    if (nickname.contains("#android", true)) SonyIPControl.createSample("192.168.178.27", "android", "sony")
                    else if (nickname.contains("sample", true)) SonyIPControl.createSample(ip, nickname, devicename)
                    else SonyIPControl(ip, nickname, devicename)
                controlViewModel.addControl(sonyIPControl)
                val navController = activity!!.findNavController(R.id.nav_host_fragment)
                navController.navigate(R.id.nav_manage_control)
            }
        )
        dialogBuilder.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() }
        )
        return dialogBuilder.create()
    }

}
