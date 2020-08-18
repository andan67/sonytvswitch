package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 */
class AddControlRegistrationDialogFragment : DialogFragment() {
    private val sonyControlViewModel: SonyControlViewModel by activityViewModels()
    private var dialog: AlertDialog? = null
    private lateinit var addedControl: SonyControl
    private var mode = 0

    private val containerView by lazy {
        this.requireActivity().layoutInflater.inflate(R.layout.fragment_add_control_register_dialog, null, false) as ViewGroup
    }

    override fun getView() = containerView

    private fun initView() {
        addControlPSKTextView.visibility = View.VISIBLE
        addControlPSKEditText.visibility = View.VISIBLE
        addControlPSKEditText.text.clear()
        addControlChallengeCodeTextView.visibility = View.GONE
        addControlChallengeCodeEditView.visibility = View.GONE
        addControlChallengeCodeEditView.text.clear()
    }

    private fun isInputValid() : Boolean {
        return (addControlNicknameEditText.validate("Enter non empty string ") { s -> s.isNotEmpty() }
                .and(addControlDevicenameEditText.validate("Enter non empty string ") { s -> s.isNotEmpty() })
                .and((mode != 1).or(addControlChallengeCodeEditView.validate("Enter 4 digits") { s -> "\\d{4}".toRegex().matches(s) })))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initView()

        sonyControlViewModel.registrationResult.observe(viewLifecycleOwner,
            EventObserver<RegistrationStatus> {
                Timber.d("observed $it")

                when (it.code) {
                    REGISTRATION_REQUIRES_CHALLENGE_CODE -> {
                        // original request without pre-shared key
                        addControlPSKTextView.visibility = View.GONE
                        addControlPSKEditText.visibility = View.GONE
                        addControlChallengeCodeTextView.visibility = View.VISIBLE
                        addControlChallengeCodeEditView.visibility = View.VISIBLE
                        addControlChallengeCodeEditView.requestFocus()
                        addControlNicknameEditText.isEnabled = false
                        addControlDevicenameEditText.isEnabled = false
                        messageTextView.text = getString(R.string.dialog_enter_challenge_code_title)
                        mode = 1
                    }
                    REGISTRATION_UNAUTHORIZED -> {
                        messageTextView.text = getString(R.string.add_control_register_unauthorized_challenge_message)
                        //initView()
                        mode = 2
                    }
                    REGISTRATION_ERROR_NON_FATAL -> {
                        //messageTextView.text = getString(R.string.add_control_register_failed_message)
                        messageTextView.text = it.message
                        initView()
                        mode = 2
                    }
                    REGISTRATION_ERROR_FATAL -> {
                        //messageTextView.text = getString(R.string.add_control_register_failed_message)
                        messageTextView.text = it.message
                        dialog!!.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        mode = 3
                    }
                    REGISTRATION_SUCCESSFUL -> {
                        sonyControlViewModel.addControl(addedControl!!)
                        sonyControlViewModel.postRegistrationFetches()
                        val navController = requireActivity().findNavController(R.id.nav_host_fragment)
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
        Timber.d("onCreateDialog")
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage(R.string.add_control_register_title)

        //var hostValue = ""

        dialogBuilder.setView(containerView)
        Timber.d(" dialogBuilder.setView(dialogView)")
        dialogBuilder.setPositiveButton(R.string.add_control_register_pos, null)
        dialogBuilder.setNegativeButton(R.string.add_control_host_neg) { dialog, _ -> dialog.cancel() }

        dialog = dialogBuilder.create()

        //addControlChallengeCodeEditView.validate("Enter 4 digits") { s -> "d{4}".toRegex().matches(s)}
        //addControlPSKEditText.validate("Enter non emtpy string") { s -> s.isNotEmpty()}
        mode = 0
        dialog!!.setOnShowListener {
            val neutralButton = dialog!!.getButton(AlertDialog.BUTTON_NEUTRAL)
            neutralButton.setOnClickListener {
                // dialog won't close by default
                //Timber.d("Test host=$host")
            }
            val positiveButton = dialog!!.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                //Timber.d("Test host=$host")
                // validate
                if(isInputValid()) {
                    when (mode) {
                        0 -> {
                            addedControl = SonyControl(
                                sonyControlViewModel.addedControlHostAddress,
                                addControlNicknameEditText.text.toString(),
                                addControlDevicenameEditText.text.toString(),
                                addControlPSKEditText.text.toString()
                            )
                            sonyControlViewModel.registerControl(addedControl)
                        }
                        1 -> {
                            sonyControlViewModel.registerControl(addedControl, addControlChallengeCodeEditView.text.toString())
                        }
                        2 -> {
                            mode = 0
                            if (!addControlPSKEditText!!.text!!.toString().isNullOrEmpty()) {
                                addedControl.preSharedKey=addControlPSKEditText.text.toString()
                                sonyControlViewModel.registerControl(addedControl)
                            } else sonyControlViewModel.registerControl(addedControl,
                                addControlChallengeCodeEditView.text.toString()
                            )
                        }
                    }
                }
            }
        }
        return dialog!!
    }
}
