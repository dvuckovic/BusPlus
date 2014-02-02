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
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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
	private DataBaseHelper helper;
	private Cursor h;
	private CursorAdapter hAdapter;
	private static final String fields[] = { "name", "desc" };
	private static final String history_fields[] = { "sid", "name" };
	private LinearLayout historyLayout;
	private Button clearBtn;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Instantiate preference manager
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Sets the view for this activity
		setContentView(R.layout.search);

		stationCodeTxt = (EditText) findViewById(R.id.stationCode);
		stationNameTxt = (EditText) findViewById(R.id.searchText);
		lineNameTxt = (EditText) findViewById(R.id.lineSearchText);

		stationCodeTxt.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				setClearBtn();
				return false;
			}
		});

		stationNameTxt.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				setClearBtn();
				return false;
			}
		});

		lineNameTxt.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				setClearBtn();
				return false;
			}
		});

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
					Intent i = new Intent(StationActivity.this,
							FavoritesActivity.class);
					i.putExtra(FavoritesActivity.EXTRA_ID, stationCode);
					i.setAction(FavoritesActivity.INTENT_NAME);
					startActivity(i);

					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(
							stationCodeTxt.getWindowToken(), 0);
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
										.getBackgroundColor(lineNumber
												.getText().toString()));
								lineNumber.setShadowLayer(3, 0, 0,
										Color.rgb(0, 0, 0));
							}
						}
					});

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
									.setNegativeButton(
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
									.setPositiveButton(
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

		clearBtn = (Button) findViewById(R.id.clearButton);
		clearBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// Clear text fields
				stationCodeTxt.setText("");
				stationNameTxt.setText("");
				lineNameTxt.setText("");
				setClearBtn();
			}
		});

		setClearBtn();

		Button prefBtn = (Button) findViewById(R.id.preferencesButton);
		prefBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// Start preferences activity
				startActivity(new Intent(getBaseContext(),
						EditPreferences.class));
			}
		});

		Button aboutBtn = (Button) findViewById(R.id.aboutButton);
		aboutBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// Start preferences activity
				startActivity(new Intent(getBaseContext(), InfoActivity.class));
			}
		});

		helper = new DataBaseHelper(this);

		if (h != null) {
			stopManagingCursor(h);
			h.close();
		}

		// Get history
		h = helper.getHistory();
		startManagingCursor(h);

		hAdapter = new SimpleCursorAdapter(this, R.layout.row, h,
				history_fields, new int[] { R.id.itemId, R.id.itemName });

		ListView lv = (ListView) findViewById(R.id.historyList);
		lv.setFastScrollEnabled(true);
		lv.setAdapter(hAdapter);
		registerForContextMenu(lv);

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				TextView itemId = (TextView) arg1.findViewById(R.id.itemId);
				String stationCode = (String) itemId.getText().toString();

				// Query with station code pressed
				if (!stationCode.equals("")) {
					Intent i = new Intent(StationActivity.this,
							FavoritesActivity.class);
					i.putExtra(FavoritesActivity.EXTRA_ID, stationCode);
					i.setAction(FavoritesActivity.INTENT_NAME);
					startActivity(i);
				}
			}
		});

		lv.setOnCreateContextMenuListener(new ListView.OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(ContextMenu menu, final View v,
					ContextMenuInfo menuInfo) {

				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
				
				Cursor c = (Cursor) hAdapter.getItem(info.position);
				final String stationId = c.getString(c.getColumnIndex("sid"));
				
				Cursor s = helper.getStationById(stationId);
				startManagingCursor(s);

				if (s.getCount() == 1) {

					s.moveToFirst();

					final String stationName = s.getString(s
							.getColumnIndex("name"));

					menu.setHeaderTitle("[" + stationId + "] " + stationName);

					// Add remove option to context menu
					menu.add(0, 0, 3, getString(R.string.history_remove))
							.setOnMenuItemClickListener(
									new OnMenuItemClickListener() {
										public boolean onMenuItemClick(
												MenuItem item) {
											// Remove favorite which was pressed
											// and refresh list
											helper.removeHistory(stationId);
											h.requery();
											historyVisibility();

											return true;
										}
									});

					// Add query option to context menu
					menu.add(0, 1, 4, getString(R.string.query))
							.setOnMenuItemClickListener(
									new OnMenuItemClickListener() {
										public boolean onMenuItemClick(
												MenuItem item) {
											// USSD query with station code
											// pressed
											Intent i = new Intent(
													getBaseContext(),
													FavoritesActivity.class);
											i.putExtra(
													FavoritesActivity.EXTRA_ID,
													stationId);
											i.setAction(FavoritesActivity.INTENT_NAME);
											startActivity(i);
											return true;
										}
									});

					// Add shortcut option to context menu
					menu.add(0, 2, 1, getString(R.string.launcher_shortcut))
							.setOnMenuItemClickListener(
									new OnMenuItemClickListener() {
										public boolean onMenuItemClick(
												MenuItem item) {
											// Setup launcher shortcut
											BusPlus bp = (BusPlus) getApplicationContext();
											bp.setupShortcut(stationId,
													stationName);
											return true;
										}
									});

					// Add show on map context menu
					menu.add(0, 4, 5, getString(R.string.show_map))
							.setOnMenuItemClickListener(
									new OnMenuItemClickListener() {
										public boolean onMenuItemClick(
												MenuItem item) {
											if (!BusPlusActivity.active) {
												BusPlus.showStationId = String
														.valueOf(stationId);
												Intent i = new Intent(
														getBaseContext(),
														BusPlusActivity.class);
												i.setAction(BusPlusActivity.SHOW_MAP_TAB);
												startActivity(i);
											} else {
												BusPlus.showStationId = String
														.valueOf(stationId);
												Intent i = new Intent(
														BusPlusActivity.SHOW_MAP_TAB);
												sendBroadcast(i);
											}

											return true;
										}
									});

					menu.add(0, 5, 6, getString(R.string.station_info))
							.setOnMenuItemClickListener(
									new OnMenuItemClickListener() {
										public boolean onMenuItemClick(
												MenuItem item) {
											// Show dialog with options on tap
											AlertDialog.Builder builder = new AlertDialog.Builder(
													StationActivity.this);

											Cursor cur = helper
													.getLinesByStation(stationId);
											startManagingCursor(cur);

											String[] lines = new String[cur
													.getCount()];
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
											View v = li.inflate(
													R.layout.message, null,
													false);

											TextView stationNameView = (TextView) v
													.findViewById(R.id.stationName);
											stationNameView
													.setTextAppearance(
															getBaseContext(),
															android.R.style.TextAppearance_Medium);
											stationNameView.setText("["
													+ stationId + "] "
													+ stationName);

											FlowLayout fl = (FlowLayout) v
													.findViewById(R.id.flowLayout);

											for (String line : lines) {

												TextView lineTextView = new TextView(
														getBaseContext());
												lineTextView
														.setTextAppearance(
																getBaseContext(),
																android.R.style.TextAppearance_Medium);
												lineTextView
														.setBackgroundColor(BusPlus
																.getBackgroundColor(line));
												lineTextView.setShadowLayer(3,
														0, 0,
														Color.rgb(0, 0, 0));
												lineTextView.setText(" " + line
														+ " ");

												fl.addView(lineTextView);
											}

											builder.setTitle(
													getString(R.string.station))
													.setView(v)
													// .setMessage(tapName +
													// " (" + tapCode + ")")
													.setNegativeButton(
															getString(R.string.query),
															new DialogInterface.OnClickListener() {
																public void onClick(
																		DialogInterface dialog,
																		int which) {
																	// USSD
																	// query
																	Intent i = new Intent(
																			getBaseContext(),
																			FavoritesActivity.class);
																	i.putExtra(
																			FavoritesActivity.EXTRA_ID,
																			stationId);
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
																	// Add
																	// favorite
																	// and toast
																	// a
																	// message
																	helper.insertFavorite(
																			Integer.parseInt(stationId),
																			stationName);
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
																	// Add
																	// shortcut
																	BusPlus bp = (BusPlus) getApplicationContext();
																	bp.setupShortcut(
																			stationId,
																			stationName);
																}
															});
											AlertDialog alert = builder
													.create();
											alert.show();

											return true;
										}
									});
				}

				stopManagingCursor(s);
				s.close();

			}
		});

		TextView historyLabel = (TextView) findViewById(R.id.historyLabel);
		historyLabel.setOnLongClickListener(new TextView.OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				// Build and show a Yes/No dialog
				AlertDialog.Builder builder = new AlertDialog.Builder(arg0
						.getContext());
				builder.setTitle(R.string.history_clear)
						.setMessage(R.string.history_clear_confirm)
						.setPositiveButton(R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										helper.clearHistory();
										h.requery();
										historyVisibility();
										dialog.dismiss();
									}
								})
						.setNegativeButton(R.string.no,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										// Cancel dialog if No was
										// pressed
										dialog.cancel();
									}
								});
				AlertDialog alert = builder.create();
				alert.show();

				return false;
			}
		});

		historyLayout = (LinearLayout) findViewById(R.id.historyLayout);
		historyVisibility();

	}

	/** Refresh list on resume of this activity **/
	@Override
	protected void onResume() {
		h.requery();
		historyVisibility();

		super.onResume();
	}

	private void historyVisibility() {
		if (h.getCount() == 0) {
			historyLayout.setVisibility(View.GONE);
		} else {
			historyLayout.setVisibility(View.VISIBLE);
		}
	}

	public void onDestroy() {
		super.onDestroy();

		stopManagingCursor(h);
		h.close();
		helper.close();
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

	private void setClearBtn() {
		if (stationCodeTxt.getText().toString().isEmpty()) {
			if (stationNameTxt.getText().toString().isEmpty()) {
				if (lineNameTxt.getText().toString().isEmpty())
					clearBtn.setEnabled(false);
				else
					clearBtn.setEnabled(true);
			} else
				clearBtn.setEnabled(true);
		} else
			clearBtn.setEnabled(true);
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