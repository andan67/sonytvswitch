package org.andan.android.tvbrowser.sonycontrolplugin.ui

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_manage_control.*
import org.andan.android.tvbrowser.sonycontrolplugin.MainActivity
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.FragmentManageControlBinding
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.FragmentProgramListBinding
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
        val binding: FragmentManageControlBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_manage_control, container, false
        )
        val view = binding.root

        binding.lifecycleOwner = activity
        binding.testViewModel = testViewModel

        testViewModel.selectedSonyControl.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "observed change ${testViewModel.selectedSonyControl.value}")
        })

        testViewModel.requestErrorMessage.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "observed requestError")
            if(!it.isNullOrEmpty()) Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        return binding.root
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

            }
            R.id.enable_wol -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }
}
