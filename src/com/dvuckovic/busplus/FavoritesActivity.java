package com.dvuckovic.busplus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

/** USSD query activity **/
public class FavoritesActivity extends Activity {

	public final static String EXTRA_ID = "com.dvuckovic.busplus._ID";
	public final static String INTENT_NAME = "com.dvuckovic.busplus.CALL_USSD_CODE";

	private SharedPreferences prefs;
	static boolean active = true;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Instantiate preference manager
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Instantiate Application class and call the setLanguage method
		BusPlus bp = (BusPlus) getApplicationContext();
		bp.setLanguage(prefs.getString("language", "sr"));

		FavoritesActivity.active = true;

		final Intent i = getIntent();
		final String action = i.getAction();

		if (INTENT_NAME.equals(action)) {
			String extra = i.getStringExtra(EXTRA_ID);
			if (extra != null)
				callUSSDCode(extra.toString());
		}

	}

	@Override
	protected void onStart() {
		FavoritesActivity.active = true;
		super.onStart();
	}

	@Override
	protected void onStop() {
		FavoritesActivity.active = false;
		super.onStop();
	}

	/**
	 * Executes USSD query using a station code as input by calling Intent on
	 * system call app
	 * 
	 * @param stationCode
	 **/
	private void callUSSDCode(final String stationCode) {
		if (prefs.getBoolean("show_warning_permanent", true)) {
			TelephonyManager manager = (TelephonyManager) getBaseContext()
					.getSystemService(Context.TELEPHONY_SERVICE);
			String carrierName = manager.getNetworkOperatorName();
			String carrierCode = manager.getNetworkOperator();
			String carrierRate = "";

			String[] carrierCodes = getResources().getStringArray(
					R.array.carrier_codes);
			String[] carrierRates = getResources().getStringArray(
					R.array.carrier_rates);
			int i = 0;
			for (String code : carrierCodes) {
				if (code.indexOf(carrierCode) == 0) {
					carrierRate = carrierRates[i];
					break;
				}
				i++;
			}

			String message = "";
			if (carrierRate.equals("")) {
				message = String.format(getString(R.string.warning_unknown),
						carrierName.trim());
			} else {
				message = String.format(getString(R.string.warning_meesage),
						carrierName.trim(), carrierRate);
			}

			// Build and show warning dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.warning_title))
					.setMessage(message)
					.setIcon(R.drawable.launcher_icon)
					.setPositiveButton(getString(R.string.yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									String ussd = "*011*" + stationCode
											+ Uri.encode("#");
									Intent i = new Intent(
											android.content.Intent.ACTION_CALL,
											Uri.parse("tel:" + ussd));
									i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									startActivity(i);

									int sid = Integer.parseInt(stationCode);
									String name = "";

									DataBaseHelper helper = new DataBaseHelper(
											FavoritesActivity.this);
									Cursor c = helper
											.getStationById(stationCode);
									startManagingCursor(c);
									if (c.getCount() > 0) {
										c.moveToFirst();
										name = c.getString(c
												.getColumnIndex("name"));
									}
									stopManagingCursor(c);
									c.close();

									helper.insertHistory(sid, name);
									helper.close();

									finish();
								}
							})
					.setNegativeButton(getString(R.string.no),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									finish();
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
		} else {
			String ussd = "*011*" + stationCode + Uri.encode("#");
			Intent i = new Intent(android.content.Intent.ACTION_CALL,
					Uri.parse("tel:" + ussd));
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);

			int sid = Integer.parseInt(stationCode);
			String name = "";

			DataBaseHelper helper = new DataBaseHelper(this);
			Cursor c = helper.getStationById(stationCode);
			startManagingCursor(c);
			if (c.getCount() > 0) {
				c.moveToFirst();
				name = c.getString(c.getColumnIndex("name"));
			}
			stopManagingCursor(c);
			c.close();

			helper.insertHistory(sid, name);
			helper.close();

			finish();
		}
	}

}
