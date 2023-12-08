import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexProgram {

    static void encode(Map<String, Map<String, postingList>> index_StatisticMap) throws IOException {
        Map<String, postingList> indexMap = index_StatisticMap.get("index");

        Map<String, postingList> statisticMap = index_StatisticMap.get("statistic");

        String baseFileName = "index";
        String txtFilePath = baseFileName + ".txt";
        String dicFilePath = baseFileName + ".dic";

        // The part where you handle the items to be compressed goes here

        int totalDoc = statisticMap.get("#").getTotalDocument();
        Compressor dic_comp = new Compressor();
        FileWriter outfile_dic = new FileWriter("index.dic");

        // Create BufferedWriter object to provide buffering
        BufferedWriter bufferedWriter = new BufferedWriter(outfile_dic);

        int longestDocLength = 0;

        for (String s : statisticMap.keySet()) {
            if (!s.equals("#")) {
                longestDocLength = Math.max(longestDocLength, statisticMap.get(s).getTermCount());
            }
        }
        String dicContent = "";
        dicContent = dic_comp.appendGamma(totalDoc, dicContent);
        double t = Math.ceil(Math.log(longestDocLength) / Math.log(2)) + 1;
        dicContent = dic_comp.appendGamma((int) t, dicContent);
        for (String s : statisticMap.keySet()) {
            if (!s.equals("#")) {
                int docLen = statisticMap.get(s).getTermCount();
                dicContent = dic_comp.appendBits(docLen, dicContent, (int) t);
            }
        }
        bufferedWriter.write(dicContent);
        bufferedWriter.close();

        StringBuilder line3 = new StringBuilder();
        StringBuilder line2 = new StringBuilder();
        StringBuilder line1 = new StringBuilder();
        int pos = 0;
        Compressor line1_comp = new Compressor();
        Compressor line2_comp = new Compressor();
        Compressor line3_comp = new Compressor();
        ArrayList<Integer> line1_offsets = new ArrayList<>();

        ArrayList<String> sortedTerms = new ArrayList<>();

        for (String term : indexMap.keySet()) {
            sortedTerms.add(term);
        }

        Collections.sort(sortedTerms);

        for (String term : sortedTerms) {
            line1_offsets.add(line2_comp.getStartBitOffset());
            int num_docs = indexMap.get(term).getDocumentCount();
            // int mod = 2;
            // line3 = line3_comp.vbyteEncode(num_docs, line3);
            // line3 = line3_comp.vbyteEncode(mod, line3);
            StringBuilder adjusted_term = new StringBuilder();
            adjusted_term.append(term);
            adjusted_term.append('\0');
            line2.append(adjusted_term);
            int sbo = line2_comp.getStartBitOffset();
            sbo += 8 * adjusted_term.toString().length();
            line2_comp.setStartBitOffset(sbo);
            line2 = line2_comp.vbyteEncode(num_docs, line2);

            int mod = 2;
            line3 = line3_comp.vbyteEncode(num_docs, line3);
            line3 = line3_comp.vbyteEncode(mod, line3);

            Map<Integer, Integer> docFreq = indexMap.get(term).getDocTermsCountMap();

            String l3 = line3_comp.appendRiceSequence(docFreq, mod, line3.toString(), -1);
            line3 = new StringBuilder(l3);
            Map<Integer, Integer> postingList = indexMap.get(term).getDocTermsCountMap();
            Map<Integer, List<Integer>> postingListDocumentIndexes = indexMap.get(term).getDocTermsIndexMap();

            // for (Integer doc : postingList.keySet()) {
            // String l = line3_comp.appendGamma(postingList.get(doc), line3.toString());
            // line3 = new StringBuilder(l);
            // ArrayList<Integer> occurances = new ArrayList<>();
            // for (Integer i : postingListDocumentIndexes.keySet()) {
            // occurances.add(i);
            // }
            // l = line3_comp.appendRiceSequence(occurances, mod, line3.toString(), -1);
            // line3 = new StringBuilder(l);
            // }
            line2 = line2_comp.vbyteEncode(line3_comp.getStartBitOffset(), line2);
            String temp = Integer.toString(line2_comp.getStartBitOffset());
            // line1.append(temp);
        }

        // int offset1 = 8 * (line1.toString().length());
        int offset2 = line2_comp.getStartBitOffset();

        double w = Math.ceil(Math.log(offset2) / Math.log(2)) + 1;

        for (Integer offset : line1_offsets) {
            String l1 = line1_comp.appendBits(offset, line1.toString(), (int) w);
            line1 = new StringBuilder(l1);
        }

        int offset1 = line1_comp.getStartBitOffset();

        Compressor line0_comp = new Compressor();

        String line0 = "";
        line0 = line0_comp.appendGamma(offset1, line0);
        line0 = line0_comp.appendGamma(offset2, line0);
        line0 = line0_comp.appendGamma((int) w, line0);

        try {
            // Create FileWriter object with filePath
            FileWriter fileWriter = new FileWriter("index.txt");

            // Create BufferedWriter object to provide buffering
            bufferedWriter = new BufferedWriter(fileWriter);

            // Write content to the file
            // bufferedWriter.write(offset1);
            // bufferedWriter.write(offset2);
            bufferedWriter.write(line0);
            bufferedWriter.write("\n");

            bufferedWriter.write(line1.toString());
            bufferedWriter.write("\n");

            bufferedWriter.write(line2.toString());
            bufferedWriter.write("\n");

            bufferedWriter.write(line3.toString());
            bufferedWriter.write("\n");

            // Always close the writer
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // System.out.println(offset1);
        System.out.println(line0);
        // System.out.println(offset2);
        // System.out.println();

        // System.out.println(w);
        System.out.println();
        System.out.println(line1.toString());
        System.out.println();

        System.out.println(line2.toString());
        System.out.println();

        System.out.println(line3.toString());
        System.out.println();

    }

    // public static Map<Integer, Integer> getDocumentFreq(Map<String, postingList>
    // indexMap, String term) {
    // Map<Integer, Integer> docTermCountList =
    // indexMap.get(term).getDocTermsCountMap();

    // for (String doc : statisticMap.keySet()) {
    // postingList pl = statisticMap.get(doc);
    // Integer docId = Integer.parseInt(doc);
    // map.put(docId, statisticMap.get(doc).getTermCount());
    // }
    // return map;
    // }

    public static void main(String[] args) throws IOException {
        String fileName = "corpus.txt";

        String indexFile = "index.txt";

        PostingListGenerator plg = new PostingListGenerator();

        Map<String, Map<String, postingList>> index_StatisticMap = plg.generateIndex(fileName);

        encode(index_StatisticMap);
    }

}