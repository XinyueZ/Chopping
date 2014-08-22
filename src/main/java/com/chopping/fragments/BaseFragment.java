package com.chopping.fragments;

import com.android.volley.VolleyError;
import com.chopping.R;
import com.chopping.application.BasicPrefs;
import com.chopping.application.ErrorHandler;
import com.chopping.application.LL;
import com.chopping.bus.AirplaneModeOnEvent;
import com.chopping.bus.ApplicationConfigurationDownloadedEvent;
import com.chopping.bus.ReloadEvent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.greenrobot.event.EventBus;

/**
 * Basic {@link android.support.v4.app.Fragment} contains an error-handling.
 *
 * @author Xinyue Zhao
 */
public abstract class BaseFragment extends Fragment {
	/**
	 * Extras. Specify an id of a{@link android.view.ViewGroup} that can show an {@link
	 * com.chopping.fragments.ErrorHandlerFragment}.
	 */
	protected static final String EXTRAS_ERR_LAYOUT_CONTAINER = "extras.err.layout.container";
	/**
	 * EXTRAS. Status of available of error-handling. Default is {@code true}
	 * <p/>
	 * See {@link #mErrorHandlerAvailable}.
	 */
	private static final String EXTRAS_ERR_AVA = "err.ava";
	/**
	 * A logical that contains controlling over all network-errors.
	 */
	private ErrorHandler mErrorHandler = new ErrorHandler();
	/**
	 * {@code true} if {@link #mErrorHandler} works and shows associated {@link com.chopping.fragments.ErrorHandlerFragment}.
	 */
	private boolean mErrorHandlerAvailable = true;

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
			mErrorHandler.openStickyBanner(getActivity(), true);
			mErrorHandler.setText(null, true);
			EventBus.getDefault().removeStickyEvent(AirplaneModeOnEvent.class);
		}
	}

	//------------------------------------------------


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup errorVG = (ViewGroup) container.findViewById(R.id.error_content);
		if (errorVG != null) {
			View stickyV = inflater.inflate(R.layout.inc_err_sticky, errorVG, false);
			errorVG.addView(stickyV);
		}
		return container;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (savedInstanceState != null) {
			mErrorHandlerAvailable = savedInstanceState.getBoolean(EXTRAS_ERR_AVA, true);
		}
		/*It'll be nice here to init for a valid activity context.*/
		initErrorHandler();
	}


	@Override
	public void onResume() {
		if (isStickyAvailable()) {
			EventBus.getDefault().registerSticky(this);
		} else {
			EventBus.getDefault().register(this);
		}
		EventBus.getDefault().register(mErrorHandler);
		super.onResume();
	}

	@Override
	public void onPause() {
		EventBus.getDefault().unregister(this);
		EventBus.getDefault().unregister(mErrorHandler);
		super.onPause();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mErrorHandler.onDestroy();
		mErrorHandler = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(EXTRAS_ERR_AVA, mErrorHandlerAvailable);
	}

	/**
	 * Set {@code true} if {@link #mErrorHandler} works and shows associated {@link
	 * com.chopping.fragments.ErrorHandlerFragment}.
	 * <p/>
	 *
	 * @throws java.lang.NullPointerException
	 * 		must be thrown if it is called at least after {@link android.support.v4.app.Fragment#onViewCreated(android.view.View,
	 *        android.os.Bundle)}.
	 */
	protected void setErrorHandlerAvailable(boolean isErrorHandlerAvailable) {
		if (mErrorHandler == null) {
			throw new NullPointerException(
					"BaseFragment#setErrorHandlerAvailable must be call at least after onViewCreated().");
		}
		mErrorHandlerAvailable = isErrorHandlerAvailable;
		mErrorHandler.setErrorHandlerAvailable(mErrorHandlerAvailable);
	}

	/**
	 * Initialize {@link com.chopping.application.ErrorHandler}.
	 * <p/>
	 * When {@link #EXTRAS_ERR_LAYOUT_CONTAINER} not specified, there's no {@link com.chopping.fragments.ErrorHandlerFragment}
	 * to show and ignored.
	 */
	private void initErrorHandler() {
		if (mErrorHandler == null) {
			mErrorHandler = new ErrorHandler();
		}
		Bundle args = getArguments();
		if (args != null) {
			int conId = args.getInt(EXTRAS_ERR_LAYOUT_CONTAINER, -1);
			if (conId == -1) {
				LL.w(String.format(
						"Warning! %s can't show an ErrorHandlerFragment, because 'extras.err.layout.container' hasn't been specified.",
						this.getClass().getSimpleName()));
			} else {
				mErrorHandler.onCreate(this, null, conId);
				mErrorHandler.setErrorHandlerAvailable(mErrorHandlerAvailable);
			}
		}
	}

	/**
	 * App that use this Chopping should know the preference-storage.
	 *
	 * @return An instance of {@link com.chopping.application.BasicPrefs}.
	 */
	protected abstract BasicPrefs getPrefs();

	/**
	 * Is the {@link android.app.Fragment}({@link android.support.v4.app.Fragment}) ready to subscribe a sticky-event or
	 * not.
	 *
	 * @return {@code true} if the {@link android.app.Fragment}({@link android.support.v4.app.Fragment})  available for
	 * sticky-events inc. normal events.
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
		mErrorHandler.setHasDataOnUI(shownDataOnUI);
	}
}
