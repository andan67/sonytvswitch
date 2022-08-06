package org.andan.android.tvbrowser.sonycontrolplugin

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.ComponentActivity
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
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var controlListAdapter: ArrayAdapter<SonyControl>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val selectActiveControlSpinner =
            navView.getHeaderView(0).findViewById<Spinner>(R.id.channelMapSelectControlSpinner)
        val navController = findNavController(R.id.nav_host_fragment)

        val sonyControlViewModel: SonyControlViewModel by viewModels()

        if (!sonyControlViewModel.isCreated) {
            val graph = navController.navInflater.inflate(R.navigation.navigation)
            when (PreferenceManager.getDefaultSharedPreferences(this).getString(
                "pref_start_screen",
                "program_list_screen"
            )) {
                "program_list_screen" -> graph.setStartDestination(R.id.nav_channel_list)
                else -> graph.setStartDestination(R.id.nav_remote_control)
            }
            navController.graph = graph
        }
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                //R.id.nav_add_control,
                R.id.nav_add_control_host,
                R.id.nav_manage_control,
                R.id.nav_remote_control,
                R.id.nav_channel_list,
                R.id.nav_channel_list2,
                R.id.nav_channel_map,
                R.id.nav_help,
                R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        controlListAdapter =
            ArrayAdapter(
                this,
                R.layout.control_spinner_item,
                sonyControlViewModel.sonyControls.value!!.controls
            )

        selectActiveControlSpinner.adapter = controlListAdapter
        selectActiveControlSpinner.setSelection(sonyControlViewModel.sonyControls.value!!.selected)

        selectActiveControlSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                Timber.i("onItemSelected position:$position")
                // check if new position/control index is set
                if (sonyControlViewModel.sonyControls.value!!.selected != position) {
                    sonyControlViewModel.setSelectedControlIndex(position)
                    Timber.d("onItemSelected setSelectedControlIndex")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Timber.d("onNothingSelected")
            }
        }

        sonyControlViewModel.selectedSonyControl.observe(this, Observer {
            controlListAdapter.notifyDataSetChanged()
        })

        // get channel list from preferences
        val startedFromTVBrowser = intent.getBooleanExtra("startedFromTVBrowser", false)
        if (startedFromTVBrowser) run {
            Timber.i("onCreate: startedFromTVBrowser=$startedFromTVBrowser")
            //controlViewModel.setChannelNameListFromPreference()
            navController.navigate(R.id.nav_channel_map)
        }
        sonyControlViewModel.isCreated = true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}
