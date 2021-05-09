package com.jesusrodri.localizacionproyecto.interfaces;

import com.jesusrodri.localizacionproyecto.Coord_Long_Lat;

import java.util.ArrayList;

/**
 * Created by chukk on 20/01/2016.
 */
public interface OnActivityInteractionListener {
    public void PassingDataToMapa(double lng, double lat, double alt, double vel, double dist);
    public void PassingRutaToMapa(ArrayList<Coord_Long_Lat> arrayList,ArrayList<Integer> tiempos, ArrayList<Double> distancias, int timepaused, float zoomLevel );
    public void PassingRutaAmigoToMapa(ArrayList<Coord_Long_Lat> arrayList,ArrayList<Integer> tiempos, ArrayList<Double> distancias);
    public void PassingBorrarRutasEnMapa(boolean state);
    public void PassingBorrarRutaAmigo (boolean state);
    public void PassingBorrarSecuenciaEnMapa(boolean state);
    public void PassingZoomLevelToMap(float zoom);
    //Pasamos la informacion de que estan activados tanta la localizacion del usuario como la del amigo:
    public void PassingStateUsuarioYAmigo(boolean state, boolean state2);
    public void passingIdEmailNametoMap(String id, String email,String name);
    public void passingNameAmigotoMap(String name);
    public void limpiarArrays();
    public void Passing_Gestion_Radares(String respuest);
    public void Passing_BotonStart_Estado(boolean state);


}
