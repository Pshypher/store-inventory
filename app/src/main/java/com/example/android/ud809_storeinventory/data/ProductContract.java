package com.example.android.ud809_storeinventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class ProductContract {

    public static final String BASE_AUTHORITY = "com.example.android.ud809_storeinventory.products";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + BASE_AUTHORITY);

    public static final String PATH_PRODUCTS = "products";

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private ProductContract() {}

    /* Inner class defines the table contents */
    public static final class ProductEntry implements BaseColumns {

        private ProductEntry() {}

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, "products");

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + BASE_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + BASE_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final String TABLE_NAME = "products";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "name";
        public static final String COLUMN_PRODUCT_PICTURE = "picture";
        public static final String COLUMN_PRODUCT_PRICE = "price";
        public static final String COLUMN_PRODUCT_QTY_STOCK = "stock_quantity";
        public static final String COLUMN_PRODUCT_QTY_SOLD = "quantity_sold";
        public static final String COLUMN_PRODUCT_SUPPLIER = "supplier";
        public static final String COLUMN_SUPPLIER_EMAIL = "email";
    }


}
