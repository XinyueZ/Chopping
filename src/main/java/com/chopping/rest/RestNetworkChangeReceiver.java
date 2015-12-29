package com.chopping.rest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.chopping.bus.UpdateNetworkStatusEvent;

import de.greenrobot.event.EventBus;

public class RestNetworkChangeReceiver extends BroadcastReceiver {
	public RestNetworkChangeReceiver() {
	}

	@Override
	public void onReceive( Context context, Intent intent ) {
		EventBus.getDefault()
				.post( new UpdateNetworkStatusEvent( RestUtils.isNetworkAvailable( context ) ) );
	}
}
