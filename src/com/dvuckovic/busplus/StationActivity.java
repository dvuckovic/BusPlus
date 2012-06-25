package com.dvuckovic.busplus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/** Station tab activity **/
public class StationActivity extends Activity {

	public final static String STATION_NAME_EXTRA = "com.dvuckovic.busplus.station_name";
	public final static String LINE_ID_EXTRA = "com.dvuckovic.busplus.line_id";
	public final static String LINE_NAME_EXTRA = "com.dvuckovic.busplus.line_name";
	public final static String LINE_SUM_EXTRA = "com.dvuckovic.busplus.line_summary";
	public final static String LINE_DIR_EXTRA = "com.dvuckovic.busplus.line_dir";
	private SharedPreferences prefs;
	private EditText stationCodeTxt;
	private EditText stationNameTxt;
	private EditText lineNameTxt;
	private static final String fields[] = { "name", "desc" };

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Instantiate preference manager
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Sets the view for this activity
		setContentView(R.layout.search);

		stationCodeTxt = (EditText) findViewById(R.id.stationCode);
		stationNameTxt = (EditText) findViewById(R.id.searchText);
		lineNameTxt = (EditText) findViewById(R.id.lineSearchText);

		Button executeBtn = (Button) findViewById(R.id.executeButton);

		// Listen for clicks on execute button
		executeBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				String stationCode = (String) stationCodeTxt.getText()
						.toString();
				if (stationCode.equals("")) {
					showMsgDialog(getString(R.string.station_code),
							getString(R.string.empty_station_code));
				} else {
					// USSD query for entered code
					BusPlus bp = (BusPlus) getApplicationContext();
					bp.callUSSDCode(stationCode);
				}
			}
		});

		Button searchBtn = (Button) findViewById(R.id.searchButton);

		// Listen for clicks on station search button
		searchBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				String stationName = stationNameTxt.getText().toString();

				// Start search activity with station name as an extra
				Intent i = new Intent(StationActivity.this,
						SearchActivity.class);
				i.putExtra(STATION_NAME_EXTRA, stationName);
				startActivity(i);
			}
		});

		Button lineSearchBtn = (Button) findViewById(R.id.lineSearchButton);

		// Listen for clicks on station search button
		lineSearchBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View view) {
				// Instantiate Application class and call the setLanguage method
				BusPlus bp = (BusPlus) getApplicationContext();
				bp.setLanguage(prefs.getString("language", "sr"));

				String lineName = lineNameTxt.getText().toString();

				final DataBaseHelper helper = new DataBaseHelper(view
						.getContext());
				LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View v = li.inflate(R.layout.list, null, false);

				TextView titleText = (TextView) v.findViewById(R.id.title);
				titleText.setVisibility(View.GONE);
				TextView helpText = (TextView) v.findViewById(R.id.help);
				helpText.setVisibility(View.GONE);

				final Dialog listDialog = new Dialog(view.getContext());
				listDialog.setTitle(getString(R.string.choose_line));
				listDialog.setContentView(v);
				listDialog.setCancelable(true);

				// Get all stations in the cursor
				final Cursor l = helper.getLinesByName(lineName);
				startManagingCursor(l);

				if (l.getCount() == 0) {
					// Alert dialog with no results message
					showMsgDialog(getString(R.string.no_results),
							getString(R.string.not_found));
				} else {
					final CursorAdapter lAdapter = new SimpleCursorAdapter(view
							.getContext(), R.layout.row_alt, l, fields,
							new int[] { R.id.itemId, R.id.itemName });

					AbsListView list = (AbsListView) listDialog
							.findViewById(android.R.id.list);
					list.setAdapter(lAdapter);
					list.setFastScrollEnabled(true);

					// Listen for clicks on items in the list
					list.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int arg2, long arg3) {

							// Get id of line clicked and close list dialog
							Cursor cursor = (Cursor) lAdapter.getItem(arg2);
							final String lineId = cursor.getString(cursor
									.getColumnIndex("_id"));
							final String lineName = cursor.getString(cursor
									.getColumnIndex("name"));
							final String lineDescA = cursor.getString(cursor
									.getColumnIndex("desc_a"));
							final String lineDescB = cursor.getString(cursor
									.getColumnIndex("desc_b"));

							// Build and show direction dialog
							AlertDialog.Builder builder = new AlertDialog.Builder(
									arg1.getContext());
							builder.setTitle(
									getString(R.string.choose_direction) + " "
											+ lineName)
									.setMessage(
											"(A) " + lineDescA + "\n\n"
													+ "(B) " + lineDescB)
									.setPositiveButton(
											"A",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int which) {
													dialog.dismiss();
													listDialog.dismiss();
													stopManagingCursor(l);
													l.close();
													helper.close();

													// Start search activity
													// with line id, line name,
													// line summary and A
													// direction as an extra
													Intent i = new Intent(
															StationActivity.this,
															SearchActivity.class);
													i.putExtra(LINE_ID_EXTRA,
															lineId);
													i.putExtra(LINE_NAME_EXTRA,
															lineName);
													i.putExtra(LINE_SUM_EXTRA,
															lineDescA);
													i.putExtra(LINE_DIR_EXTRA,
															"A");
													startActivity(i);

												}
											})
									.setNegativeButton(
											"B",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int which) {
													dialog.dismiss();
													listDialog.dismiss();
													stopManagingCursor(l);
													l.close();
													helper.close();

													// Start search activity
													// with line id, line name,
													// line summary and B
													// direction as an extra
													Intent i = new Intent(
															StationActivity.this,
															SearchActivity.class);
													i.putExtra(LINE_ID_EXTRA,
															lineId);
													i.putExtra(LINE_NAME_EXTRA,
															lineName);
													i.putExtra(LINE_SUM_EXTRA,
															lineDescB);
													i.putExtra(LINE_DIR_EXTRA,
															"B");
													startActivity(i);
												}
											});
							AlertDialog alert = builder.create();
							alert.show();
						}
					});

					listDialog.setOnCancelListener(new OnCancelListener() {

						@Override
						public void onCancel(DialogInterface dialog) {
							stopManagingCursor(l);
							l.close();
							helper.close();
						}
					});

					listDialog.show();
				}
			}
		});

	}

	@Override
	public void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);

		state.putString("station_code", (String) stationCodeTxt.getText()
				.toString());
		state.putString("station_name", (String) stationNameTxt.getText()
				.toString());
		state.putString("line_name", (String) lineNameTxt.getText().toString());
	}

	@Override
	public void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);

		stationCodeTxt.setText(state.getString("station_code"));
		stationNameTxt.setText(state.getString("station_name"));
		lineNameTxt.setText(state.getString("line_name"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Instantiate Application class and call the setLanguage method
		BusPlus bp = (BusPlus) getApplicationContext();
		bp.setLanguage(prefs.getString("language", "sr"));

		new MenuInflater(this).inflate(R.menu.option, menu);

		return (super.onCreateOptionsMenu(menu));
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.clear) {
			// Clear text fields
			stationCodeTxt.setText("");
			stationNameTxt.setText("");
			lineNameTxt.setText("");
			return true;
		} else if (item.getItemId() == R.id.prefs) {
			// Start preferences activity
			startActivity(new Intent(this, EditPreferences.class));
			return true;
		} else if (item.getItemId() == R.id.info) {
			// Start info activity
			startActivity(new Intent(this, InfoActivity.class));
			return true;
		}
		return (super.onOptionsItemSelected(item));
	}

	/**
	 * Show neutral OK dialog to user
	 * 
	 * @param title
	 * @param message
	 **/
	private void showMsgDialog(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title).setMessage(message)
				.setNeutralButton("OK", null);
		AlertDialog alert = builder.create();
		alert.show();
	}

}