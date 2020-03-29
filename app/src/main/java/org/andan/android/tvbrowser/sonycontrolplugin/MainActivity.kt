package org.andan.android.tvbrowser.sonycontrolplugin

import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.TestViewModel

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.name
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var controlListAdapter: ArrayAdapter<SonyControl>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val selectActiveControlSpinner = navView.getHeaderView(0).findViewById<Spinner>(R.id.channelMapSelectControlSpinner)
        val navController = findNavController(R.id.nav_host_fragment)

        val testViewModel: TestViewModel by viewModels()
        Log.d(TAG, "testViewModel.sonyControls.value.selected ${testViewModel.sonyControls.value}")

        if (!testViewModel.isCreated) {
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
                R.id.nav_settings,
                R.id.nav_test
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        controlListAdapter =
            ArrayAdapter(this, R.layout.control_spinner_item, testViewModel.sonyControls.value!!.controls)

        selectActiveControlSpinner.adapter = controlListAdapter
        selectActiveControlSpinner.setSelection(testViewModel.sonyControls.value!!.selected)

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
                if (testViewModel.sonyControls.value!!.selected != position) {
                    testViewModel.setSelectedControlIndex(position)
                    Log.d(TAG, "onItemSelected setSelectedControlIndex")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Log.d(TAG, "onNothingSelected")
            }
        }

        testViewModel.selectedSonyControl.observe(this, Observer {
            controlListAdapter.notifyDataSetChanged()
        })

        // get channel list from preferences
        val startedFromTVBrowser = intent.getBooleanExtra("startedFromTVBrowser", false)
        if (startedFromTVBrowser) run {
            Log.i(TAG, "onCreate: startedFromTVBrowser=$startedFromTVBrowser")
            //controlViewModel.setChannelNameListFromPreference()
            navController.navigate(R.id.nav_channel_list)
        }
        testViewModel.isCreated = true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}
