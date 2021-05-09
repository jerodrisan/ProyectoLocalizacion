package com.jesusrodri.localizacionproyecto;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
//import android.support.v7.app.NotificationCompat;
import androidx.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
//import com.jesusrodri.localizacionproyecto.interfaces.ApiInterface_Retrofit;
import com.jesusrodri.localizacionproyecto.interfaces.OnServiceInterationListener;
import com.jesusrodri.localizacionproyecto.login_files.AppConfig;
//import com.jesusrodri.localizacionproyecto.utils.ApiClient_Retrofit;
//import com.jesusrodri.localizacionproyecto.utils.Coordenadas_Json_Retrofit;
import com.jesusrodri.localizacionproyecto.login_files.SessionManager;
import com.jesusrodri.localizacionproyecto.utils.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

//import retrofit2.Call;
//import retrofit2.Callback;


/******************** OJO NO USAREMOS ESTA CLASE. USAREMOS MyIntentServiceLocalizacion.java COMO SERVICIO ***************/
/******************** OJO NO USAREMOS ESTA CLASE. USAREMOS MyIntentServiceLocalizacion.java COMO SERVICIO ***************/
/******************** OJO NO USAREMOS ESTA CLASE. USAREMOS MyIntentServiceLocalizacion.java COMO SERVICIO ***************/
/******************** OJO NO USAREMOS ESTA CLASE. USAREMOS MyIntentServiceLocalizacion.java COMO SERVICIO ***************/
// Debido a que el uso de las apis que se  usan en el otro servicio para la localizacion son mas exactos y rapidos.

/**
 * Los para pasar datos desde Fragment_Localizacion al servicio lo haremos a traves de intents.
   Para pasarlos desde MyServiceLocalizacion al Fragment lo haremos con bindService, que ya esta definido en el fragment.
  No se hara el paso de datos desde el servicio al Fragment a traves de interface ya que en ese caso se podria producir
 un NPE debido a que  la actividad esta en pausa o segundo plano. Aunque probando se ve que hay un error de conversion de clases
 al inicializar el interface en el onAtache del Fragment.
 Al final la solucion para pasar datos de servicio a Fragmen es esta:
 http://android-coding.blogspot.com.es/2011/11/pass-data-from-service-to-activity.html
 Un simple BroadcastReceiver
 Otras soluciones en StackOverflow:
 http://stackoverflow.com/questions/14351674/send-data-from-service-to-activity-android
 http://www.vogella.com/tutorials/AndroidServices/article.html
 En la solucion de Vogella es util cuando se quieren recibir los datos desde el servicio al fragment o activity pero
 en este caso solicitandolo desde la propia activity o fragment.


 */

public class MyServiceLocalizacion extends Service{

    public static final String ACTION_PROGRESO = "com.example.android_localizacionproyecto.intent.action.PROGRESO";
    public static final String ACTION_FIN = "com.example.android_localizacionproyecto.intent.action.FIN";

    private LocationManager locManager;

    public OnServiceInterationListener sListener;
    private final IBinder mBinder = new MyBinder();

    private SQLiteDataCoordenadas database_coorde;

    private int frecuenceTime;

    ArrayList<Coord_subir> arrayCoorde;
    //AlarmaRecibidor alarma;
    SessionManager sessionManager;

    int conta;
    Location loc0;
    String cadFecha0;
    ArrayList<Location> arrayLoc;
    ArrayList<String> arrayCadFecha;

    @Override
    public void onCreate() {
        super.onCreate();
        database_coorde = new SQLiteDataCoordenadas(getApplicationContext());  //Inicializamos la base de datos interna
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(MyServiceLocalizacion.this, "Activado Servicio", Toast.LENGTH_SHORT).show();
        String id_cliente = intent.getStringExtra("id_cliente");
        int  numsesion = intent.getIntExtra("numsesion",0);
        int numsesion_db = intent.getIntExtra("numsesion_db",0);
        frecuenceTime = intent.getIntExtra("tipo_ruta",12000); //valor por defecto 12000
        sessionManager = new SessionManager(this);
        arrayCoorde = new ArrayList<>();
        conta=0;

        arrayLoc = new ArrayList<>();
        arrayCadFecha = new ArrayList<>();
        setNotification();
        comenzarLocalizacion(numsesion, numsesion_db, id_cliente);

        //Log.i("servicio", "comenzado");
        /*
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GpsTrackerWakelock");
        wl.acquire();
        */
        /*
        alarma = new AlarmaRecibidor();
        alarma.setAlarm(getApplicationContext());
        */
       // keepAwake();
       // handler.post(periodicUpdate);


        return START_NOT_STICKY;
    }

