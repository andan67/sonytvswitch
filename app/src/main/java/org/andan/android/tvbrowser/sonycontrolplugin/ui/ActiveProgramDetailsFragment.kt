package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.FragmentActiveProgramDetailsBinding
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel

/**
 * A simple [Fragment] subclass.
 */
class ActiveProgramDetailsFragment : Fragment() {
    private val TAG = ActiveProgramDetailsFragment::class.java.name
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
        val binding: FragmentActiveProgramDetailsBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_active_program_details, container, false
        )

        binding.lifecycleOwner = this
        binding.sonyControlViewModel = sonyControlViewModel

        return binding.root
    }
}