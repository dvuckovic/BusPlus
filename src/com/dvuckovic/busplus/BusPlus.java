package com.dvuckovic.busplus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * This is class extends Application class in order to add some often global
 * methods. It also set's default language using a Locale hack.
 **/
public class BusPlus extends Application {

	public final static String EXTRA_ID = "com.dvuckovic.busplus._ID";
	public final static String INTENT_NAME = "com.dvuckovic.busplus.CALL_USSD_CODE";
	private SharedPreferences prefs;

	// public static boolean showMap = false;
	public static String showStationId = "";
	public static String showStationIdVal = "";
	public static String showLineId = "";
	public static int mapZoom = 17;

	@Override
	public void onCreate() {
		super.onCreate();

		// Our preference manager for getting preferences out of the storage
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Get rid of the old widget zone preference
		if (!prefs.getBoolean("widget_zone_removed2", false)) {
			prefs.edit().remove("widget_zone").commit();
			prefs.edit().putBoolean("widget_zone_removed2", true).commit();
		}

		// Reenable Web service
		if (!prefs.getBoolean("web_service_reenabled", false)) {
			prefs.edit().putString("service", "web").commit();
			prefs.edit().putBoolean("web_service_reenabled", true).commit();
		}

		// Call the setLanguage method
		setLanguage(prefs.getString("language", "sr"));

	}

	/**
	 * Sets app locale manually based on ISO 639-1 language code
	 * 
	 * @param language
	 **/
	public void setLanguage(String language) {
		String languageToLoad = language;
		Locale locale = new Locale(languageToLoad);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());
	}

	/**
	 * Creates shortcut on home launcher with station id and station name and
	 * links it to the exposed custom intent in FavoritesActivity
	 * 
	 * @param stationCode
	 * @param stationName
	 */
	public void setupShortcut(String stationCode, String stationName) {
		// First, set up the shortcut intent. For this example, we simply create
		// an intent that will bring us directly back to this activity. A more
		// typical implementation would use a data Uri in order to display a
		// more specific result, or a custom action in order to launch a
		// specific operation.

		// Let's draw a station code to the blank icon ;)
		Bitmap icon = BitmapFactory.decodeResource(getResources(),
				R.drawable.blank_icon);
		Bitmap bitmapResult = Bitmap.createBitmap(icon.getWidth(),
				icon.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas();
		c.setBitmap(bitmapResult);

		// Adjust icon hue if there is a setting in preferences
		if (prefs.getBoolean("shortcut_custom_color", false)) {
			Paint iPaint = new Paint();
			iPaint.setColorFilter(ColorFilterGenerator.adjustHue(prefs.getInt(
					"shortcut_hue", 180) - 180));
			c.drawBitmap(icon, 0, 0, iPaint);
		} else {
			c.drawBitmap(icon, 0, 0, null);
		}
		icon.recycle();

		// Get screen density
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) getApplicationContext()
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(metrics);
		int density = metrics.densityDpi;

		// Draw text on to icon
		Paint tPaint = new Paint();
		tPaint.setAntiAlias(true);
		tPaint.setTextSize((float) (density / (40 / 3)));
		tPaint.setShadowLayer(2, 0, 0, R.color.dark_blue);
		tPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
		float canvasWidth = c.getWidth();
		float canvasHeight = c.getHeight();
		float textWidth = tPaint.measureText(stationCode);
		float startPositionX = (canvasWidth - textWidth) / 2;
		float startPositionY = canvasHeight / 2 + Math.round(density / 50) + 2;
		tPaint.setTextAlign(Paint.Align.LEFT);
		tPaint.setColor(Color.WHITE);
		c.drawText(stationCode, startPositionX, startPositionY, tPaint);

		Intent shortcutIntent = new Intent(INTENT_NAME);
		shortcutIntent.setComponent(new ComponentName(this.getPackageName(),
				"com.dvuckovic.busplus.FavoritesActivity"));
		shortcutIntent.putExtra(EXTRA_ID, stationCode);

		// Then, set up the container intent (the response to the caller)
		Intent intent = new Intent();
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, stationName);

		// Put the icon as an intent extra
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmapResult);
		intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

		// Finally, send intent to the system
		sendBroadcast(intent);

		bitmapResult.recycle();
	}

	public void showToastMessage(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
				.show();
	}

	public static int getType(String line) {

		int[] tram = { 2, 3, 5, 6, 7, 9, 10, 11, 12, 13, 14 };
		List<Integer> tramList = new ArrayList<Integer>(tram.length);
		for (int i = 0; i < tram.length; i++)
			tramList.add(tram[i]);

		int[] troly = { 19, 21, 22, 28, 29, 40, 41 };
		List<Integer> trolyList = new ArrayList<Integer>(troly.length);
		for (int i = 0; i < troly.length; i++)
			trolyList.add(troly[i]);

		int[] bus = { 15, 16, 17, 18, 20, 23, 24, 25, 26, 27, 30, 31, 32, 33,
				34, 35, 37, 38, 39, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52,
				53, 54, 55, 56, 57, 58, 59, 60, 65, 67, 68, 69, 71, 72, 73, 74,
				75, 76, 77, 78, 79, 81, 82, 83, 84, 85, 87, 88, 89, 91, 92, 94,
				95, 96, 101, 102, 104, 105, 106, 107, 108, 109, 110, 202, 302,
				303, 304, 305, 306, 307, 308, 309, 310, 400, 401, 402, 403,
				405, 406, 407, 408, 503, 504, 511, 512, 513, 521, 522, 531,
				532, 533, 534, 551, 552, 601, 602, 603, 604, 605, 606, 610,
				611, 612, 700, 702, 703, 704, 705, 706, 707, 708, 709, 711 };
		List<Integer> busList = new ArrayList<Integer>(bus.length);
		for (int i = 0; i < bus.length; i++)
			busList.add(bus[i]);

		int lineInt = 0;
		String lineStr = "";

		String[] specs = { "ADA1", "ADA2", "ADA3", "ADA4", "ADA5" };

		for (String spec : specs)
			if (line.equals(spec))
				return 0;

		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			try {
				lineStr += String.valueOf(Integer.parseInt(String.valueOf(c)));
			} catch (NumberFormatException e) {
				// e.printStackTrace();
			}
		}

		try {
			lineInt = Integer.parseInt(lineStr);
		} catch (NumberFormatException e) {
			// e.printStackTrace();
		}

		if (tramList.contains(lineInt))
			return 1;
		else if (trolyList.contains(lineInt))
			return 2;
		else if (busList.contains(lineInt))
			return 3;

		return 0;

	}

	public static int getBackgroundColor(String line) {

		int type = getType(line);
		switch (type) {
		case 1:
			return Color.rgb(222, 36, 24);
		case 2:
			return Color.rgb(255, 96, 0);
		case 3:
			return Color.rgb(49, 121, 198);
		case 0:
		default:
			return Color.rgb(0, 0, 180);
		}

	}

	public static int getTextColor(String line) {

		int type = getType(line);
		switch (type) {
		case 3:
		case 1:
		case 2:
		case 0:
		default:
			return Color.rgb(255, 255, 255);
		}

	}

	public static int getShadowColor(String line) {

		int type = getType(line);
		switch (type) {
		case 3:
		case 1:
		case 2:
		case 0:
		default:
			return Color.rgb(0, 0, 0);
		}

	}
}
