package com.jesusrodri.localizacionproyecto.login_files;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by chukk on 31/12/2015.
 */
public class SessionManager {
    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();
    // Shared Preferences
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE = 0; // Shared pref mode
    private static final String PREF_NAME = "AndroidHiveLogin"; // Shared preferences file name
    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
    private static final String KEY_IS_INBACKGROUND = "isInBackground";
    private static final String KEY_LASTUSER="lastUser";
    private static final String KEY_LASTPASSWORD="lastPassword";
    private static final String KEY_ISLOCALIZABLE = "isLocalizable";
   // private static final String KEY_RUTA_NUMBER = "rutaNumber";
    private static final String KEY_TIMEINBACKGROUD = "timeinbackground";
    private static final String KEY_FRECUENCIA_GPS = " frecuencia_gps";
    private static final String KEY_NUMSESION_DB = "numsesion_db";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
    public void setLogin(boolean isLoggedIn, String lastUser, String lastPasword, boolean isinBackground) { //donde lastUser es el email
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        editor.putString(KEY_LASTUSER,lastUser);
        editor.putString(KEY_LASTPASSWORD,lastPasword);
        editor.putBoolean(KEY_IS_INBACKGROUND, isinBackground);
        editor.commit();
       // Log.d(TAG, "User login session modified!");
    }
    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }
    public boolean isInBackGround(){return pref.getBoolean(KEY_IS_INBACKGROUND, false);}


    public String getLastUser(){
        return pref.getString(KEY_LASTUSER," ");
    }
    public String getKeyLastpassword(){return pref.getString(KEY_LASTPASSWORD,"not foud");}

    public void set_time_in_background(long time_in_background){
        editor.putLong(KEY_TIMEINBACKGROUD, time_in_background);
        editor.commit();
    }
    public long get_time_in_background(){
        return pref.getLong(KEY_TIMEINBACKGROUD,0);
    }

    public void set_isLocalizable(boolean isLocalizable){
        editor.putBoolean(KEY_ISLOCALIZABLE, isLocalizable);
        editor.commit();
    }
    public boolean get_isLocalizable(){
        return pref.getBoolean(KEY_ISLOCALIZABLE,true);
    }

    public void set_frec_gps (float frec_gps){
        editor.putFloat(KEY_FRECUENCIA_GPS, frec_gps);
        editor.commit();
    }

    public float get_fre_gps(){
        return pref.getFloat(KEY_FRECUENCIA_GPS,1);
    }


    public void set_numsesion_db( int numsesion_db){
        editor.putInt(KEY_NUMSESION_DB, numsesion_db+1); //subimos 1 el numero de sesion cada vez que grabemos
        editor.commit();
    }
    public int get_numsesion_db(){
        return pref.getInt(KEY_NUMSESION_DB,1 );
    }
}
