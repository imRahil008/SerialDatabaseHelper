package com.example.e00944.serialdatabaseloader;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "contactsManager";

    // Contacts table name
    public static final String TABLE_CONTACTS = "contacts";
    private SerialDatabaseHelper helper;

    // Contacts Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_PH_NO = "phone_number";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        helper = new SerialDatabaseHelper("MyDBHandlerThread");
        helper.setDataBase(this.getWritableDatabase());
    }

    public void setListener(SerialDatabaseHelper.OnDatabaseOperationListener dbUpdateListener){
        helper.setDbUpdateListener(dbUpdateListener);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_PH_NO + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        // Create tables again
        onCreate(db);
    }

    // Adding new contact
    void addContact(String name, String phoneNumber) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Contact Name
        values.put(KEY_PH_NO, phoneNumber); // Contact Phone

        helper.insert(TABLE_CONTACTS, values);
    }


    void getContact(){
        String sql = "SELECT * FROM "+ TABLE_CONTACTS;
        String[] val = new String[]{};
        helper.query(sql, val);
    }


    public void close(){
        if(helper != null){
            helper.close();
        }
    }


}