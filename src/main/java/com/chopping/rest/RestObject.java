package com.chopping.rest;


import java.io.Serializable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmObject;

public abstract class RestObject implements Serializable {
	public static final int NOT_SYNCED     = 0;
	public static final int SYNCED         = 1;
	public static final int DELETE         = 2;
	public static final int DELETE_SYNCED  = 3;
	public static final int UPDATE         = 4;
	public static final int UPDATE_SYNCED = 5;


	//Request ID --> must be "reqId" for json/gson/jackson.
	public abstract String getReqId();

	//Time to fire the request --> must be "reqTime" for json/gson/jackson.
	public abstract long getReqTime();


	//Update database when this object changed.
	public void updateDB( int status ) {
		Realm db = Realm.getDefaultInstance();
		db.beginTransaction();
		RealmObject[] instances = newInstances(
				db,
				status
		);
		for( RealmObject instance : instances ) {
			switch( status ) {
				case DELETE_SYNCED:
					instance.removeFromRealm();
					break;
				default:
					db.copyToRealmOrUpdate( instance );
					break;
			}
		}
		db.commitTransaction();
		if( !db.isClosed() ) {
			db.close();
		}
	}

	//Create database items that will be updated into database.
	protected abstract
	@NonNull
	RealmObject[] newInstances( Realm db, int status );

	public abstract Class<? extends RealmObject> DBType();

	public
	@Nullable
	RestObject newFromDB( RealmObject dbItem ) {
		return null;
	}
}
