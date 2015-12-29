package com.chopping.rest;

import java.util.List;


public interface ExecutePending {
	void executePending( List<RestObject> pendingItems );

	RestObject build();
}
