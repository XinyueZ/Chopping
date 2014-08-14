package com.chopping.application;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.chopping.R;
import com.chopping.activities.ErrorHandlerActivity;
import com.chopping.fragments.ErrorHandlerFragment;
import com.chopping.utils.NetworkUtils;
import com.squareup.otto.Subscribe;

import org.apache.http.HttpStatus;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

/**
 * Error handling for components like {@link android.support.v4.app.Fragment}, {@link android.app.Activity}, {@link
 * android.app.Service} etc.
 * <p/>
 * This class supports only {@link android.support.v4.app.Fragment}, also {@link android.app.Activity} or {@link
 * android.support.v7.app.ActionBarActivity}.
 *
 * @author Xinyue Zhao
 */
public final class ErrorHandler implements Animation.AnimationListener, View.OnClickListener {
	/**
	 * Extras. {@link java.lang.String} description of error.
	 */
	public static final String EXTRAS_ERR_MSG = "extras.err.msg";
	/**
	 * {@link android.view.View} ref to the sticky.
	 */
	private WeakReference<View> mStickyBannerRef;
	/**
	 * {@link android.content.Context} that holding {@link ErrorHandler}.
	 */
	private WeakReference<Context> mContextWeakRef;
	/**
	 * {@link android.view.animation.Animation} for sticky.
	 */
	private AnimationSet mAnimSet;
	/**
	 * An {@link com.chopping.activities.ErrorHandlerActivity} when there's no internet connection anymore.
	 * <p/>
	 * An {@link android.app.Activity} maintains an {@link com.chopping.activities.ErrorHandlerActivity} to handle no
	 * internet.
	 */
	private Class<? extends ErrorHandlerActivity> mNoNetErrorActivity;
	/**
	 * A {@link com.chopping.fragments.ErrorHandlerFragment} when there's no internet connection anymore.
	 * <p/>
	 * A {@link android.support.v4.app.Fragment} maintains a {@link com.chopping.fragments.ErrorHandlerFragment} to
	 * handle no internet.
	 * <p/>
	 * It could be ignored if {@link #mShowErrorFragment} is {@code false}.
	 */
	private Class<? extends ErrorHandlerFragment> mNoNetErrorFragment;
	/**
	 * Resource id of a layout that can hold {@link #mNoNetErrorFragment}.
	 */
	private int mContainerResId;
	/**
	 * {@code true} if the {@link android.support.v4.app.Fragment} that has initialized an {@link
	 * com.chopping.application.ErrorHandler} maintains an error-page({@link #mNoNetErrorFragment}) by itself.
	 */
	private boolean mShowErrorFragment;
	/**
	 * {@code true} if error-page is an {@link android.app.Activity}, otherwise a {@link
	 * android.support.v4.app.Fragment}.
	 */
	private boolean mIsErrAct;

	public ErrorHandler() {
	}

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	@Subscribe
	public void onVolleyError(VolleyError e) {
		Context context = mContextWeakRef.get();
		if (context != null) {
			boolean isAirplaneModeOn = NetworkUtils.isAirplaneModeOn(context);
			if (e.networkResponse != null) {
				int statusCode = e.networkResponse.statusCode;
				LL.d(String.format("Error-Handling find abnormal status: %d", statusCode));
				if (statusCode != HttpStatus.SC_OK) {
					openStickyBanner(context, isAirplaneModeOn);
				}
				setText(e.networkResponse, isAirplaneModeOn);
			} else {
				/* Null on networkResponse means no network absolutely.*/
				showNoNetView(context, isAirplaneModeOn);
			}
		}
	}

	//------------------------------------------------

	/**
	 * onCreate Called according to the life-cycle of component(Fragment, Activity, Service etc.).
	 *
	 * @param activity
	 * 		An {@link android.app.Activity} if error is handled for activity.
	 * @param errAct
	 * 		A {@link com.chopping.activities.ErrorHandlerActivity} when there's no internet connection anymore.
	 * 		<p/>
	 * 		An {@link android.app.Activity} maintains an {@link com.chopping.activities.ErrorHandlerActivity} to handle no
	 * 		internet.
	 */
	public void onCreate(Activity activity, Class<? extends ErrorHandlerActivity> errAct) {
		mContextWeakRef = new WeakReference<Context>(activity);
		View sticky = activity.findViewById(R.id.err_sticky_container);
		mStickyBannerRef = new WeakReference<View>(sticky);
		sticky.findViewById(R.id.open_setting_btn).setOnClickListener(this);
		mNoNetErrorActivity = errAct == null ? ErrorHandlerActivity.class : errAct;
		mIsErrAct = true;
	}

	/**
	 * onCreate Called according to the life-cycle of component({@link android.support.v4.app.Fragment}, {@link
	 * android.app.Activity}, {@link android.app.Service} etc.).
	 *
	 * @param fragment
	 * 		A {@link android.support.v4.app.Fragment} if error is handled for fragment.
	 * @param errFrg
	 * 		A {@link com.chopping.fragments.ErrorHandlerFragment} when there's no internet connection anymore.
	 * 		<p/>
	 * 		A {@link android.support.v4.app.Fragment} maintains a {@link com.chopping.fragments.ErrorHandlerFragment} to
	 * 		handle no internet.
	 * 		<p/>
	 * 		It could be ignored if {@link #mShowErrorFragment} is {@code false}.
	 * @param containerResId
	 * 		Resource id of a layout that can hold {@code errFrg}.
	 */
	public void onCreate(Fragment fragment, Class<? extends ErrorHandlerFragment> errFrg,   int containerResId) {
		onCreate(fragment.getActivity(), null);
		mNoNetErrorFragment = errFrg == null ? ErrorHandlerFragment.class : errFrg;
		mContainerResId = containerResId;
		/*Force to set NULL error's activity.*/
		mNoNetErrorActivity = null;
		mIsErrAct = false;
	}


