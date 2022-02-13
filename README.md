<p align="center">
  <img src= https://github.com/L-Belluomini/OTN/blob/main/img/OTN1-0_crop_resize.png>
   
<p/>


# OTN

## Introduction
*Open Tak Navigation* is an ATAK plugin that enables offline routing, it based on graphhopper routing engine and uses Open Stree Map data to do the navigation.
The Project is now in beta.

## Note
At the moment OTN can't create a graph for the routing on the phone itself. To create it, check out [OTNcompanion](https://github.com/L-Belluomini/OTN-companion).

## For users

1. On your pc, generate the graphs of the geografical area you need using [OTNcompanion](https://github.com/L-Belluomini/OTN-companion).
2. On the GitHub project page, go to releases and download the .apk file relative to your Atak version.
3. Install the .apk on your smartphone.
4. On the Atak app, go to the plugins list and load OTN.
5. Now that OTN is loaded, go to the routes tool and select *OTN offline routing* as the routing method.
6. Pick a starting point, a destination and a profile (currently either on foot or by car).
7. Success! Here's a cat to celebrate!

![image](https://github.com/L-Belluomini/OTN/blob/main/img/51ZjBEW%2BqNL._AC_SX466_.jpg)

## Current limitations:

* Graphs must be located in \tools\OTN\cache. If you update the graphs, the app itself must be restarted.
* At the moment, the only available routing type is 'best' (which means that it defaults to the quickest type between CH, ALT and STANDARD).
* Bloodhund is implemented, but still unstable.
* Geocode and reverse-geocode are set to the default ones.
* Errors and/or crashes may occur. If so use logcat (adb).

## For developers
The steps to compile repo are based on [this](https://www.ballantyne.online/developing-atak-plugin-101/).
1. Create keystore, using keytool type jks, named *keystore.debug* in \ca
    key alias: androiddebugkey
    keypassword and keystore password: android
2. Open the repo folder in AndroidStudio
3. Install sdk21 (min sdk)
4. Synch gradle project
5. Because of a Proguard limitations, dependency libraries that have module info.class and multirelease.class must be 'hacked':
    Copy the .jar file from the remote repository and remove those two module types, save the file and place it back in the libs directory.
    The hacked libraries aren't on git. To find them, check the build.gradle :app the dependency are listed as a local import.
6. build

## Future features
* Gui (currently at 50%)
* ~~Select routing profile during query~~ DONE
* Local graph building support
* Create layer based on routing area
* ...

## Call for SME
![image](https://github.com/L-Belluomini/OTN/blob/main/img/51oEcOu.jpg)

* ~~[Inflating resource crashes app](https://github.com/L-Belluomini/OTN/issues/1)~~ SOLVED
* ~~[Build on thirdparty pipeline fails proguard](https://github.com/L-Belluomini/OTN/issues/2)~~ SOLVED
The codebase for the moment is instable, commits are made to the main branch.


## Contact
For any issue, suggestion or question, feel free to leave an issue on the [GitHub issues page](https://github.com/L-Belluomini/OTN/issues).

## License
check the [license.md](https://github.com/L-Belluomini/OTN/blob/main/LICENSE)

---

plugin developed and tested on a huawei p10 light (android 8.0 API 26) 4gb ram kirin658 cpu atak version 4.4.0.0 developer build (latest on github @ 2022-02-05)
