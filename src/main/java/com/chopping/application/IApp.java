package com.chopping.application;

/**
 * Common interface that represent an application.
 */
public interface IApp {
	/**
	 * Package-name. Android application needs.
	 * @return Package-name for Android applications.
	 */
	String getPackageName();

	/**
	 * Location of the application to download.
	 * @return The url in string for the application.
	 */
	String getStoreUrl();
}
