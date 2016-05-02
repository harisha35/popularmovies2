package com.harish.android.popularmovies;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class CustomAdapter extends ArrayAdapter {

    private final String SEPERATOR = " ### ";
    final boolean isTrailerView;

    private final String LOG_TAG = CustomAdapter.class.getSimpleName();

    public CustomAdapter(Activity context, List<String> movieResources, boolean isTrailerView) {
        super(context, 0, movieResources);
        this.isTrailerView = isTrailerView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final String movieDetails = getItem(position).toString();
        // if the view is of type trailer
        if(isTrailerView) {
            final String movieTrailer = movieDetails.split(SEPERATOR)[0];
            final String movieTrailerName = movieDetails.split(SEPERATOR)[1];

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_trailers, parent, false);
            }

            ImageButton trailerButton = (ImageButton) convertView.findViewById(R.id.trailer_play_button);
            trailerButton.setOnClickListener(new View.OnClickListener() {
                                                 @Override
                                                 public void onClick(View v) {
                                                     String trailerPath = movieTrailer;
                                                     getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailerPath)));
                                                 }
                                             }
            );

            TextView trailerNumber = (TextView) convertView.findViewById(R.id.trailer_number);
            // position starts from 0 but trailer numbers start from 1
            trailerNumber.setText(movieTrailerName);

        } else {
            final String author = movieDetails.split(SEPERATOR)[0];
            final String content = movieDetails.split(SEPERATOR)[1];

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_reviews, parent, false);
            }

            TextView reviewAuthor = (TextView) convertView.findViewById(R.id.review_author);
            reviewAuthor.setText(author);

            TextView reviewContent = (TextView) convertView.findViewById(R.id.review_content);
            reviewContent.setText(content);
        }

        return convertView;
    }
}
