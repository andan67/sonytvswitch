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
import kotlinx.android.synthetic.main.fragment_add_control_host_dialog.*
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.network.SSDP
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 */
class AddControlHostDialogFragment : DialogFragment() {
    private val sonyControlViewModel: SonyControlViewModel by activityViewModels()
    private lateinit var sonyIpAndDeviceListAdapter: ArrayAdapter<SSDP.IpDeviceItem>
    private val deviceList = mutableListOf<SSDP.IpDeviceItem>()
    private var dialog: AlertDialog? = null
    //private var host: String = ""
    private var testMode = 0


    private val containerView by lazy {
        this.activity!!.layoutInflater.inflate(R.layout.fragment_add_control_host_dialog, null, false) as ViewGroup
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

        sonyControlViewModel.interfaceInformation.observe(viewLifecycleOwner, Observer {
            //if(it.data != null && it.status==Status.SUCCESS) {
            if(testMode > 0 && sonyControlViewModel.addedControlHostAddress == "192.168.178.27") {
                //Timber.d("Product ${it.data.productName}")
                Timber.d("Test succussful")
                messageTextView!!.setTextColor(Color.GREEN)
                messageTextView!!.text = "Test successful"
                if(testMode == 2) {
                    Timber.d("Navigate to registration")
                   /* val navController = activity!!.findNavController(R.id.nav_host_fragment)
                    navController.navigate(R.id.action_nav_add_control_host_to_nav_add_control_register)*/
                    val addControlRegistrationDialogFragment = AddControlRegistrationDialogFragment()
                    val transaction: FragmentTransaction =
                        requireActivity().supportFragmentManager.beginTransaction()
                    addControlRegistrationDialogFragment.show(transaction, "add_control_register_dialog")
                    if (testMode == 2) dialog!!.dismiss()
                }
            }
            else if (testMode > 0){
                Timber.d("Test unsuccussful")
                messageTextView!!.setTextColor(Color.RED)
                messageTextView!!.text = "Test of host/ip unsuccessful. Try again!"
            }
            testMode = 0
        })
        return containerView
    }

    override fun onDestroyView() {
        // avoid leak
        super.onDestroyView()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Timber.d("onCreateDialog")
        val dialogBuilder = AlertDialog.Builder(context!!)
        dialogBuilder.setMessage(R.string.add_control_host_title)
        dialogBuilder.setView(containerView)
        Timber.d(" dialogBuilder.setView(dialogView)")
        dialogBuilder.setPositiveButton(R.string.add_control_host_pos, null)
        dialogBuilder.setNegativeButton(R.string.add_control_host_neg) { dialog, _ -> dialog.cancel() }
        dialogBuilder.setNeutralButton(R.string.add_control_host_neu,null)

        dialog = dialogBuilder.create()

        // fill device spinner
        deviceList.add(SSDP.IpDeviceItem())
        sonyControlViewModel.fetchSonyIpAndDeviceList()
        var hasSelected = false

        sonyIpAndDeviceListAdapter =
            ArrayAdapter(context!!, R.layout.control_spinner_item, deviceList)

        deviceSpinner.adapter = sonyIpAndDeviceListAdapter

        sonyControlViewModel.addedControlHostAddress = ""

        deviceSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(position > 0) {
                    Timber.d("onItemSelected $position")
                    sonyControlViewModel.addedControlHostAddress = deviceList[position].ip
                    hasSelected = true
                    addControlIPEditText.setText(sonyControlViewModel.addedControlHostAddress)
                }
                //deviceSpinner.performItemClick(view, position, id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        addControlIPEditText.doAfterTextChanged {
            Timber.d("doAfterTextChanged: ${deviceSpinner.selectedItemPosition} $hasSelected")
            if(!hasSelected) {
                deviceSpinner.setSelection(0)
                sonyControlViewModel.addedControlHostAddress = addControlIPEditText.text.toString()
            }
            hasSelected = false
        }

        dialog!!.setOnShowListener {
            val neutralButton = dialog!!.getButton(AlertDialog.BUTTON_NEUTRAL)
            neutralButton.setOnClickListener {
                // dialog won't close by default
                Timber.d("Test host=$host")
                testMode = 1
                sonyControlViewModel.fetchInterfaceInformation(sonyControlViewModel.addedControlHostAddress)
            }
            val positiveButton = dialog!!.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                Timber.d("Test host=$host")
                testMode = 2
                sonyControlViewModel.fetchInterfaceInformation(sonyControlViewModel.addedControlHostAddress)
            }

        }

        return dialog!!
    }
}
