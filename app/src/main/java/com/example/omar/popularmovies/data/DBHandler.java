package com.example.omar.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import com.example.omar.popularmovies.Container;
import com.example.omar.popularmovies.data.MoviesContract.FavoritesEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by omar on 1/9/16.
 */
public class DBHandler extends SQLiteOpenHelper {
    Context mContext;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "movies.db";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + FavoritesEntry.PATH_FAVORITES + "(" +
                FavoritesEntry.COLUMN_ID + " TEXT PRIMARY KEY," +
                FavoritesEntry.COLUMN_JSON_DETAILS + " TEXT, " +
                FavoritesEntry.COLUMN_JSON_TRAILERS + " TEXT, " +
                FavoritesEntry.COLUMN_JSON_REVIEWS + " TEXT); ";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXIST " + FavoritesEntry.PATH_FAVORITES);
        onCreate(db);
    }

    //Add new row to database
    public void addMovie(Container movie){
        new AddMovieByContainer().execute(movie);
    }

    //Delete movie from the database
    public void deleteMovie(String id){
        new RemoveMovieById().execute(id);
    }

    public int moviesCount() {
        SQLiteDatabase db =  getWritableDatabase();
        String query = "SELECT * FROM " + FavoritesEntry.PATH_FAVORITES;
        Cursor c = db.rawQuery(query, null);
        int result = c.getCount();
        c.close();
        db.close();
        return result;
    }

    public boolean findMovie(String id) {
        SQLiteDatabase db =  getWritableDatabase();
        String query = "SELECT * FROM " + FavoritesEntry.PATH_FAVORITES + " WHERE "
                + FavoritesEntry.COLUMN_ID + " = " + id;
        Cursor c = db.rawQuery(query, null);
        boolean flag = c.getCount() == 1;
        c.close();
        db.close();
        return flag;
    }

    //Return list with max 20 movies from database
    public List<Container> getMoviesFromDatabase(int page){

        String start = Integer.toString((page-1) * 20);

        //This commented code was before content provider implementation
        /*
        String query = "SELECT * FROM " + FavoritesEntry.PATH_FAVORITES +
                " ORDER BY " + FavoritesEntry.COLUMN_ID + " ASC LIMIT " +
                start + ", 20" ;
        SQLiteDatabase db =  getReadableDatabase();
        Cursor c = db.rawQuery(query, null);
        */

        //For populating favorites from content provider
        Cursor c = mContext.getContentResolver().query(
                FavoritesEntry.buildUriWithLIMIT(start, "20"),
                null,
                null,
                null,
                FavoritesEntry.COLUMN_ID + " ASC"
        );

        List<Container> result= new ArrayList<>();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String details = c.getString(c.getColumnIndex(FavoritesEntry.COLUMN_JSON_DETAILS));
            String trailers = c.getString(c.getColumnIndex(FavoritesEntry.COLUMN_JSON_TRAILERS));
            String reviews = c.getString(c.getColumnIndex(FavoritesEntry.COLUMN_JSON_REVIEWS));

            Container movie = new Container(details, trailers, reviews);
            movie.isFavorite = true;

            result.add(movie);
            c.moveToNext();
        }
        c.close();
        //db.close();
        return result;
    }

    public class RemoveMovieById extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            //Without content provider
            /*
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM " + FavoritesEntry.PATH_FAVORITES + " WHERE " +
                    FavoritesEntry.COLUMN_ID + "=\"" + params[0] + "\";");
            db.close();
            */

            //with content provider
            mContext.getContentResolver().delete(
                    FavoritesEntry.buildUriWithMovieID(params[0]),
                    null,
                    null
            );
            return null;
        }
    }

    public class AddMovieByContainer extends AsyncTask<Container, Void, Void> {
        @Override
        protected Void doInBackground(Container... params) {
            ContentValues values = new ContentValues();
            values.put(FavoritesEntry.COLUMN_ID, params[0].id);
            values.put(FavoritesEntry.COLUMN_JSON_DETAILS, params[0].detailsJsonObject);
            values.put(FavoritesEntry.COLUMN_JSON_REVIEWS, params[0].reviewsJsonArray);
            values.put(FavoritesEntry.COLUMN_JSON_TRAILERS, params[0].videosJsonArray);
            //without content provider
            /*
            SQLiteDatabase db = getWritableDatabase();
            db.insert(FavoritesEntry.PATH_FAVORITES, null, values);
            db.close();
            */

            //with content provider
            mContext.getContentResolver().insert(
                    FavoritesEntry.buildUriOfAllFavorites(), values
            );
            return null;
        }
    }

}
