package com.chopping.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.chopping.R;
import com.chopping.application.ErrorHandler;
import com.chopping.bus.ReloadEvent;
import com.chopping.utils.NetworkUtils;

import de.greenrobot.event.EventBus;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * {@link com.chopping.activities.ErrorHandlerActivity} provides basic logical for an error happens when internet is not
 * available.
 * <p/>
 * <b>Relate class</b>
 * <p/>
 * {@link com.chopping.fragments.ErrorHandlerFragment}
 */
public class ErrorHandlerActivity extends AppCompatActivity {
	private static final int LAYOUT = R.layout.activity_errorhandler;
	/**
	 * Extras. {@link java.lang.String} description of error.
	 * <p/>
	 * Equal to {@link com.chopping.fragments.ErrorHandlerFragment#EXTRAS_ERR_MSG}.
	 */
	public static final String EXTRAS_ERR_MSG = ErrorHandler.EXTRAS_ERR_MSG;
	/**
	 * Extras. A {@link boolean}, {@code true} if shows error because of airplane mode being ON, and a button that opens
	 * setting will be shown as well.
	 * <p/>
	 * Equal to {@link com.chopping.fragments.ErrorHandlerFragment#EXTRAS_AIRPLANE_MODE}.
	 */
	public static final String EXTRAS_AIRPLANE_MODE = ErrorHandler.EXTRAS_AIRPLANE_MODE;
	/**
	 * Message {@link android.widget.TextView}.
	 */
	private TextView mErrMsgTv;

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
		View retryBtn = findViewById(R.id.err_retry_btn);
		retryBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onReload();
			}
		});
		handleIntent(getIntent());
	}

	/**
	 * Call back for clicking a reload button.
	 */
	protected void onReload() {
		EventBus.getDefault().postSticky(new ReloadEvent());
		finish();
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

		View openAirplaneV = findViewById(R.id.open_airplane_setting_btn);
		boolean isAirplaneModeOn = i.getBooleanExtra(EXTRAS_AIRPLANE_MODE, false);
		openAirplaneV.setVisibility(isAirplaneModeOn ? VISIBLE : GONE);
		if(isAirplaneModeOn) {
			openAirplaneV.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					NetworkUtils.openNetworkSetting(v.getContext());
				}
			});
		}
	}
}
