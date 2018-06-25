package com.example.android.popularmoviesstage2;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmoviesstage2.data.MovieData;
import com.example.android.popularmoviesstage2.data.MovieReview;
import com.example.android.popularmoviesstage2.data.MovieTrailer;
import com.example.android.popularmoviesstage2.database.FavoriteDatabase;
import com.example.android.popularmoviesstage2.database.FavoriteEntry;
import com.example.android.popularmoviesstage2.utilities.JSONUtils;
import com.example.android.popularmoviesstage2.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class DetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<String> {

    private ImageView imageViewFavorite;

    private LinearLayout linearLayoutTrailers;
    private LinearLayout linearLayoutReviews;
    private ProgressBar loadingIndicatorTrailers;
    private ProgressBar loadingIndicatorReviews;
    private LayoutInflater layoutInflater;

    private static final int TRAILER_LOADER_ID = 2;
    private static final int REVIEW_LOADER_ID = 3;

    private static MovieData movieData;

    private static boolean isFavorite;
    private FavoriteDatabase mDatabase;
    private FavoriteEntry entry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        linearLayoutTrailers = findViewById(R.id.list_trailers);
        linearLayoutReviews = findViewById(R.id.list_reviews);
        loadingIndicatorTrailers = findViewById(R.id.pb_loading_indicator_trailers);
        loadingIndicatorReviews = findViewById(R.id.pb_loading_indicator_reviews);
        layoutInflater = LayoutInflater.from(DetailActivity.this);

        imageViewFavorite = findViewById(R.id.iv_favorite);

        Intent intent = getIntent();
        if (intent == null) {
            closeOnError();
        }

        movieData = intent.getParcelableExtra(MovieData.EXTRA_NAME_MOVIEDATA);

        final int movieId = movieData.getId();

        InitializeDetailsSection(movieData);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String api_key = sharedPrefs.getString(
                getString(R.string.settings_api_key_key),
                "");

        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.EXTRA_API_KEY), api_key);
        bundle.putInt(getString(R.string.EXTRA_MOVIE_ID), movieId);

        LoaderManager loaderManager = getSupportLoaderManager();

        Loader<Object> trailerLoader = loaderManager.getLoader(TRAILER_LOADER_ID);
        if(trailerLoader == null){
            loaderManager.initLoader(TRAILER_LOADER_ID, bundle, DetailActivity.this);
        } else{
            loaderManager.restartLoader(TRAILER_LOADER_ID, bundle, DetailActivity.this);
        }

        Loader<Object> reviewLoader = loaderManager.getLoader(REVIEW_LOADER_ID);
        if(reviewLoader == null){
            loaderManager.initLoader(REVIEW_LOADER_ID, bundle, DetailActivity.this);
        } else{
            loaderManager.restartLoader(REVIEW_LOADER_ID, bundle, DetailActivity.this);
        }

        mDatabase = FavoriteDatabase.getInstance(getApplicationContext());
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                entry = mDatabase.favoriteDao().getFavoriteByMovieId(movieData.getId());
                isFavorite = entry != null;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SetFavoriteIcon();
                    }
                });
            }
        });

        isFavorite = false;

        imageViewFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFavorite = !isFavorite;

                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {

                        if(isFavorite){
                            entry = new FavoriteEntry(movieData.getId(), movieData.getTitle(), movieData.getPoster_path());
                            mDatabase.favoriteDao().insertFavorite(entry);
                        } else {
                            mDatabase.favoriteDao().deleteFavorite(entry);
                            entry = null;
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(isFavorite){
                                    Toast.makeText(DetailActivity.this, getApplicationContext().getString(R.string.add_to_favorites), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(DetailActivity.this, getApplicationContext().getString(R.string.remove_from_favorites), Toast.LENGTH_SHORT).show();
                                }

                                SetFavoriteIcon();
                            }
                        });
                    }
                });

            }
        });
    }

    private void SetFavoriteIcon(){
        if(isFavorite){
            imageViewFavorite.setImageDrawable(getDrawable(R.drawable.baseline_favorite_black_48));
        } else {
            imageViewFavorite.setImageDrawable(getDrawable(R.drawable.baseline_favorite_border_black_48));
        }
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(final int id, final Bundle args) {

        return new AsyncTaskLoader<String>(this) {

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if(args == null){
                    return;
                }

                if(id == TRAILER_LOADER_ID) {
                    loadingIndicatorTrailers.setVisibility(View.VISIBLE);
                } else {
                    loadingIndicatorReviews.setVisibility(View.VISIBLE);
                }

                forceLoad();
            }

            @Override
            public String loadInBackground() {

                int movieId = args.getInt(getString(R.string.EXTRA_MOVIE_ID));
                String api_key = args.getString(getString(R.string.EXTRA_API_KEY));

                URL url;
                if(id == TRAILER_LOADER_ID) {
                    url = NetworkUtils.buildTrailerUrl(api_key, movieId);
                } else {
                    url = NetworkUtils.buildReviewUrl(api_key, movieId);
                }

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
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        int loaderId = loader.getId();

        if(loaderId == TRAILER_LOADER_ID) {
            InitializeTrailerSection(data);
        } else {
            InitializeReviewSection(data);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    private void InitializeDetailsSection(MovieData data){
        String title = data.getTitle();
        String release_date = data.getRelease_date();
        String poster_path = data.getPoster_path();
        double vote_average = data.getVote_average();
        String overview = data.getOverview();

        ImageView imageView = findViewById(R.id.iv_poster);
        URL posterUrl = NetworkUtils.buildPosterUrlString(poster_path);
        Picasso.with(this).load(posterUrl.toString()).into(imageView);

        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText(title);

        TextView tv_average = findViewById(R.id.tv_average);
        tv_average.setText(Double.toString(vote_average));

        TextView tv_release = findViewById(R.id.tv_release);
        tv_release.setText(release_date);

        TextView tv_overview = findViewById(R.id.tv_overview);
        tv_overview.setText(overview);
    }

    private void InitializeTrailerSection(String jsonString){
        loadingIndicatorTrailers.setVisibility(View.GONE);

        if(!TextUtils.isEmpty(jsonString)){

            List<MovieTrailer> movieTrailers = JSONUtils.ParseTrailers(jsonString);

            int displayTrailers = 0;
            if(movieTrailers.size() > 0) {
                for (int i = 0; i < movieTrailers.size(); i++) {

                    String site = movieTrailers.get(i).getSite();
                    String type = movieTrailers.get(i).getType();

                    if(site.equalsIgnoreCase("youtube") && type.equalsIgnoreCase("trailer")) {
                        View view = layoutInflater.inflate(R.layout.trailer_item, linearLayoutTrailers, false);

                        TextView textViewName = view.findViewById(R.id.tv_trailer_name);
                        textViewName.setText(movieTrailers.get(i).getName());
                        TextView textViewType = view.findViewById(R.id.tv_trailer_type);
                        String hostInfo = movieTrailers.get(i).getType() + " (" + movieTrailers.get(i).getSite() + ")";
                        textViewType.setText(hostInfo);

                        view.setTag(movieTrailers.get(i));
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MovieTrailer trailer = (MovieTrailer) v.getTag();
                                PlayTrailer(trailer);
                            }
                        });

                        linearLayoutTrailers.addView(view);
                        displayTrailers++;
                    }
                }
            }
            if(displayTrailers == 0){
                View view = layoutInflater.inflate(R.layout.trailer_no_data, linearLayoutReviews, false);
                linearLayoutTrailers.addView(view);
            }
        }
        else {
            View view = layoutInflater.inflate(R.layout.trailer_load_error, linearLayoutReviews, false);
            linearLayoutTrailers.addView(view);
        }
    }

    private void InitializeReviewSection(String jsonString){
        loadingIndicatorReviews.setVisibility(View.GONE);

        if(!TextUtils.isEmpty(jsonString)){

            List<MovieReview> movieReviews = JSONUtils.ParseReviews(jsonString);

            if(movieReviews.size() > 0) {
                for (int i = 0; i < movieReviews.size(); i++) {
                    View view = layoutInflater.inflate(R.layout.review_item, linearLayoutReviews, false);

                    TextView textViewName = view.findViewById(R.id.tv_author);
                    textViewName.setText(movieReviews.get(i).getAuthor());
                    TextView textViewType = view.findViewById(R.id.tv_content);
                    textViewType.setText(movieReviews.get(i).getContent());

                    view.setTag(movieReviews.get(i));
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MovieReview review = (MovieReview)v.getTag();
                            ShowReview(review);
                        }
                    });

                    linearLayoutReviews.addView(view);
                }
            } else{
                View view = layoutInflater.inflate(R.layout.review_no_data, linearLayoutReviews, false);
                linearLayoutReviews.addView(view);
            }
        }
        else {
            View view = layoutInflater.inflate(R.layout.review_load_error, linearLayoutReviews, false);
            linearLayoutReviews.addView(view);
        }
    }

    private void PlayTrailer(MovieTrailer trailer){
        String key = trailer.getKey();

        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + key));
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + key));

        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }

    private void ShowReview(MovieReview review){
        String url = review.getUrl();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.detail_error_message, Toast.LENGTH_SHORT).show();
    }
}
