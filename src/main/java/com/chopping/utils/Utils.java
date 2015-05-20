package com.chopping.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.chopping.application.IApp;
import com.chopping.application.LL;

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


	/**
	 * Link to an external app that has _packageName. If the App has not been installed, then links to store.
	 * <p/>
	 * It will be tracked by Tracker.
	 *
	 * @param context
	 * 		A context object
	 * @param app
	 * 		The app to open or direct to store if not be installed before.
	 */
	public static void linkToExternalApp(Context context, IApp app) {
		/* Try to find the app with _packageName. */
		boolean found;
		PackageManager pm = context.getPackageManager();
		found = isAppInstalled(app.getPackageName(), pm);
		/* Launch the App or go to store. */
		if (found) {
			/* Found. Start app. */
			Intent LaunchIntent = pm.getLaunchIntentForPackage(app.getPackageName());
			context.startActivity(LaunchIntent);
		} else {
			/*To Store.*/
			openUrl(context, app.getStoreUrl());
		}
	}

	/**
	 * Check whether the App with _packageName has been installed or not.
	 *
	 * @param packageName
	 * @param pm
	 *
	 * @return
	 */
	public static boolean isAppInstalled(String packageName, PackageManager pm) {
		boolean found;
		try {
			pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			found = true;
		} catch (PackageManager.NameNotFoundException e) {
			found = false;
		}
		return found;
	}

	/**
	 * Link to an external view.
	 *
	 * @param context
	 * @param to
	 */
	public static void openUrl(Context context, String to) {
		if (context != null) {
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(to));
			context.startActivity(i);
		}
	}


	/**
	 * Helper method to dump all data from DB from private location to public readable one.
	 * <p/>
	 * It is used for debug.
	 * @param cxt {@link android.content.Context}.
	 * @param dbName Database name.
	 */
	public static void dumpSqlite(Context cxt, String dbName) {
		String path = "/data/data/"+cxt.getPackageName()+"/databases";
		LL.d("Under Path: " + path);
		File f = new File(path);
		File files[] = f.listFiles();
		LL.d("Under Path find files: " + files.length);
		for (int i=0; i < files.length; i++) 	{
			LL.d("File:" + files[i].getName());
		}
		String sourceLocation = path +"/" +dbName;// Your database path
		String destLocation = dbName + ".db";
		try {
			File sd = Environment.getExternalStorageDirectory();
			if(sd.canWrite()){
				File source=new File(sourceLocation);
				File dest=new File(sd+"/"+destLocation);
				if(!dest.exists()){
					dest.createNewFile();
				}
				LL.d("dumpSqlite from: " + source.getAbsolutePath());
				if(source.exists()){
					LL.d("dumpSqlite to: " + dest.getAbsolutePath());
					InputStream src=new FileInputStream(source);
					OutputStream dst=new FileOutputStream(dest);
					// Copy the bits from instream to outstream
					byte[] buf = new byte[1024];
					int len;
					while ((len = src.read(buf)) > 0) {
						dst.write(buf, 0, len);
					}
					src.close();
					dst.close();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Convert uri-str to {@link URI}.
	 * @param uriStr The original uri-str.
	 * @return {@link URI}.
	 */
	public static URI uriStr2URI(String uriStr) {
		Uri uri = Uri.parse(uriStr);
		String host = uri.getHost();
		String body = uri.getEncodedPath();
		URI ui = null;
		try {
			  ui = new URI(
					"http",
					host,
					body,
					null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return ui;
	}
}
