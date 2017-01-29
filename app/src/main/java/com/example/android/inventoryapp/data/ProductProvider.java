package com.example.android.inventoryapp.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static com.example.android.inventoryapp.data.ProductContract.ProductEntry;
import static com.example.android.inventoryapp.data.ProductContract.ProductEntry.CONTENT_LIST_TYPE;


public class ProductProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ProductProvider.class.getSimpleName();

    private static final int PRODUCTS = 100;
    private static final int PRODUCTS_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCTS_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    ProductDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new ProductDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor cursor;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS: {
                cursor = db.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case PRODUCTS_ID: {
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
    }

    public Uri insertProduct(Uri uri, ContentValues contentValues) {
        String name = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        String supplier = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
        if (supplier==null){
            throw new IllegalArgumentException("Product requires a supplier");
        }

        String image = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_IMAGE);
        if(image==null){
            throw new IllegalArgumentException("Product requires an image");
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long newRowId = db.insert(ProductEntry.TABLE_NAME, null, contentValues);
        if (newRowId == -1) {
            Toast.makeText(getContext(), "Error with saving product",
                    Toast.LENGTH_LONG).show();
            return null;
        } else {
            String message = "Product saved with id: " + newRowId;
            Toast.makeText(getContext(), message,
                    Toast.LENGTH_LONG).show();
            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, newRowId);
        }
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCTS_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        String supplier = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
        if (supplier==null){
            throw new IllegalArgumentException("Product requires a supplier");
        }

        String image = values.getAsString(ProductEntry.COLUMN_PRODUCT_IMAGE);
        if(image==null){
            throw new IllegalArgumentException("Product requires an image");
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (values.size() == 0) {
            return 0;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return db.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                getContext().getContentResolver().notifyChange(uri, null);
                return database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
            case PRODUCTS_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                getContext().getContentResolver().notifyChange(uri, null);
                return database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCTS_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
