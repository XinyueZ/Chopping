package com.chopping;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

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

/**
 * Basic class that provides Preference storage and make it easy to store data.
 * 
 * Forget now edit().xxx.commit().
 * 
 * @author Xinyue Zhao
 * 
 */
public class BasicPrefs {
	/**
	 * Standard name of application's properties that contains url to the app's
	 * config.
	 */
	private static final String APP_PROPERTIES = "app.properties";
	/** Url to the application's configuration. */
	private static final String APP_CONFIG = "app_config";
	/** Fallback Url to the application's configuration. */
	private static final String APP_CONFIG_FALLBACK = "app_config_fallback";
	/**
	 * Storage for the live status of app, if true, the app can be live, false
	 * can not. App can not be live if mExp is not null.
	 */
	private static final String APP_CAN_LIVE = "app_can_live";
	protected SharedPreferences preference = null;
	protected Context mContext;
	/**
	 * Exception will be created if can not find APP_PROPERTIES or APP_CONFIG &
	 * APP_CONFIG_FALLBACK can not be found in file APP_PROPERTIES.
	 */
	private RuntimeException mExp;


	/** The Constant VERSION. */
	private final static String VERSION = "DeviceData.version";
	/** The Constant DEVICE_ID. */
	private final static String DEVICE_ID = "DeviceData.deviceid";
	/** The Constant MODEL. */
	private final static String MODEL = "DeviceData.model";
	/** The Constant OS. */
	private final static String OS = "DeviceData.os";
	/** The Constant OS_VERSION. */
	private final static String OS_VERSION = "DeviceData.osversion";

