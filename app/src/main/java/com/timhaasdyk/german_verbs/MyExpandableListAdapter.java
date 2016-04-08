package com.timhaasdyk.german_verbs;

import android.app.Activity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

public class MyExpandableListAdapter extends BaseExpandableListAdapter {

	private SparseArray<VerbGroup> verbGroups;
	public LayoutInflater inflater;
	public Activity activity;
	
	public MyExpandableListAdapter(Activity act,
			SparseArray<VerbGroup> verbGroups) {
		//save references to the Activity and
		this.activity = act;
		//...array of verb groups
		this.verbGroups = verbGroups;
		
		//Instantiate an Inflater which is used to access individual views within a view
		inflater = act.getLayoutInflater();
	}

	//Returns a specified child/verb in the array of verbs in a verbGroup
	@Override
	public Object getChild(int groupPos, int childPos) {
		return verbGroups.get(groupPos).verbForms[childPos];
	}
	
	//returns a view representing an item within an expandable List
	@Override
	public View getChildView(int groupPos, final int childPos,
			boolean isLastChild, View rowView, ViewGroup parent) {
		
		String verb = (String) getChild(groupPos, childPos);
		if (rowView == null) {
			//Inflate the view to access the views inside it
			rowView = inflater.inflate(R.layout.listrow_child, null);//!!!!!parent?
		}
		
		//Set the text of the TextView inside the Expandable List Item View (row view)
		//to the verb pulled out of the verb group
		TextView txtVerbForm = (TextView) rowView.findViewById(R.id.txtVerbForm);
		txtVerbForm.setText(verb);
		
		//Return the modified View
		return rowView;
	}
	
	@Override
	public void onGroupCollapsed(int groupPos) {
		Log.i("in onGroupCollapsed", "Here I am!");
		super.onGroupCollapsed(groupPos);
	}
	
	@Override
	public void onGroupExpanded(int groupPos) {
		Log.i("in onGroupExpanded", "Here I am!");
		super.onGroupExpanded(groupPos);
	}


	@Override
	public long getChildId(int groupPos, int childPos) {
		return 0;
	}

	//Returns the number of verbs within a specified tense/verbGroup
	@Override
	public int getChildrenCount(int groupPos) {
		return verbGroups.get(groupPos).verbForms.length;
	}

	//Returns a specified verbGroup based on an index/groupPos
	@Override
	public Object getGroup(int groupPos) {
		return verbGroups.get(groupPos);
	}

	//Returns the number of verbGroups in the array of VerbGroups.
	@Override
	public int getGroupCount() {
		return verbGroups.size();
	}

	@Override
	public long getGroupId(int groupPos) {
		return 0;
	}

	//Returns a View representing an Expandable List's "header"
	@Override
	public View getGroupView(int groupPos, boolean isExpanded,
			View rowView, ViewGroup parent) {
		if (rowView == null) {
			//Inflate the View to access the views inside it
			rowView = inflater.inflate(R.layout.listrow_group, null);
		}

		//Inside is a CheckView which as an image representing if the Expandable List Item is expanded or not
		//and text as a title
		VerbGroup group = (VerbGroup) getGroup(groupPos);
		((CheckedTextView) rowView.findViewById(R.id.chkView)).setChecked(isExpanded);
		((TextView) rowView.findViewById(R.id.txtTenseG)).setText(group.verbTense[VerbGroup.GERMAN_TENSE]);
		((TextView) rowView.findViewById(R.id.txtTenseE)).setText(group.verbTense[VerbGroup.ENGLISH_TENSE]);

		//Return the updated Group View
		return rowView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		return false;
	}
}