    //Creamos una notificacion en la barra de estado para cuando la app este en segundo plano. Leer:
    //https://developer.android.com/about/versions/oreo/background-location-limits
    //https://developer.android.com/guide/topics/ui/notifiers/notifications
    //https://developer.android.com/training/notify-user/build-notification
    //https://stackoverflow.com/questions/43251528/android-o-old-start-foreground-service-still-working
    //https://stackoverflow.com/questions/43093260/notification-not-showing-in-oreo



    private static final String ANDROID_CHANNEL_ID = "notification_localizacloud_ID_1";// The id of the channel.
    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel(){

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "localizacloud_channel";
            String description = "localiza cloud notificación";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ANDROID_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private void setNotification(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
        Intent notifyIntent = new Intent(this, Main2Activity.class);

        notifyIntent.putExtra("tap_action","tap_accion");
        // Set the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Create the PendingIntent
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), ANDROID_CHANNEL_ID)
                    .setContentTitle(getString(R.string.app_name))
                    .setSmallIcon(R.drawable.iconoloca)
                    .setContentText("Servicio en ejecución")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification:
                    .setContentIntent(notifyPendingIntent)
                    .setAutoCancel(true);

        Notification notification = builder.build();
        //notification.flags=Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        startForeground(1, notification);
    }


    private Location get_LastKnownLocation(){

        locManager  = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locManager.getProviders( true );
        Location bestLocation = null;
        for( String provider : providers ){
            Log.i("proveedor ",provider);
            Location l = locManager.getLastKnownLocation( provider );
            if( l == null ){
                continue;
            }
            if( bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy() ){
                bestLocation = l; // Found best last known location;
            }
        }
        return bestLocation;
    }


    public class MyBinder extends Binder {
        MyServiceLocalizacion getService() {
            return MyServiceLocalizacion.this;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //Toast.makeText(MyServiceLocalizacion.this, "sevicio destruido", Toast.LENGTH_SHORT).show();
        locManager.removeUpdates(locListener);
        locManager=null;
        if(arrayCoorde.size()>0){
            if(sessionManager.get_isLocalizable())
                subir_Coordenadas_Volley(arrayCoorde);
        }
       // this.stopSelf();
       // wl.release();
    }

    LocationListener locListener;
    private void comenzarLocalizacion(final int num_sesion, final int num_sesion_db, final String id_cliente){


        loc0=null;
        cadFecha0="";

        locManager  = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        //Nada mas pulsar el boton de empezar , pilla la ultima localizacion conocida por el GPS
        Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        // En caso de querer usar un proveedor con mayor precision de una lista de proveedores (no se yo si es muy funcional)
        //Location loc = get_LastKnownLocation(); //Ver https://www.programcreek.com/java-api-examples/?class=android.location.LocationManager&method=getLastKnownLocation
        if(loc!=null) {
            //preparar_Coordenadas(loc, num_sesion, num_sesion_db, id_cliente);
        }


        //Obtenemos la � pltimaosici�n conocida
        locListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //preparar_Coordenadas(location, num_sesion, num_sesion_db, id_cliente);
            }
            public void onProviderDisabled(String provider){
                // lblEstado.setText("Provider OFF");
                Toast.makeText(getApplicationContext(), "Proveedor No Operativo", Toast.LENGTH_SHORT).show();
            }
            public void onProviderEnabled(String provider){
                // lblEstado.setText("Provider ON ");
            }
            public void onStatusChanged(String provider, int status, Bundle extras){
                Log.i("", "Provider Status: " + status);
                //  lblEstado.setText("Provider Status: " + status);
            }


        };
        //Log.i("frecuencia", String.valueOf(frecuenceTime));
        // distancia min entre actualizaiones en metros, instancia del objeto location listener)
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, frecuenceTime, 0, locListener); // actualiza cada 15 seg
    }


    /*
    //Ejemplo de como obtener datos a traves de un binder solicitado desde Fragment:
    // double lal =  serviceLocalizacion.getLatitud();  Ver tutorial Vogella en comentarios arriba
    public double getLatitud(){
        return 30.19;
    }
   */

    private void subirCoordenadas(ArrayList<Location> loc, int numsesion, int num_sesion_db,
                                  String id_cliente, ArrayList<String> cadFecha ){

        for(int i=0; i<loc.size(); ++i){
            //Pasamos datos de posicionamiento a la base de datos sqlite
            database_coorde.addUser(id_cliente,num_sesion_db,
                    loc.get(i).getLatitude(), loc.get(i).getLongitude(),loc.get(i).getAltitude(),cadFecha.get(i));
            enviarBroadcast(loc.get(i).getLongitude(), loc.get(i).getLatitude(), loc.get(i).getAltitude(),cadFecha.get(i),true);
            arrayCoorde.add(new Coord_subir(loc.get(i).getLongitude(), loc.get(i).getLatitude(), loc.get(i).getAltitude(), id_cliente,
                    numsesion,cadFecha.get(i)));

           // Log.i("getlatitude ", String.valueOf(loc.get(i).getLatitude())+" cadfecha "+cadFecha.get(i));
        }
        //En caso de que tengamos activada la localizacion, subimos los datos al servidor
        if(sessionManager.get_isLocalizable()){
            subir_Coordenadas_Volley(arrayCoorde);
        }

    }


    private double latiAux=0, longiAux=0;
    private void preparar_Coordenadas(Location loc, int numsesion, int num_sesion_db, String id_cliente){

        Date fechaAct = new java.util.Date();
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String cadFecha=formato.format(fechaAct);
        String diayhora[] = cadFecha.split("\\s+"); String dia = diayhora[0]; String hora = diayhora[1];

        if(latiAux!=loc.getLatitude() && longiAux!=loc.getLongitude()){
            //En este caso estamos en movimiento. Indicaremos en el broadcast que estamos en movimiento

            switch (conta){
                case 0:
                    //en una primera subida almacemamos la ultima localizacion getLastKnownLocation y la comparamos con la siguiente
                    //para saber si esta muy lejos. En ese caso se descarta y solo pillamos las sucesivas.
                    loc0 = loc;
                    cadFecha0 = cadFecha;
                    conta++;
                   // Log.i("conta0 ",String.valueOf(conta)+" loc "+String.valueOf(loc.getLatitude()));
                    break;

                case 1:
                    if(loc0.distanceTo(loc)<40 ){ //si la distancia es menor a 40 metros consideramos que estan cerca y si metemos la ultima localizacion
                        //metemos las dos localizaciones, primero una y luego la otra
                        arrayLoc.add(loc0); arrayLoc.add(loc);
                        arrayCadFecha.add(cadFecha0); arrayCadFecha.add(cadFecha);
                        subirCoordenadas(arrayLoc, numsesion,num_sesion_db,id_cliente,arrayCadFecha );
                        conta++;
                      //  Log.i("conta1 ",String.valueOf(conta)+" dist "+String.valueOf(loc0.distanceTo(loc))
                       //         +" loc "+loc.getLatitude()+" "+loc.getLongitude()+" loc0 "+loc0.getLatitude()+" "+loc0.getLongitude());
                        break;
                    }else {
                        //metemos solo la segunda descartando la primerma
                        arrayLoc.add(loc); arrayCadFecha.add(cadFecha);
                        subirCoordenadas(arrayLoc, numsesion,num_sesion_db,id_cliente,arrayCadFecha );
                        conta++;
                       // Log.i("conta2 ", String.valueOf(conta)+" dist "+String.valueOf(loc0.distanceTo(loc))
                       //         +" loc "+loc.getLatitude()+" "+loc.getLongitude()+" loc0 "+loc0.getLatitude()+" "+loc0.getLongitude());
                        break;


                    }
                default:
                    //subimos las sucesivas localizaciones
                    arrayLoc.add(loc); arrayCadFecha.add(cadFecha);
                    subirCoordenadas(arrayLoc, numsesion, num_sesion_db,id_cliente, arrayCadFecha );
                  //  Log.i("conta3 ","siguientes");

            }

            latiAux = loc.getLatitude();
            longiAux= loc.getLongitude();

        }else{
            //En este caso estamos parados ya que las coordenadas son las mismas, y lo indicamos en el broadcast
            enviarBroadcast(loc.getLongitude(), loc.getLatitude(), loc.getAltitude(), cadFecha, false);
        }
        arrayLoc.clear(); //borramos el array
        arrayCadFecha.clear();
    }

    private void enviarBroadcast(double longi, double lati, double alti, String cadFecha, boolean moving ){
        //mandamos los datos al Fragment:
        Intent intent = new Intent();
        intent.setAction(ACTION_PROGRESO);
        intent.putExtra("longi",longi);
        intent.putExtra("lati",lati);
        intent.putExtra("alti",alti);
        intent.putExtra("cadFecha",cadFecha);
        intent.putExtra("moving", moving); //indicamos si estamos en movimiento o parados
        sendBroadcast(intent);
    }

    public void subir_Coordenadas_Volley(final ArrayList<Coord_subir> arrayCoorde){

        final String param_server="subir_coordenadas";
        // Tag used to cancel the request
        String tag_string_req = "req_register";
        /* En caso de usar GET
        StringBuilder str = new StringBuilder();
        String str1 = "?param=" + param_server;
        str.append(str1);
        for(Coord_subir coor: arrayCoorde){
            String lati = String.valueOf(coor.getLatitud());
            String longi = String.valueOf(coor.getLongitud());
            String alti =String.valueOf(coor.getAltitud());
            String id = coor.getid();
            String sesion_num =String.valueOf(coor.getSesionNum());
            String date = coor.getDate();

            str.append("&latitud[]="+longi+
                        "&longitud[]="+lati+
                        "&altitud[]="+alti+
                        "&id[]="+id+
                        "&sesion_num[]="+sesion_num+
                        "&date[]="+date);
        }
        */
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PARAM_COORDENADAS , new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean exito = jObj.getBoolean("exito");
                    String mensaje = jObj.getString("mensaje");
                    if(exito){
                        arrayCoorde.clear();          //Limpiamos el array

                    }else{
                        //textoprueba3.setText(mensaje+contprueba3++);
                    }
                } catch (JSONException e) {
                    String stackTrace = Log.getStackTraceString(e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                //Con linkedHashMap ordenamos por orden de insercion
                Map<String, String> params = new LinkedHashMap<>();
                params.put("param",param_server);

                for(int i=0; i<arrayCoorde.size(); ++i){
                    params.put("latitud["+(i)+"]", String.valueOf(arrayCoorde.get(i).getLatitud()));
                    params.put("longitud["+(i)+"]", String.valueOf(arrayCoorde.get(i).getLongitud()));
                    params.put("altitud["+(i)+"]", String.valueOf(arrayCoorde.get(i).getAltitud()));
                    params.put("id["+(i)+"]", String.valueOf(arrayCoorde.get(i).getid()));
                    params.put("sesion_num["+(i)+"]", String.valueOf(arrayCoorde.get(i).getSesionNum()));
                    params.put("date["+(i)+"]", String.valueOf(arrayCoorde.get(i).getDate()));
                }
                return params;
            }
        };
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
     /*
    Handler handler = new Handler();
    private Runnable periodicUpdate = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(periodicUpdate, 1000); // schedule next wake up every second
            Intent notificationIntent = new Intent(MyServiceLocalizacion.this, MyServiceLocalizacion.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(MyServiceLocalizacion.this, 0, notificationIntent, 0);
            AlarmManager keepAwake = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                keepAwake.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+1000, pendingIntent);
            }
            //keepAwake.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+1000, pendingIntent);

            long current = System.currentTimeMillis();
            if ((current-current%1000)%(1000*10)  == 0) { // record on every tenth seconds (0s, 10s, 20s, 30s...)
                // whatever you want to do
                subir_Coordenadas_Volley(-3.7321920306774, 40.423550717, 617.5733036628, "577967969cf882.34920319",
                        20, "2016-07-17 12:51:32");
            }
        }
    };
    */

    /*
    public void keepAwake (){

        Intent alarmIntent = new Intent(this, AlarmaRecibidor.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long triggerAtTime = SystemClock.elapsedRealtime() + (6 * 60 * 1000);
        int interval = 6000;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, interval, pendingIntent);
        }

       // alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        Toast.makeText(this, "Alarm Set", Toast.LENGTH_LONG).show();
    }
    */

}
