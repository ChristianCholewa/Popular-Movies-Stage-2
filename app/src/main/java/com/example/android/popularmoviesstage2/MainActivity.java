package com.example.android.popularmoviesstage2;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmoviesstage2.data.MovieData;
import com.example.android.popularmoviesstage2.database.FavoriteDatabase;
import com.example.android.popularmoviesstage2.database.FavoriteEntry;
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

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mJSonDataAdapter;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorMessage;
    private TextView mNoFavorites;

    private FavoriteDatabase mDatabase;
    private FavoriteAdapter mDatabaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get views
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);
        mRecyclerView = findViewById(R.id.rv_posters);
        mErrorMessage = findViewById(R.id.tv_error_message_display);
        mNoFavorites = findViewById(R.id.tv_no_favorites);

        // get preferences
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        String api_key = sharedPrefs.getString(
                getString(R.string.settings_api_key_key),
                "");

        String selectedMode  = sharedPrefs.getString(
                getString(R.string.settings_search_key),
                getString(R.string.settings_search_most_popular_value)
        );

        // set the app title
        String title;
        if(selectedMode.equals(getString(R.string.settings_search_most_popular_value))){
            title = getString(R.string.app_title_popular);
        } else if (selectedMode.equals(getString(R.string.settings_search_highest_rated_value))){
            title = getString(R.string.app_title_highest);
        } else {
            title = getString(R.string.app_title_favorites);
        }

        setTitle(title);

        // set up the RecyclerView layout
        if(selectedMode.equals(getString(R.string.settings_search_favorites_value))) {
            // 1) layout for favorites
            // room database
            mDatabase = FavoriteDatabase.getInstance(getApplicationContext());

            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Initialize the adapter and attach it to the RecyclerView
            mDatabaseAdapter = new FavoriteAdapter(this);
            mRecyclerView.setAdapter(mDatabaseAdapter);

            mLoadingIndicator.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);

            setupViewModel();

            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                // Called when a user swipes left or right on a ViewHolder
                @Override
                public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {

                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mLoadingIndicator.setVisibility(View.VISIBLE);
                                    mRecyclerView.setVisibility(View.INVISIBLE);
                                }
                            });

                            int pos = viewHolder.getAdapterPosition();
                            FavoriteEntry favoriteEntry = mDatabaseAdapter.getFavorites().get(pos);

                            mDatabase.favoriteDao().deleteFavorite(favoriteEntry);
                        }
                    });
                }
            }).attachToRecyclerView(mRecyclerView);
            
        } else {
            // 2) layout for moviedb results

            DisplayMetrics metrics = getResources().getDisplayMetrics();

            int displayWidth = metrics.widthPixels;
            int columnCount = displayWidth / W342_BITMAPWIDTH;
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, columnCount));

            // initialize the loader
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.BUNDLE_API_KEY), api_key);
            bundle.putString(getString(R.string.BUNDLE_ORDER_BY), selectedMode);

            LoaderManager loaderManager = getSupportLoaderManager();
            Loader<Object> dataLoader = loaderManager.getLoader(MAIN_LOADER_ID);

            if (dataLoader == null) {
                loaderManager.initLoader(MAIN_LOADER_ID, bundle, MainActivity.this);
            } else {
                loaderManager.restartLoader(MAIN_LOADER_ID, bundle, MainActivity.this);
            }
        }
    }

    // viewmodel for favorites
    private void setupViewModel(){

        MainViewModel mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.getFavorites().observe(this, new Observer<List<FavoriteEntry>>() {
            @Override
            public void onChanged(@Nullable List<FavoriteEntry> favoriteEntries) {
                mDatabaseAdapter.setFavorites(favoriteEntries);
                final int favoriteCount = favoriteEntries.size();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoadingIndicator.setVisibility(View.INVISIBLE);

                        mRecyclerView.setVisibility(favoriteCount > 0 ? View.VISIBLE : View.INVISIBLE);
                        mNoFavorites.setVisibility(favoriteCount > 0 ? View.INVISIBLE : View.VISIBLE);
                    }
                });
            }
        });
    }

    // item click handling, details
    @Override
    public void onItemClick(View view, int position) {
        MovieData clickedMovie = mJSonDataAdapter.getItem(position);

        launchDetailActivity(clickedMovie);
    }

    private void launchDetailActivity(MovieData clickedMovie) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(MovieData.EXTRA_NAME_MOVIEDATA, clickedMovie);
        startActivity(intent);
    }

    // loader for moviedb access
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

                String api_key = args.getString(getString(R.string.BUNDLE_API_KEY));
                String orderBy  = args.getString(getString(R.string.BUNDLE_ORDER_BY));

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
            mJSonDataAdapter = new RecyclerViewAdapter(MainActivity.this, movieArray);
            mJSonDataAdapter.setClickListener(MainActivity.this);
            mRecyclerView.setAdapter(mJSonDataAdapter);
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

// todo keine favoriten meldung