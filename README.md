Channel Browser and Control for Sony TVs
===================================

Easy to use channel browser and remote control for Sony TVs.

#### Main features
- Supports many smart Sony TVs (model year ~2013 to ???)
- Provides a full featured remote control over network
- Shows list of TV channels for easy searching and switching
- As&nbsp;<a href="https://play.google.com/store/apps/details?id=org.tvbrowser.tvbrowser.play">TV Browser</a> plugin: Switch to a TV channel from the program guide (EPG)

#### The app's architecture follows the current Android recommendations
- Use of repositories for the data layer (Room database, DataStore)
- UI layer based on Compose and ViewModels
- Use of coroutines and flows to communicate between layers
- Use of domain layer

#### Other used techniques and tools include:
- Dependency injection with Hilt (KSP based)
- Central version catalog for dependencies
- Retrofit for implementation of REST calls
- Spotless for code formatting
