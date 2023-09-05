<p align="center">
  <img src= https://github.com/L-Belluomini/OTN/blob/main/app/src/main/res/drawable/otn_logo_shield.png width="200" height="200">
   
<p/>


# OTN

## Introduction

*Open Tak Navigation* is an open source project for the TAK environment.

The project started back when the VNS (Vehicle Navigation Sytem) was not yet publicly available, with the same prime objective to deliver an offline routing instrument for the ATAK app.
OTN manages this making use of [Open Street Map](https://www.openstreetmap.org) data and the [GraphHopper](https://www.graphhopper.com/) routing engine, but the project aims to integrate many other improvements and features.

The project is comprised of two parts, *OTN*, the main plug-in for the ATAK app, and *[OTN-C](https://github.com/L-Belluomini/OTN-companion) (OTN-Companion)*, a computer program to generate the graphs needed for the routing.

**A new release is on the way. The project is soon to be out of beta.**

## Note

OTN can't directly generate a graph for the routing on the phone itself. For this purpose, check out [OTN-C](https://github.com/L-Belluomini/OTN-companion).

## For users

1. On your pc, generate the graphs of the geografical area you need using [OTN-C](https://github.com/L-Belluomini/OTN-companion).
2. On the GitHub project page, go to releases and download the .apk file relative to your Atak version.
3. Install the .apk on your smartphone.
4. On the Atak app, go to the plugins list and load OTN.
5. Now that OTN is loaded, go to the routes tool and select *OTN offline routing* as the routing method.
6. Pick a starting point, a destination and a profile.
7. Success! Here's a cat to celebrate!

![image](https://github.com/L-Belluomini/OTN/blob/main/img/51ZjBEW%2BqNL._AC_SX466_.jpg)

## Current features:

* Route around regions has been implemented.
* Routing through waypoints is now possible.
* You can store multiple graphs on your device and easily pick one for each route in app.
* The routing engine has been updated from GraphHopper 4 to GraphHopper 6.
* New GUI realized following Arsenal guidelines.
* The graph area can now be displayed as an overlay (note: the graph must contain its polyline).
* New preference menu.
* It is now possible to toggle the routing mode between memory mode and flash mode.

## Future Features:

* 3D routing calculations using DTED.
* Support for online routing engines.
* Different vehicles recogntion from the ATAK route system. 

## Current limitations:

* Graphs must be located in *atak\tools\OTN\graphs*.
* Bloodhund is implemented and should be stable, but further testing is required.
* Geocode and reverse-geocode are set to the Atak default ones, which from version 4.8.1 and above have been reported to cause some issues on some devices.
* Errors and/or crashes may still occur. If so enable logcat and kindly share the logs with us. Logs ar found at *atak\support\logs*. We will do our best to fix all bugs.

## For developers

The steps to compile repo are based on [this](https://www.ballantyne.online/developing-atak-plugin-101/).
1. Create keystore, using keytool type jks, named *keystore.debug* in \ca
    key alias: androiddebugkey
    keypassword and keystore password: android
2. Open the repo folder in AndroidStudio.
3. Install sdk21 (min sdk).
4. Synch gradle project.
5. Because of a Proguard limitations, dependency libraries that have module info.class and multirelease.class must be 'hacked':
    Copy the .jar file from the remote repository and remove those two module types, save the file and place it back in the libs directory.
    The hacked libraries aren't on git. To find them, check the build.gradle :app the dependency are listed as a local import.
6. Build.

## Contact

For any issue, suggestion or question, feel free to leave an issue on the [GitHub issues page](https://github.com/L-Belluomini/OTN/issues).

## License

check the [license.md](https://github.com/L-Belluomini/OTN/blob/main/LICENSE)

---

plugin developed and tested on a huawei p10 light (android 8.0 API 26) 4gb ram kirin658 cpu atak version 4.7.0.0 developer build (28/07/2022).
