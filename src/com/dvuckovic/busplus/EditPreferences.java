package com.dvuckovic.busplus;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/** Preference screen activity **/
public class EditPreferences extends PreferenceActivity {

	private SharedPreferences prefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Instantiate preference manager
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Instantiate Application class and call the setLanguage method
		BusPlus bp = (BusPlus) getApplicationContext();
		bp.setLanguage(prefs.getString("language", "sr"));

		// Sets the view for this activity
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public void onResume() {
		super.onResume();

		// Register for changes in preferences
		prefs.registerOnSharedPreferenceChangeListener(onChange);
	}

	@Override
	public void onPause() {
		// Unregister for changes in preferences
		prefs.unregisterOnSharedPreferenceChangeListener(onChange);

		super.onPause();
	}

	/** Listens for changes preferences **/
	SharedPreferences.OnSharedPreferenceChangeListener onChange = new SharedPreferences.OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(SharedPreferences prefs,
				String key) {

			// If language was changed, restart the activity
			if (key.equals("language")) {

				BusPlus bp = (BusPlus) getApplicationContext();
				bp.setLanguage(prefs.getString("language", "sr"));
				finish();
				startActivity(getIntent());
			}

			// If balance or widget type was changed, update the widget
			if (key.equals("widget_balance") || key.equals("widget_type")) {
				Context context = getApplicationContext();
				ComponentName name = new ComponentName(context,
						BalanceWidget.class);
				int[] ids = AppWidgetManager.getInstance(context)
						.getAppWidgetIds(name);

				Intent intent = new Intent(getApplicationContext(),
						BalanceWidget.class);
				intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
				intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
				sendBroadcast(intent);
			}

		}
	};

}
