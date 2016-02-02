package com.example.omar.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by omar on 1/9/16.
 */
public class Container implements Serializable {
    public boolean isFavorite = false;
    public boolean fromTwoPane;
    public int positionInAdapter;
    public String id = null;
    public String overView = null;
    String releaseDate = null;
    String name = null;
    String voteAverage = null;
    String imageURL = null;
    public String detailsJsonObject = null;
    public String videosJsonArray = null;
    public String reviewsJsonArray = null;

    //This constructor initialize movie & set it's imageURL
    public Container(JSONObject jsonObject){
        this.detailsJsonObject = jsonObject.toString();
        setDetailsFromJsonObject("imageURL");
    }

    public Container(String detailsJsonObject) {
        this.detailsJsonObject = detailsJsonObject;
        setDetailsFromJsonObject("imageURL");
    }


    public Container(String detailsJsonObject, String videosJsonArray
            , String reviewsJsonArray) {
        this.detailsJsonObject = detailsJsonObject;
        this.videosJsonArray = videosJsonArray;
        this.reviewsJsonArray = reviewsJsonArray;
        setDetailsFromJsonObject("imageURL");
    }

    //Set details to send to detail activity
    public Container setDetails() {
        setDetailsFromJsonObject("All");
        return this;
    }

    //Download poster
    public ImageView getImage(ImageView image, Context context) {

        final String URL_BASE = "http://image.tmdb.org/t/p/w185/";
        Uri builtUri = Uri.parse(URL_BASE).buildUpon()
                .appendPath(imageURL)
                .build();
        Log.e("container", builtUri.toString());
        //new download_image(image).execute(builtUri.toString());
        Picasso
                .with(context)
                .load(builtUri)
                .placeholder(R.drawable.preload)
                .error(R.drawable.preload_error)
                .into(image);

        return image;
    }

    //Set details from movie's JsonObject
    private void setDetailsFromJsonObject(String request){
        try {
            JSONObject JsonObject = new JSONObject(detailsJsonObject);
            if(request.equals("imageURL")) {
                imageURL = JsonObject.getString("poster_path").substring(1);
            } else {
                id = JsonObject.getString("id");
                name = JsonObject.getString("original_title");
                overView = JsonObject.getString("overview");
                releaseDate = JsonObject.getString("release_date");
                voteAverage = JsonObject.getString("vote_average");
            }

        } catch(Exception e) {
            Log.e("Container, setting " + request, "movie id" + id, e);
        }
    }

}
