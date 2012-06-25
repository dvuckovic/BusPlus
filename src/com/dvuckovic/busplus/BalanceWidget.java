package com.dvuckovic.busplus;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

/** Widget provider responsible for creating and updating widget on home screen **/
public class BalanceWidget extends AppWidgetProvider {

	private final static String WIDGET_CLICK = "com.dvuckovic.busplus.widget_click";
	private boolean clicked = false;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		// A few globals
		RemoteViews remoteViews = null;
		boolean tooLow = false;

		// Get all ids
		ComponentName thisWidget = new ComponentName(context,
				BalanceWidget.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		// Instantiate preference manager
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		// Get ticket fare from preferences
		int ticketFare = Integer.parseInt(prefs.getString("widget_zone", "60"));

		// Resolve widget style from preferences
		switch (Integer.parseInt(prefs.getString("widget_type", "1"))) {
		case 1:
			remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget1);
			break;
		case 2:
			remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget2);
			break;
		case 3:
			remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget3);
			break;
		}

		// Get balance from preferences
		int balance = Integer
				.parseInt(prefs.getString("widget_balance", "600"));

		// Check if the update was requested by a tap
		if (clicked) {

			// Decrease balance for a single fare
			if (balance - ticketFare >= 0)
				balance = balance - ticketFare;

			// Put new balance in preferences
			prefs.edit().putString("widget_balance", Integer.toString(balance))
					.commit();

			// Clear the clicked variable
			clicked = false;
		}

		// If the balance is too low for a decrease, set the flag
		if (balance - ticketFare < 0)
			tooLow = true;

		// Loop all widgets
		for (int widgetId : allWidgetIds) {

			// Set the balance in widget
			remoteViews.setTextViewText(R.id.balance, String.valueOf(balance));

			// If the too low balance flag was set
			if (tooLow) {
				// Set the click to open dialog activity
				Intent intent = new Intent(context, BalanceDialog.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
				PendingIntent pendingIntent = PendingIntent.getActivity(
						context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);
				appWidgetManager.updateAppWidget(widgetId, remoteViews);
			} else {
				// Otherwise, set the click to update the widget and set the
				// clicked flag as an extra
				Intent intent = new Intent(context, BalanceWidget.class);

				intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
						appWidgetIds);
				intent.putExtra(WIDGET_CLICK, true);

				PendingIntent pendingIntent = PendingIntent.getBroadcast(
						context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);
				appWidgetManager.updateAppWidget(widgetId, remoteViews);
			}
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// Check the intent for clicked flag in extras
		if (intent.hasExtra(WIDGET_CLICK))
			clicked = true;

		super.onReceive(context, intent);
	}

}