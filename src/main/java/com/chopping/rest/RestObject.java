package com.chopping.rest;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmObject;

public abstract class RestObject {
	public static final int NOT_SYNCED = 0;
	public static final int SYNCED     = 1;

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
			db.copyToRealmOrUpdate( instance );
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
	RestObject fromDB( RealmObject dbItem ) {
		return null;
	}
}
