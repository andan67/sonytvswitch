package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.FragmentManageControlBinding
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.TestViewModel

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
        binding.lifecycleOwner = this
        binding.testViewModel = testViewModel

        Log.d(TAG, "sonyControls: ${testViewModel.sonyControls.value!!.controls.size}")
        Log.d(TAG, "testViewModel: $testViewModel")

        testViewModel.selectedSonyControl.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "observed change ${testViewModel.selectedSonyControl.value}")
        })

        testViewModel.requestErrorMessage.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "observed requestError")
            //if(!it.isNullOrEmpty()) Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            if(it.equals("Unauthorized")) {
                binding.root.findNavController().navigate(R.id.nav_enter_challenge)
            }
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
                    testViewModel.deleteSelectedControl()
                }
                builder.setNegativeButton(
                    "No"
                ) { dialog, id -> dialog.dismiss() }
                val dialog = builder.create()
                dialog.show()
            }
            R.id.register_control -> {
                testViewModel.registerControl()
            }
            R.id.get_program_list -> {
                testViewModel.fetchProgramList()
            }
            R.id.enable_wol -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }
}
