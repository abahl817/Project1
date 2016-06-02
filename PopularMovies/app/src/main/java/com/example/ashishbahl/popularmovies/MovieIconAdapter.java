package com.example.ashishbahl.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Ashish Bahl on 31-May-16.
 */
public class MovieIconAdapter extends ArrayAdapter {
    private Context context;
    private LayoutInflater inflater;

    private String[] movUrls;

    public MovieIconAdapter(Activity context, ArrayList<String> movieUrls){
        super(context,R.layout.movie_item,movieUrls);

        this.context=context;
        this.movUrls = movieUrls.toArray(new String[movieUrls.size()]);

        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Gets the AndroidFlavor object from the ArrayAdapter at the appropriate position

        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.movie_item, parent, false);
        }

        Picasso
                .with(context)
                .load(movUrls[position])
                .fit() // will explain later
                .into((ImageView) convertView);
        /*ImageView icon = (ImageView) convertView.findViewById(R.id.movie_image);
        icon.setImageResource(movieIcons.url);*/

        return convertView;
    }
}

