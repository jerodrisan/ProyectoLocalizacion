package com.jesusrodri.localizacionproyecto;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jesusrodri.localizacionproyecto.login_files.SQLiteHandler;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Creado by chukk on 22/01/2016.
 */
public class SQLiteDataCoordenadas extends SQLiteOpenHelper{


    private static final String TAG = SQLiteHandler.class.getSimpleName();
    public static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "datos_posicionamiento";
    public static final String TABLE_USER = "tabla_rutas";
    // Login Table Columns names
    private static final String KEY_ID = "unique_id";
    private static final String KEY_NUMSESION = "sesion_num";
    private static final String KEY_POINT = "point";
    private static final String KEY_LATITUD = "latitud";
    private static final String KEY_LONGITUD = "longitud";
    private static final String KEY_ALTITUD = "altitud";
    private static final String KEY_DATE = "date";



    public SQLiteDataCoordenadas(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.i("TABLA SQLITE :",TABLE_USER);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " VARCHAR (23)," + KEY_NUMSESION + " INT(10),"
                + KEY_POINT + " INTEGER PRIMARY KEY,"
                + KEY_LATITUD + " DOUBLE," + KEY_LONGITUD + " DOUBLE," + KEY_ALTITUD + " DOUBLE,"
                + KEY_DATE + " TEXT" + ")";
               // + KEY_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";

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

    public void addUser(String uid, int numsesion, double lati, double longi, double alti, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, uid);                //(0)
        values.put(KEY_NUMSESION, numsesion);   //(1)
        values.put(KEY_LATITUD, lati);          //(3)
        values.put(KEY_LONGITUD, longi);        //(4)
        values.put(KEY_ALTITUD, alti);          //(5)
        values.put(KEY_DATE, date);

        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection
        Log.d(TAG, "New user inserted into sqlite: " + id);
        Log.d("TABLA adduser:", TABLE_USER);
    }

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("uid", cursor.getString(0));
            user.put("sesion_num", cursor.getString(1));
            user.put("point", cursor.getString(2));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());
        return user;
    }


    //Get All contacts
    public Cursor getAllContacts(){

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_USER, null);

        /*Metodo 2
        return db.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_NAME,
                KEY_EMAIL}, null, null, null, null, null);
        */
        return cursor;
    }

    //Comprobar el numero de registros que tenemos con  un determinado numero de sesion:
    public int getCountSesion(String numsesion, String uid){

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER +
                " WHERE "+KEY_NUMSESION+" = "+ numsesion+" AND "+KEY_ID+" = "+"'"+uid+"'", null);
        int numRegistros = cursor.getCount();
        cursor.close();
        return numRegistros;
    }



    //Seleccion de coordenadas de un numero de sesion determinado:
    public ArrayList getCoordenadasWithSesion (String numsesion, String uid){
        ArrayList<Coord_Long_Lat> arrayCoord = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER +
                " WHERE "+KEY_NUMSESION+" = "+ numsesion + " AND "+KEY_ID+" = "+"'"+uid+"'", null);
        if(cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                Double lati = cursor.getDouble(3);
                Double longi = cursor.getDouble(4);
                Double alti = cursor.getDouble(5);
                arrayCoord.add(new Coord_Long_Lat(longi,lati,alti));
                cursor.moveToNext();
            }
        }
        //Pruebas:
        /*
        Log.i("cursor","getCount :"+String.valueOf(cursor.getCount())+ "... getColumnNames : "+cursor.getColumnNames()
                + "... getColumName(1) : "+ cursor.getColumnName(1)
                + "... getColumnCount : " + String.valueOf(cursor.getColumnCount())
        );
        */
        cursor.close();
        return arrayCoord;
    }

    public ArrayList getDatesofCoordenadasWithSesion( String numsesion, String uid){

        ArrayList<String> arrayDates = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER +
                " WHERE "+KEY_NUMSESION+" = "+ numsesion + " AND "+KEY_ID+" = "+"'"+uid+"'", null);
        if(cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String dates = cursor.getString(6);
                arrayDates.add(dates);
                cursor.moveToNext();
            }

        }
        cursor.close();
        return arrayDates;
    }


    public ArrayList<ArrayList> getDistanciasYtiempos(String numsesion, String uid){

        ArrayList<Double> distancias = new ArrayList<>();
        ArrayList<Integer> tiempos = new ArrayList<>();
        ArrayList<Coord_Long_Lat> arrayCoord = new ArrayList<>();
        ArrayList<Integer> arrayhora = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER +
                " WHERE "+KEY_NUMSESION+" = "+ numsesion+" AND "+KEY_ID+" = "+"'"+uid+"'", null);
        if(cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                //Distancias que metemos en arraylist
                Double lati = cursor.getDouble(3);
                Double longi = cursor.getDouble(4);
                Double alti = cursor.getDouble(5);
                arrayCoord.add(new Coord_Long_Lat(longi,lati,alti));


                //sacamos la hora y metemos en arraylist
                String fechayhora = cursor.getString(6);
                String[] fecha_hora = fechayhora.split("\\s+"); //dividimos fecha y hora. Interesa hora que es la parte 1
                String[] hora_min_seg = fecha_hora[1].split(":"); //Creamos un string de tamaño 3, de hora min y seg
                // Pasamos la hora a segundos
                arrayhora.add(horaToSeg(
                        Integer.valueOf(hora_min_seg[0]),
                        Integer.valueOf(hora_min_seg[1]),
                        Integer.valueOf(hora_min_seg[2])));

                cursor.moveToNext();
            }
        }
        cursor.close();
        //Sacamos el array de distancias entre puntos y de diferencia de tiempos

        distancias.add(0,0.0); //metemos en 0 como primer elemento
        tiempos.add(0,0);

        for (int i=0; i<arrayCoord.size()-1; ++i){
            distancias.add(i+1,distance(
                    arrayCoord.get(i).getLatitud(),
                    arrayCoord.get(i).getLongitud(),
                    arrayCoord.get(i+1).getLatitud(),
                    arrayCoord.get(i+1).getLongitud(),
                    "K"));

            //Antes de las 00 de la noche calculamos la diferencia de tiempos entre dos puntos
            if(arrayhora.get(i+1)>=arrayhora.get(i)){
                tiempos.add(i+1, arrayhora.get(i+1)-arrayhora.get(i));

            }else{ //En caso de llegar a las 00:00 de la noche:
                tiempos.add(i+1, arrayhora.get(i+1) + 86400 - arrayhora.get(i));
            }

        }

        ArrayList<ArrayList> arraylista = new ArrayList<>();
        arraylista.add(distancias);
        arraylista.add(tiempos);
        arraylista.add(arrayCoord);
        return arraylista;

    }


    //Datos para conocer el nombre de todas las tablas almacenadas en caso de que las hubiesemos insertado:
    //http://stackoverflow.com/questions/15383847/how-to-get-all-table-names-in-android-sqlite-database
    public ArrayList<String> gettablesNames(){
        ArrayList<String> array = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        //Cursor c = db.rawQuery("SELECT name FROM "+DATABASE_NAME+ " WHERE type='table'", null);
        if (c.moveToFirst()) {
            while ( !c.isAfterLast() ) {
                array.add(c.getString(0));
                //Toast.makeText(activityName.this, "Table Name=> "+c.getString(0), Toast.LENGTH_LONG).show();
                c.moveToNext();
            }
        }
        c.close();
        return array;
    }


    //Borrado de coordenadas asociadas a las rutas:
    public void deleteCoordenadas(String num_sesion, String uid){
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_USER,KEY_NUMSESION+" = "+num_sesion+" AND "+KEY_ID+" = "+"'"+uid+"'",null);


    }



    public double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") { //devolvemos el resultado en kilometros
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts decimal degrees to radians						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    public double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts radians to decimal degrees						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    public double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }


    public int  horaToSeg(int horas, int minutos, int segundos){
        return (horas*3600)+(minutos*60)+segundos;
    }

}
