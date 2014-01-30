BusPlus
======

<img align="left" src="http://www.dvuckovic.com/sites/default/files/projects/icons/busplus.png" style="padding-right:10px;"/> BusPlus is a small Android app for mass transit system in Belgrade that shows how far is the next bus from your station. As my first full app I used it to teach myself several things about development for Android OS.

Coding for Android is relatively easy and I found all the necessary help by reading various Q&A at [Stack Overflow](http://stackoverflow.com) and by using [this nice book](http://commonsware.com/AdvAndroid/) as a reference. Web is full of examples and tutorials on everything Android, and if you have some idea of what you need you will probably find it.

[![Screenshot #1](http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-1_thumb.jpg "Screenshot #1")](http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-1.jpg) [![Screenshot #2](http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-2_thumb.jpg "Screenshot #2")](http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-2.jpg) [![Screenshot #3](http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-3_thumb.jpg "Screenshot #3")](http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-3.jpg) [![Screenshot #4](http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-4_thumb.jpg "Screenshot #4")](http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-4.jpg) [![Screenshot #5](http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-5_thumb.jpg "Screenshot #5")](http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-5.jpg) [![Screenshot #6](http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-6_thumb.jpg "Screenshot #6")](http://www.dvuckovic.com/sites/default/files/projects/busplus/screenshot-6.jpg) 

BusPlus system
------

At the start of 2012 mass transit in Belgrade, Serbia moved to a new system for payment which added several perks. For example, every vehicle in the system is tracked by GPS and its position is sent to the central computer. To a user this location is available through simple USSD (MMI) service which queries the server and returns information. Of course these locations are coarse (probably for security reasons) and the system can show how many bus stops is a vehicle away from you (your current bus stop). Every bus stop has a unique code which is used as an input for the service. USSD service is available only on three local mobile networks. Service has begun with charging each query since September 16th 2012.

Since there are over 2000 stations in the city, in order to use the service conveniently I came up with an idea for an app. First, I compiled a list of all stations and their codes, along with their geo-coordinates (not an easy task!). Second, I looked into ways to query these USSD codes elegantly. Unfortunately, there is still no USSD API in the works, but I managed to execute them by raising Intent to the system dialer app.

Features
------

BusPlus app has three views and can be used to query the service in several ways:

* manual code entry (if you know it :)
* search by station name (not easy, because most of station names are duplicated for both directions)
* map of the city with your current location and nearby stations
* ability to plot stations on a different location on map
* list of favorites with manual entry and entry from database

App also supports two locales (Serbian and English), which can be switched in app Settings menu.

A few updates followed, in which I managed to squeeze in a several new features (mostly from comments):

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

YouTube
------

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

License
------
Source code is released under [WTFPL license](http://sam.zoy.org/wtfpl/).

> Note: Since this app use Google Maps MapView, you will need your own API key (change it in map.xml layout file).


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