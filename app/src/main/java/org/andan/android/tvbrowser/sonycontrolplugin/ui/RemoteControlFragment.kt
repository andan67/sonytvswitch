package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.FragmentRemoteControlBinding
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 */
class RemoteControlFragment : Fragment() {
    private val sonyControlViewModel: SonyControlViewModel by activityViewModels()

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


        if (sonyControlViewModel.selectedSonyControl.value == null) {
            val alertDialogBuilder = AlertDialog.Builder(this.context)
            alertDialogBuilder.setTitle(resources.getString(R.string.alert_no_active_control_title))
            alertDialogBuilder.setMessage(resources.getString(R.string.alert_no_active_control_message))
            Timber.d("No active control")
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
                sonyControlViewModel.wakeOnLan()
            R.id.screen_off ->
                sonyControlViewModel.setPowerSavingMode("pictureOff")
            R.id.screen_on ->
                sonyControlViewModel.setPowerSavingMode("off")
        }
        //(activity as MainActivity).startControlService(extras)
        return super.onOptionsItemSelected(item)
    }

    private fun sendCode(name:String) {
        sonyControlViewModel.sendIRRCCByName(name)
    }

    class CommandListener(val clickListener: (name: String) -> Unit) {
        fun onClick(name: String) = clickListener(name)
    }

}
