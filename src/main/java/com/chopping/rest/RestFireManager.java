package com.chopping.rest;

import android.app.Application;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.Firebase.AuthResultHandler;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.chopping.bus.AuthenticatedEvent;
import com.chopping.bus.AuthenticationErrorEvent;

import de.greenrobot.event.EventBus;

/**
 * Architecture for working with Firebase.
 *
 * @author Xinyue Zhao
 */
public class RestFireManager implements AuthResultHandler, ChildEventListener {
	//Firebase.
	private String                      mUrl;
	private String                      mAuth;
	private Firebase                    mFirebase;
	private Query                       mQuery;
	private int                         mLastLimit;
	private String                      mOrderBy;
	private Class<? extends RestObject> mRespType;
	/**
	 * The id of manger.
	 */
	private long                        mId;
	/**
	 * A flag to indicate whether listener of Firebase has been assigned or not.
	 */
	private boolean                     mAddedListener;

	/**
	 * Constructor of {@link RestFireManager}
	 *
	 * @param url
	 * 		The base-url to used Firebase.
	 * @param auth
	 * 		The auth-info to used Firebase.
	 * @param lastLimit
	 * 		The last-limit data of data.
	 * @param orderBy
	 * 		Order by some keys.
	 */
	public RestFireManager( String url, String auth, int lastLimit, String orderBy ) {
		mUrl = url;
		mAuth = auth;
		mLastLimit = lastLimit;
		mOrderBy = orderBy;
	}

	/**
	 * Initialize the manager.
	 *
	 * @param app
	 * 		{@link Application} The application domain to control manager.
	 */
	public void onCreate( Application app ) {
		setId( System.currentTimeMillis() );
		Firebase.setAndroidContext( app );
		mFirebase = new Firebase( mUrl );
		mFirebase.keepSynced( true );
		mQuery = mFirebase.orderByChild( mOrderBy ).limitToLast( mLastLimit );
		mFirebase.authWithCustomToken(
				mAuth,
				this
		);
	}

	/**
	 * Set the id of manger.
	 */
	private void setId( long id ) {
		mId = id;
	}

	/**
	 * Called when do not need manager.
	 */
	public void onDestroy() {
		mQuery.removeEventListener( this );
		mAddedListener = false;
	}


	//[AuthResultHandler]
	@Override
	public void onAuthenticated( AuthData authData ) {
		EventBus.getDefault()
				.post( new AuthenticatedEvent(
						mId,
						authData
				) );
	}

	@Override
	public void onAuthenticationError( FirebaseError firebaseError ) {
		EventBus.getDefault()
				.post( new AuthenticationErrorEvent(
						mId,
						firebaseError
				) );
	}


	/**
	 * Save data on Firebase.
	 *
	 * @param newData
	 * 		{@link RestObject} to save on Firebase.
	 */
	public void save( RestObject newData ) {
		if( !mAddedListener ) {
			mQuery.addChildEventListener( this );
			mAddedListener = true;
		}
		saveInBackground( newData );
	}

	/**
	 * Save data on Firebase in background, call this in thread.
	 *
	 * @param newData
	 * 		{@link RestObject} to save on Firebase.
	 */
	public void saveInBackground( RestObject newData ) {
		mRespType = newData.getClass();
		newData.updateDB( RestObject.NOT_SYNCED );
		mFirebase.child( newData.getReqId() )
				 .setValue( newData );
		mFirebase.push();
	}

	/**
	 * Get all data from Firebase of type {@code respType}.
	 *
	 * @param respType
	 * 		Server data type {@code respType}.
	 */
	public void selectAll( Class<? extends RestObject> respType ) {
		mRespType = respType;
		if( mAddedListener ) {
			mQuery.removeEventListener( this );
			mAddedListener = false;
		}
		mQuery.addChildEventListener( this );
		mAddedListener = true;
	}

	/**
	 * Do pending request.
	 *
	 * @param exp
	 * 		{@link Application} context.
	 */
	public void executePending( ExecutePending exp ) {
		RestUtils.executePending( exp );
	}

	//[ChildEventListener]
	@Override
	public void onChildAdded( DataSnapshot dataSnapshot, String s ) {
		RestObject serverData = dataSnapshot.getValue( mRespType );
		serverData.updateDB( RestObject.SYNCED );
	}

	@Override
	public void onChildChanged( DataSnapshot dataSnapshot, String s ) {
		Log.d(
				getClass().getSimpleName(),
				"onChildChanged: " + dataSnapshot.getValue()
		);
	}

	@Override
	public void onChildRemoved( DataSnapshot dataSnapshot ) {
		Log.d(
				getClass().getSimpleName(),
				"onChildRemoved: " + dataSnapshot.getValue()
		);
	}

	@Override
	public void onChildMoved( DataSnapshot dataSnapshot, String s ) {
		Log.d(
				getClass().getSimpleName(),
				"onChildMoved: " + dataSnapshot.getValue()
		);
	}

	@Override
	public void onCancelled( FirebaseError firebaseError ) {
		Log.d(
				getClass().getSimpleName(),
				"onCancelled: " + firebaseError.toString()
		);
	}
}
