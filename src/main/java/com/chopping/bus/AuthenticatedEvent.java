package com.chopping.bus;


import com.firebase.client.AuthData;

public final class AuthenticatedEvent {
	private long     mId;
	private AuthData mAuthData;


	public AuthenticatedEvent( long id, AuthData authData ) {
		mId = id;
		mAuthData = authData;
	}


	public AuthData getAuthData() {
		return mAuthData;
	}

	public long getId() {
		return mId;
	}
}
