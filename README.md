# ROS-Turtle-Controller
Android app capable of controlling the ROS turtlesim or turtlebots.

## Prerequisites
ROS installed on a Ubuntu machine. Note that if you are running Ubuntu in a virtual machine, the network must be configured as Bridged.

## Setup - Turtlesim
### On Ubuntu
Find the machine's IP address using `ifconfig`. For each new terminal opened, export this address to `ROS_MASTER_URI` and `ROS_HOSTNAME`.

On a terminal, run `roscore`. On a second terminal, run `rosrun turtlesim turtlesim_node`. A window should pop up with an image of a turtle in the middle.

### On Android
Launch the app. In the Master URI text field, enter the Master URI found above and tap Connect. Try again if the app crashes.

A new screen should appear. Slide the sliders on the screen to control the turtle's movements on the Ubuntu machine.

## Setup - Turtlebot
Setup the system such that the essential nodes are running on the turtlebot and the remote machine.

Launch the app. In the Master URI text field, enter the Master URI and tap Connect. Try again if the app crashes.

A new screen should appear. Switch the driving mode to one of the turtlebots (Waffle or Burger). Slide the sliders on the screen to control the turtlebot's movements.
