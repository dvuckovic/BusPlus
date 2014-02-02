package com.dvuckovic.busplus;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

/** Dialog activity to quickly edit zone fare from widget **/
public class ZoneDialog extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Instantiate shared preferences
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		// Instantiate Application class and call the setLanguage method
		BusPlus bp = (BusPlus) getApplicationContext();
		bp.setLanguage(prefs.getString("language", "sr"));

		// Sets the view for this activity (dialog)
		setContentView(R.layout.zones);

		// Set the title for dialog
		setTitle(R.string.widget_zone);

		final ArrayList<String> zones = new ArrayList<String>(
				Arrays.asList(getResources().getStringArray(R.array.zones)));

		ArrayAdapter<String> adapter;
		adapter = new ArrayAdapter<String>(this, R.layout.dialog_singlechoice,
				zones);

		ListView widgetZones = (ListView) findViewById(R.id.widgetZones);
		widgetZones.setAdapter(adapter);
		widgetZones.setItemChecked(
				Integer.parseInt(prefs.getString("widget_zone", "0")), true);
		widgetZones.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				int lastZone = getApplicationContext().getResources()
						.getStringArray(R.array.zone_ids).length - 1;
				if (position == lastZone) {
					Intent intent = new Intent(getApplicationContext(),
							FareDialog.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra(FareDialog.EXTRA_ID, Integer.parseInt(prefs
							.getString("widget_zone", "0")));
					getApplicationContext().startActivity(intent);
				} else
					prefs.edit()
							.putString("widget_zone", String.valueOf(position))
							.commit();
				finish();
			}
		});

		Button cancelBtn = (Button) findViewById(R.id.cancelButton);
		cancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
