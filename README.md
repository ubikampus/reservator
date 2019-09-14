UbiKampus Reservator
==========

<img src="images/FreeEmpty.png" width="600">

How to get it
--------------

Not available in Google Play yet. Download and install the latest .apk from the releases page: https://github.com/ubikampus/reservator/releases

Description
-----------
UbiKampus Reservator is an Android application for displaying and reserving meeting rooms, developed by Futurice and the University of Helsinki. 

UbiKampus Reservator is a fork of the original Futurice Reservator at https://github.com/futurice/meeting-room-tablet. New features include support for Office365 calendars and a new UI. 

This application is meant for tablets so there are a few layout problems when using it on mobile phones.

How it works
-----------
The Reservator app does not directly communitate with cloud-based calendar services. Instead, it synchronizes itself with the Android Calendar App, which in turn takes care of the cloud-synchronization.

Installation
-----------

1. Configure the Android Calendar App to synchronize itself with the cloud-based calendar of your choice (Google calendar or Office365). In order to do this, go to Android Settings - Accounts and add your calendar account.

<img src="images/selectOffice365.png" width="200" height="300"> <img src="images/SignInYliopisto.png" width="400">

2. Using the Android Calendar App or the user interface of your cloud-based calendar, create a calendar for the meeting room you want to reserve with the Reservator app. Check that the events you create in the UI of your cloud-based calendar get synchronized in the Android calendar app.

3. Install the Reservator app. Upon first startup of the app, permission must be granted to allow access to contacts and calendar (both are required), then choose the calendar you created in step 2. Since reservator is meant to be run in public spaces, there is no UI for changing this calendar selection after you have initially set it up. If you want to switch the calendar at a later point, please uninstall the Reservator app, and re-install it. 

<img src="images/SelectCalendarAccount.png" width="300">

Installation through ADB
-----------

Use option -g to grant the app all the required permissions, and option -r to install without
uninstalling the previous version first:

   `adb install -g -r reservator.apk`

You can also configure the app from ADB:

   `adb shell am start -a android.intent.action.VIEW -d "reservator://change.reservator.settings?account=calendar@example.com\&default_room=test-room1\&lang=fi" com.futurice.android.reservator/.RemoteConfigActivity`

The config variables are:

1. account
2. default_room
3. lang
4. max_duration (minutes)
5. default_duration (minutes)
6. room_display_name
7. closing_time (do not allow reservations extend beyond this time, HH:mm)
8. mqtt_server_address the address of the mqtt server to push reservation status updates to
9. mqtt_prefix the prefix for the mqtt topics for reservations status updates

To enable Mqtt status updates, set variables 8 and 9, and copy an EC private key to
"/data/data/com.futurice.android.reservator/tablets-private-key.pem"

The errors go to the Android log and can be read by adb logcat.

Language config only succeeds if the localization is available.

Duration config fails if it's not a number, or if both durations are given and default is longer than max. If for example only the default duration is given and it is higher than the existing max, it gets raised up to the existing max.

Account config fails is the account is not available.

Room config does not fail even if the room is not available.

If both the account (verified) and room (unverified) are set, the app considers itself configured. You can change the configuration later through adb.

You can also open the config wizard if you want to reconfigure the app manually. Just run the same adb command without any config variables.

   `adb shell am start -a android.intent.action.VIEW -d "reservator://change.reservator.settings" com.futurice.android.reservator/.RemoteConfigActivity`

If you reconfigure the app while it is running, the changes will take effect on restart.

Kiosk Mode
----------

1. Make sure there are no accounts on the device. (Test whether this is even necessary with Outlook accounts).

2. Install the reservator

  `adb install -g reservator.apk`

3. Make the reservator the device owner:

  `adb shell dpm set-device-owner com.futurice.android.reservator/.MyAdmin`

4. Add the necessary accounts.

5. Start the reservator.

6. Make the reservator the home app in the settings.

7. Turn the kiosk mode on:

  `adb shell am broadcast -a "com.futurice.android.reservator.KIOSK_ON" -n com.futurice.android.reservator/.KioskStateReceiver`


The kiosk mode can be turned off:

  `adb shell am broadcast -a "com.futurice.android.reservator.KIOSK_OFF" -n com.futurice.android.reservator/.KioskStateReceiver`

  
