package com.android.internal.telephony;

/**
 * For reflection on the {@link android.telephony.TelephonyManager} to reject incoming calls.
 *
 * @author Xinyue Zhao
 */

public interface ITelephony {
	/**
	 * End call.
	 * @return {@code true} if ok.
	 */
	boolean endCall();

	/**
	 * Answer call.
	 * No used.
	 */
	void answerRingingCall();

	/**
	 * Make ring to silence.
	 */
	void silenceRinger();

}
