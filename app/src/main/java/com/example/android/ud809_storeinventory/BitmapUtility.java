package com.example.android.ud809_storeinventory;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;

public class BitmapUtility {


    private static final String LOG_TAG = BitmapUtility.class.getSimpleName();

    public static String getFilePath(Context context, Uri imageUri) {

        if (imageUri == null) { return null; }

        String[] projection = new String[] {
                MediaStore.Images.Media.DATA
        };

        Cursor cursor = context.getContentResolver().query(
                imageUri,
                projection,
                null,
                null,
                null
        );

        String absolutePath = null;
        if (cursor == null) {
            absolutePath = imageUri.getPath();
        } else {
            if (cursor.moveToFirst()) {
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                absolutePath = cursor.getString(dataColumn);
            }
            cursor.close();
        }

        return absolutePath;
    }

    // convert from Uri to bitmap
    public static Bitmap getImage(Context context, String imagePath) {

        if (TextUtils.isEmpty(imagePath)) {
            return null;
        }

        File file = new File(imagePath);
        Bitmap bitmap = null;
        if(file.exists()) {
            // load image in your imageView
            bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        }

        return bitmap;
    }
}
