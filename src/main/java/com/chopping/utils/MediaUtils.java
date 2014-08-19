package com.chopping.utils;

import com.chopping.application.LL;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

/**
 * Utils for media usages.
 *
 * @author Xinyue Zhao
 */
public final class MediaUtils {
	/**
	 * Asyn-post to get duration of video.
	 *
	 * @param _context
	 * 		{@link android.content.Context}.
	 * @param _urlToVideo
	 * 		The url to video in {@link java.lang.String}.
	 *
	 * @return {@link java.lang.String}. The Duration of video in long format  like {@code 03:45}.
	 * <p/>
	 * Null might be returned if unsuccessful since some reasons like with offline status try to get a remote video.
	 */
	public static String getVideoDuration(Context _context, String _urlToVideo) {
		String dur = null;
		try {
			MediaPlayer mp = MediaPlayer.create(_context, Uri.parse(_urlToVideo));
			int duration = mp.getDuration();
			mp.release();
			/*convert millis to appropriate time*/
			NumberFormat f = new DecimalFormat("##00");
			dur = String.format("%s:%s",
					f.format(TimeUnit.MILLISECONDS.toMinutes(duration)),
					f.format(TimeUnit.MILLISECONDS.toSeconds(duration) -
							TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))));
		} catch (Exception _e) {
			LL.e("Can't get duration of video, checkout internet connection.");
		}
		return dur;
	}
}
