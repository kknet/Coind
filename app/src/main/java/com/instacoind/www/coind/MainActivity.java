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
    private Double mBitcoinPriceOld = 0.0;

    public Double getmBitcoinPrice(){
        return mBitcoinPrice;
    }
    public void setmBitcoinPrice(Double price){
        this.mBitcoinPrice = price;
    }

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
        //TODO: currently implementing syncadapter and sqlite database persistent storage of
        // price index data based upon pref index(s)

        //GetBitcoinPrice worker = new GetBitcoinPrice();
        //worker.execute(getPrefFiat(this), getPrefPriceIndex(this));
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



}
