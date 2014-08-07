package com.chopping.utils;

import android.content.Context;
import android.support.v4.hardware.display.DisplayManagerCompat;
import android.util.DisplayMetrics;
import android.view.Display;

import static com.chopping.utils.Consts.hdpi;
import static com.chopping.utils.Consts.ldpi;
import static com.chopping.utils.Consts.mdpi;
import static com.chopping.utils.Consts.tv;
import static com.chopping.utils.Consts.xhdpi;
import static com.chopping.utils.Consts.xxhdpi;
import static com.chopping.utils.Consts.xxxhdpi;

/**
 * Utils for device i.e access a logical of device-id, get screen size etc.
 */
public final class DeviceUtils {

	/**
	 * Get resolution of screen which kind of dpi will be detected.
	 *
	 * @param cxt
	 * 		{@link  android.content.Context} .
	 *
	 * @return {@link com.chopping.utils.Consts} .
	 */
	public static Consts getDeviceResolution(Context cxt) {
		int density = cxt.getResources().getDisplayMetrics().densityDpi;
		switch (density) {
			case DisplayMetrics.DENSITY_MEDIUM:
				return mdpi;
			case DisplayMetrics.DENSITY_HIGH:
				return hdpi;
			case DisplayMetrics.DENSITY_LOW:
				return ldpi;
			case DisplayMetrics.DENSITY_XHIGH:
				return xhdpi;
			case DisplayMetrics.DENSITY_TV:
				return tv;
			case DisplayMetrics.DENSITY_XXHIGH:
				return xxhdpi;
			case DisplayMetrics.DENSITY_XXXHIGH:
				return xxxhdpi;
			default:
				return Consts.UNKNOWN;
		}
	}

	/**
	 * Get {@link com.chopping.utils.DeviceUtils.ScreenSize} of default/first display.
	 *
	 * @param cxt
	 * 		{@link  android.content.Context} .
	 *
	 * @return A {@link com.chopping.utils.DeviceUtils.ScreenSize}.
	 */
	public static ScreenSize getScreenSize(Context cxt) {
		return getScreenSize(cxt, 0);
	}

	/**
	 * Get {@link com.chopping.utils.DeviceUtils.ScreenSize} with different {@code displayIndex} .
	 *
	 * @param cxt
	 * 		{@link android.content.Context} .
	 * @param displayIndex
	 * 		The index of display.
	 *
	 * @return A {@link com.chopping.utils.DeviceUtils.ScreenSize}.
	 */
	public static ScreenSize getScreenSize(Context cxt, int displayIndex) {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		Display[] displays = DisplayManagerCompat.getInstance(cxt).getDisplays();
		Display display = displays[displayIndex];
		display.getMetrics(displaymetrics);
		return new ScreenSize(displaymetrics.widthPixels, displaymetrics.heightPixels);
	}

	/**
	 * Screen-size in pixels.
	 */
	public static class ScreenSize {
		public int Width;
		public int Height;

		public ScreenSize(int _width, int _height) {
			Width = _width;
			Height = _height;
		}
	}
}
