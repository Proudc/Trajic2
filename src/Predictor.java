public abstract class Predictor {
    
    public abstract double predictTime(double[][] tuples, int index);

    public abstract void predictCoords(double[][] tuples, int index, double[] result);

    public abstract double predictTime(long[][] tuples, int index);

    public abstract void predictCoords(long[][] tuples, int index, double[] result);

}
