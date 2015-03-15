package com.sr.android.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private String mLocation;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocation = Utility.getPreferredLocation(this);

        Log.v(LOG_TAG, "onCreate");

        setContentView(R.layout.activity_main);
        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw400dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        ForecastFragment forecastFragment =  ((ForecastFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast));
        forecastFragment.setUseTodayLayout(!mTwoPane);

        //XXX: Logs view size
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        Log.v(LOG_TAG, "mTwoPane:" + mTwoPane);
        Log.v(LOG_TAG, dpHeight +"x" + dpWidth);

    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            DetailFragment detailFragment = new DetailFragment();

            // Supply index input as an argument.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);
            detailFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, detailFragment, DETAILFRAGMENT_TAG)
                    .commit();
        }
        else {
            startActivity(new Intent(this, DetailActivity.class)
                    .setData(contentUri));
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
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
            return true;
        }

        if (id == R.id.action_map) {

            Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
            .appendQueryParameter("q", mLocation)
            .build();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(geoLocation);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }

            return true;
        }

            return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v(LOG_TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation( this );
        // update the location in our second pane using the fragment manager
        if (location != null && !location.equals(mLocation)) {
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if ( null != ff ) {
                ff.onLocationChanged();
            }
            DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( null != df ) {
                df.onLocationChanged(location);
            }
            mLocation = location;
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.v(LOG_TAG, "onPause");
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.v(LOG_TAG, "onStop");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.v(LOG_TAG, "onDestroy");
    }
}

