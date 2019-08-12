Babbage
=============
## Overview
Babbage is an Android application aimed to perform network measurements on mobile devices.

## Installation
1. Install Android Studio.
2. Select Import Project from File menu.
3. Import Babbage.
4. Install SDK 21 or above.
5. Run Babbage.

## Features
### Ping
Ping tests are performed by querying the list of targets from `ruggles.gtnoise.net/values` and then executing ping commands to each target asynchronously. If the server is unreachable for some reason, default list of targets are hard coded into the application so that the application can function on its own.
### Traceroute
Traceroute also functions similarly to ping, except when the user manually performs the test. Traceroute allows the user to choose their own destination and the type of test they want to perform (UDP or ICMP). However, automatic traceroute will query the list of targets from `ruggles.gtnoise.net/values` and execute traceroute command to each target asynchronously. Since most phones do not come with traceroute pre-installed, Babbage included an executable compiled to work with ARM processor phones.
### Data Usage
Babbage keeps track of cumulative data usage for a month instead of getting reset everytime the phone is rebooted. To do this, the application uses a database to store the usage data so that it can persist through reboots. On the first day of each month, the database will be cleared and a new cycle will begin.
## Structure
### Model
Models hold information about specific objects that are needed by Babbage. However, these objects only perform minimal functions to pass data between the activities or between the client and the server. Most of the application's functionalities are deferred to the controllers.

### View
Views are classes needed to display information to the user. All the user interface actions should be handled here. The preferred way of communicating between the controller classes and the view classes is through message handlers. Each activity should define its own message handler and pass it into a controller class.

### Controller
##### Manager
Manager classes are the highest level controllers. The view classes should only communicate with managers as the managers will hide the messy details of threading from the view classes. The sole purpose of a manager is to create a threadpool which will allow the application to perform multiple tasks asynchronously.

##### Task
Task classes are runnable classes that usually defers its task to a util class and simply packages the data returned by the util. This data is sent back to the UI layer through the handler passed down by the manager class. All tasks should implement Runnable so that it can be executed in a threadpool.

##### Util
Util classes are where all the actual work is being done. It performs a measurement and returns a model corresponding to the result. These classes should not perform multi-threaded tasks as it should be up to the manager to dispatch multiple tasks which will spawn multiple utils with different arguments.

## Related Projects
### Mobilyzer
https://github.com/mobilyzer/Mobilyzer

### Centinel
https://github.com/iclab/centinel

## Version
4.1.0

License
----
Apache License 2.0