	/**
	 * Get the url to the application's configuration.
	 * 
	 * @param context
	 *            A context object.
	 * @return The url to the app's config.
	 */
	private String getAppPropertiesUrl(Context context) {
		Properties prop = new Properties();
		InputStream input = null;
		String url = null;
		try {
			input = context.getClassLoader().getResourceAsStream(APP_PROPERTIES);
			if (input != null) {
				// load a properties file
				prop.load(input);
				url = prop.getProperty(APP_CONFIG);
				setString(APP_CONFIG, url);
				url = prop.getProperty(APP_CONFIG_FALLBACK);
				setString(APP_CONFIG_FALLBACK, url);
				if (TextUtils.isEmpty(getAppConfigUrl()) || TextUtils.isEmpty(getAppConfigFallbackUrl())) {
					mExp = new InvalidAppPropertiesException();
				}
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
		return url;
	}

	/**
	 * Constructor of BasicPrefs
	 * @param context A context object.
	 */
	protected BasicPrefs(Context context) {
		mContext = context;
		preference = context.getSharedPreferences(getClass().getPackage().toString(), Context.MODE_PRIVATE);

		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			setAppVersion(info.versionName);
			if (TextUtils.isEmpty(android.os.Build.MODEL)) {
				setDeviceModel("UNKNOWN");
			} else {
				setDeviceModel(android.os.Build.MODEL);
			}
			setOs("ANDROID");
			setOsVersion(android.os.Build.VERSION.RELEASE);
		} catch (PackageManager.NameNotFoundException _e) {
			_e.printStackTrace();
		} catch (Exception _e) {
			_e.printStackTrace();
		}

		/* Read "app.properties" under resources of project.*/
		getAppPropertiesUrl(context);

	}



	protected String getString(String key, String defValue) {
		return preference.getString(key, defValue);
	}

	protected boolean setString(String key, String value) {
		SharedPreferences.Editor edit = preference.edit();
		edit.putString(key, value);
		return edit.commit();
	}

	protected boolean getBoolean(String key, boolean defValue) {
		return preference.getBoolean(key, defValue);
	}

	protected boolean setBoolean(String key, boolean value) {
		SharedPreferences.Editor edit = preference.edit();
		edit.putBoolean(key, value);
		return edit.commit();
	}

	protected int getInt(String key, int defValue) {
		return preference.getInt(key, defValue);
	}

	protected boolean setInt(String key, int value) {
		SharedPreferences.Editor edit = preference.edit();
		edit.putInt(key, value);
		return edit.commit();
	}

	protected long getLong(String key, long defValue) {
		return preference.getLong(key, defValue);
	}

	protected boolean setLong(String key, long value) {
		SharedPreferences.Editor edit = preference.edit();
		edit.putLong(key, value);
		return edit.commit();
	}

	protected float getFloat(String key, float defValue) {
		return preference.getFloat(key, defValue);
	}

	protected boolean setFloat(String key, float value) {
		SharedPreferences.Editor edit = preference.edit();
		edit.putFloat(key, value);
		return edit.commit();
	}

	protected boolean contains(String key) {
		return preference.contains(key);
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
	 * Live-Status of the App. If true, the app can be live, false can not.
	 * 
	 * @return True, the app can be live.
	 */
	public boolean canAppLive() {
		return getBoolean(APP_CAN_LIVE, false);
	}

	/**
	 * Get app version.
	 *
	 * @return the app version
	 */
	public String getAppVersion() {
		return getString(VERSION, "");
	}

	/**
	 * Save app version.
	 *
	 * @param value
	 *            the _value
	 * @return true, if successful
	 */
	private boolean setAppVersion(String value) {
		return setString(VERSION, value);
	}



	/**
	 * Save device id.
	 *
	 * @param value
	 *            the _value
	 * @return true, if successful
	 */
	private boolean setDeviceId(String value) {
		return setString(DEVICE_ID, value);
	}

	/**
	 * Get device model.
	 *
	 * @return the device model
	 */
	public String getDeviceModel() {
		return getString(MODEL, "");
	}

	/**
	 * Save device model.
	 *
	 * @param value
	 *            the _value
	 * @return true, if successful
	 */
	private boolean setDeviceModel(String value) {
		return setString(MODEL, value);
	}

	/**
	 * Get OS name.
	 *
	 * @return the os
	 */
	public String getOs() {
		return getString(OS, "");
	}

	/**
	 * Save OS name.
	 *
	 * @param value
	 *            the _value
	 * @return true, if successful
	 */
	private boolean setOs(String value) {
		return setString(OS, value);
	}

	/**
	 * Get OS version.
	 *
	 * @return the os version
	 */
	public String getOsVersion() {
		return getString(OS_VERSION, "");
	}

	/**
	 * Save current OS version.
	 *
	 * @param value
	 *            the _value
	 * @return true, if successful
	 */
	private boolean setOsVersion(String value) {
		return setString(OS_VERSION, value);
	}


	/**
	 * Download application's configuration, internal will use url that has been
	 * loaded from app.properties. It could use fallback if the url is invalid.
	 *
	 * @throws CanNotOpenOrFindAppPropertiesException
	 * @throws InvalidAppPropertiesException
	 */
	public void downloadApplicationConfiguration() throws CanNotOpenOrFindAppPropertiesException,
			InvalidAppPropertiesException {
		if (mExp != null) {
			setBoolean(APP_CAN_LIVE, false);
			throw mExp;
		}
		/*
		 * Request app's configuration.
		 */
		StringRequest request = new StringRequest(Request.Method.GET, getAppConfigUrl(),
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						LL.i(":) Loaded app's config: " + getAppConfigUrl());
						writePrefsWithStream(new ByteArrayInputStream(response.getBytes()));
					}
				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				LL.w(":( Can't load remote config: " + getAppConfigUrl());
				LL.i(":) We load fallback: " + getAppConfigFallbackUrl());
				writePrefsWithStream(mContext.getClassLoader().getResourceAsStream(
						getAppConfigFallbackUrl()));
			}
		});
		TaskHelper.getRequestQueue().add(request);
	}

	/**
	 * Read .properties of app's configuration in stream and write into
	 * preference. Finally send event to info front.
	 * 
	 * @param input
	 *            An input-stream.
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
				setString(name, valueStr);
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
			/* Read and info front. */
			BusProvider.getBus().post(new ApplicationConfigurationDownloadedEvent());
		}
	}
}
