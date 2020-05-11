<H1> Sony TV Switch </H1>

* TOC
{:toc}

## Description

This app allows the easy control and program switching via network for many Sony TVs. 
It can also be used as plugin for the 
<a href="https:/play.google.com/store/apps/details?id=org.tvbrowser.tvbrowser.play">*TV Browser*</a> app 
to switch to programs from an electronic program guide (EPG).

### Key features

- Supports many 'smart' Sony TV models
- Full featured remote control over network
- Power on TV via Wake-on-LAN (WoL)
- Shows program list from TV to easily search for and switch to programs from the app
- Can be set-up for multiple Sony TVs
- As&nbsp;<a href="https://play.google.com/store/apps/details?id=org.tvbrowser.tvbrowser.play">*TV Browser*</a> plugin: 
Switch to a TV program directly from the program guide (EPG)

### TV preparation

- Switch on TV
- Enable remote start on your TV:<br>
[HOME] → [Settings] → [Network] → [Home Network Setup] → [Remote Start] → [On]
- If applicable set pre-shared key (i.e. arbitrary string XXXX) for your your TV:<br>
[HOME] → [Settings] → [Network] → [Home Network Setup] → [IP Control] → [Pre-Shared Key] → [XXXX]
- Enable pre-shared key on your TV if menu entry exists:<br>
[HOME] → [Settings] → [Network] → [Home Network Setup] → [IP Control] → [Authentication] → [Normal and Pre-Shared Key]
- Give your TV a static IP address, or make a DHCP reservation for a specific IP address in your router
- Get/check IP address of your TV:<br>
[HOME] → [Settings] → [Network] → [Advanced settings] → [IP address]

### Quick installation and set-up guide

- Install <a href="https://play.google.com/store/apps/details?id=org.andan.android.tvbrowser.sonycontrolplugin">*Sony TV Switch*</a>
- Open app
- Add new control for TV from main menu
- For use as <a href="https://play.google.com/store/apps/details?id=org.tvbrowser.tvbrowser.play">*TV Browser*</a> plugin
    - Install *TV Browser* 
    - Open *TV Browser*
    - Activate this app as *TV Browser* plugin
    - Open plugin settings within *TV Browser*
    - Map *TV Browser* channel names to TV programs (mostly automated)

The following sections provide more detailed descriptions of the set-up steps and other functions.

## Main function and screens

### Navigation menu
<img src="images/navigation_menu.png" width="300"/><br>
- Select active control from drop-down list in header
- If empty, select 'Add control' item to add control


### Use of control functions

#### Remote control
<img src="images/remote_control.png" width="300"/><br>
- Controls the TV over network like the standard infrared remote control
- The 3-dot menu provides actions for
    - Wake-on-LAN
    - Power saving - screen off
    - Power saving - off

#### TV program list
<img src="images/program_list.png" width="300"/><br>
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

<img src="images/current_program.png" width="300"/><br>
- Shows details of current program as received from TV

### Add and manage control functions

#### Add control
Adds and registers a new control in two steps. This is a pre-requisite to use any or the remote control functions of this app.

**1. Define IP/host address**

<img src="images/add_control_ip.png" width="300"/><br>
- Enter IP (or host name) of your Sony TV
- Alternatively, select your TV from the 'Discovered devices' drop down box (if listed) to set the IP/host address field
- Click on 'FINISH' will proceed to the next step if the entered or discovered IP/host address is valid (this is checked when button is clicked)
- Otherwise, the dialog is kept open showing an error message

**2. Define Names and pre-shared key (optional)**

<img src="images/add_control_names_psk.png" width="300"/><br>
- Enter (non-empty) nick- and device names that are used as display names
- Enter the pre-shared key if set for your TV
- Click on 'FINISH' to try to register the control on your TV
- If the registration is successful:
    - The dialog will be closed and the 'Manage control' screen will be opened
    - Additional system information and the TV program list will be fetched from the TV
    - Control is ready for use in the app for all functions
- Otherwise a message will be displayed to indicate the reason of the failure:
    - If no pre-shared key is defined you will be asked to enter a challenge key that is displayed on the TV screen
    - The 'not register anymore' message indicates that the maximum number of registered controls on your TV is reached.
   In this case you have to delete another registered control on your TV first to continue

<img src="images/add_control_names_challenge_code.png" width="300"/>
<img src="images/add_control_names_not_register_any_more.png" width="300"/>

#### Manage control
<img src="images/manage_control.png" width="300"/><br>
- Shows details of the control and the controlled TV
- 3-dot menu provides actions to register or delete control, request program list from TV, and enable Wake-on-LAN

##### Register control
- This re-registers the control on your TV with the existing settings.
- This is normally only required in case of authentication failed errors.

##### Request program list 
- The program list can be requested any time after successful registration
- The number of received programs as shown on the screen might differ from the number shown by the Sony TV as some programs are filtered out (e.g. SAT test programs)

##### Enable Wake-on-LAN
- Enables Wake-on-LAN mode of your TV if supported (will also be enabled during registration)

#### TV Browser channel map

<img src="images/channel_map.png" width="300"/><br>
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
<img src="images/channel_map_single.png" width="300"/><br>
- Allows the manual mapping between the *TV Browser* channel and the Sony TV program name
- The mapping is performed by clicking on one of the program items of the list 
- The available list items depend on the the search filter in the app bar
- If no search filter is defined, than the list shows the top 30 matches according to the automatic best match procedure
- Otherwise, the list shows all program names that comply with the search filter

### Settings
<img src="images/settings.png" width="300"/><br>
- 'Start screen' sets the screen shown on app start and the home screen for navigation within the app.

## Use as TV Browser plugin

### Open TV Browser's plugin settings
<img src="images/tv_browser_plugin_settings.png" width="300"/><br>

- Open 'Plugin settings' from *TV Browser's* main 'Settings'
- If installed, the *Sony TV Switch* app should be listed as shown above
- A click on the item will open the plugin settings of the *Sony TV Switch* app as shown [below](#open-sony-st-switch-plugin-settings)

### Open Sony TV Switch plugin settings
<img src="images/tv_browser_stv_plugin_settings.png" width="300"/><br>
- The app will be activated as plugin by checking the corresponding check box
- 'Open preferences' opens the channel mapping screen as described [above](#tv-browser-channel-map) using the active control

### Switch to Sony TV program
<img src="images/tv_browser_context_menu_marked.png" width="300"/><br>
- A **long** click on a program entry in *TV Browser's* EPG will open a context menu with the option to switch 
to the corresponding channel/program on the Sony TV as defined by the channel mapping

### Troubleshooting

#### 'Forbidden error' message
- Re-register control in Manage Control menu as described [here](#manage-control)

#### Channels cannot be switched from TV Browser app
- Stop and and re-open TV Browser app
- Check plugin activation status
- Deactivate/re-activate plugin
- Check active control in *Sony TV Switch*

