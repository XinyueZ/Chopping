package com.chopping.activities;

import java.util.Set;

import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;

import com.chopping.utils.RestUtils;
import com.chopping.bus.UpdateNetworkStatusEvent;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * A common base {@link android.app.Activity} that calls REST.
 *
 * @author Xinyue Zhao
 */
public abstract class RestfulActivity extends AppCompatActivity {

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link UpdateNetworkStatusEvent}.
	 *
	 * @param e
	 * 		Event {@link UpdateNetworkStatusEvent}.
	 */
	public void onEventMainThread( UpdateNetworkStatusEvent e ) {
		if( e.isConnected() ) {
			onNetworkConnected();
		}
	}

	//------------------------------------------------


	private Realm                               mRealm;
	private RealmResults<? extends RealmObject> mRealmData;
	private RealmChangeListener mListListener = new RealmChangeListener() {
		@Override
		public void onChange() {
			buildRestUI();
		}
	};


	private void load() {
		sendPending();
		if( !RestUtils.shouldLoadLocal( getApplication()) ) {
			loadList();
		}
	}


	protected abstract void initDataBinding();

	protected void initRestUI( ArrayMap<String, String> valueContains) {
		RealmQuery<? extends RealmObject> q = mRealm.where( getDataClazz() );
		Set<String> keys = valueContains.keySet();
		for(String key : keys) {
			q.contains( key, valueContains.get( key ) );
		}
		mRealmData = q.findAllSortedAsync(
								   "reqTime",
								   Sort.DESCENDING
						   );
		mRealmData.addChangeListener( mListListener );
		if( RestUtils.shouldLoadLocal( getApplication() ) ) {
			buildRestUI();
		}
	}

	protected void initRestUI() {
		mRealmData = mRealm.where( getDataClazz() )
						   .findAllSortedAsync(
								   "reqTime",
								   Sort.DESCENDING
						   );
		mRealmData.addChangeListener( mListListener );
		if( RestUtils.shouldLoadLocal( getApplication() ) ) {
			buildRestUI();
		}
	}

	protected abstract Class<? extends RealmObject> getDataClazz();

	protected abstract void sendPending();

	protected abstract void loadList();

	protected abstract void buildRestUI();

	/**
	 * Callback when event of network status changed and connection is connected.
	 */
	protected void onNetworkConnected() {
		sendPending();
	}

	protected RealmResults<? extends RealmObject> getData() {
		return mRealmData;
	}



	protected boolean isDataLoaded() {
		return mRealmData.isLoaded();
	}

	@Override
	protected void onResume() {
		super.onResume();
		EventBus.getDefault()
				.registerSticky( this );

	}

	@Override
	protected void onPause() {
		EventBus.getDefault()
				.unregister( this );
		super.onPause();
	}

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		mRealm = Realm.getDefaultInstance();
		initDataBinding();
		initRestUI();
		load();
	}

	@Override
	protected void onDestroy() {
		if( mRealmData != null && mListListener != null ) {
			mRealmData.removeChangeListener( mListListener );
		}
		if( mRealm != null && !mRealm.isClosed() ) {
			mRealm.close();
		}
		super.onDestroy();
	}
}
