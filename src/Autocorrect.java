import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Autocorrect
 * <p>
 * A command-line tool to suggest similar words when given one not in the dictionary.
 * </p>
 * @author Zach Blick
 * @author Alexandre Haddad-Delaveau
 */
public class Autocorrect {

    public static void main(String[] args) {
        // Demo word
        String demoWord = "borange";

        // Load the dictionary
        String[] words = loadDictionary("large");

        // Store top words
        PriorityQueue<Result> pq = new PriorityQueue<>(10, resultComparator);

        for (String word : words) {
            pq.add(new Result(word, editDistance(demoWord, word)));
        }

        // Print the top 10 words
        for (int i = 0; i < 10; i++) {
            Result r = pq.poll();
            System.out.println(r.word + " " + r.distance);
        }

    }

    /**
     * Constucts an instance of the Autocorrect class.
     * @param words The dictionary of acceptable words.
     * @param limit The maximum number of edits a suggestion can have.
     */
    public Autocorrect(String[] words, int limit) {

    }

    /**
     * Runs a test from the tester file, AutocorrectTester.
     * @param typed The (potentially) misspelled word, provided by the user.
     * @return An array of all dictionary words with an edit distance less than or equal
     * to threshold, sorted by edit distnace, then sorted alphabetically.
     */
    public String[] runTest(String typed) {

        return new String[0];
    }


    /**
     * Loads a dictionary of words from the provided textfiles in the dictionaries directory.
     * @param dictionary The name of the textfile, [dictionary].txt, in the dictionaries directory.
     * @return An array of Strings containing all words in alphabetical order.
     */
    private static String[] loadDictionary(String dictionary)  {
        try {
            String line;
            BufferedReader dictReader = new BufferedReader(new FileReader("dictionaries/" + dictionary + ".txt"));
            line = dictReader.readLine();

            // Update instance variables with test data
            int n = Integer.parseInt(line);
            String[] words = new String[n];

            for (int i = 0; i < n; i++) {
                line = dictReader.readLine();
                words[i] = line;
            }
            return words;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calculates the edit distance between two words
     * Time Complexity: O(n*m) where n is the length of word1 and m is the length of word2.
     * Space Complexity: Same as time complexity.
     * @param word1 The first word.
     * @param word2 The second word.
     * @return The edit distance between the two words.
     */
    private static int editDistance(String word1, String word2, int limit) {
        // Special Case: if either word is empty
        if (word1.isEmpty()) {
            return word2.length();
        } else if (word2.isEmpty()) {
            return word1.length();
        }

        // Convert both strings to lowercase
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        // Create matrix to store edit distances
        int[][] editDistances = new int[word1.length() + 1][word2.length() + 1];

        // Fill in the first row and column
        for (int i = 0; i <= word2.length(); i++) {
            editDistances[0][i] = i;
        }

        for (int i = 0; i <= word1.length(); i++) {
            editDistances[i][0] = i;
        }

        // Fill the rest of the matrix
        for (int i = 1; i <= word1.length(); i++) {
            for (int j = 1; j <= word2.length(); j++) {
                // Check if the characters are the same
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    editDistances[i][j] = editDistances[i - 1][j - 1];
                } else {
                    // Find the minimum of the three possible operations
                    int insert = editDistances[i][j - 1] + 1;
                    int delete = editDistances[i - 1][j] + 1;
                    int replace = editDistances[i - 1][j - 1] + 1;
                    editDistances[i][j] = Math.min(insert, Math.min(delete, replace));

                    // Check if the limit has been reached
                    if (editDistances[i][j] > limit) {
                        return Integer.MAX_VALUE;
                    }
                }
            }
        }

        // Return the edit distance
        return editDistances[word1.length()][word2.length()];
    }

    private static int editDistance(String word1, String word2) {
        return editDistance(word1, word2, Integer.MAX_VALUE);
    }

    private static class Result {
        String word;
        int distance;

        public Result(String word, int distance) {
            this.word = word;
            this.distance = distance;
        }
    }

    private static Comparator<Result> resultComparator = new Comparator<Result>() {
        @Override
        public int compare(Result r1, Result r2) {
            if (r1.distance == r2.distance) {
                return r1.word.compareTo(r2.word);
            }
            return r1.distance - r2.distance;
        }
    };
}