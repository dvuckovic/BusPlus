package com.dvuckovic.busplus;

import java.io.IOException;

import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.TabHost;

/** Main app activity responsible for drawing Tab control and content for tabs **/
@SuppressWarnings("deprecation")
public class BusPlusActivity extends TabActivity {

	public final static String SHOW_MAP_TAB = "com.dvuckovic.busplus.SHOW_MAP_TAB";
	static TabHost tabHost;
	private DataBaseHelper helper;
	private SharedPreferences prefs;
	private boolean prefsChanged;

	ShowMapTabReceiver changeTabReceiver;
	IntentFilter changeTabFilter;

	static boolean active = false;

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

		BusPlusActivity.active = true;

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

		changeTabReceiver = new ShowMapTabReceiver();
		changeTabFilter = new IntentFilter(BusPlusActivity.SHOW_MAP_TAB);
		registerReceiver(changeTabReceiver, changeTabFilter);

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
		tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;

		intent = new Intent().setClass(this, StationActivity.class);
		spec = tabHost.newTabSpec("station")
				.setIndicator(getString(R.string.station)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, LocationMap.class);
		spec = tabHost.newTabSpec("location")
				.setIndicator(getString(R.string.location)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, FavoritesListActivity.class);
		spec = tabHost.newTabSpec("favorites")
				.setIndicator(getString(R.string.favorites)).setContent(intent);
		tabHost.addTab(spec);

		final Intent in = getIntent();
		final String action = in.getAction();

		if (SHOW_MAP_TAB.equals(action)) {
			tabHost.setCurrentTab(1);
		} else {
			// Current tab from preferences
			tabHost.setCurrentTab(Integer.parseInt(prefs.getString("start_tab",
					"0")));
		}

	}

	@Override
	protected void onStart() {
		BusPlusActivity.active = true;
		super.onStart();
	}

	@Override
	protected void onStop() {
		BusPlusActivity.active = false;
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		helper.close();
		try {
			unregisterReceiver(changeTabReceiver);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

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

		try {
			registerReceiver(changeTabReceiver, changeTabFilter);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		super.onResume();
	}

	class ShowMapTabReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			tabHost.setCurrentTab(0);
			tabHost.setCurrentTab(1);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(R.string.preferences).setOnMenuItemClickListener(
				new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						startActivity(new Intent(getBaseContext(),
								EditPreferences.class));

						return true;
					}
				});

		menu.add(R.string.info).setOnMenuItemClickListener(
				new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						startActivity(new Intent(getBaseContext(),
								InfoActivity.class));

						return true;
					}
				});

		return super.onCreateOptionsMenu(menu);

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
