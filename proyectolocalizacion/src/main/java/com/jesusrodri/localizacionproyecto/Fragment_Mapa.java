package com.jesusrodri.localizacionproyecto;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//import com.example.android_localizacionproyecto.R;
//import com.jesusrodri.localizacionproyecto.R;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.jesusrodri.localizacionproyecto.interfaces.OnActivityInteractionListener;
import com.jesusrodri.localizacionproyecto.interfaces.OnFragmentInteractionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jesusrodri.localizacionproyecto.login_files.AppConfig;
import com.jesusrodri.localizacionproyecto.utils.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class Fragment_Mapa extends Fragment implements OnActivityInteractionListener, OnMapReadyCallback {
  /*
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    */
   // private ArrayList<Coord_Long_Lat> arrayCoord;
    private String id_cliente, nombre_cliente, email_cliente;
    private String nombre_amigo, email_amigo;
    private int vista=0;
    private GoogleMap mapa ;
    private SupportMapFragment supportMapFragment;
    private OnFragmentInteractionListener mListener; //Si queremos enviar desde Fragment_map
    private Button botonVerUsuario, botonVerAmigo, botonTipoVista;
    private TextView textDistancia;
   // String num_sesion; //numero de ruta o numero de sesion
    public boolean verUsuario = false;
    public boolean verAmigo = false;
    public float nivelZoom; // Origilmente ponemos un zoom de 16 pero luego cambia si aumentamos o disminuimos manualmente

    public double lngUsu=0, latUsu=0, altUsu=0;
    public String velocidad, distancia;
    public long tiempo;

    public double lngAmig=0, latAmig=0, altAmig=0;

    public ArrayList<Polyline> arrayPolylineas = new ArrayList<>();
    public Polyline lineaSecuencial;
    public PolylineOptions  lineaSecuencialOption;
    public LatLng mapCenterUsuario;
    public Marker markerSecuenciaInic, markerSecuenciaFin, markerSecuenciaAuxi;

    public Polyline lineaRuta;
    public PolylineOptions lineaRutaOptions;
    public Marker markerRutaInicio, markerRutaFinal;

    public Polyline lineaRutaAmigo;
    public PolylineOptions lineaRutaOptionsAmigo;
    public LatLng mapCenterAmigo;
    public Marker markerRutIniAmig, markerRutFinAmig;

    private Animation animTranslate;
    private Context mContext;



    public Fragment_Mapa() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Fragment_Mapa newInstance() {
        Fragment_Mapa fragment = new Fragment_Mapa();
        /*
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        */
        return fragment;
    }
    /*
    //Si queremos enviar a mainacticity
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //Inicializamos el interface que esta en MainActivity para recibir:
        ((Main2Activity)context).listener=this;
       // Si queremos enviar desde Fragment_Map:
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        */
       // arrayCoord = new ArrayList<>();
       // mListener.onRequiringClienteFromToMain(); //Solicitamos a mainactivity que nos devuelta el id, email y nombre del logado
        animTranslate = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_translate);

    }

    public void alternarVista(){
        vista = (vista + 1) % 2;
        switch(vista){
            case 0:
                mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                botonTipoVista.setText("Vista\nSatelite");
                break;
            case 1:
                mapa.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                botonTipoVista.setText("Vista\nNormal");

                break;
        }
    }


    //Hacemos uso de la libreria Easy Permisions para que una vez logados nos pida permiso para la localizacion en getLastLocation()
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @SuppressWarnings({"MissingPermission"})
    @AfterPermissionGranted(124)
    private void solicitudPermisoUbicacion() {
        String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(getContext(), perms)) {
            // Already have permission, do the thing
            //Toast.makeText(getContext(), "Ya esta otorgado el permiso ", Toast.LENGTH_SHORT).show();
            FusedLocationProviderClient cliente = LocationServices.getFusedLocationProviderClient(getActivity());
            cliente.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location!=null){ //Pillamos la localizacion actual :
                        mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10F));
                       // Log.i("Localizacion actual ", String.valueOf(location.getLatitude())+"  "+String.valueOf(location.getLongitude()));
                    }else{
                        //Centramos el mapa en España y con nivel de zoom 10
                        CameraUpdate camUpd2 =	CameraUpdateFactory.newLatLngZoom(new LatLng(40.41, -3.69), 10F);
                        mapa.animateCamera(camUpd2);
                        //Log.i("Localizacion actual ", "40,41 + 3,69");
                        //o bien
                        //LatLng mapCenter = new LatLng(40.41, -3.69);
                        //mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCenter, 16F));

                    }
                }
            });
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "Solicitar permiso de ubicación de nuevo?. Si no otorga el permiso , no se podrá usar la app",
                    124, perms);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;
        mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //Obtenemos el nivel de zoom al hacer pinzar con los dedos el mapa
        nivelZoom = mapa.getCameraPosition().zoom;
        //"Lat: " + mapa.getCameraPosition().target.latitude + "\n" +
        //"Lng: " + mapa.getCameraPosition().target.longitude + "\n" +
        //"Orientación: " + mapa.getCameraPosition().bearing + "\n" +
        //"Ángulo: " + mapa.getCameraPosition().tilt,
        //Log.i("zoom ",String.valueOf(nivelZoom)+ "Lat: " + mapa.getCameraPosition().target.latitude );
        solicitudPermisoUbicacion();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();

        FragmentManager fm = getActivity().getSupportFragmentManager();/// getChildFragmentManager();
        supportMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if (supportMapFragment == null) {
            supportMapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, supportMapFragment).commit();
        }
        supportMapFragment.getMapAsync(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContext = getActivity();
        View vista = inflater.inflate(R.layout.fragment__mapa, container, false);

        botonTipoVista = (Button)vista.findViewById(R.id.tipovista);
        botonTipoVista.setText("Vista\nSatelite");
        botonTipoVista.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                alternarVista();
            }
        });

        botonVerUsuario = (Button)vista.findViewById(R.id.vistausuario);
        botonVerUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                botonVerUsuario.startAnimation(animTranslate);
                verUsuario=true;
                verAmigo=false;
                if(mapCenterUsuario!=null)
                    mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCenterUsuario, 15F));
            }
        });
        botonVerAmigo =(Button)vista.findViewById(R.id.vistaamigo);
        botonVerAmigo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                botonVerAmigo.startAnimation(animTranslate);
                verUsuario=false;
                verAmigo=true;
                if(mapCenterAmigo!=null)
                    mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCenterAmigo, 15F));
            }
        });
        textDistancia = (TextView)vista.findViewById(R.id.textViewDistancia);

        return vista;
    }
    /* Ya no lo usaremos:
    public void BorrarRuta(){
        //String[] numer_sesion ={num_sesion};
        ArrayList<String> numer_sesion = new ArrayList<>();
        numer_sesion.add(num_sesion);
        mListener.OnPassingBorrarRuta(numer_sesion);
    }
    */

    /*------------------------------------------------------------------------------------------*/
    /*----------------------------INICIO IMPLEMENTACION RADARES ----------------------------------*/
    /*------------------------------------------------------------------------------------------*/


    @Override
    public void Passing_BotonStart_Estado(boolean state) { //Recibimos el estado del servicio si esta activado o no

        if(state){
            //Log.i("servicio activado ", "si"); //Por si queremos poner algo aqui
        }else{
            conta_radar=0; conta1=0; conta2=0; conta3=0;conta4=0; conta5=0;
           //Log.i("servicio activado ", "no");
        }
    }

    public boolean isRadar_on= false;
    @Override
    public void Passing_Gestion_Radares(String respuest) {

         if(respuest.equals("si")){
             isRadar_on=true;
             activar_Radares(respuest);
         }else if(respuest.equals("no")){
             isRadar_on=false;
             desactivar_Radares();
         }
    }

    private ArrayList<Coord_Long_Lat> arrayCoordeRadares; //Coordenadas de todos los radares.
    private ArrayList<Coord_Long_Lat> arrayCoordeRadares_NE; //Radares en zona Norte-Este . Latitud >40.00000 . Longitud >-4.00000
    private ArrayList<Coord_Long_Lat> arrayCoordeRadares_NW; //Radares zona Norte -Oeste . Latitud  > 40.00000 . Longitud <-4.00000
    private ArrayList<Coord_Long_Lat> arrayCoordeRadares_SE; //Radares zona Sur -Este .     Latitud <40.00000  . Longitud >-4.00000
    private ArrayList<Coord_Long_Lat> arrayCoordeRadares_SW; //Radares zona Sur Oeste       Latitud <40.00000  . Longitud <-4.00000


    private ArrayList<Marker> arrayMarkerRadares ;

    public void activar_Radares(String resp){

        String str = "?radares="+resp;
        String tag_string_req = "req_register";

        StringRequest strReq = new StringRequest(Request.Method.GET, AppConfig.URL_GET_RADARES + str, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    JSONArray jsonArray1 = new JSONArray(response);

                    arrayCoordeRadares = new ArrayList<>();
                    arrayCoordeRadares_NE = new ArrayList<>(); arrayCoordeRadares_NW = new ArrayList<>();
                    arrayCoordeRadares_SE = new ArrayList<>(); arrayCoordeRadares_SW = new ArrayList<>();

                    //Dividimos la peninsula en 4 zonas y metemos los radares dependiendo de su localizacion en una de las zonas:

                    //Log.i("arrayCoordeRadares ", String.valueOf(arrayCoordeRadares.size()));

                    for (int i = 0; i < jsonArray1.length(); ++i) {
                        JSONObject jsonObject = jsonArray1.getJSONObject(i);
                        String name = jsonObject.getString("name");
                        String coordenadas = jsonObject.getString("Point/coordinates");

                        String tipo_radar = name.substring(0,4);
                        String pais_radar = name.substring(9,11);
                        String info_radar = name.substring(12);

                        String str[] = coordenadas.split(",");
                        Double longitud = Double.valueOf(str[0]);
                        Double latitud = Double.valueOf(str[1]);
                        Double alti = Double.valueOf(str[2]);

                        arrayCoordeRadares.add(new Coord_Long_Lat(longitud, latitud, alti, tipo_radar, pais_radar, info_radar));
                       // Log.i("longitud ", String.valueOf(arrayCoordeRadares.get(i).getLongitud()));
                       // Log.i("RADARES NE ", arrayCoordeRadares.get(i).getTipo_radar()+"  "+arrayCoordeRadares.get(i).getPais_radar()+"  "+arrayCoordeRadares.get(i).getInfo_radar());
                        //Creamos 4 zonas de radares de la peninsula y vamos metiendolos en su zona:

                        if(latitud> 40.00000 && longitud >-4.00000){
                            arrayCoordeRadares_NE.add((new Coord_Long_Lat(longitud,latitud,alti, tipo_radar, pais_radar, info_radar)));
                            //Log.i("RADARES NE ", arrayCoordeRadares_NE.get(i).getTipo_radar()+"  "+arrayCoordeRadares_NE.get(i).getPais_radar()+"  "+arrayCoordeRadares_NE.get(i).getInfo_radar());

                        }else if(latitud> 40.00000 && longitud <-4.00000 ){
                            arrayCoordeRadares_NW.add((new Coord_Long_Lat(longitud,latitud,alti, tipo_radar, pais_radar, info_radar)));

                        }else if (latitud < 40.00000 && longitud >-4.00000){
                            arrayCoordeRadares_SE.add((new Coord_Long_Lat(longitud,latitud,alti, tipo_radar, pais_radar, info_radar)));

                        }else{ //latitud < 40.00000 && longitud >-4.00000
                            arrayCoordeRadares_SW.add((new Coord_Long_Lat(longitud,latitud,alti, tipo_radar, pais_radar, info_radar)));
                        }
                    }
                    /*
                    for (Coord_Long_Lat inforadares: arrayCoordeRadares_NE){
                        Log.i("RADARES NE ", inforadares.getTipo_radar()+"  "+inforadares.getPais_radar()+"  "+inforadares.getInfo_radar());
                    }
                    */
                    /*
                    Log.i("longitud total ", String.valueOf((arrayCoordeRadares.size())));
                    Log.i("longitud NE ", String.valueOf((arrayCoordeRadares_NE.size())));
                    Log.i("longitud NW ", String.valueOf((arrayCoordeRadares_NW.size())));
                    Log.i("longitud SE ", String.valueOf((arrayCoordeRadares_SE.size())));
                    Log.i("longitud SW ", String.valueOf((arrayCoordeRadares_SW.size())));
                    */
                        //Desplegamos los radares en el mapa :
                    desplegar_Radares(arrayCoordeRadares);



                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();

            }
        });
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    Marker markerRadar = null;
    private void desplegar_Radares( ArrayList<Coord_Long_Lat> arrayCoordeRadares){

        arrayMarkerRadares = new ArrayList<>();
       // Log.i("arrayMarkerRadares ", String.valueOf(arrayMarkerRadares.size()));
        for (Coord_Long_Lat arrayRad: arrayCoordeRadares){

            String tipo_radar = arrayRad.getTipo_radar();

            switch (tipo_radar){

                case "SEMA":
                    tipo_radar = "Radar SEMAFORO";
                    break;
                case "F020":
                    tipo_radar = "Radar Fijo límite 20";
                    break;
                case "F030":
                    tipo_radar = "Radar Fijo límite 30";
                    break;
                case "F040":
                    tipo_radar = "Radar Fijo límite 40";
                    break;
                case "F050":
                    tipo_radar = "Radar Fijo límite 50";
                    break;
                case "F060":
                    tipo_radar = "Radar Fijo límite 60";
                    break;
                case "F070":
                    tipo_radar = "Radar Fijo límite 70";
                    break;
                case "F080":
                    tipo_radar = "Radar Fijo límite 80";
                    break;
                case "F090":
                    tipo_radar = "Radar Fijo límite 90";
                    break;
                case "F100":
                    tipo_radar = "Radar Fijo límite 100";
                    break;
                case "F120":
                    tipo_radar = "Radar Fijo límite 120";
                    break;
                case "F_VA":
                    tipo_radar = "Radar Fijo Variable";
                    break;
                case "T_FI":
                    tipo_radar = "Radar tramo final";
                    break;
                case "T040":
                    tipo_radar="Radar tramo inicio límite 40";
                    break;
                case "T050":
                    tipo_radar="Radar tramo inicio límite 50";
                    break;
                case "T060":
                    tipo_radar="Radar tramo inicio límite 60";
                    break;
                case "T070":
                    tipo_radar="Radar tramo inicio límite 70";
                    break;
                case "T080":
                    tipo_radar="Radar tramo inicio límite 80";
                    break;
                case "T090":
                    tipo_radar="Radar tramo inicio límite 90";
                    break;
                case "T100":
                    tipo_radar="Radar tramo inicio límite 100";
                    break;
                case "T120":
                    tipo_radar="Radar tramo inicio límite 120";
                    break;
                case "P050":
                    tipo_radar= "Radar tunel limite 50";
                    break;
                case "P060":
                    tipo_radar= "Radar tunel limite 60";
                    break;
                case "P070":
                    tipo_radar= "Radar tunel limite 70";
                    break;
                case "P080":
                    tipo_radar= "Radar tunel limite 80";
                    break;
                case "P100":
                    tipo_radar= "Radar tunel limite 100";
                    break;
                default:
                    tipo_radar= "Sin información";
            }

            markerRadar = mapa.addMarker(new MarkerOptions().
                    position(new LatLng(arrayRad.getLatitud(), arrayRad.getLongitud())).
                     title(tipo_radar).snippet(arrayRad.getInfo_radar()).
                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            markerRadar.showInfoWindow();

            arrayMarkerRadares.add(markerRadar); //Metemos todos los radares en un array

        }
        //prueba distancias google vs calculadora de distancias app:
       // Double lat1 = 40.447928, long1 = -3.543301, lat2 = 40.44718, long2 = -3.53788;
       // Log.i("distancia app ", String.valueOf(Calc_DistyVeloc.distFrom(long1, lat1, long2, lat2)));

        //Vemos posicion de radares como informacion cuando hacemos click
        mapa.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                //Log.i("posicion marker", String.valueOf(marker.getPosition().latitude) +"   "+ String.valueOf(marker.getPosition().longitude));
                marker.getTitle();
                marker.getSnippet();
                marker.showInfoWindow();
                return true;
            }
        });
    }

    private void desactivar_Radares(){
        if(markerRadar!=null){
            for (Marker arrayRad: arrayMarkerRadares){
                arrayRad.remove();
            }
        }
    }

    int conta_radar=0;
    int conta1=0, conta2=0, conta3=0, conta4=0, conta5=0;
    private void dist_usuario_radar (double lng, double lat, double alt, ArrayList<Coord_Long_Lat> arrayCoordeRadar){ //Recibimos longi , lati y alti del usuario en tiempo real


        Double lngRadar, latRadar;
        Double min_dist = 50000.00;
        String tipo_radar="";
        String info_radar="";
        int pos=0;


        conta_radar++;
       // Log.i("contador radar", String.valueOf(conta_radar));

        //recorremos todos los radares y calculamos la distancia hasta el usuario :
        for (int i=0; i<arrayCoordeRadar.size(); ++i) {

            lngRadar = arrayCoordeRadar.get(i).getLongitud(); //longi del radar
            latRadar = arrayCoordeRadar.get(i).getLatitud(); //lati del radar
            Double dist = Calc_DistyVeloc.distFrom(lng, lat, lngRadar, latRadar);

            if ( dist < min_dist){
                min_dist = dist;
                pos = i ;
               tipo_radar = arrayCoordeRadar.get(pos).getTipo_radar();
               info_radar = arrayCoordeRadar.get(pos).getInfo_radar();
            }
        }

       // Log.i("Distancia Radar Usuario", String.valueOf(min_dist)+"  ; tipo radar "+tipo_radar+" ; info radar "+info_radar);

        if ((min_dist < 0.650) //alta velocidad
                &&
                (tipo_radar.equals("F120") || tipo_radar.equals("F100") ||   //fijos
                        tipo_radar.equals("F_VA") || //variables
                        tipo_radar.equals("T_FI") ||//tramo final
                        tipo_radar.equals("T120") //tramo inicio
                )
        ) {
            if (conta1 > 0) {
                if (conta_radar == conta1 + 1) {
                    conta1 = conta_radar;
                   // Log.i("pitar ", "dentro de area de radar , no se pita :" + String.valueOf(conta1));
                } else {
                    conta1 = conta_radar;
                   // Log.i("pitar ", "volvamos a entrar en zona de radar, se pita :" + String.valueOf(conta1));
                    getActivity().startService(new Intent(getActivity(),MyServiceRadarSound.class));
                }
            } else {
                conta1 = conta_radar;
                //Log.i("pitar ", "se pita en la primera pasada :" + String.valueOf(conta1));
                getActivity().startService(new Intent(getActivity(),MyServiceRadarSound.class));
            }

        }else if( (min_dist < 0.500)
                    &&
                (tipo_radar.equals("F090") || tipo_radar.equals("F080") ||
                        tipo_radar.equals("P080") || tipo_radar.equals("P100") ||
                        tipo_radar.equals("T080") ||tipo_radar.equals("T090") ||tipo_radar.equals("T100") )    ){


            if(conta2 > 0){
                if(conta_radar == conta2+1){

                    conta2 = conta_radar;
                    //Log.i("pitar ", "dentro de area de radar , no se pita :"+ String.valueOf(conta2));

                }else{
                    conta2 = conta_radar;
                    //Log.i("pitar ", "volvamos a entrar en zona de radar, se pita :"+ String.valueOf(conta2));
                    getActivity().startService(new Intent(getActivity(),MyServiceRadarSound.class));
                }
            }else{

                conta2 = conta_radar;
               // Log.i("pitar ", "se pita en la primera pasada :"+String.valueOf(conta2));
                getActivity().startService(new Intent(getActivity(),MyServiceRadarSound.class));
            }
        }
        else if ((min_dist < 0.350) //baja velocidad
                &&
                (tipo_radar.equals("F070") || tipo_radar.equals("F060") ||    //fijos
                 tipo_radar.equals("T060") || tipo_radar.equals("T070") ||
                 tipo_radar.equals("P060") || tipo_radar.equals("P070") )){ //de tramo inicio

            if(conta3 > 0){
                if(conta_radar == conta3+1){

                    conta3 = conta_radar;
                   // Log.i("pitar ", "dentro de area de radar , no se pita :"+ String.valueOf(conta3));
                }else{
                    conta3 = conta_radar;
                   // Log.i("pitar ", "volvamos a entrar en zona de radar, se pita :"+ String.valueOf(conta3));
                    getActivity().startService(new Intent(getActivity(),MyServiceRadarSound.class));
                }
            }else{

                conta3 = conta_radar;
               // Log.i("pitar ", "se pita en la primera pasada :"+String.valueOf(conta3));
                getActivity().startService(new Intent(getActivity(),MyServiceRadarSound.class));
            }

        }else if((min_dist < 0.300 )
                  &&
                (tipo_radar.equals("F050") || tipo_radar.equals("T040") || tipo_radar.equals("T050") || tipo_radar.equals("P050") || tipo_radar.equals("F040")) ){

            if(conta4 > 0){
                if(conta_radar == conta4+1){

                    conta4 = conta_radar;
                    //Log.i("pitar ", "dentro de area de radar , no se pita :"+ String.valueOf(conta4));

                }else{
                    conta4 = conta_radar;
                   // Log.i("pitar ", "volvamos a entrar en zona de radar, se pita :"+ String.valueOf(conta4));
                    getActivity().startService(new Intent(getActivity(),MyServiceRadarSound.class));
                }
            }else{

                conta4 = conta_radar;
               // Log.i("pitar ", "se pita en la primera pasada :"+String.valueOf(conta4));
                getActivity().startService(new Intent(getActivity(),MyServiceRadarSound.class));
            }
        }

        else if((min_dist < 0.200)
                &&
                (tipo_radar.equals("F030") || tipo_radar.equals("SEMA")) ){

            if(conta5 > 0){
                if(conta_radar == conta5+1){

                    conta5 = conta_radar;
                   // Log.i("pitar ", "dentro de area de radar , no se pita :"+ String.valueOf(conta5));

                }else{
                    conta5 = conta_radar;
                   // Log.i("pitar ", "volvamos a entrar en zona de radar, se pita :"+ String.valueOf(conta5));
                    getActivity().startService(new Intent(getActivity(),MyServiceRadarSound.class));
                }
            }else{

                conta5 = conta_radar;
               // Log.i("pitar ", "se pita en la primera pasada :"+String.valueOf(conta5));
                getActivity().startService(new Intent(getActivity(),MyServiceRadarSound.class));
            }

        }else{
           // Log.i("Radar", "no hay radares" );
        }

    }


    /*------------------------------------------------------------------------------------------*/
    /*----------------------------FIN IMPLEMENTACION RADARES ----------------------------------*/
    /*------------------------------------------------------------------------------------------*/

    //Recibimos idcliente, nombre y email del usuario por si lo necesitamos
    @Override
    public void passingIdEmailNametoMap(String id, String email, String name) {
       // Log.i("centrarrrr",name);
        this.id_cliente=id;
        this.email_cliente=email;
        this.nombre_cliente=name;
        //botonVerUsuario.setText(getActivity().getString(R.string.centrar_vista)+this.nombre_cliente);
        //Ver StackOverflow para evitar error en Lint:
        //Do not concatenate text displayed with setText. Use resource string with placeholders
        botonVerUsuario.setText(getActivity().getString(R.string.centrar_vista,this.nombre_cliente));
    }

    //Recibimos nombre del amigo cuando pulsamos el spinner de contactos
    @Override
    public void passingNameAmigotoMap(String name) {
        this.nombre_amigo=name;
        botonVerAmigo.setText(getActivity().getString(R.string.centrar_vista,this.nombre_amigo));
    }

    //Recibimos el nivel de zoom que queramos cuando seleccionamos un elemento del spinner ya sea contacto o mapa:
    @Override
    public void PassingZoomLevelToMap(float zoom) {
        this.nivelZoom=zoom;
       // Log.i("pasando zoom2 ", String.valueOf(zoom));
    }

    @Override
    public void PassingStateUsuarioYAmigo(boolean state, boolean state2) { //state= Usuario en mov, state2 = amigo en mov
      //  Log.i("estados ", String.valueOf(state) + " " + String.valueOf(state2));
        if(state && state2){ //Tenemos los usuario y amigo en movimiento, activamos los botones de ver amigo, ver usuario y la distancia
            botonVerAmigo.setVisibility(View.VISIBLE);
            botonVerUsuario.setVisibility(View.VISIBLE);
            textDistancia.setVisibility(View.VISIBLE);
            this.verUsuario=true;
            this.verAmigo=true;
        }else if(!state && !state2){ //En caso contrario los ocultamos
            botonVerAmigo.setVisibility(View.GONE);
            botonVerUsuario.setVisibility(View.GONE);
            textDistancia.setVisibility(View.GONE);
            this.verUsuario=true;
            this.verAmigo=false;
        }else if(state && !state2){
            botonVerAmigo.setVisibility(View.GONE);
            botonVerUsuario.setVisibility(View.VISIBLE);
            textDistancia.setVisibility(View.GONE);
            this.verUsuario=true;
            this.verAmigo=false;
        }else if(!state && state2){
            botonVerAmigo.setVisibility(View.VISIBLE);
            botonVerUsuario.setVisibility(View.GONE);
            textDistancia.setVisibility(View.GONE);
            this.verUsuario=false;
            this.verAmigo=true;
        }
    }

    public void formatDistancia2(){
        double distanciar = Calc_DistyVeloc.distFrom(latUsu, lngUsu, latAmig, lngAmig);
        if(distanciar<1){
            String distanc = String.format(Locale.getDefault(),"%.0f m",distanciar*1000);
            textDistancia.setText(getActivity().getString(R.string.dist_entre_2,distanc));
           // Log.i("latilongiusu ", "usu " + String.valueOf(latUsu) + " " + String.valueOf(lngUsu) +
                //    " amig " + String.valueOf(latAmig) + " " + String.valueOf(lngAmig) + " distan " + distanciar);
        }else{
            String distanc = String.format(Locale.getDefault(),"%.3f Km",distanciar);
            textDistancia.setText(getActivity().getString(R.string.dist_entre_2,distanc));
           // Log.i("latilongiusu ", "usu " + String.valueOf(latUsu) + " " + String.valueOf(lngUsu) +
                //    " amig " + String.valueOf(latAmig) + " " + String.valueOf(lngAmig) + " distan " + distanciar);
        }

    }

    //Recibimos aqui desde Localizacion  a traves de MainActivity las coordenadas del usuario
    @Override
    public void PassingDataToMapa(double lng, double lat, double alt, double vel, double dist) {

        //getActivity().startService(new Intent(getActivity(),MyServiceRadarSound.class));

        if(isRadar_on){
            //Determinamos en que cuadrante del mapa esta el usuario :
            if(lat> 40.00000 && lng >-4.00000){
                dist_usuario_radar(lng, lat,alt, arrayCoordeRadares_NE);

            }else if(lat> 40.00000 && lng <-4.00000 ){
                dist_usuario_radar(lng, lat,alt, arrayCoordeRadares_NW);

            }else if (lat < 40.00000 && lng >-4.00000){
                dist_usuario_radar(lng, lat,alt, arrayCoordeRadares_SE);

            }else{ //latitud < 40.00000 && longitud >-4.00000
                dist_usuario_radar(lng, lat,alt, arrayCoordeRadares_SW);
            }

        }

        this.velocidad=String.format(Locale.getDefault(),"%.1f Km/h",vel); //velocidad ya viene convertida

        if(dist<1){
            this.distancia=String.format(Locale.getDefault(),"%.1f m",dist*1000);
        }else if(dist>=1){
            this.distancia = String.format(Locale.getDefault(),"%.3f Km",dist);
        }
        mostrarPolylineas(lat, lng, velocidad, distancia);
        //Asignamos los valores a variables glbales para luego calcular la distancia entre usuario y amigo
        this.lngUsu = lng; this.latUsu = lat; this.altUsu=alt;
        if(verUsuario && verAmigo && latAmig!=0 && lngAmig!=0){
            formatDistancia2();
        }
    }


    //Pasamosm Desde MainActivity los datos de la ruta del amigo en el mapa.
    @Override
    public void PassingRutaAmigoToMapa(ArrayList<Coord_Long_Lat> arrayList, ArrayList<Integer> tiempos, ArrayList<Double> distancias) {
        if(lineaRutaAmigo!= null){
            lineaRutaAmigo.remove(); //En caso de que haya ruta , la borramos.
            // Log.i("borrada ruta ", lineaRuta.toString());
            markerRutIniAmig.remove();
            markerRutFinAmig.remove();
        }
        if(tiempos!=null && distancias!=null){
            //Distancia total del amigo
            double distanciaTotal=0;
            String distanciaTot="";
            for(int i=0; i<distancias.size(); ++i){
                distanciaTotal = distanciaTotal+distancias.get(i);
            }
            if(distanciaTotal<1){
                //Pomemos la distancia en metros
                distanciaTot =String.format(Locale.getDefault(),"%.1f m", distanciaTotal*1000);
            }else if(distanciaTotal>=1){
                distanciaTot =String.format(Locale.getDefault(),"%.3f Km",distanciaTotal);
            }
            //tiempos del amigo
            int tiempoTotal=0;
            for (int i=0;i<tiempos.size(); ++i){
                tiempoTotal =tiempoTotal + tiempos.get(i);
            }
            //velocidad del amigo:
            double velocidad = (distanciaTotal *3600)/tiempoTotal;
            String veloc = String.format(Locale.getDefault(),"%.0f km/h",velocidad);
            // this.num_sesion = num_sesion; //recibimos el numero de sesion desde MainActivity para poder saber que sesion tenemos activada
            //Madamos solo la distancia total
            mostrarRutaAmigo(arrayList, distanciaTot, veloc);
            //Asignamos los valores a variables glbales para luego calcular la distancia entre usuario y amigo
            this.lngAmig = arrayList.get(arrayList.size()-1).getLongitud();
            this.latAmig = arrayList.get(arrayList.size()-1).getLatitud();
            this.altAmig = arrayList.get(arrayList.size()-1).getAltitud();
            if(verUsuario && verAmigo && latUsu!=0 && lngUsu!=0){
                formatDistancia2();
            }
        }
    }

    //Borramos la ruta que haya dibujada en el mapa del amigo cuando ocultemos el layout del amigo o seleccionemos otro
    @Override
    public void PassingBorrarRutaAmigo(boolean state) {
        if(lineaRutaAmigo!= null){
            lineaRutaAmigo.remove(); //En caso de que haya ruta , la borramos.
            // Log.i("borrada ruta ", lineaRuta.toString());

            markerRutIniAmig.remove();
            markerRutFinAmig.remove();
        }
        if(markerSecuenciaFin!=null)
            markerSecuenciaFin.remove();
        if(markerSecuenciaInic!=null)
            markerSecuenciaInic.remove();

    }


    public String[] calcular_from_to (ArrayList<Coord_Long_Lat> arrayList,
                                  ArrayList<Integer> tiempos,
                                  ArrayList<Double> distancias,
                                  int from, int to,
                                  String total_o_parcial,
                                  int timepaused,
                                  float zoomLevel
                                      ){


        int tiempo_parcial=0;
        int tiempoTotal=0;
        for (int i=from;i<to; ++i){
            tiempoTotal =tiempoTotal + tiempos.get(i);
            if( i == to-1){
                tiempo_parcial=tiempos.get(i); //Tiempo parcial en el tramo que pulsamos
            }
        }

        tiempoTotal = tiempoTotal-timepaused; //descontamos al tiempo total el tiempo pausado;
        String tiempoTot=FormateoTiempos.formateoTiempos(tiempoTotal);
        //Distancia total del usuario
        double distancia_parcial=0;
        double distanciaTotal=0;
        String distanciaTot="";
        for(int i=from; i<to; ++i){
            distanciaTotal = distanciaTotal+distancias.get(i);
            if (i ==to-1){
                distancia_parcial = distancias.get(i); //distancia parcial en el tramo que pulsamos
            }
        }
        if(distanciaTotal<1){
            //Pomemos la distancia en metros
            distanciaTot =String.format(Locale.getDefault(),"%.1f m", distanciaTotal*1000);
        }else if(distanciaTotal>=1){
            distanciaTot =String.format(Locale.getDefault(),"%.3f Km",distanciaTotal);
        }
        //Velocidad del usuario:
        double velocidad = (distanciaTotal *3600)/tiempoTotal;  //velocidad total media del recorrido
        String veloc = String.format(Locale.getDefault(),"%.0f km/h",velocidad);
        double velocidad_parcial = (distancia_parcial*3600)/tiempo_parcial; //velocidad en cada tramo
        String veloc_parcial = String.format(Locale.getDefault(),"%.0f km/h",velocidad_parcial);

        if (total_o_parcial.equals("total")){ //mostramos ruta de principio a fin
            mostrarRuta(arrayList, tiempoTot, distanciaTot, veloc,tiempos, distancias, zoomLevel);
            return null;
        }else{  //En caso de que queramos mostrar marcadores parciales pulsando algun punto de la polilinea
            String [] datos = {tiempoTot,distanciaTot,veloc_parcial};
            return datos;
        }
    }

    //Recibimos desde MainActivito los datos de las rutas guaradas en la base de datos SQlite o servidor y mostramos
    //la ruta seleccionada por el spinner
    @Override
    public void PassingRutaToMapa(ArrayList<Coord_Long_Lat> arrayList,ArrayList<Integer> tiempos, ArrayList<Double> distancias, int timepaused, float zoomLevel) {
        if(lineaRuta!= null){
            lineaRuta.remove(); //En caso de que haya ruta , la borramos.
          // Log.i("borrada ruta ", lineaRuta.toString());
            //Borramos los marcadores que haya
            markerRutaFinal.remove();
            markerRutaInicio.remove();
            if(markerInter!=null)
            markerInter.remove();
        }
        if(tiempos!=null && distancias!=null){
            calcular_from_to(arrayList,tiempos,distancias,0,arrayList.size(),"total", timepaused, zoomLevel);
        }
    }

    //borramos las rutas que haya dibujadas en caso de que pulsemos el primer elemento del spinner,es decir, el titulo
    @Override
    public void PassingBorrarRutasEnMapa(boolean state) {
        if(lineaRuta!= null){
            lineaRuta.remove(); //En caso de que haya ruta , la borramos.
          //  Log.i("borrada ruta ", lineaRuta.toString());
            //Borramos los marcadores que haya
            markerRutaFinal.remove();
            markerRutaInicio.remove();
            if(markerInter!=null)
            markerInter.remove();
        }
    }

    @Override
    public void PassingBorrarSecuenciaEnMapa(boolean state) {
        if(lineaSecuencial !=null) {
            for(int i=0; i<arrayPolylineas.size(); ++i){
                arrayPolylineas.get(i).remove();
            }
            arrayPolylineas.clear();
            lineaSecuencialOption = null;
           // Log.i("borrada secuencia ", lineaSecuencial.toString());
        }
        if(markerSecuenciaFin!=null)
            markerSecuenciaFin.remove();
        if(markerSecuenciaInic!=null)
            markerSecuenciaInic.remove();
    }


    private void mostrarMarcador(final double lat, final double lng) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //centramos camara
               // LatLng mapCenter = new LatLng(lat, lng);
                //mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCenter, 16F));
                mapa.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .title(" "));
            }
        });
    }

    //Mostrar polilineas de forma secuencial del usuario
    private void mostrarPolylineas (final double lat, final double lng ,final String veloc, final String dist){
        if (markerSecuenciaFin!=null){
            markerSecuenciaFin.remove();
        }
        if(lineaSecuencialOption==null){
            lineaSecuencialOption = new PolylineOptions();
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapCenterUsuario = new LatLng(lat, lng);
                //Log.i("pasando ", String.valueOf(lat) + " " + String.valueOf(lng));
                if(!verAmigo && verUsuario)
                    mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCenterUsuario, nivelZoom));
                lineaSecuencialOption.add(new LatLng(lat, lng));
                lineaSecuencialOption.width(8);
                lineaSecuencialOption.color(Color.RED);
                lineaSecuencial = mapa.addPolyline(lineaSecuencialOption); //Asi se van metiendo de dos en dos en la Polyline
                arrayPolylineas.add(lineaSecuencial); //Metemos cada polyline que contiene una pareja en un array para luego poder borrar todas las polyline

                if(markerSecuenciaInic!=null){
                    markerSecuenciaFin = mapa.addMarker(new MarkerOptions().
                                    position(new LatLng(lat, lng)).
                                    title(nombre_cliente).
                                    snippet(veloc + " - " + dist).
                                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    );
                    markerSecuenciaFin.showInfoWindow();

                }
                if(markerSecuenciaFin==null){
                    markerSecuenciaInic = mapa.addMarker(new MarkerOptions().
                                    position(lineaSecuencialOption.getPoints().get(0)).   //obtenemos la primera localizacion
                                    title(nombre_cliente).
                                    snippet("0 m").
                                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    );
                    markerSecuenciaInic.showInfoWindow();
                }
            }
        });
    }


    Marker markerInter=null;
    //Ruta del usuario cuando seleccionamos una ruta especifica guardada en el spinner de rutas:
    private void mostrarRuta (final ArrayList<Coord_Long_Lat> arrayCoord, final String tiempo, final String distancia, final String velocidad,
                              final ArrayList<Integer> tiempos,
                              final ArrayList<Double> distancias,
                              final float zoomLevel){

        lineaRutaOptions = new PolylineOptions();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LatLng mapCenter = new LatLng(arrayCoord.get(0).getLatitud(), arrayCoord.get(0).getLongitud());
                mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCenter, zoomLevel));
                //Log.i("pasando zoom 1 ", String.valueOf(zoomLevel));
                for(int i=0; i<arrayCoord.size(); ++i){
                    lineaRutaOptions.add(new LatLng(arrayCoord.get(i).getLatitud(), arrayCoord.get(i).getLongitud()));
                }
                lineaRutaOptions.width(8);
                lineaRutaOptions.color(Color.RED);
                lineaRuta = mapa.addPolyline(lineaRutaOptions);
                //Cuando pulsemos en el mapa dentro de 10 metros de la interseccion de una polylinea, abriremos un marcador con info
                mapa.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                    @Override
                    public void onMapClick(LatLng clicklatLng) {
                            int i=0;

                            if(markerInter!=null)
                                markerInter.remove();
                            for (LatLng polyCoords : lineaRutaOptions.getPoints()) {

                                float[] results = new float[1];
                                Location.distanceBetween(clicklatLng.latitude, clicklatLng.longitude,
                                        polyCoords.latitude, polyCoords.longitude, results);
                                if (results[0] < 10) {
                                    // If distance is less than 10 meters, this is your polyline
                                    String datos[] = calcular_from_to(arrayCoord,tiempos,distancias, 0,i+1,"parcial",0, zoomLevel);
                                   //  Log.e(TAG, "datos parciales : " +datos[0]+" "+datos[1]+" "+datos[2]);
                                    if(markerInter!=null)
                                           markerInter.remove();
                                    markerInter = mapa.addMarker(new MarkerOptions().
                                                position(polyCoords).
                                                alpha(0.5f).
                                                title(datos[0]).
                                                snippet(datos[1] + " - " + datos[2]).
                                                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                    markerInter.showInfoWindow();
                                    break;
                                }
                                ++i;
                            }
                    }
                });

                //agregamos marcador de inicio y fin
                markerRutaFinal= mapa.addMarker(new MarkerOptions().
                        position(new LatLng(arrayCoord.get(arrayCoord.size()-1).getLatitud(), arrayCoord.get(arrayCoord.size()-1).getLongitud())).
                        title(nombre_cliente+" Fin de ruta").
                        snippet(tiempo + " - " + distancia).
                        icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                markerRutaFinal.showInfoWindow(); //Para ocullar, HideInfowindow
                markerRutaInicio = mapa.addMarker(new MarkerOptions().
                        position(mapCenter).
                        title(nombre_cliente).
                        snippet("Inicio de ruta").
                        icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                markerRutaInicio.showInfoWindow();


            }
        });
    }


    //Ruta del amigo
    private void mostrarRutaAmigo (final ArrayList<Coord_Long_Lat> arrayCoord, final String distancia, final String velocidad){
        lineaRutaOptionsAmigo = new PolylineOptions();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapCenterAmigo = new LatLng(arrayCoord.get(arrayCoord.size()-1).getLatitud(), arrayCoord.get(arrayCoord.size()-1).getLongitud());
                if(verAmigo && !verUsuario)
                    mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCenterAmigo, nivelZoom));
               // Log.i("zoom2 ", String.valueOf(nivelZoom));
                for(int i=0; i<arrayCoord.size(); ++i){
                    lineaRutaOptionsAmigo.add(new LatLng(arrayCoord.get(i).getLatitud(), arrayCoord.get(i).getLongitud()));
                     //Log.i("linea2 ","añadida");
                    // Log.i("amigolati ", String.valueOf(arrayCoord.get(i).getLatitud()+" amigolongi "+String.valueOf(arrayCoord.get(i).getLongitud())));
                }
                lineaRutaOptionsAmigo.width(8);
                lineaRutaOptionsAmigo.color(Color.BLUE);
                lineaRutaAmigo = mapa.addPolyline(lineaRutaOptionsAmigo);
                //marcadores amigo
                markerRutIniAmig = mapa.addMarker(new MarkerOptions().
                        position(new LatLng(arrayCoord.get(0).getLatitud(), arrayCoord.get(0).getLongitud())).
                        title(nombre_amigo).
                        snippet("Inicio de ruta").
                        icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                markerRutIniAmig.showInfoWindow();
                markerRutFinAmig= mapa.addMarker(new MarkerOptions().
                        position(new LatLng(arrayCoord.get(arrayCoord.size()-1).getLatitud(), arrayCoord.get(arrayCoord.size()-1).getLongitud())).
                        title(nombre_amigo).
                        snippet(velocidad +" - "+distancia).
                        icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                markerRutFinAmig.showInfoWindow(); //Para ocullar, HideInfowindow
            }
        });
    }

    @Override
    public void limpiarArrays() {
        //vaciamos todos los arrays
        if(lineaRutaOptionsAmigo!=null)
            lineaRutaOptionsAmigo=null;
        if(lineaRutaOptions!=null)
            lineaRutaOptions=null;
        if(arrayPolylineas!=null)
            arrayPolylineas=null;
        if(lineaSecuencialOption!=null)
            lineaSecuencialOption=null;
    }

}

