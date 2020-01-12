package org.andan.android.tvbrowser.sonycontrolplugin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import org.andan.av.sony.SonyIPControl
import org.andan.av.sony.model.SonyPlayingContentInfo
import org.tvbrowser.devplugin.Channel
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.name
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var controlViewModel: ControlViewModel
    private lateinit var controlListAdapter: ArrayAdapter<SonyIPControl>
    private lateinit var mSonyIPControlReceiver: SonyIPControlReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        controlViewModel =
            ViewModelProviders.of(this).get(ControlViewModel::class.java)
        Log.d(
            TAG,
            "onCreate: controlViewModel.getControls().value.size=${controlViewModel.getControls().value!!.size}"
        )
        Log.d(
            TAG,
            "onCreate: controlViewModel.getSelectedControlIndex()=${controlViewModel.getSelectedControlIndex()}"
        )
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val selectActiveControlSpinner = navView.getHeaderView(0).findViewById<Spinner>(R.id.channelMapSelectControlSpinner)
        val navController = findNavController(R.id.nav_host_fragment)
        if (!controlViewModel.isCreated) {
            val graph = navController.navInflater.inflate(R.navigation.navigation)
            when (PreferenceManager.getDefaultSharedPreferences(this).getString(
                "pref_start_screen",
                "program_list_screen"
            )) {
                "program_list_screen" -> graph.startDestination = R.id.nav_program_list
                else -> graph.startDestination = R.id.nav_remote_control
            }
            navController.graph = graph
        }
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_add_control,
                R.id.nav_manage_control,
                R.id.nav_remote_control,
                R.id.nav_program_list,
                R.id.nav_channel_list,
                R.id.nav_help,
                R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        controlListAdapter =
            ArrayAdapter(this, R.layout.control_spinner_item, controlViewModel.getControls().value)

        selectActiveControlSpinner.adapter = controlListAdapter
        selectActiveControlSpinner.setSelection(controlViewModel.getSelectedControlIndex())

        selectActiveControlSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                Log.i(TAG, "onItemSelected position:$position")
                // check if new position/control index is set
                if (controlViewModel.getSelectedControlIndex() != position) {
                    controlViewModel.setSelectedControlIndex(position)
                    Log.d(TAG, "onItemSelected setSelectedControlIndex")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Log.d(TAG, "onNothingSelected")
            }
        }

        controlViewModel.getSelectedControlIndexLiveData().observe(this, Observer {
            Log.d(TAG, "observed change getControls")
            Log.d(
                TAG,
                "observed: controlViewModel.getControls().value.size=${controlViewModel.getControls().value!!.size}"
            )
            Log.d(
                TAG,
                "observed: controlViewModel.getSelectedControlIndex()=${controlViewModel.getSelectedControlIndex()}"
            )
            // renew cookie if required
            val extras = Bundle()
            extras.putInt(SonyIPControlIntentService.ACTION, SonyIPControlIntentService.RENEW_COOKIE_ACTION )
            startControlService(extras)
            //selectActiveControlSpinner.setSelection(controlViewModel.getSelectedControlIndex())
            controlListAdapter.notifyDataSetChanged()
            //navController.navigate(R.id.nav_manage_control)
        })

        Log.i(TAG, "onCreate:channel list size=" + controlViewModel.getChannelNameList()?.size)
        // get channel list from preferences
        val channelList: ArrayList<Channel>? = intent.getParcelableArrayListExtra("channelList")
        if (channelList != null) run {
            Log.i(TAG, "onCreate:channelList from intent!=null")
            controlViewModel.setChannelNameListFromPreference()
            navController.navigate(R.id.nav_channel_list)
        }
        controlViewModel.isCreated = true
    }

    override fun onStart() {
        super.onStart()
        val statusIntentFilter = IntentFilter(SonyIPControlIntentService.ACTION)

        mSonyIPControlReceiver = SonyIPControlReceiver()
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mSonyIPControlReceiver, statusIntentFilter)
    }

    override fun onStop() {
        super.onStop()
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mSonyIPControlReceiver)

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent()")
        controlViewModel.setChannelNameListFromPreference()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun startControlService(extras: Bundle) {
        if(controlViewModel.getSelectedControlIndex() > -1) {
            val intentService =
                Intent(this, SonyIPControlIntentService::class.java)
            intentService.putExtra(SonyIPControlIntentService.CONTROL, controlViewModel.getSelectedControlAsJson())
            intentService.putExtras(extras)
            startService(intentService)
        }
    }

    // Broadcast receiver for receiving status updates from the IntentService
    inner class SonyIPControlReceiver : BroadcastReceiver() {
        private val navController = findNavController(R.id.nav_host_fragment)
        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == SonyIPControlIntentService.ACTION) {
                val data = intent.extras
                val action = data!!.getInt(SonyIPControlIntentService.ACTION)
                val ipControlJSON = data.getString(SonyIPControlIntentService.CONTROL)
                val resultCode = data.getInt(SonyIPControlIntentService.RESULT_CODE)
                val resultMessage = data.getString(SonyIPControlIntentService.RESULT_MESSAGE)
                Log.i(TAG, "SonyIPControlReceiver.onReceive:$resultCode")

                when (resultCode) {
                    SonyIPControlIntentService.RESULT_AUTH_REQUIRED -> {
                        Log.i(TAG, "onReceive:AUTH_REQUIRED" + ipControlJSON!!)
                        // navigate to dialog for entering challenge code
                        navController.navigate(R.id.nav_enter_challenge)
                    }
                    SonyIPControlIntentService.RESULT_OK -> {
                        // refresh view
                        when (action) {
                            SonyIPControlIntentService.SET_PROGRAM_LIST_ACTION -> {
                                val control = SonyIPControl(ipControlJSON)
                                controlViewModel.setSelectedControl(control)
                                Toast.makeText(
                                    context,
                                    resources.getString(R.string.toast_program_list_received),
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate(R.id.nav_manage_control)
                            }
                            SonyIPControlIntentService.REGISTER_CONTROL_ACTION -> {
                                val control = SonyIPControl(ipControlJSON)
                                controlViewModel.setSelectedControl(control)
                                Toast.makeText(
                                    context,
                                    resources.getString(R.string.toast_control_registered),
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate(R.id.nav_manage_control)
                            }
                            SonyIPControlIntentService.RENEW_COOKIE_ACTION -> {
                                if(!resultMessage.isNullOrEmpty())
                                {
                                    val control = SonyIPControl(ipControlJSON)
                                    controlViewModel.setSelectedControl(control)
                                }
                            }
                            SonyIPControlIntentService.ENABLE_WOL_ACTION -> {
                                val control = SonyIPControl(ipControlJSON)
                                controlViewModel.setSelectedControl(control)
                                navController.navigate(R.id.nav_manage_control)
                            }
                            SonyIPControlIntentService.SET_PLAY_CONTENT_ACTION -> {
                            }
                            SonyIPControlIntentService.GET_PLAYING_CONTENT_INFO_ACTION -> {
                                val contentInfo =
                                    data.getString(SonyIPControlIntentService.PLAYING_CONTENT_INFO)
                                Log.d(TAG, "contentInfo: $contentInfo")
                                controlViewModel.activeContentInfo.value =
                                    if (contentInfo.isNullOrEmpty() || contentInfo=="null") {
                                        controlViewModel.noActiveProgram
                                    } else {
                                            SonyIPControl.getGson().fromJson(
                                                contentInfo,
                                                SonyPlayingContentInfo::class.java)
                                        }
                                    }
                        }
                    }
                    else -> Toast.makeText(
                        this@MainActivity,
                        "Request failed with error: $resultMessage!",
                        Toast.LENGTH_LONG).show()
                }

            }
        }
    }

}
