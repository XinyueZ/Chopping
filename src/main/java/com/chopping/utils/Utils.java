package com.chopping.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

/**
 * Utils for tools used in common Apps.
 *
 * @author Xinyue Zhao
 */
public final class Utils {
	/**
	 * Show long time toast.
	 *
	 * @param context
	 * 		{@link android.content.Context}.
	 * @param messageId
	 * 		{@link android.support.annotation.StringRes}. Message to show.
	 */
	public static void showLongToast(Context context, @StringRes int messageId) {
		Toast.makeText(context, context.getString(messageId), Toast.LENGTH_LONG).show();
	}

	/**
	 * Show short time toast.
	 *
	 * @param context
	 * 		{@link android.content.Context}.
	 * @param messageId
	 * 		{@link android.support.annotation.StringRes}. Message to show.
	 */
	public static void showShortToast(Context context, @StringRes int messageId) {
		Toast.makeText(context, context.getString(messageId), Toast.LENGTH_SHORT).show();
	}

	/**
	 * Show short time toast.
	 *
	 * @param context
	 * 		{@link android.content.Context}.
	 * @param message
	 * 		{@link java.lang.String}. Message to show.
	 */
	public static void showLongToast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}

	/**
	 * Show short time toast.
	 *
	 * @param context
	 * 		{@link android.content.Context}.
	 * @param message
	 * 		{@link java.lang.String}. Message to show.
	 */
	public static void showShortToast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Encode string for http query with UTF-8.
	 * @param keywords
	 * @return The encoded string.
	 */
	public static String encode(String keywords) {
		try {
			return URLEncoder.encode(keywords, "UTF-8");
		} catch (UnsupportedEncodingException _e1) {
			return new String(keywords.trim().replace(" ", "%20").replace("&", "%26").replace(",", "%2c")
					.replace("(", "%28").replace(")", "%29").replace("!", "%21").replace("=", "%3D")
					.replace("<", "%3C").replace(">", "%3E").replace("#", "%23").replace("$", "%24")
					.replace("'", "%27").replace("*", "%2A").replace("-", "%2D").replace(".", "%2E")
					.replace("/", "%2F").replace(":", "%3A").replace(";", "%3B").replace("?", "%3F")
					.replace("@", "%40").replace("[", "%5B").replace("\\", "%5C").replace("]", "%5D")
					.replace("_", "%5F").replace("`", "%60").replace("{", "%7B").replace("|", "%7C")
					.replace("}", "%7D"));
		}
	}

	/**
	 * Encode string for http query with simple replacement.
	 * @param keywords
	 * @return The encoded string.
	 */
	public static String replaceToEncode(String keywords) {
		return new String(keywords.trim().replace(" ", "%20").replace("&", "%26").replace(",", "%2c")
				.replace("(", "%28").replace(")", "%29").replace("!", "%21").replace("=", "%3D").replace("<", "%3C")
				.replace(">", "%3E").replace("#", "%23").replace("$", "%24").replace("'", "%27").replace("*", "%2A")
				.replace("-", "%2D").replace(".", "%2E").replace("/", "%2F").replace(":", "%3A").replace(";", "%3B")
				.replace("?", "%3F").replace("@", "%40").replace("[", "%5B").replace("\\", "%5C").replace("]", "%5D")
				.replace("_", "%5F").replace("`", "%60").replace("{", "%7B").replace("|", "%7C").replace("}", "%7D"));
	}

}
