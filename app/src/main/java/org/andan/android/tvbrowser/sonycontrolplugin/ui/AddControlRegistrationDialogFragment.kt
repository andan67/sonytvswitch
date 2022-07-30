package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.FragmentAddControlRegisterDialogBinding
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

    private var binding: FragmentAddControlRegisterDialogBinding? = null

    private val containerView by lazy {
        this.requireActivity().layoutInflater.inflate(
            R.layout.fragment_add_control_register_dialog,
            null,
            false
        ) as ViewGroup
    }

    override fun getView() = containerView

    private fun initView() {
        binding!!.addControlPSKTextView.visibility = View.VISIBLE
        binding!!.addControlPSKEditText.visibility = View.VISIBLE
        binding!!.addControlPSKEditText.text.clear()
        binding!!.addControlChallengeCodeTextView.visibility = View.GONE
        binding!!.addControlChallengeCodeEditView.visibility = View.GONE
        binding!!.addControlChallengeCodeEditView.text.clear()
    }

    private fun isInputValid(): Boolean {
        return (binding!!.addControlNicknameEditText.validate("Enter non empty string ") { s -> s.isNotEmpty() }
            .and(binding!!.addControlDevicenameEditText.validate("Enter non empty string ") { s -> s.isNotEmpty() })
            .and((mode != 1).or(binding!!.addControlChallengeCodeEditView.validate("Enter 4 digits") { s ->
                "\\d{4}".toRegex().matches(s)
            })))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val _binding = FragmentAddControlRegisterDialogBinding.bind(containerView)
        binding = _binding

        initView()

        sonyControlViewModel.registrationResult.observe(viewLifecycleOwner,
            EventObserver<RegistrationStatus> {
                Timber.d("observed $it")

                when (it.code) {
                    REGISTRATION_REQUIRES_CHALLENGE_CODE -> {
                        // original request without pre-shared key
                        binding!!.addControlPSKTextView.visibility = View.GONE
                        binding!!.addControlPSKEditText.visibility = View.GONE
                        binding!!.addControlChallengeCodeTextView.visibility = View.VISIBLE
                        binding!!.addControlChallengeCodeEditView.visibility = View.VISIBLE
                        binding!!.addControlChallengeCodeEditView.requestFocus()
                        binding!!.addControlNicknameEditText.isEnabled = false
                        binding!!.addControlDevicenameEditText.isEnabled = false
                        binding!!.messageTextView.text =
                            getString(R.string.dialog_enter_challenge_code_title)
                        mode = 1
                    }
                    REGISTRATION_UNAUTHORIZED -> {
                        binding!!.messageTextView.text =
                            getString(R.string.add_control_register_unauthorized_challenge_message)
                        //initView()
                        mode = 2
                    }
                    REGISTRATION_ERROR_NON_FATAL -> {
                        //messageTextView.text = getString(R.string.add_control_register_failed_message)
                        binding!!.messageTextView.text = it.message
                        initView()
                        mode = 2
                    }
                    REGISTRATION_ERROR_FATAL -> {
                        //messageTextView.text = getString(R.string.add_control_register_failed_message)
                        binding!!.messageTextView.text = it.message
                        dialog!!.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        mode = 3
                    }
                    REGISTRATION_SUCCESSFUL -> {
                        sonyControlViewModel.addControl(addedControl!!)
                        sonyControlViewModel.postRegistrationFetches()
                        val navController =
                            requireActivity().findNavController(R.id.nav_host_fragment)
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
                if (isInputValid()) {
                    when (mode) {
                        0 -> {
                            addedControl = SonyControl(
                                sonyControlViewModel.addedControlHostAddress,
                                binding!!.addControlNicknameEditText.text.toString(),
                                binding!!.addControlDevicenameEditText.text.toString(),
                                binding!!.addControlPSKEditText.text.toString()
                            )
                            sonyControlViewModel.registerControl(addedControl)
                        }
                        1 -> {
                            sonyControlViewModel.registerControl(
                                addedControl,
                                binding!!.addControlChallengeCodeEditView.text.toString()
                            )
                        }
                        2 -> {
                            mode = 0
                            if (!binding!!.addControlPSKEditText!!.text!!.toString()
                                    .isNullOrEmpty()
                            ) {
                                addedControl.preSharedKey =
                                    binding!!.addControlPSKEditText.text.toString()
                                sonyControlViewModel.registerControl(addedControl)
                            } else sonyControlViewModel.registerControl(
                                addedControl,
                                binding!!.addControlChallengeCodeEditView.text.toString()
                            )
                        }
                    }
                }
            }
        }
        return dialog!!
    }
}
