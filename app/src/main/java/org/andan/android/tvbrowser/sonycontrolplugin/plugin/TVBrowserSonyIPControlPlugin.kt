package org.andan.android.tvbrowser.sonycontrolplugin.plugin

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.os.RemoteException
import android.preference.PreferenceManager
import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.andan.android.tvbrowser.sonycontrolplugin.MainActivity
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.network.SonyIPControlIntentService
import org.andan.av.sony.SonyIPControl
import org.tvbrowser.devplugin.*
import java.io.ByteArrayOutputStream
import java.util.*

/**
 * A service class that provides a channel switch functionality for Sony TVs within TV-Browser for Android.
 *
 * @author andan
 */
class TVBrowserSonyIPControlPlugin : Service(),
    OnSharedPreferenceChangeListener {
    /* The plugin manager of TV-Browser */
    private var mPluginManager: PluginManager? = null

    /* The set with the marking ids */
    private val mMarkingProgramIds: MutableSet<String>? = null
    private var mSonyIpControl: SonyIPControl? = null
    private val getBinder: Plugin.Stub =
        object : Plugin.Stub() {
            private val mRemovingProgramId: Long = -1
            override fun getVersion(): String {
                return getString(R.string.app_version)
            }

            override fun getDescription(): String {
                return getString(R.string.service_sonycontrol_description)
            }

            override fun getAuthor(): String {
                return "andan"
            }

            override fun getLicense(): String {
                return getString(R.string.license)
            }

            override fun getName(): String {
                return getString(R.string.service_sonycontrol_name)
            }

            override fun getMarkIcon(): ByteArray {
                val icon =
                    BitmapFactory.decodeResource(resources,
                        R.drawable.ic_action_share
                    )
                val stream = ByteArrayOutputStream()
                icon.compress(Bitmap.CompressFormat.PNG, 100, stream)
                return stream.toByteArray()
            }

            override fun onProgramContextMenuSelected(
                program: Program,
                pluginMenu: PluginMenu
            ): Boolean {
                val result = false
                Log.i(
                    TAG,
                    " onProgramContextMenuSelected:start"
                )
                if (pluginMenu.id == SWITCH_TO_CHANNEL) {
                    Log.i(
                        TAG,
                        " onProgramContextMenuSelected:switch to channel " + program.channel
                            .channelName
                    )
                    //setControlAndChannelMapFromPreferences();
                    if (mSonyIpControl != null && mSonyIpControl!!.channelProgramUriMap != null) {
                        val channelName = program.channel.channelName
                        val programUri =
                            mSonyIpControl!!.channelProgramUriMap[channelName]
                        Log.i(
                            TAG,
                            " onProgramContextMenuSelected:control " + mSonyIpControl.toString()
                        )
                        if (programUri != null && !programUri.isEmpty()) {
                            val intentService = Intent(
                                applicationContext,
                                SonyIPControlIntentService::class.java
                            )
                            val controlJSON =
                                SonyIPControl.getGson().toJson(mSonyIpControl!!.toJSON())
                            intentService.putExtra(SonyIPControlIntentService.CONTROL, controlJSON)
                            intentService.putExtra(SonyIPControlIntentService.URI, programUri)
                            intentService.putExtra(
                                SonyIPControlIntentService.ACTION,
                                SonyIPControlIntentService.SET_PLAY_CONTENT_ACTION
                            )
                            applicationContext.startService(intentService)
                            Log.i(
                                TAG,
                                "Switch to program uri:$programUri"
                            )
                        }
                    }
                }
                return result
            }

            override fun getContextMenuActionsForProgram(program: Program): Array<PluginMenu> {
                val menuList = ArrayList<PluginMenu>()
                val channelName = program.channel.channelName
                val title =
                    getString(R.string.service_sonycontrol_context_menu) + " '" + channelName + "' on TV"
                menuList.add(
                    PluginMenu(
                        SWITCH_TO_CHANNEL,
                        title
                    )
                )
                return menuList.toTypedArray()
            }

            override fun hasPreferences(): Boolean {
                return true
            }

            @Throws(RemoteException::class)
            override fun openPreferences(subscribedChannels: List<Channel>) {
                Log.i(
                    TAG,
                    "openPreferences:start"
                )
                // start main activity
                val startPref =
                    Intent(this@TVBrowserSonyIPControlPlugin, MainActivity::class.java)
                startPref.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                //startPref.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                Log.i(
                    TAG,
                    "openPreferences:start"
                )
                if (mPluginManager != null) {
                    writeChannelListIntoPreference(mPluginManager!!.subscribedChannels)
                    val arrayList =
                        ArrayList(subscribedChannels)
                    startPref.putParcelableArrayListExtra("channelList", arrayList)
                }
                startActivity(startPref)
            }

            override fun getMarkedPrograms(): LongArray {
                val markings = LongArray(mMarkingProgramIds!!.size)
                val values: Iterator<String> =
                    mMarkingProgramIds.iterator()
                for (i in markings.indices) {
                    markings[i] = values.next().toLong()
                }
                return markings
            }

            override fun handleFirstKnownProgramId(programId: Long) {
                if (programId == -1L) {
                    mMarkingProgramIds!!.clear()
                } else {
                    val knownIds =
                        mMarkingProgramIds!!.toTypedArray()
                    for (i in knownIds.indices.reversed()) {
                        if (knownIds[i].toLong() < programId) {
                            mMarkingProgramIds.remove(knownIds[i])
                        }
                    }
                }
            }

            @Throws(RemoteException::class)
            override fun onActivation(pluginManager: PluginManager) {
                mPluginManager = pluginManager
                getSharedPreferences(
                    getString(R.string.pref_control_file_key),
                    Context.MODE_PRIVATE
                ).registerOnSharedPreferenceChangeListener(this@TVBrowserSonyIPControlPlugin)
                setControlAndChannelMapFromPreferences(true)
                writeChannelListIntoPreference(mPluginManager!!.subscribedChannels)
                val intentService =
                    Intent(applicationContext, SonyIPControlIntentService::class.java)
                val controlJSON =
                    SonyIPControl.getGson().toJson(mSonyIpControl!!.toJSON())
                intentService.putExtra(SonyIPControlIntentService.CONTROL, controlJSON)
                intentService.putExtra(
                    SonyIPControlIntentService.ACTION,
                    SonyIPControlIntentService.RENEW_COOKIE_ACTION_PLUGIN
                )
                Log.d(TAG, "check token")
                applicationContext.startService(intentService)
                Log.d(TAG, "onActivation")
            }

            override fun onDeactivation() {
                /* Don't keep instance of plugin manager*/
                mPluginManager = null
                mSonyIpControl = null
            }

            override fun isMarked(programId: Long): Boolean {
                return programId != mRemovingProgramId && mMarkingProgramIds!!.contains(programId.toString())
            }

            override fun getAvailableProgramReceiveTargets(): Array<ReceiveTarget>? {
                return null
            }

            override fun receivePrograms(
                programs: Array<Program>,
                target: ReceiveTarget
            ) {
            }
        }

    override fun onBind(intent: Intent): IBinder {
        return getBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        /* Don't keep instance of plugin manager*/
        mPluginManager = null
        getSharedPreferences(
            getString(R.string.pref_control_file_key),
            Context.MODE_PRIVATE
        ).unregisterOnSharedPreferenceChangeListener(this@TVBrowserSonyIPControlPlugin)
        stopSelf()
        return false
    }

    override fun onDestroy() {
        /* Don't keep instance of plugin manager*/
        mPluginManager = null
        super.onDestroy()
    }

    private fun writeChannelListIntoPreference(channelList: List<Channel>) {
        val preferences = getSharedPreferences(
            getString(R.string.pref_channel_switch_file_key),
            Context.MODE_PRIVATE
        )
        // write channels into preference file
        val editor = preferences.edit()

        // create JSON Object
        try {
            val channelListJSON = JsonObject()
            val channelListJsonArray = JsonArray()
            for (channel in channelList) {
                channelListJsonArray.add(channel.channelName)
            }
            channelListJSON.add("subscribed channels", channelListJsonArray)
            editor.putString(
                CHANNELS_LIST_CONFIG,
                SonyIPControl.getGson().toJson(channelListJSON)
            )
        } catch (ex: Exception) {
            Log.e(TAG, ex.message)
        }
        editor.commit()
        Log.i(
            TAG,
            "writeChannelListIntoPreference"
        )
    }

    private fun setControlAndChannelMapFromPreferences(calledOnActivation: Boolean) {
        Log.i(
            TAG,
            "setControlAndChannelMapFromPreferences():start"
        )
        // try instantiating control from json stored in preference
        val controlPreferences = getSharedPreferences(
            getString(R.string.pref_control_file_key),
            Context.MODE_PRIVATE
        )
        val controlConfig =
            controlPreferences.getString(CONTROL_CONFIG, "")
        if (controlConfig != null && !controlConfig.isEmpty()) {
            var selectedControlIndex = -1
            var controlsJSON =
                SonyIPControl.getGson().fromJson(controlConfig, JsonObject::class.java)
            var controls = controlsJSON["controls"] as JsonArray
            mSonyIpControl = null
            if (controlsJSON.has("selected")) {
                selectedControlIndex = controlsJSON["selected"].asInt
                if (selectedControlIndex >= 0) mSonyIpControl =
                    SonyIPControl(controls[selectedControlIndex] as JsonObject)
            } else if (calledOnActivation) {
                // try conversion of old config/preferences
                val mapChannelPreferences =
                    PreferenceManager.getDefaultSharedPreferences(
                        applicationContext
                    )
                val mapConfig =
                    mapChannelPreferences.getString(getString(R.string.pref_map_channel_key), "")
                val channelMap =
                    LinkedHashMap<String, String>()
                var mapsForControlKey: String? = null
                Log.i(
                    TAG,
                    "setControlAndChannelMapFromPreferences():mapconfig $mapConfig"
                )
                if (mapConfig != null && !mapConfig.isEmpty()) {
                    Log.i(
                        TAG,
                        "setControlAndChannelMapFromPreferences(): mapConfig exists, try convert"
                    )
                    try {
                        val mapsForControlJSON = SonyIPControl.getGson()
                            .fromJson(mapConfig, JsonObject::class.java)
                        mapsForControlKey = mapsForControlJSON["active_control"].asString
                        val mapsJSON =
                            mapsForControlJSON[mapsForControlKey] as JsonObject
                        for ((key, value) in mapsJSON.entrySet()) {
                            channelMap[key] = value.asString
                        }
                    } catch (ex: Exception) {
                        Log.e(
                            TAG,
                            "setControlAndChannelMapFromPreferences(): " + ex.message
                        )
                    }
                }
                Log.i(
                    TAG,
                    "setControlAndChannelMapFromPreferences():map " + channelMap.size + ":" + mapsForControlKey
                )
                val mControlList =
                    ArrayList<SonyIPControl>()
                if (mapsForControlKey != null) {
                    for (i in 0 until controls.size()) {
                        val ipControl = SonyIPControl(controls[i] as JsonObject)
                        mControlList.add(ipControl)
                        if (ipControl.uuid == mapsForControlKey) {
                            selectedControlIndex = i
                        }
                    }
                }
                if (mControlList.size > 0 && selectedControlIndex == -1) {
                    // control not found by key. Use first one.
                    selectedControlIndex = 0
                }
                controlsJSON = JsonObject()
                controls = JsonArray()
                for (ipControl in mControlList) {
                    try {
                        val e = ipControl.toJSON()
                        controls.add(e)
                    } catch (ex: Exception) {
                        Log.i(TAG, ex.message)
                    }
                }
                controlsJSON.add("controls", controls)
                controlsJSON.addProperty("selected", selectedControlIndex)
                val editor = controlPreferences.edit()
                editor.putString("controlConfig", SonyIPControl.getGson().toJson(controlsJSON))
                editor.commit()
                Log.i(
                    TAG,
                    "setControlAndChannelMapFromPreferences(): written control to preference " + channelMap.size + " mappings"
                )
            }
        }
        Log.i(
            TAG,
            "setControlAndChannelMapFromPreferences():control:" + if (mSonyIpControl != null) mSonyIpControl!!.uuid else "null"
        )
        Log.i(
            TAG,
            "setControlAndChannelMapFromPreferences():end:"
        )
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences,
        key: String
    ) {
        Log.i(TAG, "onSharedPreferenceChanged")
        setControlAndChannelMapFromPreferences(false)
    }

    companion object {
        const val CONTROL_CONFIG = "controlConfig"
        const val CHANNELS_LIST_CONFIG = "channels_list_config"
        private val TAG = TVBrowserSonyIPControlPlugin::class.java.simpleName

        /* The id for the remove marking PluginMenu */
        private const val SWITCH_TO_CHANNEL = 4
    }
}