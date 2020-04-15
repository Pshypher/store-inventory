package com.example.android.ud809_storeinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.ud809_storeinventory.data.ProductContract.ProductEntry;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View itemView = inflater.inflate(R.layout.activity_item_product, parent, false);
        return itemView;
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        ImageView productImageView = (ImageView) view.findViewById(R.id.img_product);
        TextView nameTextView = (TextView) view.findViewById(R.id.product_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.product_price);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        final TextView quantitySoldTextView = (TextView) view.findViewById(R.id.quantity_sold);

        int idIndex = cursor.getColumnIndex(ProductEntry._ID);
        int imageUriIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PICTURE);
        int nameIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int stockQuantityIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QTY_STOCK);
        final int quantitySoldIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QTY_SOLD);

        final long id = cursor.getLong(idIndex);

        final String imagePath = cursor.getString(imageUriIndex);
        final String name = cursor.getString(nameIndex);
        final double price = cursor.getDouble(priceIndex);
        final int stockQuantity = cursor.getInt(stockQuantityIndex);
        final int quantitySold = cursor.getInt(quantitySoldIndex);

        Resources res = context.getResources();
        if (imagePath == null) {
            productImageView.setImageDrawable(res.getDrawable(R.drawable.ic_empty_image_placeholder));
        } else {
            Bitmap bitmap = BitmapUtility.getImage(context, imagePath);
            productImageView.setImageBitmap(bitmap);
        }
        nameTextView.setText(name);
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        formatter.setCurrency(Currency.getInstance(Locale.US));
        priceTextView.setText(formatter.format(price));
        quantityTextView.setText(String.valueOf(stockQuantity));
        quantitySoldTextView.setText(String.valueOf(quantitySold));

        setTextViewColor(context, stockQuantity, quantityTextView);

        ImageButton sellIconView = (ImageButton) view.findViewById(R.id.button_sell);
        sellIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int stockQuantity = Integer.parseInt(quantityTextView.getText().toString());
                int quantitySold = Integer.parseInt(quantitySoldTextView.getText().toString());
                if (stockQuantity > 0) {
                     stockQuantity--;
                     quantitySold++;

                     ContentValues values = new ContentValues();
                     values.put(ProductEntry.COLUMN_PRODUCT_QTY_STOCK, stockQuantity);
                     values.put(ProductEntry.COLUMN_PRODUCT_QTY_SOLD, quantitySold);
                     Uri uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                     context.getContentResolver().update(uri, values, null, null);
                }
            }
        });
    }

    private void setTextViewColor(Context context, int qty, TextView view) {
        final int LOW_STOCK_THRESHOLD = 5;
        Resources res = context.getResources();
        if (qty <= LOW_STOCK_THRESHOLD) {
            view.setTextColor(res.getColor(android.R.color.holo_red_dark));
        } else {
            view.setTextColor(res.getColor(R.color.colorAccent));
        }
    }
}
