package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.FragmentAddControlHostDialogBinding
import org.andan.android.tvbrowser.sonycontrolplugin.network.SSDP
import org.andan.android.tvbrowser.sonycontrolplugin.network.Status
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 */
class AddControlHostDialogFragment : DialogFragment() {
    //val args: AddControlHostDialogFragment by navArgs()

    private val sonyControlViewModel: SonyControlViewModel by activityViewModels()
    private lateinit var sonyIpAndDeviceListAdapter: ArrayAdapter<SSDP.IpDeviceItem>
    private val deviceList = mutableListOf<SSDP.IpDeviceItem>()
    private var dialog: AlertDialog? = null

    //private var host: String = ""
    private var testMode = 0
    private var isAddedControlMode = false

    private var binding: FragmentAddControlHostDialogBinding? = null

    private val containerView by lazy {
        this.requireActivity().layoutInflater.inflate(
            R.layout.fragment_add_control_host_dialog,
            null,
            false
        ) as ViewGroup
    }

    override fun getView() = containerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //observer needs to be defined in onCreateView
        sonyControlViewModel.sonyIpAndDeviceList.observe(viewLifecycleOwner, Observer {
            Timber.d("observed change ${sonyControlViewModel.sonyIpAndDeviceList.value}")
            deviceList.clear()
            deviceList.add(SSDP.IpDeviceItem())
            deviceList.addAll(sonyControlViewModel.sonyIpAndDeviceList.value!!)
            Timber.d("deviceList: $deviceList")
            sonyIpAndDeviceListAdapter.notifyDataSetChanged()
        })

        sonyControlViewModel.powerStatus.observe(viewLifecycleOwner, Observer {
            if (it.data != null && it.status == Status.SUCCESS) {
                if (testMode > 0) {
                    //Timber.d("Product ${it.data.productName}")
                    Timber.d("Test succussful")
                    if (!isAddedControlMode) {
                        binding!!.messageTextView.setTextColor(Color.GREEN)
                        binding!!.messageTextView.text = "Test successful"
                    }
                    if (testMode == 2) {
                        Timber.d("Navigate to registration")
                        /* val navController = activity!!.findNavController(R.id.nav_host_fragment)
                         navController.navigate(R.id.action_nav_add_control_host_to_nav_add_control_register)*/
                        val addControlRegistrationDialogFragment =
                            AddControlRegistrationDialogFragment()
                        val transaction: FragmentTransaction =
                            requireActivity().supportFragmentManager.beginTransaction()
                        addControlRegistrationDialogFragment.show(
                            transaction,
                            "add_control_register_dialog"
                        )
                        if (testMode == 2) dialog!!.dismiss()
                    }
                }
            } else if (testMode > 0) {
                Timber.d("Test unsuccessful")
                binding!!.messageTextView.setTextColor(Color.RED)
                binding!!.messageTextView.text = getString(R.string.add_control_host_failed_msg)
            }
            testMode = 0
        })
        return containerView
    }

    override fun onDestroyView() {
        // avoid leak
        super.onDestroyView()
        binding = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Timber.d("onCreateDialog")
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(containerView)
        val _binding = FragmentAddControlHostDialogBinding.bind(containerView)
        binding = _binding
        Timber.d(" dialogBuilder.setView(dialogView)")
        Timber.d("isAddControlMode: %s", arguments?.get("isAddControlMode"))
        isAddedControlMode = arguments?.getBoolean("isAddControlMode", false)!!


        if (isAddedControlMode) {
            dialogBuilder.setMessage(R.string.add_control_host_title)
            dialogBuilder.setPositiveButton(R.string.add_control_host_pos, null)
            dialogBuilder.setNegativeButton(R.string.add_control_host_neg) { dialog, _ -> dialog.cancel() }
        } else {
            dialogBuilder.setMessage(R.string.add_control_host_check_title)
            binding!!.instructionsTextView.visibility = View.GONE
            dialogBuilder.setPositiveButton(R.string.add_control_host_check_pos, null)
            dialogBuilder.setNegativeButton(R.string.add_control_host_neg) { dialog, _ -> dialog.cancel() }
            dialogBuilder.setNeutralButton(R.string.add_control_host_neu, null)
        }

        dialog = dialogBuilder.create()

        // fill device spinner
        deviceList.add(SSDP.IpDeviceItem())
        sonyControlViewModel.fetchSonyIpAndDeviceList()
        var hasSelected = false

        sonyIpAndDeviceListAdapter =
            ArrayAdapter(requireContext(), R.layout.control_spinner_item, deviceList)

        binding!!.deviceSpinner.adapter = sonyIpAndDeviceListAdapter

        if (isAddedControlMode) {
            sonyControlViewModel.addedControlHostAddress = ""
        } else {
            Timber.d("ip: ${sonyControlViewModel.selectedSonyControl.value?.ip}")
            sonyControlViewModel.addedControlHostAddress =
                sonyControlViewModel.selectedSonyControl.value?.ip ?: ""
            binding!!.addControlIPEditText.setText(sonyControlViewModel.addedControlHostAddress)
        }

        binding!!.deviceSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position > 0) {
                    Timber.d("onItemSelected $position")
                    sonyControlViewModel.addedControlHostAddress = deviceList[position].ip
                    hasSelected = true
                    binding!!.addControlIPEditText.setText(sonyControlViewModel.addedControlHostAddress)
                }
                //deviceSpinner.performItemClick(view, position, id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        binding!!.addControlIPEditText.doAfterTextChanged {
            Timber.d("doAfterTextChanged: ${binding!!.deviceSpinner.selectedItemPosition} $hasSelected")
            if (!hasSelected) {
                binding!!.deviceSpinner.setSelection(0)
                sonyControlViewModel.addedControlHostAddress =
                    binding!!.addControlIPEditText.text.toString()
            }
            hasSelected = false
        }

        dialog!!.setOnShowListener {
            val neutralButton = dialog!!.getButton(AlertDialog.BUTTON_NEUTRAL)
            neutralButton.setOnClickListener {
                // dialog won't close by default
                Timber.d("Test host=$host")
                testMode = 1
                binding!!.messageTextView.setTextColor(Color.GRAY)
                binding!!.messageTextView.text = getString(R.string.add_control_host_testing_msg)
                sonyControlViewModel.fetchPowerStatus(sonyControlViewModel.addedControlHostAddress)
            }
            val positiveButton = dialog!!.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                if (isAddedControlMode) {
                    Timber.d("Test host=$host")
                    testMode = 2
                    sonyControlViewModel.fetchPowerStatus(sonyControlViewModel.addedControlHostAddress)
                } else {
                    sonyControlViewModel.setSelectedHost(sonyControlViewModel.addedControlHostAddress)
                    dialog!!.dismiss()
                }
            }
        }

        return dialog!!
    }
}
