package com.harish.android.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.harish.android.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

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
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    private final String SEPERATOR = " ### ";
    private final String LOG_TAG = DetailFragment.class.getSimpleName();
    private String movieDataStr;
    private String OVERVIEW_UNAVAILABLE = "No overview found";
    private String TRAILER_LABEL = "trailer";
    private String REVIEW_LABEL = "review";
    private CustomAdapter mTrailerAdapter;
    private CustomAdapter mReviewAdapter;
    static final String DETAIL_URI = "URI";
    String movieData = null;

    private static final String ID = "movie_id";
    static final int COL_MOVIE_POSTER = 0;

    private ShareActionProvider mShareActionProvider;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    public static DetailFragment newInstance(int index) {
        DetailFragment detailFragment = new DetailFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        detailFragment.setArguments(args);

        return detailFragment;
    }

    public void setData(String movieData) {
        this.movieData = movieData;
        updateView(movieData, getView());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.content_detail, container, false);

        Intent intent = getActivity().getIntent();
        if ((intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) || movieData != null) {
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                movieDataStr = intent.getStringExtra(Intent.EXTRA_TEXT);
            } else {
                movieDataStr = movieData;
            }
            updateView(movieDataStr, rootView);
        }
        return rootView;
    }

    public void updateView(String movieDataStr, View rootView) {
        String[] movieData = movieDataStr.split(SEPERATOR);
        String movieId = movieData[0];
        String movieTitle = movieData[1];
        String moviePoster = movieData[2];
        String movieRelease = movieData[3];
        String movieUserRating = movieData[4];
        String movieOverview;

        // The following check is to detect if movie's overview is not available
        if (movieData.length > 5) {
            movieOverview = movieData[5];
        } else {
            movieOverview = OVERVIEW_UNAVAILABLE;
        }

        ((TextView) rootView.findViewById(R.id.movie_title))
                .setText(movieTitle);
        ((TextView) rootView.findViewById(R.id.movie_overview))
                .setText(movieOverview);
        ((TextView) rootView.findViewById(R.id.movie_release_date))
                .setText(movieRelease);
        ((TextView) rootView.findViewById(R.id.movie_rating))
                .setText(movieUserRating);
        Cursor cursor = getActivity().getContentResolver().query(MovieContract.FavouriteMovieEntry.CONTENT_URI,
                new String[]{MovieContract.FavouriteMovieEntry.COLUMN_MOVIE_POSTER}, ID + "=?", new String[]{movieId}, ID);

        if (moviePoster.isEmpty()) {
            if(cursor.getCount() >= 1) {
                cursor.moveToFirst();
                byte[] image = cursor.getBlob(COL_MOVIE_POSTER);
                ((ImageView) rootView.findViewById(R.id.movie_poster)).setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
            }

        } else {
            Picasso
                    .with(getContext())
                    .load(moviePoster)
                    .error(R.drawable.notfound)
                    .into((ImageView) rootView.findViewById(R.id.movie_poster));
        }

        cursor.close();

        FetchMovieDetailsTask movieTrailerDetailsTask = new FetchMovieDetailsTask();
        movieTrailerDetailsTask.execute(movieId, TRAILER_LABEL);

        mTrailerAdapter = new CustomAdapter(getActivity(), new ArrayList<String>(), true);
        ListView trailersList = (ListView) rootView.findViewById(R.id.listview_trailers);
        trailersList.setAdapter(mTrailerAdapter);

        FetchMovieDetailsTask movieReviewDetailsTask = new FetchMovieDetailsTask();
        movieReviewDetailsTask.execute(movieId, REVIEW_LABEL);

        mReviewAdapter = new CustomAdapter(getActivity(), new ArrayList<String>(), false);
        ListView reviewsList = (ListView) rootView.findViewById(R.id.listview_reviews);
        reviewsList.setAdapter(mReviewAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mTrailerAdapter != null && !mTrailerAdapter.isEmpty()) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mTrailerAdapter.getItem(0).toString().split(SEPERATOR)[0]);
        return shareIntent;
    }

    public class FetchMovieDetailsTask extends AsyncTask<String, Void, String[][]> {

        private final String LOG_TAG = FetchMovieDetailsTask.class.getSimpleName();

        @Override
        protected String[][] doInBackground(String... params) {

            final String MOVIE_LIST_BASE_URL =
                    "http://api.themoviedb.org/3/movie/";
            final String API_KEY_PARAM = "api_key";
            final String TRAILERS_PARAM = "videos";
            final String REVIEWS_PARAM = "reviews";
            String movieId = params[0];
            String trailerJsonStr = null;
            String reviewsJsonStr = null;

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {

                Uri builtUri = Uri.parse(MOVIE_LIST_BASE_URL).buildUpon()
                        .appendPath(movieId)
                        .appendPath(TRAILERS_PARAM)
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.MOVIES_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());
                Log.d(LOG_TAG, builtUri.toString());

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

                trailerJsonStr = buffer.toString();

                builtUri = Uri.parse(MOVIE_LIST_BASE_URL).buildUpon()
                        .appendPath(movieId)
                        .appendPath(REVIEWS_PARAM)
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.MOVIES_DB_API_KEY)
                        .build();

                url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                inputStream = urlConnection.getInputStream();
                buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }


                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                reviewsJsonStr = buffer.toString();

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
                String[] trailers = getTrailerListFromJson(trailerJsonStr);
                String[] reviews = getReviewListFromJson(reviewsJsonStr);
                String[][] result = new String[2][];
                result[0] = trailers;
                result[1] = reviews;

                return result;
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        private String[] getTrailerListFromJson(String trailersJsonStr) throws JSONException {
            final String RESULT_LIST = "results";
            final String TRAILER_KEY = "key";
            final String TRAILER_NAME = "name";
            final String YOUTUBE_VIDEO_BASE_URL = "https://www.youtube.com/watch?v=";

            JSONObject trailersJson = new JSONObject(trailersJsonStr);
            JSONArray trailersArray = trailersJson.getJSONArray(RESULT_LIST);

            String[] resultStrs = new String[trailersArray.length()];

            for (int i = 0; i < trailersArray.length(); i++) {
                JSONObject trailerObject = trailersArray.getJSONObject(i);
                String key = trailerObject.getString(TRAILER_KEY);
                String name = trailerObject.getString(TRAILER_NAME);
                resultStrs[i] = YOUTUBE_VIDEO_BASE_URL + key + SEPERATOR + name;
            }
            return resultStrs;
        }

        // All even position items return author and all odd positioned items return content of the
        // author in previous item
        private String[] getReviewListFromJson(String reviewsJsonStr) throws JSONException {
            final String RESULT_LIST = "results";
            final String REVIEW_AUTHOR_KEY = "author";
            final String REVIEW_CONTENT_KEY = "content";

            JSONObject reviewsJson = new JSONObject(reviewsJsonStr);
            JSONArray reviewsArray = reviewsJson.getJSONArray(RESULT_LIST);

            String[] resultStrs = new String[reviewsArray.length()];

            for (int i = 0; i < reviewsArray.length(); i++) {
                JSONObject reviewObject = reviewsArray.getJSONObject(i);
                String author = reviewObject.getString(REVIEW_AUTHOR_KEY);
                String content = reviewObject.getString(REVIEW_CONTENT_KEY);
                resultStrs[i] = author + SEPERATOR + content;
            }
            return resultStrs;
        }

        @Override
        protected void onPostExecute(String[][] results) {
            if (results != null) {
                if(results[0] != null) {
                    mTrailerAdapter.clear();
                    for (String result : results[0]) {
                        mTrailerAdapter.add(result);
                    }
                }
                if(results[1] != null){
                    mReviewAdapter.clear();
                    for (String result : results[1]) {
                        mReviewAdapter.add(result);
                    }
                }
            }
            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mTrailerAdapter != null && !mTrailerAdapter.isEmpty()) {
                if(mShareActionProvider != null)
                    mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }
    }
}