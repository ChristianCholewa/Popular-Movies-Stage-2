package com.example.android.popularmoviesstage2;

import android.content.ActivityNotFoundException;
import android.content.Context;
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

    private static final String EXTRA_MOVIE_ID = "extra_movie_id";
    private static final String EXTRA_API_KEY = "api_key";

    private static boolean isFavorite;

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

        MovieData data = intent.getParcelableExtra(MovieData.EXTRA_NAME_MOVIEDATA);

        int movieId = data.getId();

        InitializeDetailsSection(data);

        final Context context = getApplicationContext();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        String api_key = sharedPrefs.getString(
                getString(R.string.settings_api_key_key),
                "");

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_API_KEY, api_key);
        bundle.putInt(EXTRA_MOVIE_ID, movieId);

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

        // TODO database
        isFavorite = false;

        imageViewFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO database
                isFavorite = !isFavorite;

                if(isFavorite){
                    imageViewFavorite.setImageDrawable(getDrawable(R.drawable.baseline_favorite_black_48));
                    Toast.makeText(DetailActivity.this, context.getString(R.string.add_to_favorites), Toast.LENGTH_SHORT).show();
                } else {
                    imageViewFavorite.setImageDrawable(getDrawable(R.drawable.baseline_favorite_border_black_48));
                    Toast.makeText(DetailActivity.this, context.getString(R.string.remove_from_favorites), Toast.LENGTH_SHORT).show();
                }
            }
        });
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

                int movieId = args.getInt(EXTRA_MOVIE_ID);
                String api_key = args.getString(EXTRA_API_KEY);

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

            if(movieTrailers.size() > 0) {
                for (int i = 0; i < movieTrailers.size(); i++) {
                    View view = layoutInflater.inflate(R.layout.trailer_item, linearLayoutTrailers, false);

                    //TODO site
                    TextView textViewName = view.findViewById(R.id.tv_trailer_name);
                    textViewName.setText(movieTrailers.get(i).getName());
                    TextView textViewType = view.findViewById(R.id.tv_trailer_type);
                    textViewType.setText(movieTrailers.get(i).getType());
                    TextView textViewSite = view.findViewById(R.id.tv_trailer_site);
                    textViewSite.setText(movieTrailers.get(i).getSite());

                    view.setTag(movieTrailers.get(i));
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MovieTrailer trailer = (MovieTrailer)v.getTag();
                            PlayTrailer(trailer);
                        }
                    });

                    linearLayoutTrailers.addView(view);
                }
            } else {
                View view = layoutInflater.inflate(R.layout.trailer_no_data, linearLayoutReviews, false);
                linearLayoutReviews.addView(view);
            }
        }
        else {
            View view = layoutInflater.inflate(R.layout.trailer_load_error, linearLayoutReviews, false);
            linearLayoutReviews.addView(view);
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
//TODO site
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
