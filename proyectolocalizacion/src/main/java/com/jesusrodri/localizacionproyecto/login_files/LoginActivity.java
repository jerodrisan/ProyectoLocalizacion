package com.jesusrodri.localizacionproyecto.login_files;

//import android.app.DialogFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;


import android.app.ProgressDialog;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnLogin;
    private Button btnLinkToRegister, btnContactoAyuda;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private HashMap<String,String> usuari;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);

        inputEmail.setText(getIntent().getStringExtra("email"));
        inputPassword.setText(getIntent().getStringExtra("pass"));
       //Log.i("contraseña ",getIntent().getStringExtra("pass"));
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        btnContactoAyuda = (Button)findViewById(R.id.btn_ayudaycontacto);


        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        db = new SQLiteHandler(getApplicationContext()); // SQLite database handler
        session = new SessionManager(getApplicationContext());  // Session manager
        usuari = new HashMap<String,String>();

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // Si esta registrado vemos de que  usuario se trata y le mandamos a la pantalla principal
            String email = session.getLastUser();
            //Log.i("isloged",email);
            passToMainActivity(email);
        }
        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String  tipo_login = "islogged"; //tipo de loggin que establecemos en la base de datos para movil. Para web seria "isloggedweb
                // Check for empty data in the form
                if (!email.isEmpty() && !password.isEmpty()) {
                    // login user
                    //Log.i("error1 ",email+" "+password);
                    checkLogin(email, password, tipo_login);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Por favor, introduzca los datos!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                i.putExtra("registrar_nuevousu",true);
                startActivity(i);
                finish();
            }
        });

        //Ayuda y contacto
        btnContactoAyuda.setOnClickListener(new View.OnClickListener() {
            DialogFragment dialogo;
            @Override
            public void onClick(View v) {
                dialogo = DialogFragnentContacto.newInstance();
                dialogo.show(getSupportFragmentManager(), "dialog");
            }
        });
    }

    /**
         * function to verify login details in mysql db
         * */

    private void checkLogin(final String email, final String password, final String tipo_login) {
        // Tag used to cancel the request
        final String param_server="login";
        String tag_string_req = "req_login";
        pDialog.setMessage("Logging in ...");
        showDialog();
        //String str1 = "?param=" + param_server + "&email=" + email + "&password=" + password;
        //Usamos libreria Volley para conectar con el servidor
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PARAM_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        // Now store the user in SQLite
                        JSONObject user = jObj.getJSONObject("user");
                        String uid = jObj.getString("uid");
                        String name = user.getString("name");
                        String email = user.getString("email");
                        String created_at = user.getString("created_at");
                        //Log.i("contraseña1 ",password);
                        //Comprobamos si el usuario no haya sido insertado previamente en la base de datos interna
                        usuari= db.getUserDetail(email);
                        String usuarioEmail = usuari.get("email");
                        if(usuarioEmail==null){
                            //El usuario no se encuentra en la base de datos interna, con lo cual lo insertamos y pasamos a la pantalla sig
                            //Log.i("contraseña2 ",password);
                            db.addUser(name, email, password,uid, created_at);
                            // Log.i("variables     ", name+" "+email+" "+uid+" "+created_at);
                            //Log.i("email", email);
                            session.setLogin(true,email, password, false);
                            passToMainActivity(email);

                        }else if(usuarioEmail.equals(email)){
                            //Si el usuario ya estaba insertado entonces coinciden los dos emails. No lo insertamos y pasamos a la pantalla sig
                            // Log.i("usuarioEmail", usuarioEmail);
                            // Create login session
                            // Log.i("contraseña3 ",password);
                            session.setLogin(true,usuarioEmail, password,false);
                            // Log.i("usuario ya esta", "usu ya esta");//Pasamos directamente a la pantalla sin agregarlo a la base de datos
                            passToMainActivity(usuarioEmail);
                        }else{
                            Log.i("error interno","error en login");
                        }

                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
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
                params.put("tipo_login", tipo_login);
                return params;
            }
        };
        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }




    private void passToMainActivity(String email){
       // Log.i("pasando a main","pasando");
        Intent intent = new Intent(LoginActivity.this, Main2Activity.class);
        Bundle bundle = new Bundle();
        //bundle.putSerializable("usuariHashMap",usuari); //Ver http://stackoverflow.com/questions/11452859/android-hashmap-in-bundle
        usuari= db.getUserDetail(email);
        bundle.putString("uid", usuari.get("uid"));
        bundle.putString("name", usuari.get("name"));
        bundle.putString("email",email);
        bundle.putString("pass", usuari.get("pass"));
       // Log.i("contraseña passtoMain ",usuari.get("pass"));
       // Log.i("contraseña fromsesion ",session.getKeyLastpassword());
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }


    public static class DialogFragnentContacto extends DialogFragment{

        public static DialogFragnentContacto newInstance(){
            DialogFragnentContacto dialogFrag = new DialogFragnentContacto();
            return dialogFrag;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            LayoutInflater inflater3 = getActivity().getLayoutInflater();
            View view3 = inflater3.inflate(R.layout.activity_ayudacontacto, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(view3);
            builder.setTitle("Ayuda - Contacto")
                    .setPositiveButton("aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // FIRE ZE MISSILES!
                        }
                    });

            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}
