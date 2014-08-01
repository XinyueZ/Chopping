package com.chopping.activities;

import com.chopping.R;
import com.chopping.application.ErrorHandling;
import com.chopping.bus.BusProvider;

import android.support.v7.app.ActionBarActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * General base activity, with error-handling, loading configuration etc.
 */
public class BaseActivity extends ActionBarActivity {
	/**
	 * Basic layout that contains a error-handling(a sticky).
	 */
	private static final int LAYOUT_BASE = R.layout.activity_base;
	private final ErrorHandling mErrorHandling = new ErrorHandling();

	@Override
	protected void onResume() {
		super.onResume();
		BusProvider.getBus().register(mErrorHandling);
	}

	@Override
	protected void onPause() {
		super.onPause();
		BusProvider.getBus().unregister(mErrorHandling);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mErrorHandling.onDestroy();
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(LAYOUT_BASE);
		ViewGroup content = (ViewGroup) findViewById(R.id.content_fl);
		content.addView(getLayoutInflater().inflate(layoutResID, null),
				new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));

		mErrorHandling.onCreate(this);
	}
}
