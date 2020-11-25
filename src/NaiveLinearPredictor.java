public class NaiveLinearPredictor{

    public double predictTime(double[][] tuples, int index) {
        double cur = tuples[index - 1][0];
        double prev = index > 1 ? tuples[index - 2][0] : cur;
        return (cur * 2 - prev);
    }

    public void predictCoords(double[][] tuples, int index, double[] result) {
        GPSPoint cur = new GPSPoint(tuples[index - 1]);
        GPSPoint prev = index > 1 ? new GPSPoint(tuples[index - 2]) : cur;

        double time = predictTime(tuples, index);
        double lon = cur.lon * 2 - prev.lon;
        double lat = cur.lat * 2 - prev.lat;

        result[0] = time;
        result[1] = lon;
        result[2] = lat;
    }

}
