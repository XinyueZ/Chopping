package com.chopping.bus;

public final class UpdateNetworkStatusEvent {
	private boolean mConnected;

	public UpdateNetworkStatusEvent( boolean connected ) {
		mConnected = connected;
	}


	public boolean isConnected() {
		return mConnected;
	}
}
