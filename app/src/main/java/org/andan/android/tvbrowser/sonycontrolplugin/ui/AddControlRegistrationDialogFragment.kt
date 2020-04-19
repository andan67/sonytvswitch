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
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_add_control_register_dialog.*
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_FAILED
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_REQUIRES_CHALLENGE_CODE
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_SUCCESSFUL
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_UNAUTHORIZED
import org.andan.android.tvbrowser.sonycontrolplugin.repository.EventObserver
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel

/**
 * A simple [Fragment] subclass.
 */
class AddControlRegistrationDialogFragment : DialogFragment() {

    private val TAG = AddControlRegistrationDialogFragment::class.java.name
    private val sonyControlViewModel: SonyControlViewModel by activityViewModels()
    private var dialog: AlertDialog? = null
    private var addedControl: SonyControl? = null
    private var mode = 0

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

        sonyControlViewModel.registrationResult.observe(viewLifecycleOwner,
            EventObserver<RegistrationStatus> {
                Log.d(TAG, "observed requestError")

                when (it.code) {
                    REGISTRATION_REQUIRES_CHALLENGE_CODE -> {
                        addControlPSKTextView.visibility = View.GONE
                        addControlPSKEditText.visibility = View.GONE
                        addControlChallengeCodeTextView.visibility = View.VISIBLE
                        addControlChallengeCodeEditView.visibility = View.VISIBLE
                        addControlNicknameEditText.isEnabled = false
                        addControlDevicenameEditText.isEnabled = false
                        messageTextView.text = getString(R.string.dialog_enter_challenge_code_title)
                        mode = 1
                    }
                    REGISTRATION_UNAUTHORIZED -> {
                        messageTextView.text = getString(R.string.add_control_register_unauthorized_challenge_message)
                        mode = 2
                    }
                    REGISTRATION_FAILED -> {
                        //messageTextView.text = getString(R.string.add_control_register_failed_message)
                        messageTextView.text = it.message
                        dialog!!.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        mode = 3
                    }
                    REGISTRATION_SUCCESSFUL -> {
                        sonyControlViewModel.postRegistrationFetches()
                        dialog!!.dismiss()
                    }
                }
            }
        )
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
        dialogBuilder.setPositiveButton(R.string.add_control_register_pos, null)
        dialogBuilder.setNegativeButton(R.string.add_control_host_neg) { dialog, _ ->
            sonyControlViewModel.deleteSelectedControl()
            dialog.cancel() }
        dialogBuilder.setNeutralButton(R.string.add_control_host_neu,null)

        dialog = dialogBuilder.create()

        mode = 0
        dialog!!.setOnShowListener {
            val neutralButton = dialog!!.getButton(AlertDialog.BUTTON_NEUTRAL)
            neutralButton.setOnClickListener {
                // dialog won't close by default
                //Log.d(TAG, "Test host=$host")
            }
            val positiveButton = dialog!!.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                //Log.d(TAG, "Test host=$host")
                when (mode) {
                    0 -> {
                        addedControl = SonyControl(
                            sonyControlViewModel.addedControlHostAddress,
                            addControlNicknameEditText.text.toString(),
                            addControlDevicenameEditText.text.toString(),
                            addControlPSKEditText.text.toString()
                        )
                        sonyControlViewModel.addControl(addedControl!!)
                        // call registration
                        sonyControlViewModel.registerControl()
                    }
                    1 -> {
                        sonyControlViewModel.registerControl(addControlChallengeCodeEditView.text.toString())
                    }
                    2 -> {
                        sonyControlViewModel.selectedSonyControl.value!!.preSharedKey
                    }
                }
            }
        }
        return dialog!!
    }
}
