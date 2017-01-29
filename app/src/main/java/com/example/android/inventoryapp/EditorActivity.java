package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;


import com.example.android.inventoryapp.data.ProductContract;
import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import java.io.FileDescriptor;
import java.io.IOException;


import static com.example.android.inventoryapp.data.ProductProvider.LOG_TAG;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity{

    EditText mNameEditText;
    EditText mSupplierEditText;
    EditText mPriceEditText;
    Button addPicture;

    private Uri currentUri;

    private int RESULT_LOAD_IMAGE = 1;

    private Uri iUri;
    private Bitmap iBitmap;

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Intent intent = getIntent();
        currentUri = intent.getData();

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mSupplierEditText = (EditText) findViewById(R.id.edit_product_supplier);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        imageView = (ImageView)findViewById(R.id.selectedimage);
        addPicture = (Button)findViewById(R.id.addimage);
        addPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageSelector();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    public void saveProduct(){
        String name = mNameEditText.getText().toString().trim();
        String supplier = mSupplierEditText.getText().toString().trim();
        String strPrice= mPriceEditText.getText().toString().trim();
        int price = 0;
        if (currentUri == null &&
                TextUtils.isEmpty(name) && TextUtils.isEmpty(supplier) &&
                TextUtils.isEmpty(strPrice) ) {return;}
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME,name);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER,supplier);
        if (!TextUtils.isEmpty(strPrice)) {
            price = Integer.parseInt(strPrice);
        }
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        if(currentUri==null) {
            currentUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveProduct();
                // Do nothing for now
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (currentUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(currentUri, null, null);
            if (rowsDeleted == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, "Pet Deletion Failed",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, "Pet Deletion Successful",
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }



    public void imageSelector() {
        Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != resultData) {
            iUri = resultData.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(iUri,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            cursor.close();
            try {
                iBitmap = getBitmapFromUri(iUri);
            } catch (IOException e) {
                Log.e(LOG_TAG,"Invalid data",e);
            }
            imageView.setImageBitmap(iBitmap);
        }
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