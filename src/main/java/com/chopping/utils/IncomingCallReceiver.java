package com.chopping.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.chopping.application.LL;


public final class IncomingCallReceiver extends WakefulBroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		DeviceUtils.rejectIncomingCall(context);
		LL.i("Rejected an incoming call.");
	}
}