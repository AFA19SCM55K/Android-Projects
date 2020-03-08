package com.akshay.stockwatchapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHandler";

    private static final int DATABASE_VERSION = 1 ;
    private static final String DATABASE_NAME = "StockAppDB";
    private static final String TABLE_NAME = "CountryTable";
    private static final String SYMBOL = "Symbol";
    private static final String NAME = "Name";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    SYMBOL + " TEXT not null unique," +
                    NAME + " TEXT not null)";

    private SQLiteDatabase database;

    DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase(); // Inherited from SQLiteOpenHelper
        Log.d(TAG, "DatabaseHandler: C'tor DONE");
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: ");
        db.execSQL(SQL_CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    ArrayList<Stock> loadCountries() {

        // Load countries - return ArrayList of loaded countries
        Log.d(TAG, "loadCountries: START");
        ArrayList<Stock> stocks = new ArrayList<>();

        Cursor cursor = database.query(
                TABLE_NAME,  // The table to query
                new String[]{SYMBOL,NAME}, // The columns to return
                null, // The columns for the WHERE clause
                null, // The values for the WHERE clause
                null, // don't group the rows
                null, // don't filter by row groups
                null); // The sort order

        if (cursor != null) {
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                String symbol = cursor.getString(0);
                String name = cursor.getString(1);
                Stock c = new Stock(symbol,name);
                stocks.add(c);
                cursor.moveToNext();
            }
            cursor.close();
        }
        Log.d(TAG, "loadCountries: DONE");

        return stocks;
    }

    void addStocks(Stock stock){
        ContentValues contentValues = new ContentValues();
        contentValues.put(SYMBOL,stock.getSymbol());
        contentValues.put(NAME,stock.getName());

        long key = database.insert(TABLE_NAME,null,contentValues);
        Log.d(TAG, "addStocks: "+key);

    }
    void deleteStock(Stock stock) {
        Log.d(TAG, "deleteStock: " + stock.getSymbol());

        int cnt = database.delete(TABLE_NAME, SYMBOL + " = ?", new String[]{stock.getSymbol()});

        Log.d(TAG, "deleteStock: " + cnt);
    }

    void shutDown(){
        database.close();
    }

    void dumpDbToLog() {
        Cursor cursor = database.rawQuery("select * from " + TABLE_NAME, null);
        if (cursor != null) {
            cursor.moveToFirst();

            Log.d(TAG, "dumpDbToLog: vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
            for (int i = 0; i < cursor.getCount(); i++) {
                String country = cursor.getString(0);
                String region = cursor.getString(1);

                Log.d(TAG, "dumpDbToLog: " +
                        String.format("%s %-18s", SYMBOL + ":", country) +
                        String.format("%s %-18s", NAME + ":", region) );

                cursor.moveToNext();
            }
            cursor.close();
        }

        Log.d(TAG, "dumpDbToLog: ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
    }


}
