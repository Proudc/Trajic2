import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.File;
import java.util.ArrayList;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class Trajic {

    public static void main(String[] args) {
        String mode   = args[0];
        String infile = args[1];
        double mte = Double.valueOf(args[2]);
        double mse = Double.valueOf(args[3]);
        if (mode.equals("c")) {
            compress(infile, mte, mse);
        } else {
            // decompress(infile);
            decompressByteArray(readByte(infile));
        }
    }
    
    public static void compress(final String fileName, double mte, double mse) {
        PredictiveCompressor c = new PredictiveCompressor(mte, mse);
        
        System.out.println("Reading file...");
        ArrayList<GPSPoint> points = readPoints(fileName);
        
        System.out.println("Compressing...");
        String writeFileName = PublicFunc.fileBaseName(fileName) + ".tjc";
        
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(writeFileName));
            Obstream obs = new Obstream(out);
            c.compress(obs, points);
            System.out.println(obs.totalBits);
            System.out.println(obs.totalChars);
            obs.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Done.");
    }

    public static void decompress(final String fileName) {
        PredictiveCompressor c = new PredictiveCompressor();

        System.out.println("Decompressing...");
        String writeFileName = PublicFunc.fileBaseName(fileName) + ".txt";
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(fileName));
            Ibstream ibs = new Ibstream(in);
            ArrayList<GPSPoint> points = c.decompress(ibs);
            in.close();
            System.out.println("Writing file...");
            writePoints(writeFileName, points);
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("Done.");
    }

    public static ArrayList<GPSPoint> readPoints(String fileName) {
        GPSReader reader;
        
        if (PublicFunc.fileExt(fileName).equals("csv")) {
            reader = new CSVReader(fileName);
        } else {
            reader = new TXTReader(fileName);
        }
        
        return reader.readPoints();
    }

    public static void writePoints(String fileName, ArrayList<GPSPoint> points) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName)));
            for (int i = 0; i < points.size(); i++) {
                GPSPoint point = points.get(i);
                writer.write(point.time + "\t" + point.lon + "\t" + point.lat + "\n");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void decompressByteArray(byte[] inputData) {
        PredictiveCompressor c = new PredictiveCompressor();
        
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(inputData));
            Ibstream ibs = new Ibstream(in);
            ArrayList<GPSPoint> points = c.decompress(ibs);
            in.close();
            for (int i = 0; i < points.size(); i++) {
                GPSPoint point = points.get(i);
                System.out.println(point.time + "\t" + point.lon + "\t" + point.lat);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] readByte(final String path) {
        ArrayList<Byte> data = new ArrayList<>();
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(path)));
            byte[] tem = new byte[1];
            while (in.read(tem) != -1) {
                data.add(tem[0]);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] returnData = new byte[data.size()];
        for (int i = 0; i < data.size(); i++) {
            returnData[i] = data.get(i);
        }
        return returnData;
    }

}
