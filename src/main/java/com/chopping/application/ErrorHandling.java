package com.chopping.application;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.chopping.R;
import com.chopping.net.NetworkUtils;
import com.squareup.otto.Subscribe;

import org.apache.http.HttpStatus;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
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
public final class ErrorHandling implements Animation.AnimationListener, View.OnClickListener {
	/**
	 * View ref to the sticky.
	 */
	private WeakReference<View> mStickyBannerRef;
	/**
	 * Context that holding {@link ErrorHandling}.
	 */
	private WeakReference<Context> mContextWeakReference;
	/**
	 * Animation for sticky.
	 */
	private AnimationSet mAnim;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	@Subscribe
	public void onVolleyError(VolleyError e) {
		Context context = mContextWeakReference.get();
		if (context != null) {
			boolean isAirplaneModeOn = NetworkUtils.isAirplaneModeOn(context);
			if (e.networkResponse != null) {
				int statusCode = e.networkResponse.statusCode;
				LL.d(String.format("Error-Handling find abnormal status: %d", statusCode));
				if (statusCode != HttpStatus.SC_OK) {
					openStickyBanner(context, isAirplaneModeOn);
				}
			} else {
				openStickyBanner(context, isAirplaneModeOn);
			}
			setText(e.networkResponse, isAirplaneModeOn);
		}
	}

	//------------------------------------------------

	/**
	 * onCreate Called according to the life-cycle of component({@link android.support.v4.app.Fragment}, {@link
	 * android.app.Activity}, {@link android.app.Service} etc.).
	 *
	 * @param android.support.v4.app.Fragment
	 * 		A fragment if error is handled for fragment.
	 */
	public void onCreate(Fragment fragment) {
		onCreate(fragment.getActivity());
	}

	/**
	 * onCreate Called according to the life-cycle of component(Fragment, Activity, Service etc.).
	 *
	 * @param Activity
	 * 		An activity if error is handled for activity.
	 */
	public void onCreate(Activity activity) {
		mContextWeakReference = new WeakReference<Context>(activity);
		View sticky = activity.findViewById(R.id.err_sticky_container);
		mStickyBannerRef = new WeakReference<View>(sticky);
		sticky.findViewById(R.id.open_setting_btn).setOnClickListener(this);
	}


	/**
	 * onDestroy Called according to the life-cycle of component({@link android.support.v4.app.Fragment}, {@link
	 * android.app.Activity}, {@link android.app.Service} etc.).
	 * <p/>
	 * For fragment calls it in onDestroyView().
	 */
	public void onDestroy() {
//		_context.unregisterReceiver(mConnectivityReceiver);
		mContextWeakReference = null;
//		mIntentFilterConnectivityReceiver = null;
//		mConnectivityReceiver = null;
		stopAnim();
		mAnim = null;
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
		if (mAnim != null) {
			mAnim.cancel();
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
		int duration1 = 700;
		int duration2 = 500;
		if (isAirplane) {
			duration1 *= 10;
			duration2 *= 10;
		}
		try {
			stopAnim();
			mAnim = new AnimationSet(true);
			TranslateAnimation a = new TranslateAnimation(0, 0, -10, 0);
			a.setStartOffset(200);
			a.setDuration(duration1);
			mAnim.addAnimation(a);
			a = new TranslateAnimation(0, 0, 5, 0);
			a.setStartOffset(3500);
			a.setDuration(duration2);
			mAnim.addAnimation(a);
			mAnim.setAnimationListener(this);
			showStickyBanner(isAirplane);
			View sticky = mStickyBannerRef.get();
			if (sticky != null) {
				sticky.startAnimation(mAnim);
			}
		} catch (RuntimeException _ex) {
			// Thrown if the App was closed during animation
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
	 * 		A response from volley, it might be NULL if internet has been disconnected.
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


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.open_setting_btn) {
			closeStickyBanner();
			NetworkUtils.openNetworkSetting(v.getContext());
		}
	}
}
