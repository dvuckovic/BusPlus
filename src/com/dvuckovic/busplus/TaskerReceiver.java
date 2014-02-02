package com.dvuckovic.busplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TaskerReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(
				"com.twofortyfouram.locale.intent.action.FIRE_SETTING")) {

			Intent i = new Intent(context, BalanceWidget.class);
			i.setAction(BalanceWidget.TASKER_ACTION);
			context.sendBroadcast(i);
		}
	}

}
