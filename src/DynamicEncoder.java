import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.util.ArrayList;

public class DynamicEncoder extends Encoder{
    
    public Codebook codebook;

    public DynamicEncoder(Obstream obs, long[] nums, int len) {
        double[] freqs = new double[65];
        for (int i = 0; i < 65; i++) {
            freqs[i] = 0.0;
        }
        double step = 1.0 / len;
        int nFreqs = 0;

        for (int i = 0; i < len; i++) {
            String temStr = Long.toUnsignedString(nums[i]);
            double temNum = Double.valueOf(temStr);
            int minLen = 0;
            if (temNum > 0) {
                minLen = (int)(Math.log(temNum) / Math.log(2.0)) + 1;
            }
            freqs[minLen] += step;
            nFreqs = Math.max(nFreqs, minLen + 1);
            
        }
        
        int maxDivs = 0;
        for (int i = 0; i < nFreqs; i++) {
            if (freqs[i] > 0.02) {
                maxDivs += 1;
            }
        }
        if (maxDivs < 4) {
            maxDivs = 4;
        }
        if (maxDivs > 32) {
            maxDivs = 32;
        }

        int[] dividers = new int[maxDivs];
        double minCost = Double.MAX_VALUE;
        LengthFrequencyDivider lfd = new LengthFrequencyDivider(freqs, nFreqs, maxDivs);
        lfd.calculate();

        int nDivs = maxDivs;

        for (int nCodeWords = 2; nCodeWords <= maxDivs; nCodeWords++) {
            double cost = lfd.getCost(nCodeWords) + 7.0 * nCodeWords / len;
            if (cost < minCost) {
                minCost = cost;
                nDivs = nCodeWords;
                lfd.getDividers(dividers, nDivs);
            }
        }

        double[] clumpedFreqs = new double[nDivs];
        int b = 0;
        for (int i = 0; (i < nFreqs && b < nDivs); i++) {
            clumpedFreqs[b] += freqs[i];
            if (i == dividers[b]) {
                b++;
            }
        }

        for (int i = 0; i < nDivs; i++) {
            System.out.println(clumpedFreqs[i]);
        }

        ArrayList<Integer> divVec = new ArrayList<>();
        for (int i = 0; i < nDivs; i++) {
            divVec.add(dividers[i]);
        }

        ArrayList<String> codeWords = Huffman.createCodewords(clumpedFreqs, nDivs);
        this.codebook = new Codebook(divVec, codeWords);
        try {
            obs.writeInt(this.codebook.getAlphabet().size(), 8);
            for (int symbol : this.codebook.getAlphabet()) {
                obs.writeInt(symbol, 8);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        

        this.codebook.encode(obs);
    }

    public DynamicEncoder(Ibstream ibs) {
        int alphabetLen = ibs.readByte();
        ArrayList<Integer> alphabet = new ArrayList<>(alphabetLen);
        for (int i = 0; i < alphabetLen; i++) {
            alphabet.add(null);
        }
        for (int i = 0; i < alphabetLen; i++) {
            int temNum = (int) ibs.readByte();
            alphabet.set(i, temNum);
        }
        this.codebook = new Codebook(alphabet, ibs);
    }

    public void encode(Obstream obs, long num) {
        ArrayList<Integer> dividers = codebook.getAlphabet();
        int minLen = 0;
        String temStr = Long.toUnsignedString(num);
        double temNum = Double.valueOf(temStr);
        if (temNum > 0) {
            minLen = (int) (Math.log(temNum) / Math.log(2.0)) + 1;
        }
        int index = 0;
        while(dividers.get(index) < minLen) {
            index += 1;
        }
        String str = codebook.getCodewords().get(index);
        try {
            for (int i = 0; i < str.length(); i++) {
                obs.writeBit(str.charAt(i) != '0');
            }
            obs.writeInt(num, dividers.get(index));
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }

    public long decode(Ibstream ibs) {
        int numLen = codebook.lookup(ibs);
        return ibs.readInt(numLen);
    }

}
