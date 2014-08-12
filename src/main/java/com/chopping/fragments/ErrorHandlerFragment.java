package com.chopping.fragments;

import com.chopping.activities.ErrorHandlerActivity;

/**
 * {@link com.chopping.fragments.ErrorHandlerFragment} provides basic logical for an error happens when internet is not
 * available.
 * <p/>
 * <b>Relate class</b>
 * <p/>
 * {@link com.chopping.activities.ErrorHandlerActivity}
 */
public class ErrorHandlerFragment extends BaseFragment {
	/**
	 * Extras. {@link java.lang.String} description of error.
	 * <p/>
	 * Equal to {@link com.chopping.activities.ErrorHandlerActivity#EXTRAS_ERR_MSG}.
	 */
	public static final String EXTRAS_ERR_MSG = ErrorHandlerActivity.EXTRAS_ERR_MSG;
}
