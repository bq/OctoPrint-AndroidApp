# OctoPrint App for Android (beta version)

OctoPrint App for Android is an Android application for controlling one or more 3D printers (RepRap, Witbox, Hephestos, …) through an [OctoPrint server](http://www.octoprint.org). It is Free Software and released under the terms of the [GNU General Public License, version 3](http://www.gnu.org/licenses/gpl-3.0.en.html).

This app is not yet finished and provided as a public beta for now. You are encouraged to give it a try and help improve it by contributing.

You are currently looking at the source code repository of OctoPrint App for Android.

## What does it do?

The goal of this app is to provide a user friendly but powerful interface to your 3D printer, with full support for slicing and directly printing any STL models.

The features currently included in this application are:

  * Initial setup
    * Automatic discovery of the OctoPrint Server
    * Automatic setup of machine parameters for BQ Witbox and BQ Prusa i3 Hephestos & guided setup for any RepRap printer
    * Easy connection to existing OctoPrint Servers through API Key entry or even fully automatically through the [OctoPrint AppCompanion Plugin](https://github.com/bq/OctoPrint-AppCompanion)

![alt tag](./media/image01.png?raw=true)

  * 3D model editing and slicing
    * Rotating, scaling, duplicating, etc. - everything you need to get your model ready for printing
    * Configure layer height, infill, support settings etc and store those slicing settings in handy profiles
    * Preview the sliced GCODE in 3D
    * Have the app slice your model automatically after each change you do (configurable)

![alt tag](./media/image02.png?raw=true)

![alt tag](./media/image04.png?raw=true)

  * 3D model library
    * STL and GCODE file management: Access and manage your files stored on the tablet, the OctoPrint Server and the printer’s SD card
    * Preview your 3D models
    * Linking of STL and GCODE files of previous print jobs for easy replication of them with equal settings
    * Printing history

![alt tag](./media/image03.png?raw=true)

  * Printing control
    * Quick print of STL files with default slicing settings
    * Video monitoring for multiple printers
    * Basic printing actions (jogging the axes, extruding/retracting, adjusting the temperatures, pause, resume and cancel the print job…)
    * Concurrent control of multiple printers
    * 3D visualization of GCODE progress
    * Get notified about the progress of your print jobs

![alt tag](./media/image00.png?raw=true)

## Setup

### App Installation

The app can be installed downloading the apk of the latest release:

[OctoPrint App for Android (latest release)](https://github.com/bq/OctoPrint-AndroidApp/releases/latest)

Please note that at the moment only Android tablets are supported. Installation of software from 3rd party sources needs to be enabled.

### Server Installation

The app should work out of the box with the current [**OctoPi release 0.12.0**](http://octoprint.org/download/) that includes OctoPrint 1.2 including working network discovery. If you are new to OctoPrint, this is the easiest way to get started. You’ll need a Raspberry Pi (Raspberry Pi B, B+ and Raspberry Pi 2 B are all fine) and an SD card. Please follow the setup instructions found on the page linked above. If you want the convenience of not having to enter your OctoPrint API key during setup of the connection between the app and your server, also make sure you install the [OctoPrint AppCompanion Plugin](https://github.com/bq/OctoPrint-AppCompanion) through the Plugin Manager.

If you don’t want to use OctoPi 0.12.0, make sure you have the following:

  * [OctoPrint 1.2](http://www.octoprint.org/download/) or higher (current stable release - note that older releases of OctoPrint will not work). Please refer to the [various setup guides available on OctoPrint’s wiki](https://github.com/foosel/OctoPrint/wiki#assorted-guides) for instructions on how to install that.
  * Optional: OctoPrint Discovery Plugin (bundled with OctoPrint 1.2), set up with pybonjour support [as described on the wiki](https://github.com/foosel/OctoPrint/wiki/Plugin:-Discovery#installing-pybonjour) for the automatic network discovery to work properly.
  * Optional: [OctoPrint AppCompanion Plugin](https://github.com/bq/OctoPrint-AppCompanion) from the plugin repository

## Contributing

Please see the [Contribution Guidelines](./CONTRIBUTING.md) for details on how to contribute to this project.

## About us

This application has been developed by [BQ](http://www.bq.com/) in close collaboration and full alignment with the [OctoPrint](http://www.octoprint.org) project.
