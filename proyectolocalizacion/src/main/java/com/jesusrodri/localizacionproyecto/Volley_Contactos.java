package com.jesusrodri.localizacionproyecto;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.jesusrodri.localizacionproyecto.interfaces.OnFragmentInteractionListener;
import com.jesusrodri.localizacionproyecto.login_files.AppConfig;
import com.jesusrodri.localizacionproyecto.utils.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by chukk on 10/02/2016.
 */
public class Volley_Contactos {

    Context context;
    ProgressDialog pDialog;
    OnFragmentInteractionListener fragListener;

    public Volley_Contactos(Context context){
        this.context=context;
        //Inicializamos fragListener:
        if(context instanceof OnFragmentInteractionListener){
            fragListener=((OnFragmentInteractionListener)context);
        }else{
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        pDialog = new ProgressDialog(this.context);
    }

    //---------------------------------------
    //Conectamos con el servidor via volley para obtener los contactos aceptados para mostrarlos en el spinner
    //------------------------------------------
    public void getContactList(final ArrayList<String> arrayContacts, final String email_cliente) {

        final String param_server = "mostrar_contactos";
        // Tag used to cancel the request
        String tag_string_req = "req_register";
        pDialog.setMessage("Recuperando contactos");
        showDialog();
        //String str1 = "?param=" + param_server + "&solicitante=" + email_cliente ;
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PARAM_CONTACTOS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        JSONArray jsonEmails = jObj.getJSONArray("aceptados"); //POdemos tambien seleccionar por email :
                        JSONArray jsonNombres = jObj.getJSONArray("nombres");
                        for (int i = 0; i < jsonEmails.length(); ++i) {
                                arrayContacts.add(jsonNombres.get(i).toString()+"\n"+jsonEmails.get(i).toString());
                        }

                    } else {
                            String errorMsg = jObj.getString("mensaje");
                        Toast.makeText(context.getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context.getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();

            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                //Con linkedHashMap ordenamos por orden de insercion
                Map<String, String> params = new LinkedHashMap<>();
                params.put("param",param_server);
                params.put("solicitante",email_cliente);
                return params;

            }
        };
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //----------------------------------------------------------------
    //Envio de solicitud a un contacto:
    //--------------------------------------------------------------------
    public void enviarSolicitud(final String solicitante, final String solicitado, final String texto){

        final String param_server = "insertar_contactos";
        // Tag used to cancel the request
        String tag_string_req = "req_register";
        pDialog.setMessage("Recuperando contactos");
        showDialog();
        //String str1 = "?param=" + param_server + "&solicitante=" + solicitante + "&solicitado=" + solicitado + "&texto=" + texto;
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PARAM_CONTACTOS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        String mensaje = jObj.getString("mensaje");
                        Toast.makeText(context.getApplicationContext(), mensaje , Toast.LENGTH_SHORT).show();
                        fragListener.enviarSolicitudContactoFirebase(solicitante, solicitado, texto, error, mensaje);

                    } else {
                        String mensaje = jObj.getString("mensaje");
                        Toast.makeText(context.getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
                        fragListener.enviarSolicitudContactoFirebase(solicitante, solicitado, texto, error, mensaje);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context.getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                //Con linkedHashMap ordenamos por orden de insercion
                Map<String, String> params = new LinkedHashMap<>();
                params.put("param",param_server);
                params.put("solicitante",solicitante);
                params.put("solicitado",solicitado);
                params.put("texto",texto);
                return params;

            }
        };
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //----------------------------------------------------
    //---------BUSQUEDA DE SOLICITUDES PENDIENTES POR ACEPTAR O RECHAZAR QUE TIENE EL CONTACTO:
    //-----------------------------------------------------

    public void buscarSolicitudes(final String email){
        final ArrayList<String> arrayEmails = new ArrayList<>();
        final ArrayList<String> arrayTextos = new ArrayList<>();
        final String param_server = "buscar_solicitudes";
        // Tag used to cancel the request
        String tag_string_req = "req_register";
        pDialog.setMessage("Recuperando contactos");
        showDialog();
        //String str1 = "?param=" + param_server + "&solicitado=" + email;
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PARAM_CONTACTOS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    String mensaje = jObj.getString("mensaje");
                    if (!error) {
                        JSONArray jsonArray = jObj.getJSONArray("solicitudes");
                        JSONArray jsonArrayTextos = jObj.getJSONArray("textos");
                        for(int i=0;i<jsonArray.length();++i){
                            arrayEmails.add(jsonArray.get(i).toString());
                            arrayTextos.add(jsonArrayTextos.get(i).toString());
                        }
                        Log.i("arrayEmails ", arrayEmails.toString());
                        fragListener.retornarArraySolicitudesPendientes(arrayEmails, arrayTextos);
                    } else {
                        // String errorMsg = jObj.getString("mensaje");
                        Toast.makeText(context.getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context.getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                //Con linkedHashMap ordenamos por orden de insercion
                Map<String, String> params = new LinkedHashMap<>();
                params.put("param",param_server);
                params.put("solicitado",email);
                return params;
            }
        };

        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //---------------------------------------------------------------------
    //--------- ACEPTAR O RECHAZAR SOLICITUDES SELECCIONADAS A TRAVES DEL ALERTDIALOG
    //-----------------------------------------------------------------------------

    public void acept_rechaz_Solicitudes(final String email, final ArrayList<String> listaContacts, final int acept_rechaz){
        //Lo haremos a traves de solicitud GET ya que cuando hay que pasar arrays por parametros no se pueden repetir las claves con HashMap
        final ArrayList<String> arrayEmails = new ArrayList<>();
        final String param_server = "aceptar_o_rechazar_solic";
        // Tag used to cancel the request
        String tag_string_req = "req_register";
        pDialog.setMessage("Recuperando contactos");
        showDialog();
        /*
        StringBuilder params = new StringBuilder();
        String str1 = "?solicitado=" + email + "&aceptar_o_rechazar=" + String.valueOf(acept_rechaz) + "&param=" + param_server;
        params.append(str1);
        for(String str2: listaContacts)
            params.append("&array_contactos[]="+str2);
        */
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PARAM_CONTACTOS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                   // boolean error = jObj.getBoolean("error");
                    String mensaje = jObj.getString("mensaje");
                    Toast.makeText(context.getApplicationContext(),mensaje, Toast.LENGTH_SHORT).show();
                    //Una vez aceptada o rechazada las solicitudes, hay que actualizar el spinner de contactos
                    fragListener.actualizarSpinnerContactos();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context.getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();

            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                //Con linkedHashMap ordenamos por orden de insercion
                Map<String, String> params = new LinkedHashMap<>();
                params.put("param",param_server);
                params.put("aceptar_o_rechazar",String.valueOf(acept_rechaz));
                params.put("solicitado",email);
                for(int i=0; i<listaContacts.size(); ++i){
                    params.put("array_contactos["+(i)+"]",listaContacts.get(i));
                }
                return params;
            }
        }
        ;
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //----------------------------------------------------------
    // --- BORRAR CONTACTOS QUE SE TENGAN EN LA LISTA COMO AGREGADOS
    //    Obtenemos la lista de contactos y luego pasamos el array a Main2 para posteriormente mostrarlos en AlertDialog
    //  1.  pillamos la lista de contactos
    //  2   Borramos los contactos seleccionados a traves de alertDialog
    //---------------------------------------------------------------

    //1:
    public void getListaContactos(final String email_cliente) {

        final String param_server = "mostrar_contactos";
        // Tag used to cancel the request
        String tag_string_req = "req_register";
        pDialog.setMessage("Recuperando contactos");
        showDialog();
        final ArrayList<String> arrayContactos = new ArrayList<>();
        //String str1 = "?param=" + param_server + "&solicitante=" + email_cliente;
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PARAM_CONTACTOS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {

                        JSONArray jsonContactos = jObj.getJSONArray("aceptados");
                        for (int i = 0; i < jsonContactos.length(); ++i) {
                            arrayContactos.add(jsonContactos.get(i).toString());
                        }
                        fragListener.retornarListaContactos(arrayContactos);

                    } else {
                        String errorMsg = jObj.getString("mensaje");
                        Toast.makeText(context.getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context.getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();

            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                //Con linkedHashMap ordenamos por orden de insercion
                Map<String, String> params = new LinkedHashMap<>();
                params.put("param",param_server);
                params.put("solicitante",email_cliente);
                return params;

            }
        };
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //2:     -----------------

    public void borrarContactosFromDialog(final String email, final ArrayList<String> listaContacts){
        //Lo haremos a traves de solicitud GET ya que cuando hay que pasar arrays por parametros no se pueden repetir las claves con HashMap
        final ArrayList<String> arrayEmails = new ArrayList<>();
        final String param_server = "borrar_contactos";
        // Tag used to cancel the request
        String tag_string_req = "req_register";
        pDialog.setMessage("Recuperando contactos");
        showDialog();
        /*
        StringBuilder params = new StringBuilder();
        String str1 = "?param=" + param_server + "&solicitante="+email;
        params.append(str1);
        for(String str2: listaContacts)
            params.append("&array_contactos[]="+str2);
        */
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PARAM_CONTACTOS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    // boolean error = jObj.getBoolean("error");
                    String mensaje = jObj.getString("mensaje");
                    Toast.makeText(context.getApplicationContext(),mensaje, Toast.LENGTH_SHORT).show();
                    //Una vez borrados los contactos, hay que actualizar el spinner de contactos
                    fragListener.actualizarSpinnerContactos();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context.getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();

            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                //Con linkedHashMap ordenamos por orden de insercion
                Map<String, String> params = new LinkedHashMap<>();
                params.put("param",param_server);
                params.put("solicitante",email);
                for(int i=0; i<listaContacts.size(); ++i){
                    params.put("array_contactos["+(i)+"]",listaContacts.get(i));
                }
                return params;

            }
        };
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /// ENVIO DE TORKEN DE REGISTRO AL SERVIDOR para gestion de contactos a traves de Firebase Messaging SIN USAR BASES DE DATOS
    //Ver : https://www.youtube.com/watch?v=LiKCEa5_Cs8 (Deprecado y no funciona)

    public void sendRegistrationToken(final String email, final String token){
        final String param_server = "insertToken";
        // Tag used to cancel the request
        String tag_string_req = "req_register";
        pDialog.setMessage("Recuperando FCM");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PARAM_CONTACTOS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    String mensaje = jObj.getString("mensaje");
                    Toast.makeText(context.getApplicationContext(),mensaje, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context.getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                //Con linkedHashMap ordenamos por orden de insercion
                Map<String, String> params = new LinkedHashMap<>();
                params.put("param",param_server);
                params.put("email",email);
                params.put("token", token);

                return params;
            }
        };
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //Envio de notitificaciones al servidor
    public void sendNotificationtoServer(){

        final String param_server = "send_notif";
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        final String token="fqT5oHssQQiBuleSYwCaEr:APA91bEStxTrbOoU8am54-dybtt1nyzpwcqaamBNxndvPHKIawSEx02UNWRn361JjVp-JiHBdPj9aTFFeAF48UMjL0KFhExDwW8WEySrHjs3-F0SjaX5VOa_NMkHBsPRl0Fg3rsz2pJj";
        final String message ="aqui estamos tronco";
        pDialog.setMessage("Recuperando FCM");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PARAM_CONTACTOS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    Log.i("recibida notificacion ",jObj.toString());
                    //String mensaje = jObj.getString("mensaje");
                    //Toast.makeText(context.getApplicationContext(),mensaje, Toast.LENGTH_SHORT).show();


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context.getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                //Con linkedHashMap ordenamos por orden de insercion
                Map<String, String> params = new LinkedHashMap<>();
                params.put("param",param_server);
                params.put("token",token);
                params.put("message", message);

                return params;
            }
        };
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /* *******************************************************************/

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

}
