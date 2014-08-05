package com.chopping.application;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.chopping.bus.ApplicationConfigurationDownloadedEvent;
import com.chopping.bus.BusProvider;
import com.chopping.exceptions.CanNotOpenOrFindAppPropertiesException;
import com.chopping.exceptions.InvalidAppPropertiesException;
import com.chopping.net.TaskHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

/**
 * Basic class that provides Preference storage and make it easy to store data globally. We call {@link
 * android.preference.PreferenceManager#getDefaultSharedPreferences(android.content.Context)} so that all
 * preference-components like {@link android.preference.PreferenceActivity}, {@link
 * android.preference.PreferenceFragment} could share a same storage-base.
 * <p/>
 * <p/>
 * Forget <code>edit().xxx.commit().</code>
 *
 * @author Xinyue Zhao
 */
public class BasicPrefs {
	private SharedPreferences mPreferences = null;
	protected Context mContext;
	//----------------------------------------------------------
	// Description: Constants
	//----------------------------------------------------------
	private static final String UNKNOWN = "UNKNOWN";
	private static final String ANDROID = "ANDROID";
	private static final long ONE_HOUR = 3600000;
	private static final long SIX_HOURS = 21600000;
	/**
	 * Standard name of application's properties that contains url to the App's config.
	 * <p/>
	 * <b>The name is forced to be named.</b>
	 */
	private static final String APP_PROPERTIES = "app.properties";
	/**
	 * Url to the application's configuration.
	 * <p/>
	 * <b>Mandatory, name is forced to be named</b> in app.properties.
	 */
	private static final String APP_CONFIG = "app_config";
	/**
	 * Fallback Url to the application's configuration.
	 * <p/>
	 * <b>Mandatory, name is forced to be named</b> in app.properties.
	 */
	private static final String APP_CONFIG_FALLBACK = "app_config_fallback";
	/**
	 * Update time-rate. Default is 6 Hours making new loading for configuration.
	 * <p/>
	 * <b>Optional, name is forced to be named</b> in app.properties.
	 * <p/>
	 * Generally the Application will load its configuration by calling {@link BasicPrefs#downloadApplicationConfiguration()}
	 * which communicates backend with this rate internally. But after the App itself has been updated with new
	 * version(new {@link com.chopping.application.BasicPrefs#APP_CODE}), this rate must be ignored.
	 */
	private static final String UPDATE_RATE = "update_rate";
	/**
	 * Storage for the live status of app, if true, the app can be live, false can not. App can not be live if mExp is
	 * not null.
	 */
	private static final String APP_CAN_LIVE = "app_can_live";
	/**
	 * Storage for last update.
	 */
	private static final String LAST_UPDATE = "last_update";
	/**
	 * Exception will be created if can not find APP_PROPERTIES, APP_CONFIG & APP_CONFIG_FALLBACK can not be found in
	 * file APP_PROPERTIES.
	 */
	private RuntimeException mExp;
	/**
	 * Temp storage for update-rate. It will be written into preference after App's configurations have been loaded.
	 */
	private String mUpdateRate;
	/**
	 * True if App is a new version.
	 */
	private boolean mNewAppVersion;
	/**
	 * Storage for App's VERSION.
	 */
	private final static String APP_VERSION = "DeviceData.app.version";
	/**
	 * Storage for App's VERSION-CODE.
	 */
	private final static String APP_CODE = "DeviceData.app.code";
	/**
	 * Storage for DEVICE_ID.
	 */
	private final static String DEVICE_ID = "DeviceData.deviceid";
	/**
	 * Storage for Device-MODEL.
	 */
	private final static String DEVICE_MODEL = "DeviceData.model";
	/**
	 * Storage for OS type.
	 */
	private final static String OS_NAME = "DeviceData.os";
	/**
	 * Storage for OS Releases' Version.
	 */
	private final static String OS_VERSION = "DeviceData.os.version";
	/**
	 * Storage for API level.
	 */
	private final static String OS_API_LEVEL = "DeviceData.os.api.level";
	/**
	 * Storage for screen resolution(dpi).
	 */
	private final static String SCREEN_DPI = "DeviceData.screen.dpi";


