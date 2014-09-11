package com.chopping.application;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.chopping.R;
import com.chopping.activities.ErrorHandlerActivity;
import com.chopping.bus.AirplaneModeOnEvent;
import com.chopping.fragments.ErrorHandlerFragment;
import com.chopping.utils.NetworkUtils;

import org.apache.http.HttpStatus;

import de.greenrobot.event.EventBus;

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
	 * Extras. A {@link boolean}, {@code true} if shows error because of airplane mode being ON, and a button that opens
	 * setting will be shown as well.
	 */
	public static final String EXTRAS_AIRPLANE_MODE = "extras.airplane.mode";
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
	 * A {@link android.support.v4.app.Fragment} maintains an {@link com.chopping.fragments.ErrorHandlerFragment} to
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
	/**
	 * {@code true} if the host class (subclass of {@link com.chopping.activities.BaseActivity},{@link
	 * com.chopping.fragments.BaseFragment}) of {@link com.chopping.application.ErrorHandler} has shown data on UI.
	 */
	private boolean mHasDataOnUI;
	/**
	 * {@code true} if an instance of {@link ErrorHandler} works and shows associated {@link
	 * com.chopping.activities.ErrorHandlerActivity} or an {@link com.chopping.fragments.ErrorHandlerFragment}.
	 */
	private boolean mErrorHandlerAvailable = true;
	/**
	 * {@link android.content.IntentFilter} for airplane mode change.
	 */
	private IntentFilter mAirPlaneFilter = new IntentFilter(
			Intent.ACTION_AIRPLANE_MODE_CHANGED);
	/**
	 * {@link android.content.BroadcastReceiver} for airplane mode change.
	 */
	private BroadcastReceiver mAirPlaneReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, Intent intent) {
			if (NetworkUtils.isAirplaneModeOn(context)) {
				if(mHasDataOnUI) {
					EventBus.getDefault().postSticky(new AirplaneModeOnEvent());
				} else {
					if(mContextWeakRef.get() != null) {
						showFullView(mContextWeakRef.get(), null, true);
					}
				}
			}
		}
	};

	public ErrorHandler() {
	}

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	public void onEvent(VolleyError e) {
		if (mErrorHandlerAvailable) {
			Context context = mContextWeakRef.get();
			if (context != null) {
				boolean isAirplaneModeOn = NetworkUtils.isAirplaneModeOn(context);
				if (isAirplaneModeOn && mHasDataOnUI) {//Show sticky.
					openStickyBanner(context, true);
					setText(e.networkResponse, true);
				} else if (mHasDataOnUI) {//Show sticky.
					if (e.networkResponse == null ||//absolute no network.
							(e.networkResponse != null &&
									e.networkResponse.statusCode != HttpStatus.SC_OK)//online but some problems.
							) {
						openStickyBanner(context, false);
					}
					setText(e.networkResponse, false);
				} else {//Show full view instead of sticky.
					/* Null on networkResponse means no network absolutely.*/
					showFullView(context, e.networkResponse, isAirplaneModeOn);
				}
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
		sticky.findViewById(R.id.open_airplane_setting_btn)
				.setOnClickListener(this);
		mNoNetErrorActivity = errAct == null ? ErrorHandlerActivity.class : errAct;
		mIsErrAct = true;
		Context context = activity.getApplicationContext();
		context.registerReceiver(mAirPlaneReceiver, mAirPlaneFilter);
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
	 * 		A {@link android.support.v4.app.Fragment} maintains an {@link com.chopping.fragments.ErrorHandlerFragment} to
	 * 		handle no internet.
	 * 		<p/>
	 * 		It could be ignored if {@link #mShowErrorFragment} is {@code false}.
	 * @param containerResId
	 * 		{@link IdRes}. Resource id of a layout that can hold {@code errFrg}.
	 */
	public void onCreate(Fragment fragment, Class<? extends ErrorHandlerFragment> errFrg, @IdRes int containerResId) {
		try {
			mContextWeakRef = new WeakReference<Context>(fragment.getActivity());
			View sticky = fragment.getView().findViewById(R.id.err_sticky_container);
			mStickyBannerRef = new WeakReference<View>(sticky);
			sticky.findViewById(R.id.open_airplane_setting_btn)
					.setOnClickListener(this);
			mNoNetErrorFragment = errFrg == null ? ErrorHandlerFragment.class : errFrg;
			mContainerResId = containerResId;
			/*Force to set NULL error's activity.*/
			mNoNetErrorActivity = null;
			mIsErrAct = false;
			Context context = fragment.getActivity().getApplicationContext();
			context.registerReceiver(mAirPlaneReceiver, mAirPlaneFilter);
		} catch (NullPointerException e) {
			throw new NullPointerException(
					"Can't create error-handling for fragment, checkout whether called onCreate at least after/in onViewCreated() of host-fragment.");
		}
	}


	/**
	 * onDestroy Called according to the life-cycle of component({@link android.support.v4.app.Fragment}, {@link
	 * android.app.Activity}, {@link android.app.Service} etc.).
	 * <p/>
	 * For fragment calls it in onDestroyView().
	 */
	public void onDestroy() {
		if (mContextWeakRef != null && //mContextWeakRef could be null when the error-handling module never initialized.
				mContextWeakRef.get() != null) {
			Context context = mContextWeakRef.get().getApplicationContext();
			context.unregisterReceiver(mAirPlaneReceiver);
		}
		mAirPlaneReceiver = null;
		mContextWeakRef = null;
		mAirPlaneFilter = null;
		stopAnim();
		mAnimSet = null;
	}

	@Override
	public void onClick(View v) {
		closeStickyBanner();
		NetworkUtils.openNetworkSetting(v.getContext());
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
	public void openStickyBanner(Context context, boolean isAirplane) {
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
	 * Show wordings for different network errors on sticky.
	 *
	 * @param networkResponse
	 * 		A response from {@link com.android.volley.toolbox.Volley}, it might be NULL if internet has been disconnected.
	 * @param isAirplaneModeOn
	 * 		True if the airplane has been on, and a "setting button" can open system setting to shit-down it.
	 */
	public void setText(NetworkResponse networkResponse, boolean isAirplaneModeOn) {
		View sticky = mStickyBannerRef.get();
		if (sticky != null) {
			TextView errTv = (TextView) sticky.findViewById(R.id.err_tv);
			TextView errMoreTv = (TextView) sticky.findViewById(R.id.err_more_tv);
			if (isAirplaneModeOn) {
				/*Airplane-mode ignores all other network-errors.*/
				errTv.setText(R.string.meta_airplane_mode);
				errMoreTv.setText(R.string.meta_reset_airplane_mode);
			} else {
				/*Some network-errors.*/
				if (networkResponse != null) {
					/*Online errors.*/
					switch (networkResponse.statusCode) {
						case HttpStatus.SC_FORBIDDEN:
						case HttpStatus.SC_MOVED_TEMPORARILY:
						case HttpStatus.SC_SERVICE_UNAVAILABLE:
							errTv.setText(R.string.meta_server_old_black);
							break;
						default:
							errTv.setText(R.string.meta_load_error);
							break;
					}
				} else {
					/*Offline error, no object-ref to NetworkResponse.*/
					errTv.setText(R.string.meta_server_old_black);
				}
			}
		}
	}

	/**
	 * Show {@link com.chopping.activities.ErrorHandlerActivity} or {@link com.chopping.fragments.ErrorHandlerFragment}
	 * when there's no internet.
	 *
	 * @param context
	 * 		{@link android.content.Context}.
	 * @param networkResponse
	 * 		A response from {@link com.android.volley.toolbox.Volley}, it might be NULL if internet has been disconnected.
	 * @param isAirplaneModeOn
	 * 		True if the airplane has been on, and a "setting button" can open system setting to shit-down it.
	 */
	private void showFullView(Context context, NetworkResponse networkResponse, boolean isAirplaneModeOn) {
		String msg;
		if (isAirplaneModeOn) {
			/*Airplane-mode ignores all other network-errors.*/
			msg = context.getString(R.string.meta_airplane_mode);
		} else {
			/*Some network-errors.*/
			if (networkResponse != null) {
				/*Online errors.*/
				switch (networkResponse.statusCode) {
					case HttpStatus.SC_FORBIDDEN:
					case HttpStatus.SC_MOVED_TEMPORARILY:
					case HttpStatus.SC_SERVICE_UNAVAILABLE:
						msg = context.getString(R.string.meta_server_black);
						break;
					default:
						msg = context.getString(R.string.meta_load_error);
						break;
				}
			} else {
				/*Offline error, no object-ref to NetworkResponse.*/
				msg = context.getString(R.string.meta_server_black);
			}
		}
		if (mIsErrAct) {
			Intent i = new Intent(context, mNoNetErrorActivity);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			i.putExtra(EXTRAS_ERR_MSG, msg);
			i.putExtra(EXTRAS_AIRPLANE_MODE, isAirplaneModeOn);
			context.startActivity(i);
		} else {
			Bundle args = new Bundle();
			args.putString(EXTRAS_ERR_MSG, msg);
			args.putBoolean(EXTRAS_AIRPLANE_MODE, isAirplaneModeOn);
			Fragment f = Fragment.instantiate(context, mNoNetErrorFragment.getName(), args);
			if (context instanceof FragmentActivity) {
				final FragmentActivity activity = (FragmentActivity) context;
				activity.getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in,
						android.R.anim.fade_out, android.R.anim.fade_in,
						android.R.anim.fade_out).add(mContainerResId, f).addToBackStack(null).commitAllowingStateLoss();
			}
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

	/**
	 * Set {@code true} if {@link ErrorHandler} works and shows associated {@link com.chopping.activities.ErrorHandlerActivity}
	 * or {@link com.chopping.fragments.ErrorHandlerFragment}.
	 */
	public void setErrorHandlerAvailable(boolean _isErrorHandlerAvailable) {
		mErrorHandlerAvailable = _isErrorHandlerAvailable;
	}

	/**
	 * Set {@code true} if the host class (subclass of {@link com.chopping.activities.BaseActivity},{@link
	 * com.chopping.fragments.BaseFragment}) of {@link com.chopping.application.ErrorHandler} has shown data on UI.
	 */
	public void setHasDataOnUI(boolean _hasDataOnUI) {
		mHasDataOnUI = _hasDataOnUI;
	}
}
