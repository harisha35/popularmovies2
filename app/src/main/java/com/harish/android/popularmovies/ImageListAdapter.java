package com.harish.android.popularmovies;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ImageListAdapter extends ArrayAdapter {
    final String SEPERATOR = " ### ";
    private ArrayList<Bitmap> images;

    public ImageListAdapter(Activity context, List<String> movieData) {
        super(context, 0, movieData);
    }

    public void setImages(ArrayList<Bitmap> images) {
        this.images = images;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String movieData = getItem(position).toString();

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_view_movies, parent, false);
        }

        String imageUrl = movieData.split(SEPERATOR)[2];

        if(imageUrl != null && !imageUrl.isEmpty()) {
            Picasso
                    .with(getContext())
                    .load(imageUrl)
                    .error(R.drawable.notfound)
                    .into((ImageView) convertView);
        } else {
            ImageView imageView = (ImageView) convertView;
            imageView.setImageBitmap(images.get(position));
        }

        return convertView;
    }
}