	/**
	 * Constructor of {@link com.chopping.application.BasicPrefs}.
	 *
	 * @param context
	 * 		A context object.
	 */
	protected BasicPrefs(Context context) {
		mContext = context;
		mPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		/* Asking for some other information.*/
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			setString(APP_VERSION, info.versionName);
			int lastAppCode = getInt(APP_CODE, -1);
			if (lastAppCode < 0) {
				/*First installation.*/
				mNewAppVersion = true;
			} else if (info.versionCode > lastAppCode) {
				/*Update installation.*/
				mNewAppVersion = true;
			} else {
				/*Only start without update.*/
				mNewAppVersion = false;
			}
			setInt(APP_CODE, info.versionCode);
			if (TextUtils.isEmpty(android.os.Build.MODEL)) {
				setString(DEVICE_MODEL, UNKNOWN);
			} else {
				setString(DEVICE_MODEL, android.os.Build.MODEL);
			}
			setString(OS_NAME, ANDROID);
			setString(OS_VERSION, android.os.Build.VERSION.RELEASE);
			setInt(OS_API_LEVEL, android.os.Build.VERSION.SDK_INT);
			setString(SCREEN_DPI, getDeviceResolution());
		} catch (PackageManager.NameNotFoundException _e) {
			_e.printStackTrace();
		} catch (Exception _e) {
			_e.printStackTrace();
		}

