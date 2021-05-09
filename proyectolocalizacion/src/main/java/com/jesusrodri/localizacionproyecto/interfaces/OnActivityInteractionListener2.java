package com.jesusrodri.localizacionproyecto.interfaces;

import java.util.ArrayList;

/**
 * Created by chukk on 07/03/2016.
 */
public interface OnActivityInteractionListener2 {
    public void activarLayoutContacto(boolean var, String correo, String name, ArrayList<Integer> tiempos, ArrayList<Double> distancias);
    public void passingDistVelocTiempoRutas(ArrayList<Integer> tiempos, ArrayList<Double> distancias, int timepaused);
    public void passingIdEmailNametoFragment(String id, String email,String name);
    public void passingOnFinishApp(boolean var);
    public void passingRutayFechatoMap(String ruta, String dia, String hora);
    public void setFrecuenceTime(int frecTime, String tipoRuta);

}
