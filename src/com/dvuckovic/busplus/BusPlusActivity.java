package com.dvuckovic.busplus;

import java.io.IOException;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.SQLException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TabHost;

/** Main app activity responsible for drawing Tab control and content for tabs **/
public class BusPlusActivity extends TabActivity {

	static TabHost tabHost;
	private DataBaseHelper helper;
	private SharedPreferences prefs;
	private boolean prefsChanged;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Instantiate preference manager and register for any changes in the
		// preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(prefListener);

		// Instantiate Application class and call the setLanguage method
		BusPlus bp = (BusPlus) getApplicationContext();
		bp.setLanguage(prefs.getString("language", "sr"));

		// Sets the view for this activity
		switch (Integer.parseInt(prefs.getString("tabs_position", "1"))) {
		case 2:
			setContentView(R.layout.bottom);
			break;
		case 1:
		default:
			setContentView(R.layout.main);
			break;
		}

		// Instantiate helper object for work on database
		helper = new DataBaseHelper(this);

		// Setup database and try to open it
		try {
			helper.createDataBase();
		} catch (IOException ioe) {
			throw new Error("Unable to create database");
		}

		try {
			helper.openDataBase();
		} catch (SQLException sqle) {
			throw sqle;
		}

		// Setup tab contents using different activities
		Resources res = getResources();
		tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;

		intent = new Intent().setClass(this, StationActivity.class);
		spec = tabHost
				.newTabSpec("station")
				.setIndicator(getString(R.string.station),
						res.getDrawable(R.drawable.ic_tab_station))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, LocationMap.class);
		spec = tabHost
				.newTabSpec("location")
				.setIndicator(getString(R.string.location),
						res.getDrawable(R.drawable.ic_tab_location))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, FavoritesActivity.class);
		spec = tabHost
				.newTabSpec("favorites")
				.setIndicator(getString(R.string.favorites),
						res.getDrawable(R.drawable.ic_tab_favorites))
				.setContent(intent);
		tabHost.addTab(spec);

		// Current tab from preferences
		tabHost.setCurrentTab(Integer.parseInt(prefs
				.getString("start_tab", "0")));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		helper.close();
	}

	/**
	 * Overrides onResume method and check if the language has changed. If it
	 * did set global language in app.
	 **/
	@Override
	protected void onResume() {
		if (prefsChanged) {
			BusPlus bp = (BusPlus) getApplicationContext();
			bp.setLanguage(prefs.getString("language", "sr"));
			prefsChanged = false;
			finish();
			startActivity(getIntent());
		}

		super.onResume();
	}

	/** Listens for pref changes and sets variable if language has changed **/
	private SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			if (key.equals("language") || key.equals("tabs_position")
					|| key.equals("map_satellite") || key.equals("sort_by")) {
				prefsChanged = true;
			}
		}
	};

}
