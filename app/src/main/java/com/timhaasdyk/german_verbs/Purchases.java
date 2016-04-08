package com.timhaasdyk.german_verbs;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.timhaasdyk.german_verbs.util.IabHelper;
import com.timhaasdyk.german_verbs.util.IabResult;
import com.timhaasdyk.german_verbs.util.Inventory;
import com.timhaasdyk.german_verbs.util.Purchase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by timh on 03.08.15.
 *
 * This class is a singleton
 */
public class Purchases {

    private IabHelper mHelper;

    public static final int REMOVE_ADS = 0;
    public static final int ALL_VERBS = 1;
    //public static final int ALL_TENSES_AND_ENGLISH = 2;
    private String trialVerbs;
    private Boolean[] purchases = new Boolean[2];

    private static Purchases instance = null;

    private Context myContext;
    private Activity myActivity;
    private View callerView;

    private static final String REMOVE_ADS_ID = "remove_adds";
    private static final String ALL_VERBS_ID = "all_verbs";

    private List productIds;

    public Boolean dialogReady = false;
    private final int REQUEST_CODE = 5432123;

    private Purchases (Activity activity, Context context) {

        purchases[REMOVE_ADS] = false;
        purchases[ALL_VERBS] = false;

        productIds = new ArrayList();
        productIds.add(REMOVE_ADS_ID);
        productIds.add(ALL_VERBS_ID);

        this.myContext = context;
        this.myActivity = activity;
        setTrialVerbs(myContext.getResources());
        makeIabHelper();
    }

    public static Purchases getInstance(Activity activity, Context context) {
        if (instance == null) {
            instance = new Purchases(activity, context);
        }

        return instance;
    }

