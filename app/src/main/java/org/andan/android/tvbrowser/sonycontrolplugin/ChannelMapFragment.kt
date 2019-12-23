package org.andan.android.tvbrowser.sonycontrolplugin


import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
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
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_manage_control.*
import org.andan.android.tvbrowser.sonycontrolplugin.databinding.FragmentChannelListBinding
import org.andan.av.sony.model.SonyProgram
import java.text.DateFormat

/**
 * A simple [Fragment] subclass.
 */
class ChannelMapFragment : Fragment() {
    private val TAG = ChannelMapFragment::class.java.name
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
        val binding: FragmentChannelListBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_channel_list, container, false
        )

        val view = binding.root
        controlViewModel = ViewModelProviders.of(activity!!).get(ControlViewModel::class.java)

        binding.controlViewModel = controlViewModel

        if (controlViewModel.getSelectedControl() == null || controlViewModel.getChannelNameList().isNullOrEmpty()) {
            val alertDialogBuilder = AlertDialog.Builder(this.context)
            alertDialogBuilder.setCancelable(false)
            if (controlViewModel.getSelectedControl() == null) {
                alertDialogBuilder.setTitle(resources.getString(R.string.alert_no_active_control_title))
                alertDialogBuilder.setMessage(resources.getString(R.string.alert_no_active_control_message))
            } else if (controlViewModel.getChannelNameList().isNullOrEmpty()) {
                alertDialogBuilder.setTitle(resources.getString(R.string.alert_no_channels_title))
                alertDialogBuilder.setMessage(resources.getString(R.string.alert_no_channels_message))
            }
            alertDialogBuilder.setPositiveButton( resources.getString(R.string.dialog_ok)
            ) { dialog, arg1 -> dialog.dismiss() }
            alertDialogBuilder.create().show()
        } else {
            val adapter = ChannelMapItemRecyclerViewAdapter(
                ChannelMapListener(
                    { view: View, channelName: String ->
                        if(controlViewModel.programTitleList.isNullOrEmpty()) {
                            alertNoPrograms()
                        } else
                        {
                            controlViewModel.selectedChannelName = channelName
                            view.findNavController()
                                .navigate(R.id.action_nav_channel_list_to_channelMapSingleFragment)
                        }
                    },
                    { channelName: String ->
                        val uri: String?  = (controlViewModel.getSelectedControl())!!.channelProgramUriMap[channelName]
                        if (!uri.isNullOrEmpty()) {
                            val program = controlViewModel.uriProgramMap[uri]
                            val extras = Bundle()
                            extras.putInt(
                                SonyIPControlIntentService.ACTION,
                                SonyIPControlIntentService.SET_AND_GET_PLAY_CONTENT_ACTION
                            )
                            extras.putString(SonyIPControlIntentService.URI, program?.uri)
                            (activity as MainActivity).startControlService(extras)
                            Toast.makeText(
                                context,
                                "Switched to ${program?.title}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        true
                    }), controlViewModel
            )

            binding.listChannelMap.adapter = adapter
            Log.d(
                TAG,
                "controlViewModel.channelList.size ${controlViewModel.getFilteredChannelNameList().value?.size}"
            )

            controlViewModel.getControls().observe(viewLifecycleOwner, Observer {
                Log.d(TAG, "observed change getControls")
                adapter.notifyDataSetChanged()
            })

            controlViewModel.getFilteredChannelNameList().observe(viewLifecycleOwner, Observer {
                Log.d(TAG, "observed change filtered Channel")
                adapter.notifyDataSetChanged()
            })


            if (view is RecyclerView) {
                view.isNestedScrollingEnabled = false
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
            if (controlViewModel.getChannelNameSearchQuery().isNullOrEmpty()) {
                searchView?.isIconified = true
            } else {
                searchView?.setQuery(controlViewModel.getChannelNameSearchQuery(), true)
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
                        controlViewModel.filterChannelNameList(query)
                    }
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    Log.i(TAG, "onQueryTextSubmit: $query")
                    //searchQuery = query
                    controlViewModel.filterChannelNameList(query)
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
                //ToDo:
                // 1. Plugin uses channelMap which maps channel name to either code or index of program list
                // 2. channelMap is stored as shared preference and used by plugin for actual channel switch to eiher code or program
                // via index and uri from SonyProgram
                // 3. this fragment shows list of channel maps whic can be used to specify mapping
                // 4. final result is stored as channelMap in shared preference
                if (controlViewModel.programTitleList.isEmpty()) {
                    alertNoPrograms()
                } else {
                    controlViewModel.performFuzzyMatchForChannelList()
                }
                Toast.makeText(
                    context,
                    resources.getString(R.string.toast_channel_map_matched),
                    Toast.LENGTH_SHORT
                ).show()
            }
            R.id.clear_match -> {
                controlViewModel.clearMapping(true)
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
