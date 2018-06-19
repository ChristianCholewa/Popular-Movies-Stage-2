package com.example.android.popularmoviesstage1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.example.android.popularmoviesstage1.data.MovieData;
import com.example.android.popularmoviesstage1.utilities.JSONUtils;
import com.example.android.popularmoviesstage1.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RecyclerViewAdapter.ItemClickListener {

    private static final int W342_BITMAPWIDTH = 342;

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

        //asynctask
        DataFetcher dataFetcher = new DataFetcher();
        dataFetcher.execute();

        // set up the RecyclerView
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        int displayWidth = metrics.widthPixels;
        int columnCount = displayWidth / W342_BITMAPWIDTH;
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, columnCount));
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

    // data loading
    class DataFetcher extends AsyncTask<URL, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String doInBackground(URL... urls) {

            //api key and sorting
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

            String api_key = sharedPrefs.getString(
                    getString(R.string.settings_api_key_key),
                    "");

            String orderBy  = sharedPrefs.getString(
                    getString(R.string.settings_search_key),
                    getString(R.string.settings_search_most_popular_value)
            );

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

        @Override
        protected void onPostExecute(String jsonString) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);

            if(!TextUtils.isEmpty(jsonString)){
                mRecyclerView.setVisibility(View.VISIBLE);
                List<MovieData> mMovieArray = JSONUtils.ParseOverview(jsonString);
                mAdapter = new RecyclerViewAdapter(MainActivity.this, mMovieArray);
                mAdapter.setClickListener(MainActivity.this);
                mRecyclerView.setAdapter(mAdapter);
            } else {
                mErrorMessage.setVisibility(View.VISIBLE);
            }
        }
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
