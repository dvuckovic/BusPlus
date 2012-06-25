package com.dvuckovic.busplus;

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
import android.net.Uri;
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

	@Override
	public void onCreate() {
		super.onCreate();

		// Our preference manager for getting preferences out of the storage
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Call the setLanguage method
		setLanguage(prefs.getString("language", "sr"));

	}

	/**
	 * Executes USSD query using a station code as input by calling Intent on
	 * system call app
	 * 
	 * @param stationCode
	 **/
	public void callUSSDCode(String stationCode) {
		String ussd = "*011*" + stationCode + Uri.encode("#");
		Intent i = new Intent(android.content.Intent.ACTION_CALL,
				Uri.parse("tel:" + ussd));
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
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
		float startPositionY = canvasHeight / 2 + Math.round(density / 50);
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
}
