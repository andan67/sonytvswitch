package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_add_control_register_dialog.*
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_ERROR_FATAL
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_ERROR_NON_FATAL
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
                Log.d(TAG, "observed $it")

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
                    REGISTRATION_ERROR_NON_FATAL -> {
                        //messageTextView.text = getString(R.string.add_control_register_failed_message)
                        messageTextView.text = it.message
                        mode = 2
                    }
                    REGISTRATION_ERROR_FATAL -> {
                        //messageTextView.text = getString(R.string.add_control_register_failed_message)
                        messageTextView.text = it.message
                        dialog!!.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        mode = 3
                    }
                    REGISTRATION_SUCCESSFUL -> {
                        sonyControlViewModel.postRegistrationFetches()
                        val navController = activity!!.findNavController(R.id.nav_host_fragment)
                        navController.navigate(R.id.nav_manage_control)
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
                        if(!addControlPSKEditText!!.text!!.toString().isNullOrEmpty()) {
                            sonyControlViewModel.selectedSonyControl.value!!.preSharedKey
                            sonyControlViewModel.registerControl()
                        }
                        else sonyControlViewModel.registerControl(addControlChallengeCodeEditView.text.toString())
                    }
                }
            }
        }
        return dialog!!
    }
}
