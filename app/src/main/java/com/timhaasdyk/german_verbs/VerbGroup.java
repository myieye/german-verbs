package com.timhaasdyk.german_verbs;

//Class representing an Expandable List Item
public class VerbGroup {

	public static final int GERMAN_TENSE = 0;
	public static final int ENGLISH_TENSE = 1;

	//The title of the List Item
	public String[] verbTense;
	//The items within the Expandable List Item
	public String[] verbForms;
	
	public VerbGroup(String[] tense, String[] forms) {
		this.verbTense = tense;
		this.verbForms = forms;
	}
}
