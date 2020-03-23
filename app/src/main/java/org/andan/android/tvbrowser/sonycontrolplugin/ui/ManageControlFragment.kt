package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_manage_control.*
import org.andan.android.tvbrowser.sonycontrolplugin.MainActivity
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.network.SonyIPControlIntentService
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.ControlViewModel
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.TestViewModel
import java.text.DateFormat
import java.text.DateFormat.getDateTimeInstance

class ManageControlFragment : Fragment() {
    private val TAG = ManageControlFragment::class.java.name
    private val testViewModel: TestViewModel by activityViewModels()

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
        testViewModel.sonyControls.observe(viewLifecycleOwner, Observer {
            if(testViewModel.sonyControls.value!!.selected >= 0) {
                controlDetailIPValueTextView.text = testViewModel.getSelectedControl()?.ip
                controlDetailNicknameValueTextView.text =
                    testViewModel.getSelectedControl()?.nickname
                controlDetailDevicenameValueTextView.text =
                    testViewModel.getSelectedControl()?.devicename
                controlDetailUuidValueTextView.text = testViewModel.getSelectedControl()?.uuid
                controlDetailCookieValueTextView.text = testViewModel.getSelectedControl()?.cookie
                controlDetailCookieExpireValueTextView.text = ""
                controlDetailNumberOfProgramsTextView.text = String.format(
                    "%d",
                    testViewModel.getSelectedControl()?.programList?.size ?: -1
                )
                //controlDetailSystemModel.text = testViewModel.getSelectedControl()?.systemProductInformation
                controlDetailSystemModel.text = ""
                controlDetailSystemMacAddr.text = testViewModel.getSelectedControl()?.systemMacAddr
                controlDetailSystemWolMode.text = testViewModel.getSelectedControl()?.systemWolMode.toString()

                Log.d(TAG, "auth cookie: ${testViewModel.getSelectedControl()?.cookie}")
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
                    //controlViewModel.deleteSelectedControl()
                }
                builder.setNegativeButton(
                    "No"
                ) { dialog, id -> dialog.dismiss() }
                val dialog = builder.create()
                dialog.show()
            }
            R.id.register_control -> {
                /*val extras = Bundle()
                extras.putInt(
                    SonyIPControlIntentService.ACTION,
                    SonyIPControlIntentService.REGISTER_CONTROL_ACTION
                )
                (activity as MainActivity).startControlService(extras)*/
                testViewModel.registerControl()
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
