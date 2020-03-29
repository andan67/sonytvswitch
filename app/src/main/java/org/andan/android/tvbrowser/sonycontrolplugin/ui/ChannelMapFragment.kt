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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.selects.select
import org.andan.android.tvbrowser.sonycontrolplugin.*
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.FragmentChannelListBinding
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.MapChannnelItemBinding
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyProgram2
import org.andan.android.tvbrowser.sonycontrolplugin.network.SonyIPControlIntentService
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.ControlViewModel
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.TestViewModel
import org.andan.av.sony.model.SonyProgram

/**
 * A simple [Fragment] subclass.
 */
class ChannelMapFragment : Fragment() {
    private val TAG = ChannelMapFragment::class.java.name
    private val testViewModel: TestViewModel by activityViewModels()
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
        val binding: FragmentChannelListBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_channel_list, container, false
        )

        val view = binding.root
        testViewModel.onSelectedIndexChange()
        binding.testViewModel = testViewModel
        Log.d(TAG, "onCreateView: ${testViewModel.channelNameList.size}")
        if (testViewModel.selectedSonyControl.value == null || testViewModel.channelNameList.isNullOrEmpty()) {
            val alertDialogBuilder = AlertDialog.Builder(this.context)
            alertDialogBuilder.setCancelable(false)
            if (testViewModel.selectedSonyControl.value == null) {
                alertDialogBuilder.setTitle(resources.getString(R.string.alert_no_active_control_title))
                alertDialogBuilder.setMessage(resources.getString(R.string.alert_no_active_control_message))
                Log.d(TAG, "No active control")
            } else if (testViewModel.channelNameList.isNullOrEmpty()) {
                alertDialogBuilder.setTitle(resources.getString(R.string.alert_no_channels_title))
                alertDialogBuilder.setMessage(resources.getString(R.string.alert_no_channels_message))
            }
            alertDialogBuilder.setPositiveButton( resources.getString(R.string.dialog_ok)
            ) { dialog, arg1 -> dialog.dismiss() }
            alertDialogBuilder.create().show()
        } else {
            val adapter =
                ChannelMapItemRecyclerViewAdapter(
                    ChannelMapListener(
                        { view: View, channelName: String ->
                            if (testViewModel.programTitleList.isNullOrEmpty()) {
                                alertNoPrograms()
                            } else {
                                testViewModel.selectedChannelName = channelName
                                Log.d(TAG, "selectedChannelName: $channelName")
                                view.findNavController()
                                    .navigate(R.id.action_nav_channel_list_to_channelMapSingleFragment)
                            }
                        },
                        { channelName: String ->
                            val uri: String? =
                                testViewModel.selectedSonyControl.value!!.channelProgramMap[channelName]
                            if (!uri.isNullOrEmpty()) {
                                val program = testViewModel.uriProgramMap[uri]
                                // switch to program
                                Toast.makeText(
                                    context,
                                    "Switched to ${program?.title}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            true
                        }), testViewModel
                )

            binding.listChannelMap.adapter = adapter
            Log.d(
                TAG,
                "controlViewModel.channelList.size ${testViewModel.getFilteredChannelNameList().value?.size}"
            )

            testViewModel.sonyControls.observe(viewLifecycleOwner, Observer {
                Log.d(TAG, "observed change getControls")
                adapter.notifyDataSetChanged()
            })

            testViewModel.getFilteredChannelNameList().observe(viewLifecycleOwner, Observer {
                Log.d(TAG, "observed change filtered Channel")
                adapter.notifyDataSetChanged()
            })


            if (view is RecyclerView) {
                view.isNestedScrollingEnabled = true
            }

            val manager = LinearLayoutManager(activity)
            binding.listChannelMap.layoutManager = manager
            binding.listChannelMap.adapter?.notifyDataSetChanged()
        }
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.channel_map_menu, menu)

        val searchManager = activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager

        (menu.findItem(R.id.action_search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))
        }

        val searchItem = menu.findItem(R.id.action_search)

        if (searchItem != null) {
            searchView = searchItem.actionView as SearchView
        }
        if (searchView != null) {
            if (testViewModel.channelNameSearchQuery.isNullOrEmpty()) {
                searchView?.isIconified = true
            } else {
                searchView?.setQuery(testViewModel.channelNameSearchQuery, true)
                searchView?.isIconified = false
                searchView?.clearFocus()
                // searchView.setIconified(true);
            }
            queryTextListener = object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(query: String): Boolean {
                    // Using this listener results in incorrectly displayed adapter positions.
                    // Cause?: Frequency of changes vs reflection of changes in adapter?
                    //if(query.isNullOrEmpty() || query.length > 1) {
                    if (query.isNullOrEmpty()) {
                        Log.i(TAG, "onQueryTextChange: $query")
                        //searchQuery = query
                        testViewModel.filterChannelNameList(query)
                    }
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    Log.i(TAG, "onQueryTextSubmit: $query")
                    //searchQuery = query
                    testViewModel.filterChannelNameList(query)
                    searchView?.clearFocus()
                    return false
                }
            }
            searchView?.setOnQueryTextListener(queryTextListener)
        }
        super.onCreateOptionsMenu(menu, menuInflater)
    }

    private fun alertNoPrograms() {
        val alertDialogBuilder = AlertDialog.Builder(this.context)
        alertDialogBuilder.setTitle(resources.getString(R.string.alert_no_programs_title))
        alertDialogBuilder.setMessage(resources.getString(R.string.alert_no_programs_message))
        alertDialogBuilder.setPositiveButton(
            resources.getString(R.string.dialog_ok)
        ) { dialog, arg1 -> dialog.dismiss() }
        alertDialogBuilder.create().show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search ->
                // Not implemented here
                return true
            R.id.match_channels -> {
                if (testViewModel.programTitleList.isEmpty()) {
                    alertNoPrograms()
                } else {
                    testViewModel.performFuzzyMatchForChannelList()
                }
                Toast.makeText(
                    context,
                    resources.getString(R.string.toast_channel_map_matched),
                    Toast.LENGTH_SHORT
                ).show()
            }
            R.id.clear_match -> {
                //testViewModel.clearMapping(true)
                Toast.makeText(
                    context,
                    resources.getString(R.string.toast_channel_map_program_cleared),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
            }
        }
        searchView?.setOnQueryTextListener(queryTextListener)
        return super.onOptionsItemSelected(item)
    }
}

