package com.timhaasdyk.german_verbs;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

//The adapter for manager our "Pages"/Fragment Views
public class TabsPagerAdapter extends FragmentPagerAdapter {

	public TabsPagerAdapter(FragmentManager fm) {
		super(fm);
	}
	
	//When an item is called
	@Override
	public Fragment getItem(int index) {
		
		//Use the index to determine which Fragment is requested
		switch (index) {
		case 0:
			return new SearchFragment();
		case 1:
			return new ListsFragment();
		default:
			return null;
		}
	}
	
	//Returns the total number of tabs being used
	@Override
	public int getCount() {
		return 2;
	}

}
