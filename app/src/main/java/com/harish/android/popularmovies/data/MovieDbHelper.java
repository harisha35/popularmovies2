package com.harish.android.popularmovies.data;

import com.harish.android.popularmovies.data.MovieContract.FavouriteMovieEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MovieDbHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "movies";
    private static final int DATABASE_VERSION = 1;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold favourite movies
        final String SQL_CREATE_FAVOURITE_MOVIES_TABLE = "CREATE TABLE " +
                FavouriteMovieEntry.TABLE_NAME + " (" +
                FavouriteMovieEntry.COLUMN_MOVIE_ID + " INTEGER PRIMARY KEY," +
                FavouriteMovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                FavouriteMovieEntry.COLUMN_MOVIE_POSTER + " BLOB, " +
                FavouriteMovieEntry.COLUMN_MOVIE_SYNOPSIS + " TEXT NOT NULL, " +
                FavouriteMovieEntry.COLUMN_MOVIE_USER_RATING + " REAL NOT NULL, " +
                FavouriteMovieEntry.COLUMN_MOVIE_RELEASE_DATE + " TEXT NOT NULL " +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_FAVOURITE_MOVIES_TABLE);
    }



    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // The tables should not be removed on upgrade without saving the already present data
        // because user wants to see his favourite movie list even after upgrade. Here a logic
        // should be written during the upgrade on how to import all the data into the new schema
    }
}
