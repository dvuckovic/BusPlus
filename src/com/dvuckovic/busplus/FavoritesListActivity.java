package com.dvuckovic.busplus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/** Favorites tab activity **/
public class FavoritesListActivity extends ListActivity {

	private SharedPreferences prefs;
	private Cursor c;
	private CursorAdapter cAdapter;
	private Cursor s;
	private CursorAdapter sAdapter;
	private DataBaseHelper helper;
	private static final String fields[] = { "_id", "name" };
	private static final String fields_line[] = { "name", "desc" };
	private EditText stationCodeTxt;
	private EditText stationNameTxt;
	private Dialog listDialog;
	private String lineSummary = "";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Instantiate preference manager
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Instantiate Application class and call the setLanguage method
		BusPlus bp = (BusPlus) getApplicationContext();
		bp.setLanguage(prefs.getString("language", "sr"));

		// Sets the view for this activity
		setContentView(R.layout.favorites);

		Button addBtn = (Button) findViewById(R.id.addButton);
		addBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View view) {
				// Inflate dialog with two fields
				LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				View v = li.inflate(R.layout.add, null, false);

				listDialog = new Dialog(view.getContext());
				listDialog.setTitle(getString(R.string.add));
				listDialog.setContentView(v);
				listDialog.setCancelable(true);

				stationCodeTxt = (EditText) listDialog
						.findViewById(R.id.stationCode);
				stationNameTxt = (EditText) listDialog
						.findViewById(R.id.stationName);

				Button addButton = (Button) listDialog
						.findViewById(R.id.addButton);

				// Listens for Add button presses
				addButton.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						String stationCode = (String) stationCodeTxt.getText()
								.toString();
						String stationName = (String) stationNameTxt.getText()
								.toString();

						// Check if we have any empty fields
						if (stationCode.equals("")) {
							showMsgDialog(getString(R.string.station_code),
									getString(R.string.blank_station_code));
						} else {
							if (stationName.equals("")) {
								showMsgDialog(getString(R.string.station_name),
										getString(R.string.blank_station_name));
							} else {
								// Finally add new Favorite and refresh list
								helper.insertFavorite(
										Integer.parseInt(stationCode),
										stationName);
								listDialog.dismiss();
								c.requery();
							}
						}
					}
				});

				Button cancelButton = (Button) listDialog
						.findViewById(R.id.cancelButton);

				// Listens for Cancel button presses
				cancelButton.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						listDialog.cancel();
					}
				});

				// Show dialog
				listDialog.show();
			}
		});

		Button listBtn = (Button) findViewById(R.id.listButton);
		listBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View view) {
				// Inflate dialog with list view
				LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				View v = li.inflate(R.layout.list, null, false);

				TextView titleText = (TextView) v.findViewById(R.id.title);
				titleText.setVisibility(View.GONE);
				TextView helpText = (TextView) v.findViewById(R.id.help);
				helpText.setVisibility(View.GONE);

				listDialog = new Dialog(view.getContext());
				listDialog.setTitle(getString(R.string.choose_station));
				listDialog.setContentView(v);
				listDialog.setCancelable(true);

				// Get all stations in the cursor
				s = helper.getAllStations();
				startManagingCursor(s);

				// Set up a list adapter with two fields
				sAdapter = new SimpleCursorAdapter(view.getContext(),
						R.layout.row, s, fields, new int[] { R.id.itemId,
								R.id.itemName });

				AbsListView list = (AbsListView) listDialog
						.findViewById(android.R.id.list);
				list.setAdapter(sAdapter);
				list.setFastScrollEnabled(true);

				// Listen for clicks on items in the list
				list.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						Cursor cursor = (Cursor) sAdapter.getItem(arg2);
						int stationId = cursor.getInt(cursor
								.getColumnIndex("_id"));
						String stationName = cursor.getString(cursor
								.getColumnIndex("name"));

						// Insert favorite and enable option menu and refresh
						// list items
						helper.insertFavorite(stationId, stationName);
						listDialog.dismiss();
						c.requery();
					}
				});

				listDialog.show();
			}
		});

		Button lineBtn = (Button) findViewById(R.id.lineButton);
		lineBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View view) {
				// Inflate dialog with list view
				final LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View v = li.inflate(R.layout.list, null, false);

				TextView titleText = (TextView) v.findViewById(R.id.title);
				titleText.setVisibility(View.GONE);
				TextView helpText = (TextView) v.findViewById(R.id.help);
				helpText.setVisibility(View.GONE);

				final Dialog listDialog = new Dialog(view.getContext());
				listDialog.setTitle(getString(R.string.choose_line));
				listDialog.setContentView(v);
				listDialog.setCancelable(true);

				// Get all stations from a line in the cursor
				final Cursor l = helper.getLinesByName("");
				startManagingCursor(l);

				// Set up a list adapter with two fields
				final CursorAdapter lAdapter = new SimpleCursorAdapter(view
						.getContext(), R.layout.row_alt, l, fields_line,
						new int[] { R.id.itemId, R.id.itemName });

				AbsListView list = (AbsListView) listDialog
						.findViewById(android.R.id.list);
				list.setAdapter(lAdapter);
				list.setFastScrollEnabled(true);

				list.setOnScrollListener(new OnScrollListener() {

					@Override
					public void onScrollStateChanged(AbsListView view,
							int scrollState) {
						//
					}

					@Override
					public void onScroll(AbsListView view,
							int firstVisibleItem, int visibleItemCount,
							int totalItemCount) {
						int childCount = view.getChildCount();

						for (int i = 0; i < childCount; i++) {
							View v = view.getChildAt(i);
							TextView lineNumber = (TextView) v
									.findViewById(R.id.itemId);
							lineNumber.setBackgroundColor(BusPlus
									.getBackgroundColor(lineNumber.getText()
											.toString()));
							lineNumber.setShadowLayer(3, 0, 0,
									Color.rgb(0, 0, 0));
						}
					}
				});

				// Listen for clicks on items in the list
				list.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0,
							final View arg1, int arg2, long arg3) {

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
										"(A) " + lineDescA + "\n\n" + "(B) "
												+ lineDescB)
								.setNegativeButton("A",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												dialog.dismiss();
												listDialog.dismiss();
												stopManagingCursor(l);
												l.close();

												// Inflate dialog with list view
												View v = li.inflate(
														R.layout.list, null,
														false);

												TextView titleText = (TextView) v
														.findViewById(R.id.title);
												titleText
														.setVisibility(View.GONE);
												TextView helpText = (TextView) v
														.findViewById(R.id.help);
												helpText.setVisibility(View.GONE);

												final Dialog listDialog = new Dialog(
														arg1.getContext());
												listDialog
														.setTitle(getString(R.string.choose_station));
												listDialog.setContentView(v);
												listDialog.setCancelable(true);

												// Get all stations in the
												// cursor
												s = helper.getStationsByLine(
														lineId, "A");
												startManagingCursor(s);

												// Set up a list adapter with
												// two
												// fields
												sAdapter = new SimpleCursorAdapter(
														arg1.getContext(),
														R.layout.row, s,
														fields, new int[] {
																R.id.itemId,
																R.id.itemName });

												AbsListView list = (AbsListView) listDialog
														.findViewById(android.R.id.list);
												list.setAdapter(sAdapter);
												list.setFastScrollEnabled(true);

												// Listen for clicks on items in
												// the
												// list
												list.setOnItemClickListener(new OnItemClickListener() {
													public void onItemClick(
															AdapterView<?> arg0,
															View arg1,
															int arg2, long arg3) {
														Cursor cursor = (Cursor) sAdapter
																.getItem(arg2);
														int stationId = cursor.getInt(cursor
																.getColumnIndex("_id"));
														String stationName = cursor
																.getString(cursor
																		.getColumnIndex("name"));

														// Insert favorite and
														// enable option menu
														// and
														// refresh list
														// items
														lineSummary = lineDescA;
														String suffix = " ("
																+ lineSummary
																		.replaceAll(
																				"\\s+/.*?/",
																				"")
																+ ")";
														helper.insertFavorite(
																stationId,
																stationName
																		+ suffix);

														listDialog.dismiss();
														c.requery();
													}
												});

												listDialog.show();
											}
										})
								.setPositiveButton("B",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												dialog.dismiss();
												listDialog.dismiss();
												stopManagingCursor(l);
												l.close();

												// Inflate dialog with list view
												View v = li.inflate(
														R.layout.list, null,
														false);

												TextView titleText = (TextView) v
														.findViewById(R.id.title);
												titleText
														.setVisibility(View.GONE);
												TextView helpText = (TextView) v
														.findViewById(R.id.help);
												helpText.setVisibility(View.GONE);

												final Dialog listDialog = new Dialog(
														arg1.getContext());
												listDialog
														.setTitle(getString(R.string.choose_station));
												listDialog.setContentView(v);
												listDialog.setCancelable(true);

												// Get all stations in the
												// cursor
												s = helper.getStationsByLine(
														lineId, "B");
												startManagingCursor(s);

												// Set up a list adapter with
												// two
												// fields
												sAdapter = new SimpleCursorAdapter(
														arg1.getContext(),
														R.layout.row, s,
														fields, new int[] {
																R.id.itemId,
																R.id.itemName });

												AbsListView list = (AbsListView) listDialog
														.findViewById(android.R.id.list);
												list.setAdapter(sAdapter);
												list.setFastScrollEnabled(true);

												// Listen for clicks on items in
												// the
												// list
												list.setOnItemClickListener(new OnItemClickListener() {
													public void onItemClick(
															AdapterView<?> arg0,
															View arg1,
															int arg2, long arg3) {
														Cursor cursor = (Cursor) sAdapter
																.getItem(arg2);
														int stationId = cursor.getInt(cursor
																.getColumnIndex("_id"));
														String stationName = cursor
																.getString(cursor
																		.getColumnIndex("name"));

														// Insert favorite and
														// enable option menu
														// and
														// refresh list
														// items
														lineSummary = lineDescB;
														String suffix = " ("
																+ lineSummary
																		.replaceAll(
																				"\\s+/.*?/",
																				"")
																+ ")";
														helper.insertFavorite(
																stationId,
																stationName
																		+ suffix);

														listDialog.dismiss();
														c.requery();
													}
												});

												listDialog.show();
											}
										});
						AlertDialog alert = builder.create();
						alert.show();
					}
				});

				listDialog.show();
			}
		});

		// Instantiate helper object for work on database
		helper = new DataBaseHelper(this);

		// Initialize and populate list view
		initList();

		// Registers for any context menus (long taps) on list view
		registerForContextMenu(getListView());
	}

	/** Perform any final cleanup before an activity is destroyed. */
	@Override
	public void onDestroy() {
		super.onDestroy();

		stopManagingCursor(c);
		c.close();
		helper.close();
	}

	/** Refresh list on resume of this activity **/
	@Override
	protected void onResume() {
		c.requery();

		super.onResume();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, final View v,
			ContextMenuInfo menuInfo) {

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Cursor cursor = (Cursor) cAdapter.getItem(info.position);
		final String stationId = cursor.getString(cursor.getColumnIndex("_id"));
		final String stationName = cursor.getString(cursor
				.getColumnIndex("name"));

		menu.setHeaderTitle("[" + stationId + "] " + stationName);

		// Add remove option to context menu
		menu.add(0, 3, 2, getString(R.string.rename))
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {

						AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
								.getMenuInfo();
						Cursor cursor = (Cursor) cAdapter
								.getItem(info.position);
						final String stationId = cursor.getString(cursor
								.getColumnIndex("_id"));
						String stationName = cursor.getString(cursor
								.getColumnIndex("name"));

						// Inflate dialog with two fields
						LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						View view = li.inflate(R.layout.add, null, false);

						listDialog = new Dialog(v.getContext());
						listDialog.setTitle(getString(R.string.rename));
						listDialog.setContentView(view);
						listDialog.setCancelable(true);

						stationCodeTxt = (EditText) listDialog
								.findViewById(R.id.stationCode);
						stationCodeTxt.setText(stationId);
						stationCodeTxt.setEnabled(false);
						stationCodeTxt.setFocusable(false);

						stationNameTxt = (EditText) listDialog
								.findViewById(R.id.stationName);
						stationNameTxt.setText(stationName);

						Button renameButton = (Button) listDialog
								.findViewById(R.id.addButton);
						renameButton.setText(getString(R.string.rename));

						// Listens for Rename button presses
						renameButton.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								String stationName = (String) stationNameTxt
										.getText().toString();

								// Check if we have an empty field
								if (stationName.equals("")) {
									showMsgDialog(
											getString(R.string.station_name),
											getString(R.string.blank_station_name));
								} else {
									// Finally rename Favorite and refresh list
									helper.renameFavorite(stationId,
											stationName);
									listDialog.dismiss();
									c.requery();
								}
							}
						});

						Button cancelButton = (Button) listDialog
								.findViewById(R.id.cancelButton);

						// Listens for Cancel button presses
						cancelButton.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								listDialog.cancel();
							}
						});

						// Show dialog
						listDialog.show();
						return true;
					}
				});

		// Add remove option to context menu
		menu.add(0, 0, 3, getString(R.string.remove))
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
								.getMenuInfo();
						Cursor cursor = (Cursor) cAdapter
								.getItem(info.position);
						String stationId = cursor.getString(cursor
								.getColumnIndex("_id"));

						// Remove favorite which was pressed and refresh list
						helper.removeFavorite(stationId);
						c.requery();
						return true;
					}
				});

		// Add query option to context menu
		menu.add(0, 1, 4, getString(R.string.query))
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
								.getMenuInfo();
						Cursor cursor = (Cursor) cAdapter
								.getItem(info.position);
						String stationCode = cursor.getString(cursor
								.getColumnIndex("_id"));

						// USSD query with station code pressed
						Intent i = new Intent(FavoritesListActivity.this,
								FavoritesActivity.class);
						i.putExtra(FavoritesActivity.EXTRA_ID, stationCode);
						i.setAction(FavoritesActivity.INTENT_NAME);
						startActivity(i);
						return true;
					}
				});

		// Add shortcut option to context menu
		menu.add(0, 2, 1, getString(R.string.launcher_shortcut))
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
								.getMenuInfo();
						Cursor cursor = (Cursor) cAdapter
								.getItem(info.position);
						String stationCode = cursor.getString(cursor
								.getColumnIndex("_id"));
						String stationName = cursor.getString(cursor
								.getColumnIndex("name"));

						// Setup launcher shortcut
						BusPlus bp = (BusPlus) getApplicationContext();
						bp.setupShortcut(stationCode, stationName);
						return true;
					}
				});

		// Add show on map context menu
		menu.add(0, 4, 5, getString(R.string.show_map))
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						if (!BusPlusActivity.active) {
							BusPlus.showStationId = String.valueOf(stationId);
							Intent i = new Intent(getBaseContext(),
									BusPlusActivity.class);
							i.setAction(BusPlusActivity.SHOW_MAP_TAB);
							startActivity(i);
						} else {
							BusPlus.showStationId = String.valueOf(stationId);
							Intent i = new Intent(BusPlusActivity.SHOW_MAP_TAB);
							sendBroadcast(i);
						}

						return true;
					}
				});

		menu.add(0, 5, 6, getString(R.string.station_info))
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						// Show dialog with options on tap
						AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
								.getMenuInfo();
						Cursor cursor = (Cursor) cAdapter
								.getItem(info.position);
						final String stationCode = cursor.getString(cursor
								.getColumnIndex("_id"));
						final String stationName = cursor.getString(cursor
								.getColumnIndex("name"));

						AlertDialog.Builder builder = new AlertDialog.Builder(
								FavoritesListActivity.this);

						Cursor cur = helper.getLinesByStation(stationCode);
						startManagingCursor(cur);

						String[] lines = new String[cur.getCount()];
						int k = 0;

						if (cur.getCount() != 0) {
							cur.moveToFirst();
							while (cur.isAfterLast() == false) {
								lines[k] = cur.getString(cur
										.getColumnIndex("name"));
								k++;
								cur.moveToNext();
							}
						}

						stopManagingCursor(cur);
						cur.close();

						LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						View v = li.inflate(R.layout.message, null, false);

						TextView stationNameView = (TextView) v
								.findViewById(R.id.stationName);
						stationNameView.setTextAppearance(
								FavoritesListActivity.this,
								android.R.style.TextAppearance_Medium);
						stationNameView.setText("[" + stationCode + "] "
								+ stationName);

						FlowLayout fl = (FlowLayout) v
								.findViewById(R.id.flowLayout);

						for (String line : lines) {

							TextView lineTextView = new TextView(
									FavoritesListActivity.this);
							lineTextView.setTextAppearance(
									FavoritesListActivity.this,
									android.R.style.TextAppearance_Medium);
							lineTextView.setBackgroundColor(BusPlus
									.getBackgroundColor(line));
							lineTextView.setShadowLayer(3, 0, 0,
									Color.rgb(0, 0, 0));
							lineTextView.setText(" " + line + " ");

							fl.addView(lineTextView);
						}

						builder.setTitle(getString(R.string.station))
								.setView(v)
								// .setMessage(tapName + " (" + tapCode + ")")
								.setNegativeButton(getString(R.string.query),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												// USSD query
												Intent i = new Intent(
														FavoritesListActivity.this,
														FavoritesActivity.class);
												i.putExtra(
														FavoritesActivity.EXTRA_ID,
														stationCode);
												i.setAction(FavoritesActivity.INTENT_NAME);
												startActivity(i);
												dialog.dismiss();
											}
										})
								.setNeutralButton(
										getString(R.string.favorites),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												String suffix = "";
												if (!lineSummary.equals(""))
													suffix = " ("
															+ lineSummary
																	.replaceAll(
																			"\\s+/.*?/",
																			"")
															+ ")";

												// Add favorite and toast a
												// message
												helper.insertFavorite(Integer
														.parseInt(stationCode),
														stationName + suffix);
												BusPlus bp = (BusPlus) getApplicationContext();
												bp.showToastMessage(getString(R.string.favorite_added_to_list));
											}
										})
								.setPositiveButton(
										getString(R.string.shortcut),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												String suffix = "";
												if (!lineSummary.equals(""))
													suffix = " ("
															+ lineSummary
																	.replaceAll(
																			"\\s+/.*?/",
																			"")
															+ ")";

												// Add shortcut
												BusPlus bp = (BusPlus) getApplicationContext();
												bp.setupShortcut(stationCode,
														stationName + suffix);
											}
										});
						AlertDialog alert = builder.create();
						alert.show();

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

		// USSD query with station code pressed
		if (!stationCode.equals("")) {
			Intent i = new Intent(FavoritesListActivity.this,
					FavoritesActivity.class);
			i.putExtra(FavoritesActivity.EXTRA_ID, stationCode);
			i.setAction(FavoritesActivity.INTENT_NAME);
			startActivity(i);
		}
	}

	/** Initialize favorite list and set up list adapter **/
	private void initList() {
		if (c != null) {
			stopManagingCursor(c);
			c.close();
		}

		int sortBy = Integer.parseInt(prefs.getString("sort_by", "0"));

		// Get all favorites
		c = helper.getFavorites(sortBy);
		startManagingCursor(c);
		cAdapter = new SimpleCursorAdapter(this, R.layout.row, c, fields,
				new int[] { R.id.itemId, R.id.itemName });

		getListView().setFastScrollEnabled(true);
		setListAdapter(cAdapter);
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