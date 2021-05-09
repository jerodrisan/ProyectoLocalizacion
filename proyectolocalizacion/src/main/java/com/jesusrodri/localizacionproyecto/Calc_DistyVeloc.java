package com.jesusrodri.localizacionproyecto;

public class Calc_DistyVeloc {
	
	public Calc_DistyVeloc(){}
	
	public static double distFrom(double lat1, double lng1, double lat2, double lng2) {  
        //double earthRadius = 3958.75;//miles  
        double earthRadius = 6371;//kilometers  
        double dLat = Math.toRadians(lat2 - lat1);  
        double dLng = Math.toRadians(lng2 - lng1);  
        double sindLat = Math.sin(dLat / 2);  
        double sindLng = Math.sin(dLng / 2);  
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)  
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));  
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));  
        double dist = earthRadius * c;  
        //Retomamos la distancia en Kilometros        
        return dist;  
    }

        //---calculo de distancia entre dos puntos: Metodo Havesine
        //--http://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi
    /*
 * Calculate distance between two points in latitude and longitude taking
 * into account height difference. If you are not interested in height
 * difference pass 0.0. Uses Haversine method as its base.
 *
 * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
 * el2 End altitude in meters
 * @returns Distance in Meters
 */
    private  double distance(double lat1, double lat2, double lon1,  double lon2, double el1, double el2) {

            final int R = 6371; // Radius of the earth

            Double latDistance = Math.toRadians(lat2 - lat1);
            Double lonDistance = Math.toRadians(lon2 - lon1);
            Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = R * c * 1000; // convert to meters

            double height = el1 - el2;

            distance = Math.pow(distance, 2) + Math.pow(height, 2);

            return Math.sqrt(distance);
    }



     //Metodo 3 (El que hemos usado el el servidor PHP:

        public static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
                double theta = lon1 - lon2;
                double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
                dist = Math.acos(dist);
                dist = rad2deg(dist);
                dist = dist * 60 * 1.1515;
                if (unit == "K") {
                        dist = dist * 1.609344;
                } else if (unit == "N") {
                        dist = dist * 0.8684;
                }

                return (dist);
        }

        /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts decimal degrees to radians						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
        public static double deg2rad(double deg) {
                return (deg * Math.PI / 180.0);
        }

        /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts radians to decimal degrees						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
        public static double rad2deg(double rad) {
                return (rad * 180 / Math.PI);
        }


        public int  horaToSeg(int horas, int minutos, int segundos){
                return (horas*3600)+(minutos*60)+segundos;
        }





}
