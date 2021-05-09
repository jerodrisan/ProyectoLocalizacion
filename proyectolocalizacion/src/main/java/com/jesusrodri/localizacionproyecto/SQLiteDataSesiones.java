package com.jesusrodri.localizacionproyecto;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jesusrodri.localizacionproyecto.login_files.SQLiteHandler;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by chukk on 31/05/2017.
 */

public class SQLiteDataSesiones extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();
    public static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "datos_sesiones";
    public static final String TABLE_USER = "tabla_sesiones";
    // Login Table Columns names
    private static final String KEY_POINT = "id";
    private static final String KEY_ID = "unique_id";
    private static final String KEY_NUMSESION = "session_num";
    private static final String KEY_TIMEPAUSED = "time_paused";
    private static final String KEY_DATE = "creado_el";





    public SQLiteDataSesiones (Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_POINT + " INTEGER PRIMARY KEY,"
                + KEY_ID + " VARCHAR (23),"
                + KEY_NUMSESION + " INT(10),"
                + KEY_TIMEPAUSED + " INT(10),"
               // + KEY_DATE + "DATETIME DEFAULT (datetime('now','localtime')));";
                //+ KEY_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";
                 + KEY_DATE + " TEXT" + ")";

        db.execSQL(CREATE_LOGIN_TABLE);
        Log.d(TAG, "Database tables created");

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
        Log.i("Version de la tabla :", "oldversion :" + oldVersion + " " + "newversion :" + newVersion);

    }



    public void addSesionNum ( String uid, int numsesion, String date){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, uid);                //(1)
        values.put(KEY_NUMSESION, numsesion);   //(2)
        values.put(KEY_DATE, date);             //(3)

        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection
        Log.d(TAG, "New user inserted into sqlite: " + id);
        Log.d("TABLA adduser:", TABLE_USER);


    }


    public Cursor getAllSesions(String uid){

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_USER+" WHERE "+KEY_ID+" = "+"'"+uid+"'", null);
        return cursor;

    }

    public void deleteSesion(ArrayList<String> sesiones, Context context, String uid){

       // String [] sesions = sesiones.toArray(new String[sesiones.size()]);
        SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDataCoordenadas dataCoordenadas = new SQLiteDataCoordenadas(context);
        for(String ses: sesiones){
           db.delete(TABLE_USER,KEY_NUMSESION+" = "+ses+" AND "+KEY_ID+" = "+"'"+uid+"'",null);
           dataCoordenadas.deleteCoordenadas(ses, uid);
       }
        db.close();
    }

    public boolean is_sesion_deleted(String numsesion, String uid){

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER +
                " WHERE "+KEY_NUMSESION+" = "+ numsesion+" AND "+KEY_ID+" = "+"'"+uid+"'", null);
        int numRegistros = cursor.getCount();
        cursor.close();
        if(numRegistros==0){
            return true;
        }else{
            return false;
        }
    }



    public int  getTimePaused(String numses, String uid){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT "+  KEY_TIMEPAUSED + " FROM "+TABLE_USER +
                " WHERE "+ KEY_NUMSESION +" = "+numses+" AND "+KEY_ID+" = "+"'"+uid+"'", null);
        int time_paused=0;
        if(cursor.getCount()>0) {
            cursor.moveToFirst();
            time_paused = cursor.getInt(cursor.getColumnIndex(KEY_TIMEPAUSED));
        }
        return time_paused;
    }


    public void insertTimePaused (String numsesion, String timepaused, String uid){

        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TIMEPAUSED, timepaused);
        db.update(TABLE_USER,values,KEY_NUMSESION+" = "+numsesion+" AND "+KEY_ID+" = "+"'"+uid+"'",null);
        db.close();

    }


    public void deleteTabla(){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("delete from "+ TABLE_USER);
    }

}






