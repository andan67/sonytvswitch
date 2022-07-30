package org.andan.android.tvbrowser.sonycontrolplugin.ui


import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.FragmentChannelMapSingleBinding
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyChannel
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel
import timber.log.Timber
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class ChannelMapSingleFragment : Fragment() {
    private val sonyControlViewModel: SonyControlViewModel by activityViewModels()
    private var searchView: SearchView? = null
    private var queryTextListener: SearchView.OnQueryTextListener? = null
    private var searchQuery: String? = null
    var currentChannelPosition: Int = -1
    private var channelPosition: Int = -1
    private var initialChannelUri: String? = ""
    lateinit var binding: FragmentChannelMapSingleBinding
    lateinit var arrayAdapter: ChannelMapAdapter
    private var selectedChannelName: String? = null
    private var channelUriMatchList: ArrayList<String> = ArrayList()
    var control: SonyControl? = null

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
            inflater,
            R.layout.fragment_channel_map_single, container, false
        )

        selectedChannelName = sonyControlViewModel.selectedChannelName
        Timber.d("selectedChannelName: $selectedChannelName")
        val channelPosition =
            sonyControlViewModel.getFilteredTvbChannelNameList().value?.indexOfFirst { it == selectedChannelName }
        binding.channelPosition = channelPosition!! + 1

        binding.tvbChannelName = selectedChannelName
        control = sonyControlViewModel.selectedSonyControl.value!!

        initialChannelUri = control!!.channelMap[selectedChannelName!!]
        Timber.d("initialProgramUri : $initialChannelUri")
        sonyControlViewModel.setSelectedChannelMapChannelUri(
            binding.tvbChannelName,
            initialChannelUri
        )

        sonyControlViewModel.selectedChannelMapChannelUri.observe(viewLifecycleOwner, Observer {
            val selectedProgramUri = sonyControlViewModel.selectedChannelMapChannelUri.value
            if (!selectedProgramUri.isNullOrEmpty()) {
                val channel: SonyChannel? =
                    sonyControlViewModel.selectedSonyControl.value!!.channelUriMap!![selectedProgramUri]
                binding.channelName = channel?.title
                binding.channelSourceWithType = channel?.sourceWithType
            } else {
                binding.channelName = "--unmapped--"
                binding.channelSourceWithType = ""
            }
        })

        createMatchIndicesListAndSetPositions(null)
        arrayAdapter =
            ChannelMapAdapter(
                context,
                channelUriMatchList,
                sonyControlViewModel.selectedSonyControl.value!!.channelUriMap!!
            )
        binding.channelMapChannelListView.setSelector(R.drawable.list_selector)
        binding.channelMapChannelListView.adapter = arrayAdapter
        binding.channelMapChannelListView.setSelection(currentChannelPosition)
        binding.channelMapChannelListView.setItemChecked(currentChannelPosition, true)
        arrayAdapter.notifyDataSetChanged()

        binding.channelMapChannelListView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                binding.channelMapChannelListView.setSelection(position)
                //binding.channelMapProgramListView.setItemChecked(position,true)
                currentChannelPosition = position
                val selectedChannelMapChannelUri = channelUriMatchList[currentChannelPosition]
                sonyControlViewModel.setSelectedChannelMapChannelUri(
                    binding.tvbChannelName,
                    selectedChannelMapChannelUri
                )
                // Toast.makeText( context, "Clicked item #${position}",  Toast.LENGTH_SHORT).show()
            }
        binding.channelMapChannelListView.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, _, position, _ ->
                //binding.channelMapProgramListView.setItemChecked(position,true)
                currentChannelPosition = position
                val program =
                    sonyControlViewModel.uriChannelMap[channelUriMatchList[currentChannelPosition]]
                // switch to program
                Toast.makeText(context, "Switched to ${program?.title}", Toast.LENGTH_LONG).show()
                true
            }

        Timber.d("currentProgramPosition=${currentChannelPosition}")
        Timber.d("adapter.count=${arrayAdapter.count}")

        sonyControlViewModel.sonyControls.observe(viewLifecycleOwner, Observer {
            Timber.d("observed change getControls")

        })

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.channel_map_single_menu, menu)

        val searchManager =
            requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager

        (menu.findItem(R.id.action_search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        }

        val searchItem = menu.findItem(R.id.action_search)

        if (searchItem != null) {
            searchView = searchItem.actionView as SearchView
            Timber.d("onCreateOptionsMen:$searchQuery")
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
                    if (query.isNullOrEmpty() || query.length > 1) {
                        Timber.d("onQueryTextChange: $query")
                        createMatchIndicesListAndSetPositions(query)
                        binding.channelMapChannelListView.setSelection(currentChannelPosition)
                        binding.channelMapChannelListView.setItemChecked(
                            currentChannelPosition,
                            true
                        )
                        arrayAdapter.notifyDataSetChanged()
                    }
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    Timber.d("onQueryTextSubmit: $query")
                    createMatchIndicesListAndSetPositions(query)
                    binding.channelMapChannelListView.setSelection(currentChannelPosition)
                    binding.channelMapChannelListView.setItemChecked(currentChannelPosition, true)
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
        channelUriMatchList.clear()
        channelUriMatchList.addAll(
            sonyControlViewModel.createChannelUriMatchList(
                selectedChannelName,
                query
            )
        )
        channelPosition = channelUriMatchList.indexOfFirst { it == initialChannelUri }
        currentChannelPosition = if (!initialChannelUri.isNullOrEmpty()) channelPosition else -1
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search ->
                // Not implemented here
                return true
            R.id.channel_map_reset -> {
                sonyControlViewModel.setSelectedChannelMapChannelUri(
                    binding.channelName,
                    initialChannelUri
                )
                currentChannelPosition = channelPosition
                binding.channelMapChannelListView.setItemChecked(currentChannelPosition, true)
            }
            R.id.channel_map_unmap -> {
                sonyControlViewModel.setSelectedChannelMapChannelUri(binding.channelName, "")
                Toast.makeText(
                    context,
                    "Selected item with currentProgramPosition: $currentChannelPosition",
                    Toast.LENGTH_SHORT
                ).show()
                binding.channelMapChannelListView.setItemChecked(currentChannelPosition, false)
                binding.channelMapChannelListView.clearChoices()

            }
        }
        searchView?.setOnQueryTextListener(queryTextListener)
        return super.onOptionsItemSelected(item)
    }
}

