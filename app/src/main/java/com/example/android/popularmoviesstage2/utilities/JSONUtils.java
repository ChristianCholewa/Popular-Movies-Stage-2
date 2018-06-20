package com.example.android.popularmoviesstage2.utilities;

import android.text.TextUtils;

import com.example.android.popularmoviesstage2.data.MovieData;
import com.example.android.popularmoviesstage2.data.MovieReview;
import com.example.android.popularmoviesstage2.data.MovieTrailer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JSONUtils {

    // common keys
    private static String JSON_KEY_RESULTS = "results";

    // overview
    private static String JSON_KEY_MOVIE_ID = "id";
    private static String JSON_KEY_TITLE = "title";
    private static String JSON_KEY_RELEASE_DATE = "release_date";
    private static String JSON_KEY_POSTER_PATH = "poster_path";
    private static String JSON_KEY_VOTE_AVERAGE = "vote_average";
    private static String JSON_KEY_OVERVIEW = "overview";

    // trailers
    private static String JSON_KEY_TRAILER_KEY = "key";
    private static String JSON_KEY_TRAILER_NAME = "name";
    private static String JSON_KEY_TRAILER_SITE = "site";
    private static String JSON_KEY_TRAILER_TYPE = "type";

    // review
    private static String JSON_KEY_REVIEW_AUTHOR = "author";
    private static String JSON_KEY_REVIEW_CONTENT = "content";
    private static String JSON_KEY_REVIEW_URL = "url";

    public static List<MovieData> ParseOverview(String jsonString) {
        List movieData = new ArrayList();

        if(!TextUtils.isEmpty(jsonString)){
            try {
                JSONObject allData = new JSONObject(jsonString);
                JSONArray results = allData.getJSONArray(JSON_KEY_RESULTS);

                for (int i = 0; i < results.length(); i++){
                    JSONObject movie = results.getJSONObject(i);

                    MovieData newMovie = new MovieData(
                        movie.getInt(JSON_KEY_MOVIE_ID),
                        movie.getString(JSON_KEY_TITLE),
                        movie.getString(JSON_KEY_RELEASE_DATE),
                        movie.getString(JSON_KEY_POSTER_PATH),
                        movie.getDouble(JSON_KEY_VOTE_AVERAGE),
                        movie.getString(JSON_KEY_OVERVIEW)
                    );

                    movieData.add(newMovie);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return movieData;
    }

    public static List<MovieTrailer> ParseTrailers(String jsonString) {
        List movieTrailers = new ArrayList();

        if(!TextUtils.isEmpty(jsonString)){
            try {
                JSONObject allData = new JSONObject(jsonString);
                JSONArray results = allData.getJSONArray(JSON_KEY_RESULTS);

                for (int i = 0; i < results.length(); i++){
                    JSONObject trailer = results.getJSONObject(i);

                    MovieTrailer newTrailer = new MovieTrailer(
                            trailer.getString(JSON_KEY_TRAILER_KEY),
                            trailer.getString(JSON_KEY_TRAILER_NAME),
                            trailer.getString(JSON_KEY_TRAILER_SITE),
                            trailer.getString(JSON_KEY_TRAILER_TYPE)
                    );

                    movieTrailers.add(newTrailer);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return movieTrailers;
    }

    public static List<MovieReview> ParseReviews(String jsonString) {
        List movieReviews = new ArrayList();

        if(!TextUtils.isEmpty(jsonString)){
            try {
                JSONObject allData = new JSONObject(jsonString);
                JSONArray results = allData.getJSONArray(JSON_KEY_RESULTS);

                for (int i = 0; i < results.length(); i++){
                    JSONObject review = results.getJSONObject(i);

                    MovieReview newReview = new MovieReview(
                            review.getString(JSON_KEY_REVIEW_AUTHOR),
                            review.getString(JSON_KEY_REVIEW_CONTENT),
                            review.getString(JSON_KEY_REVIEW_URL)
                    );

                    movieReviews.add(newReview);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return movieReviews;
    }
}
