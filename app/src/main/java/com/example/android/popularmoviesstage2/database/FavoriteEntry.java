package com.example.android.popularmoviesstage2.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "favorites")
public class FavoriteEntry {

    @PrimaryKey(autoGenerate = false)
    private int id;
    private String title;
    private String poster_path;

    public FavoriteEntry(int id, String title, String poster_path){
        this.id = id;
        this.title = title;
        this.poster_path = poster_path;
    }

    public int getId() {
        return id;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public String getTitle() {
        return title;
    }
}
