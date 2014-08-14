package com.chopping.fragments;

import com.chopping.application.BasicPrefs;
import com.chopping.application.ErrorHandler;
import com.chopping.application.LL;
import com.chopping.bus.BusProvider;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

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
	 * A logical that contains controlling over all network-errors.
	 */
	private final ErrorHandler mErrorHandler = new ErrorHandler();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		/*It'll be nice here to init for a valid activity context.*/
		initErrorHandler();
	}

	@Override
	public void onResume() {
		BusProvider.getBus().register(this);
		BusProvider.getBus().register(mErrorHandler);
		super.onResume();
	}

	@Override
	public void onPause() {
		BusProvider.getBus().unregister(this);
		BusProvider.getBus().unregister(mErrorHandler);
		super.onPause();
	}


	/**
	 * Initialize {@link com.chopping.application.ErrorHandler}.
	 * <p/>
	 * When {@link #EXTRAS_ERR_LAYOUT_CONTAINER} not specified, there's no {@link com.chopping.fragments.ErrorHandlerFragment}
	 * to show and ignored.
	 */
	private void initErrorHandler() {
		Bundle args = getArguments();
		if (args != null) {
			int conId = args.getInt(EXTRAS_ERR_LAYOUT_CONTAINER, -1);
			if (conId == -1) {
				LL.w(String.format(
						"Warning! %s can't show an ErrorHandlerFragment, because 'extras.err.layout.container' hasn't been specified.",
						this.getClass().getSimpleName()));
			} else {
				mErrorHandler.onCreate(this, null, conId);
			}
		}
	}

	/**
	 * App that use this Chopping should know the preference-storage.
	 *
	 * @return An instance of {@link com.chopping.application.BasicPrefs}.
	 */
	protected abstract BasicPrefs getPrefs();
}
