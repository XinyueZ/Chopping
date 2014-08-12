package com.chopping.activities;

import com.chopping.R;
import com.chopping.application.BasicPrefs;
import com.chopping.application.ErrorHandler;
import com.chopping.bus.BusProvider;
import com.chopping.exceptions.CanNotOpenOrFindAppPropertiesException;
import com.chopping.exceptions.InvalidAppPropertiesException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * General base activity, with error-handling, loading configuration etc.
 */
public abstract class BaseActivity extends ActionBarActivity {
	/**
	 * Basic layout that contains an error-handling(a sticky).
	 */
	private static final int LAYOUT_BASE = R.layout.activity_base;
	/**
	 * A logical that contains controlling over all network-errors.
	 */
	private final ErrorHandler mErrorHandler = new ErrorHandler();

	@Override
	protected void onResume() {
		BusProvider.getBus().register(mErrorHandler);
		BusProvider.getBus().register(this);
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
		BusProvider.getBus().unregister(this);
		BusProvider.getBus().unregister(mErrorHandler);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mErrorHandler.onDestroy();
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(LAYOUT_BASE);
		ViewGroup content = (ViewGroup) findViewById(R.id.content_fl);
		content.addView(getLayoutInflater().inflate(layoutResID, null),
				new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));

		mErrorHandler.onCreate(this, null);
	}

	/**
	 * App that use this Chopping should know the preference-storage.
	 *
	 * @return An instance of {@link com.chopping.application.BasicPrefs}.
	 */
	protected abstract BasicPrefs getPrefs();
}
