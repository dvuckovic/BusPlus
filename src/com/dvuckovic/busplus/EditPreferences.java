package com.dvuckovic.busplus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Iterator;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Base64;

/** Preference screen activity **/
public class EditPreferences extends PreferenceActivity {

	private SharedPreferences prefs;
	private DataBaseHelper helper;

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

		helper = new DataBaseHelper(getBaseContext());

		initPrefs();
	}

	private void initPrefs() {

		final Preference widgetZoneBtn = (Preference) findPreference("widget_zone");
		widgetZoneBtn
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

					// If custom value is selected in widget zone list
					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {

						Context context = getApplicationContext();

						int lastZone = context.getResources().getStringArray(
								R.array.zone_ids).length - 1;
						int currentZone = Integer.parseInt((String) newValue);

						if (currentZone == lastZone) {
							Intent intent = new Intent(context,
									FareDialog.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.putExtra(FareDialog.EXTRA_ID, Integer.parseInt(prefs.getString("widget_zone", "0")));
							context.startActivity(intent);
						}
						
						return true;

					}
				});

		final Preference favoritesExportBtn = (Preference) findPreference("favorites_export");

		favoritesExportBtn
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {

						Cursor favorites = helper.getFavorites(0);
						ArrayList<ArrayList<String>> fav = new ArrayList<ArrayList<String>>();

						favorites.moveToFirst();
						while (favorites.isAfterLast() == false) {
							int id = favorites.getInt(favorites
									.getColumnIndex("_id"));
							String name = favorites.getString(favorites
									.getColumnIndex("name"));
							ArrayList<String> row = new ArrayList<String>();
							row.add(Integer.toString(id));
							row.add(name);
							fav.add(row);
							favorites.moveToNext();
						}
						favorites.close();

						try {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							ObjectOutputStream oos = new ObjectOutputStream(
									baos);
							oos.writeObject(fav);
							oos.close();
							String serializedFav = new String(Base64
									.encodeToString(baos.toByteArray(),
											Base64.NO_WRAP));

							EncryptDecrypt ed = new EncryptDecrypt(
									"R{%%>:nL7%T>]+-5~'rlt#(1%kH{{mkb");
							String encFav = ed.e(serializedFav);

							File favFile = new File(Environment
									.getExternalStorageDirectory().getPath(),
									"busplus.fav");
							if (!favFile.exists())
								favFile.createNewFile();
							BufferedWriter buf = new BufferedWriter(
									new FileWriter(favFile, false));
							buf.write(encFav);
							buf.close();

							showMsgDialog(
									getString(R.string.favorites_export),
									getString(R.string.favorites_export_success)
											+ "\n"
											+ Environment
													.getExternalStorageDirectory()
													.getPath() + "/busplus.fav");

						} catch (IOException e) {
							// e.printStackTrace();
							showMsgDialog(getString(R.string.favorites_export),
									getString(R.string.favorites_export_error));
						}

						return true;
					}
				});

		final Preference favoritesClearBtn = (Preference) findPreference("favorites_clear");

		favoritesClearBtn
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {

						// Build and show a Yes/No dialog
						AlertDialog.Builder builder = new AlertDialog.Builder(
								arg0.getContext());
						builder.setTitle(getString(R.string.clear_favorites))
								.setMessage(
										getString(R.string.clear_favorites_confirm))
								.setPositiveButton(getString(R.string.yes),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												// Clear favorites if Yes was
												// pressed,
												// disable option menu and
												// refresh list
												// items
												helper.clearFavorites();
												favoritesExportBtn
														.setEnabled(false);
												favoritesClearBtn
														.setEnabled(false);
												dialog.dismiss();
											}
										})
								.setNegativeButton(getString(R.string.no),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												// Cancel dialog if No was
												// pressed
												dialog.cancel();
											}
										});
						AlertDialog alert = builder.create();
						alert.show();

						return true;
					}
				});

		Preference favoritesImportBtn = (Preference) findPreference("favorites_import");
		favoritesImportBtn
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {

						File favFile = new File(Environment
								.getExternalStorageDirectory().getPath(),
								"busplus.fav");

						if (!favFile.exists())
							showMsgDialog(getString(R.string.favorites_import),
									getString(R.string.favorites_import_fnf));
						else {

							StringBuilder sb = new StringBuilder();

							boolean exception = false;

							try {
								BufferedReader br = new BufferedReader(
										new FileReader(favFile));
								String line;
								while ((line = br.readLine()) != null) {
									sb.append(line);
									sb.append("\n");
								}
								br.close();

								String encFav = new String(sb);

								EncryptDecrypt ed = new EncryptDecrypt(
										"R{%%>:nL7%T>]+-5~'rlt#(1%kH{{mkb");
								String serializedFav = ed.d(encFav);

								byte[] favStream = Base64.decode(serializedFav,
										Base64.NO_WRAP);
								ObjectInputStream ois;
								ois = new ObjectInputStream(
										new ByteArrayInputStream(favStream));
								Object o = ois.readObject();
								ois.close();

								@SuppressWarnings("unchecked")
								ArrayList<ArrayList<String>> fav = (ArrayList<ArrayList<String>>) o;

								Iterator<ArrayList<String>> iterator = fav
										.iterator();

								while (iterator.hasNext()) {
									ArrayList<String> row = iterator.next();
									helper.insertFavorite(
											Integer.parseInt(row.get(0)),
											row.get(1));
								}

								favoritesExportBtn.setEnabled(true);
								favoritesClearBtn.setEnabled(true);

								showMsgDialog(
										getString(R.string.favorites_import),
										getString(R.string.favorites_import_success));

							} catch (StreamCorruptedException e) {
								// e.printStackTrace();
								exception = true;
							} catch (IOException e) {
								// e.printStackTrace();
								exception = true;
							} catch (ClassNotFoundException e) {
								// e.printStackTrace();
								exception = true;
							} catch (ClassCastException e) {
								// e.printStackTrace();
								exception = true;
							}

							if (exception)
								showMsgDialog(
										getString(R.string.favorites_import),
										getString(R.string.favorites_import_error));

						}

						return true;
					}
				});

		Cursor favorites = helper.getFavorites(0);
		if (favorites.getCount() > 0) {
			favoritesExportBtn.setEnabled(true);
			favoritesClearBtn.setEnabled(true);
		}
		favorites.close();
	}

	protected void showMsgDialog(String title, String message) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title).setMessage(message)
				.setNeutralButton("OK", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						dialog.dismiss();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();

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

	/** Perform any final cleanup before an activity is destroyed. */
	@Override
	public void onDestroy() {
		super.onDestroy();

		helper.close();
	}

	/** Listens for changes preferences **/
	SharedPreferences.OnSharedPreferenceChangeListener onChange = new SharedPreferences.OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(SharedPreferences prefs,
				String key) {

			// If language was changed, restart the activity
			if (key.equals("language")) {

				BusPlus bp = (BusPlus) getApplicationContext();
				bp.setLanguage(prefs.getString("language", "sr"));
				setPreferenceScreen(null);
				addPreferencesFromResource(R.xml.preferences);

				initPrefs();

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

				if (key.equals("widget_balance")) {
					if (TaskerIntent.testStatus(context).equals(
							TaskerIntent.Status.OK)
							|| TaskerIntent.testStatus(context).equals(
									TaskerIntent.Status.NotEnabled)) {

						TaskerIntent i = new TaskerIntent("SET_VARS");

						i.addAction(ActionCodes.SET_VARIABLE)
								.addArg("%BusPlusBalance")
								.addArg(prefs
										.getString("widget_balance", "730"))
								.addArg(false).addArg(false);

						context.sendBroadcast(i);
					}
				}
			}

		}
	};

}
