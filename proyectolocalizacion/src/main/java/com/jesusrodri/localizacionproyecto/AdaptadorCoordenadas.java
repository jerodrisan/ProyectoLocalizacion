package com.jesusrodri.localizacionproyecto;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

//import com.example.android_localizacionproyecto.R;
//import com.jesusrodri.localizacionproyecto.R;

public class AdaptadorCoordenadas extends ArrayAdapter<Coord_Long_Lat>{
	
	Activity contexto;
	int res;
	//Coord_Long_Lat[] datos;
	ArrayList<Coord_Long_Lat> datos;
	
	public AdaptadorCoordenadas(Activity context, int resource,	ArrayList<Coord_Long_Lat> objects) {
		super(context, resource, objects);
		this.res = resource;
		this.contexto = context;
		this.datos = objects;
	}
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = contexto.getLayoutInflater();
		View item = inflater.inflate(res, null);
	
		TextView textLati = (TextView)item.findViewById(R.id.txtLatitud);
		textLati.setText("latitud : "+String.valueOf(datos.get( position).getLatitud() ));
		TextView textLongi = (TextView)item.findViewById(R.id.txtLongitud);
		textLongi.setText("longitud : "+String.valueOf(datos.get( position).getLongitud() ));
		TextView textAlti = (TextView)item.findViewById(R.id.txtAltitud);
		textAlti.setText("Altitud : "+String.valueOf(datos.get( position).getAltitud() ) );				
		
		return (item);
	}
}
