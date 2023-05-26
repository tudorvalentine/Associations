package com.tudorvalentine.augmentedimages.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.tudorvalentine.augmentedimages.associationslist.Association;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLiteHandler extends SQLiteOpenHelper {
    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "user_database";

    // Login table name
    private static final String TABLE_USER = "user";
    private static final String TABLE_ASSOC = "associations";
    // user table fields
    private static final String KEY_ID_USER = "id_user";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    //associations table fields
    private static final String KEY_ID_ASSOC = "id_assoc";
    private static final String KEY_IMAGE_NAME = "image_name";
    private static final String KEY_DOCUMENT_NAME = "document_name";
    private static final String KEY_DOCUMENT_PREVIEW = "document_preview";

    public SQLiteHandler(Context context){
        super(context,DATABASE_NAME,null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE_LOGIN = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID_USER + " INTEGER PRIMARY KEY," + KEY_USERNAME
                + " TEXT UNIQUE," + KEY_EMAIL + " TEXT UNIQUE )";
        String CREATE_TABLE_ASSOC = "CREATE TABLE " +
                TABLE_ASSOC + "(" +
                KEY_ID_ASSOC +" INTEGER PRIMARY KEY,"+
                KEY_IMAGE_NAME +" TEXT, " + KEY_DOCUMENT_NAME +" TEXT," + KEY_DOCUMENT_PREVIEW + " TEXT" + ")";
        sqLiteDatabase.execSQL(CREATE_TABLE_ASSOC);
        sqLiteDatabase.execSQL(CREATE_TABLE_LOGIN);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSOC);
        onCreate(sqLiteDatabase);
    }
    public void addUser(int id_user,String username, String email){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID_USER, id_user);//id
        values.put(KEY_USERNAME, username); // Name
        values.put(KEY_EMAIL, email); // Email
        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into sqlite: " + id);
    }
    public void addAssociation(Association association){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID_ASSOC, association.getId());
        values.put(KEY_IMAGE_NAME, association.getImage_name());
        values.put(KEY_DOCUMENT_NAME, association.getDoc_name());
        values.put(KEY_DOCUMENT_PREVIEW, association.getDoc_prev());
        long id = db.insert(TABLE_ASSOC, null, values);

        db.close();

        Log.d(TAG, "Data user inserted into SQLite: " + id);

    }
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("id", cursor.getString(0));
            user.put("username", cursor.getString(1));
            user.put("email", cursor.getString(2));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    public List<Map<String, String>> getUserData(){

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_ASSOC;

        Cursor cursor = db.rawQuery(selectQuery,null);

        List<Map<String, String>> data = new ArrayList<>();

        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            Map<String,String> map = new HashMap<>();
            map.put("id_assoc", cursor.getString(0));
            map.put("image", cursor.getString(1));
            map.put("document", cursor.getString(2));
            map.put("document_prev", cursor.getString(3));
            data.add(i, map);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        return data;
    }
    public String getDocument(String image_name_recognized){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT " + KEY_DOCUMENT_NAME + " FROM " + TABLE_ASSOC + " WHERE " + KEY_IMAGE_NAME + "= ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{image_name_recognized});
        cursor.moveToFirst();
        return cursor.getString(0);
    }

    public int countRowsAssoc(){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_ASSOC;
        Cursor cursor = db.rawQuery(selectQuery,null);
        int countedRows = cursor.getCount();
        cursor.close();
        return countedRows;
    }
    public int deleteRowsAssoc(){
        SQLiteDatabase db = this.getWritableDatabase();
        int rowDeleted = db.delete(TABLE_ASSOC, null, null);
        db.close();
        Log.d(TAG, "Deleted all rows from table " + TABLE_ASSOC);
        return rowDeleted;
    }
    public int deleteRowAssoc(String image_name){
        SQLiteDatabase db = this.getWritableDatabase();
        int rowDeleted = db.delete(TABLE_ASSOC, "image_name=?", new String[]{image_name});
        Log.d(TAG, "Deleted row from table" + TABLE_ASSOC);
        db.close();
        return rowDeleted;
    }
    public void deleteUserAndData() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.delete(TABLE_ASSOC,null,null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }
}
