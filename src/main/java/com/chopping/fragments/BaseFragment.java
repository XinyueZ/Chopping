package com.chopping.fragments;

import com.chopping.bus.BusProvider;

import android.support.v4.app.Fragment;

/**
 * Basic {@link android.support.v4.app.Fragment}.
 */
public   class BaseFragment extends Fragment {

	@Override
	public void onResume() {
		BusProvider.getBus().register(this);
		super.onResume();
	}

	@Override
	public void onPause() {
		BusProvider.getBus().unregister(this);
		super.onPause();
	}


}
