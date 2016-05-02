package com.harish.android.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.harish.android.popularmovies.data.MovieContract;
import com.harish.android.popularmovies.data.MovieContract.FavouriteMovieEntry;

import java.io.DataInputStream;
import java.net.URL;
import java.net.URLConnection;

public class MovieDetailActivity extends ActionBarActivity {

    private static final String LOG_TAG = MovieDetailActivity.class.getSimpleName();

    private static final String ID = "movie_id";
    private String SEPERATOR = " ### ";
    private String OVERVIEW_UNAVAILABLE = "No overview found";
    private String movieDataStr;
    private String movieId, movieTitle, moviePoster, movieRelease, movieUserRating, movieOverview;
    Intent intent;
    private static boolean isFavorite = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {

            Bundle arguments = new Bundle();

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, new DetailFragment())
                    .commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);

        intent = getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            movieDataStr = intent.getStringExtra(Intent.EXTRA_TEXT);

            String[] movieData = movieDataStr.split(SEPERATOR);
            movieId = movieData[0];
            movieTitle = movieData[1];
            moviePoster = movieData[2];
            movieRelease = movieData[3];
            movieUserRating = movieData[4];

            // The following check is to detect if movie's overview is not available
            if (movieData.length > 5) {
                movieOverview = movieData[5];
            } else {
                movieOverview = OVERVIEW_UNAVAILABLE;
            }
        }

        Cursor cursor = getContentResolver().query(MovieContract.FavouriteMovieEntry.CONTENT_URI,
                new String[]{MovieContract.FavouriteMovieEntry.COLUMN_MOVIE_POSTER}, ID + "=?", new String[]{movieId}, ID);

        if(cursor.getCount() > 0) {
            (menu.findItem(R.id.action_favorite)).setIcon(R.drawable.favourite_after);
            isFavorite = true;
        } else {
            (menu.findItem(R.id.action_favorite)).setIcon(R.drawable.favourite_before);
            isFavorite = false;
        }

        cursor.close();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_favorite) {

            if(isFavorite) {
                getContentResolver().delete(FavouriteMovieEntry.CONTENT_URI, ID + "=?", new String[] {movieId});

                isFavorite = false;
                item.setIcon(R.drawable.favourite_before);

            } else {
                ContentValues contentValues = new ContentValues();

                Intent intent = getIntent();
                if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                    contentValues.put(FavouriteMovieEntry.COLUMN_MOVIE_ID, movieId);
                    contentValues.put(FavouriteMovieEntry.COLUMN_MOVIE_TITLE, movieTitle);
                    contentValues.put(FavouriteMovieEntry.COLUMN_MOVIE_RELEASE_DATE, movieRelease);
                    contentValues.put(FavouriteMovieEntry.COLUMN_MOVIE_USER_RATING, movieUserRating);
                    contentValues.put(FavouriteMovieEntry.COLUMN_MOVIE_SYNOPSIS, movieOverview);

                    try {
                        URL url = new URL(moviePoster);
                        URLConnection conn = url.openConnection();
                        int contentLength = conn.getContentLength();

                        DataInputStream stream = new DataInputStream(url.openStream());
                        byte[] buffer = new byte[contentLength];
                        stream.readFully(buffer);
                        stream.close();

                        contentValues.put(FavouriteMovieEntry.COLUMN_MOVIE_POSTER, buffer);
                    } catch(Exception e) {
                        Log.e(LOG_TAG, e.toString());
                    }

                    getContentResolver().insert(FavouriteMovieEntry.CONTENT_URI, contentValues);

                    isFavorite = true;
                    item.setIcon(R.drawable.favourite_after);
                }
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
