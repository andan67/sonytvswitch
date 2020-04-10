package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.TestFragmentBinding
import org.andan.android.tvbrowser.sonycontrolplugin.network.PlayingContentInfoResponse
import org.andan.android.tvbrowser.sonycontrolplugin.network.Resource
import org.andan.android.tvbrowser.sonycontrolplugin.network.Status
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.TestViewModel

class TestFragment : Fragment() {

    private val TAG = TestFragment::class.java.name
    private val viewModel: TestViewModel by activityViewModels()
    companion object {
        fun newInstance() = TestFragment()
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding: TestFragmentBinding = DataBindingUtil.inflate(
            inflater, R.layout.test_fragment, container, false
        )

        // Set the LifecycleOwner to be able to observe LiveData objects
        binding.lifecycleOwner = this
        binding.viewmodel = viewModel

        /* viewModel.currentTime.observe(viewLifecycleOwner, Observer<String> {
             Log.d(TAG, "observed change currenttime")
         })*/

        viewModel.playingContentInfo.observe(
            viewLifecycleOwner,
            Observer<PlayingContentInfoResponse> {
                Log.d(TAG, "observed change playingcontentinfo")
                binding.playingContentInfo = it
            })

        viewModel.selectedSonyControl.observe(
            viewLifecycleOwner,
            Observer {
                binding.sonyControl = it
                Log.d(TAG, "observed change selectedSonyControl")
            }
        )



        return binding.root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
}
