package com.chopping.bus;


import com.firebase.client.FirebaseError;

public final class AuthenticationErrorEvent {
	private long mId;

	private FirebaseError mFirebaseError;


	public AuthenticationErrorEvent( long id, FirebaseError firebaseError ) {
		mId = id;
		mFirebaseError = firebaseError;
	}


	public FirebaseError getFirebaseError() {
		return mFirebaseError;
	}

	public long getId() {
		return mId;
	}
}
