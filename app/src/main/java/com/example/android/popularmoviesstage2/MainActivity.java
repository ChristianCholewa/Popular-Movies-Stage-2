package com.example.android.popularmoviesstage2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmoviesstage2.R;
import com.example.android.popularmoviesstage2.data.MovieData;
import com.example.android.popularmoviesstage2.utilities.JSONUtils;
import com.example.android.popularmoviesstage2.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        RecyclerViewAdapter.ItemClickListener,
        LoaderManager.LoaderCallbacks<String>{

    private static final int W342_BITMAPWIDTH = 342;

    private static final int MAIN_LOADER_ID = 1;
    private static final String EXTRA_API_KEY = "api_key";
    private static final String EXTRA_ORDER_BY = "order_by";

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);
        mRecyclerView = findViewById(R.id.rv_posters);
        mErrorMessage = findViewById(R.id.tv_error_message_display);

        // set up the RecyclerView
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        int displayWidth = metrics.widthPixels;
        int columnCount = displayWidth / W342_BITMAPWIDTH;
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, columnCount));

        // set up the loader
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        String api_key = sharedPrefs.getString(
                getString(R.string.settings_api_key_key),
                "");

        String orderBy  = sharedPrefs.getString(
                getString(R.string.settings_search_key),
                getString(R.string.settings_search_most_popular_value)
        );

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_API_KEY, api_key);
        bundle.putString(EXTRA_ORDER_BY, orderBy);

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<Object> dataLoader = loaderManager.getLoader(MAIN_LOADER_ID);

        if(dataLoader == null){
            loaderManager.initLoader(MAIN_LOADER_ID, bundle, MainActivity.this);
        } else{
            loaderManager.restartLoader(MAIN_LOADER_ID, bundle, MainActivity.this);
        }
    }

    // item click handling, details
    @Override
    public void onItemClick(View view, int position) {
        MovieData clickedMovie = mAdapter.getItem(position);

        launchDetailActivity(clickedMovie);
    }

    private void launchDetailActivity(MovieData clickedMovie) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(MovieData.EXTRA_NAME_MOVIEDATA, clickedMovie);
        startActivity(intent);
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if(args == null){
                    return;
                }

                mLoadingIndicator.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.INVISIBLE);

                forceLoad();
            }

            @Override
            public String loadInBackground() {

                String api_key = args.getString(EXTRA_API_KEY);
                String orderBy  = args.getString(EXTRA_ORDER_BY);

                URL url = NetworkUtils.buildUrl(api_key, orderBy);
                String jsonString = "";
                try {
                    jsonString = NetworkUtils.getResponseFromHttpUrl(url);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }

                return jsonString;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {

        mLoadingIndicator.setVisibility(View.INVISIBLE);

        if(!TextUtils.isEmpty(data)){
            mRecyclerView.setVisibility(View.VISIBLE);
            List<MovieData> movieArray = JSONUtils.ParseOverview(data);
            mAdapter = new RecyclerViewAdapter(MainActivity.this, movieArray);
            mAdapter.setClickListener(MainActivity.this);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mErrorMessage.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    // settings menu
    @Override
    // This method initialize the contents of the Activity's options menu.
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the Options Menu we specified in XML
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
