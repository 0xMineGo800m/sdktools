# sdktools

The purpose of this project is to present in a range/domain graph, input data (comprised of x,y,z values) that is obtained by either:
- Reading from a file
- Reading from a socket

# UI design
The application has a single screen with two main ways to load data. Both methods appear in the MenuBar. They are:
- Open recording... - load from file dialog
- Connect to port - wait for incoming connections and read data from a connected socket (Usually a C++ program that writes to a socket)
When the GUI is launched, and an input method is selected and data starts streaming in, a main graph view will start rendering 3 axes: X, Y, Z

If the user chose to load a file, he will have 3 buttons to control the graph:
- Play recording - will start reading the fileinput
- Stop playing - will stop the playback
- Reset chart y range - will reset chart range to (-10.0, 10.00) values. The user can right click for options and also drag a rectangle around the axes
The buttons are not relevent when reading from socket. 

# Start flow
The app main function starts and then 3 action occur:
- rememberd states (state hoisting):
  - DataRepository is remembered - acts as a data provider (similar to db)
  - ChartLogic is remembered - handles the logic for the chart. Also holds a GraphState object to save different UI states
  - MenuBarActions is rememeberd - handles the logic for the MenuBar actions (Load file..., exit..., connect to socket... etc)
- a WindowMenuBar Compose function is called with dependency
- a ChartScreen Compose function is called with dependency

# Components:
- DataRepository - when a user loads a file, the File is saved in this class for easy access later on.
- ChartLogic - in here the onEvent function is present. It interacts with the UI and also updates the different states that the UI is observing. It handles:
  - Reading from file and updating x,y,z states
  - Socket input and updating x,y,z states
  - Handles selected axis visibility changes
  - Handles button clicks actions
- MenuBarActions - Handles the UI actions that occur in the MenuBar:
  -  Launching a Select File Dialog if user requests
  -  Initates a ServerSocket operation if user requests


# My main concern points are:
- Is the seperation of ChartScreen (UI) and ChartLogic (Busines logic) and ChartState (State) and DataRepository done correctly?
- Do I use 'remember', 'mutableStateOf' where needed and in a correct fashion?
- The chart rendering is done using a Java Swing componenet called JFreeChart. I am concerened this object is recreated every recompose (when data is changed and needs to be rendered). Do I need to use LaunchedEffect to resolve an issue here?

# Miscl
- There is an output.txt file in /assets folder that one can load if needed
