package com.jesusrodri.localizacionproyecto;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
//import com.example.android_localizacionproyecto.R;
//import com.jesusrodri.localizacionproyecto.R;
//import com.jesusrodri.localizacionproyecto.interfaces.ApiInterface_Retrofit;
import com.jesusrodri.localizacionproyecto.interfaces.OnActivityInteractionListener2;
import com.jesusrodri.localizacionproyecto.interfaces.OnFragmentInteractionListener;
import com.jesusrodri.localizacionproyecto.login_files.AppConfig;
import com.jesusrodri.localizacionproyecto.login_files.SessionManager;
//import com.jesusrodri.localizacionproyecto.utils.ApiClient_Retrofit;
//import com.jesusrodri.localizacionproyecto.utils.Coordenadas_Json_Retrofit;
import com.jesusrodri.localizacionproyecto.utils.MyApplication;


import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

//import retrofit2.Call;
//import retrofit2.Callback;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class Fragment_Localizacion extends Fragment implements OnActivityInteractionListener2,
        ServiceConnection{
    /* Variables definidas cuando creamos el fragment desde android studio:
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    */
    //Interface para comuniarse con otros fragments y con MainActivity
    private OnFragmentInteractionListener mListener;

    private Button btnActualizar, btnOcultarAmigo, btnPausar;
    private TextView LblVelocidad,  lblDist, lblTiempo, lblNombreUsuario, lblRuta, lblDia, lblHora, lblnumRuta;


    private TextView lblVelocidadAmigo, lblDistAmigo, lblTiempoAmigo, lblNombreAmigo;
    private LinearLayout layAmigoIzqu,layAmigoDerech,layBusque;

    private double distanciaTotal=0 ;

    private  LocationManager locManager;

    // JSON_PARSER jsonParser = new JSON_PARSER();

    private int frec_Time=12000;
    private String tipoRuta= "12 seg";
    private long totalTime=0;

    String id_cliente, nombre_cliente, email_cliente;

    //private int session_number_value;;  // Numero de sesion que obtendremos del servidor al iniciar la localizacion.
    //Una vez desactivada la sesion subiremos este numero +1 sesion
    private int num_sesion;
    private int num_sesion_DB ; //Numero de sesion relativo a la base de datos interna
    public boolean isGPSEnable = false;

    public boolean sesionSu;
    private boolean closeApp = false;
    private Animation animTranslate;

    public LinearLayout.LayoutParams lparams;

    private TextView textoprueba, textoprueba2, textoprueba3, textoprueba4, textoprueba5;


    private SQLiteDataCoordenadas database_coorde ;
    private SQLiteDataSesiones database_sesiones;
    private SessionManager sessionManager;

    public MyIntentServiceLocalizacion serviceLocalizacion;

    public Fragment_Localizacion() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Fragment_Localizacion newInstance() {
        Fragment_Localizacion fragment = new Fragment_Localizacion();
        //En caso de poner parametros en el constructor Fragment_Localizacion newInstance(String param1, String param2):
        //En este caso no porque usamos un PagerAdapter
        /*
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        */
        return fragment;
    }


    //Enviamos datos a Fragmet_Mapa a traves de mainactivity
    public void onSendingDataToMap(double lng,double lat, double alti, double vel,double dist ) {
        if (mListener != null) {
            mListener.onPassingDataToMapa(lng, lat, alti, vel, dist);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //Para recibir acciones desde MainActivity:
        ((Main2Activity)context).listener2=this;
        //Inicializamos (Solo si quieremos enviar):
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        //((MyIntentServiceLocalizacion)getContext()).sListener=this;  <-- Muestra error de conversion de clases
        //Con lo cual no se pueden usar interfaces para recibir info desde el Servicio al Fragment

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /*
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    */


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* En caso de usar arguemntos en el constructor
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        */
        animTranslate = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_translate);
        mListener.onRequiringClienteFromToMain(); //Solicitamos a mainactivity que nos devuelta el id, email y nombre del logado
        //Obtenemos una referencia al LocationManager
        locManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        MyIntentServiceLocalizacion.MyBinder b = (MyIntentServiceLocalizacion.MyBinder) binder;
        serviceLocalizacion = b.getService();
        //Con serviceLocalizacon podriamos obtener datos del servicio simplemente llamando a alguno de sus metodos
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        serviceLocalizacion=null;

    }

    MyReceiver myReceiver;
    ArrayList<Double> contaElements;

    public class MyReceiver extends BroadcastReceiver{

        double distancia;
        double velocidad =0;
        int difTiempo;
        ArrayList<Integer> arraySeg = new ArrayList<>();
        ArrayList<Coord_Long_Lat> arrayDistanc = new ArrayList<>();


        public MyReceiver() {
            super();
            contaElements = new ArrayList<>(); //Contador de elementos que entran
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            double longi= intent.getDoubleExtra("longi",0);
            double lati= intent.getDoubleExtra("lati",0);
            double alti= intent.getDoubleExtra("alti",0);
            boolean moving = intent.getBooleanExtra("moving",false);
            String cadFecha = intent.getStringExtra("cadFecha");

            String[] fecha_hora = cadFecha.split("\\s+"); //dividimos fecha y hora. Interesa hora que es la parte 1
            String[] hora_min_seg = fecha_hora[1].split(":"); //Creamos un string de tamaño 3, de hora min y seg
            int segundos = horaToSeg(Integer.valueOf(hora_min_seg[0]),
                                    Integer.valueOf(hora_min_seg[1]),
                                    Integer.valueOf(hora_min_seg[2]));
            if(moving){
                ordenar_en_movimiento(longi, lati, alti, segundos);
            }else{
               ordenar_en_parado(longi, lati,alti);
            }
            setUsuRutaDiaHora(nombre_cliente, String.valueOf(num_sesion_DB), getTipoRuta(),"dia_actual","hora_actual");

        }

        public void ordenar_en_movimiento(double longi, double lati, double alti, int segundos){


            //Metemos parejas de coordenadas para calcular su distancia entre los dos puntos
            arrayDistanc.add(new Coord_Long_Lat(longi, lati, alti));
            arraySeg.add(segundos); //Idem para la diferencia entre tiempos.
            contaElements.add(longi);

            if(arrayDistanc.size()==2){

                distancia =Calc_DistyVeloc.distFrom(
                        arrayDistanc.get(0).getLatitud(), arrayDistanc.get(0).getLongitud(),
                        arrayDistanc.get(1).getLatitud(), arrayDistanc.get(1).getLongitud()
                );
                difTiempo = arraySeg.get(1) - arraySeg.get(0);
                totalTime+=difTiempo;
                distanciaTotal+= distancia;
                //Calculamos la velocidad parcial por tramos:
                velocidad = ((distancia *1000*3.6))/difTiempo;
               // Log.i("velocidad",String.valueOf(velocidad)+ " distancia "+String.valueOf(distancia)+" tiempo "+String.valueOf(difTiempo));

                setDatosToTextViews(velocidad, distanciaTotal, totalTime); //Realmente no usaremos totalTime
                //Pasamos al mapa:
                onSendingDataToMap(longi, lati, alti, velocidad, distanciaTotal);
                arrayDistanc.set(0, arrayDistanc.get(1));
                arrayDistanc.remove(1);
                arraySeg.set(0, arraySeg.get(1));
                arraySeg.remove(1);

            }else if(arrayDistanc.size()==1){ //este caso sera solo para poner el primer punto en el mapa
                if(contaPausa < 2){
                  setDatosToTextViews(0, 0, 0); //Realmente no usaremos totalTime
                  //Pasamos al mapa:
                  onSendingDataToMap(longi, lati, alti, 0, 0);
                }
            }

        }

        public void ordenar_en_parado(double longi, double lati, double alti){

            //Si los puntos son iguales en coordenadas , indicamos que la velocidad vale 0
            setDatosToTextViews(0, distanciaTotal, totalTime);  //Realmente no usaremos totalTime
            //Pasamos al mapa:
            onSendingDataToMap(longi, lati, alti, velocidad, distanciaTotal);

        }
        public int  horaToSeg(int horas, int minutos, int segundos){
            return (horas*3600)+(minutos*60)+segundos;
        }
    }



    public void startServicio(){
        isServiceStopped=false;
        mListener.isServiceStopped(false);
        // //Register BroadcastReceiver
        //to receive event from our service
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyIntentServiceLocalizacion.ACTION_PROGRESO);
        getActivity().registerReceiver(myReceiver, intentFilter);
        //Creamos el servicio
        Intent intent = new Intent(getActivity(), MyIntentServiceLocalizacion.class);
        intent.putExtra("id_cliente", id_cliente);
        intent.putExtra("numsesion",num_sesion);
        intent.putExtra("numsesion_db", num_sesion_DB);
        intent.putExtra("tipo_ruta", getFrecuenceTime());



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().startForegroundService(intent);
        } else {
            getActivity().startService(intent);
        }
       // getActivity().startService(intent);
        getActivity().bindService(intent, this, Context.BIND_AUTO_CREATE);

    }


    public boolean isServiceStopped = false;
    public void stopServicio(){
        mListener.isServiceStopped(true);
        Intent intent = new Intent(getActivity(), MyIntentServiceLocalizacion.class);
        getActivity().unbindService(this);
        getActivity().stopService(intent);
        this.isServiceStopped=true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public int contaTime=0;
    Timer timerTiempoTotal;
    public void setTimeCounter(final int contaGlobal){
        timerTiempoTotal = new Timer();
        timerTiempoTotal.scheduleAtFixedRate(new TimerTask() {
            int contaTiempo=contaGlobal;
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contaTiempo++;
                        contaTime=contaTiempo; //Asignamos a una variable global el el tiempo acumulado
                        //formatTiempo(lblTiempo,contaTiempo);
                        lblTiempo.setText(FormateoTiempos.formateoTiempos(contaTiempo));
                    }
                });
            }
        },2000,1000);
    }

    //Comprobacion de que hay conexion a internet.
    protected boolean isOnline() {
        Log.i("hay internet","hay internet");
        ConnectivityManager cm = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean  checkGPS(){
        isGPSEnable = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGPSEnable){
            mListener.onPassingSpinnerStateToMain(false, false); //desactivamos el spinner y ponemos primer elemento
            mListener.onPassingActivarBotonn(true); //indicamos que hemos activado la localizacion
            //Subimos los datos de la sesion
            subir_sesion_volley(id_cliente);
        }else{
            //activamos el gps
            showSettingsAlert();
            //checkGPS();
        }
        return isGPSEnable;
    }

    /* A partir de Android 6.0 (nivel de API 23) hay que establecer permisos en las apps para ciertas funcionalidades como la localizacion en TIEMPO DE EJECUCION, es decir,
        ademas de poner los permisos en el manifest  hay que establecer permiso para que el cliente pulse permitir o denegar tal funcion.
        En caso de no establecer estos permisos , sale un error en tiempo de ejecicion en las llamadas por ejemplo a requestLocationUpdates.
        Para hacer hacer el permiso he usado la libreria Easy Permisions :
        https://github.com/googlesamples/easypermissions
        Ejemplo: https://www.youtube.com/watch?v=iHbdDAOJHIU
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(123)
    private void solicitudPermisoUbicacion() {
        String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(getContext(), perms)) {
            // Already have permission, do the thing
            //Toast.makeText(getContext(), "Ya esta otorgado el permiso ", Toast.LENGTH_SHORT).show();
            bot_Start();

        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "Solicitar permiso de ubicación de nuevo?.Importante: Si no otorga el permiso , no podrá hacer rutas que estan basadas en la ubicacion del dispositivo",
                    123, perms);
        }
    }

    public void bot_Start(){

        distanciaTotal = 0;
        totalTime = 0;
        contaTime=0;
        LblVelocidad.setText("");
        lblDist.setText("");

        if (checkGPS() &&  isOnline()) {
            setTimeCounter(0);
            btnActualizar.setText(getActivity().getString(R.string.desactivar));
            lparams.weight =0.5f;
            btnActualizar.setLayoutParams(lparams);
            btnPausar.setVisibility(View.VISIBLE);
            btnPausar.setText(getActivity().getString(R.string.pausar));
            setUsuRutaDiaHora(nombre_cliente,"", getTipoRuta(),"dia_actual","hora_actual");
        }else{
            alternar=0;
            Toast.makeText(getContext(),"Compruebe ajustes y conexión a internet", Toast.LENGTH_SHORT).show();
        }
    }


    public void botonStart(){
        //ShowNotificationJob.schedulePeriodic();
        solicitudPermisoUbicacion();
    }


    public void botonParar(){

        timerTiempoTotal.cancel();
        // lblTiempo.setText("");
        totalTime=0;
        distanciaTotal=0; //reseteamos la distancia total
        btnActualizar.setText(getActivity().getString(R.string.empezarruta));
        lparams.weight =1f;
        btnActualizar.setLayoutParams(lparams);
        btnPausar.setVisibility(View.GONE);
        alternarPausa=0;
        contaPausa=0;
        if(!isServiceStopped) {
            stopServicio();
            getActivity().unregisterReceiver(myReceiver);
        }

        //Solo subimos los datos si tenemos acumulados minimo dos datos a subir registrados
        if(contaElements.size()>1){ //Cualquiera de los arrays nos vale
            //Log.i("arraydist1", String.valueOf(contaElements.size()));
            //new subir_Datos_LiveaCero().execute();
            if(btnPausar.getText().equals("Pausar")){
                subir_liveacero_y_deleteruta("no",id_cliente, String.valueOf(num_sesion), String.valueOf(tiempoPausadoTotal+contaTiempoPausado));
                database_sesiones.insertTimePaused(String.valueOf(num_sesion_DB), String.valueOf(tiempoPausadoTotal+contaTiempoPausado), id_cliente);
                sessionManager.set_numsesion_db(num_sesion_DB); //almacenamos el numero de sesion en preferencias
            }else if(btnPausar.getText().equals("Reanudar")){
                subir_liveacero_y_deleteruta("no",id_cliente, String.valueOf(num_sesion), String.valueOf(tiempoPausadoTotal));
                database_sesiones.insertTimePaused(String.valueOf(num_sesion_DB), String.valueOf(tiempoPausadoTotal), id_cliente);
                sessionManager.set_numsesion_db(num_sesion_DB); //almacenamos el numero de sesion en preferencias
            }
        }else{
            //Log.i("arraydist2", String.valueOf(contaElements.size()));
            //borramos la sesion (la ruta)  si hay un dato subido o ningun dato
            mListener.onBorrarDatosAlFinalizar(id_cliente,String.valueOf(num_sesion_DB),false);
        }

        //Activamos spinner y lo actualizamos con la nueva ruta
        mListener.onPassingSpinnerStateToMain(true, true); //activamos el spinner para ver los mapas y ponemos el ultimo elemento
        mListener.onPassingActivarBotonn(false); //indicamos que hemos desactivado la localizacion
        //ponemos a cero los contadores de tieimpoTotal
        tiempoPausadoTotal=0;
        contaTiempoPausado=0;
        if(timerTiempoPausado!=null){ //En caso de que desactivemos estando el gps en pausa, cortamos el timer
            timerTiempoPausado.cancel();
        }
    }

    public int alternar=0;
    public int alternarPausa=0;
    public int contaPausa = 0; //Contador para conocer las veces que alternamos el boton de pausa
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);

        //Controles usuario
        lblNombreUsuario = (TextView)view.findViewById(R.id.Lblusu);
        lblRuta =(TextView)view.findViewById(R.id.LblRuta);
        lblnumRuta = (TextView)view.findViewById(R.id.Lbl_numRuta);
        lblDia = (TextView)view.findViewById(R.id.Lbldia);
        lblHora = (TextView)view.findViewById(R.id.LblHora);
        setUsuRutaDiaHora(nombre_cliente,"",getTipoRuta(),"dia_actual","");

        LblVelocidad = (TextView)view.findViewById(R.id.LblVelocidad);
        lblDist = (TextView)view.findViewById(R.id.lblDistancia);
        lblTiempo =  (TextView)view.findViewById(R.id.lblTiempoEmpleado);



        btnActualizar = (Button)view.findViewById(R.id.BtnActualizar);
        textoprueba = (TextView)view.findViewById(R.id.textoprueba);

        database_coorde = new SQLiteDataCoordenadas(getContext());

        textoprueba.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Prueba database coordenadas
                /*
                ArrayList<Coord_Long_Lat> arrayCoord;
                arrayCoord=database_coorde.getCoordenadasWithSesion("28");
                for(Coord_Long_Lat coord: arrayCoord){
                    Log.i("lati :", String.valueOf(coord.getLatitud())+
                            " longi :"+ String.valueOf(coord.getLongitud())+
                            " alti :"+ String.valueOf(coord.getAltitud())
                    );
                }
                */
                //Prueba database fechas from coordenadas:
                /*
                ArrayList<String> dates;
                dates = database_coorde.getDatesofCoordenadasWithSesion(String.valueOf(num_sesion), id_cliente);
                for (String date : dates){
                    Log.i("fechas :", date);

                }
                */

              //  double lal =  serviceLocalizacion.getLatitud();
              //  textoprueba2.setText(String.valueOf(lal));
                //pruebaArrayVollew();
                //enviarCorreo();
                //getActivity().startService(new Intent(getActivity(), MyIntentServiceLocalizacion.class));

            }}
        );
       // textoprueba2 = (TextView)view.findViewById(R.id.textoprueba2);
        database_sesiones = new SQLiteDataSesiones(getContext());

       // textoprueba2.setOnClickListener(new View.OnClickListener(){
           // int numeroses =0;
          //  @Override
           // public void onClick(View v) {
                //getActivity().stopService(new Intent(getActivity(), MyIntentServiceLocalizacion.class));
                /* //añadir sesiones
                database_sesiones.addSesionNum(id_cliente, numeroses);
                numeroses++;
                */
                /*
                ArrayList<Double> arrayDistancias;
                ArrayList<Integer> arrayTiempos;
                ArrayList<Coord_Long_Lat> arrayCoord;
                ArrayList<ArrayList> arraylista;
                arraylista = database_coorde.getDistanciasYtiempos(String.valueOf(num_sesion), id_cliente);
                int timepaused = database_sesiones.getTimePaused(String.valueOf(num_sesion), id_cliente);

                Log.i("tamaño ", String.valueOf(arraylista.size())+" distancias  "+String.valueOf(arraylista.get(0).size()) +
                ", tiempos "+ String.valueOf(arraylista.get(1).size())+ " , coordenadas "+ String.valueOf(arraylista.get(2).size())
                +", timepaused "+ String.valueOf(timepaused));

                if(arraylista.get(0).size() > 1) {
                    arrayDistancias = arraylista.get(0);
                    arrayTiempos = arraylista.get(1);
                    arrayCoord = arraylista.get(2);
                    for (Integer tmp: arrayTiempos){
                        Log.i("tiempos ", String.valueOf(tmp));
                    }
                }else{
                    Log.i("tiempos ","No hay registros que ofrecer");
                }
                */
       //     }
        //});

       // textoprueba3 = (TextView)view.findViewById(R.id.textoprueba3);
       // textoprueba3.setOnClickListener(new View.OnClickListener(){
           // @Override
          //  public void onClick(View v) {
                //mostrar sesiones
                /*
                ArrayList<String> dato = new ArrayList<String>();
                Cursor cursor = database_sesiones.getAllSesions(id_cliente);
                if(cursor.moveToFirst()){
                    while(!cursor.isAfterLast()){
                        dato.add(cursor.getString(4));
                        cursor.moveToNext();
                    }
                    for(int i=0; i<cursor.getCount(); ++i){
                        Log.i("fechas",String.valueOf(dato.get(i)));
                    }
                }
                else{
                    Log.i("cursor", "no hay datos a mostrar");
                }
                */

          //  }
      //  });


      // textoprueba4 = (TextView)view.findViewById(R.id.textoprueba4);
      //  textoprueba4.setOnClickListener(new View.OnClickListener(){
          //  @Override
           // public void onClick(View v) {
                /*borrar seesiones
                ArrayList<String> sesiones = new ArrayList<String>();
                sesiones.add(String.valueOf(2)); sesiones.add(String.valueOf(0));
                database_sesiones.deleteSesion(sesiones);
                */
                /*Actualizar registro . Ej. Subir valor timepaused
                database_sesiones.insertTimePaused("25", "0");
                */
                /*comprobar si una sesion se ha borrado:
                boolean isdeleted = database_sesiones.is_sesion_deleted("26");
                Log.i("deleted? ",String.valueOf(isdeleted));
                */

               // comprobar si las coordenadas de una sesion se han borrado:
                /*
                int count = database_coorde.getCountSesion("26");
                if(count>0){
                    Log.i("isdeleted","no se borraron todas las coordenadas");

                }else{
                    Log.i("isdeleted","si se borraron todas las coordenadas");
                }
                */
                //comprobar el numero de coordenadas de una sesion:
                /*
                int count = database_coorde.getCountSesion(String.valueOf(num_sesion), id_cliente);
                Log.i("numero de coordenadas  ", String.valueOf(count));
                */


           // }
       // });

     //  textoprueba5 = (TextView)view.findViewById(R.id.textoprueba5);
       // textoprueba5.setOnClickListener(new View.OnClickListener(){
          //  @Override
           // public void onClick(View v) {
                /* Prueba multiarray:
                ArrayList<Coord_subir> arrayCoorde = new ArrayList<Coord_subir>();
                arrayCoorde.add(new Coord_subir(40.2545, -3.564, 650.55, id_cliente, 30,"2017-06-04 23:46:28"));
                //arrayCoorde.add(new Coord_subir(40.55545, -4.564, 633.55, id_cliente, 30,"2017-06-04 23:46:30"));
                //arrayCoorde.add(new Coord_subir(41.2545, -5.564, 750.55, id_cliente, 30,"2017-06-04 23:46:40"));
                subir_Coordenadas_Volley2(arrayCoorde);
                */

          //  }
      //  });

        lparams = (LinearLayout.LayoutParams)btnActualizar.getLayoutParams();

        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnActualizar.startAnimation(animTranslate);
                alternar = (alternar+1)%2;
                switch (alternar){
                    case 0:
                        botonParar();
                        break;
                    case 1:

                        botonStart();
                        break;
                }
            }
        });
        btnPausar = (Button)view.findViewById(R.id.BtnPausar);
        btnPausar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                btnPausar.startAnimation(animTranslate);
                alternarPausa = (alternarPausa+1)%2;
                contaPausa++;
                switch (alternarPausa){
                    case 0:
                        reanudarLocalizacion();
                        btnPausar.setText(getActivity().getString(R.string.pausar));
                        break;
                    case 1:

                        pausarLocalizacion();
                        btnPausar.setText(getActivity().getString(R.string.reanudar));
                        break;
                }
            }
        });

        //Controles Contacto
        layAmigoDerech = (LinearLayout)view.findViewById(R.id.layinfderecho);
        layAmigoIzqu =(LinearLayout)view.findViewById(R.id.layinfeizq);
        layBusque = (LinearLayout)view.findViewById(R.id.laytextobuscar);

        btnOcultarAmigo =(Button)view.findViewById(R.id.BtnComenzarAmigo);
        btnOcultarAmigo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mListener.onPassingOcultarAmigo(true); //cancelamos el timer si hay  movimiento
                activarLayoutContacto(false,null, null, null, null);
                //activamos el spinner para ver los mapas y ponemos el primer elemento en caso solo de que el usuario no este en mov.En caso de que este en mov, no se activa
                if(btnActualizar.getText().equals("Empezar"));
                mListener.onPassingSpinnerStateToMain(true, false);
                mListener.onPassingResetSpinnerContactos(); // Reseteamos el spinner de contactos  para que no se quede marcado el contacto.
            }
        });

        lblNombreAmigo = (TextView)view.findViewById(R.id.lblAmigo);
        lblVelocidadAmigo =(TextView)view.findViewById(R.id.LblVelocidadAmigo);
        lblDistAmigo = (TextView)view.findViewById(R.id.lblDistanciaAmigo);
        lblTiempoAmigo = (TextView)view.findViewById(R.id.lblTiempoEmpleadoAmigo);

        return view;
    }


    Timer timerTiempoPausado;
    private int contaTiempoPausado=0;
    public void pausarLocalizacion(){
        // pausamos el reloj
        if(!isServiceStopped){
            stopServicio();
            getActivity().unregisterReceiver(myReceiver);
        }

        timerTiempoTotal.cancel(); //cancelamos el timer
        //contamos el tiempo pausado:
        timerTiempoPausado = new Timer();
        timerTiempoPausado.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run() {
                contaTiempoPausado++;
            }
        },1000,1000);
    }




    public int tiempoPausadoTotal=0;
    public void reanudarLocalizacion(){
        startServicio();
        setTimeCounter(contaTime); //Pasamos el acumulado de tiempo al contandor interno de tiempo y creamos nuevo instancia
        //cancelamos el timer del tiempo pausado y sumamos el tiempo pausado al total:
        timerTiempoPausado.cancel();
        tiempoPausadoTotal+=contaTiempoPausado;
        contaTiempoPausado=0;
        //System.out.println("tiempopausado "+String.valueOf(tiempoPausadoTotal));
    }

    //cuando pulsamos salimos de la app, subimos los datos live a cero:
    @Override
    public void passingOnFinishApp(boolean var) {
        //Comprobamos si esta o no desactivado el boton de activar.
        //EN caso de que este desactivado no hacemos nada, salimos de la app como si nada
        //En caso contrario , llamamos al boton pararLocalizacion
        this.closeApp=var;
        if(var) {
            if(btnActualizar.getText().equals("Desactivar")){ //En este caso tenemos activado el servicio de localizacion, hay que pararlo antes de salir
                timerTiempoTotal.cancel();
                if(timerTiempoPausado!=null)
                    timerTiempoPausado.cancel();
                //Solo subimos los datos si tenemos acumulados minimo dos datos a subir registrados
                if(contaElements.size()>1){ //Cualquiera de los arrays nos vale
                    //new subir_Datos_LiveaCero().execute();  //dentro de esa clase llamamos a onCerrarAPP
                    if(btnPausar.getText().equals("Pausar")){
                        subir_liveacero_y_deleteruta("no",id_cliente, String.valueOf(num_sesion), String.valueOf(tiempoPausadoTotal+contaTiempoPausado));
                        database_sesiones.insertTimePaused(String.valueOf(num_sesion_DB), String.valueOf(tiempoPausadoTotal+contaTiempoPausado), id_cliente);
                        sessionManager.set_numsesion_db(num_sesion_DB); //almacenamos el numero de sesion en preferencias
                    }else if(btnPausar.getText().equals("Reanudar")){
                        subir_liveacero_y_deleteruta("no",id_cliente, String.valueOf(num_sesion), String.valueOf(tiempoPausadoTotal));
                        database_sesiones.insertTimePaused(String.valueOf(num_sesion_DB), String.valueOf(tiempoPausadoTotal), id_cliente);
                        sessionManager.set_numsesion_db(num_sesion_DB); //almacenamos el numero de sesion en preferencias
                    }
                }else{
                    mListener.onBorrarDatosAlFinalizar(id_cliente,String.valueOf(num_sesion_DB),true); // cerramos la app al asegurar el borrado de datos
                }
            }else{ //En este caso cerramos la app normalmente y ponemos el live a cero para que no se quede como si estuvieses siempre conectado
                mListener.onCerrarApp(true);
            }
        }
    }

    //Recibimos desde MainActivity el email, id, nombre del usuario que se loga:
    @Override
    public void passingIdEmailNametoFragment(String id, String email, String name) {
        this.id_cliente=id; this.email_cliente=email; this.nombre_cliente =name;
        // Log.i("comprobacion entrada", id_cliente);
    }

    //Recibimos tambien el numero de ruta, dia y hora selecionado al pulsar cualquier elemento del spinner para ponerlo en la etiqueta
    @Override
    public void passingRutayFechatoMap(String num_ruta, String dia, String hora) {
        if(num_ruta!=null && dia!=null & hora!=null){
            setUsuRutaDiaHora(nombre_cliente,num_ruta, "",dia, hora);

        }else{
            setUsuRutaDiaHora(nombre_cliente,"","","","");
        }
    }

    //Cuanod pulsamos cualquier ruta para verla, mostramos los datos generales en el layout del usuario
    @Override
    public void passingDistVelocTiempoRutas(ArrayList<Integer> tiempos, ArrayList<Double> distancias, int timepaused) {

        //Tiempo total del usuario:
        if(tiempos!=null && distancias!=null){
            int tiempoTotal=0;
            for (int i=0;i<tiempos.size(); ++i){
                tiempoTotal =tiempoTotal + tiempos.get(i);
            }
            //descontamos del tiempo total los posibles tiempos pausados:
            tiempoTotal= tiempoTotal-timepaused;
            //Log.i("eltiempo2",String.valueOf(tiempoTotal));
            // formatTiempo(lblTiempo, tiempoTotal);
            lblTiempo.setText(FormateoTiempos.formateoTiempos(tiempoTotal));
            //Distancia total del usuario
            double distanciaTotal=0;
            for(int i=0; i<distancias.size(); ++i){
                distanciaTotal = distanciaTotal+distancias.get(i);
            }
            if(distanciaTotal<1){
                //Pomemos la distancia en metros
                lblDist.setText(String.format(Locale.getDefault(),"%.2f m",distanciaTotal*1000));
            }else if(distanciaTotal>=1){
                lblDist.setText(String.format(Locale.getDefault(),"%.2f Km",distanciaTotal));
            }
            //Velocidad del usuario:
            double velocidad = (distanciaTotal *3600)/tiempoTotal;
            LblVelocidad.setText(String.format(Locale.getDefault(),"%.2f Km/h",velocidad));
        }else{
            lblTiempo.setText("");
            lblDist.setText("");
            LblVelocidad.setText("");
        }
    }


    //Activamos o desactivamos el layout del amigo para ver sus datos de velocidad , tiempo y distancia
    @Override
    public void activarLayoutContacto(boolean var, String correo, String name, ArrayList<Integer> tiempos, ArrayList<Double> distancias) {
        if (var){
            layAmigoIzqu.setVisibility(View.VISIBLE);
            layAmigoDerech.setVisibility(View.VISIBLE);
            layBusque.setVisibility(View.GONE);
            //
            lblNombreAmigo.setText(getActivity().getString(R.string.user,name));
            //Tiempo total del amigo:
            int tiempoTotal=0;
            for (int i=0;i<tiempos.size(); ++i){
                tiempoTotal =tiempoTotal + tiempos.get(i);
            }
            //formatTiempo(lblTiempoAmigo, tiempoTotal);
            lblTiempoAmigo.setText(FormateoTiempos.formateoTiempos(tiempoTotal));
            //Distancia total del amigo
            double distanciaTotal=0;
            for(int i=0; i<distancias.size(); ++i){
                distanciaTotal = distanciaTotal+distancias.get(i);
            }
            if(distanciaTotal<1){
                //Pomemos la distancia en metros
                lblDistAmigo.setText(String.format(Locale.getDefault(),"%.2f m",distanciaTotal*1000));
            }else if(distanciaTotal>=1){
                lblDistAmigo.setText(String.format(Locale.getDefault(),"%.2f Km",distanciaTotal));
            }
            //Velocidad del amigo:
            double velocidad = (distanciaTotal *3600)/tiempoTotal;
            lblVelocidadAmigo.setText(String.format(Locale.getDefault(),"%.2f Km/h",velocidad));
        }else{
            //Deseleccionamos el contacto y ocultamos los datos
            layAmigoDerech.setVisibility(View.GONE);
            layAmigoIzqu.setVisibility(View.GONE);
            layBusque.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setFrecuenceTime(int frecTime, String tipoRuta) {
        if(btnActualizar.getText().equals(getActivity().getString(R.string.empezarruta))){
            this.frec_Time = frecTime;
            this.tipoRuta = tipoRuta;
            lblRuta.setText(getActivity().getString(R.string.ruta,tipoRuta));
        }

    }
    public int getFrecuenceTime(){
        return  this.frec_Time;
    }
    public String getTipoRuta(){return this.tipoRuta;}

    private void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");
        // Setting Dialog Message
        alertDialog.setMessage("GPS no activado . Quieres ir a los menus de ajustes?");
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }


    public void setUsuRutaDiaHora(String usu, String num_ruta, String ruta, String dia, String hora){

        Date fechaAct = new java.util.Date();
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String cadFecha=formato.format(fechaAct);
        String diayhora[] = cadFecha.split("\\s+"); String diaActual = diayhora[0]; String horaActual = diayhora[1];

        lblNombreUsuario.setText(getActivity().getString(R.string.user,usu));
        lblnumRuta.setText(getActivity().getString(R.string.numruta,num_ruta));
        lblRuta.setText(getActivity().getString(R.string.ruta,ruta));
        if(hora.equals("") && dia.equals("dia_actual")){
            lblDia.setText(getActivity().getString(R.string.dia,diaActual));
            lblHora.setText(getActivity().getString(R.string.hora,""));
        }else if(hora.equals("") && ruta.equals("")) {
            lblDia.setText(getActivity().getString(R.string.dia,""));
            lblHora.setText(getActivity().getString(R.string.hora,""));
            lblRuta.setText(getActivity().getString(R.string.ruta,""));
        }else if(hora.equals("hora_actual") && dia.equals("dia_actual")){
            lblDia.setText(getActivity().getString(R.string.dia,diaActual));
            lblHora.setText(getActivity().getString(R.string.hora,horaActual));
        }else{
            lblDia.setText(getActivity().getString(R.string.dia,dia));
            lblHora.setText(getActivity().getString(R.string.hora,hora));
        }

    }


    //El tiempo no lo usaremos ya que usaremos un cronometro
    public void setDatosToTextViews(double veloc, double distancia, long tiempo) {
        //Escribimos los datos en los textView:
        LblVelocidad.setText(String.format(Locale.getDefault(),"%.2f Km/h",veloc));
        if(distancia< 1) {
            //Pomemos la distancia en metros
            lblDist.setText(String.format(Locale.getDefault(),"%.2f m",distancia*1000));
        }else if(distancia>=1){
            lblDist.setText(String.format(Locale.getDefault(),"%.2f Km",distancia));
        }
    }

    //---------------------------------------------------------------------------
    //-----------FUNCIONES PARA SUBIR LOS DATOS POR VOLLEY -------------------
    //------------------------------------------------------------------------
    //Actualizamos el numero de sesion: (METODO POST). Para APIS bajas no funciona el metodo get de subir sesion.
    public void subir_sesion_volley(final String id){

        String tag_string_req = "req_register";
        Date fechaAct = new java.util.Date();
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String cadFecha=formato.format(fechaAct);
        final String param_server="subirNumSesion";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PARAM_SESIONES, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jObj = new JSONObject(response);

                    int success = jObj.getInt("exito");
                    String mensaje = jObj.getString("mensaje");
                    String updated = jObj.getString("update");
                    if (success == 1) {
                         //Log.i("exitovoley",mensaje);
                        Log.i("2","volley");
                        num_sesion = jObj.getInt("num_sesion"); //Obtenemos numero de sesion
                        sessionManager = new SessionManager(getContext());
                        num_sesion_DB =  sessionManager.get_numsesion_db();
                        database_sesiones.addSesionNum(id_cliente, num_sesion_DB, cadFecha); //Almacenamos numero de sesion en base de datos
                        startServicio();    //comenzamos servicio
                    } else {
                        //Log.i("exitovoley","no exito");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //Con linkedHashMap ordenamos por orden de insercion
                Map<String, String> params = new LinkedHashMap<>();
                params.put("param",param_server);
                params.put("id",id);
                params.put("date",cadFecha);

                return params;

            }
            /*
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                //params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("Content-Type", "application/json; charset=utf-8");
                return params;
            }
            */
        }

        ;
        MyApplication.getInstance().addToRequestQueue(strReq,tag_string_req);
    }

    //Actualizamos el numero de sesion: (METODO GET).
    /*
    public void subir_sesion_volley(final String id){

        Date fechaAct = new java.util.Date();
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String cadFecha=formato.format(fechaAct);

        final android.os.Handler handler = new android.os.Handler();

        String param_server="subirNumSesion";
        // Tag used to cancel the request
        String tag_string_req = "req_register";
        //String parametro = "?id="+id + "&date=" + cadFecha+ "&param="+param_server ;
        String parametro2 = "?id="+id + "&date=" + cadFecha;

        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_PARAM_SESIONES_POST+parametro2, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jObj = new JSONObject(response);

                    int success = jObj.getInt("exito");
                    String mensaje = jObj.getString("mensaje");
                   //String updated = jObj.getString("update");
                    if (success == 1) {
                        Log.i("exitovoley",mensaje);
                       // num_sesion = jObj.getInt("num_sesion"); //Obtenemos numero de sesion
                       // database_sesiones.addSesionNum(id_cliente, num_sesion, cadFecha); //Almacenamos numero de sesion en base de datos
                       // startServicio();    //comenzamos servicio


                    } else {
                        System.out.println("Datos no subidos debido a :" +mensaje);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                //https://stackoverflow.com/questions/26796965/android-volley-basicnetwork-performrequest-unexpected-response-code-400
                // As of f605da3 the following should work
            }
        }
        );
        MyApplication.getInstance().addToRequestQueue(strReq,tag_string_req);
    }
    */

    //Subimos Numero Live poniendolo a Cero y tambien borramos las coordenadas relativas a la ruta
    public void subir_liveacero_y_deleteruta(final String deleteruta,final String num_id, final String sesion_num, final String timepaused){

        final String param_server="subir_numeroliveacero_y_deleteruta";
        String tag_string_req = "req_register";
        //String parametro = "?id="+num_id+"&numeroSesion="+sesion_num+"&timepaused="+timepaused + "&param=" + param_server;

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PARAM_SESIONES, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    int success = jObj.getInt("exito");
                    String mensaje = jObj.getString("mensaje");
                    if (success == 1) {
                        if(closeApp){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mListener.onCerrarApp(true);
                                }
                            });
                        }
                    } else {
                        // Log.i("livecero ", mensaje);
                        if(closeApp){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mListener.onCerrarApp(true);
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                //Con linkedHashMap ordenamos por orden de insercion
                Map<String, String> params = new LinkedHashMap<>();
                params.put("param",param_server);
                params.put("id",num_id);
                params.put("numeroSesion",sesion_num);
                params.put("timepaused",timepaused);
                params.put("deleteruta",deleteruta);
                return params;

            }
        };
        MyApplication.getInstance().addToRequestQueue(strReq,tag_string_req);
    }


    //---------------PBUEBAS ALARMA --------------------------
    /*
    public class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            // For our recurring task, we'll just display a message
            Toast.makeText(getActivity().getApplicationContext(), "I'm running", Toast.LENGTH_SHORT).show();
            Log.i("tostada","im running");
        }
    }
    */
    //--------------------PRUEBA JOB SCHEDULER ------------------------------



    //------------------------------------------------------------------

   /*
    public void enviarCorreo(String cuerpo){
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto","chukksiphone@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject: prueba email");
        emailIntent.putExtra(Intent.EXTRA_TEXT, cuerpo);
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }
    */

    /*
    public void pruebaArrayVollew(){

        // Tag used to cancel the request
        String tag_string_req = "req_register";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PRUEBAARRAYS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jObj = new JSONObject(response);

                    // JSONArray jsonArray = jObj.getJSONArray("array1");
                    //boolean error = jObj.getBoolean("error");
                    //  String mensaje = jObj.getString("array1");

                    // String[] str = jsonArray.split(",");
                    for(int i=0; i<jObj.length();++i){
                        System.out.println("array1 es "+jObj.getString("array1"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity().getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                //pasar array por post en volley :
                //http://stackoverflow.com/questions/36741810/send-arraylist-as-parameter-in-volley-request/38175956#38175956
                ArrayList<String> arraylist1 = new ArrayList<>();
                ArrayList<String> arraylist2 = new ArrayList<>();

                arraylist1.add("0123"); arraylist1.add("4587"); arraylist1.add("7891");
                arraylist2.add("elem4"); arraylist2.add("elem5"); arraylist2.add("elem6");

                Map<String, String> params = new HashMap<String, String>();

                for(int i=0; i<arraylist1.size(); ++i){
                    params.put("array1["+(i)+"]",arraylist1.get(i));
                    params.put("array2["+(i)+"]",arraylist2.get(i));
                }
                return params;
            }
        };

        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);

    }
    */
    //************************************************

    //-------------------------------------------------------------------
    //------------FUNCION PARA SUBIR DATOS POR RETROFIT ----------------
    //---------------------------------------------------------------------
    /*
    //Ver que esta bien ubicado el servidor en utils-AppCliente_Retrofit y en ApInterface_Retrofit
    String stackTrace;
    int contaerror=0;
    public void subir_Coordenadas_retrofit(final double longi, final double lati,final double alti,
                                           final String id,final int sesion_num, final String date){

        textoprueba5.setText("llamadas a subircoordenadas "+contprueba5++);


        ApiInterface_Retrofit apiService = ApiClient_Retrofit.getClient().create(ApiInterface_Retrofit.class);
        Call<Coordenadas_Json_Retrofit> call = apiService.savePost(String.valueOf(longi),
                String.valueOf(lati), String.valueOf(alti), id,String.valueOf(sesion_num),date);

        call.enqueue(new Callback<Coordenadas_Json_Retrofit>(){

            @Override
            public void onResponse(Call<Coordenadas_Json_Retrofit> call, retrofit2.Response<Coordenadas_Json_Retrofit> response) {

                int statusCode = response.code();
                String mensaje = response.body().getMensaje();
                boolean exito = response.body().getExito();
                if(exito){
                    textoprueba2.setText(mensaje+contprueba2++);
                }else{
                    textoprueba3.setText(mensaje+contprueba3++);
                }

            }

            @Override
            public void onFailure(Call<Coordenadas_Json_Retrofit> call, Throwable t) {
                contaerror++;
                // Log error here since request failed
                stackTrace = Log.getStackTraceString(t);
                //e.printStackTrace();
                textoprueba4.setText("Errores recogidos : "+" "+contprueba4++);
                Log.e("errorfatal",stackTrace);
                if(contaerror==5){
                    enviarCorreo(stackTrace);
                }
            }
        });


    }
    */

    /*
    //------------------------------------------------------------------------------------------------------------
    //-----------------PRUEBA DE SERVICIO PARA EJECUTAR LAS PETICIONES EN SEGUNDO PLANO ------------------------------
    //---------------------------------------------------------------------------------------------------------

    public static class MyServiceLocalizacion2 extends Service {
        int counter = 0;
        static final int UPDATE_INTERVAL = 1000;
        private Timer timer = new Timer();

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            doSomethingRepeatedly();
            try {
                new DoBackgroundTask().execute(  //ejecutamos una tarea asincrona dentro de nuestro servicio
                        new URL("http://www.amazon.com/somefiles.pdf"),
                        new URL("http://www.wrox.com/somefiles.pdf"),
                        new URL("http://www.google.com/somefiles.pdf"),
                        new URL("http://www.learn2develop.net/somefiles.pdf"));

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return START_STICKY;

        }
        private void doSomethingRepeatedly() {
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    Log.d("MyService", String.valueOf(++counter));
                }
            }, 0, UPDATE_INTERVAL);
        }
        private int DownloadFile(URL url) {
            try {
                //---simulate taking some time to download a file---
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //---return an arbitrary number representing
            // the size of the file downloaded---
            return 100;
        }
        private class DoBackgroundTask extends AsyncTask<URL, Integer, Long> {
            protected Long doInBackground(URL... urls) {
                int count = urls.length;
                long totalBytesDownloaded = 0;
                for (int i = 0; i < count; i++) {
                    totalBytesDownloaded += DownloadFile(urls[i]);
                    //---calculate percentage downloaded and
                    // report its progress--- Esta llamada invoca al metodo onProgressUpdate()
                    publishProgress((int) (((i+1) / (float) count) * 100));
                }
                return totalBytesDownloaded;
            }
            //este metodo acepta el segundo argumento del asynctask
            protected void onProgressUpdate(Integer... progress) {
                Log.d("Downloading files", String.valueOf(progress[0]) + "% downloaded");
                Toast.makeText(getBaseContext(),
                        String.valueOf(progress[0]) + "% downloaded",
                        Toast.LENGTH_LONG).show();
            }

            //This method is  invoked in the UI thread and is called when the doInBackground() method has finished
            // execution. Este metodo acepta el tercer argumento Long del Asynctask
            protected void onPostExecute(Long result) {
                Toast.makeText(getBaseContext(),
                        "Downloaded " + result + " bytes",
                        Toast.LENGTH_LONG).show();
                stopSelf();
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (timer != null){
                timer.cancel();
            }
            Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        }

    }
    */
}
