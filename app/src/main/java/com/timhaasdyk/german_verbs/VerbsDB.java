package com.timhaasdyk.german_verbs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class VerbsDB {
	//I'm not using these yet but they're table and View names
	static final String INF_TABLE = "infinitive";
	static final String PAST_TABLE = "past";
	static final String ALL_VERBS = "AllVerbs";

	//Column names
	static final String VERB_ID = "_id";

	static final String ENG = "english";

	static final String FIRST_S = "firstS";
	static final String SECOND_S = "secondS";
	static final String THIRD_S = "thirdS";
	static final String FIRST_P = "firstP";
	static final String SECOND_P = "secondP";
	static final String SECOND_PF = "secondPF";
	static final String THIRD_P = "thirdP";
	static final String THIRD_P_SECOND_PF = "thirdP";

	//The base tables in my Database (i.e. the tables with the majority of the data that are all in the
	//same format
	final String[] BASE_TABLES = new String[] {"Present", "Past", "Future",
			"Pres_Perf", "Past_Perf", "Fut_Perf", "Pres_Sub_I", "Pres_Sub_II",
			"Past_Sub_I", "Past_Sub_II", "Fut_Sub_I", "Fut_Sub_II",
			"Fut_Perf_Sub_I", "Fut_Perf_Sub_II"};

	//The verb tenses in German
	final private String[] GERMAN_TENSES = new String[] {
			"Indikativ Präsens", "Indikativ Präteritum", "Indikativ Futur I",
			"Indikativ Perfekt", "Indikativ Plusquamperfekt", "Indikativ Futur II",
			"Konjunktiv I Präsens", "Konjunktiv II Präteritum", "Konjunktiv I Perfekt",
			"Konjunktiv II Plusquamperfekt", "Konjunktiv I Futur I", "Konjunktiv II Futur I",
			"Konjunktiv I Futur II", "Konjunktiv II Futur II", "Infinitiv", "Imperativ"
	};

	//The verb tenses in English
	final private String[] ENGLISH_TENSES = new String[] {
			"Indicative Present", "Indicative Simple Past", "Indicative Future",
			"Indicative Present Perfect", "Indicative Past Perfect", "Indicative Future Perfect",
			"Subjunctive I Present", "Subjunctive II Present", "Subjunctive I Past",
			"Subjunctive II Past", "Subjunctive I Future", "Subjunctive II Future",
			"Subjunctive I Future Perfect","Subjunctive II Future Perfect",
			"Imperative", "Infinitive"
	};

	//The pronouns that precede each verb case
	final private String[] PRONOUNS = new String[]{"ich", "du", "er/sie/es", "wir", "ihr", "sie/Sie"};

	private class DatabaseHelper extends SQLiteOpenHelper {

		// Android' default system path for application databases
		private final String DB_PATH;
		private static final String DB_NAME = "VerbsUTF8Android.sqlite";
		static final int DB_VERSION = 6;

		private final Context myContext;
		private final Activity myActivity;

		/*
		 * Constructor - Takes the reference of the passed context so it can
		 * access the application assets and resources
		 */
		public DatabaseHelper(Activity activity, Context context) {
			super(context, DB_NAME, null, DB_VERSION);
			this.myContext = context;
			this.myActivity = activity;

			DB_PATH = context.getDatabasePath(DB_NAME).getPath();

			if (databaseNeedsCreating())
				this.createAndCopyDatabase();
			else if (databaseNeedsUpgrading())
				this.upgradeDatabase();
		}

		/*
		 * creates an empty database on the system and rewrites it with ours
		 */
		private void createAndCopyDatabase() {

			Log.d("*tim*", "createAndCopy()");
			//...create a new one
			this.getReadableDatabase();
			//close it
			this.close();

			try {//...then copy my database into the new one
				copyDatabase();
			} catch (IOException e) {
				e.printStackTrace();
				throw new Error("Error copying database");
			}

		}

		private void upgradeDatabase() {
			Log.d("*tim*", "upgrade()");

			ArrayList<String> favs = saveFavorites();
			deleteDatabase();
			createAndCopyDatabase();
			verbsDB = this.getWritableDatabase();
			restoreFavorites(favs);
		}

		private ArrayList<String> saveFavorites() {
			verbsDB = this.getWritableDatabase();
			Cursor c = getFavourites();

			ArrayList<String> favs = new ArrayList<String>();

			c.moveToFirst();
			while(!c.isAfterLast()) {

				favs.add(c.getString(0));
				c.moveToNext();
			}
			return favs;
		}

		private void restoreFavorites(ArrayList<String> favs) {
			ContentValues args = new ContentValues();
			args.put("favourite", 1);
			String whereClause = "";

			boolean first = true;

			for(String fav : favs){

				if (!first)
					whereClause += " OR ";
				else
					first = false;

				whereClause += "InfinitiveG='" + fav + "'";
			}

			verbsDB.update(INF_TABLE, args, whereClause, null);
		}

		/*
		 * Check if the database already exists This way it will only be copied
		 * the first time the application runs
		 */
		private boolean databaseNeedsCreating() {

			File dbFile = new File(DB_PATH);

			return !dbFile.exists();
		}

		private boolean databaseNeedsUpgrading() {

			getReadableDatabase(); //Opens the database, causing onUpgrade() to be called
					// but we don't want to mess around with the file itself while the open helper
					// isusing it

			return needsUpgrade;
		}

		private void deleteDatabase() {
			File dbFile = new File(DB_PATH);

			if (dbFile.exists())
				dbFile.delete();
		}

		/*
		 * Copies the database from the local assets-folder to the system so
		 * that it can be accessed
		 */
		private void copyDatabase() throws IOException {
			// Open the database
			InputStream input = myContext.getAssets().open(DB_NAME);

			String path = DB_PATH;

			OutputStream output = new FileOutputStream(path);

			//Use the input and output streams to copy my database into the recently created one
			byte[] buffer = new byte[1024];
			int length;
			while ((length = input.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}

			//Close 'er up
			output.close();
			output.flush();
			input.close();
		}

		//Opens the database and then returns it
		public SQLiteDatabase openDataBase() throws SQLException {
			// Open the database

			boolean test = databaseNeedsCreating();
			return SQLiteDatabase.openDatabase(DB_PATH, null,
					SQLiteDatabase.OPEN_READWRITE);
		}

		@Override
		public synchronized void close() {
			if (verbsDB != null)
				verbsDB.close();

			super.close();
		}

		//I don't need to create any tables or anything
		//because my database already exists
		@Override
		public void onCreate(SQLiteDatabase db) {
		}

		private boolean needsUpgrade = false;
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			needsUpgrade = true;
		}
	}

	private DatabaseHelper dbHelper;
	private SQLiteDatabase verbsDB;

	public VerbsDB(Activity a, Context c) {
		//Instantiate the database helper and open the database
		dbHelper = new DatabaseHelper(a, c);
		verbsDB = dbHelper.getWritableDatabase();
		Log.d("*tim*", "curr: " + verbsDB.getVersion());

		Log.d("*tim*", "finished!");
	}

	//Method for selecting the infinitives on the initial verb search
	public Cursor getInfinitives(String search) {

		/*String tableName = INF_TABLE + " JOIN " + PAST_TABLE + " USING(" + VERB_ID +")";
		String[] infColumns = new String[] {"InfinitiveG", "InfinitiveE", "favourite", "pastParticiple", "pastAuxiliary", "thirdS AS 'simplePast'", "strength", "type"};

		//Selects records in which the German Infinitive *contains* the word
		//rather than specifically matches is
		String whereClause = "infinitiveG LIKE '%" + search + "%'";

		Purchases purchasesInstance;

		try {
			purchasesInstance = Purchases.getInstance(dbHelper.myContext);
		} catch (NullPointerException ex) {
			purchasesInstance = Purchases.getInstance(null);
		}

		if (!purchasesInstance.getPurchases()[Purchases.ALL_VERBS]) {
			whereClause += " AND infinitiveG IN (" + purchasesInstance.getTrialVerbs() +")";
		}


		String orderBy = "length(InfinitiveG), InfinitiveG";

		verbsDB = dbHelper.getWritableDatabase();
		//Gets and returns the cursor representing the result
		return verbsDB.query(tableName, infColumns,
				whereClause, null, null, null, orderBy);
		*/

		Purchases purchasesInstance;
		purchasesInstance = Purchases.getInstance(dbHelper.myActivity, dbHelper.myContext);

		String query = "SELECT * FROM \n" +
				"(SELECT infinitiveG, infinitiveE, favourite, pastParticiple, pastAuxiliary, thirdS AS 'simplePast', strength, type, 'de', infinitiveG AS 'keyInfinitive', length(infinitiveG) AS 'keyLength'\n" +
				"FROM infinitive JOIN past USING(_id)\n" +
				"WHERE infinitiveG LIKE '%" + search + "%'\n" +
				(purchasesInstance.getPurchases()[Purchases.ALL_VERBS] ? ""
						: "AND infinitiveG IN (" + purchasesInstance.getTrialVerbs() +")\n") +
				"UNION\n" +
				"SELECT infinitiveG, infinitiveE, favourite, pastParticiple, pastAuxiliary, thirdS AS 'simplePast', strength, type, 'en', infinitiveE AS 'keyInfinitive', length(infinitiveE)-3 AS 'keyLength'\n" +
				"FROM infinitive JOIN past USING(_id)\n" +
				"WHERE infinitiveE LIKE '%" + search + "%'\n" +
				(purchasesInstance.getPurchases()[Purchases.ALL_VERBS] ? ")"
						: "AND infinitiveG IN (" + purchasesInstance.getTrialVerbs() +"))\n") +
				"ORDER BY keyLength, keyInfinitive;";

		verbsDB = dbHelper.getWritableDatabase();
		return verbsDB.rawQuery(query, null);
	}

	//Method for selecting just favourite infinitives on the initial verb search
	public Cursor getFavourites() {

		String tableName = INF_TABLE + " JOIN " + PAST_TABLE + " USING(" + VERB_ID +")";
		String[] infColumns = new String[] {"InfinitiveG", "InfinitiveE", "favourite", "pastParticiple", "pastAuxiliary", "thirdS AS 'simplePast'", "strength", "type"};

		//Selects records in which the German Infinitive *contains* the word
		//rather than specifically matches is
		String whereClause = "favourite = 1";

		//Gets and returns the cursor representing the result
		return verbsDB.query(tableName, infColumns,
				whereClause, null, null, null, null);
	}

	//retrieves all the German columns from a specified tense for a specified verb
	public Cursor getVerbSet(int tense, String verb) {
		String tableName = BASE_TABLES[tense];
		final String[] BASE_COLUMNS = new String[] {FIRST_S, SECOND_S,
				THIRD_S, FIRST_P, SECOND_P, THIRD_P_SECOND_PF};

		String whereClause = VERB_ID + " = (SELECT " + VERB_ID + " FROM Infinitive" +
				" WHERE infinitiveG = '" + verb + "')";

		return verbsDB.query(tableName, BASE_COLUMNS,
				whereClause, null, null, null, null);
	}

	//Updates the favourite status of a verb
	public void setFavStatus(String verb, boolean favStatus) {
		ContentValues args = new ContentValues();
		args.put("favourite", favStatus);
		String whereClause = VERB_ID + " = (SELECT " + VERB_ID + " FROM Infinitive" +
				" WHERE infinitiveG = '" + verb + "')";

		verbsDB.update(INF_TABLE, args, whereClause, null);
	}


	//Returns the names of a tense in German and English given a tense ID
	public String[] getTense(int tenseID) {
		String tense[] = {GERMAN_TENSES[tenseID], ENGLISH_TENSES[tenseID]};

		return tense;
	}

	//returns the array of German pronouns
	public String[] getPronouns() {
		return PRONOUNS;
	}

	//Returns the number of "base" tables
	public int getBaseTableCount() {
		return BASE_TABLES.length;
	}
}