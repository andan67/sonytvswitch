<H1> Sony TV Switch </H1>

* TOC
{:toc}

## Description

This app allows the easy control and program switching via network for many Sony TVs. 
It can also be used as plugin for the 
<a href="https:/play.google.com/store/apps/details?id=org.tvbrowser.tvbrowser.play">*TV Browser*</a> app 
to switch to programs from an electronic program guide (EPG).
### Key features

- Supports many 'smart' Sony TVs from model year 2013
- Full featured remote control over network
- Power on TV via Wake-on-LAN (WoL)
- Shows program list from TV to easily search for and switch to programs from the app
- Can be set-up for multiple Sony TVs
- As&nbsp;<a href="https://play.google.com/store/apps/details?id=org.tvbrowser.tvbrowser.play">*TV Browser*</a> plugin: 
Switch to a TV program directly from the program guide (EPG)

## Installation and basic set-up steps

- Install <a href="https://play.google.com/store/apps/details?id=org.andan.android.tvbrowser.sonycontrolplugin">*Sony TV Switch*</a>
- Open app
- Add new control from navigation menu
- From 'Manage control' screen menu    
    - Register control at the TV
    - Request program list from TV
- For use as <a href="https://play.google.com/store/apps/details?id=org.tvbrowser.tvbrowser.play">*TV Browser*</a> plugin
    - Install *TV Browser* 
    - Open *TV Browser*
    - Activate this app as *TV Browser* plugin
    - Open plugin settings within *TV Browser*
    - Map *TV Browser* channel names to TV programs (mostly automated)
    
## Main function and screens

### Navigation menu
<img src="images/navigation_menu.png" width="300"/>
- Select active control from drop-down list in header
- If empty, select 'Add control' item to add control

### Remote control and program switch functions

#### Remote control
<img src="images/remote_control.png" width="300"/>
- Controls the TV over network like the standard infrared remote control
- The 3-dot menu provides actions for
    - Wake-on-LAN
    - Power saving - screen off
    - Power saving - off

#### TV program list
<img src="images/program_list.png" width="300"/>
- List programs (channels) from the TV with additional infos
    - <img src="images/ic_input_black.png" width="16"/>: program source
    - <img src="images/ic_widget_simple_black.png" width="16"/>: mapped *TV Browser* channel name
- Highlighted header item shows current program as received from TV (if available)
    - Click to show further details (see [below](#details-of-current-program))
    - Long click to refresh from TV
- Switch to program by clicking on respective list item
- Swap between current and previous program with the action button placed in the bottom right corner
- List can be filtered by using the search action in the app bar
- The 3-dot menu provides actions for
  - Wake-on-LAN
  - Power saving - screen off
  - Power saving - off

##### Details of current program

<img src="images/current_program.png" width="300"/>
- Shows details of current program as received from TV

### Manage functions

#### Add control
<img src="images/add_control.png" width="300"/>
- Adds new control by providing basic settings
- The nick and device names are used as display names in the remote control settings of your Sony TV
- Host address can be either IP address or host name
- After control is added, the 'Manage control' screen opens to perform further actions

#### Manage control
<img src="images/manage_control.png" width="300"/>
- Shows details of the control and the controlled TV
- 3-dot menu provides actions to register or delete control, request program list from TV, and enable Wake-on-LAN

##### Register control
- Each control needs to be registered for use with your Sony TV
- Ensure the Sony TV is switched on and accessible via network prior registration
- On first registration, a challenge code will be displayed on the TV screen that needs to be entered into the pop-up dialog
- If registration has been successful, the Wake-on-LAN mode of your TV will be enabled to allow switching on the TV via network
- Registration can be performed any time
- In case of any problem, check the settings and any information displayed by the app or on the Sony TV screen

##### Request program list 
- The program list can be requested any time after successful registration
- The number of received programs as shown on the screen might differ from the number shown by the Sony TV as some programs are filtered out (e.g. SAT test programs)

##### Enable Wake-on-LAN
- Enables Wake-on-LAN mode of your TV if supported (will also be enabled during registration)

#### TV Browser channel map

<img src="images/channel_map.png" width="300"/>
- Manages the mapping between the channel names from the <a href="https://play.google.com/store/apps/details?id=org.tvbrowser.tvbrowser.play">TV-Browser's</a> EPG to 
the corresponding program names as received from the TV
- This mapping is used by the plugin to switch to the corresponding TV program using the channel name from the EPG
- The reason why this mapping is required is that the names are not standardized and that multiple program names might be 
related to a single channel name (e.g. HD and SD programs)
- The channel list is only displayed when the plugin has been activated in 
<a href="https://play.google.com/store/apps/details?id=org.tvbrowser.tvbrowser.play">*TV-Browser*</a> and the plugin settings have been opened
- The 3-dot menu in the app bar provides actions to match with programs and to clear mapping

##### Match with programs
- Applies a fuzzy based match procedure between the TV Browser channel and the Sony TV program names for the complete (possibly filtered) list
- The mapped program name is displayed after the <img src="images/ic_action_tv.png" width="16"/> mapped *TV Browser* channel name
- Usually this will provide accurate results for most of the channel items
- If needed, a manual match can be performed by clicking on channel item from the list (see [below](#manual-mapping))

##### Clear all mappings
- Clears all mappings of the list

##### Manual mapping
<img src="images/channel_map_single.png" width="300"/>
- Allows the manual mapping between the *TV Browser* channel and the Sony TV program name
- The mapping is performed by clicking on one of the program items of the list 
- The available list items depend on the the search filter in the app bar
- If no search filter is defined, than the list shows the top 30 matches according to the automatic best match procedure
- Otherwise, the list shows all program names that comply with the search filter

### Settings
<img src="images/settings.png" width="300"/>

- 'Start screen' sets the screen shown on app start and the home screen for navigation within the app.

## Use as TV Browser plugin

### Open TV Browser's plugin settings
<img src="images/tv_browser_plugin_settings.png" width="300"/>
- Open 'Plugin settings' from *TV Browser's* main 'Settings'
- If installed, the *Sony TV Switch* app should be listed as shown above
- A click on the item will open the plugin settings of the *Sony TV Switch* app as shown [below](#open-sony-st-switch-plugin-settings)

### Open Sony TV Switch plugin settings
<img src="images/tv_browser_stv_plugin_settings.png" width="300"/>
- The app will be activated as plugin by checking the corresponding check box
- 'Open preferences' opens the channel mapping screen as described [above](#tv-browser-channel-map) using the active control

### Switch to Sony TV program
<img src="images/tv_browser_context_menu_marked.png" width="300"/>
- A **long** click on a program entry in *TV Browser's* EPG will open a context menu with the option to switch 
to the corresponding channel/program on the Sony TV as defined by the channel mapping


### Troubleshooting
In case the plugin does not work check/try the following:
- Close and re-open TV Browser
- Check activation status
- Deactivate/re-activate plugin
- Check active control in *Sony TV Switch*

