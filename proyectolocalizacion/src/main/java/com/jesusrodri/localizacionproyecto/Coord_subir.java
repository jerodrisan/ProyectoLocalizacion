package com.jesusrodri.localizacionproyecto;

/**
 * Created by chukk on 05/06/2017.
 */

public class Coord_subir {
    private double longitud, latitud, altitud;
    private String id, date;
    private int sesion_num;

    public  Coord_subir(double longi, double lati,double alti,
                        String id, int sesion_num, String date){
        this.longitud = longi;
        this.latitud = lati;
        this.altitud = alti;
        this.id=id;
        this.date = date;
        this.sesion_num=sesion_num;



    }

    public double getLongitud (){
        return longitud;
    }

    public double getLatitud (){
        return latitud;
    }

    public double getAltitud (){
        return altitud;
    }

    public String getid(){return id;}

    public String getDate(){return date;};

    public int getSesionNum (){return sesion_num;}
}
