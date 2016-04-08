package com.timhaasdyk.german_verbs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

//Fragment for letting users select specific lists of verbs to view
public class ListsFragment extends Fragment {

	ListView lstFavVerbs;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInsanceState) {
		View v = inflater.inflate(R.layout.verb_lists_fragment, container, false);
		
		//Get a reference to the List of Lists
		lstFavVerbs = (ListView) v.findViewById(R.id.lstVerbsFavs);

		return v;
	}

	private void loadFavoritesList() {

	}
}
