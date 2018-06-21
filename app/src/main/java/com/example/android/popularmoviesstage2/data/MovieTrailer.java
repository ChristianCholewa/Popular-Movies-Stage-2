package com.example.android.popularmoviesstage2.data;

public class MovieTrailer {
    private String key;
    private String site;
    private String name;
    private String type;

    public MovieTrailer(String key, String name, String site, String type){
        this.key = key;
        this.name = name;
        this.site = site;
        this.type = type;
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

    public String getType() { return type; }
}
