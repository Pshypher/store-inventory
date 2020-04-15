package com.example.android.ud809_storeinventory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.ud809_storeinventory.data.ProductContract.ProductEntry;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class ProductEditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {


    private TextInputEditText mNameField;
    private ImageView mProductImageView;
    private TextInputEditText mPriceField;
    private TextInputEditText mStockQuantityField;
    private TextInputEditText mQuantitySoldField;
    private TextInputEditText mSupplierField;
    private TextInputEditText mEmailField;

    private Uri mCurrentProductUri;
    private Uri mProductImageUri;

    private boolean mProductHasChanged = false;
    private View.OnTouchListener mOnTouchListener =
            new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    mProductHasChanged = true;
                    return false;
                }
            };

    private static final int REQUEST_CODE = 0;
    private static final int PRODUCT_LOADER = 1;
    private static final String LOG_TAG = ProductEditorActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);


        mNameField = (TextInputEditText) findViewById(R.id.product_name);
        mPriceField = (TextInputEditText) findViewById(R.id.product_price);
        mProductImageView = (ImageView) findViewById(R.id.product_image);
        mStockQuantityField = (TextInputEditText) findViewById(R.id.quantity_in_stock);
        mQuantitySoldField = (TextInputEditText) findViewById(R.id.quantity_sold);
        mSupplierField = (TextInputEditText) findViewById(R.id.supplier);
        mEmailField = (TextInputEditText) findViewById(R.id.email_address);

        mNameField.setOnTouchListener(mOnTouchListener);
        mPriceField.setOnTouchListener(mOnTouchListener);
        mSupplierField.setOnTouchListener(mOnTouchListener);
        mEmailField.setOnTouchListener(mOnTouchListener);

        mCurrentProductUri = getIntent().getData();
        if (mCurrentProductUri == null) {
            getSupportActionBar().setTitle(R.string.editor_activity_title_insert_product);
        } else {
            getSupportActionBar().setTitle(R.string.editor_activity_title_edit_product);
            getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        }

        invalidateOptionsMenu();

        ImageButton increaseStockButton = (ImageButton)
                findViewById(R.id.action_add_quantity_in_stock);
        increaseStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProductHasChanged = true;

                String stockQuantity = mStockQuantityField.getText().toString();
                int currentValue = 0;
                if (!TextUtils.isEmpty(stockQuantity)) {
                    currentValue = Integer.parseInt(stockQuantity);
                }
                currentValue++;

                mStockQuantityField.setText(String.valueOf(currentValue));
            }
        });

        ImageButton decreaseStockButton = (ImageButton)
                findViewById(R.id.action_remove_quantity_in_stock);
        decreaseStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProductHasChanged = true;

                TextInputEditText stockEditText = (TextInputEditText)
                        findViewById(R.id.quantity_in_stock);
                String stockQuantity = stockEditText.getText().toString();
                int currentValue = 0;
                if (!TextUtils.isEmpty(stockQuantity)) {
                    currentValue = Integer.parseInt(stockQuantity);
                }

                if (currentValue > 0) {
                    currentValue--;
                }

                stockEditText.setText(String.valueOf(currentValue));
            }
        });

        Button uploadButton = (Button) findViewById(R.id.action_image_upload);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProductHasChanged = true;
                Intent requestImageIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                requestImageIntent.setType("image/jpg");
                startActivityForResult(requestImageIntent, REQUEST_CODE);
            }
        });

        final ImageButton orderButton = (ImageButton) findViewById(R.id.order);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = mEmailField.getText().toString().trim();
                String product = mNameField.getText().toString().trim();
                String subject = String.format("Order for %s", product);

                TextInputEditText orderQuantityField = (TextInputEditText) findViewById(R.id.amount);
                String orderString = orderQuantityField.getText().toString().trim();
                int order = Integer.parseInt(orderString);
                String body = String.format("We are placing an order for %s.\n" +
                        "Please confirm that you are able to supply a quantity of %d.\n" +
                        "\nBest regards,", product, order);

                composeEmail(address, subject, body);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_editor, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_save_product:
                saveProduct();
                return true;
            case R.id.action_delete_product:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.delete_product_dialog_msg);
                builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteProduct();
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NavUtils.navigateUpFromSameTask(ProductEditorActivity.this);
                            }
                        };
                showUnsavedDialog(discardButtonListener);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mCurrentProductUri == null) {
            menu.findItem(R.id.action_delete_product).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {

        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ProductEditorActivity.super.onBackPressed();
                    }
                };
        showUnsavedDialog(discardButtonListener);
    }

    private void showUnsavedDialog(DialogInterface.OnClickListener
                                           positiveDialogButtonListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        builder.setPositiveButton(R.string.discard, positiveDialogButtonListener);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteProduct() {

        int rowsDeleted = getContentResolver().delete(
                mCurrentProductUri,
                null,
                null);
        if (rowsDeleted == 0) {
            Toast.makeText(this, R.string.delete_product_action_failed,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.delete_product_action_successful,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProduct() {

        String name = mNameField.getEditableText().toString().trim();
        String priceString = mPriceField.getEditableText().toString().trim();
        String availableStockString = mStockQuantityField.getEditableText().toString().trim();
        String supplier = mSupplierField.getEditableText().toString().trim();
        String email = mEmailField.getEditableText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.empty_name_field_warning,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (mProductImageUri == null && mCurrentProductUri == null) {
            Toast.makeText(this, R.string.empty_image_field_warning,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, R.string.empty_price_field_warning,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(availableStockString)) {
            Toast.makeText(this, R.string.empty_stock_qty_field_warning,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Character.isDigit(priceString.charAt(0))) {
            priceString = priceString.substring(1);
        }
        double price = Double.parseDouble(priceString);
        int availableStock = Integer.parseInt(availableStockString);

        ContentValues values = new ContentValues();

        values.put(ProductEntry.COLUMN_PRODUCT_NAME, name);
        String imagePath = BitmapUtility.getFilePath(this, mProductImageUri);
        values.put(ProductEntry.COLUMN_PRODUCT_PICTURE, imagePath);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductEntry.COLUMN_PRODUCT_QTY_STOCK, availableStock);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplier);
        values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL, email);

        if (mCurrentProductUri == null){
            Uri uri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            if (uri == null) {
                Toast.makeText(this, R.string.insert_product_action_failed,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.insert_product_action_successful,
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(
                    mCurrentProductUri,
                    values,
                    null,
                    null
            );
            if (rowsAffected == 0) {
                Toast.makeText(this, R.string.update_product_action_failed,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.update_product_action_successful,
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            mProductImageUri = data.getData();
            String imagePath = BitmapUtility.getFilePath(this, mProductImageUri);
            Bitmap bitmap = BitmapUtility.getImage(this, imagePath);
            if (bitmap != null) {
                mProductImageView.setImageResource(0);
                mProductImageView.setImageBitmap(bitmap);
            }
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = new String[] {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PICTURE,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QTY_STOCK,
                ProductEntry.COLUMN_PRODUCT_QTY_SOLD,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductEntry.COLUMN_SUPPLIER_EMAIL
        };

        return new CursorLoader(
                this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

        if (cursor.moveToFirst()) {
            int nameColumn = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int imageUriStringColumn = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PICTURE);
            int priceColumn = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int stockQuantityColumn = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QTY_STOCK);
            int quantitySoldColumn = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QTY_SOLD);
            int supplierColumn = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
            int emailColumn = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_EMAIL);

            String name = cursor.getString(nameColumn);
            String imageUriString = cursor.getString(imageUriStringColumn);
            Bitmap bitmap = BitmapUtility.getImage(this, imageUriString);
            double price = cursor.getDouble(priceColumn);
            int stock = cursor.getInt(stockQuantityColumn);
            int sold = cursor.getInt(quantitySoldColumn);
            String supplier = cursor.getString(supplierColumn);
            String email = cursor.getString(emailColumn);

            mNameField.setText(name);
            if (bitmap != null) {
                mProductImageView.setImageBitmap(bitmap);
            }
            if (imageUriString != null) {
                mProductImageUri = Uri.parse(imageUriString);
            }
            NumberFormat formatter = NumberFormat.getCurrencyInstance();
            formatter.setCurrency(Currency.getInstance(Locale.US));
            mPriceField.setText(formatter.format(price));
            mStockQuantityField.setText(String.valueOf(stock));
            mQuantitySoldField.setText(String.valueOf(sold));
            mSupplierField.setText(supplier);
            mEmailField.setText(email);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mNameField.setText(null);
        mProductImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_image_upload));
        mPriceField.setText(null);
        mStockQuantityField.setText(null);
        mQuantitySoldField.setText(null);
        mSupplierField.setText(null);
        mEmailField.setText(null);
    }

    public void composeEmail(String... args) {

        String address = args[0];
        String subject = args[1];
        String body = args[2];

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, address);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
