This repository lets your ROS software communicate with IOP services. You can find an overview of all supported services at [doc/other_packages.md](doc/other_packages.md).

Build status of latest version:

[![Build Status](https://travis-ci.org/fkie/iop_core.svg?branch=master)](https://travis-ci.org/fkie/iop_core)

# Installation of the ROS/IOP Bridge

## Install dependencies for JAUS Toolset

ROS/IOP Bridge uses JAUS Toolset (JTS) to generate basic C++ code from JSIDL definitions of JAUS. You find the JAUS Toolset desciption [here](http://jaustoolset.org/). Currently only a part of sources of JTS is used.
This part will build while you build the ROS/IOP Bridge sources. For successful build JTS needs follow external software:

Install OpenJKD:

```console
sudo apt-get install default-jdk -y
```

>If you prefer Oracle Java, see [here](doc/install_oracle_java.md)

## Set up your ROS environment

Make sure, that your catin workspace is set up correctly:

- Using [catkin](http://wiki.ros.org/catkin/Tutorials/create_a_workspace)
- Using [catkin_tools](https://catkin-tools.readthedocs.io/en/latest/quick_start.html#initializing-a-new-workspace)

For easier download of bridge packages install **[wstool](http://wiki.ros.org/wstool)**:

```console
sudo apt-get install python-wstool -y
```

You need also some other dependencies while you build the complete bridge:

```console
sudo apt install python-lxml ros-kinetic-geodesy ros-kinetic-gps-common ros-kinetic-geographic-msgs ros-kinetic-moveit-msgs ros-kinetic-moveit-planners
```

>For other ROS version replace kinetic by your ROS version

----

## Download the ROS/IOP-Bridge packages

The sourcecode of the ROS/IOP-Bridge is splitted into different Git repositories. Depending on your configuration you need a different set of packages. An overview of existing packages you find [here](doc/other_packages.md)

For download the ROS/IOP-Bridge sources we use the [wstool](http://wiki.ros.org/wstool):
  > If you do not already have an *.rosinstall* go into you ROS workspace and call  
  >`cd catkin_ws/; wstool init src/iop`

Merge the iop.rosinstall file and fetch code.

```console
wstool merge -t src/iop https://raw.githubusercontent.com/fkie/iop_core/master/iop.rosinstall
wstool update -t src/iop
```

## Build the sources

Go to the root folder of your ROS workspace and type:

```console
catkin build
```

>In case of "permission denied" errors you have to mark the \*.py files in *fkie\_iop\_builder/cmake/* as runnable
>You can also use `catkin build` instead of `catkin_make`, if you have [catkin_tools](https://catkin-tools.readthedocs.io/en/latest/) installed.  

>If some errors occur while JTS build regarding missing *pthread* and *timer* the *libpthread* and *librt* have to be included. This can be done by replacing LIBS=[] by LIBS=['-lpthread', '-lrt'] in   *jaustoolset/GUI/templates/Common/SConstruct*

## Additional Information

For convenient usage of ROS environment use the `node_manager` of `multimaster_fkie`. You can install it from  [https://github.com/fkie/multimaster_fkie](https://github.com/fkie/multimaster_fkie) using

```console
wstool merge -t src https://raw.githubusercontent.com/fkie/multimaster_fkie/master/fkie_multimaser.rosinstall
wstool update -t src
```

On each host you run IOP components you need to start a ``Node Manager``. There are two alternatives:

### 1. JTSNodeManager

This _Node Manager_ is part of JausToolSet and included in **iop_core**. You can start this Node Manager with following command:

```console
bash rosrun jaustoolset jaus_node_manager.sh start
```

>To exit the script press `CTRL+C` or type `bash rosrun jaustoolset jaus_node_manager.sh stop` in a new terminal window.
>See example below how to integrate the JTS-`nodeManager`into launch-file.

### 2. IOPNodeManager

This Node Manager is written in Python and is an alternative development to JTSNodeManager. You need to install it as separate package from [https://github.com/fkie/iop_node_manager](https://github.com/fkie/iop_node_manager) using

```console
wstool merge -t src/iop https://raw.githubusercontent.com/fkie/iop_node_manager/master/iop_node_manager.rosinstall
wstool update -t src/iop
catkin build
```

## Example

For a simple example with turtlesim see [fkie_iop_cfg_sim_turtle](https://github.com/fkie/iop_examples/blob/master/fkie_iop_cfg_sim_turtle/README.md) package.

You find other examples in [iop_examples](https://github.com/fkie/iop_examples) repository.

----

## [How it works - an overview](doc/how_it_works.md)

## [IOP packages - an overview](doc/other_packages.md)

## [IOP core packages](doc/iop_core_packages.md)

## [Builder-package](fkie_iop_builder/README.md)

## [How to start with own configuration](doc/howto_minimal_config.md)

## [How to create own plugin](doc/howto_create_plugin.md)
