package com.instacoind.www.coind;

import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

/**
 * Created by tbh643 on 5/2/2016.
 */
public class IndexContract{
    // The content authority is the name for the entire content provider
    public static final String CONTENT_AUTHORITY = "com.instacoind.www.coind";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PRICES = "prices";
    public static final String PATH_HOLDINGS = "holdings";
}
