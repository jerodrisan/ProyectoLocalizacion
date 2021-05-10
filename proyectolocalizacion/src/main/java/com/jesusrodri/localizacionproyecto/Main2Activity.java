package com.jesusrodri.localizacionproyecto;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
//import com.example.android_localizacionproyecto.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jesusrodri.localizacionproyecto.interfaces.OnActivityInteractionListener;
import com.jesusrodri.localizacionproyecto.interfaces.OnActivityInteractionListener2;
import com.jesusrodri.localizacionproyecto.interfaces.OnFragmentInteractionListener;
import com.jesusrodri.localizacionproyecto.login_files.AlertDialogo;
import com.jesusrodri.localizacionproyecto.login_files.AppConfig;
import com.jesusrodri.localizacionproyecto.login_files.LoginActivity;
import com.jesusrodri.localizacionproyecto.login_files.SessionManager;
import com.jesusrodri.localizacionproyecto.utils.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class Main2Activity extends AppCompatActivity implements OnFragmentInteractionListener {

    public Toolbar toolbarContactos, toolbarRuta;
    public ViewPager viewPager;
    public MiFragmentPagerAdapter fragPagerAdap;
    public OnActivityInteractionListener listener;
    public OnActivityInteractionListener2 listener2;
    private SQLiteDataCoordenadas dbCoord;
    private ProgressDialog pDialog;
    private Spinner cmbToolbar, cmbToolbarContactos;

    //private ArrayList<Clase_Ruta_Dia> arrayClaseRutaDia;
    private ArrayList<String> arrayFechaYSesion;
    private ArrayAdapter<String> adapter;

    private String id_cliente, email_cliente, name_cliente, pass;
    private int lastItemSpinner = 1;

    private ProgressBar pbarProgreso;

    private DatabaseReference mFirebaseDatabase;
    private ArrayList<String> selectedContactsString, selectedContatsToDelete;
    private SQLiteDataCoordenadas database_coorde;
    private SQLiteDataSesiones database_sesiones;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        /*
        String tap_value_notification = getIntent().getStringExtra("tap_action");
        if (tap_value_notification != null) {
            if(tap_value_notification.equals("tap_accion")){
               //Main2Activity.super.onResume();

            }
        */
        //} else {


        pDialog = new ProgressDialog(this);
        //Appbar2 relativo al Fragment_localizacion

        //Appbar relativa al Fragment_Mapa
        toolbarRuta = (Toolbar) findViewById(R.id.appbarRuta);
        setSupportActionBar(toolbarRuta);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarRuta.setVisibility(View.GONE);

        toolbarContactos = (Toolbar) findViewById(R.id.appbarContactos);
        setSupportActionBar(toolbarContactos);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //toolbarContactos.setVisibility(View.GONE);
        //inicializacion base de datos:
        database_sesiones = new SQLiteDataSesiones(this);
        database_coorde = new SQLiteDataCoordenadas(this);


        fragPagerAdap = new MiFragmentPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(fragPagerAdap);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        toolbarContactos.setVisibility(View.VISIBLE);
                        toolbarRuta.setVisibility(View.GONE);
                        break;
                    case 1:
                        toolbarContactos.setVisibility(View.GONE);
                        toolbarRuta.setVisibility(View.VISIBLE);
                        // Log.i("estados1 ", String.valueOf(botonActivar) + " " + String.valueOf(amigoEnMovimiento));
                        listener.PassingStateUsuarioYAmigo(botonActivar, amigoEnMovimiento);
                        listener.passingIdEmailNametoMap(id_cliente, email_cliente, name_cliente);

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.appbartabs);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(viewPager);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            id_cliente = bundle.getString("uid");
            email_cliente = bundle.getString("email");
            name_cliente = bundle.getString("name");
            pass = bundle.getString("pass");
            //Log.i("datoscliente ", id_cliente + " " + email_cliente + " " + name_cliente+ " "+pass);
            //listener2.passingIdEmailNametoFragment(id_cliente,email_cliente, name_cliente);

        }
        //Generamos token de Firebase para enviar las solicitudes a los contactos, Ver : https://www.youtube.com/watch?v=LiKCEa5_Cs8 (Deprecado no funciona)
        //generarTokenFirebase(this);

        //Generamos Spinner
        generarSpinnerContactos(this, email_cliente);
        generarSpinner(id_cliente, true, 0, 1); //ponemos el primer elemento del spinner al arrancar

        //Pillamos la referencia a la base de datos al nodo notificationRequests (lo creamos directamente nombrandolo) para enviar solicitudes a contactos.
        FirebaseDatabase mFirebaseInstance;
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("SolicitudesAmistad");
        buscarSolicitudesFirebase();

        //añadimos coment
        //segundo comment
    }


    private void generarTokenFirebase(final Context contexto){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("token failed", "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        // Get new FCM registration token
                        final String token = task.getResult();
                        //Log.i("token email", token+' '+email_cliente);
                        Volley_Contactos volley_contactos = new Volley_Contactos (contexto);
                        volley_contactos.sendRegistrationToken(email_cliente, token);
                        //Toast.makeText(getApplicationContext(), "token desde main "+token, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    Timer timerTostada;
    @Override
    protected void onPause() { //Ver archico MyApplication
        super.onPause();
        MyApplication.activityPaused();
        //Log.i("back onpause",String.valueOf(MyApplication.isActivityVisible()));
        if(!isservice_stopped) { //Solo lo ejecutamos si esta activado el servicio
            timerTostada = new Timer();
            setTostadainBack(timerTostada, MyApplication.isActivityVisible());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.activityResumed();
        //Log.i("back onresume ",String.valueOf(MyApplication.isActivityVisible()));
        if(timerTostada!=null)  //No esta activado el servicio de localizacion, cancelamos entonces
            setTostadainBack(timerTostada, MyApplication.isActivityVisible());

    }

    //Creamos una tostada que avise cada cierto tiempo en caso de si el servicio esta en segundo plano.
    //Si esta en primer plano, lo desactivamos
    public void setTostadainBack(Timer timer, boolean isActivityVisible){
        if(!isActivityVisible){
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Main2Activity.this.runOnUiThread(new Runnable() { //Ejecutamos la tostada desde el hilo principal
                        @Override
                        public void run() {
                            Toast.makeText(Main2Activity.this, "ejecutando servicio de localizacion",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            },0, 180000); // Lo ejecutamos cada 3 minutos
        }else{
            timer.cancel(); //Cancelamos si la app es visible  el no esta activado el servido de localizacion
        }
    }

    public void generarSpinnerContactos(Context contexto, String email_cliente){

        ArrayList<String> arrayContacts = new ArrayList<>();
        arrayContacts.add("Seleccione Contacto");
        //Recuperamos listado a traves de voley
        Volley_Contactos volley_contactos = new Volley_Contactos(this);
        volley_contactos.getContactList(arrayContacts, email_cliente);
        //Generamos el spinner:
        Spinner cmbToolbarContactos = (Spinner)findViewById(R.id.espinerContactos);
        ArrayAdapter adapter = new ArrayAdapter<>(
                getSupportActionBar().getThemedContext(),
                R.layout.appbar_filter_title, arrayContacts);
        adapter.setDropDownViewResource(R.layout.appbar_filter_list);
        cmbToolbarContactos.setAdapter(adapter);
        cmbToolbarContactos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            public void resetSpinnnerContactos(){
                Main2Activity.this.amigoEnMovimiento=false; // ponemos la variable en false
                listener2.activarLayoutContacto(false, null,null, null, null); //Ocultamos el Layout del amigo
                listener.PassingBorrarRutaAmigo(true); //Borramos la ruta dibujada para el amigo
                // volvemos a activar el spinner para ver las rutas que queramos, solo si el usuario no esta usando la localizacion
                if(!botonActivar){
                    onPassingSpinnerStateToMain(true, false); // si no se  pulso el boton activar activamos el spinner para ver mapas.
                }
                if(timer!=null){
                    timer.cancel();
                }
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                int itemSelected = parent.getSelectedItemPosition();
                if(itemSelected>0){
                    String Amigo = parent.getSelectedItem().toString();
                    String[] parts = Amigo.split("\n"); String nombre = parts[0].replaceAll("\\s","") ; String correo =parts[1].replaceAll("\\s","");
                    if(timer!=null){
                        resetSpinnnerContactos();
                        timer = new Timer();
                    }else{
                        timer = new Timer();
                    }
                    scheduleFixed(correo, nombre);
                    onPassingSpinnerStateToMain(false,false); //desactivamos el spinner y borramos las rutas del usuario
                    listener.PassingZoomLevelToMap(16); //pasamos el nivel de zoom que queramos al mapa cuando seleccionamos el contacto
                    listener.passingNameAmigotoMap(nombre); //enviamos el nombre del amigo al mapa, (Si queremos enviar el correo enviamos el correo)

                }else{
                    resetSpinnnerContactos();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    //private Handler handler =  new Handler();
    private Timer timer;

    private void scheduleFixed( final String correo, final String nombre){
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Main2Activity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getCoordenadasAmigo(correo, nombre);
                    }
                });
            }
        }, 0, 12000);
    }

    public boolean amigoEnMovimiento=false;
    public void getCoordenadasAmigo(final String correo, final String nombre){
        // Tag used to cancel the request
        final String param_server="getCoordenadas_amigo";
        String tag_string_req = "req_register";
        //pDialog.setMessage("Recuperando ...");
        //showDialog();
        //String str1 = "?param=" + param_server + "&correo=" + correo;
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PARAM_COORDENADAS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                //hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    final String mensa = jObj.getString("mensaje");
                    if (!error) {
                        Main2Activity.this.amigoEnMovimiento=true;
                        ArrayList<Coord_Long_Lat> arrayCoord = new ArrayList<>();
                        //http://stackoverflow.com/questions/31855390/android-json-parse-getting-array-within-array
                        //Ver como esta estructurado el Json que viene del servidor en la extesion de crome api Rest
                        JSONArray jsonArrayReg = jObj.getJSONArray("registros");

                        for (int i = 0; i < jsonArrayReg.length(); ++i) {

                            JSONObject object = jsonArrayReg.getJSONObject(i);
                            Double lati = object.getDouble("latitud");
                            Double longi = object.getDouble("longitud");
                            Double alti = object.getDouble(("altitud"));
                            arrayCoord.add(new Coord_Long_Lat(longi, lati, alti));
                        }
                        //Obtenemos los tiempos y las distancias que ya vienen en array en el json:
                        JSONArray arrayTiemp = jObj.getJSONArray("tiempos");
                        JSONArray arrayDist = jObj.getJSONArray("distancias");
                        ArrayList<Integer> arrayTiempos = new ArrayList<>();
                        ArrayList<Double> arrayDistancias = new ArrayList<>();
                        for (int i=0; i<jsonArrayReg.length(); ++i){
                            int tiempos = arrayTiemp.getJSONArray(0).getInt(i);
                            arrayTiempos.add(tiempos);
                            double distancias = arrayDist.getJSONArray(0).getDouble(i);
                            arrayDistancias.add(distancias);
                        }
                        listener2.activarLayoutContacto(true, correo, nombre, arrayTiempos, arrayDistancias);
                        listener.PassingRutaAmigoToMapa(arrayCoord, arrayTiempos, arrayDistancias);


                    } else {
                        Main2Activity.this.amigoEnMovimiento=false;
                        Toast.makeText(getApplicationContext(), mensa, Toast.LENGTH_LONG).show();
                        timer.cancel();
                        generarSpinnerContactos(Main2Activity.this, email_cliente);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                //hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                //Con linkedHashMap ordenamos por orden de insercion
                Map<String, String> params = new LinkedHashMap<>();
                params.put("param",param_server);
                params.put("correo",correo);
                return params;
            }
        };
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //Cuando pulsamos el boton de ocultar en fragmenLocation , reseteamos el spinner de contactos.
    @Override
    public void onPassingResetSpinnerContactos() {
       // Log.i("reset spiner ", "si");
        this.amigoEnMovimiento=false;
        generarSpinnerContactos(this, email_cliente);
    }

    //Ocultamos el Layout del amigo.
    @Override
    public void onPassingOcultarAmigo(boolean var) {
        this.amigoEnMovimiento=false;
        if(var){
            listener.PassingBorrarRutaAmigo(true); //Borramos la ruta dibujada para el amigo
            if(timer!=null){
                timer.cancel();
            }
        }
    }

//-------------------
// --------------------

    //Recibimos datos de fragment_Localizacion y se los enviamos a FragmentMapa
    @Override
    public void onPassingDataToMapa(double lng, double lat, double alt, double vel, double dist) {
        listener.PassingDataToMapa(lng, lat, alt, vel, dist);
    }

    //Recibimos el estado del boton activar desde fragmantLocation:
    public boolean botonActivar=false;
    @Override
    public void onPassingActivarBotonn(boolean state) {
        this.botonActivar = state;
        listener.PassingZoomLevelToMap(16); //pasamos el zoom que queremos a fragment mapa
        listener.Passing_BotonStart_Estado(state);
    }

    //Recibimos informacion de si esta el servicio parado o activado
    public boolean isservice_stopped = true;
    @Override
    public void isServiceStopped(boolean state) {
        this.isservice_stopped=state;
    }

    //Si activamos la localizacion desde fragment localizacioin desactivamos el spinner de mapas y cuando la desactivamos activaos el spinner de mapas
    @Override
    public void onPassingSpinnerStateToMain(boolean enableSpinner, boolean lastElement) {
        if (enableSpinner && lastElement) { //pulsamosel boton de parar localizacion y activamos el spinner , actualizamos la base de datos y actualiamos el spinner

            listener.PassingBorrarSecuenciaEnMapa(true);     //Borramos las polilineas secuenciales en el mapa
            generarSpinner(id_cliente, enableSpinner, lastItemSpinner,0);  //Ponemos el ultimo elemento

        } else if (!enableSpinner && !lastElement){
            //Si activamos el el boton para iniciar la localizacion,desactivamos el spinner entonces hay que borrar los mapas dibujados y volver a
            // cargar el spinner de cero
            listener.PassingBorrarRutasEnMapa(true);
            generarSpinner(id_cliente, enableSpinner, 0,0); // ponemos el elemento 0 del spinner

        }else if (enableSpinner && !lastElement){ //caso de ocultar el amigo o bien pulsar el primer elemento de contactos
            //En este caso activamos el espinner, borramos rutas pero ponemos el primer elemento del spinner
            listener.PassingBorrarRutasEnMapa(true);
            generarSpinner(id_cliente, enableSpinner, 0,0); // ponemos el elemento 0 del spinner
        }

    }

    //Pillamos el id y correo del cliente al abrir la aplicacion para obtener las rutas y cargarlas en el spinner
    @Override
    public void onRequiringClienteFromToMain() {
        // getRutas(id);
        listener2.passingIdEmailNametoFragment(id_cliente,email_cliente, name_cliente);
        /*
        generarSpinnerContactos(this, email );
        generarSpinner(id, true, 0); //ponemos el primer elemento del spinner al arrancar
        this.id_cliente = id;
        this.email_cliente = email;
        */
    }

    //Recibimos la orden desde FragmentMpara para borar La ruta seleccionada. (Ya no lo  usaremos)
    @Override
    public void OnPassingBorrarRuta(ArrayList<String> ses_num) {
        // Toast.makeText(Main2Activity.this, "num ruta "+ses_num, Toast.LENGTH_SHORT).show();
       // deleteRuta(id_cliente, ses_num);
    }

    //Generacion del spinner que muestran las rutas
    private void generarSpinner(String id, boolean spinerState, int itemSpinner, int deleteLive) { //deleteLive=1 o deleteLive=0
       // Log.i("reset spinner2 ","si");
        ArrayList arrayFechaYSesion = new ArrayList<>();
        arrayFechaYSesion.add("Tus Rutas :");
        //En caso de usar las rubas almacenadas en el servidor:
        //getRutas(id, arrayFechaYSesion,deleteLive); //deleteLive =1 --> Al arrancar la app comprobamos que no haya ningun live=1 . Ver PHP
        //Encaso de usar base de datos interna sqlite. Ponemos este bloque:

        if(deleteLive==1){
            deleteSesionLive1(id,deleteLive); //cambiamos el live a cero en caso de que este a 1 al arrancar el programa y:
            getRutasDB(arrayFechaYSesion); //obtenemos las rutas desde la base de datos y la mostramos
        }else{
            getRutasDB(arrayFechaYSesion); //obtenemos las rutas desde la base de datos y la mostramos
        }

        Spinner cmbToolbar = (Spinner) findViewById(R.id.espinerRuta);
        ArrayAdapter adapter = new ArrayAdapter<>(
                getSupportActionBar().getThemedContext(),
                R.layout.appbar_filter_title, arrayFechaYSesion);

        adapter.setDropDownViewResource(R.layout.appbar_filter_list);
        cmbToolbar.setAdapter(adapter);
        cmbToolbar.setEnabled(spinerState);
        cmbToolbar.setSelection(itemSpinner);

        cmbToolbar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            int itemSelected;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                itemSelected = parent.getSelectedItemPosition();
                if (itemSelected > 0) {
                    String s = parent.getItemAtPosition(position).toString();
                    String[] sub = s.split("\n");
                    String rut = sub[0].substring(5, sub[0].length());
                    String num_ses = rut.replace(" ", ""); //sacamos numero de ruta

                    String [] rutayfecha = sub[1].split("\\s+"); //dividimos por espacio
                    String dia = rutayfecha[0]; String hora = rutayfecha[1];

                   // getCoordenadas(id_cliente, num_ses); //Coordenadas almacenadas en servidor
                    getCoordenadasDB(num_ses);              //Coordenadas almacenadas en Base de datos interna
                    listener.PassingZoomLevelToMap(16); //al seleccionar mapa ponemos el nivel de zoom que queramos
                    listener2.passingRutayFechatoMap(num_ses, dia, hora);

                } else {
                    //borramos las rutas que haya en caso de pulsasr el titulo
                    listener.PassingBorrarRutasEnMapa(true);
                    //limpiamos los textview
                    listener2.passingDistVelocTiempoRutas(null, null, 0);
                    listener2.passingRutayFechatoMap(null, null, null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    SetRutas setRutas;
    public class SetRutas{
        ArrayList<String> arraRutasDialogo = new ArrayList<>();
        public void setArrayruta (ArrayList<String> arrayRuta){
            this.arraRutasDialogo.addAll(arrayRuta);
        }
        public ArrayList<String> getArrayruta(){
            return this.arraRutasDialogo;
        }
    }


    //Borramos las sesiones que se hayan quedado colgadas por ejemplo un cuelgue del programa o cierre inesperado.
    //Ejemplo si se queda la seesionlive a 1, lo cambiamos a cero.
    // O bien se queda sesion live 1 y una coordenada subida. En este caso borramos los dos datos
    //O bien se queda sesion live a 1 pero ninguna coordenada almacenada. En este caso booramos la sesion.

    public void deleteSesionLive1 (final String id, int deleteLive){
        //En este caso el parametro de entrada deleteLive sera siempre 1
        final String param_server="deleteSesionLive1";
        final String deleteLiv = String.valueOf(deleteLive);
        String tag_string_req = "req_register";
        //String str1 = "?id="+id + "&param=" + param_server + "&deleteLive=" + deleteLiv;
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PARAM_SESIONES,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    String tipo = jObj.getString("tipo_borrado");
                    ArrayList<String> arraySesiones = new ArrayList<>();
                    if (tipo.equals("1 coordenada 1 sesion")) {
                        JSONArray jsonArrayReg = jObj.getJSONArray("sesion");
                        for (int i = 0; i < jsonArrayReg.length(); ++i) {
                            String sesi = (String) jsonArrayReg.get(i);
                            arraySesiones.add(sesi);
                            Log.i("sesi ", sesi);
                            //Borramos las sesiones tambien de la base de datos interna y las coordenadas
                            database_sesiones.deleteSesion(arraySesiones, Main2Activity.this, id_cliente);

                        }

                    }else if (tipo.equals("0 coordenada 1 sesion")){
                        JSONArray jsonArrayReg = jObj.getJSONArray("sesion");
                        for (int i = 0; i < jsonArrayReg.length(); ++i) {
                            String sesi = (String) jsonArrayReg.get(i);
                            arraySesiones.add(sesi);
                            Log.i("sesi ", sesi);
                            //Borramos las sesiones que haya (coordenadas no habria)
                            database_sesiones.deleteSesion(arraySesiones, Main2Activity.this, id_cliente);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                //Con linkedHashMap ordenamos por orden de insercion
                Map<String, String> params = new LinkedHashMap<>();
                params.put("param",param_server);
                params.put("id",id);
                params.put("deleteLive",deleteLiv);

                return params;

            }
        };
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    //OBTENEMOS LAS RUTAS DESDE LA BASE DE DATOS INTERNA SQLITE :
    public void getRutasDB (ArrayList<String> arrayFechaYSesion){

       // final ArrayList<String> arraysesiones = new ArrayList<>();
       // final ArrayList<String> arrayfechas = new ArrayList<>();
        final ArrayList<String> arraRutasDialogFrag = new ArrayList<>();
        Cursor cursor = database_sesiones.getAllSesions(id_cliente);
        if(cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                //Obtenemos las fechas y las sesiones
                arrayFechaYSesion.add("Ruta " + String.valueOf(cursor.getInt(2)) + "\n" + cursor.getString(4));
                arraRutasDialogFrag.add("Ruta " + String.valueOf(cursor.getInt(2)) + "\n" + cursor.getString(4));
                cursor.moveToNext();
            }
            setRutas = new SetRutas();
            setRutas.setArrayruta(arraRutasDialogFrag);
            Main2Activity.this.lastItemSpinner = arrayFechaYSesion.size() -1;
            /*
            for(int i=0; i<cursor.getCount(); ++i){
                Log.i("sesiones",String.valueOf(arraysesiones.get(i)));
                Log.i("fechas",String.valueOf(arrayfechas.get(i)));
            }
            */
        }
        else{
            //Toast.makeText(getApplicationContext(), "No hay rutas todavía", Toast.LENGTH_LONG).show();
        }
    }


    //EN CASO DE OBTENER LAS RUTAS DESDE EL SERVIDOR Y NO DESDE LA BASE DE DATOS INTERNA SQLITE :
    //Conectamos con el servidor via volley para obtener las sesiones o rutas para mostrarlas en el spinner
    /*
    public void getRutas(final String id, final ArrayList<String> arrayFechaYSesion, final int deleteLive) {

        final String param_server="getNumeroSesiones";
        // Tag used to cancel the request
        final String deleteLiv = String.valueOf(deleteLive);
        String tag_string_req = "req_register";
        pDialog.setMessage("Recuperando ...");
        showDialog();
        //Cada vez que recuperemos las rutas, las cargamos en el DialogFragment por si queremos borrarlas:
        final ArrayList<String> arraRutasDialogFrag = new ArrayList<>();

       // String str1 = "?id="+id + "&param=" + param_server + "&deleteLive=" + deleteLiv;
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PARAM_SESIONES,new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        String registros = jObj.getString("registros");
                        if (registros.equals("si")) { //En caso de que haya registros

                            JSONArray jsonFechas = jObj.getJSONArray("fechas");
                            JSONArray jsonSesiones = jObj.getJSONArray("sesiones");

                            for (int i = 0; i < jsonFechas.length(); ++i) {
                                // arrayClaseRutaDia.add(new Clase_Ruta_Dia( jsonSesiones.get(i).toString(),  jsonFechas.get(i).toString()));
                                // arrayFechaYSesion.add("Ruta :" + arrayClaseRutaDia.get(i+1).getRuta() +" \n" + arrayClaseRutaDia.get(i+1).getDia());
                                arrayFechaYSesion.add("Ruta " + jsonSesiones.get(i).toString() + "\n" + jsonFechas.get(i).toString());
                                arraRutasDialogFrag.add("Ruta " + jsonSesiones.get(i).toString() + "\n" + jsonFechas.get(i).toString());
                            }
                            setRutas = new SetRutas();
                            setRutas.setArrayruta(arraRutasDialogFrag);
                            Main2Activity.this.lastItemSpinner = arrayFechaYSesion.size() - 1;
                        }
                    } else {
                        //String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), "No hay rutas todavía", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();

            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                //Con linkedHashMap ordenamos por orden de insercion
                Map<String, String> params = new LinkedHashMap<>();
                params.put("param",param_server);
                params.put("id",id);
                params.put("deleteLive",deleteLiv);
                return params;

            }
        };
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
    */

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    //Recibimos la orden desde FragmentLocalizacion para borrar los datos de ruta que solo tengan un dato subido.
    @Override
    public void onBorrarDatosAlFinalizar(String id, String num_sesion,boolean cerrarApp) {
        ArrayList<String> arraynum = new ArrayList<>();
        arraynum.add(num_sesion);
        //deleteRuta(id, arraynum, cerrarApp);    //Borramos los datos del servidor
        deleteRutaDB(arraynum,cerrarApp);       //Borramos los datos de la base de datos interna
    }

    public void getCoordenadasDB(String num_sesion){

        ArrayList<Double> arrayDistancias;
        ArrayList<Integer> arrayTiempos;
        ArrayList<Coord_Long_Lat> arrayCoord;
        ArrayList<ArrayList> arraylista;
        arraylista = database_coorde.getDistanciasYtiempos(num_sesion, id_cliente);
        int timePaused = database_sesiones.getTimePaused(num_sesion, id_cliente);
        if(arraylista.get(0).size()>1){

            arrayDistancias = arraylista.get(0);
            arrayTiempos = arraylista.get(1);
            arrayCoord = arraylista.get(2);

            listener2.passingDistVelocTiempoRutas(arrayTiempos, arrayDistancias, timePaused);
            listener.PassingRutaToMapa(arrayCoord, arrayTiempos, arrayDistancias, timePaused,16);
        }else{
            Toast.makeText(getApplicationContext(), "No hay registros que mostrar", Toast.LENGTH_SHORT).show();
        }
    }


    //OBTENCION DE COORDENADAS RELATIVAS A CADA MAPA, es decir, las caoordenadas asociados a cada uno de las sesiones o rutas
    //de modoa que cuando pulsaoms el el spinner se muestra la ruta correspondiente
    /*
    public void getCoordenadas(final String id, final String num_sesion) {

        final String param_server="getCoordenadas";
        // Tag used to cancel the request
        String tag_string_req = "req_register";
        pDialog.setMessage("Recuperando ...");
        showDialog();
        //String params = "?id_cliente=" + id + "&numeroSesion=" + num_sesion + "&param=" + param_server;
       // Log.i("idysesion", id+" "+num_sesion);

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PARAM_COORDENADAS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {

                        ArrayList<Coord_Long_Lat> arrayCoord = new ArrayList<>();
                        //http://stackoverflow.com/questions/31855390/android-json-parse-getting-array-within-array
                        //Ver como esta estructurado el Json que viene del servidor en la extesion de crome api Rest
                        JSONArray jsonArrayReg = jObj.getJSONArray("registros");

                        for (int i = 0; i < jsonArrayReg.length(); ++i) {

                            JSONObject object = jsonArrayReg.getJSONObject(i);
                            Double lati = object.getDouble("latitud");
                            Double longi = object.getDouble("longitud");
                            Double alti = object.getDouble(("altitud"));
                            arrayCoord.add(new Coord_Long_Lat(longi, lati, alti));
                        }

                        //Obtenemos los tiempos y las distancias que ya vienen en array en el json:
                        JSONArray arrayTiemp = jObj.getJSONArray("tiempos");
                        JSONArray arrayDist = jObj.getJSONArray("distancias");
                        ArrayList<Integer> arrayTiempos = new ArrayList<>();
                        ArrayList<Double> arrayDistancias = new ArrayList<>();
                        for (int i=0; i<jsonArrayReg.length(); ++i){
                            int tiempos = arrayTiemp.getJSONArray(0).getInt(i);
                            arrayTiempos.add(tiempos);
                            double distancias = arrayDist.getJSONArray(0).getDouble(i);
                            arrayDistancias.add(distancias);
                        }
                        int timePaused = jObj.getInt("time_paused");
                        listener2.passingDistVelocTiempoRutas(arrayTiempos, arrayDistancias, timePaused);
                        listener.PassingRutaToMapa(arrayCoord, arrayTiempos, arrayDistancias, timePaused);

                    } else {
                        Toast.makeText(getApplicationContext(), "No hay registros que mostrar", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               // Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "no se que pasa", Toast.LENGTH_LONG).show();
                hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                //Con linkedHashMap ordenamos por orden de insercion
                Map<String, String> params = new LinkedHashMap<>();
                params.put("param",param_server);
                params.put("id_cliente",id);
                params.put("numeroSesion",num_sesion);
                return params;
            }
        };
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
    */


    //BORRADO DE RUTAS SELECCIONADAS DE LA BASE DE DATOS INTERNA
    public void deleteRutaDB( ArrayList<String> num_sesion, boolean cerrarApp){

        //Log.i("arraydist3", "arraydist3");
        //Borramos las sesiones y las rutas
        //Comprobamos que han sido borradas las sesiones:
        boolean isdeleted_sesiones = true;
        database_sesiones.deleteSesion(num_sesion, this, id_cliente);

        for(String ses : num_sesion){
            isdeleted_sesiones= database_sesiones.is_sesion_deleted(ses, id_cliente);
            if(!isdeleted_sesiones){
                //Log.i("isdeleted","no se borraron todas las sesiones");
                break;
            }
        }
        //Comprobamos que se borraron todas las coordenadas:
        boolean isdeleted_coorde=true;
        for (String ses : num_sesion){
            database_coorde.deleteCoordenadas(ses, id_cliente);
            int count = database_coorde.getCountSesion(ses, id_cliente);
            if(count>0){
               // Log.i("isdeleted","no se borraron todas las coordenadas");
                isdeleted_coorde=false;
                break;
            }
        }
        if(!isdeleted_coorde && !isdeleted_sesiones){
            if(cerrarApp){
                //Log.i("arraydist4", "arraydist4");
                onCerrarApp(true);
            }
            //Log.i("isdeleted2 :", "no se borraron sesiones ni coordenadas");
        }else{
            if(cerrarApp){ //cerramos la app en caso de que lo ordenemos al salir de la app
                //Log.i("arraydist5", "arraydist5");
                onCerrarApp(true);
            }else{
                //Log.i("arraydist6", "arraydist6");
                generarSpinner(id_cliente, true, 0,1);
            }
            //Log.i("isdeleted2 :", "se borraron sesiones y coordenadas");
        }
    }



    //BORRADO DE LAS RUTAS SELECCIONADAS QUE ESTAN ALMACENADAS EN EL SERVIDOR.
    //PERO YA NO LO USAREMOS YA QUE CUANDO TERMINEMOS LA SESION, BORRAREMOS LAS COORDENADAS RELATIVAS A LA RUTA
    //AUNQUE DEJAREMOS LA RUTA. SE BORRARAN LAS COORDENADAS RELATIVAS A LA RUTA EN LA FUNCION: subir_numeroliveacero_y_deleteruta

    public void deleteRuta(final String id, final ArrayList<String> num_sesion, final boolean cerrarApp) {

        final String param_server="borrarSesiones";
        // Tag used to cancel the request
        String tag_string_req = "req_register";
        pDialog.setMessage("Recuperando ...");
        showDialog();
        /*
        String paramss = "?id=" + id + "&param=" + param_server;
        StringBuilder build = new StringBuilder();
        build.append(paramss);
        for (int i = 0; i < num_sesion.size(); ++i) {
            String paramSes = "&array_numeroSesion[]=" + num_sesion.get(i);
            build.append(paramSes);
        }
        */
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PARAM_SESIONES, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();
                //En caso de usar solo DATOS DEL SERVIDOR Y NO DE LA BASE DE DATOS INTERNA, ENTOCES USARIAMOS ESTE BLOCQUE
                //Y QUITARIAMOS la funcion deleteRutaDB:
                /*
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        int exito = jObj.getInt("exito");
                        if (exito == 1) {
                            if(cerrarApp){ //cerramos la app en caso de que lo ordenemos al salir de la app
                                onCerrarApp(true);
                            }else{
                                String mensaje = jObj.getString("mensaje");
                                generarSpinner(id_cliente, true, 0,0);
                            }
                        }
                    } else { //En este caso cerramos la app si damos la orden
                        if(cerrarApp){
                            onCerrarApp(true);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                */

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                //Con linkedHashMap ordenamos por orden de insercion
                Map<String, String> params = new LinkedHashMap<>();
                params.put("param",param_server);
                params.put("id",id);
                for(int i=0; i<num_sesion.size(); ++i){
                    params.put("array_numeroSesion["+(i)+"]",num_sesion.get(i));
                }
                return params;

            }
        };
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_actionbar, menu);
        //setColorItem(menu, 0,"Gestion Mapas");
        /*
        int positionOfMenuItem = 3; // or whatever...
        MenuItem item = menu.getItem(positionOfMenuItem);
        SpannableString s = new SpannableString("Cerrar Sesion");
        s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
        item.setTitle(s);
        */
        setMenu(menu);
        return true;
    }

    private Menu menu;
    public void setMenu (Menu menu){
        this.menu= menu;
    }
    public Menu getMenu(){
        return this.menu;
    }

    //Desactivamos los botonoes del menu opciones mientras la app este funcionando
    //Ver:http://stackoverflow.com/questions/5440601/android-how-to-enable-disable-option-menu-item-on-button-click
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SessionManager sessionManager= new SessionManager(Main2Activity.this);
        if(sessionManager.get_isLocalizable()){
            menu.findItem(R.id.si_serlocalizable).setChecked(true);
            menu.findItem(R.id.no_serlocalizable).setChecked(false);
        }else{
            menu.findItem(R.id.si_serlocalizable).setChecked(false);
            menu.findItem(R.id.no_serlocalizable).setChecked(true);
        }

        if(botonActivar){
            menu.findItem(R.id.gestion_mapas).setEnabled(false);
            menu.findItem(R.id.gestion_contactos).setEnabled(false);
            menu.findItem(R.id.tiporuta).setEnabled(false);
            menu.findItem(R.id.app_web).setEnabled(false);
            menu.findItem(R.id.cerrar_sesion).setEnabled(false);

        }else{
            menu.findItem(R.id.gestion_mapas).setEnabled(true);
            menu.findItem(R.id.gestion_contactos).setEnabled(true);
            menu.findItem(R.id.tiporuta).setEnabled(true);
            menu.findItem(R.id.app_web).setEnabled(true);
            menu.findItem(R.id.cerrar_sesion).setEnabled(true);
            /* Esto ya lo colocamos en el menu_actionbar.xml
            menu.getItem(2).getSubMenu().getItem(0).setCheckable(true);
            menu.getItem(2).getSubMenu().getItem(1).setCheckable(true);
            menu.getItem(2).getSubMenu().getItem(2).setCheckable(true);
            menu.getItem(2).getSubMenu().getItem(3).setCheckable(true);
            */
        }
        return true;
    }

    public void set_checked (ArrayList<Integer> arrayIds, int numItem , int item){

        for(int i=0; i< arrayIds.size(); ++i){
            if(arrayIds.get(i) == item){
                //Ponemos checked al item seleccionado
                getMenu().getItem(numItem).getSubMenu().getItem(i).setChecked(true);
            }else{
                //Ponemos unchecked el resto de items:
                getMenu().getItem(numItem).getSubMenu().getItem(i).setChecked(false);
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        ArrayList<Integer> arrayIds = new ArrayList<>(); //Ids de tipo de ruta
        arrayIds.add(R.id.ruta_coche);
        arrayIds.add(R.id.ruta_corta);
        arrayIds.add(R.id.ruta_normal);
        arrayIds.add(R.id.ruta_larga);
        arrayIds.add(R.id.seleccionar_tiempo);

        ArrayList<Integer> arrayIds2 = new ArrayList<>(); // Ids de si quiero o no ser localizable:
        arrayIds2.add(R.id.si_serlocalizable); arrayIds2.add(R.id.no_serlocalizable);

        ArrayList<Integer> arrayIds3 = new ArrayList<>(); //Ids de si quiero o no radares:
        arrayIds3.add(R.id.si_radares); arrayIds3.add(R.id.no_radares);

        String[] mistring;
        DialogFragment dialogo;
        SessionManager sessionManager= new SessionManager(Main2Activity.this);


        switch (item.getItemId()) {
            case R.id.gestion_mapas: //Borramos los mapas seleccionados
                if(setRutas!=null){
                    mistring = setRutas.getArrayruta().toArray(new String[setRutas.getArrayruta().size()]);
                    // mistring = arraRutasDialogFrag.toArray(new String[arraRutasDialogFrag.size()]);
                    dialogo = AlertDialogo.newInstance(mistring, R.id.gestion_mapas);
                    dialogo.show(getSupportFragmentManager(), "dialog");
                }
                return true;
            case R.id.add_contacto:
                dialogo = AlertDialogo.newInstance2(R.id.add_contacto);
                dialogo.show(getSupportFragmentManager(), "dialog");
                return true;
            case R.id.delete_amigo:
                //Buscamos Contactos que se tengan aceptados:
                Volley_Contactos voleyC = new Volley_Contactos(this);
                voleyC.getListaContactos(email_cliente);
                //Cuando se recibe el array de contactos que se tienen agregados en volley, vamos al metodo:
                //retornarListaContactos para crear el dialogFragment y eliminar los que se quieran
                return true;
            case R.id.buscar_solicitudes:
                //Buscar Solicitudes que tengas pendientes:
                Volley_Contactos volleyContactos = new Volley_Contactos(this);
                volleyContactos.buscarSolicitudes(email_cliente);
                //Cuando se recibe el array de solicitudes pendientes por voley ,
                // ir a metodo retornarArraySolicitudesPendientes para crear el dialogFragment
                return true;

            case R.id.update_contactos:
                generarSpinnerContactos(this, email_cliente);
                return true;

            case R.id.ruta_coche:
                set_checked(arrayIds,2, item.getItemId());
                listener2.setFrecuenceTime(3000, " 3 seg");
                return true;


            case R.id.ruta_corta:
                set_checked(arrayIds,2, item.getItemId());
                listener2.setFrecuenceTime(5000, " 5 seg");
                return true;


            case R.id.ruta_normal:
                set_checked(arrayIds,2, item.getItemId());
                listener2.setFrecuenceTime(12000," 12 seg");
                return false;


            case R.id.ruta_larga:
                set_checked(arrayIds,2, item.getItemId());
                listener2.setFrecuenceTime(25000, " 25 seg");
                return true;


            case R.id.seleccionar_tiempo:
                //set_checked(arrayIds,2, item.getItemId());
                //listener2.setFrecuenceTime(5000, "en coche");
                set_checked(arrayIds,2, item.getItemId());
                dialogo = AlertDialogo.newInstance2(R.id.seleccionar_tiempo);
                dialogo.show(getSupportFragmentManager(), "dialog");
                return true;


            case R.id.si_serlocalizable:
                set_checked(arrayIds2,3, item.getItemId());
               //Guardamos en shared preferencias si queremos estar o no localizable
                Toast.makeText(Main2Activity.this, "Estas localizable para tus contactos", Toast.LENGTH_LONG).show();
                sessionManager.set_isLocalizable(true);
                return true;

            case R.id.no_serlocalizable:
                set_checked(arrayIds2,3, item.getItemId());
                Toast.makeText(Main2Activity.this, "Tus contactos no te pueden localizar", Toast.LENGTH_LONG).show();
                sessionManager.set_isLocalizable(false);
                return true;

            case R.id.si_radares:
                set_checked(arrayIds3,4, item.getItemId());
                Toast.makeText(Main2Activity.this, "Radares activados", Toast.LENGTH_LONG).show();
                listener.Passing_Gestion_Radares("si");

                return true;

            case R.id.no_radares:
                set_checked(arrayIds3,4, item.getItemId());
                Toast.makeText(Main2Activity.this, "Radares desactivados", Toast.LENGTH_LONG).show();
                listener.Passing_Gestion_Radares("no");
                return true;

            case R.id.app_web:
                dialogo = AlertDialogo.newInstance2(R.id.app_web);
                dialogo.show(getSupportFragmentManager(), "dialog");
                return true;

            case R.id.cerrar_sesion:
                dialogo = AlertDialogo.newInstance5(R.id.cerrar_sesion);
                dialogo.show(getSupportFragmentManager(), "dialog");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void enviarFrecuenciaGps(float frec_gps) {
        //el tiempo al estar en formato 0.5 , 1, 1.5 etc. Multiplicamos por 360000 milisegundos que tiene 1 min
        //Toast.makeText(this, String.valueOf((int)(60000*frec_gps)), Toast.LENGTH_SHORT).show();
        int frecu_gps = (int)frec_gps; //quitamos los decimales al float
        listener2.setFrecuenceTime((int)(1000*frec_gps), String.valueOf(frecu_gps)+" seg");
        /* En caso de usar en el seekbar minutos en vez de segundos
        String frecuencia = String.valueOf(frec_gps);
        if (frecuencia.endsWith("5")){
            listener2.setFrecuenceTime((int)(60000*frec_gps), String.valueOf(frec_gps)+" min"); // dejamos el .5 decimal
        }else{
            listener2.setFrecuenceTime((int)(60000*frec_gps), String.valueOf((int)frec_gps)+" min"); //quitamos el cero decimal
        }
        */

    }

    @Override   //Array que recibimos desde volley y pasamos a AlertDialog
    public void retornarArraySolicitudesPendientes(ArrayList<String> arrayEmails, ArrayList<String> arrayTextos) {
        DialogFragment dialogo;
        dialogo = AlertDialogo.newInstance3(R.id.buscar_solicitudes, arrayEmails, arrayTextos);
        dialogo.show(getSupportFragmentManager(), "dialog");
    }

    @Override   //Array que recibimos desde volley y pasamos a AlertDialog
    public void retornarListaContactos(ArrayList<String> arrayList) {
        DialogFragment dialogo;
        dialogo = AlertDialogo.newInstance4(R.id.delete_amigo, arrayList);
        dialogo.show(getSupportFragmentManager(), "dialog");
    }

    @Override   //Array qeu recibimos desde AlertDialog para borrar las rutas
    public void borrarRutasFromDialog(ArrayList<Integer> lista) {
        ArrayList<Integer> list = lista;
        //Sacamos los elementos a borrar
        ArrayList<String> newlist = new ArrayList<>();
        ArrayList<String> num_sesiones = new ArrayList<>();
        for (int i=0; i<list.size(); ++i) {
            newlist.add(setRutas.getArrayruta().get(list.get(i)));
           // newlist.add(arraRutasDialogFrag.get(list.get(i)));
            String[] sub = newlist.get(i).split("\n");
            String[] sub0 = sub[0].split("\\s+");
            String num = sub0[1].substring(0, sub0[1].length());
            String num_ses = num.replace(" ", "");
            num_sesiones.add(num_ses);
        }
       // deleteRuta(id_cliente, num_sesiones,false); //borramos las rutas del servidor. YA NO LO  USAREOMS.
        deleteRutaDB(num_sesiones,false);           //Borramos las rutas de la base de datos interna
    }

    @Override   //Datos que recibimos desde AlertDialog para posteriormente enviar a voley
    public void enviarSolicitudContacto(String solicitado, String comentario) {  //correo so
       //A traves de volley enviamos la solicitud al contacto que queramos:
        Volley_Contactos volley_contactos = new Volley_Contactos(this);
        volley_contactos.enviarSolicitud(email_cliente, solicitado, comentario);

    }

    @Override
    public void enviarSolicitudContactoFirebase(String solicitante, String solicitado, String texto,
                                                boolean error, String mensaje) {
        //Si recibimos un error del servidor no almacenamos la solicitud en firebase, en caso contrario la almacenamos
        if(!error)
            enviarSolicitudFirebase(email_cliente, solicitado, texto);
    }

    @Override   //Datos que recibimos desde AlertDialog para posteriormente enviar a voley
    public void aceptar_rechazar_SolicitudesPendientesFromDialog(ArrayList<String> listaAceptRechaz, int acep_rechaz) {
        //Atraves de volley y con la lista de elementos seleccionados y con la decision tomada (1=>Aceptar, 2=>Rechazar)
        Volley_Contactos volleyContactos = new Volley_Contactos(this);
        volleyContactos.acept_rechaz_Solicitudes(email_cliente, listaAceptRechaz, acep_rechaz);
    }

    @Override   //Datos que recibimos desde AlertDialog para posteriormente enviar a voley
    public void borrarContactosFromDialog(ArrayList<String> listaContactos) {
        //Atraves de voley borramos los contactos seleccionados
        Volley_Contactos volleyContactos = new Volley_Contactos(this);
        volleyContactos.borrarContactosFromDialog(email_cliente, listaContactos);

    }

    @Override   //Recibimos la orden deesde voley para actualizar la lista de contactos
    public void actualizarSpinnerContactos() {
        generarSpinnerContactos(this, email_cliente);
    }



    //OJO PORQUE SI SE DESINSTALA LA APP SIN CERRAR SESION , ESTA SE QUEDA ABIERTA EN EL SERVIDOR!!
    //Cierre de sesion a traves del menu de opciones
    SessionManager session;
    @Override   //Recibimos la orden deesde voley para cerrar la sesion y volver a la pantalla de login
    public void cerrarSesionFromDialog() {
        //Cerramos la sesion en el servidor cambiando el valor de islogged a 0
        final String param_server="cerrarSesion";
        String tag_string_req = "req_register";
        final String tipo_login = "islogged";
        //String str1 = "?email="+email_cliente+ "&param=" + param_server;
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PARAM_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try{
                    JSONObject jObj = new JSONObject(response);
                    boolean hecho = jObj.getBoolean("hecho");
                    if(hecho){
                        //Si se inserta bien el valor de islogged =0
                        session = new SessionManager(Main2Activity.this);
                        session.setLogin(false, email_cliente, pass,false); //cierro sesion y no lo dejo en segundo plano
                        Intent i = new Intent(Main2Activity.this, LoginActivity.class);
                        i.putExtra("email",email_cliente);
                        i.putExtra("pass",pass);
                        startActivity(i);
                        finish();
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                //Con linkedHashMap ordenamos por orden de insercion
                Map<String, String> params = new LinkedHashMap<>();
                params.put("param",param_server);
                params.put("email",email_cliente);
                params.put("tipo_login", tipo_login);
                return params;

            }
        };
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);

    }


    @Override
    public void onBackPressed() {
        //Este bloque lo usaremos solo si al pulsar la opcion de back nos diga si queremos salir o no de la app:
        /*
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);
        // Setting Dialog Title
        alertDialog.setTitle("Salir de la App");
        // Setting Dialog Message
        alertDialog.setMessage("Realmente quieres salir?");
        // On pressing Settings button
        alertDialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (timer != null) {
                    timer.cancel();  //cancelamos timer en caso de que estemos viendo algun amigo
                }
                //listener.limpiarArrays();
                listener2.passingOnFinishApp(true);
            }
        });
        // on pressing cancel button
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                listener2.passingOnFinishApp(false);
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();
        */

        //En caso de que pulsemos atras y se ponga en segundo plano pero funcionando, usaremos esto:
        session = new SessionManager(Main2Activity.this);
        session.setLogin(false, email_cliente, pass, true); //indicamos que esta en segundo plano pero no logado
        session.set_time_in_background(System.currentTimeMillis()); //Almacenamos la ultima hora de conexion
        moveTaskToBack(true);   //asi salimos de la app dejandola en segundo plano.

    }




    @Override
    public void onCerrarApp(boolean var) {
        if(var){
           // setappinBackground(id_cliente,1);
            session = new SessionManager(Main2Activity.this);
            session.setLogin(false, email_cliente, pass, true); //indicamos que esta en segundo plano pero no logado
            session.set_time_in_background(System.currentTimeMillis()); //Almacenamos la ultima hora de conexion

            Main2Activity.super.onBackPressed();

        }
    }


/*
    Cuando pulsemos atras para salir de la aplicacion, la pondremosm en segundo plano.
    En el momento en que ponemos la app en segundo plano (con el boton back) lo que haremos es
    poner la variable inBackground = 1 y la variable isLogged = 0.
    De esta forma conseguiremos posteriormeten , desconectar la sesion desde el servidor poniendo las variables a cero
    pasado por ejemplo 24 horas.
    Ojo , no es lo mismo que este en segundo plano pulsando el boton back que cuando se pone el boton home y se deja
    correr la app con todas sus funciones en segundo plano
    */

   /*
    private void setappinBackground(final String id, int inBackground){

        String tag_string_req = "req_register";
        pDialog.setMessage("Recuperando ...");
        showDialog();
        String params = "?id=" + id + "&setinback=" + inBackground;
        // Log.i("idysesion", id+" "+num_sesion);

        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_CHECKBACKGROUND + params, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean hecho = jObj.getBoolean("hecho");
                    int islogged = jObj.getInt("islogged");
                    int inbackground = jObj.getInt("inbackground");
                    String viene = jObj.getString("viene_de");
                    if(hecho){
                        Log.i("estalogado",String.valueOf(hecho)+" "+String.valueOf(islogged)+" "+String.valueOf(inbackground)+" "+viene);
                        //Si se inserta bien el valor de islogged =0
                         session = new SessionManager(Main2Activity.this);
                         session.setLogin(false, email_cliente, pass, true); //indicamos que esta en segundo plano pero no logado
                         Main2Activity.super.onBackPressed();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "no se que pasa", Toast.LENGTH_LONG).show();
                hideDialog();
            }
        });
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);

    }
    */




   // private DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot();
    //private String temp_key;

    String userId;
    private void enviarSolicitudFirebase(String solicitante, String solicitado, String comentario){

        userId =  mFirebaseDatabase.push().getKey();
        Map<String,Object> map2 = new HashMap<String, Object>();
        map2.put("solicitante",solicitante);
        map2.put("solicitado",solicitado);
        map2.put("mensaje",comentario);
        mFirebaseDatabase.child(userId).setValue(map2);

    }

    //int j=0;
    //long nodos;
    private void buscarSolicitudesFirebase(){
        mFirebaseDatabase.addChildEventListener(new ChildEventListener() {
           @Override
           public void onChildAdded(DataSnapshot dataSnapshot, String s) {
               //Este metodo se ejecuta tantas veces como children o nodos haya , por eso se usa un hashset o map
               //por eso se usa un hasset o map en el metodo refreshingChild(), para que se agreguen los valores
               //sin que se dupliquen
               //nodos = dataSnapshot.getChildrenCount(); //valor que da el numero de nodos de cada hijo
               refreshingChild(dataSnapshot);
               //++j;
               //Log.i("contasuma",String.valueOf(j));
               //Aparecia el error siguiente: http://stackoverflow.com/questions/9529504/unable-to-add-window-token-android-os-binderproxy-is-not-valid-is-your-activ
               //Con lo cual deje de usar un contador y use isfinishing() para saber si se termino cualquier proceso en la activity
               //incluido la busqueda de nodos en onChildAdded
               /*
               if(j==1){
                   new Handler().postDelayed(new Runnable() {
                       @Override
                       public void run() {
                         deteccion();
                       }
                   },1000);
               }
               */
               new Handler().post(new Runnable() {
                   @Override
                   public void run() {
                       if(!(isFinishing())){
                           deteccion();
                       }
                   }
               });
           }
           @Override
           public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
           @Override
           public void onChildRemoved(DataSnapshot dataSnapshot) {}
           @Override
           public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
           @Override
           public void onCancelled(DatabaseError databaseError) {}
       });
    }

    Set<Solicitantes> setsoli = new HashSet<>();
   // long ka = 0;
    private void refreshingChild (DataSnapshot dataSnapshot) {
        String mensaje, solicitado, solicitante;
        Iterator i = dataSnapshot.getChildren().iterator();
        while (i.hasNext()) {
            mensaje = (String) ((DataSnapshot) i.next()).getValue();
            solicitado = (String) ((DataSnapshot) i.next()).getValue();
            solicitante = (String) ((DataSnapshot) i.next()).getValue();
            setsoli.add(new Solicitantes(mensaje,solicitado,solicitante));
        }
    }


    //Comprobamos si soy el solicitado por algun solicitante
    private void deteccion(){
       // j=0;
        boolean haySolic=false;
        ArrayList<Solicitantes> arraySolicitudes = new ArrayList<>();
        for(Solicitantes elem: setsoli){
            String solicitado = elem.getSolicitadoM();
            String solicitante = elem.getSolicitM();
            String mensaje = elem.getMensaje();
            if(solicitado.equals(email_cliente)){
                 //Creamos un array con todas las solicitudes que recibimos
                  arraySolicitudes.add(elem);
                  haySolic=true;
            }
        }
        if(haySolic){
            //for(Solicitantes elem: arraySolicitudes){
              //  Log.i("arraysoli", elem.getSolicitadoM()+" "+elem.getSolicitM()+" "+elem.getMensaje());
            //}
            crearAlertDialog(arraySolicitudes);
        }
        setsoli.clear();
    }

    private void deleteContactosFIrebase (final ArrayList<String> selectedContacts){

        Query elems = mFirebaseDatabase.orderByChild("solicitado").equalTo(email_cliente);

        elems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot cont: dataSnapshot.getChildren()) { //todos los registros de la base de datos
                    for( DataSnapshot elem : cont.getChildren()){   //Cada Fila tiene estos parametros: {solicitado=aa@a.com, solicitante=a@a.com, mensaje=hdjabsh}
                        if(elem.getKey().equals("solicitante")){    //bucle que se recorre por los tres hijos de cada registro key, value
                            String value = elem.getValue().toString();
                            for(String item: selectedContacts){
                                    if(value.equals(item)){
                                    cont.getRef().removeValue();    //eliminamos el registro
                                }
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Log.e("error en delete", "onCancelled", databaseError.toException());
            }
        });
    }


    public void crearAlertDialog(ArrayList<Solicitantes> arraySolic){

        final ArrayList<String> arrayEmails = new ArrayList<>();
        final ArrayList<String> arrayTextos = new ArrayList<>();

        for (int i=0; i<arraySolic.size(); ++i){
            arrayEmails.add(arraySolic.get(i).getSolicitM());
            arrayTextos.add(arraySolic.get(i).getMensaje());
        }
        String[] str = arrayEmails.toArray(new String[arrayEmails.size()]);
        //Metemos correo y textos separados por dos saltos de linea
        for(int i=0;i<arrayEmails.size(); ++i){
            str[i]= str[i]+"\n\n"+arrayTextos.get(i);
        }
        selectedContactsString = new ArrayList<>(); //Aqui metemos la lista en string a aceptar o rechazar
       // Log.i("selectedd ", String.valueOf(selectedContactsString.size()));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Desea agregar a estos contactos?");

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Obtemenos la lista en String de los elementos aceptados
                //Log.i("selected ", selectedContactsString.toString());
                if(selectedContactsString.size()>0){
                    //Borramos los contactos seleccionados de la base de Firebase
                    deleteContactosFIrebase(selectedContactsString);
                    aceptar_rechazar_SolicitudesPendientesFromDialog(selectedContactsString, 1); //valor 1: aceptar
                }

            }
        });
        builder.setNegativeButton("Rechazar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Obtemenos la lista en String de los elementos rechazados
                if(selectedContactsString.size()>0) {
                    deleteContactosFIrebase(selectedContactsString);
                    aceptar_rechazar_SolicitudesPendientesFromDialog(selectedContactsString, 2); //valor 2: rechazar
                }
            }
        });
        builder.setMultiChoiceItems(str, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if(isChecked){
                    selectedContactsString.add(arrayEmails.get(which));
                    //Log.i("selected 1", selectedContactsString.toString());
                }else if (selectedContactsString.contains(arrayEmails.get(which))) {
                    // selectedContacts.remove(Integer.valueOf(which)); //En caso de haber pulsado algun checked y agregado, lo quitamos del array
                    selectedContactsString.remove(arrayEmails.get(which));
                    //Log.i("selected 2", selectedContactsString.toString());
                }
            }
        });
        builder.show();
    }



    public class Solicitantes{
        private String mensaje, solicitM, solicitadoM;
        public Solicitantes(String mensaje, String solicitadoM, String solicitM){
            this.mensaje=mensaje; this.solicitM = solicitM; this.solicitadoM=solicitadoM;
        }
        public String getMensaje() {return mensaje;}
        public String getSolicitadoM() {return solicitadoM;}
        public String getSolicitM() {return solicitM;}
    }
}

