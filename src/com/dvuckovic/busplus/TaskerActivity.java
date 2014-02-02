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

public class TaskerActivity extends Activity {

	private static final String TASKER_BLURB = "com.twofortyfouram.locale.intent.extra.BLURB";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Instantiate shared preferences
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		// Instantiate Application class and call the setLanguage method
		BusPlus bp = (BusPlus) getApplicationContext();
		bp.setLanguage(prefs.getString("language", "sr"));

		setTitle(R.string.widget_name);

		setContentView(R.layout.balance);

		EditText balanceTxt = (EditText) findViewById(R.id.widget_balance);
		balanceTxt.setVisibility(View.GONE);

		TextView txtView = (TextView) findViewById(R.id.widget_help);
		Button okBtn = (Button) findViewById(R.id.okButton);
		Button cancelBtn = (Button) findViewById(R.id.cancelButton);

		final Intent i = getIntent();

		boolean alreadySet = false;

		try {
			if (i.getStringExtra(TASKER_BLURB).equals("OK")) {
				alreadySet = true;
			}
		} catch (NullPointerException e) {
			// e.printStackTrace();
		}

		if (alreadySet) {

			txtView.setText(R.string.tasker_help);

			// Close activity when clicked on OK button
			okBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
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

		} else {

			txtView.setText(R.string.tasker_integration);

			// Set up on click listener for OK button
			okBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent data = new Intent();
					data.putExtra(TASKER_BLURB, "OK");
					setResult(RESULT_OK, data);
					finish();
				}
			});

			// Close activity when clicked on Cancel
			cancelBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent data = new Intent();
					setResult(RESULT_CANCELED, data);
					finish();
				}
			});

		}
	}

	@Override
	public void onBackPressed() {
		Intent data = new Intent();
		setResult(RESULT_CANCELED, data);
		super.onBackPressed();
	}
}
