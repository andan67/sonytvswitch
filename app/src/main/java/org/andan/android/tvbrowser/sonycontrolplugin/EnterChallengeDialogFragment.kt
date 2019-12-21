package org.andan.android.tvbrowser.sonycontrolplugin


import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders

import org.andan.av.sony.SonyIPControl

/**
 * A simple [Fragment] subclass.
 */
class EnterChallengeDialogFragment : DialogFragment() {

    private val TAG = EnterChallengeDialogFragment::class.java.name

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(context!!)
        dialogBuilder.setMessage(R.string.dialog_enter_challenge_code_title)
        val dialogView:View = this.activity!!.layoutInflater.inflate(R.layout.fragment_enter_challenge_code_dialog, null, false)
        dialogBuilder.setView(dialogView)

        val controlViewModel = ViewModelProviders.of(activity!!).get(ControlViewModel::class.java)
        val ipControlJSON = controlViewModel.getSelectedControlAsJson()

        dialogBuilder.setPositiveButton(R.string.dialog_set,
            DialogInterface.OnClickListener { dialog, which ->
                // Write your code here to execute after dialog
                val challengeCodeEditText : EditText = dialogView.findViewById(R.id.challengeEditText)

                val challengeCode = challengeCodeEditText.text.toString()
                Log.i(TAG, "Enter Challenge & Register:$challengeCode")
                val intentService = Intent(context, SonyIPControlIntentService::class.java)
                intentService.putExtra(SonyIPControlIntentService.CONTROL, ipControlJSON)
                intentService.putExtra(SonyIPControlIntentService.ACTION, SonyIPControlIntentService.REGISTER_CONTROL_ACTION)
                intentService.putExtra(SonyIPControlIntentService.CODE, challengeCode)
                activity?.startService(intentService)
                dialog.dismiss()
            }
        )
        dialogBuilder.setNeutralButton(R.string.dialog_cancel,
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() }
        )
        return dialogBuilder.create()
    }

}