class ChannelMapItemRecyclerViewAdapter(val clickListener: ChannelMapListener, val testViewModel: TestViewModel) :
    RecyclerView.Adapter<ChannelMapItemRecyclerViewAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val channelName = testViewModel.getFilteredChannelNameList().value?.get(position)!!
        holder.bind(channelName, clickListener, testViewModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(
            parent
        )
    }

    override fun getItemCount(): Int {
        return testViewModel.getFilteredChannelNameList().value!!.size
    }

    class ViewHolder private constructor(val binding: MapChannnelItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String, clickListener: ChannelMapListener, testViewModel: TestViewModel) {
            binding.channelName = item
            binding.channelPosition = adapterPosition+1
            val programUri: String? = testViewModel.selectedSonyControl.value!!.channelProgramMap[item]
            if (!programUri.isNullOrEmpty()) {
                val program: SonyProgram2? = testViewModel.selectedSonyControl.value!!.programUriMap!![programUri]
                binding.programTitle = program?.title
                binding.programSourceWithType = program?.sourceWithType
            } else
            {
                binding.programTitle="--unmapped--"
                binding.programSourceWithType = ""
            }

            binding.clickListener = clickListener
            binding.testViewModel = testViewModel
            binding.executePendingBindings()
            //ToDO: set in layout file (however, it seems than android:onLongClick attribute does not exist)
            binding.root.setOnLongClickListener { clickListener.longClickListener(item)}
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = MapChannnelItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(
                    binding
                )
            }
        }
    }
}

class ChannelMapListener(val clickListener: (view: View, channelName: String) -> Unit,
                         val longClickListener: (channelName: String) -> Boolean) {
    fun onClick(view: View, channelName: String) = clickListener(view, channelName)
    fun onLongClick(channelName: String) = longClickListener(channelName)
}
