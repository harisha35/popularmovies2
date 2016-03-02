package com.harish.android.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageListAdapter extends ArrayAdapter {
    final String SEPERATOR = " ### ";

    public ImageListAdapter(Activity context, List<String> movieData) {
        super(context, 0, movieData);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String movieData = getItem(position).toString();

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_view_movies, parent, false);
        }

        String imageUrl = movieData.split(SEPERATOR)[1];

        Picasso
                .with(getContext())
                .load(imageUrl)
                .into((ImageView) convertView);

        return convertView;
    }
}
