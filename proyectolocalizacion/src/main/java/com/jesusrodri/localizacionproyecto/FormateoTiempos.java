package com.jesusrodri.localizacionproyecto;

import android.widget.TextView;

/**
 * Created by chukk on 14/07/2016.
 */
public class FormateoTiempos {

    public FormateoTiempos(){}

    public static String formateoTiempos(int tiempo){
        //Tiempos
        String tiempoFormat="";
        if(tiempo<60){
            if(tiempo<10){
                //labelTiempo.setText("0"+String.valueOf(tiempo)+"''");
                tiempoFormat+="0"+String.valueOf(tiempo)+"''";
            }else{
               // labelTiempo.setText(String.valueOf(tiempo)+"''");
                tiempoFormat+=String.valueOf(tiempo)+"''";
            }
        }else{
            if(tiempo>=60 && tiempo<600){
                if((60*(tiempo/60)) <= tiempo && tiempo  < ((60*(tiempo/60))+10)){
                    //labelTiempo.setText("0"+String.valueOf(tiempo / 60)+"' "+"0"+String.valueOf(tiempo%60)+"''");
                    tiempoFormat+="0"+String.valueOf(tiempo / 60)+"' "+"0"+String.valueOf(tiempo%60)+"''";
                }else{
                   // labelTiempo.setText("0"+String.valueOf(tiempo / 60)+"' "+String.valueOf(tiempo%60)+"''");
                    tiempoFormat+="0"+String.valueOf(tiempo / 60)+"' "+String.valueOf(tiempo%60)+"''";
                }
            }else{
                if(tiempo>=600 && tiempo<3600) {
                    if((60*(tiempo/60)) <= tiempo && tiempo  < ((60*(tiempo/60))+10)){
                       // labelTiempo.setText(String.valueOf(tiempo / 60)+"' "+"0"+String.valueOf(tiempo%60)+"''");
                        tiempoFormat+=String.valueOf(tiempo / 60)+"' "+"0"+String.valueOf(tiempo%60)+"''";
                    }else{
                       // labelTiempo.setText(String.valueOf(tiempo / 60)+"' "+String.valueOf(tiempo%60)+"''");
                        tiempoFormat+=String.valueOf(tiempo / 60)+"' "+String.valueOf(tiempo%60)+"''";
                    }
                }else if(tiempo>= 3600){
                    int hor = tiempo/3600;
                    //tiempo entre la hora y 60 segundos:
                    if ((3600*hor) <=tiempo && tiempo < (3600*hor)+60){
                        //ponemos el cero en el segundo y en el minuto
                        if(tiempo < (3600*hor)+10){
                           // labelTiempo.setText(String.valueOf(hor)+"h "+ "00' "+ "0"+String.valueOf(tiempo%3600)+"''");
                            tiempoFormat+=String.valueOf(hor)+"h "+ "00' "+ "0"+String.valueOf(tiempo%3600)+"''";
                        }else{
                            //entre 10 y 60: quitamos el cero del segundo
                           // labelTiempo.setText(String.valueOf(hor)+"h "+ "00' "+String.valueOf(tiempo%3600)+"''");
                            tiempoFormat+=String.valueOf(hor)+"h "+ "00' "+String.valueOf(tiempo%3600)+"''";
                        }
                    }else{
                        //tiempo entre la hora 1 minuto y 10 minutos ponemos el cero
                        if(((3600*hor)+60)<=tiempo && tiempo <((3600*hor)+600)){
                            //ponemos cero entre los primeros 10 segundos
                            if(((3600*hor)+(60*(tiempo%3600)/60))<=tiempo && tiempo <((3600*hor)+(((60*((tiempo%3600)/60)))+10))){
                              //  labelTiempo.setText(String.valueOf(hor)+"h "+ "0"+String.valueOf((tiempo%3600)/60)+"' "+ "0"+String.valueOf((tiempo%3600)%60)+"''");
                                tiempoFormat+=String.valueOf(hor)+"h "+ "0"+String.valueOf((tiempo%3600)/60)+"' "+ "0"+String.valueOf((tiempo%3600)%60)+"''";
                                //System.out.println("A tiempo menor 10"+String.valueOf(tiempo));
                            }else{
                                //quitamos el cero en los segundos
                               // labelTiempo.setText(String.valueOf(hor)+"h "+ "0"+String.valueOf((tiempo%3600)/60)+"' "+String.valueOf((tiempo%3600)%60)+"''");
                                tiempoFormat+=String.valueOf(hor)+"h "+ "0"+String.valueOf((tiempo%3600)/60)+"' "+String.valueOf((tiempo%3600)%60)+"''";
                                //System.out.println("A tiempo mayor o igual 10"+String.valueOf(tiempo));
                            }
                        }else{
                            //tiempo entre la hora y pasados los 10 minutos
                            //pasados los 10 minutos, quitamos los ceros del minuto pero dejamos el cero en los segundos
                            if (((3600 * hor) + (60 * (tiempo % 3600) / 60)) <= tiempo && tiempo < ((3600 * hor) + (((60 * ((tiempo % 3600) / 60))) + 10))) {
                               // labelTiempo.setText(String.valueOf(hor) + "h " + String.valueOf((tiempo % 3600) / 60) + "' " + "0" + String.valueOf((tiempo % 3600) % 60) + "''");
                                tiempoFormat+=String.valueOf(hor) + "h " + String.valueOf((tiempo % 3600) / 60) + "' " + "0" + String.valueOf((tiempo % 3600) % 60) + "''";
                                //System.out.println("B tiempo menor 10" + String.valueOf(tiempo));
                            } else {
                              //  labelTiempo.setText(String.valueOf(hor) + "h " + String.valueOf((tiempo % 3600) / 60) + "' " + String.valueOf((tiempo % 3600) % 60) + "''");
                                tiempoFormat+=String.valueOf(hor) + "h " + String.valueOf((tiempo % 3600) / 60) + "' " + String.valueOf((tiempo % 3600) % 60) + "''";
                                //System.out.println("B tiempo mayor o igual 10" + String.valueOf(tiempo));
                            }
                        }
                    }
                }
            }
        }
        return tiempoFormat;
    }
}

