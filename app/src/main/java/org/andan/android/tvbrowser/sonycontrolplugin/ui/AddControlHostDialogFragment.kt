package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.network.InterfaceInformationResponse
import org.andan.android.tvbrowser.sonycontrolplugin.network.SSDP
import org.andan.android.tvbrowser.sonycontrolplugin.network.Status
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel

/**
 * A simple [Fragment] subclass.
 */
class AddControlHostDialogFragment : DialogFragment() {

    private val TAG = AddControlHostDialogFragment::class.java.name
    private val sonyControlViewModel: SonyControlViewModel by activityViewModels()
    private lateinit var sonyIpAndDeviceListAdapter: ArrayAdapter<SSDP.IpDeviceItem>
    private val deviceList = mutableListOf<SSDP.IpDeviceItem>()
    private var dialog: AlertDialog? = null
    private var dialogView: View? = null
    private var messageTextView: TextView? = null
    private var host: String = ""
    private var testMode = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //observer needs to be defined in onCreateView
        sonyControlViewModel.sonyIpAndDeviceList.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "observed change ${sonyControlViewModel.sonyIpAndDeviceList.value}")
            deviceList.clear()
            deviceList.add(SSDP.IpDeviceItem())
            deviceList.addAll(sonyControlViewModel.sonyIpAndDeviceList.value!!)
            Log.d(TAG, "deviceList: $deviceList")
            sonyIpAndDeviceListAdapter.notifyDataSetChanged()
        })

        sonyControlViewModel.interfaceInformation.observe(viewLifecycleOwner, Observer {
            //if(it.data != null && it.status==Status.SUCCESS) {
            if(testMode > 0 && host == "192.168.178.27") {
                //Log.d(TAG, "Product ${it.data.productName}")
                Log.d(TAG, "Test succussful")
                messageTextView!!.setTextColor(Color.GREEN)
                messageTextView!!.text = "Test successful"
                if(testMode == 2) {
                    Log.d(TAG, "Navigate to registration")
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
                Log.d(TAG, "Test unsuccussful")
                messageTextView!!.setTextColor(Color.RED)
                messageTextView!!.text = "Test of host/ip unsuccessful. Try again!"
            }
            testMode = 0
        })




        return dialogView
    }

    override fun onDestroyView() {
        // avoid leak
        dialogView = null
        super.onDestroyView()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(TAG, "onCreateDialog")
        val dialogBuilder = AlertDialog.Builder(context!!)
        dialogBuilder.setMessage(R.string.add_control_host_title)
        dialogView = this.activity!!.layoutInflater.inflate(R.layout.fragment_add_control_host_dialog, null, false)
        // fill device spinner
        deviceList.add(SSDP.IpDeviceItem())
        sonyControlViewModel.fetchSonyIpAndDeviceList()
        var hasSelected = false

        //var hostValue = ""
        val hostEditTest = dialogView!!.findViewById(R.id.addControlIPEditText) as EditText
        sonyIpAndDeviceListAdapter =
            ArrayAdapter(context!!, R.layout.control_spinner_item, deviceList)

        messageTextView = dialogView!!.findViewById(R.id.messageTextView) as TextView

        val deviceSpinner = dialogView!!.findViewById<Spinner>(R.id.deviceSpinner)
2
        deviceSpinner.adapter = sonyIpAndDeviceListAdapter

        deviceSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(position > 0) {
                    Log.d(TAG, "onItemSelected $position")
                    host = deviceList[position].ip
                    hasSelected = true
                    hostEditTest.setText(host)
                }
                //deviceSpinner.performItemClick(view, position, id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        hostEditTest.doAfterTextChanged {
            Log.d(TAG,"doAfterTextChanged: ${deviceSpinner.selectedItemPosition} $hasSelected")
            if(!hasSelected) {
                deviceSpinner.setSelection(0)
                host = hostEditTest.text.toString()
            }
            hasSelected = false
        }

        dialogBuilder.setView(dialogView)
        Log.d(TAG, " dialogBuilder.setView(dialogView)")
        dialogBuilder.setPositiveButton(R.string.add_control_host_pos, null)
        dialogBuilder.setNegativeButton(R.string.add_control_host_neg) { dialog, _ -> dialog.cancel() }
        dialogBuilder.setNeutralButton(R.string.add_control_host_neu,null)

        dialog = dialogBuilder.create()

        dialog!!.setOnShowListener {
            val neutralButton = dialog!!.getButton(AlertDialog.BUTTON_NEUTRAL)
            neutralButton.setOnClickListener {
                // dialog won't close by default
                Log.d(TAG, "Test host=$host")
                testMode = 1
                sonyControlViewModel.fetchInterfaceInformation(host)
            }
            val positiveButton = dialog!!.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                Log.d(TAG, "Test host=$host")
                testMode = 2
                sonyControlViewModel.fetchInterfaceInformation(host)
            }

        }

        return dialog!!
    }
}
