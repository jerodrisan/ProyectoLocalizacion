package com.jesusrodri.localizacionproyecto.login_files;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.jesusrodri.localizacionproyecto.Main2Activity;
import com.jesusrodri.localizacionproyecto.R;
//import com.jesusrodri.localizacionproyecto.R;
import com.jesusrodri.localizacionproyecto.utils.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputFullName;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private HashMap<String,String> usuari;
    private static final int time_of_desconexion = 180000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputFullName = (EditText) findViewById(R.id.name);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        // Session manager
        session = new SessionManager(getApplicationContext());
        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());
        usuari = new HashMap<String,String>();
        String email = session.getLastUser();

        //Si ya estamos registrados en el sistema, entonces pasamos directamente a la pantalla de loginActivity o MainActivity
        //dependiendo de si venimos de  una sesion cerrada o si venimos de estar en segundo plano (background)
        if(!email.equals(" ") && !getIntent().getBooleanExtra("registrar_nuevousu",false)){

            usuari= db.getUserDetail(email);
            String id = usuari.get("uid");
           // check_inBackground(id, 0);
           check_in_background();

            //Si no estamos registrados en el sistema, pasamos directamente a la pantalla de loginActivity
        }else if(email.equals(" ") && !getIntent().getBooleanExtra("registrar_nuevousu",false)){

            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String name = inputFullName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    if(name.length()>10) {
                        Toast.makeText(getApplicationContext(),
                                "Introduce un nombre de longitud menor de 10 caracteres!", Toast.LENGTH_LONG)
                                .show();
                    }else if(email.length()>35) {
                        Toast.makeText(getApplicationContext(),
                                "Introduce un email de longitud menor de 35 caracteres!", Toast.LENGTH_LONG)
                                .show();

                    }else if(password.length()>80){
                        Toast.makeText(getApplicationContext(),
                                "Introduce una contraseña de longitud menor de 80 caracteres!", Toast.LENGTH_LONG)
                                .show();
                    }else{
                        registerUser(name, email, password);
                    }

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Por favor, introduce tus datos!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String email = session.getLastUser();
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                i.putExtra("email",email);
                startActivity(i);
                finish();
            }
        });

    }

    //Funcion en caso de que estemos registrados en el sistema
    public void check_in_background(){

        //Si venimos de estar en segundo plano. Entonoces pasamos directamente a MainActivity dependiendo del
        //tiempo de inactividad
        if(!session.isLoggedIn() && session.isInBackGround()){

            long last_time_conected = session.get_time_in_background();
            long current_time = System.currentTimeMillis();
            //Log.i("desconexion ", String.valueOf(last_time_conected)+"   "+String.valueOf(current_time)+ "   "+String.valueOf(current_time-last_time_conected));
            if(current_time - last_time_conected > time_of_desconexion){
                //En este caso no pasamos a MainActivity y nos quedamos en LoginActivity. Se agoto el tiempo de inactividad
                //Cerramos la sesion en el servidor para que no se quede abierta y vamos a la pantalla de LoginActivity
                String email = session.getLastUser();
                cerrar_sesion(email);

            }else{
                //En este caso pasamos a MainActivity directamente
                String email = session.getLastUser();
                String pass = session.getKeyLastpassword();
                session.setLogin(true,email, pass,false);
                goto_MainActivity();
            }
        //Si venimos de una sesion cerrada, entoncemos pasamos a la pantalla de login
        }else if (!session.isLoggedIn() && !session.isInBackGround()){
            goto_LoginActivity();

        }

    }

    public void goto_MainActivity(){
        Intent intent = new Intent(RegisterActivity.this, Main2Activity.class);
        Bundle bundle = new Bundle();
        //bundle.putSerializable("usuariHashMap",usuari); //Ver http://stackoverflow.com/questions/11452859/android-hashmap-in-bundle

        bundle.putString("uid", usuari.get("uid"));
        bundle.putString("name", usuari.get("name"));
        bundle.putString("pass", usuari.get("pass"));
        bundle.putString("email",usuari.get("email"));
        intent.putExtras(bundle);

        startActivity(intent);
        finish();

    }

    public void goto_LoginActivity(){
        String email = session.getLastUser();
        String pass = session.getKeyLastpassword();
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.putExtra("email",email);
        intent.putExtra("pass",pass);
        startActivity(intent);
        finish();

    }
    public void cerrar_sesion(final String email) {
        //Cerramos la sesion en el servidor cambiando el valor de islogged a 0
        final String param_server="cerrarSesion";
        String tag_string_req = "req_register";
        final String tipo_login="islogged";
        //String str1 = "?param=" + param_server + "&email=" + email ;
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PARAM_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try{
                    JSONObject jObj = new JSONObject(response);
                    boolean hecho = jObj.getBoolean("hecho");
                    if(hecho){
                        goto_LoginActivity();
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
                params.put("email",email);
                params.put("tipo_login", tipo_login);
                return params;
            }
        };
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);

    }
    /*
    private void check_inBackground(String id, int inBackground){
        // Tag used to cancel the request
        String tag_string_req = "req_register";
        pDialog.setMessage("Recuperando ...");
        showDialog();
        String params = "?id=" + id + "&setinback=" + inBackground;


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
                    if(hecho && islogged==1 && inbackground==0){
                        Log.i("estalogado",String.valueOf(hecho)+" "+String.valueOf(islogged)+" "+String.valueOf(inbackground)+" "+viene);
                        //Dentro de la pantalla de login, puede que vengamos de estar en segundo plano (inbackgroud), en cuyo caso
                        //pasariamos directamente de loginActivity a MainActivity sin logarnos.
                        //A su vez ya hemos cambiado el valor en la base de datos a no estar en segundo plano
                        goto_MainActivity();

                    }else if(hecho && islogged==0 && inbackground==0){
                        Log.i("estalogadoNO",String.valueOf(hecho)+" "+String.valueOf(islogged)+" "+String.valueOf(inbackground)+" "+viene);
                        //En este caso venimos de una sesion cerrada en cuyo caso
                        // Nos quedamos solo en LoginActivity para logarnos
                        goto_LoginActivity();

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

    /**
         * Function to store user in MySQL database will post params(tag, name,
         * email, password) to register url
         * Subimos los datos y encriptamos la contraseña en el servidor
     * */
    private void registerUser(final String name, final String email,  final String password) {

            final String param_server="registro";
            // Tag used to cancel the request
            String tag_string_req = "req_register";
            pDialog.setMessage("Registering ...");
            showDialog();
            //String str1 = "?param=" + param_server + "&email=" + email + "&password=" + password + "&name=" + name;
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_PARAM_LOGIN, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Register Response: " + response.toString());
                    hideDialog();

                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");
                        if (!error) {
                            // User successfully stored in MySQL
                            // Now store the user in sqlite
                            String uid = jObj.getString("uid");
                            //obtenemos los datos del usuario de la base de datos en el servidor menos la contraseña encriptada
                            JSONObject user = jObj.getJSONObject("user");
                            String name = user.getString("name");
                            String email = user.getString("email");
                            String created_at = user.getString("created_at");

                            // Insertamos al usuario en SQLlite, aunque la contraseña no esta encriptada ya que la tomamos directamente del Edittext
                            db.addUser(name, email, password,uid, created_at);

                            Toast.makeText(getApplicationContext(), "Alta correcta. Ahora LOGIN!", Toast.LENGTH_LONG).show();
                            // Launch login activity
                            Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                            intent.putExtra("email",email);
                            startActivity(intent);
                            finish();
                        } else {
                            // Error occurred in registration. Get the error
                            // message
                            String errorMsg = jObj.getString("error_msg");
                            Toast.makeText(getApplicationContext(),
                                    errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error en el registro: " + error.getMessage());
                    Toast.makeText(getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_LONG).show();
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
                    params.put("password",password);
                    params.put("name",name);
                    return params;
                }
            };
            // Adding request to request queue
            MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
            if (!pDialog.isShowing())
                    pDialog.show();
    }

    private void hideDialog() {
            if (pDialog.isShowing())
                pDialog.dismiss();
    }

}

