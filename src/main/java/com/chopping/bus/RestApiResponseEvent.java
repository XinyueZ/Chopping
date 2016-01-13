package com.chopping.bus;


public final class RestApiResponseEvent {
	private boolean mSuccess;

	public RestApiResponseEvent( boolean success ) {
		mSuccess = success;
	}


	public boolean isSuccess() {
		return mSuccess;
	}
}
