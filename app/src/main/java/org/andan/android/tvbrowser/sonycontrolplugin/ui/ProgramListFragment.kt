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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.andan.android.tvbrowser.sonycontrolplugin.MainActivity
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.network.SonyIPControlIntentService
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.FragmentProgramListBinding
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.ProgramItemBinding
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyProgram2
import org.andan.android.tvbrowser.sonycontrolplugin.network.PlayingContentInfoResponse
import org.andan.android.tvbrowser.sonycontrolplugin.network.Status
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.ControlViewModel
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.TestViewModel
import org.andan.av.sony.model.SonyProgram

/**
 * A simple [Fragment] subclass.
 */
class ProgramListFragment : Fragment() {
    private val TAG = ProgramListFragment::class.java.name
    //private lateinit var controlViewModel: ControlViewModel
    private lateinit var testViewModel: TestViewModel
    private var searchView: SearchView? = null
    private var queryTextListener: SearchView.OnQueryTextListener? = null
    private lateinit var playingContentInfo: PlayingContentInfoResponse
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
        val fab: FloatingActionButton = view.findViewById(R.id.listProgramFab)
        fab.setOnClickListener { view ->
            if(testViewModel.lastProgram != null) {
                // Toast.makeText(context, "Switched to ${controlViewModel.lastProgram?.title}", Toast.LENGTH_LONG).show()
                setPlayContent(testViewModel.lastProgram!!)
            }

        }
        testViewModel = ViewModelProvider(this).get(TestViewModel::class.java)
        testViewModel.onSelectedIndexChange()

        if (testViewModel.getFilteredProgramList().value.isNullOrEmpty()) {
            val alertDialogBuilder = AlertDialog.Builder(this.context)
            alertDialogBuilder.setCancelable(false)
            if (testViewModel.getSelectedControl() == null) {
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
            playingContentInfo = testViewModel.noPlayingContentInfo
            if(testViewModel.playingContentInfo.value != null && testViewModel.playingContentInfo.value!!.status == Status.SUCCESS ) {
                testViewModel.playingContentInfo.value!!.data!!
            }

            binding.testViewModel = testViewModel
            binding.activeProgram.activeProgram = playingContentInfo
            binding.activeProgram.testViewModel = testViewModel

            binding.activeProgram.activeProgramView.setOnClickListener {
                //Toast.makeText(context, "Click on ${activeProgram?.title}", Toast.LENGTH_LONG) .show()
                if (playingContentInfo == testViewModel.noPlayingContentInfo)
                {
                    Toast.makeText(context, "No current program", Toast.LENGTH_LONG).show()
                } else {
                    view.findNavController()
                        .navigate(R.id.action_nav_program_list_to_activeProgramDetailsFragment)
                }
            }
            binding.activeProgram.activeProgramView.setOnLongClickListener {
                    fetchPlayingContentInfo()
                    Toast.makeText(context, "Refreshed current program", Toast.LENGTH_LONG).show()
                    true }

            val adapter =
                ProgramItemRecyclerViewAdapter(
                    ProgramListener(
                        { program: SonyProgram2 ->
                            //Toast.makeText(context, "Switched to ${program.title}", Toast.LENGTH_LONG).show()
                            setPlayContent(program)
                        },
                        { program: SonyProgram2 -> true
                            // Toast.makeText(context, "Long clicked  ${program.title}", Toast.LENGTH_LONG) .show()
                            //testViewModel.onProgramLongClicked(program)
                        }), testViewModel
                )

            binding.listProgram.adapter = adapter

            testViewModel.getFilteredProgramList().observe(viewLifecycleOwner, Observer {
                Log.d(TAG, "observed change filtered program list with filter ${testViewModel.getProgramSearchQuery()}")
                adapter.notifyDataSetChanged()
                fetchPlayingContentInfo()
            })

            testViewModel.requestErrorMessage.observe(viewLifecycleOwner, Observer {
                Log.d(TAG, "observed requestError")
                Toast.makeText(context, "$it.value", Toast.LENGTH_LONG).show()
            })

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
            if (testViewModel.getProgramSearchQuery() != null) {
                searchView?.setQuery(testViewModel.getProgramSearchQuery(), true)
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
                        testViewModel.filterProgramList(query)
                    }
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    Log.i(TAG, "onQueryTextSubmit: $query")
                    //searchQuery = query
                    //mProgramItemRecyclerViewAdapter.getFilter().filter(query)
                    testViewModel.filterProgramList(query)
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
        (activity as MainActivity).startControlService(extras)
        return super.onOptionsItemSelected(item)
    }

    private fun fetchPlayingContentInfo() {
        Log.d(TAG,"getPlayingInfo")
        /*val extras = Bundle()
        extras.putInt(
            SonyIPControlIntentService.ACTION,
            SonyIPControlIntentService.GET_PLAYING_CONTENT_INFO_ACTION
        )
        (activity as MainActivity).startControlService(extras)*/
        testViewModel.fetchPlayingContentInfo()
    }

    private fun setPlayContent(program: SonyProgram2) {
        /*val extras = Bundle()
        extras.putInt(
            SonyIPControlIntentService.ACTION,
            SonyIPControlIntentService.SET_AND_GET_PLAY_CONTENT_ACTION
        )
        extras.putString(SonyIPControlIntentService.URI, program.uri)
        (activity as MainActivity).startControlService(extras)*/
        testViewModel.setPlayContent(program.uri)
        testViewModel.updateCurrentProgram(program)
    }
}

class ProgramItemRecyclerViewAdapter(val clickListener: ProgramListener, val testViewModel: TestViewModel) :
    RecyclerView.Adapter<ProgramItemRecyclerViewAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return testViewModel.getFilteredProgramList().value!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val program = testViewModel.getFilteredProgramList().value!![position]
        holder.bind(program, clickListener,testViewModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(
            parent
        )
    }

    class ViewHolder private constructor(val binding: ProgramItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SonyProgram2, clickListener: ProgramListener, testViewModel: TestViewModel) {
            binding.program = item
            binding.clickListener = clickListener
            binding.testViewModel = testViewModel
            binding.executePendingBindings()
            //ToDO: set in layout file (however, it seems than android:onLongClick attribute does not exist)
            binding.root.setOnLongClickListener { clickListener.longClickListener(item)}
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

class ProgramListener(val clickListener: (program: SonyProgram2) -> Unit,
                      val longClickListener: (program: SonyProgram2) -> Boolean) {
    fun onClick(program: SonyProgram2) = clickListener(program)
}