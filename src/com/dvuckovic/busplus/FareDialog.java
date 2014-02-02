package com.dvuckovic.busplus;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/** Dialog activity to edit custom zone fare **/
public class FareDialog extends Activity {

	public final static String EXTRA_ID = "com.dvuckovic.busplus.PREVIOUS_ZONE";

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
		setContentView(R.layout.balance);

		// Set the title for dialog
		setTitle(R.string.widget_zone_custom_title);

		final TextView bodyTxt = (TextView) findViewById(R.id.widget_help);
		bodyTxt.setText(R.string.widget_zone_custom_body);

		// Find text box and both buttons
		final EditText fareTxt = (EditText) findViewById(R.id.widget_balance);
		Button okBtn = (Button) findViewById(R.id.okButton);
		Button cancelBtn = (Button) findViewById(R.id.cancelButton);

		// Set box to custom value (from preferences)
		fareTxt.setText(prefs.getString("widget_zone_custom", "0"));

		// Set up on click listener for OK button
		okBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Make sure to set widget zone to custom value
				int lastZone = getApplicationContext().getResources()
						.getStringArray(R.array.zone_ids).length - 1;
				prefs.edit().putString("widget_zone", String.valueOf(lastZone))
						.commit();

				// Edit the balance value in settings
				prefs.edit()
						.putString("widget_zone_custom",
								fareTxt.getText().toString()).commit();

				finish();
			}
		});

		final Intent i = getIntent();

		// Close activity when clicked on Cancel
		cancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int previousZone = i.getIntExtra(EXTRA_ID, -1);
				if (previousZone == -1)
					previousZone = Integer.parseInt(prefs.getString(
							"widget_zone", "0"));

				prefs.edit()
						.putString("widget_zone", String.valueOf(previousZone))
						.commit();

				finish();
			}
		});

	}
}
