package com.dvuckovic.busplus;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

//import android.util.Log;

/** SQLite database worker with predefined methods **/
public class DataBaseHelper extends SQLiteOpenHelper {

	private static final int SCHEMA_VERSION = 18;
	private static String DB_PATH = "/data/data/com.dvuckovic.busplus/databases/";
	private static String DB_NAME = "busplus.db";
	private SQLiteDatabase myDataBase;
	private final Context myContext;
	
	/**
	 * Constructor takes and keeps a reference of the passed context in order to
	 * access to the application assets and resources.
	 * 
	 * @param context
	 */
	public DataBaseHelper(Context context) {
		super(context, DB_NAME, null, SCHEMA_VERSION);
		this.myContext = context;
	}

	/**
	 * Creates an empty database on the system and rewrites it with our own
	 * database.
	 **/
	public void createDataBase() throws IOException {

		boolean dbExist = checkDataBase();

		if (dbExist) {

			SQLiteDatabase oldDB = null;

			String myPath = DB_PATH + DB_NAME;
			oldDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READONLY);
			int ver = oldDB.getVersion();

			if (ver < SCHEMA_VERSION) {

				Cursor favorites = oldDB.rawQuery(
						"SELECT _id, name FROM favorites", null);
				ArrayList<ArrayList<String>> fav = new ArrayList<ArrayList<String>>();

				favorites.moveToFirst();
				while (favorites.isAfterLast() == false) {
					int id = favorites.getInt(favorites.getColumnIndex("_id"));
					String name = favorites.getString(favorites
							.getColumnIndex("name"));
					ArrayList<String> row = new ArrayList<String>();
					row.add(Integer.toString(id));
					row.add(name);
					fav.add(row);
					favorites.moveToNext();
				}
				favorites.close();

				ArrayList<ArrayList<String>> his = new ArrayList<ArrayList<String>>();

				if (ver >= 16) {
					Cursor history = oldDB.rawQuery(
							"SELECT _id, sid, name FROM history", null);

					history.moveToFirst();
					while (history.isAfterLast() == false) {
						int id = history.getInt(history.getColumnIndex("_id"));
						int sid = history.getInt(history.getColumnIndex("sid"));
						String name = history.getString(history
								.getColumnIndex("name"));
						ArrayList<String> row = new ArrayList<String>();
						row.add(Integer.toString(id));
						row.add(Integer.toString(sid));
						row.add(name);
						his.add(row);
						history.moveToNext();
					}
					history.close();
				}

				oldDB.close();

				myContext.deleteDatabase(DB_NAME);

				try {
					copyDataBase();
				} catch (IOException e) {
					throw new Error("Error copying database");
				}

				Iterator<ArrayList<String>> iterator = fav.iterator();
				while (iterator.hasNext()) {
					ArrayList<String> row = iterator.next();
					ContentValues cv = new ContentValues();
					cv.put("_id", row.get(0));
					cv.put("name", row.get(1));
					try {
						getWritableDatabase().insertOrThrow("favorites",
								"name", cv);
					} catch (SQLiteConstraintException e) {
						//
					}
				}

				if (ver >= 16) {
					Iterator<ArrayList<String>> iterator2 = his.iterator();
					while (iterator2.hasNext()) {
						ArrayList<String> row = iterator2.next();
						ContentValues cv = new ContentValues();
						cv.put("_id", row.get(0));
						cv.put("sid", row.get(1));
						cv.put("name", row.get(2));
						try {
							getWritableDatabase().insertOrThrow("history",
									"name", cv);
						} catch (SQLiteConstraintException e) {
							//
						}
					}
				}

				/*
				 * Log.d("com.dvuckovic.busplus", "Database updated (v" +
				 * SCHEMA_VERSION + ")!");
				 */
			} else {
				oldDB.close();
			}

		} else {

			// By calling this method an empty database will be created into
			// the default system path of your application so we are gonna be
			// able to overwrite that file with our database.
			this.getReadableDatabase();

			try {

				copyDataBase();

			} catch (IOException e) {

				throw new Error("Error copying database");

			}
		}

	}

	/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time you open the application.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase() {

		SQLiteDatabase checkDB = null;

		try {
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READONLY);

		} catch (SQLiteException e) {

			// database does't exist yet.

		}

		if (checkDB != null) {

			checkDB.close();

		}

		return checkDB != null ? true : false;
	}

	/**
	 * Copies your database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering bytestream.
	 * */
	private void copyDataBase() throws IOException {

		// Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(DB_NAME);

		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

	/** Opens database and creates it if doesn't exist **/
	public void openDataBase() throws SQLException {

		// Open the database
		String myPath = DB_PATH + DB_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READONLY);

	}

	@Override
	public synchronized void close() {

		if (myDataBase != null)
			myDataBase.close();

		super.close();

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	/**
	 * Inserts a station in Favorites table
	 * 
	 * @param id
	 * @param name
	 **/
	public void insertFavorite(int id, String name) {
		ContentValues cv = new ContentValues();

		cv.put("_id", id);
		cv.put("name", name);

		try {
			getWritableDatabase().insertOrThrow("favorites", "name", cv);
		} catch (SQLiteConstraintException e) {

		}
	}

	/**
	 * Rename a station in Favorites table based on it's id
	 * 
	 * @param id
	 **/
	public void renameFavorite(String id, String name) {
		String[] args = { id };

		ContentValues cv = new ContentValues();

		cv.put("name", name);

		try {
			getWritableDatabase().update("favorites", cv, "_ID=?", args);
		} catch (SQLiteConstraintException e) {

		}
	}

	/**
	 * Removes a station in Favorites table based on it's id
	 * 
	 * @param id
	 **/
	public void removeFavorite(String id) {
		String[] args = { id };

		getWritableDatabase().delete("favorites", "_ID=?", args);
	}

	/** Clears Favorites table **/
	public void clearFavorites() {
		getWritableDatabase().delete("favorites", null, null);
	}

	/**
	 * Returns all records from the Favorites table
	 * 
	 * @param sortBy
	 * 
	 * @return Cursor with results
	 **/
	public Cursor getFavorites(int sortBy) {
		Cursor c = null;

		// Check sort order
		switch (sortBy) {
		case 0:
			c = getReadableDatabase().rawQuery(
					"SELECT _id, name FROM favorites ORDER BY _id", null);
			break;
		case 1:
			c = getReadableDatabase().rawQuery(
					"SELECT _id, name FROM favorites ORDER BY name", null);
			break;
		}

		return c;
	}

	/**
	 * Inserts a station in History table
	 * 
	 * @param id
	 * @param name
	 **/
	public void insertHistory(int sid, String name) {
		String[] args = { String.valueOf(sid) };
		getWritableDatabase().delete("history", "sid=?", args);

		ContentValues cv = new ContentValues();

		cv.put("sid", sid);
		cv.put("name", name);

		try {
			getWritableDatabase().insertOrThrow("history", "name", cv);
			getWritableDatabase()
					.delete("history",
							"_id NOT IN (SELECT _id FROM history ORDER BY _id DESC LIMIT 10)",
							null);
		} catch (SQLiteConstraintException e) {

		}
	}

	/**
	 * Removes a station in History table based on it's id
	 * 
	 * @param id
	 **/
	public void removeHistory(String id) {
		String[] args = { id };
		getWritableDatabase().delete("history", "sid=?", args);
	}

	/** Clears History table **/
	public void clearHistory() {
		getWritableDatabase().delete("history", null, null);
	}

	/**
	 * Returns all records from the History table
	 * 
	 * @return Cursor with results
	 **/
	public Cursor getHistory() {
		Cursor c = null;

		c = getReadableDatabase().rawQuery(
				"SELECT _id, sid, name FROM history ORDER BY _id DESC", null);

		return c;
	}

	/**
	 * Returns all records from the Stations table
	 * 
	 * @return Cursor with results
	 **/
	public Cursor getAllStations() {
		return (getReadableDatabase().rawQuery(
				"SELECT _id, name, lat, lon FROM stations", null));
	}

	/**
	 * Returns 10 stations which are closest to the input lat/lon point. Uses
	 * approximation method of great-circle distance.
	 * 
	 * @param lat
	 * @param lon
	 * 
	 * @return Cursor with results
	 **/
	public Cursor getNearbyStations(Double lat, Double lon) {
		double fudge = Math.pow(Math.cos(Math.toRadians(lat)), 2);
		String[] args = { Double.toString(lat), Double.toString(lat),
				Double.toString(lon), Double.toString(lon),
				Double.toString(fudge) };
		return (getReadableDatabase()
				.rawQuery(
						"SELECT _id, name, lat, lon, "
								+ "((? - lat) * (? - lat) + (? - lon) * (? - lon) * ?) AS distance "
								+ "FROM stations "
								+ "WHERE lat IS NOT NULL AND lon IS NOT NULL "
								+ "ORDER BY distance LIMIT 10", args));
	}

	/**
	 * Returns all stations visible on the map view based on its center point
	 * and lat/long spans
	 * 
	 * @param lat
	 * @param lon
	 * @param latSpan
	 * @param lonSpan
	 * 
	 * @return Cursor with results
	 **/
	public Cursor getStationsInView(Double lat, Double lon, int latSpan,
			int lonSpan) {
		double latBound = (double) ((latSpan / 1E6) / 2);
		double lonBound = (double) ((lonSpan / 1E6) / 2);
		String[] args = { Double.toString(lat - latBound),
				Double.toString(lat + latBound),
				Double.toString(lon - lonBound),
				Double.toString(lon + lonBound) };
		return (getReadableDatabase()
				.rawQuery(
						"SELECT _id, name, lat, lon FROM stations "
								+ "WHERE lat > (?) AND lat < (?) AND lon > (?) AND lon < (?) ",
						args));
	}

	/**
	 * Returns station with the input id code
	 * 
	 * @param id
	 * @return Cursor with results
	 **/
	public Cursor getStationById(String id) {
		String[] args = { id };
		return (getReadableDatabase().rawQuery(
				"SELECT _id, name, lat, lon FROM stations WHERE _ID=?", args));
	}

	/**
	 * Returns all records from the Stations table which match simple text
	 * search filter.
	 * 
	 * @param str
	 * @return Cursor with results
	 **/
	public Cursor getStationByName(String str) {
		String[] args = { str + "%", "% " + str + "%", "%/" + str + "%",
				"%\"" + str + "%", str + "%", "% " + str + "%",
				"%/" + str + "%", "%\"" + str + "%", str + "%",
				"% " + str + "%", "%/" + str + "%", "%\"" + str + "%" };
		return (getReadableDatabase()
				.rawQuery(
						"SELECT _id, name, lat, lon FROM stations WHERE "
								+ "(name_ascii LIKE ? OR name_ascii LIKE ? OR name_ascii LIKE ? OR name_ascii LIKE ?)"
								+ " OR "
								+ "(name LIKE ? OR name LIKE ? OR name LIKE ? OR name LIKE ?)"
								+ " OR "
								+ "(_id LIKE ? OR _id LIKE ? OR _id LIKE ? OR _id LIKE ?)",
						args));
	}

	/**
	 * Returns stations from a single line in selected direction. Uses UNION
	 * query with additional sort field which is enumerated using split method
	 * 
	 * @param lineId
	 * @param dir
	 * 
	 * @return Cursor with results
	 **/
	public Cursor getStationsByLine(String lineId, String dir) {

		String[] args = { lineId };
		Cursor c = getReadableDatabase().rawQuery(
				"SELECT _id, stations_" + dir.toLowerCase(Locale.US)
						+ " FROM lines WHERE _id=? LIMIT 1", args);
		c.moveToFirst();

		String stationsStr = c.getString(1);
		String[] stations = stationsStr.split(",");

		c.close();

		String query = "";
		for (int i = 0; i < stations.length; i++) {
			query += "SELECT _id, name, lat, lon, " + i
					+ " AS sort_id FROM stations WHERE _id=?";
			if (i != stations.length - 1)
				query += " UNION ";
			else
				query += " ORDER BY sort_id";
		}

		return (getReadableDatabase().rawQuery(query, stations));
	}

	/**
	 * Returns all records from the Lines table which match simple text search
	 * filter.
	 * 
	 * @param str
	 * 
	 * @return Cursor with results
	 **/
	public Cursor getLinesByName(String str) {
		String[] args = { str + "%", "% " + str + "%", "%/" + str + "%",
				"%\"" + str + "%", str + "%", "% " + str + "%",
				"%/" + str + "%", "%\"" + str + "%" };
		return (getReadableDatabase()
				.rawQuery(
						"SELECT _id, name, desc, desc_a, desc_b FROM lines WHERE "
								+ "(name LIKE ? OR name LIKE ? OR name LIKE ? OR name LIKE ?)"
								+ " OR "
								+ "(desc_ascii LIKE ? OR desc_ascii LIKE ? OR desc_ascii LIKE ? OR desc_ascii LIKE ?)",
						args));
	}

	public Cursor getLinesByStation(String str) {
		String[] args = { str + ",%", "%," + str + ",%", "%," + str,
				str + ",%", "%," + str + ",%", "%," + str };
		return (getReadableDatabase()
				.rawQuery(
						"SELECT _id, name FROM lines WHERE "
								+ "(stations_a LIKE ? OR stations_a LIKE ? OR stations_a LIKE ?)"
								+ " OR "
								+ "(stations_b LIKE ? OR stations_b LIKE ? OR stations_b LIKE ?)",
						args));
	}

}
