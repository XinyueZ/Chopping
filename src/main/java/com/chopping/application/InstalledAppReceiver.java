package com.chopping.application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.chopping.bus.ExternalAppChangedEvent;

import de.greenrobot.event.EventBus;

/**
 * Event that will be sent after an external App has been installed.
 *
 * @author Xinyue Zhao
 */
public final class InstalledAppReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		//Info UI to refresh button status.
		Uri data = intent.getData();
		if(data != null) {
			String packageName = data.getSchemeSpecificPart();
			EventBus.getDefault().post(new ExternalAppChangedEvent(packageName));
		}
	}
}
