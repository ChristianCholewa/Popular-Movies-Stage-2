package com.example.android.popularmoviesstage2.data;

import android.os.Parcel;
import android.os.Parcelable;

public class MovieData implements Parcelable  {

    public static final String EXTRA_NAME_MOVIEDATA = "movie_data";

    //Movies are displayed in the main layout via a grid of their corresponding movie poster thumbnails.
    //Movie details layout contains title, release date, movie poster, vote average, and plot synopsis.
    private int id;
    private String title;
    private String release_date; // "2018-05-15"
    private String poster_path;
    private double vote_average; // 7.9
    private String overview; // this is the plot synopsis

    public MovieData(int id, String title, String release_date, String poster_path, double vote_average, String overview){
        this.id = id;
        this.title = title;
        this.release_date = release_date;
        this.poster_path = poster_path;
        this.vote_average = vote_average;
        this.overview = overview;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public double getVote_average() {
        return vote_average;
    }

    public String getOverview() {
        return overview;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public String getRelease_date() { return release_date; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeDouble(vote_average);
        dest.writeString(overview);
        dest.writeString(poster_path);
        dest.writeString(release_date);
    }

    private void readFromParcel(Parcel in) {
        id = in.readInt();
        title = in.readString();
        vote_average = in.readDouble();
        overview = in.readString();
        poster_path = in.readString();
        release_date = in.readString();
    }

    private MovieData(Parcel in){
        readFromParcel(in);
    }

    public static final Parcelable.Creator<MovieData> CREATOR = new Parcelable.Creator<MovieData>() {
        @Override
        public MovieData createFromParcel(Parcel source) {
            return new MovieData(source);
        }

        @Override
        public MovieData[] newArray(int size) {
            return new MovieData[size];
        }
    };
}
