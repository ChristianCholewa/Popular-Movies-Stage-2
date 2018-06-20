package com.example.android.popularmoviesstage2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmoviesstage2.R;
import com.example.android.popularmoviesstage2.data.MovieData;
import com.example.android.popularmoviesstage2.data.MovieReview;
import com.example.android.popularmoviesstage2.data.MovieTrailer;
import com.example.android.popularmoviesstage2.utilities.JSONUtils;
import com.example.android.popularmoviesstage2.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        if (intent == null) {
            closeOnError();
        }

        MovieData data = intent.getParcelableExtra(MovieData.EXTRA_NAME_MOVIEDATA);

        int id = data.getId();
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

        //asynctask
        TrailerDataFetcher trailerDataFetcher = new TrailerDataFetcher();
        trailerDataFetcher.execute(id);

        ReviewDataFetcher reviewDataFetcher = new ReviewDataFetcher();
        reviewDataFetcher.execute(id);
    }

    // data loading
    class TrailerDataFetcher extends AsyncTask<Integer, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            mLoadingIndicator.setVisibility(View.VISIBLE);
//            mRecyclerView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String doInBackground(Integer... ids) {

            int movieId = ids[0];

            //api key and sorting
            Context context = getApplicationContext();
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            String api_key = sharedPrefs.getString(
                    getString(R.string.settings_api_key_key),
                    "");

            URL url = NetworkUtils.buildTrailerUrl(api_key, movieId);
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
//            mLoadingIndicator.setVisibility(View.INVISIBLE);

            if(!TextUtils.isEmpty(jsonString)){
//                mRecyclerView.setVisibility(View.VISIBLE);
                List<MovieTrailer> movieTrailers = JSONUtils.ParseTrailers(jsonString);
//                mAdapter = new RecyclerViewAdapter(MainActivity.this, mMovieArray);
//                mAdapter.setClickListener(MainActivity.this);
//                mRecyclerView.setAdapter(mAdapter);
                for(int i = 0; i < movieTrailers.size(); i++){

                }
            }
//            else {
//                mErrorMessage.setVisibility(View.VISIBLE);
//            }
        }
    }

    class ReviewDataFetcher extends AsyncTask<Integer, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            mLoadingIndicator.setVisibility(View.VISIBLE);
//            mRecyclerView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String doInBackground(Integer... ids) {

            int movieId = ids[0];

            //api key and sorting
            Context context = getApplicationContext();
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            String api_key = sharedPrefs.getString(
                    getString(R.string.settings_api_key_key),
                    "");

            URL url = NetworkUtils.buildReviewUrl(api_key, movieId);
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
//            mLoadingIndicator.setVisibility(View.INVISIBLE);

            if(!TextUtils.isEmpty(jsonString)){
//                mRecyclerView.setVisibility(View.VISIBLE);
                List<MovieReview> movieReviews = JSONUtils.ParseReviews(jsonString);
//                mAdapter = new RecyclerViewAdapter(MainActivity.this, mMovieArray);
//                mAdapter.setClickListener(MainActivity.this);
//                mRecyclerView.setAdapter(mAdapter);
                for(int i = 0; i < movieReviews.size(); i++){

                }
            }
//            else {
//                mErrorMessage.setVisibility(View.VISIBLE);
//            }
        }
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.detail_error_message, Toast.LENGTH_SHORT).show();
    }
}
