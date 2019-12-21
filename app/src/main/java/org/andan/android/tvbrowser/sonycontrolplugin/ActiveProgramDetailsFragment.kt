package org.andan.android.tvbrowser.sonycontrolplugin

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.ActiveProgramItemBinding
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.FragmentActiveProgramDetailsBinding
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.FragmentProgramListBinding
import org.andan.av.sony.SonyIPControl
import org.andan.av.sony.model.SonyPlayingContentInfo
import org.andan.av.sony.model.SonyProgram

/**
 * A simple [Fragment] subclass.
 */
class ActiveProgramDetailsFragment : Fragment() {
    private val TAG = ActiveProgramDetailsFragment::class.java.name
    private lateinit var controlViewModel: ControlViewModel
    private var searchView: SearchView? = null
    private var queryTextListener: SearchView.OnQueryTextListener? = null
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
        val binding: FragmentActiveProgramDetailsBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_active_program_details, container, false
        )
        val view = binding.root

        controlViewModel = ViewModelProviders.of(activity!!).get(ControlViewModel::class.java)
        binding.activeProgram = controlViewModel.activeContentInfo.value

        controlViewModel.activeContentInfo.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "observed change activeContentInfo")
            binding.activeProgram = controlViewModel.activeContentInfo.value
        })

        return binding.root
    }
}