public abstract class Encoder {
    
    public abstract void encode(Obstream obs, long num);

    public abstract long decode(Ibstream ibs);

}
