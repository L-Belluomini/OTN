<p align="center">
  <img src= https://github.com/MightyBakedPotato/Imagefiles/blob/main/OTN1-0%20crop%20resize.png>
   
<p/>


# OTN

## Introduction
*Open Tak Navigation* is an ATAK plugin that enables offline routing, it based on graphhopper routing engine and uses Open Stree Map data to do the navigation.
the Project is in pre-alpha state, the only feature available right now is the rountig part.

## Note
At the moment OTN can't create a graph for the routing on the phone itself. To create it, check out [OTNcompanion](https://github.com/L-Belluomini/OTN-companion).

## For users
We are sorry, the developer build is the only available one at the moment. Here's a cat to apologize

![image](https://github.com/MightyBakedPotato/Imagefiles/blob/main/1zgnt0qzh9s71.png)

## For developers
The steps to compile repo are based on [this](https://www.ballantyne.online/developing-atak-plugin-101/).
1. Create keystore, using keytool type jks, named *keystore.debug* in \ca
    key alias: androiddebugkey
    keypassword and keystore password: android
2. Open the repo folder in AndroidStudio
3. Install sdk21 (min sdk)
4. Synch gradle project
5. build

## Future features
* Gui
* Select routing profile during query
* Local graph building support
* Create layer based on routing area
* ...

## Call for SME
![image](https://github.com/MightyBakedPotato/Imagefiles/blob/main/51oEcOu.jpg)

There are two breaking issues at the moment:

* [Inflating resource crashes app](https://github.com/L-Belluomini/OTN/issues/1)
* 


## Contact
For any issue, suggestion or question, feel free to leave an issue on the [GitHub issues page](https://github.com/L-Belluomini/OTN/issues).

## License
check the [license.md](https://github.com/L-Belluomini/OTN/blob/main/LICENSE)

---

plugin developed and tested on a huawei p10 light (android 8.0 API 26) 4gb ram kirin658 cpu atak version 4.4.0.0 developer build (latest on github @ 2022-02-05)
