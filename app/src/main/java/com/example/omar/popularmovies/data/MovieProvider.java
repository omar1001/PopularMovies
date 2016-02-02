package com.example.omar.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.omar.popularmovies.data.MoviesContract.FavoritesEntry;

/**
 * Created by omar on 1/31/16.
 */
public class MovieProvider extends ContentProvider  {
    private DBHandler mDBHandler;

    private static final int ALL_FAVORITES = 1;
    private static final int ALL_FAVORITES_WITH_PAGE_NUM = 2;
    private static final int MOVIE_BY_ID = 3;

    private static UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        final String AUTHORITY = MoviesContract.CONTENT_AUTHORITY;

        //Uri to get all favorite movies
        matcher.addURI(AUTHORITY, FavoritesEntry.PATH_FAVORITES,
                ALL_FAVORITES);
        //Uri with movies LIMIT from data base
        matcher.addURI(AUTHORITY, FavoritesEntry.PATH_FAVORITES + "/*/*",
                ALL_FAVORITES_WITH_PAGE_NUM);
        //Uri with movie _id
        matcher.addURI(AUTHORITY, FavoritesEntry.PATH_FAVORITES + "/*",
                MOVIE_BY_ID);

    }

    @Override
    public boolean onCreate() {
        mDBHandler = new DBHandler(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor rCursor;

        switch (matcher.match(uri)) {
            case ALL_FAVORITES_WITH_PAGE_NUM: {
                rCursor = mDBHandler.getReadableDatabase().query(
                        FavoritesEntry.PATH_FAVORITES,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder,
                        FavoritesEntry.getLIMITFromUri(uri)
                );
                break;
            }
            case MOVIE_BY_ID: {
                rCursor = mDBHandler.getReadableDatabase().query(
                        FavoritesEntry.PATH_FAVORITES,
                        projection,
                        FavoritesEntry.COLUMN_ID + " = ?",
                        new String[] {FavoritesEntry.getIDFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case ALL_FAVORITES: {
                rCursor = mDBHandler.getReadableDatabase().query(
                        FavoritesEntry.PATH_FAVORITES,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        rCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return rCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        switch (matcher.match(uri)) {

            case ALL_FAVORITES:
                return FavoritesEntry.CONTENT_TYPE;
            case ALL_FAVORITES_WITH_PAGE_NUM:
                return FavoritesEntry.CONTENT_TYPE;
            case MOVIE_BY_ID:
                return FavoritesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri rUri;
        Long _id = mDBHandler.getWritableDatabase().insert(
                FavoritesEntry.PATH_FAVORITES,
                null,
                values
        );
        if( _id != -1 ) {
            rUri = FavoritesEntry.buildUriWithMovieID(Long.toString(_id));
        } else {
            throw new android.database.SQLException("Failed to insert row");
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowDeleted;

        switch(matcher.match(uri)) {
            case ALL_FAVORITES: {
                rowDeleted = mDBHandler.getWritableDatabase().delete(
                        FavoritesEntry.PATH_FAVORITES,
                        selection,
                        selectionArgs
                );
                break;
            }
            case MOVIE_BY_ID: {
                rowDeleted = mDBHandler.getWritableDatabase().delete(
                        FavoritesEntry.PATH_FAVORITES,
                        FavoritesEntry.COLUMN_ID + " = ?",
                        new String[] {FavoritesEntry.getIDFromUri(uri)}
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(rowDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int rowsUpdated;

        switch (matcher.match(uri)) {
            case MOVIE_BY_ID: {
                rowsUpdated = mDBHandler.getWritableDatabase().update(
                        FavoritesEntry.PATH_FAVORITES,
                        values,
                        FavoritesEntry.COLUMN_ID + " = ?",
                        new String[] { FavoritesEntry.getIDFromUri(uri) }
                );
                break;
            }
            case ALL_FAVORITES: {
                rowsUpdated = mDBHandler.getWritableDatabase().update(
                        FavoritesEntry.PATH_FAVORITES,
                        values,
                        null,
                        null
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(rowsUpdated != -1) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
