package com.example.android.ud809_storeinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.android.ud809_storeinventory.data.ProductContract.ProductEntry;

public class ProductDbHelper extends SQLiteOpenHelper {

    private static ProductDbHelper mDbHelper;

    // If you change the database schema, you must increment the database version.
    public static final String DATABASE_NAME = "Inventory.db";
    public static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + ProductEntry.TABLE_NAME
            + " (" + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
            + ProductEntry.COLUMN_PRODUCT_PICTURE + " TEXT, "
            + ProductEntry.COLUMN_PRODUCT_PRICE + " REAL NOT NULL, "
            + ProductEntry.COLUMN_PRODUCT_QTY_STOCK + " INTEGER NOT NULL DEFAULT 0, "
            + ProductEntry.COLUMN_PRODUCT_QTY_SOLD + " INTEGER NOT NULL DEFAULT 0, "
            + ProductEntry.COLUMN_PRODUCT_SUPPLIER + " TEXT, "
            + ProductEntry.COLUMN_SUPPLIER_EMAIL + " TEXT)";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
            + ProductEntry.TABLE_NAME;

    private ProductDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static ProductDbHelper getDbHelperInstance(Context context) {
        if (mDbHelper == null) {
            mDbHelper = new ProductDbHelper(context);
        }

        return mDbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
