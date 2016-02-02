package com.example.omar.popularmovies.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by omar on 1/30/16.
 */
public class MoviesContract {

    public static final String CONTENT_AUTHORITY = "com.example.omar.PopularMovies.app";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    public static final class FavoritesEntry implements BaseColumns {

        public static final String PATH_FAVORITES = "favorites";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES)
                        .build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES;

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_JSON_DETAILS = "jsonMovieDetails";
        public static final String COLUMN_JSON_TRAILERS = "jsonTrailers";
        public static final String COLUMN_JSON_REVIEWS = "jsonReviews";



        public static String getLIMITFromUri(Uri uri) {
            return uri.getPathSegments().get(1) + ", " +
                    uri.getPathSegments().get(2);
        }

        public static String getIDFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static Uri buildUriWithLIMIT(String LIMIT, String end) {
            return CONTENT_URI.buildUpon().appendPath(LIMIT)
                    .appendPath(end)
                    .build();
        }

        public static Uri buildUriOfAllFavorites() {
            return CONTENT_URI;
        }

        public static Uri buildUriWithMovieID(String _id) {
            return CONTENT_URI.buildUpon().appendPath(_id).build();
        }

    }
}
