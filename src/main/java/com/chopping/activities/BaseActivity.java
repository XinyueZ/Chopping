package com.chopping.activities;

import java.io.File;
import java.util.List;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.chopping.R;
import com.chopping.application.BasicPrefs;
import com.chopping.application.ErrorHandler;
import com.chopping.application.LL;
import com.chopping.bus.AirplaneModeOnEvent;
import com.chopping.bus.ApplicationConfigurationDownloadedEvent;
import com.chopping.bus.ApplicationConfigurationLoadingIgnoredEvent;
import com.chopping.bus.ReloadEvent;
import com.chopping.exceptions.CanNotOpenOrFindAppPropertiesException;
import com.chopping.exceptions.InvalidAppPropertiesException;

import de.greenrobot.event.EventBus;

import static android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE;

/**
 * General base activity, with error-handling, loading configuration etc.
 */
public abstract class BaseActivity extends AppCompatActivity {
	/**
	 * EXTRAS. Status of available of error-handling. Default is {@code true}
	 * <p/>
	 * See {@link #mErrorHandlerAvailable}.
	 */
	private static final String EXTRAS_ERR_AVA = "err.ava";
	/**
	 * A logical that contains controlling over all network-errors.
	 */
	private ErrorHandler mErrorHandler;
	/**
	 * {@code true} if {@link #mErrorHandler} works and shows associated {@link com.chopping.activities.ErrorHandlerActivity}.
	 */
	private boolean mErrorHandlerAvailable = true;
	/**
	 * An id for a downloading instance.
	 */
	private long downloadId;
	/**
	 * A progress indicator while downloading update.
	 */
	private ProgressDialog mLoadNewVersionPgdlg;
	/**
	 * {@link android.content.IntentFilter} for completing download of new update version.
	 */
	private IntentFilter downloadCompleteIntentFilter = new IntentFilter(ACTION_DOWNLOAD_COMPLETE);

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.chopping.bus.ApplicationConfigurationDownloadedEvent}
	 *
	 * @param e
	 * 		Event {@link  com.chopping.bus.ApplicationConfigurationDownloadedEvent}.
	 */
	public void onEvent(ApplicationConfigurationDownloadedEvent e) {
		onAppConfigLoaded();
		downloadUpdate();
	}

	/**
	 * Handler for {@link com.chopping.bus.ApplicationConfigurationLoadingIgnoredEvent}.
	 *
	 * @param e
	 * 		Event {@link com.chopping.bus.ApplicationConfigurationLoadingIgnoredEvent}.
	 */
	public void onEvent(ApplicationConfigurationLoadingIgnoredEvent e) {
		LL.i("Ignored a change to load application's configuration.");
		onAppConfigIgnored();
	}

	/**
	 * Handler for {@link com.android.volley.VolleyError}
	 *
	 * @param e
	 * 		Event {@link  com.android.volley.VolleyError}.
	 */
	public void onEvent(VolleyError e) {
		onNetworkError();
	}

	/**
	 * Handler for {@link com.chopping.bus.ReloadEvent}
	 *
	 * @param e
	 * 		Event {@link  com.chopping.bus.ReloadEvent}.
	 */
	public void onEvent(ReloadEvent e) {
		onReload();
		EventBus.getDefault().removeStickyEvent(e);
	}

	/**
	 * Handler for {@link com.chopping.bus.AirplaneModeOnEvent}
	 *
	 * @param e
	 * 		Event {@link  com.chopping.bus.AirplaneModeOnEvent}.
	 */
	public void onEvent(AirplaneModeOnEvent e) {
		if (mErrorHandlerAvailable) {
			mErrorHandler.openStickyBanner(this, true);
			mErrorHandler.setText(null, true);
			EventBus.getDefault().removeStickyEvent(AirplaneModeOnEvent.class);
		}
	}

	//------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mErrorHandlerAvailable = savedInstanceState.getBoolean(EXTRAS_ERR_AVA, true);
		}

		getApplication().registerReceiver(downloadCompleteReceiver, downloadCompleteIntentFilter);
		//Resume a download indicator for not completed loading process.
		if (getPrefs().isDownloadingUpdate()) {
			String loadNewVersion = getString(R.string.msg_update_download, getPrefs().getUpdateVersionName());
			mLoadNewVersionPgdlg = ProgressDialog.show(BaseActivity.this, getString(R.string.app_name), loadNewVersion);
			mLoadNewVersionPgdlg.setCancelable(false);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		initErrorHandler();
	}

	@Override
	protected void onResume() {
		if (isStickyAvailable()) {
			EventBus.getDefault().registerSticky(this);
		} else {
			EventBus.getDefault().register(this);
		}
		EventBus.getDefault().register(mErrorHandler);
		super.onResume();

		String mightError = null;
		try {
			getPrefs().downloadApplicationConfiguration();
		} catch (InvalidAppPropertiesException _e) {
			mightError = _e.getMessage();
		} catch (CanNotOpenOrFindAppPropertiesException _e) {
			mightError = _e.getMessage();
		}
		if (mightError != null) {
			new AlertDialog.Builder(this).setTitle(R.string.app_name).setMessage(mightError).setCancelable(false)
					.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).create().show();
		}
	}

	@Override
	protected void onPause() {
		EventBus.getDefault().unregister(this);
		EventBus.getDefault().unregister(mErrorHandler);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		getApplication().unregisterReceiver(downloadCompleteReceiver);
		super.onDestroy();
		mErrorHandler.onDestroy();
		mErrorHandler = null;
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);

		ViewGroup errorVG = (ViewGroup) findViewById(R.id.error_content);
		if (errorVG != null) {
			View stickyV = getLayoutInflater().inflate(R.layout.inc_err_sticky, errorVG, false);
			errorVG.addView(stickyV);
		}
	}

	public void setUpErrorHandling(ViewGroup errorContentVg) {
		ViewGroup errorVG = errorContentVg;
		if (errorVG != null) {
			View stickyV = getLayoutInflater().inflate(R.layout.inc_err_sticky, errorVG, false);
			errorVG.addView(stickyV);
		}
	}

	/**
	 * Set {@code true} if {@link #mErrorHandler} works and shows associated {@link
	 * com.chopping.activities.ErrorHandlerActivity}.
	 *
	 * @throws NullPointerException
	 * 		must be thrown if it is called at lest after {@link android.app.Activity#onPostCreate(android.os.Bundle)}.
	 */
	protected void setErrorHandlerAvailable(boolean isErrorHandlerAvailable) {
		if (mErrorHandler == null) {
			throw new NullPointerException(
					"BaseActivity#setErrorHandlerAvailable must be call at least after onPostCreate().");
		}
		mErrorHandlerAvailable = isErrorHandlerAvailable;
		mErrorHandler.setErrorHandlerAvailable(mErrorHandlerAvailable);
	}

	/**
	 * Initialize {@link com.chopping.application.ErrorHandler}.
	 */
	private void initErrorHandler() {
		if (mErrorHandler == null) {
			mErrorHandler = new ErrorHandler();
		}
		mErrorHandler.onCreate(this, null);
		mErrorHandler.setErrorHandlerAvailable(mErrorHandlerAvailable);
	}

	/**
	 * App that use this Chopping should know the preference-storage.
	 *
	 * @return An instance of {@link com.chopping.application.BasicPrefs}.
	 */
	protected abstract BasicPrefs getPrefs();

	/**
	 * Is the {@link android.app.Activity}({@link android.support.v4.app.FragmentActivity}) ready to subscribe a
	 * sticky-event or not.
	 *
	 * @return {@code true} if the {@link android.app.Activity}({@link android.support.v4.app.FragmentActivity})
	 * available for sticky-events inc. normal events.
	 * <p/>
	 * <b>Default is {@code true}</b>.
	 */
	protected boolean isStickyAvailable() {
		return true;
	}

	/**
	 * Callback after App's config loaded.
	 */
	protected void onAppConfigLoaded() {

	}

	/**
	 * Callback after ignoring a config loading.
	 */
	protected void onAppConfigIgnored() {
	}

	/**
	 * Callback when {@link com.android.volley.VolleyError} occurred.
	 */
	protected void onNetworkError() {

	}

	/**
	 * Callback when a reload({@link com.chopping.bus.ReloadEvent}) is required.
	 */
	protected void onReload() {

	}

	/**
	 * Call this method to decide whether a sticky on top which animates to bottom to show information about network
	 * error or an {@link com.chopping.activities.ErrorHandlerActivity}({@link com.chopping.fragments.ErrorHandlerFragment}).
	 * <p/>
	 * <p/>
	 * <b>Default shows an {@link com.chopping.activities.ErrorHandlerActivity}({@link
	 * com.chopping.fragments.ErrorHandlerFragment})</b>
	 *
	 * @param shownDataOnUI
	 * 		Set {@code true}, then a sticky will always show when network error happens.
	 */
	protected void setHasShownDataOnUI(boolean shownDataOnUI) {
		if(mErrorHandler!=null) {
			mErrorHandler.setHasDataOnUI(shownDataOnUI);
		}
	}


	/**
	 * Helper method for install an APK file from file's uri.
	 *
	 * @param uri
	 * 		An uri of file.
	 */
	private void installApk(String uri) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.parse(uri), "application/vnd.android.package-archive");
		startActivity(intent);
	}


	/**
	 * Download a new update if possible.
	 * <p/>
	 * See. <a href="http://jhshi.me/2013/12/02/how-to-use-downloadmanager">How to Use Android DownloadManager</a>
	 * <p/>
	 * See. <a href="http://blog.sina.cn/dpool/blog/s/blog_7575ed8b010148k0.html">Android--版本自动更新实现实例(系统自带DownloadManager实现)</a>
	 */
	private void downloadUpdate() {
		if (getPrefs().getUpdateVersionCode() > getPrefs().getAppCode()) {
			String newVersion = getString(R.string.msg_update, getPrefs().getUpdateVersionName());
			AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.app_name).setMessage(
					newVersion).setPositiveButton(R.string.btn_now_load, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String loadNewVersion = getString(R.string.msg_update_download, getPrefs().getUpdateVersionName());

					DownloadManager.Request request = new DownloadManager.Request(Uri.parse(getPrefs().getUpdateUrl()));
					request.setDescription(loadNewVersion);
					request.setVisibleInDownloadsUi(true);
					request.setMimeType("application/vnd.android.package-archive");
					DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
					Uri uri = Uri.parse(getPrefs().getUpdateUrl());
					List<String> segments = uri.getPathSegments();
					String fileName = segments.get(segments.size() - 1);
					request.setDestinationInExternalPublicDir(getString(R.string.app_name), fileName);
					new File(Environment.getExternalStoragePublicDirectory(getString(R.string.app_name)), fileName);
					getPrefs().setDownloadingUpdate(true);
					downloadId = downloadManager.enqueue(request);

					mLoadNewVersionPgdlg = ProgressDialog.show(BaseActivity.this, getString(R.string.app_name),
							loadNewVersion);
					mLoadNewVersionPgdlg.setCancelable(false);
				}
			}).setCancelable(!getPrefs().isUpdateMandatory());
			if (!getPrefs().isUpdateMandatory()) {
				builder.setNegativeButton(R.string.btn_not_yet_load, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			}
			builder.create().show();
		} else {
			LL.i("No update found.");
		}
	}

	/**
	 * {@link android.content.BroadcastReceiver} for completing download of new update version.
	 */
	private BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//First we confirm that the downloading is finished.
			getPrefs().setDownloadingUpdate(false);

			//Not a good id, download fail.
			long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
			if (id != downloadId) {
				return;
			}

			DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
			DownloadManager.Query query = new DownloadManager.Query();
			query.setFilterById(downloadId);
			Cursor cursor = downloadManager.query(query);

			//It shouldn't be empty, but just in case, download fail.
			if (!cursor.moveToFirst()) {
				return;
			}

			int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
			//Download not success.
			if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex)) {
				mLoadNewVersionPgdlg.dismiss();
				return;
			}

			//Download all finished, and open installation.
			if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(statusIndex)) {
				int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
				String downloadedPackageUriString = cursor.getString(uriIndex);
				installApk(downloadedPackageUriString);
				mLoadNewVersionPgdlg.dismiss();
			}
		}
	};
}
