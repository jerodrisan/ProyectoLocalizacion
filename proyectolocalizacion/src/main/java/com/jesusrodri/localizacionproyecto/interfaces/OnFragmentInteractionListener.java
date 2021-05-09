package com.jesusrodri.localizacionproyecto.interfaces;

import java.util.ArrayList;

/**
 * Created by chukk on 20/01/2016.
 */
public interface OnFragmentInteractionListener {
    public void onPassingDataToMapa(double lng, double lat, double alt, double vel, double dist);
    public void onRequiringClienteFromToMain ();
    public void onPassingSpinnerStateToMain (boolean enableSpinner, boolean lastElement);
    public void onPassingActivarBotonn(boolean state);
    public void isServiceStopped(boolean state);
    public void OnPassingBorrarRuta(ArrayList<String> ses_num);
    //Funciones para pasar acciones desde AlertDialogFragment:
    public void borrarRutasFromDialog(ArrayList<Integer> lista); //Borrar rutas seleccionadas con multichoice
    public void enviarSolicitudContacto(String contact, String comentario);
    public void retornarArraySolicitudesPendientes(ArrayList<String> arrayEmails, ArrayList<String> arrayTextos);

    public void aceptar_rechazar_SolicitudesPendientesFromDialog(ArrayList<String> listaAceptRechaz, int acep_rechaz);
    public void actualizarSpinnerContactos();

    public void retornarListaContactos(ArrayList<String> arrayList);
    public void borrarContactosFromDialog(ArrayList<String> listaContactos);

    public void cerrarSesionFromDialog();

    public void onPassingOcultarAmigo(boolean var);
    public void onPassingResetSpinnerContactos();

    public void onBorrarDatosAlFinalizar(String id, String num_sesion, boolean cerrarApp);
    public void onCerrarApp(boolean var);

    public void enviarSolicitudContactoFirebase(String solicitante, String solicitado, String texto, boolean error, String mensaje);

    public void enviarFrecuenciaGps (float frec_gps);



}
