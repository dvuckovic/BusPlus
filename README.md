BusPlus
======

<img align="right" src="http://www.dvuckovic.com/sites/default/files/projects/icons/busplus.png"/>BusPlus is a small Android app that shows how far is the next bus/tram/trolly from your station. It utilizes info service for Belgrade mass transit system.

<a href="http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-1.jpg" target="_blank" title="Screenshot #1"><img src="http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-1_thumb.jpg" alt="Screenshot #1" border="0"/></a> <a href="http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-2.jpg" target="_blank" title="Screenshot #2"><img src="http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-2_thumb.jpg" alt="Screenshot #2" border="0"/></a> <a href="http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-3.jpg" target="_blank" title="Screenshot #3"><img src="http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-3_thumb.jpg" alt="Screenshot #3" border="0"/></a> <a href="http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-4.jpg" target="_blank" title="Screenshot #4"><img src="http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-4_thumb.jpg" alt="Screenshot #4" border="0"/></a> <a href="http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-5.jpg" target="_blank" title="Screenshot #5"><img src="http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-5_thumb.jpg" alt="Screenshot #5" border="0"/></a> <a href="http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-6.jpg" target="_blank" title="Screenshot #6"><img src="http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-6_thumb.jpg" alt="Screenshot #6" border="0"/></a>

How it works
------

It works by sending USSD (MMI) queries into info service. App streamlines these queries by organizing stations and lines that uses them along the way. Of course, locations of vehicles are coarse and the system can only show how many bus stops is a vehicle away from you (your current bus stop). Every bus stop has a unique code which is used as an input for the service. This service is available only on three local mobile networks in Serbia, and is charged accordingly (1.8 RSD per query since September 16th 2012).

Unfortunately, there is still no USSD API for Android in the works, but I managed to execute them by raising Intent to the system dialer app.

Features
------

BusPlus app has three views and can be used to make queries in several ways:

* manual code entry (if you know it :)
* search by station name (not easy, because most of station names are duplicated for both directions)
* map of the city with your current location and nearby stations
* ability to plot station locations on a map
* list of favorites with manual entry and entry from database

App also supports two locales (Serbian and English), which can be switched in app Settings menu.

Several new features have been squeezed in over time (mostly from comments on Play Store):

* more complete station database
* more precise locations for most stations
* new line database (with stations in both directions)
* direct launcher shortcuts
* custom tab positions
* satellite map view
* renaming of favorites
* custom color launcher icons
* support for both Cupcake (1.5) and ICS/JB (4.x)
* option to move app to SD card (2.2+)
* home screen widget with balance
* dismissible charge warning before each query
* suburban stations & lines
* Tasker integration
* ICS API

YouTube
------

Take a look at the app in action:

<a href="http://www.youtube.com/watch?feature=player_embedded&v=a_RA8AqtA94" target="_blank"><img src="http://img.youtube.com/vi/a_RA8AqtA94/0.jpg" 
alt="BusPlus Video" width="320" border="10" /></a>

Download
------

App can be downloaded using Google Play Store, as a free app. Feel free to visit its page if you want to check it out before downloading:

<a href="http://play.google.com/store/apps/details?id=com.dvuckovic.busplus" target="_blank"><img src="http://www.dvuckovic.com/sites/default/files/projects/android_app_on_play_large.png" 
alt="BusPlus Video" border="10" /></a>

Link is safe for Android devices too, because it can be opened in the Market app.

Donations
------

This project was open to optional donations, but because of absolute lack of interest it isn't anymore. I will continue to support and update the app in my free time.

Source
------

App has been written in Java for Android, and compiled using official Android SDK.

> Note: Since this app uses Google Maps MapView, you will need your own API key (just change it in `map.xml` layout file).

    <?xml version="1.0" encoding="utf-8"?>
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="fill_parent"
        android:layout_height="fill_parent"
        android:orientation="vertical" >

        <com.dvuckovic.busplus.MyMapView
            android:id="@+id/mapview"
            android:layout_width="fill_parent"
            android:layout_height="0dip"
            android:layout_weight="1"
            android:apiKey="-----------YOUR_API_KEY_HERE-----------"
            android:clickable="true" />
    ...

License
------

Source code is released under [WTFPL license](http://sam.zoy.org/wtfpl/).