		/* Read "app.properties" under resources of project.*/
		getAppPropertiesUrl(context);

	}


	/**
	 * Get resolution of screen which kind of dpi will be detected.
	 *
	 * @return DPI type in string.
	 */
	private String getDeviceResolution() {
		int density = mContext.getResources().getDisplayMetrics().densityDpi;
		switch (density) {
			case DisplayMetrics.DENSITY_MEDIUM:
				return "mdpi";
			case DisplayMetrics.DENSITY_HIGH:
				return "hdpi";
			case DisplayMetrics.DENSITY_LOW:
				return "ldpi";
			case DisplayMetrics.DENSITY_XHIGH:
				return "xhdpi";
			case DisplayMetrics.DENSITY_TV:
				return "tv";
			case DisplayMetrics.DENSITY_XXHIGH:
				return "xxhdpi";
			case DisplayMetrics.DENSITY_XXXHIGH:
				return "xxxhdpi";
			default:
				return UNKNOWN;
		}
	}

	/**
	 * Get the url to the application's configuration.
	 *
	 * @param context
	 * 		A context object.
	 *
	 * @return The url to the App's config.
	 */
	private String getAppPropertiesUrl(Context context) {
		Properties prop = new Properties();
		InputStream input = null;
		String value = null;
		try {
			/*From "resources".*/
			input = context.getClassLoader().getResourceAsStream(APP_PROPERTIES);
			if (input != null) {
				// load a properties file
				prop.load(input);
				value = prop.getProperty(APP_CONFIG);
				setString(APP_CONFIG, value);
				value = prop.getProperty(APP_CONFIG_FALLBACK);
				setString(APP_CONFIG_FALLBACK, value);
				if (TextUtils.isEmpty(getAppConfigUrl()) || TextUtils.isEmpty(getAppConfigFallbackUrl())) {
					mExp = new InvalidAppPropertiesException();
				}
				/*Get update-rate. We don't save it first in preference until App's configurations have been loaded.*/
				mUpdateRate = prop.getProperty(UPDATE_RATE);
				if (TextUtils.isEmpty(mUpdateRate) || //Do not provide, prompt to use 6 hours.
						!TextUtils.isDigitsOnly(mUpdateRate)) {//Invalid format of update-rate.
					mUpdateRate = "6";
				}
				LL.i(String.format("Properly loading after %s hours.", mUpdateRate));
			} else {
				mExp = new CanNotOpenOrFindAppPropertiesException();
			}
		} catch (IOException ex) {
			mExp = new CanNotOpenOrFindAppPropertiesException();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return value;
	}


	protected String getString(String key, String defValue) {
		return mPreferences.getString(key, defValue);
	}

	protected boolean setString(String key, String value) {
		SharedPreferences.Editor edit = mPreferences.edit();
		edit.putString(key, value);
		return edit.commit();
	}

	protected boolean getBoolean(String key, boolean defValue) {
		return mPreferences.getBoolean(key, defValue);
	}

	protected boolean setBoolean(String key, boolean value) {
		SharedPreferences.Editor edit = mPreferences.edit();
		edit.putBoolean(key, value);
		return edit.commit();
	}

	protected int getInt(String key, int defValue) {
		return mPreferences.getInt(key, defValue);
	}

	protected boolean setInt(String key, int value) {
		SharedPreferences.Editor edit = mPreferences.edit();
		edit.putInt(key, value);
		return edit.commit();
	}

	protected long getLong(String key, long defValue) {
		return mPreferences.getLong(key, defValue);
	}

	protected boolean setLong(String key, long value) {
		SharedPreferences.Editor edit = mPreferences.edit();
		edit.putLong(key, value);
		return edit.commit();
	}

	protected float getFloat(String key, float defValue) {
		return mPreferences.getFloat(key, defValue);
	}

	protected boolean setFloat(String key, float value) {
		SharedPreferences.Editor edit = mPreferences.edit();
		edit.putFloat(key, value);
		return edit.commit();
	}

	protected boolean contains(String key) {
		return mPreferences.contains(key);
	}

	/**
	 * Get url to application's configuration..
	 *
	 * @return Url in string.
	 */
	private String getAppConfigUrl() {
		return getString(APP_CONFIG, null);
	}

	/**
	 * Get fallback url to application's configuration..
	 *
	 * @return Url in string.
	 */
	private String getAppConfigFallbackUrl() {
		return getString(APP_CONFIG_FALLBACK, null);
	}

	/**
	 * Live-Status of the App. If true, the app can be live, false can not. This method would be called at life-cycle's
	 * onResume in order to prevent user from be in an invalidate App-status.
	 *
	 * @return True, the app can be live.
	 */
	public boolean canAppLive() {
		return getBoolean(APP_CAN_LIVE, false);
	}

	/**
	 * Get App version-name.
	 *
	 * @return App's version-name.
	 */
	public String getAppVersion() {
		return getString(APP_VERSION, null);
	}

	/**
	 * Get App's version-code.
	 *
	 * @return App's version-code.
	 */
	public int getAppCode() {
		return getInt(APP_CODE, -1);
	}


	/**
	 * Get device model.
	 *
	 * @return the device model, it might be BasicPrefs.UNKNOWN.
	 */
	public String getDeviceModel() {
		return getString(DEVICE_MODEL, UNKNOWN);
	}


	/**
	 * Get OS name.
	 *
	 * @return the os name, it must be BasicPrefs.ANDROID.
	 */
	public String getOsName() {
		return getString(OS_NAME, ANDROID);
	}


	/**
	 * Get OS releases' version.
	 *
	 * @return the os version
	 */
	public String getOsVersion() {
		return getString(OS_VERSION, null);
	}

	/**
	 * Get OS API Level.
	 *
	 * @return API Level in int.
	 */
	public int getOsApiLevel() {
		return getInt(OS_API_LEVEL, -1);
	}

	/**
	 * Get screen resolution(DPI).
	 *
	 * @return DPI in string. <p>ldpi, mdpi, hdpi,xhdpi,xxhdpi, tv</p>, otherwise BasicPrefs.UNKNOWN.
	 */
	public String getDeviceDPI() {
		return getString(SCREEN_DPI, null);
	}


	/**
	 * Download application's configuration, internal will use url that has been loaded from app.properties. It could
	 * use fallback if the url is invalid.
	 * <p/>
	 * Call this at {@code onResume} .
	 *
	 * @throws CanNotOpenOrFindAppPropertiesException
	 * 		If can not find  "app.properties" .
	 * @throws InvalidAppPropertiesException
	 * 		If can not find some mandatory properties.
	 */
	public void downloadApplicationConfiguration() throws CanNotOpenOrFindAppPropertiesException,
	                                                      InvalidAppPropertiesException {
		if (mExp != null) {
			setBoolean(APP_CAN_LIVE, false);
			throw mExp;
		}
		long lastUpdate = getLong(LAST_UPDATE, -1);
		boolean loadingConfig =
				lastUpdate < 0 || //Fist install, no last update.
						System.currentTimeMillis() - lastUpdate >= getLong(UPDATE_RATE, SIX_HOURS) ||
						//Long time use and try to load newly.
						mNewAppVersion; //App has been updated.
		if (loadingConfig) {
			LL.i("Loading App's configuration.");
			/*
			 * Request App's configuration.
			 */
			StringRequest request = new StringRequest(Request.Method.GET, getAppConfigUrl(),
					new Response.Listener<String>() {
						@Override
						public void onResponse(String response) {
							LL.i(":) Loaded App's config: " + getAppConfigUrl());
							writePrefsWithStream(new ByteArrayInputStream(response.getBytes()));
							saveUpdateRate();
						}
					},
					new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							LL.w(":( Can't load remote config: " + getAppConfigUrl());
							LL.i(":) We load fallback: " + getAppConfigFallbackUrl());
							writePrefsWithStream(mContext.getClassLoader().getResourceAsStream(
									getAppConfigFallbackUrl()));
							saveUpdateRate();
						}
					});
			request.setRetryPolicy(new DefaultRetryPolicy(
					10 * 1000,
					DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
			TaskHelper.getRequestQueue().add(request);
		} else {
			/* Finish loading configuration directly. */
			BusProvider.getBus().post(new ApplicationConfigurationDownloadedEvent());
		}
	}

	/**
	 * Refresh update-rate for loading App's configurations.
	 */
	private void saveUpdateRate() {
		setLong(UPDATE_RATE, !TextUtils.isEmpty(mUpdateRate) ? Integer.valueOf(mUpdateRate) *
				ONE_HOUR :
				SIX_HOURS);
		LL.i(String.format("Loading after %d hours.", getLong(UPDATE_RATE, -1)));
	}

	/**
	 * Read .properties of App's configuration in stream and write into preference. Finally send event to info front.
	 * Recode saving time into preference.
	 *
	 * @param input
	 * 		An input-stream.
	 */
	private void writePrefsWithStream(InputStream input) {
		Properties prop = new Properties();
		try {
			prop.load(input);
			Set<String> names = prop.stringPropertyNames();
			String valueStr;
			/*
			 * Read all properties and store into Android's preference.
			 */
			for (String name : names) {
				valueStr = prop.getProperty(name);
				LL.d(String.format("%s=%s", name, valueStr));
				if (TextUtils.isDigitsOnly(valueStr)) {
					try {
						int intValue = Integer.valueOf(valueStr);
						setInt(name, intValue);
					} catch (NumberFormatException eL) {
						long longValue = Long.valueOf(valueStr);
						setLong(name, longValue);
					}
				} else {
					try {
						float floatValue = Float.parseFloat(valueStr);
						setFloat(name, floatValue);
					} catch (Exception eF) {
						if (TextUtils.equals(valueStr.toLowerCase(), "true") ||
								TextUtils.equals(valueStr.toLowerCase(), "false")) {
							setBoolean(name, Boolean.parseBoolean(valueStr));
						} else {
							/*Have no choice, then all in to string.*/
							setString(name, valueStr);
						}
					}
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			setBoolean(APP_CAN_LIVE, true);
			setLong(LAST_UPDATE, System.currentTimeMillis());
			mNewAppVersion = false;
			/* Read and info front. */
			BusProvider.getBus().post(new ApplicationConfigurationDownloadedEvent());
		}
	}
}