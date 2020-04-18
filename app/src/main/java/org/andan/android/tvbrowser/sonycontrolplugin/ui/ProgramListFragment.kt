package org.andan.android.tvbrowser.sonycontrolplugin.ui

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.FragmentProgramListBinding
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.ProgramItemBinding
import org.andan.android.tvbrowser.sonycontrolplugin.domain.PlayingContentInfo
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyProgram
import org.andan.android.tvbrowser.sonycontrolplugin.network.PlayingContentInfoResponse
import org.andan.android.tvbrowser.sonycontrolplugin.repository.EventObserver
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel

/**
 * A simple [Fragment] subclass.
 */
class ProgramListFragment : Fragment() {
    private val TAG = ProgramListFragment::class.java.name
    private val sonyControlViewModel: SonyControlViewModel by activityViewModels()
    private var searchView: SearchView? = null
    private var queryTextListener: SearchView.OnQueryTextListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentProgramListBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_program_list, container, false
        )
        val view = binding.root
        binding.lifecycleOwner = this

        val fab: FloatingActionButton = view.findViewById(R.id.listProgramFab)
        fab.setOnClickListener { view ->
            if (sonyControlViewModel.lastProgramUri.isNotEmpty()) {
                sonyControlViewModel.setAndFetchPlayContent(sonyControlViewModel.lastProgramUri)
            }
        }

        if (sonyControlViewModel.getFilteredProgramList().value.isNullOrEmpty()) {
            val alertDialogBuilder = AlertDialog.Builder(this.context)
            alertDialogBuilder.setCancelable(false)
            if (sonyControlViewModel.selectedSonyControl.value == null) {
                alertDialogBuilder.setTitle(resources.getString(R.string.alert_no_active_control_title))
                alertDialogBuilder.setMessage(resources.getString(R.string.alert_no_active_control_message))
                Log.d(TAG, "No active control")
            } else {
                alertDialogBuilder.setTitle(resources.getString(R.string.alert_no_programs_title))
                alertDialogBuilder.setMessage(resources.getString(R.string.alert_no_programs_message))
            }
            alertDialogBuilder.setPositiveButton(
                resources.getString(R.string.dialog_ok)
            ) { dialog, _ -> dialog.dismiss() }
            alertDialogBuilder.create().show()
        } else {
            binding.sonyControlViewModel = sonyControlViewModel
            binding.activeProgram.sonyControlViewModel = sonyControlViewModel

            binding.activeProgram.activeProgramView.setOnClickListener {
                //Toast.makeText(context, "Click on ${activeProgram?.title}", Toast.LENGTH_LONG) .show()
                if (sonyControlViewModel.playingContentInfo.value == PlayingContentInfo()) {
                    Toast.makeText(context, "No current program", Toast.LENGTH_LONG).show()
                } else {
                    view.findNavController()
                        .navigate(R.id.action_nav_program_list_to_activeProgramDetailsFragment)
                }
            }

            binding.activeProgram.activeProgramView.setOnLongClickListener {
                sonyControlViewModel.fetchPlayingContentInfo()
                true
            }

            val adapter =
                ProgramItemRecyclerViewAdapter(
                    ProgramListener(
                        { program: SonyProgram ->
                            //Toast.makeText(context, "Switched to ${program.title}", Toast.LENGTH_LONG).show()
                            sonyControlViewModel.setAndFetchPlayContent(program.uri)
                        },
                        { _: SonyProgram ->
                            true
                            // Toast.makeText(context, "Long clicked  ${program.title}", Toast.LENGTH_LONG) .show()
                            //sonyControlViewModel.onProgramLongClicked(program)
                        }), sonyControlViewModel
                )

            binding.listProgram.adapter = adapter

            sonyControlViewModel.getFilteredProgramList().observe(viewLifecycleOwner, Observer {
                Log.d(
                    TAG,
                    "observed change filtered program list with filter ${sonyControlViewModel.getProgramSearchQuery()}"
                )
                adapter.notifyDataSetChanged()
            })

            sonyControlViewModel.requestErrorMessage.observe(viewLifecycleOwner,
                EventObserver<String> {
                    Log.d(TAG, it)
                    if (it.isNotEmpty()) Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                }
            )

            val manager = GridLayoutManager(activity, 1)
            binding.listProgram.layoutManager = manager
            binding.listProgram.adapter?.notifyDataSetChanged()
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if(sonyControlViewModel.selectedSonyControl.value!= null) sonyControlViewModel.fetchPlayingContentInfo()
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.program_list_menu, menu)

        val searchManager = activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager

        (menu.findItem(R.id.action_search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))
        }

        val searchItem = menu.findItem(R.id.action_search)

        if (searchItem != null) {
            searchView = searchItem.actionView as SearchView
        }

        if (searchView != null) {
            if (sonyControlViewModel.getProgramSearchQuery() != null) {
                searchView?.setQuery(sonyControlViewModel.getProgramSearchQuery(), true)
                searchView?.isIconified = false
                searchView?.clearFocus()
                //searchView.setIconified(false);
            } else {
                // searchView.setIconified(true);
            }
            queryTextListener = object : SearchView.OnQueryTextListener {

                override fun onQueryTextChange(query: String): Boolean {
                    if (query.isNullOrEmpty() || query.length > 1) {
                        Log.i(TAG, "onQueryTextChange: $query")
                        //searchQuery = query
                        //mProgramItemRecyclerViewAdapter.getFilter().filter(query)
                        sonyControlViewModel.filterProgramList(query)
                    }
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    Log.i(TAG, "onQueryTextSubmit: $query")
                    //searchQuery = query
                    //mProgramItemRecyclerViewAdapter.getFilter().filter(query)
                    sonyControlViewModel.filterProgramList(query)
                    searchView?.clearFocus()
                    return false
                }
            }
            searchView?.setOnQueryTextListener(queryTextListener)
        }
        super.onCreateOptionsMenu(menu, menuInflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> return super.onOptionsItemSelected(item)
            R.id.wake_on_lan -> {
                sonyControlViewModel.wakeOnLan()
            }
            R.id.screen_off ->
                sonyControlViewModel.setPowerSavingMode("pictureOff")
            R.id.screen_on ->
                sonyControlViewModel.setPowerSavingMode("off")
        }
        return super.onOptionsItemSelected(item)
    }
}

class ProgramItemRecyclerViewAdapter(
    val clickListener: ProgramListener,
    val sonyControlViewModel: SonyControlViewModel
) :
    RecyclerView.Adapter<ProgramItemRecyclerViewAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return sonyControlViewModel.getFilteredProgramList().value!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val program = sonyControlViewModel.getFilteredProgramList().value!![position]
        holder.bind(program, clickListener, sonyControlViewModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(
            parent
        )
    }

    class ViewHolder private constructor(val binding: ProgramItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: SonyProgram,
            clickListener: ProgramListener,
            sonyControlViewModel: SonyControlViewModel
        ) {
            binding.program = item
            binding.clickListener = clickListener
            binding.sonyControlViewModel = sonyControlViewModel
            binding.executePendingBindings()
            //ToDO: set in layout file (however, it seems than android:onLongClick attribute does not exist)
            binding.root.setOnLongClickListener { clickListener.longClickListener(item) }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ProgramItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(
                    binding
                )
            }
        }
    }

}

class ProgramListener(
    val clickListener: (program: SonyProgram) -> Unit,
    val longClickListener: (program: SonyProgram) -> Boolean
) {
    fun onClick(program: SonyProgram) = clickListener(program)
}