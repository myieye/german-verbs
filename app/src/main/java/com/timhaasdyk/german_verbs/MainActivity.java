package com.timhaasdyk.german_verbs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.content.Intent;
import android.database.Cursor;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.timhaasdyk.german_verbs.util.AppRater;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener {

	private ViewPager viewPager;
	private TabsPagerAdapter pagerAdapter;
	private int activeTab = 0;

	private ActionBar actionBar;

	private ListView lstView;
	private SimpleAdapter adapter;

	// Tab titles
	private String[] tabs;

	// A cursor to use throughout the class
	Cursor c;
	// My database pointer for use throughout the class
	VerbsDB verbsDB;

	// Hash map for storing the search result
	List<HashMap<String, Object>> verbsFoundMain;
	List<HashMap<String, Object>> verbsFoundFavs;
	List<HashMap<String, Object>> verbsFound = verbsFoundMain;


	// Hash map for checking the favourite status of verbs
	HashMap<String, Object> verbFavsMain;
	HashMap<String, Object> verbFavsFavs;
	HashMap<String, Object> verbFavs = verbFavsMain;

	Purchases purchaseInstance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		purchaseInstance = Purchases.getInstance(getParent(), this);

		//checkAds();

		//Gain an instance of the databaseSurgeon
		verbsDB = new VerbsDB(getParent(), this);
		
		//The View Pager is for fliping between tabs/pages/fragments
		viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();
		
		//Setup the pager with its respective adapter
		pagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(pagerAdapter);
		
		//Give it Listener so that we can update the tab selection on swipes
		viewPager.setOnPageChangeListener(onPageChangeListener);
		//actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		tabs = new String[] {getString(R.string.tab_verb_search), getString(R.string.tab_favourites)};

		// Add the tabs
		for (String tab : tabs) {
			actionBar.addTab(actionBar.newTab().setText(tab)
					.setTabListener(this));
		}

		AdView mAdView = (AdView) findViewById(R.id.adViewHomeBanner);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);

		AppRater appRater = new AppRater(this);
		appRater.setPhrases(R.string.rate_title, R.string.rate_explanation, R.string.rate_now, R.string.rate_later, R.string.rate_never);
		AlertDialog mAlertDialog = appRater.show();

		if (mAlertDialog != null) {
			try {
				styleDialog(mAlertDialog);
			}catch (Exception ex) {

			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		lstView = (ListView) findViewById(R.id.lstVerbs);

		String searchText = ((TextView) findViewById(R.id.txtSearch))
				.getText().toString();

		/*int index = lstView.getFirstVisiblePosition();
		View v = lstView*/
		int listCount = (verbsFound != null) ? verbsFound.size() : 0;

		savedInstanceState.putString("SearchText", searchText);
		savedInstanceState.putSerializable("FavsList", verbFavs);
		savedInstanceState.putInt("ListCount", listCount);
		savedInstanceState.putParcelable("listState", lstView.onSaveInstanceState());

		for (int i = 0; i < listCount; i++)
			savedInstanceState.putSerializable("Verb:" + i, verbsFound.get(i));
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		String searchText = savedInstanceState.getString("SearchText");
		int index = savedInstanceState.getInt("index");
		int listCount = savedInstanceState.getInt("ListCount");
		Parcelable listState = savedInstanceState.getParcelable("listState");

		((TextView) findViewById(R.id.txtSearch)).setText(searchText);
		verbFavs = (HashMap<String, Object>) savedInstanceState.getSerializable("FavsList");

		verbsFound = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < listCount; i++)
			verbsFound.add((HashMap < String, Object>)
					savedInstanceState.getSerializable("Verb:" + i)
			);

		fillList();

		((ListView) findViewById(R.id.lstVerbs)).onRestoreInstanceState(listState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		purchaseInstance.destroy();
	}

	AlertDialog.Builder builder;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		builder = new AlertDialog.Builder(this);
		AlertDialog d = null;

		switch(item.getItemId()) {
			case(R.id.action_unlock):
				builder.setTitle(getString(R.string.alert_title_purchases));

				View dialogView = this.getLayoutInflater().inflate(R.layout.dialog_unlock, null);
				Switch swcVerbs = (Switch) dialogView.findViewById(R.id.swtAllVerbs);
				Switch swcAdds = (Switch) dialogView.findViewById(R.id.swtRemoveAds);

				setSwitchEvents(swcVerbs, swcAdds);

				Boolean purchases[] = purchaseInstance.getPurchases();

				Boolean purchasedAllVerbs = purchases[Purchases.ALL_VERBS];
				Boolean purchasedRemoveAdds = purchases[Purchases.REMOVE_ADS];
				swcVerbs.setChecked(purchasedAllVerbs);
				swcVerbs.setEnabled(!purchasedAllVerbs);

				swcAdds.setChecked(purchasedRemoveAdds);
				swcAdds.setEnabled(!purchasedRemoveAdds);

				purchaseInstance.addPricesToView(dialogView);

				builder.setView(dialogView);
				builder.setNeutralButton("OK", closeDialogClickListener);

				d = builder.show();
				purchaseInstance.dialogReady = false;
				break;
			case (R.id.action_help):
				builder.setTitle(getString(R.string.alert_title_help));
				builder.setMessage(Html.fromHtml(getHelpMessage()));
				builder.setNeutralButton("OK", closeDialogClickListener);
				d = builder.show();
				break;
			/*case(R.id.action_settings):
				break;*/
			case (R.id.action_info):
				builder.setTitle(getString(R.string.alert_title_license));
				builder.setMessage(getGNUPolicy());
				builder.setNeutralButton("OK", closeDialogClickListener);
				d = builder.show();
				break;
		}

		if (d != null)
			styleDialog(d);

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

	//Goes to the relevant "Page" when a tab is selected
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		activeTab = tab.getPosition();
		viewPager.setCurrentItem(activeTab);
		if (activeTab == 1) {
			loadVerbList("");
		} else {
			verbFavs = verbFavsMain;
			verbsFound = verbsFoundMain;

			selectSearchText();
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

	ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {

		//Selects the relevant tab with a "Page" is swiped to
		@Override
		public void onPageSelected(int pos) {
			actionBar.setSelectedNavigationItem(pos);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}
	};

	// Click event for the search button
	public void onSearchClick(View v) {
		EditText txtSearch = (EditText) this.findViewById(R.id.txtSearch);

		String search = txtSearch.getText().toString().toLowerCase().trim();

		// Pass the "clicked" verb to the method that searches the database ad
		// loads the list
		loadVerbList(search);

		hideKeyboard();
	}

	//Toggle whether a verb is a favourite or not when the start is clicked
	public void toggleFavourite(View v) {
		//Get the verb that goes with the star
		View verbItem = (View) v.getParent();
		String verbG = ((TextView) verbItem.findViewById(R.id.txtVerbG))
				.getText().toString();
		int i = Integer.valueOf(((TextView) verbItem.findViewById(R.id.index))
				.getText().toString());

		//Check the image that is currently displayed and use it to determine if the verb
		//should be a favourite now or not
		int currImgR = (Integer) verbFavs.get(verbG);

		boolean isFav = currImgR == R.drawable.fav_yes ? true : false;
		boolean newFavStatus = !isFav;
		int newImgR = newFavStatus ? R.drawable.fav_yes : R.drawable.fav_no;

		//Make the change in the database
		verbsDB.setFavStatus(verbG, newFavStatus);

		lstView = (ListView) this.findViewById(R.id.lstVerbs);
		verbsFound.get(i).put("imgFav", newImgR);
		adapter.notifyDataSetChanged();

		//Make the change in the view
		((ImageView) v).setImageResource(newImgR);
		
		//Make the change in the array
		verbFavs.put(verbG, newImgR);
	}

	// Loads the list of verbs based on the word the user enters
	private void loadVerbList(String searchWord) {

		// Retrieves the cursor
		switch(activeTab) {
			case (0):
				c = verbsDB.getInfinitives(searchWord);
				break;
			case (1):
				c = verbsDB.getFavourites();
				break;
		}

		//check for all verbs purchase
		//if ()

		// Get the number of verbs found
		int count = c.getCount();

		// build an array that we can feed to an adapter
		verbsFound = new ArrayList<HashMap<String, Object>>();

		// HashMap for tracking the fav status of displayed verbs
		if (activeTab == 0) {
			verbsFoundMain = new ArrayList<HashMap<String, Object>>();
			verbsFound = verbsFoundMain;

			verbFavsMain = new HashMap<String, Object>();
			verbFavs = verbFavsMain;
		} else {
			verbsFoundFavs = new ArrayList<HashMap<String, Object>>();
			verbsFound = verbsFoundFavs;

			verbFavsFavs = new HashMap<String, Object>();
			verbFavs = verbFavsFavs;
		}

		// String[] verbs = new String[count];
		HashMap<String, Object> verbItem;

		//Turn the cursor into an array
		//(I should probably just be using a CursorAdapter)
		while (c.moveToNext()) {
			verbItem = new HashMap<String, Object>();
			Integer favInt = (c.getInt(2) == 1 ? R.drawable.fav_yes
					: R.drawable.fav_no);

			if (activeTab == 0) {
				Integer flagInt = (c.getString(8)).equals("de") ? R.drawable.flag_de
						: R.drawable.flag_en;
				verbItem.put("imgFlag", flagInt);
			}

			verbItem.put("txtVerbG", c.getString(0));
			verbItem.put("txtVerbE", c.getString(1));
			verbItem.put("imgFav", favInt);
			verbItem.put("pastParticiple", c.getString(3) + " (" + c.getString(4) + ")");
			verbItem.put("simplePast", c.getString(5));
			verbItem.put("strength", c.getString(6));
			verbItem.put("type", c.getString(7));
			verbItem.put("index", c.getPosition());
			verbItem.put("txtStrengthTypeSeparator", (c.getString(7).length() > 0 ? " - " : ""));

			verbsFound.add(verbItem);

			verbFavs.put(c.getString(0), favInt);
		}

		fillList();
	}

	private void fillList() {

		//Arrays required for the SimpleAdapter
		String[] from = new String[] { "txtVerbG", "txtVerbE",
				"pastParticiple", "simplePast",
				"strength", "type", "txtStrengthTypeSeparator",
				"imgFav", "imgFlag", "index"};
		int[] to = new int[] { R.id.txtVerbG, R.id.txtVerbE,
				R.id.txtPastParticiple, R.id.txtSimplePast,
				R.id.txtStrength, R.id.txtType, R.id.txtStrengthTypeSeparator,
				R.id.imgFav, R.id.imgFlag, R.id.index };

		if (activeTab == 0)
			adapter = new SimpleAdapter(this, verbsFound,
					R.layout.listitem_verb, from, to);
		else
			adapter = new SimpleAdapter(this, verbsFound,
					R.layout.listitem_verb_favs, from, to);

		switch(activeTab) {
			case (0):
				lstView = (ListView) this.findViewById(R.id.lstVerbs);
				break;
			case (1):
				lstView = (ListView) this.findViewById(R.id.lstVerbsFavs);
				break;
		}
		// Instantiate an adapter with our list and a default List layout
		// link the list to the adapter
		lstView.setAdapter(adapter);

		// give the list a click event for its items
		lstView.setOnItemClickListener(verbClickListener);
	}

	private String getGNUPolicy() {

		StringBuilder policy = new StringBuilder();

		Resources resources = getResources();

		try {
			InputStream in;

			in = resources.openRawResource(R.raw.license_intro);
			addInputStreamToStringBuilder(policy, in);

			in = resources.openRawResource(R.raw.gnu_general_public_license_v2);//aMan.open("raw-en/gnu_general_public_license_v2.txt");
			addInputStreamToStringBuilder(policy, in);

		} catch (IOException e) {
			policy.append(getString(R.string.license_not_found));
		}

		return policy.toString();
	}

	private String getHelpMessage() {
		StringBuilder help = new StringBuilder();

		InputStream in = getResources().openRawResource(R.raw.help);

		try {
			addInputStreamToStringBuilder(help, in);
		} catch (IOException e) {
			e.printStackTrace();
			help = new StringBuilder(getString(R.string.help_file_fail));
		}

		return help.toString();
	}

	public void onClickUnlockVerbs(View v) {

	}

	public void onClickUnlockAdds(View v) {

	}

	//The listener for when a verb is selected
	OnItemClickListener verbClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
			//
			// Get the "clicked" verb
			String verbG = ((TextView) v.findViewById(R.id.txtVerbG)).getText()
					.toString();
			String verbE = ((TextView) v.findViewById(R.id.txtVerbE)).getText()
					.toString();

			// Pass it to the next intent
			Intent i = new Intent("com.timhaasdyk.german_verbs.VerbGutsActivity");
			i.putExtra("verbG", verbG);
			i.putExtra("verbE", verbE);

			startActivity(i);
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		selectSearchText();
	}

	DialogInterface.OnClickListener closeDialogClickListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {

		}
	};

	private static void addInputStreamToStringBuilder(StringBuilder string, InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		String line;
		while((line = reader.readLine()) != null)
			string.append(line).append("\n");
	}

	private void styleDialog(AlertDialog d) {
		int dividerId = d.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
		View divider = d.findViewById(dividerId);
		divider.setBackgroundColor(getResources().getColor(R.color.primary));

		int textViewId = d.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
		TextView tv = (TextView) d.findViewById(textViewId);
		tv.setTextColor(getResources().getColor(R.color.primary));
	}

	private void hideKeyboard() {
		InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
		//Find the currently focused view, so we can grab the correct window token from it.
		View view = this.getCurrentFocus();
		//If no view currently has focus, create a new one, just so we can grab a window token from it
		if(view == null) {
			view = new View(this);
		}
		inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	public void checkAds() {
		if (purchaseInstance != null) {
			if (purchaseInstance.getPurchases()[Purchases.REMOVE_ADS])
				findViewById(R.id.adViewHomeBanner).setVisibility(View.GONE);
		}
	}

	private void selectSearchText() {EditText txtSearch = (EditText) findViewById(R.id.txtSearch);

		if (txtSearch != null) {
			if (txtSearch.length() > 0) {
				if (txtSearch.requestFocus()) {
					getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
					txtSearch.selectAll();
				}
			}
		}
	}

	private void setSwitchEvents(Switch swcVerbs, Switch swcAdds) {

		swcVerbs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked || purchaseInstance.getPurchases()[purchaseInstance.ALL_VERBS])
					return;

				purchaseInstance.makePurchase(purchaseInstance.ALL_VERBS);
			}
		});

		swcAdds.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked || purchaseInstance.getPurchases()[purchaseInstance.REMOVE_ADS])
					return;

				purchaseInstance.makePurchase(purchaseInstance.REMOVE_ADS);
			}
		});
	}

	/*@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (purchaseInstance != null && resultCode == RESULT_CANCELED)
			purchaseInstance.onActivityResult(requestCode, resultCode, data);


	}*/

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (purchaseInstance != null) {
			// Pass on the activity result to the helper for handling
			if (!purchaseInstance.handleActivityResult(requestCode, resultCode, data)) {
				// not handled, so handle it ourselves (here's where you'd
				// perform any handling of activity results not related to in-app
				// billing...
				super.onActivityResult(requestCode, resultCode, data);
			} else {
				Log.d("OnActivityResult", "onActivityResult handled by IABUtil.");
			}
		}
	}
}