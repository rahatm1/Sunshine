package com.sr.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sr.android.sunshine.app.data.WeatherContract;
import com.sr.android.sunshine.app.data.WeatherContract.WeatherEntry;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String DETAIL_URI = "DETAIL_URI";
    private Uri mUri;
    private static final int WEATHER_FOR_DATE_LOADER = 102;
    private ShareActionProvider mShareActionProvider;
    private String forecast;
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private static final String[] DETAIL_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_PRESSURE,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_DEGREES,
            WeatherEntry.COLUMN_WEATHER_ID
    };

    // these constants correspond to the projection defined above, and must change if the
    // projection changes
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;
    private static final int COL_WEATHER_HUMIDITY = 5;
    private static final int COL_WEATHER_PRESSURE = 6;
    private static final int COL_WEATHER_WIND_SPEED = 7;
    private static final int COL_WEATHER_DEGREES = 8;
    private static final int COL_WEATHER_CONDITION_ID = 9;

    private ImageView mIconView;
    private TextView mMonthDayView;
    private TextView mDayView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedBundleInstanceState) {
        super.onActivityCreated(savedBundleInstanceState);
        getLoaderManager().initLoader(WEATHER_FOR_DATE_LOADER, null, this);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_fragment, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (forecast != null) {
            mShareActionProvider.setShareIntent(sendForecastIntent());
        }
        else {
            Log.d(LOG_TAG, "Share action provider is null");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Bundle args = getArguments();
        if (args != null) {
            mUri = args.getParcelable(DETAIL_URI);
        }

        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mMonthDayView = (TextView) rootView.findViewById(R.id.month_date_textview);
        mDayView = (TextView) rootView.findViewById(R.id.day_name_textview);
        mDescriptionView = (TextView) rootView.findViewById(R.id.forecast_textview);
        mHighTempView = (TextView) rootView.findViewById(R.id.high_textview);
        mLowTempView = (TextView) rootView.findViewById(R.id.low_textview);
        mHumidityView = (TextView) rootView.findViewById(R.id.humidity_textview);
        mWindView = (TextView) rootView.findViewById(R.id.wind_textview);
        mPressureView = (TextView) rootView.findViewById(R.id.pressure_textview);

        return rootView;
    }

    public Intent sendForecastIntent() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, forecast + "#SunshineApp");
        sendIntent.setType("text/plain");
        return sendIntent;
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        if (mUri != null) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        else {
            return  null;
        }
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) { return; }

        int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
        // Use placeholder Image
        mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

        Long date = data.getLong(COL_WEATHER_DATE);

        String dateString = Utility.formatDate(date);

        String dayName = Utility.getDayName(getActivity(), date);
        mDayView.setText(dayName);

        String monthDay = Utility.getFormattedMonthDay(getActivity(), date);
        mMonthDayView.setText(monthDay);

        String weatherDescription =
                data.getString(COL_WEATHER_DESC);
        mDescriptionView.setText(weatherDescription);

        boolean isMetric = Utility.isMetric(getActivity());

        String high = Utility.formatTemperature(
                getActivity(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        mHighTempView.setText(high);

        String low = Utility.formatTemperature(
                getActivity(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
        mLowTempView.setText(low);

        float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
        mHumidityView.setText(getString(R.string.format_humidity, humidity));

        float pressure = data.getFloat(COL_WEATHER_PRESSURE);
        mPressureView.setText(getString(R.string.format_pressure, pressure));


        String wind = Utility.getFormattedWind(
                getActivity(),
                data.getFloat(COL_WEATHER_WIND_SPEED),
                data.getFloat(COL_WEATHER_DEGREES)
        );

        mWindView.setText(wind);

        forecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);
        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(sendForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(WEATHER_FOR_DATE_LOADER, null, this);
        }
    }
}