class ChannelMapAdapter(
    context: Context?,
    programUriMatchList: ArrayList<String>,
    channelUriMap: MutableMap<String, SonyChannel>
) :
    BaseAdapter() {


    private var ctx: Context? = null
    private var channelUriMap: MutableMap<String, SonyChannel>? = null
    private var programUriMatchList: ArrayList<String>? = null

    private var mInflater: LayoutInflater? = null

    init {
        this.ctx = context
        mInflater = (ctx as Activity).layoutInflater
        this.channelUriMap = channelUriMap
        this.programUriMatchList = programUriMatchList

    }

    override fun getCount(): Int {
        return programUriMatchList!!.size
    }

    override fun getItem(arg0: Int): Any? {
        return null
    }


    override fun getItemId(arg0: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, arg2: ViewGroup): View {
        var convertView = convertView

        val channel: SonyChannel? = channelUriMap!![programUriMatchList!![position]]

        val holder: ViewHolder

        if (convertView == null) {
            holder =
                ViewHolder()
            convertView = mInflater!!.inflate(R.layout.map_channnel_single_item, null)
            holder.programPosView =
                convertView!!.findViewById<View>(R.id.channel_map_channel_pos) as TextView
            holder.programNameView =
                convertView.findViewById<View>(R.id.channel_map_channel_name) as TextView
            holder.programSourceView =
                convertView.findViewById<View>(R.id.channel_map_channel_source) as TextView
            convertView.tag = holder

        } else {
            holder = convertView.tag as ViewHolder
        }

        holder.programPosView!!.text = (position + 1).toString() + "."
        holder.programNameView!!.text = channel?.title
        holder.programSourceView!!.text = channel?.sourceWithType

        return convertView

    }

    private class ViewHolder {
        internal var programPosView: TextView? = null
        internal var programNameView: TextView? = null
        internal var programSourceView: TextView? = null
    }

}
