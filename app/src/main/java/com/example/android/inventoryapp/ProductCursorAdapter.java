package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;


public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context,Cursor c){
        super(context,c,0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(
                R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView summary = (TextView) view.findViewById(R.id.summary);
        // Extract properties from cursor
        String body = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_NAME));
        String priority = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_SUPPLIER));
        // Populate fields with extracted properties
        name.setText(body);
        summary.setText(String.valueOf(priority));
    }
}

