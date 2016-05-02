package com.instacoind.www.coind;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * Created by tbh643 on 5/2/2016.
 */
public class IndexSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = IndexSyncAdapter.class.getSimpleName();

    public static final int SYNC_INTERVAL = 60 * 5;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    // Coindesk JSON Parsing constants
    public static final String CD_BPI = "bpi";
    public static final String[] CD_FIAT = {"USD", "EUR", "CNY"};
    public static final String CD_RATE = "rate_float";

    // CEX JSON Parsing constants
    public static final String CEX_RATE = "bid";

    // Coinbase JSON Parsing constants
    public static final String CB_BPI = "data";
    public static final String CB_RATE = "amount";

    // OkCoin JSON Parsing constants
    public static final String OK_BPI = "";

    public IndexSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

    }
}
