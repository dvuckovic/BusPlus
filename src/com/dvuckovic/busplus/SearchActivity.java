package com.dvuckovic.busplus;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/** Search results activity **/
public class SearchActivity extends ListActivity {

	private SharedPreferences prefs;
	private DataBaseHelper helper;
	private Cursor c;
	private static final String fields[] = { "_id", "name" };
	private CursorAdapter dataSource;
	private String lineSummary = "";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Instantiate preference manager
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Instantiate Application class and call the setLanguage method
		BusPlus bp = (BusPlus) getApplicationContext();
		bp.setLanguage(prefs.getString("language", "sr"));

		// Sets the view for this activity
		setContentView(R.layout.list);

		if (getIntent().hasExtra(StationActivity.STATION_NAME_EXTRA)) {

			// Get extra with station name for search
			String stationName = getIntent().getStringExtra(
					StationActivity.STATION_NAME_EXTRA);

			// Instantiate helper object for work on database
			helper = new DataBaseHelper(this);

			// Search database for station name
			c = helper.getStationByName(stationName);

			if (c.getCount() == 0) {
				// Alert dialog with no results message
				showMsgDialog(getString(R.string.no_results),
						getString(R.string.not_found), true);
			} else {
				// Text views with title and help hint
				TextView titleText = (TextView) findViewById(R.id.title);
				TextView helpText = (TextView) findViewById(R.id.help);

				if (stationName.equals("")) {
					titleText.setText(getString(R.string.search_results_all)
							+ ":");
				} else {
					titleText.setText(getString(R.string.search_results_for)
							+ " '" + stationName + "':");
				}

				helpText.setText(getString(R.string.help_search));
			}

		} else if (getIntent().hasExtra(StationActivity.LINE_ID_EXTRA)
				&& getIntent().hasExtra(StationActivity.LINE_NAME_EXTRA)
				&& getIntent().hasExtra(StationActivity.LINE_SUM_EXTRA)
				&& getIntent().hasExtra(StationActivity.LINE_DIR_EXTRA)) {

			// Get extras with line id and dir
			String lineId = getIntent().getStringExtra(
					StationActivity.LINE_ID_EXTRA);
			String lineName = getIntent().getStringExtra(
					StationActivity.LINE_NAME_EXTRA);
			lineSummary = getIntent().getStringExtra(
					StationActivity.LINE_SUM_EXTRA);
			String lineDir = getIntent().getStringExtra(
					StationActivity.LINE_DIR_EXTRA);

			// Instantiate helper object for work on database
			helper = new DataBaseHelper(this);

			// Search database for station name
			c = helper.getStationsByLine(lineId, lineDir);

			if (c.getCount() == 0) {
				// Alert dialog with no results message
				showMsgDialog(getString(R.string.no_results),
						getString(R.string.not_found), true);
			} else {
				// Text views with title and help hint
				TextView titleText = (TextView) findViewById(R.id.title);
				titleText.setText(getString(R.string.stations_for_line) + " "
						+ lineName + " " + getString(R.string.in_dir) + " "
						+ lineDir + ":");
				TextView helpText = (TextView) findViewById(R.id.help);
				helpText.setText(getString(R.string.help_search));
			}

		} else
			finish();

		// Start managing Cursor
		startManagingCursor(c);

		// Create and bind adapter to the list
		dataSource = new SimpleCursorAdapter(this, R.layout.row, c, fields,
				new int[] { R.id.itemId, R.id.itemName });

		getListView().setFastScrollEnabled(true);

		setListAdapter(dataSource);

		// Listen for any context menus on list view
		registerForContextMenu(getListView());

	}

	public void onDestroy() {
		super.onDestroy();

		stopManagingCursor(c);
		c.close();
		helper.close();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Cursor cursor = (Cursor) dataSource.getItem(info.position);
		final int stationId = cursor.getInt(cursor.getColumnIndex("_id"));
		final String stationName = cursor.getString(cursor.getColumnIndex("name"));

		menu.setHeaderTitle("["+Integer.toString(stationId)+"] "+stationName);

		// Add to favorites context menu
		menu.add(0, 0, 1, getString(R.string.add_to_favorites))
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						// Insert station to favorites and toast a message
						String suffix = "";
						if (!lineSummary.equals(""))
							suffix = " ("
									+ lineSummary.replaceAll("\\s+/.*?/", "")
									+ ")";
						helper.insertFavorite(stationId, stationName + suffix);
						BusPlus bp = (BusPlus) getApplicationContext();
						bp.showToastMessage(getString(R.string.favorite_added_to_list));
						return true;
					}
				});

		// Query context menu
		menu.add(0, 1, 3, getString(R.string.query))
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						// USSD query of station code
						BusPlus bp = (BusPlus) getApplicationContext();
						bp.callUSSDCode(Integer.toString(stationId));
						return true;
					}
				});

		// Shortcut context menu
		menu.add(0, 2, 2, getString(R.string.launcher_shortcut))
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						// Create shortcut on home launcher
						String suffix = "";
						if (!lineSummary.equals(""))
							suffix = " ("
									+ lineSummary.replaceAll("\\s+/.*?/", "")
									+ ")";
						BusPlus bp = (BusPlus) getApplicationContext();
						bp.setupShortcut(Integer.toString(stationId), stationName + suffix);
						return true;
					}
				});

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public void onListItemClick(ListView list, View view, int position, long id) {
		super.onListItemClick(list, view, position, id);

		TextView itemId = (TextView) view.findViewById(R.id.itemId);
		String stationCode = (String) itemId.getText().toString();

		// USSD query of station pressed
		if (!stationCode.equals("")) {
			BusPlus bp = (BusPlus) getApplicationContext();
			bp.callUSSDCode(stationCode);
		}

		// Close this activity and return to previous, our work here is probably
		// done
		finish();
	}

	/**
	 * Show message dialog and terminate activity if finishThis parameter is
	 * true
	 * 
	 * @param title
	 * @param message
	 * @param finishThis
	 **/
	public void showMsgDialog(String title, String message,
			final boolean finishThis) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title).setMessage(message)
				.setNeutralButton("OK", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (finishThis)
							finish();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

}
