package org.andan.android.tvbrowser.sonycontrolplugin


import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.FragmentChannelSingleBinding
import org.andan.av.sony.SonyIPControl
import org.andan.av.sony.model.SonyProgram
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class ChannelMapSingleFragment : Fragment() {
    private val TAG = ChannelMapSingleFragment::class.java.name
    private lateinit var controlViewModel: ControlViewModel
    private var searchView: SearchView? = null
    private var queryTextListener: SearchView.OnQueryTextListener? = null
    private var searchQuery: String? = null
    var currentProgramPosition: Int = -1
    private var programPosition: Int = -1
    private var initialProgramUri: String? = ""
    lateinit var binding: FragmentChannelSingleBinding
    lateinit var arrayAdapter: ChannelMapProgramListAdapter
    private var selectedChannelName: String? = null
    private var programUriMatchList: ArrayList<String> = ArrayList()
    var control: SonyIPControl? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get a reference to the binding object and inflate the fragment views.
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_channel_single, container, false)

        val view = binding.root
        controlViewModel = ViewModelProviders.of(activity!!).get(ControlViewModel::class.java)

        selectedChannelName =  controlViewModel.selectedChannelName
        val channelPosition = controlViewModel.getFilteredChannelNameList().value?.indexOfFirst { it==selectedChannelName }
        binding.channelPosition = channelPosition!! +1

        binding.channelName = selectedChannelName
        control=controlViewModel.getSelectedControl()

        initialProgramUri = control!!.channelProgramUriMap[selectedChannelName]
        Log.d(TAG,"initialProgramUri : $initialProgramUri")
        controlViewModel.setSelectedChannelMapProgramUri(binding.channelName , initialProgramUri)

        controlViewModel.selectedChannelMapProgramUri.observe(viewLifecycleOwner, Observer {
            val selectedProgramUri = controlViewModel.selectedChannelMapProgramUri.value
            if (!selectedProgramUri.isNullOrEmpty()) {
                val program: SonyProgram? = controlViewModel.getSelectedControl()!!.programUriMap[selectedProgramUri]
                binding.programTitle = program?.title
                binding.programSourceWithType = program?.sourceWithType
            } else
            {
                binding.programTitle="--unmapped--"
                binding.programSourceWithType = ""
            }
        })

        createMatchIndicesListAndSetPositions(null)
        arrayAdapter = ChannelMapProgramListAdapter(context, programUriMatchList, controlViewModel.getSelectedControl()!!.programUriMap)
        binding.channelMapProgramListView.setSelector(R.drawable.list_selector)
        binding.channelMapProgramListView.adapter = arrayAdapter
        binding.channelMapProgramListView.setSelection(currentProgramPosition)
        binding.channelMapProgramListView.setItemChecked(currentProgramPosition, true)
        arrayAdapter.notifyDataSetChanged()

        binding.channelMapProgramListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            binding.channelMapProgramListView.setSelection(position)
            //binding.channelMapProgramListView.setItemChecked(position,true)
            currentProgramPosition = position
            val selectedChannelMapProgramUri = programUriMatchList[currentProgramPosition]
            controlViewModel.setSelectedChannelMapProgramUri(binding.channelName, selectedChannelMapProgramUri)
            // Toast.makeText( context, "Clicked item #${position}",  Toast.LENGTH_SHORT).show()
        }
        binding.channelMapProgramListView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            //binding.channelMapProgramListView.setItemChecked(position,true)
            currentProgramPosition = position
            val program = controlViewModel.uriProgramMap[programUriMatchList[currentProgramPosition]]
            val extras = Bundle()
            extras.putInt(
                SonyIPControlIntentService.ACTION,
                SonyIPControlIntentService.SET_AND_GET_PLAY_CONTENT_ACTION
            )
            extras.putString(SonyIPControlIntentService.URI, program?.uri)
            (activity as MainActivity).startControlService(extras)
            Toast.makeText(context, "Switched to ${program?.title}", Toast.LENGTH_LONG).show()
            true
        }

        Log.d(TAG,"currentProgramPosition=${currentProgramPosition}")
        Log.d(TAG,"adapter.count=${arrayAdapter.count}")

        controlViewModel.getControls().observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "observed change getControls")

        })

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.channel_map_single_menu, menu)

        val searchManager = activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager

        (menu.findItem(R.id.action_search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))
        }

        val searchItem = menu.findItem(R.id.action_search)

        if (searchItem != null) {
            searchView = searchItem.actionView as SearchView
            Log.d(TAG, "onCreateOptionsMen:$searchQuery")
        }
        if (searchView != null) {
            if (searchQuery != null) {
                searchView?.setQuery(searchQuery, true)
                searchView?.isIconified = false
                searchView?.clearFocus()
                //searchView.setIconified(false);
            } else {
                // searchView.setIconified(true);
            }
            queryTextListener = object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(query: String): Boolean {
                    if(query.isNullOrEmpty() || query.length>1) {
                        Log.i(TAG, "onQueryTextChange: $query")
                        createMatchIndicesListAndSetPositions(query)
                        binding.channelMapProgramListView.setSelection(currentProgramPosition)
                        binding.channelMapProgramListView.setItemChecked(
                            currentProgramPosition,
                            true
                        )
                        arrayAdapter.notifyDataSetChanged()
                    }
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    Log.i(TAG,"onQueryTextSubmit: $query")
                    createMatchIndicesListAndSetPositions(query)
                    binding.channelMapProgramListView.setSelection(currentProgramPosition)
                    binding.channelMapProgramListView.setItemChecked(currentProgramPosition, true)
                    arrayAdapter.notifyDataSetChanged()
                    searchView?.clearFocus()
                    return false
                }
            }
            searchView?.setOnQueryTextListener(queryTextListener)
        }
        super.onCreateOptionsMenu(menu, menuInflater)
    }

    fun createMatchIndicesListAndSetPositions(query: String?) {
        programUriMatchList.clear()
        programUriMatchList.addAll(controlViewModel.createProgramUriMatchList(selectedChannelName, query))
        programPosition = programUriMatchList.indexOfFirst { it==initialProgramUri }
        currentProgramPosition = if (!initialProgramUri.isNullOrEmpty()) programPosition else -1
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search ->
                // Not implemented here
                return true
            R.id.channel_map_reset -> {
                controlViewModel.setSelectedChannelMapProgramUri(binding.channelName, initialProgramUri)
                currentProgramPosition = programPosition
                binding.channelMapProgramListView.setItemChecked(currentProgramPosition, true)
            }
            R.id.channel_map_unmap -> {
                controlViewModel.setSelectedChannelMapProgramUri( binding.channelName, "")
                Toast.makeText( context, "Selected item with currentProgramPosition: $currentProgramPosition",  Toast.LENGTH_SHORT).show()
                binding.channelMapProgramListView.setItemChecked(currentProgramPosition, false)
                binding.channelMapProgramListView.clearChoices()

            }
        }
        searchView?.setOnQueryTextListener(queryTextListener)
        return super.onOptionsItemSelected(item)
    }
}
