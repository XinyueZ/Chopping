package com.chopping.activities;

import com.chopping.R;
import com.chopping.application.ErrorHandler;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import de.greenrobot.event.EventBus;

/**
 * {@link com.chopping.activities.ErrorHandlerActivity} provides basic logical for an error happens when internet is not
 * available.
 * <p/>
 * <b>Relate class</b>
 * <p/>
 * {@link com.chopping.fragments.ErrorHandlerFragment}
 */
public class ErrorHandlerActivity extends ActionBarActivity {
	private static final int LAYOUT = R.layout.activity_errorhandler;
	/**
	 * Extras. {@link java.lang.String} description of error.
	 * <p/>
	 * Equal to {@link com.chopping.fragments.ErrorHandlerFragment#EXTRAS_ERR_MSG}.
	 */
	public static final String EXTRAS_ERR_MSG = ErrorHandler.EXTRAS_ERR_MSG;
	/**
	 * Message {@link android.widget.TextView}.
	 */
	private TextView mErrMsgTv;
	/**
	 * Retry {@link android.widget.Button}.
	 */
	private Button mErrRetryBtn;

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
	protected void onResume() {
		EventBus.getDefault().register(this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		EventBus.getDefault().unregister(this);
		super.onPause();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(LAYOUT);
		mErrMsgTv = (TextView) findViewById(R.id.err_msg_tv);
		mErrRetryBtn = (Button) findViewById(R.id.err_retry_btn);
		mErrRetryBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	/**
	 * Handle {@link android.content.Intent}.
	 *
	 * @param i
	 * 		Current {@link android.content.Intent} values.
	 */
	private void handleIntent(Intent i) {
		String msg = i.getStringExtra(EXTRAS_ERR_MSG);
		mErrMsgTv.setText(msg);
	}
}
