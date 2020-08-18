package org.andan.android.tvbrowser.sonycontrolplugin.ui


import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.FragmentChannelMapBinding
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.MapChannelItemBinding
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyChannel
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 */
class ChannelMapFragment : Fragment() {
    private val sonyControlViewModel: SonyControlViewModel by activityViewModels()
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
        val binding: FragmentChannelMapBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_channel_map, container, false
        )

        val view = binding.root
        sonyControlViewModel.onSelectedIndexChange()
        binding.sonyControlViewModel = sonyControlViewModel
        Timber.d("onCreateView: ${sonyControlViewModel.tvbChannelNameList.size}")
        if (sonyControlViewModel.selectedSonyControl.value == null || sonyControlViewModel.tvbChannelNameList.isNullOrEmpty()) {
            val alertDialogBuilder = AlertDialog.Builder(this.context)
            alertDialogBuilder.setCancelable(false)
            if (sonyControlViewModel.selectedSonyControl.value == null) {
                alertDialogBuilder.setTitle(resources.getString(R.string.alert_no_active_control_title))
                alertDialogBuilder.setMessage(resources.getString(R.string.alert_no_active_control_message))
                Timber.d("No active control")
            } else if (sonyControlViewModel.tvbChannelNameList.isNullOrEmpty()) {
                alertDialogBuilder.setTitle(resources.getString(R.string.alert_no_tvb_channels_title))
                alertDialogBuilder.setMessage(resources.getString(R.string.alert_no_tvb_channels_message))
            }
            alertDialogBuilder.setPositiveButton( resources.getString(R.string.dialog_ok)
            ) { dialog, _ -> dialog.dismiss() }
            alertDialogBuilder.create().show()
        } else {
            val adapter =
                ChannelMapItemRecyclerViewAdapter(
                    ChannelMapListener(
                        { view2: View, channelName: String ->
                            if (sonyControlViewModel.channelNameList.isNullOrEmpty()) {
                                alertNoPrograms()
                            } else {
                                sonyControlViewModel.selectedChannelName = channelName
                                Timber.d("selectedChannelName: $channelName")
                                view2.findNavController()
                                    .navigate(R.id.action_nav_channel_list_to_channelMapSingleFragment)
                            }
                        },
                        { channelName: String ->
                            val uri: String? =
                                sonyControlViewModel.selectedSonyControl.value!!.channelMap[channelName]
                            if (!uri.isNullOrEmpty()) {
                                val program = sonyControlViewModel.uriChannelMap[uri]
                                // switch to program
                                Toast.makeText(
                                    context,
                                    "Switched to ${program?.title}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            true
                        }), sonyControlViewModel
                )

            binding.channelMap.adapter = adapter
            Timber.d("controlViewModel.channelList.size ${sonyControlViewModel.getFilteredTvbChannelNameList().value?.size}")

            sonyControlViewModel.sonyControls.observe(viewLifecycleOwner, Observer {
                Timber.d("observed change getControls")
                adapter.notifyDataSetChanged()
            })

            sonyControlViewModel.getFilteredTvbChannelNameList().observe(viewLifecycleOwner, Observer {
                Timber.d("observed change filtered Channel")
                adapter.notifyDataSetChanged()
            })


            if (view is RecyclerView) {
                view.isNestedScrollingEnabled = true
            }

            val manager = LinearLayoutManager(activity)
            binding.channelMap.layoutManager = manager
            binding.channelMap.adapter?.notifyDataSetChanged()
        }
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.channel_map_menu, menu)

        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager

        (menu.findItem(R.id.action_search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        }

        val searchItem = menu.findItem(R.id.action_search)

        if (searchItem != null) {
            searchView = searchItem.actionView as SearchView
        }
        if (searchView != null) {
            if (sonyControlViewModel.tvbChannelNameSearchQuery.isNullOrEmpty()) {
                searchView?.isIconified = true
            } else {
                searchView?.setQuery(sonyControlViewModel.tvbChannelNameSearchQuery, true)
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
                        Timber.d("onQueryTextChange: $query")
                        //searchQuery = query
                        sonyControlViewModel.filterTvbChannelNameList(query)
                    }
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    Timber.d("onQueryTextSubmit: $query")
                    //searchQuery = query
                    sonyControlViewModel.filterTvbChannelNameList(query)
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
        alertDialogBuilder.setTitle(resources.getString(R.string.alert_no_channels_title))
        alertDialogBuilder.setMessage(resources.getString(R.string.alert_no_channels_message))
        alertDialogBuilder.setPositiveButton(
            resources.getString(R.string.dialog_ok)
        ) { dialog, _ -> dialog.dismiss() }
        alertDialogBuilder.create().show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search ->
                // Not implemented here
                return true
            R.id.match_channels -> {
                if (sonyControlViewModel.channelNameList.isEmpty()) {
                    alertNoPrograms()
                } else {
                    sonyControlViewModel.performFuzzyMatchForChannelList()
                }
                Toast.makeText(
                    context,
                    resources.getString(R.string.toast_channel_map_matched),
                    Toast.LENGTH_SHORT
                ).show()
            }
            R.id.clear_match -> {
                sonyControlViewModel.clearMapping()
                Toast.makeText(
                    context,
                    resources.getString(R.string.toast_channel_map_channel_cleared),
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

class ChannelMapItemRecyclerViewAdapter(val clickListener: ChannelMapListener, val sonyControlViewModel: SonyControlViewModel) :
    RecyclerView.Adapter<ChannelMapItemRecyclerViewAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val channelName = sonyControlViewModel.getFilteredTvbChannelNameList().value?.get(position)!!
        holder.bind(channelName, clickListener, sonyControlViewModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(
            parent
        )
    }

    override fun getItemCount(): Int {
        return sonyControlViewModel.getFilteredTvbChannelNameList().value!!.size
    }

    class ViewHolder private constructor(val binding: MapChannelItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String, clickListener: ChannelMapListener, sonyControlViewModel: SonyControlViewModel) {
            binding.tvbChannelName = item
            binding.channelPosition = adapterPosition+1
            val programUri: String? = sonyControlViewModel.selectedSonyControl.value!!.channelMap[item]
            if (!programUri.isNullOrEmpty()) {
                val channel: SonyChannel? = sonyControlViewModel.selectedSonyControl.value!!.channelUriMap!![programUri]
                binding.channelName = channel?.title
                binding.channelSourceWithType = channel?.sourceWithType
            } else
            {
                binding.channelName="--unmapped--"
                binding.channelSourceWithType = ""
            }

            binding.clickListener = clickListener
            binding.sonyControlViewModel = sonyControlViewModel
            binding.executePendingBindings()
            //ToDO: set in layout file (however, it seems than android:onLongClick attribute does not exist)
            binding.root.setOnLongClickListener { clickListener.longClickListener(item)}
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = MapChannelItemBinding.inflate(layoutInflater, parent, false)
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
