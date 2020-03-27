package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import org.andan.android.tvbrowser.sonycontrolplugin.MainActivity
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.network.SonyIPControlIntentService
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.FragmentRemoteControlBinding
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.ControlViewModel

/**
 * A simple [Fragment] subclass.
 */
class RemoteControlFragment : Fragment() {
    private val TAG = RemoteControlFragment::class.java.name
    private lateinit var controlViewModel: ControlViewModel
    //private var searchQuery: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentRemoteControlBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_remote_control, container, false)

        val view = binding.root

        controlViewModel = ViewModelProviders.of(activity!!).get(ControlViewModel::class.java)

        if(controlViewModel.getSelectedControlIndex()<0) {
            val alertDialogBuilder = AlertDialog.Builder(this.context)
            alertDialogBuilder.setCancelable(false)
            if (controlViewModel.getSelectedControl() == null) {
                alertDialogBuilder.setTitle(resources.getString(R.string.alert_no_active_control_title))
                alertDialogBuilder.setMessage(resources.getString(R.string.alert_no_active_control_message))
            } else {
                alertDialogBuilder.setTitle(resources.getString(R.string.alert_no_programs_title))
                alertDialogBuilder.setMessage(resources.getString(R.string.alert_no_programs_message))
            }
            alertDialogBuilder.setPositiveButton(
                resources.getString(R.string.dialog_ok)
            ) { dialog, arg1 -> dialog.dismiss() }
            alertDialogBuilder.create().show()
        } else
        {
            binding.clickListener =
                CommandListener { name: String ->
                    sendCode(name)
                }

        }
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.remote_control_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val extras = Bundle()
        when (item.itemId) {
            R.id.wake_on_lan ->
                extras.putInt(
                    SonyIPControlIntentService.ACTION,
                    SonyIPControlIntentService.WOL_ACTION
                )
            R.id.screen_off ->
                extras.putInt(
                    SonyIPControlIntentService.ACTION,
                    SonyIPControlIntentService.SCREEN_ON_ACTION
                )
            R.id.screen_on ->
                extras.putInt(
                    SonyIPControlIntentService.ACTION,
                    SonyIPControlIntentService.SCREEN_OFF_ACTION
                )
        }
        //(activity as MainActivity).startControlService(extras)
        return super.onOptionsItemSelected(item)
    }

    private fun sendCode(name:String) {
        val extras = Bundle()
        extras.putInt(
            SonyIPControlIntentService.ACTION,
            SonyIPControlIntentService.SEND_IRCC_BY_NAME_ACTION
        )
        extras.putString(SonyIPControlIntentService.CODE, name)
        //(activity as MainActivity).startControlService(extras)
    }

    class CommandListener(val clickListener: (name: String) -> Unit) {
        fun onClick(name: String) = clickListener(name)
    }

}
