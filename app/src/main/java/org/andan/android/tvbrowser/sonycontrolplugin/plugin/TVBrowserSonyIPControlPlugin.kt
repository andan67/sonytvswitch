package org.andan.android.tvbrowser.sonycontrolplugin.plugin

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.MainActivity
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.SonyControlApplication
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyRepository
import org.tvbrowser.devplugin.*
import java.io.ByteArrayOutputStream

/**
 * A service class that provides a channel switch functionality for Sony TVs within TV-Browser for Android.
 *
 * @author andan
 */
class TVBrowserSonyIPControlPlugin : Service() {
    /* The plugin manager of TV-Browser */
    private var mPluginManager: PluginManager? = null
    private val TAG = TVBrowserSonyIPControlPlugin::class.java.name
    /* The set with the marking ids */
    private val mMarkingProgramIds: MutableSet<String>? = null
    private val repository: SonyRepository =
        SonyControlApplication.get().appComponent.sonyRepository()

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
                    BitmapFactory.decodeResource(
                        resources,
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
                    try {
                        val programUri =
                            repository.selectedSonyControl.value!!.channelProgramMap[program.channel.channelName]
                        GlobalScope.launch(Dispatchers.IO) {
                            repository.setPlayContent(programUri!!)
                            Log.i(TAG, "Switched to program uri:$programUri")
                        }
                    } catch (ex: java.lang.Exception) {
                        Log.e(TAG, ex.message)
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
                val startPref = Intent(this@TVBrowserSonyIPControlPlugin, MainActivity::class.java)
                startPref.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (mPluginManager != null) {
                    updateChannelProgramMap(mPluginManager!!.subscribedChannels)
                    startPref.putExtra("startedFromTVBrowser", true)
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
                updateChannelProgramMap(mPluginManager!!.subscribedChannels)
                Log.d(TAG, "onActivation")
            }

            override fun onDeactivation() {
                /* Don't keep instance of plugin manager*/
                mPluginManager = null
                //mSonyIpControl = null
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
        stopSelf()
        return false
    }

    override fun onDestroy() {
        /* Don't keep instance of plugin manager*/
        mPluginManager = null
        super.onDestroy()
    }

    private fun updateChannelProgramMap(channelList: List<Channel>) {
        val channelNameList = ArrayList<String>()
        for (channel in channelList) {
            channelNameList.add(channel.channelName)
        }
        Log.d(TAG,"updateChannelProgramMap: ${channelNameList.size}")
        repository.updateChannelMapsFromChannelNameList(channelNameList)
    }



    companion object {
        //const val CONTROL_CONFIG = "controlConfig"
        const val CHANNELS_LIST_CONFIG = "channels_list_config"
        private val TAG = TVBrowserSonyIPControlPlugin::class.java.simpleName

        /* The id for the remove marking PluginMenu */
        private const val SWITCH_TO_CHANNEL = 4
    }
}