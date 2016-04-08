package com.timhaasdyk.german_verbs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//Fragement for the Dictionary Search Screen
public class DictionaryFragment extends Fragment {

	EditText txtSearch;
	TextView txtResult;

	String[] lines;

	boolean started = false;

	// Max length of the results returned for sub and main finds (so total max =
	// 4000)
	final int MAX_LENGTH = 2000;

	// Name of the file to find in Assets and create internally
	final String DICT_FILE = "DE-EN dict.txt";

	// The search word (made public for a Handler
	public String word;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInsanceState) {
		// Grab the base view so we can use it
		View v = inflater.inflate(R.layout.dictionary_fragment, container,
				false);

		// set the button's click event
		Button btnSearch = (Button) v.findViewById(R.id.btnSearch);
		btnSearch.setOnClickListener(onClickListener);

		// Grab references to the text fiels
		txtSearch = (EditText) v.findViewById(R.id.txtSearch);
		txtResult = (TextView) v.findViewById(R.id.txtResult);
		// Make the results scrollable
		txtResult.setMovementMethod(new ScrollingMovementMethod());

		// Check if the array representing the dictionary exists yet
		// And check to see if this was the first load, so as not to start
		// loading the array a second time while the first one is still running
		if (lines == null && started == false) {
			// Note that the array is now loading
			started = true;

			// A Thread to load the array in the background
			Thread theThread = new Thread(new Runnable() {
				public void run() {
					try {
						// Copy the file from Assets (if not already copied)
						File dictFile = copyFileContents();
						// Get the contents of the dictionary as a string (this
						// is the slow code)
						String fileContents = readFile(dictFile);
						// turn the contents into an Array for easy searching
						lines = fileContents.split(System
								.getProperty("line.separator"));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			theThread.start();
		}

		this.setTargetFragment(getParentFragment(), getTargetRequestCode());
		return v;
	}

	// Click event for the Search button
	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// grab the word entered
			word = txtSearch.getText().toString();
			// Verify that the field is not empty
			if (word.trim().equals(""))
				Toast.makeText(getActivity(), "Please enter a word",
						Toast.LENGTH_SHORT).show();
			else {
				// Check if the array is ready
				if (lines == null) { // if not...
					// Load a progress dialog to show the user that it's still
					// loading
					final ProgressDialog pDialog = ProgressDialog.show(
							getActivity(), "Word list still loading...",
							"Please Wait", true);

					// A Thread to check for the creation of the array
					Thread theThread = new Thread(new Runnable() {
						public void run() {
							try {
								// Check the array every 100 millis
								while (lines == null)
									Thread.sleep(100);

								// close the dialog
								pDialog.dismiss();
								// Tell the handler that we have a job for it
								handler.sendEmptyMessage(0);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					});
					theThread.start();
				}
				// If everything's good, then just do the search
				doSearch(word);
			}
		}
	};

	// The Handler can put things in a queue for the Main Activity to perform
	// This is necessary because a Separate Thread cannot access the views in
	// the base view
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// Run the search
			doSearch(word);
		}
	};

	// Method for searching the dictionary and displaying results
	private void doSearch(String word) {

		int wl = word.length();
		// the regular expression is in accordance with the file structure
		String regExp = word + "(( :)|( \\|)|(; )|( \\()|( \\{))";
		// Main finds are displayed on top and
		// sub Finds on the bottom
		String mainOutput = "";
		String subOutput = "";

		try {
			// Loop through each line
			for (String strLine : lines) {
				// Check that the current line contains the sought word
				if (strLine.indexOf(word) != -1
						&& (mainOutput.length() < MAX_LENGTH || subOutput
								.length() < MAX_LENGTH)) {

					// Break the line into German and English parts
					String[] line = strLine.split(":: ");

					String lg = line[0];
					String le;
					if (line.length > 1) // A few irregular lines will not split
											// into two, but they are irrelevant
						le = line[1];
					else
						le = "";

					// Determine if we need to check punctuation after the word
					// in order to verify a perfect match (that's where the
					// regExp comes into play
					// Otherwise we're just going to compare the words
					int gEndI = (lg.indexOf(" ") != lg.lastIndexOf(" ") ? 2 : 0);
					int eEndI = (le.indexOf(" ") != le.lastIndexOf(" ") ? 2 : 0);

					// Determine if the line in the dictionary is actually long
					// enough for comparison
					String gWord = (lg.length() > (wl + gEndI)) ? lg.substring(
							0, wl + gEndI) : lg.trim();
					String eWord = (le.length() > (wl + eEndI)) ? le.substring(
							0, wl + eEndI) : le.trim();

					// check for good matches
					if (mainOutput.length() < MAX_LENGTH
							&& gWord.matches(gEndI == 0 ? word : regExp)
							|| eWord.matches(eEndI == 1 ? word : regExp)) {
						mainOutput += "- GERMAN:\n"
								+ lg.trim().replaceAll("\\|", "\n")
								+ "\n- ENGLISH:\n"
								+ le.trim().replaceAll("\\|", "\n") + "\n\n";
					} else if (subOutput.length() < MAX_LENGTH) //and less than perfect matches
						subOutput += "- GERMAN:\n"
								+ lg.trim().replaceAll("\\|", "\n")
								+ "\n- ENGLISH:\n"
								+ le.trim().replaceAll("\\|", "\n") + "\n\n";
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		//Display the output
		txtResult = (TextView) getView().findViewById(R.id.txtResult);
		txtResult.setText(mainOutput + "\n-----------------\n\n" + subOutput);
	}

	private File copyFileContents() {

		// The internal file path of the dictionary file
		String filePath = getActivity().getFilesDir().getPath() + "/"
				+ DICT_FILE;
		File f = new File(filePath);

		// Check that the file exists or needs to be brought in from Assets
		if (!f.exists())

			try {
				f.createNewFile();

				// Open the two necessary Streams
				InputStream input = getActivity().getAssets().open(DICT_FILE);

				OutputStream output = new FileOutputStream(filePath);

				// Use the input and output streams to copy the dictionary into
				// internal memory
				byte[] buffer = new byte[1024];
				int length;
				while ((length = input.read(buffer)) > 0) {
					output.write(buffer, 0, length);
				}

				// Close 'er up
				output.flush();
				output.close();
				input.close();

			} catch (Exception e) {
				e.printStackTrace();
			}

		// Return the file that now definitely exists
		return new File(filePath);
	}

	private String readFile(File file) throws IOException {

		// Use a String builder to turn the file contents into one big String
		StringBuilder fileContents = new StringBuilder((int) file.length());

		Scanner scanner = new Scanner(file);

		// Place a lineSeparater between each line
		// This makes it easy to split them into an array
		String lineSeparator = System.getProperty("line.separator");

		try {// Take every line
			while (scanner.hasNextLine()) {
				fileContents.append(scanner.nextLine() + lineSeparator);
			}

			// Return the entire contents of the file
			return fileContents.toString();
		} finally {
			scanner.close();
		}
	}
}