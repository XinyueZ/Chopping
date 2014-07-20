package com.chopping.exceptions;

/**
 * Exception must  e fired when the "app.properties" is invalid.
 * For example:
 *
 * app_config=....
 * app_config_fallback=....
 *
 * These two properties must be included in.
 */
public final class InvalidAppPropertiesException extends Exception{
	@Override
	public String getMessage() {
		return "app.properties doesn't have standard properties like\napp_config\napp_config_fallback\netc.";
	}

	@Override
	public String toString() {
		return getMessage();
	}
}
