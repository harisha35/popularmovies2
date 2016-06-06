package com.harish.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.harish.android.popularmovies.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieListFragment extends Fragment {

    private static ArrayList<String> moviesData = new ArrayList<>();

    private final String LOG_TAG = MovieListFragment.class.getSimpleName();

    private static final String ID = "movie_id";
    private static final String[] FAVOURITES_COLUMNS = {
            MovieContract.FavouriteMovieEntry.COLUMN_MOVIE_ID,
            MovieContract.FavouriteMovieEntry.COLUMN_MOVIE_TITLE,
            MovieContract.FavouriteMovieEntry.COLUMN_MOVIE_POSTER,
            MovieContract.FavouriteMovieEntry.COLUMN_MOVIE_SYNOPSIS,
            MovieContract.FavouriteMovieEntry.COLUMN_MOVIE_USER_RATING,
            MovieContract.FavouriteMovieEntry.COLUMN_MOVIE_RELEASE_DATE
    };

    // These indices are tied to FAVOURITES_COLUMNS.  If FAVOURITES_COLUMNS changes, these
    // must change.
    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_TITLE = 1;
    static final int COL_MOVIE_POSTER = 2;
    static final int COL_MOVIE_SYNOPSIS = 3;
    static final int COL_MOVIE_USER_RATING = 4;
    static final int COL_MOVIE_RELEASE_DATE = 5;

    private ImageListAdapter imageListAdapter;
    final String SEPERATOR = " ### ";

    public MovieListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        imageListAdapter = new ImageListAdapter(getActivity(), moviesData);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.movies_list);
        gridView.setAdapter(imageListAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Log.i(LOG_TAG, "clicked position " + position);
                String movieData = imageListAdapter.getItem(position).toString();
                ((OnSelectListener) getActivity()).showDetails(movieData);
            }
        });
        return rootView;
    }

    private void updateList() {
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortType = sharedPrefs.getString(
                getString(R.string.pref_sort_key),
                getString(R.string.pref_units_popularity));
        if(sortType.equals(getString(R.string.pref_units_favorites))) {
            fetchFavourites();
        } else {
            FetchMoviesTask moviesTask = new FetchMoviesTask();
            moviesTask.execute();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateList();
    }

    private void fetchFavourites() {

        ArrayList<Bitmap> images = new ArrayList<>();
        Cursor cursor = getContext().getContentResolver().query(MovieContract.FavouriteMovieEntry.CONTENT_URI, FAVOURITES_COLUMNS, null, null, ID);
        String[] resultStrs = new String[cursor.getCount()];
        int i = 0;
        while(cursor.moveToNext()) {
            long id = cursor.getLong(COL_MOVIE_ID);
            String title = cursor.getString(COL_MOVIE_TITLE);
            String posterPath = "";
            String releaseDate = cursor.getString(COL_MOVIE_RELEASE_DATE);
            String voteAverage = String.valueOf(cursor.getFloat(COL_MOVIE_USER_RATING));
            String overview = cursor.getString(COL_MOVIE_SYNOPSIS);
            resultStrs[i] = id + SEPERATOR + title + SEPERATOR + posterPath + SEPERATOR + releaseDate + SEPERATOR + voteAverage + SEPERATOR + overview;
            i++;

            byte[] image = cursor.getBlob(COL_MOVIE_POSTER);
            if(image != null) {
                images.add(BitmapFactory.decodeByteArray(image, 0, image.length));
            }
        }
        imageListAdapter.clear();
        imageListAdapter.setImages(images);
        for (String movie : resultStrs) {
            imageListAdapter.add(movie);
        }
        cursor.close();
    }

    public class FetchMoviesTask extends AsyncTask<Void, Void, String[]> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            SharedPreferences sharedPrefs =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sortType = sharedPrefs.getString(
                    getString(R.string.pref_sort_key),
                    getString(R.string.pref_units_popularity));

            try {
                final String MOVIE_LIST_BASE_URL =
                        "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_FORMAT_PARAM = "sort_by";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIE_LIST_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_FORMAT_PARAM, sortType)
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.MOVIES_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }


                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the movies json string.
            return null;
        }

        private String[] getMovieDataFromJson(String moviesJsonStr) throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String RESULT_LIST = "results";
            final String ID = "id";
            final String TITLE = "title";
            final String POSTER_PATH = "poster_path";
            final String RELEASE_DATE = "release_date";
            final String OVERVIEW = "overview";
            final String VOTE_AVERAGE = "vote_average";
            final String MOVIE_POSTER_BASE_URL = "http://image.tmdb.org/t/p/w500";

            JSONObject movieJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = movieJson.getJSONArray(RESULT_LIST);

            String[] resultStrs = new String[moviesArray.length()];

            for (int i = 0; i < moviesArray.length(); i++) {

                JSONObject movieObject = moviesArray.getJSONObject(i);
                long id = movieObject.getLong(ID);
                String title = movieObject.getString(TITLE);
                String posterPath = MOVIE_POSTER_BASE_URL + movieObject.getString(POSTER_PATH);
                String releaseDate = movieObject.getString(RELEASE_DATE);
                String voteAverage = movieObject.getString(VOTE_AVERAGE);
                String overview = movieObject.getString(OVERVIEW);

                resultStrs[i] = id + SEPERATOR + title + SEPERATOR + posterPath + SEPERATOR + releaseDate + SEPERATOR + voteAverage + SEPERATOR + overview;
                Log.d(LOG_TAG, resultStrs[i]);
            }
            return resultStrs;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                imageListAdapter.clear();
                for (String i : result) {
                    imageListAdapter.add(i);
                }
            }
        }
    }

    public interface OnSelectListener {
        void showDetails(String data);
    }
}
