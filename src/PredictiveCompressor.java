import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.util.ArrayList;

public class PredictiveCompressor {
    
    public double maxTemporalError;
    
    public double maxSpatialError;

    public Predictor predictor;

    public PredictiveCompressor(Predictor predictor, double maxTemporalError, double maxSpatialError) {
        this.predictor        = predictor;
        this.maxTemporalError = maxTemporalError;
        this.maxSpatialError  = maxSpatialError;
    }

    public PredictiveCompressor(double maxTemporalError, double maxSpatialError) { 
        this(new LinearPredictor(), maxTemporalError, maxSpatialError);
    }

    public PredictiveCompressor() {
        this.predictor = new LinearPredictor();
    }

    public int calculateDiscardedBits(double maxValue, double errorBound) {
        long   bits = Double.doubleToLongBits(maxValue);
        long   exponent = (bits & 0x7ff0000000000000L) >> 52;
        double tem = errorBound * Math.pow(2, 1075 - exponent) + 1;
        return Math.min((int)(Math.log(tem) / Math.log(2.0)), 52);
    }

    public void compress(Obstream obs, ArrayList<GPSPoint> points) {
        double maxTime  = 0.0;
        double maxCoord = 0.0;
        
        for (GPSPoint point : points) {
            maxTime  = Math.max(maxTime, point.time);
            maxCoord = Math.max(maxCoord, point.lon);
            maxCoord = Math.max(maxCoord, point.lat);
        }

        int[] discard = new int[3];
        discard[0] = calculateDiscardedBits(maxTime, this.maxTemporalError);
        discard[1] = calculateDiscardedBits(maxCoord, this.maxSpatialError);
        discard[2] = discard[1];
        

        obs.writeInt(discard[0], 8);
        obs.writeInt(discard[1], 8);

        obs.writeInt(points.size(), 32);
        obs.writeDouble(points.get(0).time);
        obs.writeDouble(points.get(0).lon);
        obs.writeDouble(points.get(0).lat);
        System.out.println(discard[0] + "\t" + discard[1] + "\t" + discard[2] + "\t" + points.size());

        double[][] tuples = new double[points.size()][3];
        for (int i = 0; i < points.size(); i++) {
            tuples[i][0] = points.get(i).time;
            tuples[i][1] = points.get(i).lon;
            tuples[i][2] = points.get(i).lat;
        }

        long[][] residuals = new long[3][points.size() - 1];
        for (int i = 0; i < points.size() - 1; i++) {
            if (discard[0] > 0) {
                long predTime = Double.doubleToLongBits(predictor.predictTime(tuples, i + 1));
                long residual = Double.doubleToLongBits(tuples[i + 1][0]) ^ predTime;
                residual = (residual >>> discard[0]) << discard[0];
                tuples[i + 1][0] = Double.longBitsToDouble(predTime ^ residual);
            }
            
            double[] pred = new double[3];
            predictor.predictCoords(tuples, i + 1, pred);

            for (int j = 0 ; j < 3; j++) {
                long residual = Double.doubleToLongBits(tuples[i + 1][j]) ^ Double.doubleToLongBits(pred[j]);
                residual >>>= discard[j];
                residuals[j][i] = residual;
                residual <<= discard[j];
                tuples[i + 1][j] = Double.longBitsToDouble(Double.doubleToLongBits(pred[j]) ^ residual);
            }
        }


        Encoder[] encoders = new Encoder[3];

        for (int j = 0; j < 3; j++) {
            encoders[j] = new DynamicEncoder(obs, residuals[j], points.size() - 1);
        }

        for (int i = 0; i < points.size() - 1; i++) {
            for (int j = 0; j < 3; j++) {
                encoders[j].encode(obs, residuals[j][i]);
            }
        }
    }

    public ArrayList<GPSPoint> decompress(Ibstream ibs) {
        int[] discard = new int[3];
        discard[0] = ibs.readByte();
        discard[1] = ibs.readByte();
        discard[2] = discard[1];
        
        int nPoints = (int) ibs.readInt(32);
        long[][] tuples = new long[nPoints][3];
        for (int i = 0; i < 3; i++) {
            tuples[0][i] = ibs.readInt(64);
        }

        Encoder[] decoders = new Encoder[3];
        for (int i = 0; i < 3; i++) {
            decoders[i] = new DynamicEncoder(ibs);
        }
        for (int i = 1; i < nPoints; i++) {
            long[] residuals = new long[3];
            for (int j = 0; j < 3; j++) {
                long temNum = decoders[j].decode(ibs);
                residuals[j] = temNum << discard[j];
            }
            long time = Double.doubleToLongBits(this.predictor.predictTime(tuples, i)) ^ residuals[0];
            tuples[i][0] = time;

            double[] pred = new double[3];
            predictor.predictCoords(tuples, i, pred);

            for (int j = 0; j < 3; j++) {
                tuples[i][j] = Double.doubleToLongBits(pred[j]) ^ residuals[j];
            }
        }


        ArrayList<GPSPoint> points = new ArrayList<GPSPoint>();
        for (int i = 0; i < nPoints; i++) {
            // System.out.println(Double.longBitsToDouble(tuples[i][0]) + "\t" + Double.longBitsToDouble(tuples[i][1]) + "\t" + Double.longBitsToDouble(tuples[i][2]));
            GPSPoint point = new GPSPoint(Double.longBitsToDouble(tuples[i][0]),
                                            Double.longBitsToDouble(tuples[i][1]),
                                            Double.longBitsToDouble(tuples[i][2]));
            points.add(point);
        }

        return points;
    }

}
