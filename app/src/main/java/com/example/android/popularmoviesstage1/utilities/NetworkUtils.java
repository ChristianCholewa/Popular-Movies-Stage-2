package com.example.android.popularmoviesstage1.utilities;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class NetworkUtils {

    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();

    private final static String TMDB_MOVIES_BASE_URL =
            "https://api.themoviedb.org/3/movie";

    private final static String TMDB_POSTER_BASE_URL =
            "https://image.tmdb.org/t/p/w342";

    private final static String PARAM_API_KEY =
            "api_key";

    private final static String PATH_TRAILER =
            "videos";

    private final static String PATH_REVIEW =
            "reviews";

    public static URL buildUrl(String api_key, String sortString){

        Uri builtUri = Uri.parse(TMDB_MOVIES_BASE_URL).buildUpon()
                .appendPath(sortString)
                .appendQueryParameter(PARAM_API_KEY, api_key)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL buildPosterUrlString(String poster_path){
        URL url = null;
        try {
            url = new URL(TMDB_POSTER_BASE_URL + poster_path);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL buildTrailerUrl(String api_key, int movieId){

        Uri builtUri = Uri.parse(TMDB_MOVIES_BASE_URL).buildUpon()
                .appendPath(Integer.toString(movieId))
                .appendPath(PATH_TRAILER)
                .appendQueryParameter(PARAM_API_KEY, api_key)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL buildReviewUrl(String api_key, int movieId){

        Uri builtUri = Uri.parse(TMDB_MOVIES_BASE_URL).buildUpon()
                .appendPath(Integer.toString(movieId))
                .appendPath(PATH_REVIEW)
                .appendQueryParameter(PARAM_API_KEY, api_key)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
