public class LinearPredictor extends Predictor {
    
    public double predictTime(double[][] tuples, int index) {
        double cur = tuples[index - 1][0];
        double prev = index > 1 ? tuples[index - 2][0] : cur;
        return (cur * 2 - prev);
    }

    public void predictCoords(double[][] tuples, int index, double[] result) {
        GPSPoint cur = new GPSPoint(tuples[index - 1]);
        GPSPoint prev = index > 1 ? new GPSPoint(tuples[index - 2]) : cur;

        double prevInterval = cur.time - prev.time;
        double interval = tuples[index][0] - cur.time;
        double ratio = prevInterval > 0 ? interval / prevInterval : 1;

        double time = predictTime(tuples, index);
        double lon = cur.lon + (cur.lon - prev.lon) * ratio;
        double lat = cur.lat + (cur.lat - prev.lat) * ratio;

        result[0] = time;
        result[1] = lon;
        result[2] = lat;
    }

    public double predictTime(long[][] tuples, int index) {
        double cur = Double.longBitsToDouble(tuples[index - 1][0]);
        double prev = index > 1 ? Double.longBitsToDouble(tuples[index - 2][0]) : cur;
        return (cur * 2 - prev);
    }

    public void predictCoords(long[][] tuples, int index, double[] result) {
        GPSPoint cur = new GPSPoint(Double.longBitsToDouble(tuples[index - 1][0]),
                                    Double.longBitsToDouble(tuples[index - 1][1]),
                                    Double.longBitsToDouble(tuples[index - 1][2]));
        GPSPoint prev = index > 1 ? new GPSPoint(Double.longBitsToDouble(tuples[index - 2][0]),
                                    Double.longBitsToDouble(tuples[index - 2][1]),
                                    Double.longBitsToDouble(tuples[index - 2][2])) : cur;

        double prevInterval = cur.time - prev.time;
        double interval = Double.longBitsToDouble(tuples[index][0]) - cur.time;
        double ratio = prevInterval > 0 ? interval / prevInterval : 1;

        double time = predictTime(tuples, index);
        double lon = cur.lon + (cur.lon - prev.lon) * ratio;
        double lat = cur.lat + (cur.lat - prev.lat) * ratio;

        result[0] = time;
        result[1] = lon;
        result[2] = lat;
    }

}
