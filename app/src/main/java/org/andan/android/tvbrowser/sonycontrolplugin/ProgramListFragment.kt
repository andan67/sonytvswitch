package org.andan.android.tvbrowser.sonycontrolplugin

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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.FragmentProgramListBinding
import org.andan.av.sony.SonyIPControl
import org.andan.av.sony.model.SonyProgram

/**
 * A simple [Fragment] subclass.
 */
class ProgramListFragment : Fragment() {
    private val TAG = ProgramListFragment::class.java.name
    private lateinit var controlViewModel: ControlViewModel
    private var searchView: SearchView? = null
    private var queryTextListener: SearchView.OnQueryTextListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        //getPlayingContentInfo()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentProgramListBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_program_list, container, false
        )
        val view = binding.root
        val fab: FloatingActionButton = view.findViewById(R.id.listProgramFab)
        fab.setOnClickListener { view ->
            if(controlViewModel.lastProgram != null) {
                // Toast.makeText(context, "Switched to ${controlViewModel.lastProgram?.title}", Toast.LENGTH_LONG).show()
                setPlayContent(controlViewModel.lastProgram!!)
            }

        }

        controlViewModel = ViewModelProviders.of(activity!!).get(ControlViewModel::class.java)
        getPlayingContentInfo()

        if (controlViewModel.getFilteredProgramList().value.isNullOrEmpty()) {
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
        } else {

            binding.controlViewModel = controlViewModel

            binding.activeProgram.activeProgram=controlViewModel.noActiveProgram
            binding.activeProgram.controlViewModel = controlViewModel

            binding.activeProgram.activeProgramView.setOnClickListener {
                //Toast.makeText(context, "Click on ${activeProgram?.title}", Toast.LENGTH_LONG) .show()
                if (controlViewModel.activeContentInfo.value?.title.isNullOrEmpty() || controlViewModel.activeContentInfo.value?.title!!.contentEquals(
                        controlViewModel.noActiveProgram.title
                    )
                ) {
                    Toast.makeText(context, "No current program", Toast.LENGTH_LONG).show()
                } else {
                    view.findNavController()
                        .navigate(R.id.action_nav_program_list_to_activeProgramDetailsFragment)
                }
            }
            binding.activeProgram.activeProgramView.setOnLongClickListener {
                    getPlayingContentInfo()
                    Toast.makeText(context, "Refreshed current program", Toast.LENGTH_LONG).show()
                    true }

            val adapter = ProgramItemRecyclerViewAdapter(
                ProgramListener(
                    { program: SonyProgram ->
                        //Toast.makeText(context, "Switched to ${program.title}", Toast.LENGTH_LONG).show()
                        setPlayContent(program)
                    },
                    { program: SonyProgram ->
                        // Toast.makeText(context, "Long clicked  ${program.title}", Toast.LENGTH_LONG) .show()
                        controlViewModel.onProgramLongClicked(program)
                    }), controlViewModel
            )

            binding.listProgram.adapter = adapter

            controlViewModel.getFilteredProgramList().observe(viewLifecycleOwner, Observer {
                Log.d(TAG, "observed change filtered program list with filter ${controlViewModel.getProgramSearchQuery()}")
                adapter.notifyDataSetChanged()
            })

            controlViewModel.activeContentInfo.observe(viewLifecycleOwner, Observer {
                Log.d(TAG, "observed change activeContentInfo")
                binding.activeProgram.activeProgram = controlViewModel.activeContentInfo.value
                val activeProgramUri = controlViewModel.activeContentInfo.value!!.uri
                if (controlViewModel.uriProgramMap.containsKey(activeProgramUri)) {
                    controlViewModel.updateCurrentProgram(controlViewModel.uriProgramMap[activeProgramUri]!!)
                }
            })

            if (view is RecyclerView) {
                view.isNestedScrollingEnabled = true
            }

            //val manager = LinearLayoutManager(activity,LinearLayoutManager.VERTICAL, false)
            val manager = GridLayoutManager(activity,1)
            binding.listProgram.layoutManager = manager
            binding.listProgram.adapter?.notifyDataSetChanged()
        }
        return binding.root
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
            if (controlViewModel.getProgramSearchQuery() != null) {
                searchView?.setQuery(controlViewModel.getProgramSearchQuery(), true)
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
                        controlViewModel.filterProgramList(query)
                    }
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    Log.i(TAG, "onQueryTextSubmit: $query")
                    //searchQuery = query
                    //mProgramItemRecyclerViewAdapter.getFilter().filter(query)
                    controlViewModel.filterProgramList(query)
                    searchView?.clearFocus()
                    return false
                }
            }
            searchView?.setOnQueryTextListener(queryTextListener)
        }
        super.onCreateOptionsMenu(menu, menuInflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val extras = Bundle()
        when (item.itemId) {
            R.id.action_search -> return super.onOptionsItemSelected(item)
            R.id.wake_on_lan ->
                extras.putInt(SonyIPControlIntentService.ACTION, SonyIPControlIntentService.WOL_ACTION )
            R.id.screen_off ->
                extras.putInt(SonyIPControlIntentService.ACTION, SonyIPControlIntentService.SCREEN_ON_ACTION )
            R.id.screen_on ->
                extras.putInt(SonyIPControlIntentService.ACTION, SonyIPControlIntentService.SCREEN_OFF_ACTION )
        }
        (activity as MainActivity).startControlService(extras)
        return super.onOptionsItemSelected(item)
    }

    private fun getPlayingContentInfo() {
        Log.d(TAG,"getPlayingInfo")
        val extras = Bundle()
        extras.putInt(
            SonyIPControlIntentService.ACTION,
            SonyIPControlIntentService.GET_PLAYING_CONTENT_INFO_ACTION
        )
        (activity as MainActivity).startControlService(extras)
    }

    private fun setPlayContent(program: SonyProgram) {
        val extras = Bundle()
        extras.putInt(
            SonyIPControlIntentService.ACTION,
            SonyIPControlIntentService.SET_AND_GET_PLAY_CONTENT_ACTION
        )
        extras.putString(SonyIPControlIntentService.URI, program.uri)
        (activity as MainActivity).startControlService(extras)
        controlViewModel.updateCurrentProgram(program)
    }

}