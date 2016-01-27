package com.chopping.rest;

import java.io.IOException;

import android.util.Log;

import com.chopping.bus.RestApiResponseEvent;
import com.chopping.utils.RestUtils;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Architecture for working with Retrofit.
 *
 * @author Xinyue Zhao
 */
public class RestApiManager {
	/**
	 * The id of manger.
	 */
	private long mId;


	/**
	 * Initialize the manager.
	 */

	public void onCreate() {
		setId( java.lang.System.currentTimeMillis() );
	}

	/**
	 * Set the id of manger.
	 */
	private void setId( long id ) {
		mId = id;
	}

	/**
	 * Run a rest request for non-delete or update async.
	 *
	 * @param call
	 * 		The {@link Call} to the request.
	 * @param requestObject
	 * 		The request data to post on server.
	 */
	public <LD extends RestObject, SD extends RestObject> void execAsync( Call<SD> call, LD requestObject ) {
		execAsync(
				call,
				requestObject,
				RestObject.NOT_SYNCED,
				RestObject.SYNCED
		);
	}

	/**
	 * Run a rest request for delete async.
	 *
	 * @param call
	 * 		The {@link Call} to the request.
	 * @param requestObject
	 * 		The request data to post on server.
	 */
	public <LD extends RestObject, SD extends RestObject> void deleteAsync( Call<SD> call, LD requestObject ) {
		execAsync(
				call,
				requestObject,
				RestObject.DELETE,
				RestObject.DELETE_SYNCED
		);
	}

	/**
	 * Run a rest request for update async.
	 *
	 * @param call
	 * 		The {@link Call} to the request.
	 * @param requestObject
	 * 		The request data to post on server.
	 */
	public <LD extends RestObject, SD extends RestObject> void updateAsync( Call<SD> call, LD requestObject ) {
		execAsync(
				call,
				requestObject,
				RestObject.UPDATE,
				RestObject.UPDATE_SYNCED
		);
	}

	/**
	 * Run a rest request async.
	 *
	 * @param call
	 * 		The {@link Call} to the request.
	 * @param requestObject
	 * 		The request data to post on server.
	 * @param statusBefore
	 * 		The status of begin request.
	 * @param statusAfter
	 * 		The status of after request.
	 */
	public <LD extends RestObject, SD extends RestObject> void execAsync( Call<SD> call, LD requestObject, int statusBefore, final int statusAfter
	) {
		//MAKE A LOCAL STATUS.
		requestObject.updateDB( statusBefore );
		//CALL API.
		call.enqueue( new  Callback<SD>() {
			@Override
			public void onResponse( Response<SD> response  ) {
				if( response.isSuccess() ) {
					//-------------------------
					//THE REQUEST IS SUCCESS.
					//-------------------------
					RestObject serverData = response.body();
					//UPDATE LOCAL STATUS.
					serverData.updateDB( statusAfter );
					EventBus.getDefault().post( new RestApiResponseEvent( true ) );
				}
			}

			@Override
			public void onFailure( Throwable t ) {
				Log.d(
						getClass().getSimpleName(),
						"onFailure: " + t.toString()

				);
				EventBus.getDefault().post( new RestApiResponseEvent( false ) );
			}
		} );
	}


	/**
	 * Run a rest request for non-delete or update sync.
	 *
	 * @param call
	 * 		The {@link Call} to the request.
	 * @param requestObject
	 * 		The request data to post on server.
	 */
	public <LD extends RestObject, SD extends RestObject> void execSync( Call<SD> call, LD requestObject ) {
		execSync(
				call,
				requestObject,
				RestObject.NOT_SYNCED,
				RestObject.SYNCED
		);
	}

	/**
	 * Run a rest request for delete sync.
	 *
	 * @param call
	 * 		The {@link Call} to the request.
	 * @param requestObject
	 * 		The request data to post on server.
	 */
	public <LD extends RestObject, SD extends RestObject> void deleteSync( Call<SD> call, LD requestObject ) {
		execSync(
				call,
				requestObject,
				RestObject.DELETE,
				RestObject.DELETE_SYNCED
		);
	}


	/**
	 * Run a rest request for update sync.
	 *
	 * @param call
	 * 		The {@link Call} to the request.
	 * @param requestObject
	 * 		The request data to post on server.
	 */
	public <LD extends RestObject, SD extends RestObject> void updateSync( Call<SD> call, LD requestObject ) {
		execSync(
				call,
				requestObject,
				RestObject.UPDATE,
				RestObject.UPDATE_SYNCED
		);
	}

	/**
	 * Run a rest request sync.
	 *
	 * @param call
	 * 		The {@link Call} to the request.
	 * @param requestObject
	 * 		The request data to post on server.
	 * @param statusBefore
	 * 		The status of begin request.
	 * @param statusAfter
	 * 		The status of after request.
	 */
	public <LD extends RestObject, SD extends RestObject> void execSync( Call<SD> call, LD requestObject, int statusBefore, int statusAfter ) {
		//MAKE A LOCAL STATUS.
		requestObject.updateDB( statusBefore );
		try {
			//CALL API.
			Response<SD> response = call.execute();
			if( response.isSuccess() ) {
				//-------------------------
				//THE REQUEST IS SUCCESS.
				//-------------------------
				RestObject serverData = response.body();
				//UPDATE LOCAL STATUS.
				serverData.updateDB( statusAfter );
			}
		} catch( IOException e ) {
			Log.e(
					"RestApiManager",
					"execSync: " + e.getMessage()

			);
		}
	}

	public void executePending( ExecutePending exp, int statusBefore ) {
		RestUtils.executePending( exp, statusBefore );
	}
}
