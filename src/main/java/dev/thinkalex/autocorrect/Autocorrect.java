package dev.thinkalex.autocorrect;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Autocorrect
 * <p>
 * A command-line tool to suggest similar words when given one not in the dictionary.
 * </p>
 * @author Zach Blick
 * @author Alexandre Haddad-Delaveau
 */
@Service
public class Autocorrect {
    // Dictionary
    HashSet<String> dictionary;

    // Configuration
    private int editDistanceLimit;
    private int responseLimit;
    private final boolean ignoreValidWords;


    public Autocorrect(String[] words, int editDistanceLimit, int responseLimit, boolean ignoreValidWords) {
        // Load the dictionary
        dictionary = new HashSet<>();
        dictionary.addAll(Arrays.asList(words));

        // Save configuration
        this.editDistanceLimit = editDistanceLimit;
        this.responseLimit = responseLimit;
        this.ignoreValidWords = ignoreValidWords;
    }

    /**
     * Special constructor for Blick's test cases!
     *
     * @param words The dictionary of acceptable words.
     * @param limit The maximum number of edits a suggestion can have.
     */
    public Autocorrect(String[] words, int limit) {
        this(words, limit, Integer.MAX_VALUE, false);
    }

    /**
     * Special constructor for Autowiring.
     */
    public Autocorrect() {
        this(loadDictionary("large"), 2, 10, true);
    }

    /**
     * Runs a test from the tester file, AutocorrectTests.
     *
     * @param typed The (potentially) misspelled word, provided by the user.
     * @return An array of all dictionary words with an edit distance less than or equal
     * to a threshold, sorted by edit distance, then sorted alphabetically.
     */
    public String[] runTest(String typed) {
        List<Result> results = getTopResults(typed);
        String[] matches = new String[results.size()];
        for (int i = 0; i < results.size(); i++) {
            matches[i] = results.get(i).word;
        }

        return matches;
    }

    /**
     * Calculates the edit distance between two words
     * Time Complexity: O(n*m) where n is the length of word1 and m is the length of word2.
     * Space Complexity: Same as time complexity.
     *
     * @param word1 The first word.
     * @param word2 The second word.
     * @return The edit distance between the two words.
     */
    private int editDistance(String word1, String word2) {
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

        // Finish filling in the matrix
        for (int i = 1; i <= word1.length(); i++) {
            int min = Integer.MAX_VALUE;
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
                }
                min = Math.min(min, editDistances[i][j]);
            }

            // Early exit if the minimum edit distance is greater than the limit
            if (min > editDistanceLimit) {
                return Integer.MAX_VALUE;
            }
        }

        // Return the edit distance
        return editDistances[word1.length()][word2.length()] > editDistanceLimit ? Integer.MAX_VALUE : editDistances[word1.length()][word2.length()];
    }

    private static class Result {
        String word;
        int distance;

        public Result(String word, int distance) {
            this.word = word;
            this.distance = distance;
        }
    }

    private static final Comparator<Result> resultComparator = (r1, r2) -> {
        if (r1.distance == r2.distance) {
            return r1.word.compareTo(r2.word);
        }
        return r1.distance - r2.distance;
    };

    /**
     * Returns the top matches for a given word.
     *
     * @param word The word to find matches for.
     * @return A list of the top matches.
     */
    public List<Result> getTopResults(String word) {
        // Exit early if the word exists
        if (ignoreValidWords && dictionary.contains(word)) {
            return null;
        }

        // Store top words
        PriorityQueue<Result> pq = new PriorityQueue<>(10, resultComparator);

        // Go through all words in the dictionary
        for (String dictionaryWord : dictionary.stream().toList()) {
            pq.add(new Result(dictionaryWord, editDistance(dictionaryWord, word)));
        }

        // Return top matches
        List<Result> results = new ArrayList<>();
        while (!pq.isEmpty()) {
            // Break if we have enough results
            if (results.size() >= responseLimit) {
                break;
            }

            // Get the next result
            Result result = pq.poll();

            // Break if the edit distance is greater than the limit
            if (result.distance == Integer.MAX_VALUE) {
                break;
            }

            results.add(result);
        }

        return results;
    }

    public List<String> getTopStrings(String word) {
        List<Result> results = getTopResults(word);
        if (results == null) {
            return null;
        }
        List<String> matches = new ArrayList<>();
        for (Result result : results) {
            matches.add(result.word);
        }
        return matches;
    }

    /**
     * Debug function to print the 2D array of edit distances.
     *
     * @param arr   The array to print
     * @param word1 The first word
     * @param word2 The second word
     */
    private void print2DArray(int[][] arr, String word1, String word2) {
        System.out.println("Edit distance between " + word1 + " and " + word2 + ":");

        // Print the top row (word2 characters)
        System.out.print("    ");
        for (int j = 0; j < word2.length(); j++) {
            System.out.print(" " + word2.charAt(j) + " ");
        }
        System.out.println();

        // Print the matrix with word1 characters on the side
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                System.out.print(word1.charAt(i - 1) + " ");
            } else {
                System.out.print("  ");
            }
            for (int j = 0; j < arr[i].length; j++) {
                System.out.print(arr[i][j] + " ");
            }
            System.out.println();
        }
    }

    /**
     * Loads a dictionary of words from the provided textfiles in the dictionaries directory.
     * @param dictionary The name of the textfile, [dictionary].txt, in the dictionaries directory.
     * @return An array of Strings containing all words in alphabetical order.
     */
    private static String[] loadDictionary(String dictionary)  {
        try {
            String line;
            Resource dictionaryResource = new ClassPathResource("dictionaries/" + dictionary + ".txt");
            BufferedReader dictReader = new BufferedReader(new InputStreamReader(dictionaryResource.getInputStream()));
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

    // Setters / Getters for configuration
    public void setMaxEditDistance(int maxEditDistance) {
        this.editDistanceLimit = maxEditDistance;
    }

    public int getMaxEditDistance() {
        return editDistanceLimit;
    }

    public void setResponseLimit(int responseLimit) {
        this.responseLimit = responseLimit;
    }

    public int getResponseLimit() {
        return responseLimit;
    }
}