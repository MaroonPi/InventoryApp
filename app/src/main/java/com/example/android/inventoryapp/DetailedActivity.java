package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import java.io.FileDescriptor;
import java.io.IOException;

import static com.example.android.inventoryapp.R.id.productimage;
import static com.example.android.inventoryapp.R.id.productname;
import static com.example.android.inventoryapp.R.id.productprice;
import static com.example.android.inventoryapp.R.id.productsupplier;
import static com.example.android.inventoryapp.data.ProductProvider.LOG_TAG;

public class DetailedActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri currentUri;

    TextView productname;
    TextView productsupplier;
    TextView productprice;
    ImageView productimage;
    Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);

        Intent intent = getIntent();
        currentUri = intent.getData();

        productname = (TextView)findViewById(R.id.productname);
        productsupplier = (TextView)findViewById(R.id.productsupplier);
        productprice = (TextView)findViewById(R.id.productprice);
        productimage = (ImageView)findViewById(R.id.productimage);
        getLoaderManager().initLoader(0,null,this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (currentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    public void updateProduct(){

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_IMAGE
        };
        return new CursorLoader(this,currentUri,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor.moveToFirst()) {
            // Find the columns of attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String image = cursor.getString(imageColumnIndex);
            if (currentUri != null) {
                productname.setText(name);
                productsupplier.setText(supplier);
                productprice.setText(price);
                setImage(image);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void setImage(String imageString){
        Uri imageUri = Uri.parse(imageString);
        String[] filePathColumn = { MediaStore.Images.Media.DATA };

        Cursor cursor = getContentResolver().query(imageUri,
                filePathColumn, null, null, null);
        cursor.moveToFirst();
        cursor.close();
        try {
            imageBitmap = getBitmapFromUri(imageUri);
        } catch (IOException e) {
            Log.e(LOG_TAG,"Invalid data",e);
        }
        productimage.setImageBitmap(imageBitmap);
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

}
