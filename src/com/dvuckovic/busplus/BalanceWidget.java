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

	private final static String WIDGET_CLICK = "com.dvuckovic.busplus.WIDGET_CLICK";
	private final static String SETTINGS_CLICK = "com.dvuckovic.busplus.SETTINGS_CLICK";
	final static String TASKER_ACTION = "com.dvuckovic.busplus.UPDATE_WIDGET";
	private boolean clicked = false;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		myUpdate(context, appWidgetManager, appWidgetIds);

	}

	private void myUpdate(Context context, AppWidgetManager appWidgetManager,
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
		int ticketFare;
		int lastZone = context.getResources().getStringArray(R.array.zone_ids).length - 1;
		int currentZone = Integer.parseInt(prefs.getString("widget_zone", "0"));

		if (currentZone == lastZone)
			ticketFare = Integer.parseInt(prefs.getString("widget_zone_custom",
					"0"));
		else
			ticketFare = Integer.parseInt(context.getResources()
					.getStringArray(R.array.zone_fares)[currentZone]);

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
		case 4:
			remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget4);
			break;
		}

		// Get balance from preferences
		int balance = Integer.parseInt(prefs.getString("widget_balance", String
				.valueOf(Integer.parseInt(context.getResources()
						.getStringArray(R.array.zone_fares)[0]) * 10)));

		// If the balance is too low for a decrease, set the flag
		if (balance - ticketFare < 0)
			tooLow = true;

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

			if (TaskerIntent.testStatus(context).equals(TaskerIntent.Status.OK)
					|| TaskerIntent.testStatus(context).equals(
							TaskerIntent.Status.NotEnabled)) {

				TaskerIntent i = new TaskerIntent("SET_VARS");

				i.addAction(ActionCodes.SET_VARIABLE).addArg("%BusPlusBalance")
						.addArg(Integer.toString(balance)).addArg(false)
						.addArg(false);

				context.sendBroadcast(i);

				if (tooLow) {
					Intent intent = new Intent(context, BalanceDialog.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);
				}
			}
		}

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

				intent = new Intent(context, BalanceWidget.class);
				intent.setAction(SETTINGS_CLICK);

				pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
						0);
				remoteViews.setOnClickPendingIntent(R.id.settings,
						pendingIntent);

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

				intent = new Intent(context, BalanceWidget.class);
				intent.setAction(SETTINGS_CLICK);

				pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
						0);
				remoteViews.setOnClickPendingIntent(R.id.settings,
						pendingIntent);

				appWidgetManager.updateAppWidget(widgetId, remoteViews);
			}
		}
	}

	@Override
	public void onReceive(final Context context, Intent intent) {
		// Check the intent for clicked flag in extras
		if (intent.hasExtra(WIDGET_CLICK))
			clicked = true;

		if (intent.getAction().equals(SETTINGS_CLICK)) {
			Intent i = new Intent(context, ZoneDialog.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}

		if (intent.getAction().equals(TASKER_ACTION)) {
			clicked = true;

			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			int[] appWidgetIds = appWidgetManager
					.getAppWidgetIds(new ComponentName(context, this.getClass()));

			myUpdate(context, appWidgetManager, appWidgetIds);
		}

		super.onReceive(context, intent);
	}

}