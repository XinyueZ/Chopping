package com.chopping.bus;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Event-Bus provider for system.
 *
 * @author Xinyue Zhao
 */
public final class BusProvider {

	private static volatile Bus sBus = new Bus(ThreadEnforcer.MAIN);


	public static Bus getBus() {
		return sBus;
	}
}
