package com.harish.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    private final String SEPERATOR = " ### ";
    private String movieDataStr;
    private String OVERVIEW_UNAVAILABLE = "No overview found";

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.content_detail, container, false);

        // The detail Activity called via intent.  Inspect the intent for forecast data.
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            movieDataStr = intent.getStringExtra(Intent.EXTRA_TEXT);

            String[] movieData = movieDataStr.split(SEPERATOR);
            String movieTitle = movieData[0];
            String moviePoster = movieData[1];
            String movieRelease = movieData[2];
            String movieUserRating = movieData[3];
            String movieOverview;

            // The following check is to detect if movie's overview is not available
            if(movieData.length > 4) {
                movieOverview = movieData[4];
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
            Picasso
                    .with(getContext())
                    .load(moviePoster)
                    .into((ImageView) rootView.findViewById(R.id.movie_poster));

        }

        return rootView;
    }
}