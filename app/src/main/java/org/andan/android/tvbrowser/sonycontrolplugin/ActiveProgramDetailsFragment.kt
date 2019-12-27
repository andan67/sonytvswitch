package org.andan.android.tvbrowser.sonycontrolplugin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.FragmentActiveProgramDetailsBinding

/**
 * A simple [Fragment] subclass.
 */
class ActiveProgramDetailsFragment : Fragment() {
    private val TAG = ActiveProgramDetailsFragment::class.java.name
    private lateinit var controlViewModel: ControlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentActiveProgramDetailsBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_active_program_details, container, false
        )

        controlViewModel = ViewModelProviders.of(activity!!).get(ControlViewModel::class.java)
        binding.activeProgram = controlViewModel.activeContentInfo.value

        controlViewModel.activeContentInfo.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "observed change activeContentInfo")
            binding.activeProgram = controlViewModel.activeContentInfo.value
        })

        return binding.root
    }
}