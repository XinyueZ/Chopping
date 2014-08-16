package com.chopping.fragments;

import com.chopping.R;
import com.chopping.application.BasicPrefs;
import com.chopping.application.ErrorHandler;
import com.chopping.application.LL;

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
	 * Basic layout that contains an error-handling(a sticky).
	 */
	private static final int LAYOUT_BASE = R.layout.activity_base;
	/**
	 * Extras. Specify an id of a{@link android.view.ViewGroup} that can show an {@link
	 * com.chopping.fragments.ErrorHandlerFragment}.
	 */
	protected static final String EXTRAS_ERR_LAYOUT_CONTAINER = "extras.err.layout.container";
	/**
	 * EXTRAS. Status of available of error-handling. Default is {@code true}
	 * <p/>
	 * See {@link #mIsErrorHandlerAvailable}.
	 */
	private static final String EXTRAS_ERR_AVA = "err.ava";
	/**
	 * A logical that contains controlling over all network-errors.
	 */
	private ErrorHandler mErrorHandler = new ErrorHandler();
	/**
	 * {@code true} if {@link #mErrorHandler} works and shows associated {@link com.chopping.fragments.ErrorHandlerFragment}.
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup parent = (ViewGroup) inflater.inflate(LAYOUT_BASE, null);
		parent.addView(container);
		return parent;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (savedInstanceState != null) {
			mIsErrorHandlerAvailable = savedInstanceState.getBoolean(EXTRAS_ERR_AVA, true);
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
		outState.putBoolean(EXTRAS_ERR_AVA, mIsErrorHandlerAvailable);
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
	protected void setErrorHandlerAvailable(boolean _isErrorHandlerAvailable) {
		if (mErrorHandler == null) {
			throw new NullPointerException(
					"BaseFragment#setErrorHandlerAvailable must be call at least after onViewCreated().");
		}
		mIsErrorHandlerAvailable = _isErrorHandlerAvailable;
		mErrorHandler.setErrorHandlerAvailable(mIsErrorHandlerAvailable);
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
				mErrorHandler.setErrorHandlerAvailable(mIsErrorHandlerAvailable);
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
	 * <b>Default is {@code false}</b>.
	 */
	protected boolean isStickyAvailable() {
		return false;
	}
}