package com.chopping.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;


public final class IncomingCallReceiver extends WakefulBroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		DeviceUtils.rejectIncomingCall(context);
	}
}