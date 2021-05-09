package com.jesusrodri.localizacionproyecto.login_files;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.jesusrodri.localizacionproyecto.R;
//import com.jesusrodri.localizacionproyecto.R;
import com.jesusrodri.localizacionproyecto.interfaces.OnFragmentInteractionListener;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by chukk on 04/02/2016.
 */
public  class AlertDialogo extends DialogFragment {

    ArrayAdapter<String> adapter;
    ArrayList<String> myArray;
    String mistring;
    ArrayList<Integer> selectedRutas;
    ArrayList<String> selectedContactsString, selectedContatsToDelete;
    OnFragmentInteractionListener alertListener;


    //instancia para la muestra de rutas y borrado correspondiente
    public static AlertDialogo newInstance(String[] str, int item_id){
            AlertDialogo dialogFrag = new AlertDialogo();
            Bundle args = new Bundle();
            args.putStringArray("array", str);
            args.putInt("itemId", item_id);
            dialogFrag.setArguments(args);
            return dialogFrag;
    }

    //Instancia para agregar contacto
    public static AlertDialogo newInstance2(int item_id){
            AlertDialogo dialogFrag = new AlertDialogo();
            Bundle args = new Bundle();
            args.putInt("itemId", item_id);
            dialogFrag.setArguments(args);
            return dialogFrag;
    }

    //Instancia para buscar solicitudes
    public static AlertDialogo newInstance3 (int item_id, ArrayList<String> listaSolicitudes, ArrayList<String> listaTextos){
        AlertDialogo dialogFrag = new AlertDialogo();
        Bundle args = new Bundle();
        args.putInt("itemId", item_id);
        args.putStringArrayList("listaSolicitudes", listaSolicitudes);
        args.putStringArrayList("listaTextos", listaTextos);
        dialogFrag.setArguments(args);
        return dialogFrag;

    }
    //Instancia para borrar amigos:
    public static AlertDialogo newInstance4 (int item_id, ArrayList<String> listaContactos) {
        AlertDialogo dialogFrag = new AlertDialogo();
        Bundle args = new Bundle();
        args.putInt("itemId", item_id);
        args.putStringArrayList("listaContactos", listaContactos);
        dialogFrag.setArguments(args);
        return dialogFrag;
    }

