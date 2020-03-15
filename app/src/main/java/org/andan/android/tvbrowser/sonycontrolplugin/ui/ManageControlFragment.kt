package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_manage_control.*
import org.andan.android.tvbrowser.sonycontrolplugin.MainActivity
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.network.SonyIPControlIntentService
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.ControlViewModel
import java.text.DateFormat
import java.text.DateFormat.getDateTimeInstance

class ManageControlFragment : Fragment() {
    private val TAG = ManageControlFragment::class.java.name

    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_manage_control, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        controlViewModel = ViewModelProviders.of(activity!!).get(ControlViewModel::class.java)
        // TODO: Use the ViewModel
        controlViewModel.getControls().observe(viewLifecycleOwner, Observer {
            if(controlViewModel.getSelectedControlIndex() >= 0) {
                controlDetailIPValueTextView.text = controlViewModel.getSelectedControl()?.ip
                controlDetailNicknameValueTextView.text =
                    controlViewModel.getSelectedControl()?.nickname
                controlDetailDevicenameValueTextView.text =
                    controlViewModel.getSelectedControl()?.devicename
                controlDetailUuidValueTextView.text = controlViewModel.getSelectedControl()?.uuid
                controlDetailCookieValueTextView.text = controlViewModel.getSelectedControl()?.cookie
                controlDetailCookieExpireValueTextView.text =
                    getDateTimeInstance(DateFormat.SHORT, DateFormat.DEFAULT).format(controlViewModel.getSelectedControl()?.cookieExprireTime)
                controlDetailNumberOfProgramsTextView.text = String.format(
                    "%d",
                    controlViewModel.getSelectedControl()?.programList?.size ?: -1
                )
                controlDetailSystemModel.text = controlViewModel.getSelectedControl()?.systemProductInformation
                controlDetailSystemMacAddr.text = controlViewModel.getSelectedControl()?.systemMacAddr
                controlDetailSystemWolMode.text = controlViewModel.getSelectedControl()?.systemWolMode.toString()

                Log.d(TAG, "auth cookie: ${controlViewModel.getSelectedControl()?.cookie}")
            } else {
                controlDetailIPValueTextView.text = ""
                controlDetailNicknameValueTextView.text = ""
                controlDetailDevicenameValueTextView.text = ""
                controlDetailUuidValueTextView.text = ""
                controlDetailCookieValueTextView.text = ""
                controlDetailCookieExpireValueTextView.text = ""
                controlDetailNumberOfProgramsTextView.text = ""
            }
            Log.d(TAG, "observed change getControls")
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.manage_control_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_control -> {
                val builder = AlertDialog.Builder(context!!)
                builder.setMessage("Do you want to delete this control?").setTitle("Confirm delete")
                builder.setPositiveButton("Yes") { dialog, id ->
                    Log.d(TAG, "deleteControl")
                    controlViewModel.deleteSelectedControl()
                }
                builder.setNegativeButton(
                    "No"
                ) { dialog, id -> dialog.dismiss() }
                val dialog = builder.create()
                dialog.show()
            }
            R.id.register_control -> {
                val extras = Bundle()
                extras.putInt(
                    SonyIPControlIntentService.ACTION,
                    SonyIPControlIntentService.REGISTER_CONTROL_ACTION
                )
                (activity as MainActivity).startControlService(extras)
            }
            R.id.get_program_list -> {
                val extras = Bundle()
                extras.putInt(
                    SonyIPControlIntentService.ACTION,
                    SonyIPControlIntentService.SET_PROGRAM_LIST_ACTION
                )
                (activity as MainActivity).startControlService(extras)
            }
            R.id.enable_wol -> {
                val extras = Bundle()
                extras.putInt(
                    SonyIPControlIntentService.ACTION,
                    SonyIPControlIntentService.ENABLE_WOL_ACTION
                )
                (activity as MainActivity).startControlService(extras)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