    public Boolean[] getPurchases () {
        return purchases;
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener;
    private Boolean gotInventoryListenerMade = false;
    private void updatePurchases() {

        if (!gotInventoryListenerMade)
            makeGotInventoryListener();

        mHelper.queryInventoryAsync(mGotInventoryListener);
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener;
    private Boolean purchaseListenerMade = false;

    public void makePurchase(final int PROD_I) {

        if (!purchaseListenerMade)
            makePurchaseListener();

        mHelper.launchPurchaseFlow((Activity)myContext, (String) productIds.get(PROD_I),
                REQUEST_CODE, mPurchaseFinishedListener, "PAYLOAD");
    }


    private void setTrialVerbs(Resources resources) {

        StringBuilder builder = new StringBuilder();
        InputStream in = null;

        try {
            in = resources.getAssets().open("500common.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while((line = reader.readLine()) != null)
                builder.append(line);

            trialVerbs = builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    IabHelper.QueryInventoryFinishedListener mQueryFinishedListener;
    private Boolean productQueryListenerMade = false;
    public void addPricesToView(View view) {

        if (!productQueryListenerMade)
            makeProductQueryListener();

        this.callerView = view;
        mHelper.queryInventoryAsync(true, productIds,
                mQueryFinishedListener);
    }

    public String getTrialVerbs() {
        return trialVerbs;
    }

    private void makeIabHelper() {
        StringBuilder base64EncodedPublicKey = new StringBuilder();

        base64EncodedPublicKey.append("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA");
        base64EncodedPublicKey.append("kpo9zEVAD919YYnwS1ZLYKRKSnb8CnhzPnpJSDesBgrG");
        base64EncodedPublicKey.append("PRwzjdB9ijjFXfa7KfutTeSK/RzYT80zP4/Sw9WE3Djf");
        base64EncodedPublicKey.append("8GbjGo+GYxlmwJD2D4s8smCfHN2vATony8yo7O4oWW4j");
        base64EncodedPublicKey.append("/f/m85VWd3vd+bbRUBGNyryabF6aFiveufrjKJoYvx0K");
        base64EncodedPublicKey.append("80PAc8Pb6uJpp1riiXTXJ+KygjXTtoBrLfLKSzXzBnYF");
        base64EncodedPublicKey.append("kdhWlZmRwSyzeZgktsaMHYRYTyWJsGt/l7Py6rEMwDsF");
        base64EncodedPublicKey.append("h0llJ0qAVVAefPJ2db5XgvCIkPNnx++WfIGIgpNqk2p/");
        base64EncodedPublicKey.append("jEto+k3BrkurChv+Wl1hJx+7pKrGyVFPKwIDAQAB");

        // compute your public key and store it in base64EncodedPublicKey
        mHelper = new IabHelper(myContext, base64EncodedPublicKey.toString());

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.d("Error", "Problem setting up In-app Billing: " + result);
                }
                // Hooray, IAB is fully set up!

                updatePurchases();
            }
        });
    }

    private void makeGotInventoryListener() {
        mGotInventoryListener
                = new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result,
                                                 Inventory inventory) {

                if (result.isFailure()) {
                    // handle error here
                }
                else {
                    //Toast.makeText(myContext,"consuming", Toast.LENGTH_SHORT).show();
                    //mHelper.consumeAsync(inventory.getPurchase((String) productIds.get(REMOVE_ADS)), null);
                    //mHelper.consumeAsync(inventory.getPurchase((String)productIds.get(ALL_VERBS)), null);
                    purchases[REMOVE_ADS] = inventory.hasPurchase((String)productIds.get(REMOVE_ADS));
                    purchases[ALL_VERBS] = inventory.hasPurchase((String)productIds.get(ALL_VERBS));

                    ((MainActivity)myContext).checkAds();
                }
            }
        };
    }

    private void makePurchaseListener() {
        mPurchaseFinishedListener
                = new IabHelper.OnIabPurchaseFinishedListener() {
            public void onIabPurchaseFinished(IabResult result, Purchase purchase)
            {
                if (result.isFailure()) {
                    Log.d("Purchase-Listener", "Error purchasing: " + result);
                }
                else if (purchase.getSku().equals(productIds.get(REMOVE_ADS))) {
                    purchases[REMOVE_ADS] = true;
                    ((MainActivity) myContext).checkAds();
                }
                else if (purchase.getSku().equals(productIds.get(ALL_VERBS))) {
                    purchases[ALL_VERBS] = true;
                }

                updateSwitches();
            }
        };

        purchaseListenerMade = true;
    }

    private void updateSwitches() {

        Switch swcVerbs = (Switch) callerView.findViewById(R.id.swtAllVerbs);
        Switch swcAdds = (Switch) callerView.findViewById(R.id.swtRemoveAds);

        swcVerbs.setChecked(purchases[ALL_VERBS]);
        swcVerbs.setEnabled(!purchases[ALL_VERBS]);

        swcAdds.setChecked(purchases[REMOVE_ADS]);
        swcAdds.setEnabled(!purchases[REMOVE_ADS]);
    }

    private void makeProductQueryListener() {
                mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result, Inventory inventory)
            {
                if (result.isFailure()) {
                    // handle error
                    return;
                }

                String removeAdsPrice =
                        inventory.getSkuDetails(REMOVE_ADS_ID).getPrice();
                String allVerbsPrice =
                        inventory.getSkuDetails(ALL_VERBS_ID).getPrice();

                String removeAdsDescription =
                        inventory.getSkuDetails(REMOVE_ADS_ID).getDescription();
                String allVerbsDescription =
                        inventory.getSkuDetails(ALL_VERBS_ID).getDescription();

                // update the UI
                //Labels
                ((TextView) callerView.findViewById(R.id.txtRemoveAdsDescription)).setText(removeAdsDescription);
                ((TextView) callerView.findViewById(R.id.txtAllVerbsDescription)).setText(allVerbsDescription);
                //Switches
                ((Switch) callerView.findViewById(R.id.swtRemoveAds)).setTextOff(removeAdsPrice);
                ((Switch) callerView.findViewById(R.id.swtAllVerbs)).setTextOff(allVerbsPrice);
                ((Switch) callerView.findViewById(R.id.swtRemoveAds)).refreshDrawableState();
                ((Switch) callerView.findViewById(R.id.swtAllVerbs)).refreshDrawableState();
                ((Switch) callerView.findViewById(R.id.swtRemoveAds)).requestLayout();
                ((Switch) callerView.findViewById(R.id.swtAllVerbs)).requestLayout();

                dialogReady = true;
            }
        };

        productQueryListenerMade = true;
    }

    public void destroy() {
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
        instance = null;
        gotInventoryListenerMade = false;
        productQueryListenerMade = false;
        purchaseListenerMade = false;
    }

    /*public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mHelper == null)
            return;

        if (resultCode == Activity.RESULT_CANCELED) {
            updateSwitches();
        }
    }*/

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        return mHelper.handleActivityResult(requestCode, resultCode, data);
    }
}