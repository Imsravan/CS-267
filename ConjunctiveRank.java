import java.util.ArrayList;
import java.util.Map;

public class ConjunctiveRank {

   /**
    * Method that checks if the input parameters are proper.
    */
   private static boolean checkInputArgs(String[] args) {
      boolean isValid = false;

      if (args.length >= 3) {
         isValid = true;
      } else {
         isValid = false;
         System.out.println("Please enter all the inputs.\n" +
               "1. Text file name \n" +
               "2. Number of documents sorted in desc order of rank \n" +
               "3. Query ");
      }

      return isValid;
   }

   /**
    * Method that prints the result
    */
   private void displayResult(double[][] sortedDocumentScoreArray, int n) {
      if (null == sortedDocumentScoreArray || sortedDocumentScoreArray.length == 0) {
         System.out.println("no documents found!!");
      } else {
         for (int i = 0; i < sortedDocumentScoreArray.length && i < n; i++) {
            System.out.println("(" +
                  (((int) sortedDocumentScoreArray[i][0]) + 1)
                  + "," + sortedDocumentScoreArray[i][1] + ")");
         }
      }
   }

   public static void main(String[] args) {

      ConjunctiveRank search = new ConjunctiveRank();

      double[][] sortedDocumentScoreArray = null;

      if (true) {

         StringBuilder sb = new StringBuilder();

         for (int i = 2; i < args.length; i++) {
            sb.append(args[i]);
            sb.append(" ");
         }

         // String query = sb.toString();
         // String fileName = args[0];
         // int n = Integer.valueOf(args[1]);

         String query = "quarrel sir";
         String fileName = "test.txt";
         int n = 5;

         cosineRankCalculator cosineRank = new cosineRankCalculator();

         cosineRank.generateIndex(fileName);

         String q[] = query.split("\\s+");

         ArrayList<String> st = new ArrayList<>();

         for (int i = 0; i < q.length; i++) {
            st.add(q[i]);
         }

         cosineRank.rankBM25DocumentAtATime(st, n);

         IndexCompression ic = new IndexCompression();

         search.displayResult(sortedDocumentScoreArray, n);

      }

   }

}
