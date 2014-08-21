package com.chopping.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import com.chopping.R;
import com.chopping.activities.ErrorHandlerActivity;
import com.chopping.application.ErrorHandler;
import com.chopping.bus.ReloadEvent;
import com.chopping.utils.NetworkUtils;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.greenrobot.event.EventBus;

/**
 * {@link com.chopping.fragments.ErrorHandlerFragment} provides basic logical for an error happens when internet is not
 * available.
 * <p/>
 * <b>Relate class</b>
 * <p/>
 * {@link com.chopping.activities.ErrorHandlerActivity}
 */
public class ErrorHandlerFragment extends Fragment {
	private static int LAYOUT = R.layout.fragment_errorhandler;

	/**
	 * Extras. {@link java.lang.String} description of error.
	 * <p/>
	 * Equal to {@link com.chopping.activities.ErrorHandlerActivity#EXTRAS_ERR_MSG}.
	 */
	public static final String EXTRAS_ERR_MSG = ErrorHandlerActivity.EXTRAS_ERR_MSG;
	/**
	 * Extras. A {@link boolean}, {@code true} if shows error because of airplane mode being ON, and a button that opens
	 * setting will be shown as well.
	 * <p/>
	 * Equal to {@link com.chopping.activities.ErrorHandlerActivity#EXTRAS_AIRPLANE_MODE}.
	 */
	public static final String EXTRAS_AIRPLANE_MODE = ErrorHandler.EXTRAS_AIRPLANE_MODE;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return inflater.inflate(LAYOUT, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		View retryBtn = view.findViewById(R.id.err_retry_btn);
		retryBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onReload();
			}
		});

		Bundle args = getArguments();
		if(args != null) {
			TextView errMsgTv = (TextView) view.findViewById(R.id.err_msg_tv);
			String msg = args.getString(EXTRAS_ERR_MSG);
			errMsgTv.setText(msg);

			View openAirplaneV = view.findViewById(R.id.open_airplane_setting_btn);
			boolean isAirplaneModeOn = args.getBoolean(EXTRAS_AIRPLANE_MODE, false);
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

	/**
	 * Call back for clicking a reload button.
	 */
	protected void onReload() {
		EventBus.getDefault().postSticky(new ReloadEvent());
		getFragmentManager().popBackStack(null,
				FragmentManager.POP_BACK_STACK_INCLUSIVE);
		FragmentTransaction trans = getFragmentManager().beginTransaction();
		// trans.remove(_f);
		trans.commit();
	}

	@Override
	public void onResume() {
		EventBus.getDefault().register(this);
		super.onResume();
	}

	@Override
	public void onPause() {
		EventBus.getDefault().unregister(this);
		super.onPause();
	}

}
