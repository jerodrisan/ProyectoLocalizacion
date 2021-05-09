package com.jesusrodri.localizacionproyecto;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.jesusrodri.localizacionproyecto.interfaces.OnServiceInterationListener;
import com.jesusrodri.localizacionproyecto.login_files.AppConfig;
import com.jesusrodri.localizacionproyecto.login_files.SessionManager;
import com.jesusrodri.localizacionproyecto.utils.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
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
public class MyIntentServiceLocalizacion extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
        //com.google.android.gms.location.LocationListener
                                                        {

    private static final String TAG = "MainActivity";
    public static final String ACTION_PROGRESO = "com.example.android_localizacionproyecto.intent.action.PROGRESO";
    public static final String ACTION_FIN = "com.example.android_localizacionproyecto.intent.action.FIN";

    private GoogleApiClient mGoogleApiClient;
    FusedLocationProviderClient flp_cliente;
    private com.google.android.gms.location.LocationListener listener;
    private long UPDATE_INTERVAL = 1000;  /* 5 secs */
    public OnServiceInterationListener sListener;
    private final IBinder mBinder = new MyIntentServiceLocalizacion.MyBinder();

    private SQLiteDataCoordenadas database_coorde;

    private int frecuenceTime;
    private String id_cliente;
    private int numsesion;
    private int numsesion_db;

    ArrayList<Coord_subir> arrayCoorde;
    //AlarmaRecibidor alarma;
    SessionManager sessionManager;


    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        flp_cliente= LocationServices.getFusedLocationProviderClient(getApplicationContext());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        if(flp_cliente!=null){
            flp_cliente.removeLocationUpdates(mLocationCallback); //al destruir el servicio quitamos las localizaciones.
        }
        if(arrayCoorde.size()>0){
            if(sessionManager.get_isLocalizable()){
                subir_Coordenadas_Volley(arrayCoorde);
            }
        }
       // Toast.makeText(MyIntentServiceLocalizacion.this, "sevicio destruido", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(MyIntentServiceLocalizacion.this, "Activado Servicio", Toast.LENGTH_SHORT).show();
        id_cliente = intent.getStringExtra("id_cliente");
        numsesion = intent.getIntExtra("numsesion",0);
        numsesion_db = intent.getIntExtra("numsesion_db",0);
        frecuenceTime = intent.getIntExtra("tipo_ruta",12000); //valor por defecto 12000
        sessionManager = new SessionManager(this);
        arrayCoorde = new ArrayList<>();
        database_coorde = new SQLiteDataCoordenadas(getApplicationContext());  //Inicializamos la base de datos interna

        setNotification();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        return START_NOT_STICKY;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }


    private double latiAux=0, longiAux=0;
    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {

            for(Location location : locationResult.getLocations()){
                //Log.i("frecuencias ", String.valueOf(location.getLatitude()));
                //Toast.makeText(getApplicationContext(), "Localizacion "+String.valueOf(location.getLatitude()), Toast.LENGTH_SHORT).show();
                //subimos las coordenadas
                Date fechaAct = new java.util.Date();
                SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String cadFecha=formato.format(fechaAct);
                String diayhora[] = cadFecha.split("\\s+"); String dia = diayhora[0]; String hora = diayhora[1];

                if(latiAux!=location.getLatitude() && longiAux!=location.getLongitude()){

                   // Log.i("nullpointer ",id_cliente+"  "+String.valueOf(numsesion_db)+"  "+String.valueOf(location.getLatitude())+
                     //       "  "+String.valueOf(location.getLongitude())+"  "+String.valueOf(location.getAltitude())+"  "+cadFecha);

                    database_coorde.addUser(id_cliente,numsesion_db, location.getLatitude(), location.getLongitude(),location.getAltitude(),cadFecha);
                    enviarBroadcast(location.getLongitude(), location.getLatitude(), location.getAltitude(),cadFecha,true);
                    arrayCoorde.add(new Coord_subir(location.getLongitude(), location.getLatitude(), location.getAltitude(), id_cliente, numsesion,cadFecha));

                    //En caso de que tengamos activada la localizacion, subimos los datos al servidor
                    if(sessionManager.get_isLocalizable()){
                        subir_Coordenadas_Volley(arrayCoorde);
                    }

                    latiAux = location.getLatitude();
                    longiAux= location.getLongitude();

                }else{
                    //En este caso estamos parados ya que las coordenadas son las mismas, y lo indicamos en el broadcast
                    enviarBroadcast(location.getLongitude(), location.getLatitude(), location.getAltitude(), cadFecha, false);
                }
            }
        }
    };

    //Quitamos el warning ya que hemos usado en la clase Fragment_localizacion el permiso en tiempo de ejecucion al igual que en Fragmen_Mapa
    @SuppressWarnings({"MissingPermission"})
    protected void startLocationUpdates() {
        // Create the location request
        LocationRequest mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(frecuenceTime) //ver diferencia entre setinterval() y setfastestInterval()
                .setInterval(UPDATE_INTERVAL);
               // .setFastestInterval(FASTEST_INTERVAL);

        // Request location updates
        flp_cliente.requestLocationUpdates(mLocationRequest,mLocationCallback,null);
       // LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this); //DEPRECADA FusedLocationApi
        //Log.d("startlocation", "--->>>>");
    }

    /*
    @Override
    public void onLocationChanged(Location location) {
    }
    */

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


    @Override
    public void onConnectionSuspended(int i) {
       // Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {
        MyIntentServiceLocalizacion getService() {
            return MyIntentServiceLocalizacion.this;
        }
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




    /*------------------  CODIGO PARA NOTIFICACION --------------------------------------*/
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
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                // Set the intent that will fire when the user taps the notification:
                //.setContentIntent(notifyPendingIntent) //No funciona en esta app ya que es complicado hacerlo con los fragments
                //.setAutoCancel(true);

        Notification notification = builder.build();
        //notification.flags=Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        startForeground(1, notification);
    }
}