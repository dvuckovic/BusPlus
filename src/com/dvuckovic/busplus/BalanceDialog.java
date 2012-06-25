package com.dvuckovic.busplus;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/** Dialog activity to quickly edit balance value from widget **/
public class BalanceDialog extends Activity {
	
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
		setTitle(R.string.widget_name);

		// Find text box and both buttons
		final EditText balanceTxt = (EditText) findViewById(R.id.widget_balance);
		Button okBtn = (Button) findViewById(R.id.okButton);
		Button cancelBtn = (Button) findViewById(R.id.cancelButton);

		// Set box to current balance (from settings)
		balanceTxt.setText(prefs.getString("widget_balance", "600"));

		// Set up on click listener for OK button
		okBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Edit the balance value in settings
				prefs.edit()
						.putString("widget_balance",
								balanceTxt.getText().toString()).commit();

				// Update widget using broadcast intent
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
				finish();
			}
		});

		// Close activity when clicked on Cancel
		cancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
