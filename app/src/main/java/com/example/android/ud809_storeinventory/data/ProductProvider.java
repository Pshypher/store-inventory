package com.example.android.ud809_storeinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.android.ud809_storeinventory.data.ProductContract.ProductEntry;

public class ProductProvider extends ContentProvider {


    private ProductDbHelper mDbHelper;

    public static final int PRODUCTS = 100;
    public static final int PRODUCT_ID = 101;

    // Creates a UriMatcher object
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {

        /**
         * Sets the integer value for multiple rows in the products table.
         */
        sUriMatcher.addURI(ProductContract.BASE_AUTHORITY, "products", PRODUCTS);

        /**
         * Sets the code for a single row in the products table.
         */
        sUriMatcher.addURI(ProductContract.BASE_AUTHORITY, "products/#", PRODUCT_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = ProductDbHelper.getDbHelperInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
            @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(
                        ProductEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null
                );
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + " = ? ";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(
                        ProductEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null
                );
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI" + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        String type;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                type = ProductEntry.CONTENT_LIST_TYPE;
                break;
            case PRODUCT_ID:
                type = ProductEntry.CONTENT_ITEM_TYPE;
                break;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }

        return type;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                long id = insertProduct(uri, values);
                Uri productUri = null;
                if (id >= 0) {
                    productUri = ContentUris.withAppendedId(uri, id);
                }
                return productUri;
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

    }

    private long insertProduct(Uri uri, ContentValues values) {

        String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        Double price = values.getAsDouble(ProductEntry.COLUMN_PRODUCT_PRICE);
        Integer availableQuantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QTY_STOCK);

        long id = 0;
        if (TextUtils.isEmpty(name) || price == null
                || availableQuantity == null) {
            return id;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        id =  database.insert(ProductEntry.TABLE_NAME, null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return id;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;
        switch (match) {
            case PRODUCTS:
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + " = ? ";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        int rowsAffected;
        switch (match) {
            case PRODUCTS:
                rowsAffected = updateProduct(uri, values, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + " = ? ";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsAffected = updateProduct(uri, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

        if (rowsAffected > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsAffected;
    }

    private int updateProduct(Uri uri, ContentValues values, String selection,
                              String[] selectionArgs) {
        int rowsAffected = 0;
        if (values.size() == 0) {
            return rowsAffected;
        }

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (TextUtils.isEmpty(name)) {
                return rowsAffected;
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)) {
            Double price = values.getAsDouble(ProductEntry.COLUMN_PRODUCT_PRICE);
            if (price == null) {
                return rowsAffected;
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_QTY_STOCK)) {
            Integer qtyAvailable = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QTY_STOCK);
            if (qtyAvailable == null) {
                return rowsAffected;
            }
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        rowsAffected = database.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        return rowsAffected;
    }
}