    //Instancia para Cerrar Sesion :
    public static AlertDialogo newInstance5 (int item_id) {
        AlertDialogo dialogFrag = new AlertDialogo();
        Bundle args = new Bundle();
        args.putInt("itemId", item_id);
        dialogFrag.setArguments(args);
        return dialogFrag;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //Para recibir:

        //Para enviar:
        if (context instanceof OnFragmentInteractionListener) {
            alertListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int option = getArguments().getInt("itemId");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        switch (option){
            case R.id.add_contacto:

                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.alertdialog_inputcontact_view, null);
                final TextView contactEmail = (TextView)view.findViewById(R.id.contactEmail);
                final TextView mensajeToCont =(TextView)view.findViewById(R.id.messageToContact);

                builder.setView(view);
                // Add action buttons
                builder.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                       if(!contactEmail.equals("")){ //Si hay texto de correo, enviamos
                            alertListener.enviarSolicitudContacto(contactEmail.getText().toString(),
                                    mensajeToCont.equals("")?"vacio":mensajeToCont.getText().toString());
                       }else{
                           Toast.makeText(getActivity(), "Escriba la direccion de correo de su contacto", Toast.LENGTH_SHORT).show();
                       }
                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                return builder.create();

            case R.id.buscar_solicitudes:
                final ArrayList<String> arrayEmails = getArguments().getStringArrayList("listaSolicitudes");
                final ArrayList<String> arrayTextos = getArguments().getStringArrayList("listaTextos");
                String[] str1 = arrayEmails.toArray(new String[arrayEmails.size()]);
                //Metemos correo y textos separados por dos saltos de linea
                for(int i=0;i<arrayEmails.size(); ++i){
                    str1[i]= str1[i]+"\n\n"+arrayTextos.get(i);
                }

                selectedContactsString = new ArrayList<>(); //Aqui metemos la lista en string a aceptar o rechazar
                Log.i("selectedd ", String.valueOf(selectedContactsString.size()));

                builder.setTitle("Acepte o Rechace contactos");
                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       //Obtemenos la lista en String de los elementos aceptados
                       // Log.i("selected ", selectedContactsString.toString());
                        if(selectedContactsString.size()>0)
                            alertListener.aceptar_rechazar_SolicitudesPendientesFromDialog(selectedContactsString, 1);

                    }
                });
                builder.setNegativeButton("Rechazar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Obtemenos la lista en String de los elementos rechazados
                        if(selectedContactsString.size()>0)
                            alertListener.aceptar_rechazar_SolicitudesPendientesFromDialog(selectedContactsString, 2);

                    }
                });

                builder.setMultiChoiceItems(str1, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if(isChecked){
                            selectedContactsString.add(arrayEmails.get(which));
                            Log.i("selected 1", selectedContactsString.toString());
                        }else if (selectedContactsString.contains(arrayEmails.get(which))) {
                           // selectedContacts.remove(Integer.valueOf(which)); //En caso de haber pulsado algun checked y agregado, lo quitamos del array
                            selectedContactsString.remove(arrayEmails.get(which));
                            Log.i("selected 2", selectedContactsString.toString());
                        }
                    }
                });
                return builder.create();


            case R.id.delete_amigo:

                final ArrayList<String> arrayContactos = getArguments().getStringArrayList("listaContactos");
                Log.i("delete amigo ",arrayContactos.toString());
                String[] str2 = arrayContactos.toArray(new String[arrayContactos.size()]);
                selectedContatsToDelete = new ArrayList<>(); //Aqui metemos la lista en string a borrar

                builder.setTitle("Borre los contactos seleccionados");
                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Obtemenos la lista en String de los elementos a borrar
                        Log.i("selected ", selectedContatsToDelete.toString());
                        if(selectedContatsToDelete.size()>0)
                            alertListener.borrarContactosFromDialog(selectedContatsToDelete);
                    }
                });

                builder.setNegativeButton("Camcelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       //No hacemos nada
                    }
                });

                builder.setMultiChoiceItems(str2, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            // selectedContacts.add(which);
                            selectedContatsToDelete.add(arrayContactos.get(which));
                            Log.i("selected 1", selectedContatsToDelete.toString());
                        } else if (selectedContatsToDelete.contains(arrayContactos.get(which))) {
                            // selectedContacts.remove(Integer.valueOf(which)); //En caso de haber pulsado algun checked y agregado, lo quitamos del array
                            selectedContatsToDelete.remove(arrayContactos.get(which));
                            Log.i("selected 2", selectedContatsToDelete.toString());
                        }
                    }
                });
                return builder.create();


            case R.id.gestion_mapas:
                String[] str = getArguments().getStringArray("array");
               // Log.i("mistring2 ", String.valueOf(str.length));
                selectedRutas = new ArrayList<>(); //Aqui metemos las rutas seleccionadas para borrar en multichoiceItems
                builder.setTitle("Seleccione Rutas a Borrar");
                // builder.setMessage("Lista de contactos");
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(getActivity(), "Pulsado boton si ", Toast.LENGTH_SHORT).show();
                        if(selectedRutas.size()>0)
                        alertListener.borrarRutasFromDialog(selectedRutas);

                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(getActivity(), "Pulsado boton NO ", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setMultiChoiceItems(str, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            selectedRutas.add(which);   //Agregamos las rutas seleccionadas al array por posicion

                        } else if (selectedRutas.contains(which)) {
                            selectedRutas.remove(Integer.valueOf(which)); //En caso de haber pulsado algun checked y agregado, lo quitamos del array
                        }
                    }
                });
                return builder.create();

            case R.id.seleccionar_tiempo:

               //builder.setTitle("Escribir frecuencia en segundos ");
                LayoutInflater inflater2 = getActivity().getLayoutInflater();
                View view2 = inflater2.inflate(R.layout.alertdialog_selecciontiempo, null);
                final TextView text_frecuencia = (TextView)view2.findViewById(R.id.texview_frecuencia);
                final DiscreteSeekBar discreteSeekBar = (DiscreteSeekBar) view2.findViewById(R.id.andersSeekbar);

                //Seleccionamos de las preferencias compartidas el ultima valor de seekbar:
                SessionManager sessionManager = new SessionManager(getContext());
                float valorfrompref = sessionManager.get_fre_gps();
                int valorFromPref = (int)valorfrompref; //
                discreteSeekBar.setProgress(valorFromPref);
                //Usaremos segundos en vez de minutos
                text_frecuencia.setText(String.format(Locale.getDefault(),"%d seg",valorFromPref ));
                discreteSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
                    float valor;
                    @Override
                    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                        value = value+3; //empezamos el seekbar en 1
                        valor = Float.valueOf(value);
                        String valorformateado  = String.format(Locale.getDefault(),"%.0f",valor);
                        seekBar.setIndicatorFormatter(valorformateado);
                        text_frecuencia.setText(valorformateado+" seg");
                        /*en caso de usar valor de 0.5 en 0.5 con minutos en vez de segundos
                        valor =(float)value / (float)2; // en caso de ir de 0.5 en 0.5 y usar minutos en vez de segundos
                        String valorformateado  = String.format(Locale.getDefault(),"%.1f",valor);
                        if(valorformateado.endsWith("5")){
                            seekBar.setIndicatorFormatter(valorformateado);
                            text_frecuencia.setText(valorformateado+" min");
                        }else{
                            seekBar.setIndicatorFormatter(String.format(Locale.getDefault(),"%.0f",valor));
                            text_frecuencia.setText(String.format(Locale.getDefault(),"%.0f seg",valor));
                        }
                        */
                    }

                    @Override
                    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
                        SessionManager sessionManager = new SessionManager(getContext());
                        setValorFrec(sessionManager.get_fre_gps());
                    }

                    @Override
                    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                            setValorFrec(valor);
                    }
                });

                builder.setView(view2);
                // Add action buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(getValorFrec()!=0){
                            //Guardamos el valor en sharedpreferencias
                           // Toast.makeText(getContext(), String.valueOf(getValorFrec()), Toast.LENGTH_SHORT).show();
                            SessionManager sessionManager = new SessionManager(getContext());
                            sessionManager.set_frec_gps(getValorFrec());
                            alertListener.enviarFrecuenciaGps(getValorFrec());
                        }

                    }
                });
                return builder.create();

            case R.id.app_web:
                LayoutInflater inflater3 = getActivity().getLayoutInflater();
                View view3 = inflater3.inflate(R.layout.activity_infoweb, null);
                builder.setView(view3);
                builder.setTitle("Localiza Cloud en la web");
                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                return builder.create();


            case R.id.cerrar_sesion:
                builder.setTitle("Desea Cerrar la Sesión? ");
                // builder.setMessage("Lista de contactos");
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       // Cerramos la session
                        alertListener.cerrarSesionFromDialog();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                return builder.create();

        }
        return builder.create();
    }
    float valorFrec=0;
    public void setValorFrec (float valor){
        this.valorFrec=valor;
    }
    public float getValorFrec(){
        return this.valorFrec;
    }

}
