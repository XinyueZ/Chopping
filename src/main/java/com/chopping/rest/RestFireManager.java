package com.chopping.rest;

import android.app.Application;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;
import android.util.Log;

import com.chopping.bus.AuthenticatedEvent;
import com.chopping.bus.AuthenticationErrorEvent;
import com.chopping.utils.RestUtils;
import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.Firebase.AuthResultHandler;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;

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
	private int                         mLimit;
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
	 * Collection of Firebase keys associated with request-id.
	 */
	private SimpleArrayMap<String, String> mKeyList = new ArrayMap<>();


	/**
	 * Constructor of {@link RestFireManager}
	 *
	 * @param url
	 * 		The base-url to used Firebase.
	 * @param auth
	 * 		The auth-info to used Firebase.
	 * @param limit
	 * 		The last-limit data of data.
	 * @param orderBy
	 * 		Order by some keys.
	 */
	public RestFireManager( String url, String auth, int limit, String orderBy ) {
		mUrl = url;
		mAuth = auth;
		mLimit = limit;
		mOrderBy = orderBy;
	}


	/**
	 * Constructor of {@link RestFireManager}
	 *
	 * @param url
	 * 		The base-url to used Firebase.
	 * @param auth
	 * 		The auth-info to used Firebase.
	 * @param limit
	 * 		The last-limit data of data.
	 */
	public RestFireManager( String url, String auth, int limit ) {
		this(
				url,
				auth,
				limit,
				null
		);
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
		mQuery = mFirebase.limitToLast( mLimit );
		if( !TextUtils.isEmpty( mOrderBy ) ) {
			mQuery = mQuery.orderByChild( mOrderBy );
		}
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
	 * Delete data on Firebase.
	 *
	 * @param data
	 * 		{@link RestObject} to delete on Firebase.
	 */
	public void delete( RestObject data ) {
		if( !mAddedListener ) {
			mQuery.addChildEventListener( this );
			mAddedListener = true;
		}
		deleteInBackground( data );
	}

	/**
	 * Delete data on Firebase in background, call this in thread.
	 *
	 * @param data
	 * 		{@link RestObject} to delete on Firebase.
	 */
	public void deleteInBackground( RestObject data ) {
		mRespType = data.getClass();
		data.updateDB( RestObject.DELETE );
		mFirebase.child( data.getReqId() )
				 .removeValue();
	}


	/**
	 * Change data on Firebase.
	 *
	 * @param data
	 * 		{@link RestObject} to change on Firebase.
	 */
	public void update( RestObject data ) {
		if( !mAddedListener ) {
			mQuery.addChildEventListener( this );
			mAddedListener = true;
		}
		updateInBackground( data );
	}

	/**
	 * Change data on Firebase in background, call this in thread.
	 *
	 * @param data
	 * 		{@link RestObject} to change on Firebase.
	 */
	public void updateInBackground( RestObject data ) {
		mRespType = data.getClass();
		data.updateDB( RestObject.UPDATE );
		mFirebase.child( data.getReqId() )
				 .setValue( data );
	}


	/**
	 * Get all data from Firebase of type {@code respType} with {@link #mLimit}.
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
	 * Get all data from {@code fromObject}.
	 *
	 * @param fromObject
	 * 		The object to start selection.
	 */
	public void selectFrom( RestObject fromObject ) {
		mRespType = fromObject.getClass();
		if( mAddedListener ) {
			mQuery.removeEventListener( this );
			mAddedListener = false;
		}
		String key = mKeyList.get( fromObject.getReqId() );
		mQuery = mFirebase.endAt(
				null,
				key
		).limitToLast( mLimit );
		mQuery.addChildEventListener( this );
		mAddedListener = true;
	}


	/**
	 * Do pending request.
	 *
	 * @param exp
	 * 		{@link Application} context.
	 */
	public void executePending( ExecutePending exp, int statusBefore ) {
		RestUtils.executePending(
				exp,
				statusBefore
		);
	}

	//[ChildEventListener]
	@Override
	public void onChildAdded( DataSnapshot dataSnapshot, String s ) {
		RestObject serverData = dataSnapshot.getValue( mRespType );
		serverData.updateDB( RestObject.SYNCED );
		if( !TextUtils.isEmpty( dataSnapshot.getKey() ) ) {
			mKeyList.put(
					serverData.getReqId(),
					dataSnapshot.getKey()
			);
			Log.i(
					"Fire Mgr",
					"onChildAdded: <-" + dataSnapshot.getKey()
			);
			Log.i(
					"Fire Mgr",
					"onChildAdded: " + dataSnapshot.getKey()
			);
		}
	}

	@Override
	public void onChildRemoved( DataSnapshot dataSnapshot ) {
		RestObject serverData = dataSnapshot.getValue( mRespType );
		String     reqId      = serverData.getReqId();
		String      key = dataSnapshot.getKey();
		serverData.updateDB( RestObject.DELETE_SYNCED );
		if( !TextUtils.isEmpty( dataSnapshot.getKey() ) ) {
			mKeyList.remove( reqId );
			Log.i(
					"Fire Mgr",
					"onChildRemoved: ->" + dataSnapshot.getKey()
			);
			Log.i(
					"Fire Mgr",
					"onChildRemoved: " + key
			);
		}
	}

	@Override
	public void onChildChanged( DataSnapshot dataSnapshot, String s ) {
		RestObject serverData = dataSnapshot.getValue( mRespType );
		serverData.updateDB( RestObject.UPDATE_SYNCED );
		if( !TextUtils.isEmpty( dataSnapshot.getKey() ) ) {
			mKeyList.put(
					serverData.getReqId(),
					dataSnapshot.getKey()
			);
			Log.i(
					"Fire Mgr",
					"onChildChanged: #" + dataSnapshot.getKey()
			);
			Log.i(
					"Fire Mgr",
					"onChildChanged: " + dataSnapshot.getKey()
			);
		}
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
