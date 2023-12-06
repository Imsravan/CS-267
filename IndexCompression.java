import java.util.Map;

public class IndexCompression {
    public static String compressToGammaCode(int number) {
        String binary = Integer.toBinaryString(number);
        String unary = "0".repeat(binary.length() - 1) + "1";
        return unary + " " + binary;
    }

    public void fetchDocumentDetails(Map<Integer, Integer> documentTermMap) {
        int totalDocCount = documentTermMap.size();
        System.out.println("Total Documents: " + compressToGammaCode(totalDocCount));

        for (Integer i : documentTermMap.keySet()) {
            System.out.println("Docid: " + i + " Document Size " + compressToGammaCode(documentTermMap.get(i)) + " "
                    + documentTermMap.get(i));
        }
    }

}
