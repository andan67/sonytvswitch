package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.app.Dialog
import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_add_control_register_dialog.*
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.network.SSDP
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel

/**
 * A simple [Fragment] subclass.
 */
class AddControlRegistrationDialogFragment : DialogFragment() {

    private val TAG = AddControlRegistrationDialogFragment::class.java.name
    private val sonyControlViewModel: SonyControlViewModel by activityViewModels()
    private var dialog: AlertDialog? = null

    private val containerView by lazy {
        this.activity!!.layoutInflater.inflate(R.layout.fragment_add_control_register_dialog, null, false) as ViewGroup
    }

    override fun getView() = containerView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        addControlChallengeCodeTextView.visibility = View.GONE
        addControlChallengeCodeEditView.visibility = View.GONE
        return containerView
    }

    override fun onDestroyView() {
        // avoid leak
        super.onDestroyView()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(TAG, "onCreateDialog")
        val dialogBuilder = AlertDialog.Builder(context!!)
        dialogBuilder.setMessage(R.string.add_control_register_title)

        //var hostValue = ""

        dialogBuilder.setView(containerView)
        Log.d(TAG, " dialogBuilder.setView(dialogView)")
        dialogBuilder.setPositiveButton(R.string.add_control_host_pos, null)
        dialogBuilder.setNegativeButton(R.string.add_control_host_neg) { dialog, _ -> dialog.cancel() }
        dialogBuilder.setNeutralButton(R.string.add_control_host_neu,null)

        dialog = dialogBuilder.create()

        dialog!!.setOnShowListener {
            val neutralButton = dialog!!.getButton(AlertDialog.BUTTON_NEUTRAL)
            neutralButton.setOnClickListener {
                // dialog won't close by default
                //Log.d(TAG, "Test host=$host")
            }
            val positiveButton = dialog!!.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                //Log.d(TAG, "Test host=$host")
                sonyControlViewModel.addedControlParameter!!.nickname = addControlNicknameEditText.text.toString()
                sonyControlViewModel.addedControlParameter!!.devicename = addControlDevicenameEditText.text.toString()
                sonyControlViewModel.addedControlParameter!!.preSharedKey = addControlPSKEditText.text.toString()
            }

        }



        return dialog!!
    }
}
