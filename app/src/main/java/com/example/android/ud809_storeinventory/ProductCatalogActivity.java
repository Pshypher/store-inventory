package com.example.android.ud809_storeinventory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.android.ud809_storeinventory.data.ProductContract.ProductEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProductCatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private ProductCursorAdapter mAdapter;
    private AdapterView.OnItemClickListener mOnItemClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(ProductCatalogActivity.this,
                            ProductEditorActivity.class);
                    intent.setData(ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id));
                    startActivity(intent);
                }
            };

    private static final int PRODUCTS_LOADER = 0;
    private static final int REQUEST_READ_IMAGE_PERMISSION = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        getSupportLoaderManager().initLoader(PRODUCTS_LOADER, null, this);

        mAdapter = new ProductCursorAdapter(this, null);

        GridView gridView = (GridView) findViewById(R.id.grid_view);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(mOnItemClickListener);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductCatalogActivity.this,
                        ProductEditorActivity.class);
                startActivity(intent);
            }
        });

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE },
                    REQUEST_READ_IMAGE_PERMISSION);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_catalog, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_add_dummy_product:
                insertRandomProduct();
                return true;
            case R.id.action_delete_all_products:
                getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void insertRandomProduct() {

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Vitra Panton Chairs");
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 316.59);
        values.put(ProductEntry.COLUMN_PRODUCT_QTY_STOCK, 49);
        values.put(ProductEntry.COLUMN_PRODUCT_QTY_SOLD, 361);
        values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL, "paulmoret@furniture.wood");
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, "Paul Moret");

        Uri uri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
        if (uri == null) {
            Toast.makeText(this, R.string.add_random_product_action_failed,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.add_random_product_action_successful,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = new String[] {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_PICTURE,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QTY_STOCK,
                ProductEntry.COLUMN_PRODUCT_QTY_SOLD
        };

        return new CursorLoader(
                this,
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);

        GridView gridView = (GridView) findViewById(R.id.grid_view);
        View emptyView = findViewById(R.id.empty_inventory_view);
        gridView.setEmptyView(emptyView);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
