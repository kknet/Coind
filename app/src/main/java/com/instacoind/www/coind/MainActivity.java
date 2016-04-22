package com.instacoind.www.coind;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Double mBitcoinPrice = 0.0;
    public Double getmBitcoinPrice(){
        return mBitcoinPrice;
    }
    public void setmBitcoinPrice(Double price){
        this.mBitcoinPrice = price;
    }
    private Double mBitcoinPriceOld = 0.0;
    public Double getmBitcoinPriceOld(){
        return mBitcoinPriceOld;
    }
    public void setmBitcoinPriceOld(Double price){
        this.mBitcoinPriceOld = price;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBitcoinPrice != 0.0) {
                    Snackbar.make(view, "Last Change: " + priceChange(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }else{
                    Snackbar.make(view, "SYNC FAILED ERROR::01", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent start_settings = new Intent(this, SettingsActivity.class);
            startActivity(start_settings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart(){
        super.onStart();
        updatePrice();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void updatePrice(){
        GetBitcoinPrice worker = new GetBitcoinPrice();
        worker.execute(getPrefFiat(this), getPrefPriceIndex(this));
    }
    public static String getPrefFiat(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_currency_key),
                context.getString(R.string.pref_currency_def));
    }
    public static String getPrefPriceIndex(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_source_key),
                context.getString(R.string.pref_source_default));
    }
    public String priceChange(){
        Double past = getmBitcoinPriceOld();
        Double pres = getmBitcoinPrice();
        Double change = 0.0;
        String fin = "price_change";
        if(past != null){
            if(pres >= past){
                change = pres - past;
                fin = "+ " + Double.toString(change);
            }else if(pres <= past){
                change = past - pres;
                fin = "- " + Double.toString(change);
            }
        }else{
            setmBitcoinPriceOld(getmBitcoinPrice());
        }
        return fin;
    }
    public void refreshPriceDisplays(){
        TextView PriceDisplay = (TextView) findViewById(R.id.current_price_display);
        String error = "No Bitcoin Price";
        try {
            Log.v("RefreshPriceDisplays", "mBitcoinPrice: " + Double.toString(getmBitcoinPrice()));
            PriceDisplay.setText(Double.toString(getmBitcoinPrice()));
        }catch(NullPointerException e) {
            Log.v("RefreshPriceDisplays", "mBitcoinPrice NULL");
            PriceDisplay.setText("ERR");
        }
    }


    private class GetBitcoinPrice extends AsyncTask<String, Void, Double> {

        public String mPrefFiat;
        public String mPrefIndex;

        protected Double doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String bitcoinJsonStr = null;
            this.mPrefFiat = params[0];
            this.mPrefIndex = params[1];



            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL(buildUrl(this.mPrefFiat,this.mPrefIndex));

                // Create the request to Index Source, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    bitcoinJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    bitcoinJsonStr = null;
                }
                bitcoinJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("GetBitcoinPrice", "Error ", e);
                // If the code didn't successfully get the price index data, there's no point in attemping
                // to parse it.
                bitcoinJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("GetBitcoinPrice", "Error closing stream", e);
                    }
                }
            }

            switch(this.mPrefIndex){

                case "https://api.coindesk.com/v1/bpi/currentprice.json":
                    return parseCoindesk(bitcoinJsonStr);
                case "https://api.coinbase.com/v2/prices/spot?currency=":
                    return parseCoinbase(bitcoinJsonStr);
                case "https://www.okcoin.com/api/v1/ticker.do?symbol=":
                    return parseOkcoin(bitcoinJsonStr);
                case "https://cex.io/api/ticker/BTC/":
                    return parseCex(bitcoinJsonStr);
                default:
                    return 0.0;
            }

        }
        private Double parseCex(String bitcoinJsonStr){
            Double price = 0.0;

            try {
                JSONObject obj = new JSONObject(bitcoinJsonStr);
                price = obj.getDouble("bid");
                Log.v("ConvertPriceStr", "Current Price is " + Double.toString(price));
                if (price == null){
                    Log.e("CEXParsing", "Parsed to Null");
                }

            }catch (JSONException x){
                Log.e("ConvertPriceStr", "Error Parsing bitcoinJsonStr");
            }
            return price;
        }
        private Double parseCoindesk(String bitcoinJsonStr){
            Double price = 0.0;

            try {
                JSONObject obj = new JSONObject(bitcoinJsonStr);
                JSONObject bpi = obj.getJSONObject("bpi");
                JSONObject usd = bpi.getJSONObject("USD");
                price = usd.getDouble("rate_float");
                Log.v("ConvertPriceStr", "Current Price is " + Double.toString(price));

            }catch (JSONException x){
                Log.e("ConvertPriceStr", "Error Parsing bitcoinJsonStr");
            }
            return price;
        }
        private Double parseCoinbase(String bitcoinJsonStr){
            Double price = 0.0;
            try{
                JSONObject obj = new JSONObject(bitcoinJsonStr);
                JSONObject bpi = obj.getJSONObject("data");
                price = bpi.getDouble("amount");
                Log.v("ParseCoinbase", "Current Price is " + Double.toString(price));
            }catch(JSONException e){
                Log.e("ParseCoinbase", "Error Parsing bitcoinJsonStr");
            }
            return price;
        }
        private Double parseOkcoin(String bitcoinJsonStr){
            Double price = 0.0;
            try{
                JSONObject obj = new JSONObject(bitcoinJsonStr);
                JSONObject bpi = obj.getJSONObject("ticker");
                price = bpi.getDouble("buy");
                Log.v("ParseOkCoin", "Current Price is " + Double.toString(price));
            }catch(JSONException e){
                Log.e("ParseOkCoin", "Error Parsing bitcoinJsonStr");
            }
            return price;
        }
        private String buildUrl(String prefFiat, String prefIndex){
            String url = null;
            // TODO: change to clean looking switch(case) statement and make this an actual uri builder
            // unfortunately at the current time I haven't looked at the documentation so I'm just going
            // to combine strings.
            try {
                if (prefIndex == "https://api.coinbase.com/v2/prices/spot?currency=") {
                    url = prefIndex + prefFiat;
                } else if (prefIndex == "https://api.coindesk.com/v1/bpi/currentprice.json") {
                    url = prefIndex + prefFiat;
                } else if (prefIndex == "https://www.okcoin.com/api/v1/ticker.do?symbol=") {
                    String modFiat;
                    modFiat = "btc_" + prefFiat;
                    url = prefIndex + modFiat;
                } else if (prefIndex == "https://cex.io/api/ticker/BTC/") {
                    String modFiat;
                    modFiat = prefIndex + prefFiat;
                    url = modFiat;
                } else {
                    url = "https://api.coindesk.com/v1/bpi/currentprice.json";
                }
            }catch(NullPointerException e){
                Log.e("URL Builder", "Error Constructing Proper URL", e);
            }
            return url;
        }

        protected void onPostExecute(Double result){
            setmBitcoinPriceOld(getmBitcoinPrice());
            setmBitcoinPrice(result);
            refreshPriceDisplays();
        }
    }
}
