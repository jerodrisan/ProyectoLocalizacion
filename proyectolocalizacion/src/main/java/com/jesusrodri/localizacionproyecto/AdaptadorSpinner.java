package com.jesusrodri.localizacionproyecto;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

//import com.example.android_localizacionproyecto.R;
//import com.jesusrodri.localizacionproyecto.R;

import java.util.ArrayList;

/**
 * Created by chukk on 26/01/2016.
 */
public class AdaptadorSpinner extends BaseAdapter {

    LayoutInflater inflater;
    ArrayList<Clase_Ruta_Dia> arrayList;

    public AdaptadorSpinner (Context contexto, ArrayList<Clase_Ruta_Dia> arrayList){
        this.inflater= LayoutInflater.from(contexto);
        this.arrayList=arrayList;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        ViewHolder viewHolder=null;



        if(view==null){
            view=inflater.inflate(R.layout.spinner_view, null);
            viewHolder = new ViewHolder();
            viewHolder.textDia = (TextView)view.findViewById(R.id.textDia);
            viewHolder.textRuta = (TextView)view.findViewById(R.id.textRuta);
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)view.getTag();
        }

        Clase_Ruta_Dia clase = (Clase_Ruta_Dia) getItem(position);
        viewHolder.textDia.setText(clase.getDia());
        viewHolder.textRuta.setText(clase.getRuta());
        //viewHolder.textDia.setText(arrayList.get(position).getDia());
        //viewHolder.textRuta.setText(arrayList.get(position).getRuta());

        return view;
    }

    class ViewHolder{
        TextView textRuta;
        TextView textDia;
    }
}
