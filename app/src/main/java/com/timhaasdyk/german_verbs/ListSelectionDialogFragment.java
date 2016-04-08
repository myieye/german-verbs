package com.timhaasdyk.german_verbs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.widget.EditText;

//This ListSelectionDialogFragment is for adding verbs to lists for later reference
public class ListSelectionDialogFragment extends DialogFragment implements
		DialogInterface.OnClickListener {

	//final String LIST_NAMES_FILE = "ListNames.txt";
	//final String[] LOCKED_LISTS = new String[] { "100common", "200common" };
	
	//String listFilePath;
	//String fileDir;

	//String newName;

	//boolean doSave;

	//The New List option in the List of Lists
	final String NEW_LIST = "New List";
	//The verb to save to a new list
	String mVerb;
	//The list selected from the list of lists
	String mListName;
	//The List of Lists to choose from
	String[] lstLists;

	static ListSelectionDialogFragment newInstance(String verb) {
		ListSelectionDialogFragment dialogFragment = new ListSelectionDialogFragment();

		// Return a ListSelectionDialogFragment with a Bundle instead of the default
		Bundle bunds = new Bundle();
		bunds.putString("verb", verb);
		dialogFragment.setArguments(bunds);

		return dialogFragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);

		//Grab the verb that was long clicked
		mVerb = getArguments().getString("verb");

		//fileDir = getActivity().getFilesDir().getPath();
		//listFilePath = fileDir + LIST_NAMES_FILE;
		//doSave = true;
		
		//A sample list for siplay purposes
		lstLists = new String[]{"Sample List 1", "Sample List 2", NEW_LIST};

		// The Builder class is for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Add \"" + mVerb + "\" to:").setItems(lstLists, this);

		//Return the Dialog for display
		return builder.create();
	}

	//Triggered when a list is selected from the list
	@Override
	public void onClick(DialogInterface dialog, int pos) {
		//Retrieve the List based on an index
		mListName = lstLists[pos];

		//Check for the "New List" Option
		if (mListName.equals(NEW_LIST)) {
			makeNewList();
		}
	}

//	private String[] getListOfLists() {
//
//		String names = "";
//
//		try {
//			File lstFile = new File(listFilePath);
//			if (!lstFile.exists()) {
//				lstFile.createNewFile();
//			} else {
//				FileInputStream inputStream = new FileInputStream(listFilePath);
//				BufferedReader reader = new BufferedReader(
//						new InputStreamReader(inputStream));
//
//				String line;
//
//				boolean first = true;
//
//				while ((line = reader.readLine()) != null) {
//					if (first)
//						first = false;
//					else
//						names += ";;;";
//
//					names += line;
//
//				}
//
//				inputStream.close();
//				reader.close();
//
//				names += ";;;";
//			}
//
//			names += NEW_LIST;
//
//			return names.split(";;;");
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//
//		return null;
//	}

	//Displays a dialog for entering a new list name
	private void makeNewList() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("New List Name");

		// Set up the EditText for input
		final EditText input = new EditText(getActivity());
		
		// Specify the input type
		input.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_NORMAL);
		builder.setView(input);

		// Set up the buttons
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
//				newName = input.getText().toString();
//				mListName = newName;
//				if (!Arrays.asList(lstLists).contains(newName)) {
//					createFile(newName);
//				} else {
//					Toast.makeText(getActivity(), "Name Already In Use",
//							Toast.LENGTH_LONG).show();
//				}
			}

//			private void createFile(String fileName) {
//				File newFile = new File(fileDir + fileName);
//				try {
//					newFile.createNewFile();
//					
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//			}
		});

		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
//						doSave = false;
//						dialog.cancel();
					}
				});

		builder.show();
	}
//	
//	public String getSavePath() {
//		return fileDir + mListName;
//	}
//	
//	public String getVerb() {
//		return mVerb;
//	}
//
//	private void copyListFile(File file) {
//		try {
//			file.createNewFile();
//			InputStream input = getActivity().getAssets().open(LIST_NAMES_FILE);
//
//			OutputStream output = new FileOutputStream(listFilePath);
//
//			// Use the input and output streams to copy my database into the
//			// recently created one
//			byte[] buffer = new byte[1024];
//			int length;
//			while ((length = input.read(buffer)) > 0) {
//				output.write(buffer, 0, length);
//			}
//
//			// Close 'er up
//			output.flush();
//			output.close();
//			input.close();
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//	}
}
