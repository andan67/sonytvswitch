package org.andan.android.tvbrowser.sonycontrolplugin.ui


import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.TestViewModel

/**
 * A simple [Fragment] subclass.
 */
class EnterChallengeDialogFragment : DialogFragment() {

    private val TAG = EnterChallengeDialogFragment::class.java.name
    private val testViewModel: TestViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(context!!)
        dialogBuilder.setMessage(R.string.dialog_enter_challenge_code_title)
        val dialogView:View = this.activity!!.layoutInflater.inflate(R.layout.fragment_enter_challenge_code_dialog, null, false)
        dialogBuilder.setView(dialogView)


        dialogBuilder.setPositiveButton(
            R.string.dialog_set
        ) { dialog, _ ->
            // Write your code here to execute after dialog
            val challengeCodeEditText : EditText = dialogView.findViewById(R.id.challengeEditText)

            val challengeCode = challengeCodeEditText.text.toString()
            Log.i(TAG, "Enter Challenge & Register:$challengeCode")
            /*val intentService = Intent(context, SonyIPControlIntentService::class.java)
            intentService.putExtra(SonyIPControlIntentService.CONTROL, ipControlJSON)
            intentService.putExtra(
                SonyIPControlIntentService.ACTION,
                SonyIPControlIntentService.REGISTER_CONTROL_ACTION
            )
            intentService.putExtra(SonyIPControlIntentService.CODE, challengeCode)
            activity?.startService(intentService)*/
            dialog.dismiss()
        }
        dialogBuilder.setNeutralButton(R.string.dialog_cancel)
            { dialog, _ -> dialog.cancel() }
        return dialogBuilder.create()
    }
}
