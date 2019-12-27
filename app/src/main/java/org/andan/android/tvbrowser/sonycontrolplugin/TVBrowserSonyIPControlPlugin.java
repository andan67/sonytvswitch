package org.andan.android.tvbrowser.sonycontrolplugin;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.andan.av.sony.SonyIPControl;
import org.tvbrowser.devplugin.Channel;
import org.tvbrowser.devplugin.Plugin;
import org.tvbrowser.devplugin.PluginManager;
import org.tvbrowser.devplugin.PluginMenu;
import org.tvbrowser.devplugin.Program;
import org.tvbrowser.devplugin.ReceiveTarget;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A service class that provides a channel switch functionality for Sony TVs within TV-Browser for Android.
 *
 * @author andan
 */
public class TVBrowserSonyIPControlPlugin extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String CONTROL_CONFIG = "controlConfig";
    public static final String CHANNELS_LIST_CONFIG = "channels_list_config";
    private static final String TAG = TVBrowserSonyIPControlPlugin.class.getSimpleName();

    /* The id for the remove marking PluginMenu */
    private static final int SWITCH_TO_CHANNEL = 4;

    /* The plugin manager of TV-Browser */
    private PluginManager mPluginManager;

    /* The set with the marking ids */
    private Set<String> mMarkingProgramIds;

    private SonyIPControl mSonyIpControl;

    private final Plugin.Stub getBinder = new Plugin.Stub() {
        private final long mRemovingProgramId = -1;

        @Override
        public String getVersion() {
            return getString(R.string.version);
        }

        @Override
        public String getDescription() {
            return getString(R.string.service_sonycontrol_description);
        }

        @Override
        public String getAuthor() {
            return "andan";
        }

        @Override
        public String getLicense() {
            return getString(R.string.license);
        }

        @Override
        public String getName() {
            return getString(R.string.service_sonycontrol_name);
        }

        @Override
        public byte[] getMarkIcon() {
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_share);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            icon.compress(Bitmap.CompressFormat.PNG, 100, stream);

            return stream.toByteArray();
        }

        @Override
        public boolean onProgramContextMenuSelected(Program program, PluginMenu pluginMenu) {
            boolean result = false;
            Log.i(TAG, " onProgramContextMenuSelected:start");
            if (pluginMenu.getId() == SWITCH_TO_CHANNEL) {
                Log.i(TAG, " onProgramContextMenuSelected:switch to channel " + program.getChannel().getChannelName());
                //setControlAndChannelMapFromPreferences();
                if (mSonyIpControl != null && mSonyIpControl.getChannelProgramUriMap()!=null) {
                    String channelName = program.getChannel().getChannelName();
                    final String programUri = mSonyIpControl.getChannelProgramUriMap().get(channelName);
                    Log.i(TAG, " onProgramContextMenuSelected:control " + mSonyIpControl.toString());
                    Intent intentService = new Intent(getApplicationContext(), SonyIPControlIntentService.class);
                    String controlJSON = SonyIPControl.getGson().toJson(mSonyIpControl.toJSON());
                    intentService.putExtra(SonyIPControlIntentService.CONTROL, controlJSON);
                    if (programUri!= null && !programUri.isEmpty()) {
                        intentService.putExtra(SonyIPControlIntentService.CONTROL, controlJSON);
                        intentService.putExtra(SonyIPControlIntentService.URI, programUri);
                        intentService.putExtra(SonyIPControlIntentService.ACTION, SonyIPControlIntentService.SET_PLAY_CONTENT_ACTION);
                        getApplicationContext().startService(intentService);
                        Log.i(TAG, "Switch to program uri:" +  programUri);
                    }
                }
            }
            return result;
        }

        @Override
        public PluginMenu[] getContextMenuActionsForProgram(Program program) {
            ArrayList<PluginMenu> menuList = new ArrayList<>();
            String channelName = program.getChannel().getChannelName();
            String title = getString(R.string.service_sonycontrol_context_menu) + " '" + channelName + "' on TV";
            menuList.add(new PluginMenu(SWITCH_TO_CHANNEL, title));
            return menuList.toArray(new PluginMenu[menuList.size()]);
        }

        @Override
        public boolean hasPreferences() {
            return true;
        }

        @Override
        public void openPreferences(List<Channel> subscribedChannels) throws RemoteException {
            Log.i(TAG, "openPreferences:start");
            // start main activity
            Intent startPref = new Intent(TVBrowserSonyIPControlPlugin.this, MainActivity.class);
            startPref.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //startPref.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            Log.i(TAG, "openPreferences:start");

            if (mPluginManager != null) {
                writeChannelListIntoPreference(mPluginManager.getSubscribedChannels());
                final ArrayList<Channel> arrayList = new ArrayList<>(subscribedChannels);
                startPref.putParcelableArrayListExtra("channelList", arrayList);
            }
            startActivity(startPref);
        }

        @Override
        public long[] getMarkedPrograms() {
            long[] markings = new long[mMarkingProgramIds.size()];

            Iterator<String> values = mMarkingProgramIds.iterator();

            for (int i = 0; i < markings.length; i++) {
                markings[i] = Long.parseLong(values.next());
            }

            return markings;
        }

        @Override
        public void handleFirstKnownProgramId(long programId) {
            if (programId == -1) {
                mMarkingProgramIds.clear();
            } else {
                String[] knownIds = mMarkingProgramIds.toArray(new String[mMarkingProgramIds.size()]);

                for (int i = knownIds.length - 1; i >= 0; i--) {
                    if (Long.parseLong(knownIds[i]) < programId) {
                        mMarkingProgramIds.remove(knownIds[i]);
                    }
                }
            }
        }

        @Override
        public void onActivation(PluginManager pluginManager) throws RemoteException {
            mPluginManager = pluginManager;
            getSharedPreferences(getString(R.string.pref_control_file_key), MODE_PRIVATE).registerOnSharedPreferenceChangeListener(TVBrowserSonyIPControlPlugin.this);
            setControlAndChannelMapFromPreferences(true);
            writeChannelListIntoPreference(mPluginManager.getSubscribedChannels());

            Log.d(TAG, "onActivation");
        }


        @Override
        public void onDeactivation() {
            /* Don't keep instance of plugin manager*/
            mPluginManager = null;
            mSonyIpControl = null;
        }

        @Override
        public boolean isMarked(long programId) {
            return programId != mRemovingProgramId && mMarkingProgramIds.contains(String.valueOf(programId));
        }

        @Override
        public ReceiveTarget[] getAvailableProgramReceiveTargets() {
            return null;
        }

        @Override
        public void receivePrograms(Program[] programs, ReceiveTarget target) {
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return getBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        /* Don't keep instance of plugin manager*/
        mPluginManager = null;
        getSharedPreferences(getString(R.string.pref_control_file_key), MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(TVBrowserSonyIPControlPlugin.this);
        stopSelf();

        return false;
    }

    @Override
    public void onDestroy() {
        /* Don't keep instance of plugin manager*/
        mPluginManager = null;

        super.onDestroy();
    }

    private void writeChannelListIntoPreference(List<Channel> channelList) {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.pref_channel_switch_file_key), MODE_PRIVATE);
        // write channels into preference file
        SharedPreferences.Editor editor = preferences.edit();

        // create JSON Object
        try {
            JsonObject channelListJSON = new JsonObject();
            JsonArray channelListJsonArray = new JsonArray();

            for (Channel channel : channelList) {
                channelListJsonArray.add(channel.getChannelName());
            }
            channelListJSON.add("subscribed channels", channelListJsonArray);
            editor.putString(CHANNELS_LIST_CONFIG, SonyIPControl.getGson().toJson(channelListJSON));
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
        editor.commit();
        Log.i(TAG, "writeChannelListIntoPreference");
    }

    private void setControlAndChannelMapFromPreferences(boolean calledOnActivation) {
        Log.i(TAG, "setControlAndChannelMapFromPreferences():start");
        // try instantiating control from json stored in preference
        SharedPreferences controlPreferences = getSharedPreferences(getString(R.string.pref_control_file_key), MODE_PRIVATE);


        String controlConfig = controlPreferences.getString(CONTROL_CONFIG, "");

        if (controlConfig!=null && !controlConfig.isEmpty()) {
            int selectedControlIndex = -1;
            JsonObject controlsJSON = SonyIPControl.getGson().fromJson(controlConfig, JsonObject.class);
            JsonArray controls = (JsonArray) controlsJSON.get("controls");
            mSonyIpControl = null;

            if (controlsJSON.has("selected")) {
                selectedControlIndex = controlsJSON.get("selected").getAsInt();
                if (selectedControlIndex >= 0)
                    mSonyIpControl = new SonyIPControl((JsonObject) controls.get(selectedControlIndex));
            } else if (calledOnActivation) {
                // try conversion of old config/preferences
                SharedPreferences mapChannelPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String mapConfig = mapChannelPreferences.getString(getString(R.string.pref_map_channel_key), "");
                LinkedHashMap<String, String> channelMap = new LinkedHashMap<>();
                String mapsForControlKey = null;
                Log.i(TAG, "setControlAndChannelMapFromPreferences():mapconfig " + mapConfig);
                if (mapConfig!=null && !mapConfig.isEmpty()) {
                    Log.i(TAG, "setControlAndChannelMapFromPreferences(): mapConfig exists, try convert");
                    try {
                        JsonObject mapsForControlJSON = SonyIPControl.getGson().fromJson(mapConfig, JsonObject.class);
                        mapsForControlKey = mapsForControlJSON.get("active_control").getAsString();
                        JsonObject mapsJSON = (JsonObject) mapsForControlJSON.get(mapsForControlKey);
                        for (Map.Entry<String, JsonElement> entry : mapsJSON.entrySet()) {
                            channelMap.put(entry.getKey(), entry.getValue().getAsString());
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "setControlAndChannelMapFromPreferences(): " + ex.getMessage());
                    }
                }
                Log.i(TAG, "setControlAndChannelMapFromPreferences():map " + channelMap.size() + ":" + mapsForControlKey);
                ArrayList<SonyIPControl> mControlList = new ArrayList<>();
                if (mapsForControlKey != null) {
                    for (int i = 0; i < controls.size(); i++) {
                        SonyIPControl ipControl = new SonyIPControl((JsonObject) controls.get(i));
                        mControlList.add(ipControl);
                        if (ipControl.getUuid().equals(mapsForControlKey)) {
                            selectedControlIndex = i;
                        }
                    }
                }
                if (mControlList.size() > 0 && selectedControlIndex == -1) {
                    // control not found by key. Use first one.
                    selectedControlIndex = 0;
                }

                controlsJSON = new JsonObject();
                controls = new JsonArray();

                for (SonyIPControl ipControl : mControlList) {
                    try {
                        JsonObject e = ipControl.toJSON();
                        controls.add(e);
                    } catch (Exception ex) {
                        Log.i(TAG, ex.getMessage());
                    }
                }
                controlsJSON.add("controls", controls);
                controlsJSON.addProperty("selected", selectedControlIndex);
                SharedPreferences.Editor editor = controlPreferences.edit();
                editor.putString("controlConfig", SonyIPControl.getGson().toJson(controlsJSON));
                editor.commit();
                Log.i(TAG, "setControlAndChannelMapFromPreferences(): written control to preference " + channelMap.size() + " mappings");
            }
        }
        Log.i(TAG, "setControlAndChannelMapFromPreferences():control:" + (mSonyIpControl != null ? mSonyIpControl.getUuid() : "null"));
        Log.i(TAG, "setControlAndChannelMapFromPreferences():end:");
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Log.i(TAG, "onSharedPreferenceChanged");
        setControlAndChannelMapFromPreferences(false);
    }
}
