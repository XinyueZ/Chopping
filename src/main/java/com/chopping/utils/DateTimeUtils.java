package com.chopping.utils;

import java.util.Calendar;
import java.util.TimeZone;

import android.content.Context;

import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_TIME;
import static android.text.format.DateUtils.FORMAT_SHOW_YEAR;
import static android.text.format.DateUtils.formatDateTime;

/**
 * Utils for some methods that operate on time, date, calendar etc.
 *
 * @author Xinyue Zhao
 */
public final class DateTimeUtils {
	/**
	 * Convert the <code>date</code> with <code>oldZone</code> to <cod>newZone</cod>.
	 * <p/>
	 * Here is an example: We get some datetime from server (in BeiJing GMT+08) and compare the local time on device to get elapsed seconds
	 * between them like "10 minutes ago" or "1 hour ago" etc.
	 * <p/>
	 * <pre>
	 * <code>
	 *
	 * Calendar editTime = Calendar.getInstance();
	 * try {
	 * 	editTime.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(item.getPubDate()));
	 * 	editTime = Utils.transformTime(editTime, TimeZone.getTimeZone("GMT+08"), TimeZone.getDefault());
	 * 	CharSequence elapsedSeconds = DateUtils.getRelativeTimeSpanString(editTime.getTimeInMillis(),
	 * 						System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
	 * 	//... show time
	 * } catch (ParseException e) {
	 * 	//....
	 * }
	 * </code>
	 * </pre>
	 *
	 * @param date
	 * 		A data which needs new time-zone.
	 * @param oldZone
	 * 		Old time-zone that will be compared with new one.
	 * @param newZone
	 * 		New time-zone.
	 *
	 * @return A new data with <code>newZone</code>.
	 */
	public static Calendar transformTime(Calendar date, TimeZone oldZone, TimeZone newZone) {
		Calendar finalDate = Calendar.getInstance();
		if (date != null) {
			long timeOffset = oldZone.getOffset(date.getTimeInMillis()) - newZone.getOffset(date.getTimeInMillis());
			finalDate.setTimeInMillis(date.getTimeInMillis() - timeOffset);
		}
		return finalDate;

	}

	/**
	 * Convert a timestamps to a readable date in string.
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 * @param timestamps
	 * 		A long value for a timestamps.
	 *
	 * @return A date string format.
	 */
	public static String convertTimestamps2DateString(Context cxt, long timestamps) {
		return formatDateTime(cxt, timestamps, FORMAT_SHOW_YEAR | FORMAT_SHOW_DATE |
				FORMAT_SHOW_TIME | FORMAT_ABBREV_MONTH);
	}
}
