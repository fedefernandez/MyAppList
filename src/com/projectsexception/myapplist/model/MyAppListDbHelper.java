package com.projectsexception.myapplist.model;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class MyAppListDbHelper extends SQLiteOpenHelper {
    
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "MyAppList.db";
    
    public static abstract class AppInfoIgnored implements BaseColumns {
        // Prevents the class from being instantiated.
        private AppInfoIgnored() { }
        
        public static final String TABLE_NAME = "app_info_ignored";
        public static final String COLUMN_PACKAGE = "package";
        
    }
    
    private static final String TEXT_TYPE = " TEXT";
    private static final String SQL_CREATE =
        "CREATE TABLE " + AppInfoIgnored.TABLE_NAME 
            + " (" 
                + AppInfoIgnored._ID + " INTEGER PRIMARY KEY," 
                + AppInfoIgnored.COLUMN_PACKAGE + TEXT_TYPE 
            + " )";

    public MyAppListDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_CREATE);
    }
    
    public List<String> getPackages() {
        List<String> packages = new ArrayList<String>();
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = { AppInfoIgnored.COLUMN_PACKAGE };
        String sortOrder = AppInfoIgnored.COLUMN_PACKAGE + " ASC";    
        Cursor c = db.query(AppInfoIgnored.TABLE_NAME, projection, null,null, null, null, sortOrder);
        if (c != null) {
            while (c.moveToNext()) {
                packages.add(c.getString(0));
            }
            c.close();
        }
        return packages;
    }
    
    public void savePackages(List<String> packages) {
        if (packages != null && !packages.isEmpty()) {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(AppInfoIgnored.TABLE_NAME, null, null);
            ContentValues values = new ContentValues();
            for (String packageName : packages) {                
                values.put(AppInfoIgnored.COLUMN_PACKAGE, packageName);
                db.insert(AppInfoIgnored.TABLE_NAME, null, values);
            }
        }
    }

}
