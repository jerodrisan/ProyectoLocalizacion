package com.jesusrodri.localizacionproyecto;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.jesusrodri.localizacionproyecto.login_files.AppConfig;
import com.jesusrodri.localizacionproyecto.utils.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
  Esta clase se creo para mandar por interface el numero de sesion desde Fragment_Localizacion a MyServiceLocalizacion
 sin embargo no se usara.
 Este inferface habria que implantarlo en MyServiceLocacion

 */
public class Volley_Coordenadas {


    public Context context;
    public int num_sesion;
    public onPassingNumSession numListener;

    public Volley_Coordenadas(Context contexto){
        this.context=contexto;
        //Inicializamos fragListener:
        /*
        if(context instanceof OnFragmentInteractionListener){
            fragListener=((OnFragmentInteractionListener)context);
        }else{
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        */
       // numListener = new MyServiceLocalizacion();
       // numListener = new Fragment_Localizacion();
    }



    public interface onPassingNumSession{
        public void onGettingNumSess(int num_sesion);
    }

    //Subimos coordenadas
    public void subir_Coordenadas(Location location, String id, int sesion_num){

        String longitud = String.valueOf(location.getLongitude());
        String latitud = String.valueOf(location.getLatitude());
        String altitud = String.valueOf(location.getAltitude());

        Date fechaAct = new java.util.Date();
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = formato.format(fechaAct);
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        String str1 = "?longitud=" + String.valueOf(longitud) + "&latitud=" + String.valueOf(latitud) + "&altitud=" + String.valueOf(altitud)
                +"&id=" + id + "&sesion_num=" + sesion_num + "&date=" + String.valueOf(date);

        Log.i("string1 ", str1);
        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_PARAM_COORDENADAS+str1, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                     boolean error = jObj.getBoolean("exito");
                    String mensaje = jObj.getString("mensaje");
                    Toast.makeText(context.getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
                    //Una vez aceptada o rechazada las solicitudes, hay que actualizar el spinner de contactos
                    //fragListener.actualizarSpinnerContactos();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context.getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
             //   hideDialog();

            }
        });
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    //Actualizamos el numero de sesion:
    public void subir_sesion(final String id, final Location loc){

        final android.os.Handler handler = new android.os.Handler();
        String param_server="subirNumSesion";
        // Tag used to cancel the request
        String tag_string_req = "req_register";
        String str1 = "?id=" + id + "&param=" + param_server;

        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_PARAM_SESIONES+str1, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);

                    int success = jObj.getInt("exito");
                    String mensaje = jObj.getString("mensaje");
                    if (success == 1) {
                        num_sesion = jObj.getInt("num_sesion");
                        //System.out.println("Conseguido el numero de sesion "+num_sesion);
                        //ponemos las corrdenadas dentro de hilo2 una vez establecida la sesion
                        numListener.onGettingNumSess(num_sesion);

                        if(loc!=null){

                            //Dejamos pasar un tiempo para subir el primer dato ya que si se consigue la primera localizacion
                            //getLastKnownLocation inmediatamente , se podria subir accidentalemnte el valor num_sesion=0 al servidor
                            //al ejecutarse new Thread(new hilo(loc)).start(); un poco antes que num_sesion = json.getInt("num_sesion");

                            handler.postDelayed(new Runnable(){ //
                                @Override
                                public void run() {
                                   // new Thread(new hilo(loc)).start();
                                    subir_Coordenadas(loc, id, num_sesion);
                                }
                            }, 200);

                        }

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
                Toast.makeText(context.getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                //   hideDialog();

            }
        });
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);

       // return num_sesion;
    }
}
