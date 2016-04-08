package com.timhaasdyk.german_verbs;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.ExpandableListView;


public class VerbGutsActivity extends Activity {
	
	//An array for the "groups" that each represent an expandable list item
	SparseArray<VerbGroup> verbGroups = new SparseArray<VerbGroup>();
	Cursor c;
	
	//Gain a reference to the database
	VerbsDB verbsDB;
	//Grab the pronouns from the database class
	String[] pronouns;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.verb_guts);
		//getActionBar().setDisplayHomeAsUpEnabled(true);

		verbsDB = new VerbsDB(getParent(), this);
		pronouns = verbsDB.getPronouns();
		//Pull out the verb from the passed intent
		Intent intent = getIntent();
		String verbG = intent.getStringExtra("verbG");
		String verbE = intent.getStringExtra("verbE");
		
		setTitle(verbG + " - " + verbE);
		
		//load up the Expandable List
		fillGroups(verbG);
	}
	
	private void fillGroups(String verb) {
		//For each "base" table, create a VerbGroup that will represent an Expandable List Item
		for (int tenseID = 0; tenseID < verbsDB.getBaseTableCount(); tenseID++) {
			fillGroup(tenseID, verb);
		}
		
		//Gain a reference to the Expandable List
		ExpandableListView listView = (ExpandableListView) findViewById(R.id.lstTenses);
		//Instantiate the Expandable List adapter for our list 
		MyExpandableListAdapter adapter = new MyExpandableListAdapter(
				this, verbGroups);
		
		//link the list and adapter
		listView.setAdapter(adapter);
	}
	
	//Creates and fills a VerbGroup representing a single Expandable List Item
	private void fillGroup(int tenseID, String verb) {
		//Select the verbs from the database based on the specified tense and verb
		c = verbsDB.getVerbSet(tenseID, verb);
		
		String[] verbForms = new String[c.getColumnCount()];
		
		//Just make sure that the verb was found (there may be a few errors in the database)
		if(c.moveToFirst()) //Then pull them all out and store them with their respective pronouns for display
			for (int i = 0; i < c.getColumnCount(); i++)
				verbForms[i] = pronouns[i] + " " + c.getString(i);
		
		//Add a new VerbGroup to the Array of VerbGroups
		verbGroups.append(tenseID, new VerbGroup(verbsDB.getTense(tenseID), verbForms));
	}
}
