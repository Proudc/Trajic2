public class GPSPoint {

    public double time;
    public double lon;
    public double lat;

    public static final int EARTH_RADIUS = 6371;
    
    public GPSPoint(double time, double lon, double lat) {
        this.time = time;
        this.lon  = lon;
        this.lat  = lat;
    }

    public GPSPoint() {
        this(0.0, 0.0, 0.0);
    }

    public GPSPoint(double[] triple) {
        this(triple[0], triple[1], triple[2]);
    }

    public double distance(GPSPoint other) {
        return Math.sqrt(Math.pow(other.lon - this.lon, 2) +
                         Math.pow(other.lat - this.lat, 2));
    }

    public double distanceKms(GPSPoint other) {
        double lon1 = this.lon  * Math.PI / 180;
        double lon2 = other.lon * Math.PI / 180;
        double lat1 = this.lat  * Math.PI / 180;
        double lat2 = other.lat * Math.PI / 180;

        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) +
                    Math.sin(dlon / 2) * Math.sin(dlon / 2) *
                    Math.cos(lat1) * Math.cos(lat2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

}