package com.timhaasdyk.german_verbs;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

//Fragment for searching the verb database
public class SearchFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInsanceState) {
		//Get a reference to the base view
		View v = inflater.inflate(R.layout.verb_search_fragment, container,
				false);

		//So we can gain references to the list inside
		ListView lstVerbs = (ListView) v.findViewById(R.id.lstVerbs);
		//and set up a longClick listener on its items
		//lstVerbs.setOnItemLongClickListener(itemLongClickListener);

		this.setTargetFragment(getParentFragment(), getTargetRequestCode());
		return v;
	}

	@Override
	public void onStart() {
		super.onStart();

		EditText txtSearch = (EditText) getActivity().findViewById(R.id.txtSearch);
		txtSearch.getBackground().setColorFilter(getResources().getColor(R.color.primary2), PorterDuff.Mode.SRC_ATOP);
	}

	//Long click listener for the items in the verb List
	OnItemLongClickListener itemLongClickListener = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> list, View v,
				int pos, long id) {
			//Tag to identifiy the Dialog
			final String TAG = "listDialog";

			//Get the LongClicked verb
			String verb = ((TextView) v.findViewById(R.id.txtVerbG)).getText()
					.toString();
			
			//Prepare to stick a fragment into our Dialog
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			Fragment prev = getFragmentManager().findFragmentByTag(TAG);
			if (prev != null)
				ft.remove(prev);
			ft.addToBackStack(null);
			
			//Gain an instance of the ListSelectionDialogFragment, passing it the verb so that we can retreive
			//it from the bundle in the Dialog Fragment
			ListSelectionDialogFragment listDialog = ListSelectionDialogFragment.newInstance(verb);
			//Show the DialogFragment
			listDialog.show(ft, TAG);
			Toast.makeText(getActivity(), "Long click!", Toast.LENGTH_SHORT)
					.show();
			System.out.println("Long click!");
			return true;
		}
	};
	
	// Build the click event for the list items
	// private OnItemClickListener verbClickListener = new OnItemClickListener()
	// {
	//
	// @Override
	// public void onItemClick(AdapterView<?> list, final View v, int pos,
	// long id) {
	//
	// //Get the "clicked" verb
	// String verbG =
	// ((TextView)v.findViewById(R.id.txtVerbG)).getText().toString();
	// String verbE =
	// ((TextView)v.findViewById(R.id.txtVerbE)).getText().toString();
	//
	// //Pass it to the next intent
	// Intent i = new Intent("com.timhaasdyk.ideutsch.VerbGutsActivity");
	// i.putExtra("verbG", verbG);
	// i.putExtra("verbE", verbE);
	//
	// startActivity(i);
	// }
	//
	// };
}
