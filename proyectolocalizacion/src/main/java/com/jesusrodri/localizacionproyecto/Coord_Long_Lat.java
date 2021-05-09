package com.jesusrodri.localizacionproyecto;

public class Coord_Long_Lat {
	
	private double longitud, latitud, altitud;
	private String tipo_radar, info_radar, pais_radar;
	
	public  Coord_Long_Lat(double longitud, double lat, double altitud){
		this.longitud = longitud;
		this.latitud = lat;		
		this.altitud = altitud;
		
	}

	public Coord_Long_Lat(double longitud, double lat, double altitud, String tipo_radar, String pais_radar,  String info_radar){
		this.longitud = longitud;
		this.latitud = lat;
		this.altitud = altitud;
		this.tipo_radar = tipo_radar;
		this.pais_radar = pais_radar;
		this.info_radar = info_radar;

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

	public String getTipo_radar(){return  tipo_radar;}

	public String getPais_radar(){return pais_radar;}

	public String getInfo_radar(){return info_radar;}


}