	/**
	 * onDestroy Called according to the life-cycle of component({@link android.support.v4.app.Fragment}, {@link
	 * android.app.Activity}, {@link android.app.Service} etc.).
	 * <p/>
	 * For fragment calls it in onDestroyView().
	 */
	public void onDestroy() {
//		_context.unregisterReceiver(mConnectivityReceiver);
		mContextWeakRef = null;
//		mIntentFilterConnectivityReceiver = null;
//		mConnectivityReceiver = null;
		stopAnim();
		mAnimSet = null;
	}


	/**
	 * Show the sticky banner when network breaks down. Call openStickyBanner directly instead of calling this
	 * function.
	 *
	 * @param isAirplaneMode
	 * 		True if airplane-mode is on, false if off.
	 */
	private void showStickyBanner(boolean isAirplaneMode) {
		View sticky = mStickyBannerRef.get();
		if (sticky != null) {
			sticky.setVisibility(View.VISIBLE);
			sticky.findViewById(R.id.airplane_mode_ll).setVisibility(
					isAirplaneMode ? View.VISIBLE : View.GONE);
		}
	}

	/**
	 * Force to stop animation of sticky.
	 */
	private void stopAnim() {
		if (mAnimSet != null) {
			mAnimSet.cancel();
		}
	}


	/**
	 * Close sticky, stop animation and set invisible on sticky.
	 */
	private void closeStickyBanner() {
		stopAnim();
		dismissStickyBanner();
	}

	/**
	 * Dismiss the sticky banner when network breaks down. Call closeStickyBanner directly instead of calling this
	 * function.
	 */
	private void dismissStickyBanner() {
		View sticky = mStickyBannerRef.get();
		if (sticky != null) {
			sticky.setVisibility(View.GONE);
		}
	}


	/**
	 * Open the sticky with some animations.
	 */
	private void openStickyBanner(Context context, boolean isAirplane) {
		mAnimSet = (AnimationSet) AnimationUtils.loadAnimation(context, R.anim.slide_in_and_out);
		mAnimSet.setAnimationListener(this);
		showStickyBanner(isAirplane);
		View sticky = mStickyBannerRef.get();
		if (sticky != null) {
			sticky.startAnimation(mAnimSet);
		}
	}


	@Override
	public void onAnimationEnd(Animation animation) {
		dismissStickyBanner();
	}

	@Override
	public void onAnimationStart(Animation animation) {
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	/**
	 * Show wordings for different network errors.
	 *
	 * @param _networkResponse
	 * 		A response from {@link com.android.volley.toolbox.Volley}, it might be NULL if internet has been disconnected.
	 * @param _isAirplaneModeOn
	 * 		True if the airplane has been on, and a "setting button" can open system setting to shit-down it.
	 */
	private void setText(NetworkResponse _networkResponse, boolean _isAirplaneModeOn) {
		View sticky = mStickyBannerRef.get();
		if (sticky != null) {
			TextView errTv = (TextView) sticky.findViewById(R.id.err_tv);
			TextView errMoreTv = (TextView) sticky.findViewById(R.id.err_more_tv);
			if (_isAirplaneModeOn) {
				/*Airplane-mode ignores all other network-errors.*/
				errTv.setText(R.string.meta_airplane_mode);
				errMoreTv.setText(R.string.meta_reset_airplane_mode);
			} else {
				/*Some network-errors.*/
				if (_networkResponse != null) {
					/*Online errors.*/
				} else {
					/*Offline error, no object-ref to NetworkResponse.*/
					errTv.setText(R.string.meta_data_old_offline);
				}
			}
		}
	}

	private void showNoNetView(Context context, boolean isAirplaneModeOn) {
		String msg = context.getString(R.string.meta_data_old_offline);
		if (mIsErrAct) {
			Intent i = new Intent(context, mNoNetErrorActivity);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			i.putExtra(EXTRAS_ERR_MSG, msg);
			context.startActivity(i);
		} else {
			Bundle args = new Bundle();
			args.putString(EXTRAS_ERR_MSG, msg);
			Fragment f = Fragment.instantiate(context, mNoNetErrorFragment.getName(), args);
			if (context instanceof FragmentActivity) {
				final FragmentActivity activity = (FragmentActivity) context;
				activity.getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in,
						android.R.anim.fade_out, android.R.anim.fade_in,
						android.R.anim.fade_out).add(mContainerResId, f).addToBackStack(null).commitAllowingStateLoss();
			}
		}
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.open_setting_btn) {
			closeStickyBanner();
			NetworkUtils.openNetworkSetting(v.getContext());
		}
	}

	/**
	 * Set {@code true} if the {@link android.support.v4.app.Fragment} that has initialized an {@link
	 * com.chopping.application.ErrorHandler} maintains an error-page({@link #mNoNetErrorFragment}) by itself.
	 * <p/>
	 * If the error-page is an {@link android.app.Activity} effect of this method is ignored.
	 */
	public void setShowErrorFragment(boolean _showErrorFragment) {
		mShowErrorFragment = _showErrorFragment;
	}
}
