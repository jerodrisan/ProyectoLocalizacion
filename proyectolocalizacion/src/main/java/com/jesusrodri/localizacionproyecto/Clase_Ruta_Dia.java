package com.jesusrodri.localizacionproyecto;

/**
 * Created by chukk on 26/01/2016.
 */
public class Clase_Ruta_Dia {

    private String ruta;
    private String dia;

    public Clase_Ruta_Dia(String ruta, String dia){
        this.ruta=ruta;
        this.dia=dia;
    }

    public String getRuta() {
        return this.ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public String getDia() {
        return this.dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }
}
