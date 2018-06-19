package com.example.android.popularmoviesstage1.data;

public class MovieTrailer {
    private String key;
    private String site;
    private String name;

    public MovieTrailer(String key, String name, String site){
        this.key = key;
        this.name = name;
        this.site = site;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getSite() {
        return site;
    }
}
