package com.chopping.activities;

import com.chopping.R;
import com.chopping.application.BasicPrefs;
import com.chopping.application.ErrorHandler;
import com.chopping.exceptions.CanNotOpenOrFindAppPropertiesException;
import com.chopping.exceptions.InvalidAppPropertiesException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import de.greenrobot.event.EventBus;

/**
 * General base activity, with error-handling, loading configuration etc.
 */
public abstract class BaseActivity extends ActionBarActivity {
	/**
	 * Basic layout that contains an error-handling(a sticky).
	 */
	private static final int LAYOUT_BASE = R.layout.activity_base;
	/**
	 * EXTRAS. Status of available of error-handling. Default is {@code true}
	 * <p/>
	 * See {@link #mIsErrorHandlerAvailable}.
	 */
	private static final String EXTRAS_ERR_AVA = "err.ava";
	/**
	 * A logical that contains controlling over all network-errors.
	 */
	private ErrorHandler mErrorHandler;
	/**
	 * {@code true} if {@link #mErrorHandler} works and shows associated {@link com.chopping.activities.ErrorHandlerActivity}.
	 */
	private boolean mIsErrorHandlerAvailable = true;
	
	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link }
	 *
	 * @param e
	 * 		Event {@link  }.
	 */
	public void onEvent(Object e) {

	}

	//------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mIsErrorHandlerAvailable = savedInstanceState.getBoolean(EXTRAS_ERR_AVA, true);
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
		super.onDestroy();
		mErrorHandler.onDestroy();
		mErrorHandler = null;
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(LAYOUT_BASE);
		ViewGroup content = (ViewGroup) findViewById(R.id.content_fl);
		content.addView(getLayoutInflater().inflate(layoutResID, null),
				new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
	}

	/**
	 * Set {@code true} if {@link #mErrorHandler} works and shows associated {@link
	 * com.chopping.activities.ErrorHandlerActivity}.
	 *
	 * @throws NullPointerException
	 * 		must be thrown if it is called at lest after {@link android.app.Activity#onPostCreate(android.os.Bundle)}.
	 */
	protected void setErrorHandlerAvailable(boolean _isErrorHandlerAvailable) {
		if (mErrorHandler == null) {
			throw new NullPointerException(
					"BaseActivity#setErrorHandlerAvailable must be call at least after onPostCreate().");
		}
		mIsErrorHandlerAvailable = _isErrorHandlerAvailable;
		mErrorHandler.setErrorHandlerAvailable(mIsErrorHandlerAvailable);
	}

	/**
	 * Initialize {@link com.chopping.application.ErrorHandler}.
	 */
	private void initErrorHandler() {
		if (mErrorHandler == null) {
			mErrorHandler = new ErrorHandler();
		}
		mErrorHandler.onCreate(this, null);
		mErrorHandler.setErrorHandlerAvailable(mIsErrorHandlerAvailable);
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
	 * <b>Default is {@code false}</b>.
	 */
	protected boolean isStickyAvailable() {
		return false;
	}
}
